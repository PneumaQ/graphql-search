package com.example.graphql.platform.search;

import com.example.graphql.platform.filter.*;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.springframework.stereotype.Component;
import java.lang.reflect.RecordComponent;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ElasticsearchQueryBuilder {

    public Query build(Object filter) {
        if (filter == null) return null;

        BoolQuery.Builder bool = new BoolQuery.Builder();
        boolean hasClauses = false;

        try {
            // Iterate over all fields in the filter record
            Class<?> clazz = filter.getClass();
            if (!clazz.isRecord()) {
                throw new IllegalArgumentException("Filter object must be a Java Record");
            }

            for (RecordComponent component : clazz.getRecordComponents()) {
                String fieldName = component.getName();
                Object value = component.getAccessor().invoke(filter);

                if (value == null) continue;

                // Handle Recursive Logic (AND/OR/NOT)
                if (fieldName.equals("and") && value instanceof List<?> list) {
                    for (Object sub : list) {
                        Query q = build(sub);
                        if (q != null) { bool.must(q); hasClauses = true; }
                    }
                } else if (fieldName.equals("or") && value instanceof List<?> list) {
                    for (Object sub : list) {
                        Query q = build(sub);
                        if (q != null) { bool.should(q); hasClauses = true; }
                    }
                    if (hasClauses) bool.minimumShouldMatch("1");
                } else if (fieldName.equals("not")) {
                    Query q = build(value);
                    if (q != null) { bool.mustNot(q); hasClauses = true; }
                } 
                // Handle Typed Filters (Leaf Nodes)
                else if (value instanceof StringFilter sf) {
                    bool.must(buildStringQuery(resolveField(fieldName), sf));
                    hasClauses = true;
                } else if (value instanceof IntFilter inf) {
                    bool.must(buildIntQuery(resolveField(fieldName), inf));
                    hasClauses = true;
                } else if (value instanceof FloatFilter ff) {
                    bool.must(buildFloatQuery(resolveField(fieldName), ff));
                    hasClauses = true;
                } else if (value instanceof DateFilter df) {
                    bool.must(buildDateQuery(resolveField(fieldName), df));
                    hasClauses = true;
                } else if (value instanceof BooleanFilter bf) {
                    if (bf.eq() != null) {
                        bool.must(q -> q.term(t -> t.field(resolveField(fieldName)).value(bf.eq())));
                        hasClauses = true;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error building Elasticsearch query via reflection", e);
        }

        return hasClauses ? Query.of(q -> q.bool(bool.build())) : null;
    }

    private String resolveField(String inputName) {
        // Convention: Replace "_" with "." to handle nesting
        // e.g., "addresses_city" -> "addresses.city"
        return inputName.replace("_", ".");
    }

    private Query buildStringQuery(String fieldPath, StringFilter filter) {
        // Automatic Keyword Detection:
        // Ideally, we check mapping. For POC, we assume any field *might* have a .keyword subfield 
        // OR we just query the field directly if it's nested or ID.
        // Simple Heuristic: If it's "id" or nested (contains "."), use raw field. Else try .keyword for Exact/Sorting.
        
        String exactField = (fieldPath.equals("id") || fieldPath.contains(".")) ? fieldPath : fieldPath + ".keyword";
        // Correction: Spring Data ES usually maps nested fields as just "path.field". 
        // Nested keyword fields are usually "path.field.keyword".
        // For this Generic Builder, we will try the ".keyword" suffix for all exact operations unless it's "id".
        if (!fieldPath.equals("id") && !fieldPath.endsWith(".keyword")) {
             // For nested fields like addresses.city, we ideally want addresses.city.keyword.
             // But if the mapping isn't keyword, this might fail. 
             // Let's stick to the .keyword convention for all strings except 'id'.
             exactField = fieldPath + ".keyword";
        }
        if (fieldPath.equals("id")) exactField = "id";

        String searchField = exactField; // Use keyword for exact matching by default

        // Logic for Nested Query Wrapping
        // If the path contains ".", we MUST wrap it in a 'nested' query logic?
        // Wait, 'nested' query in ES is only needed if the mapping type is 'nested'.
        // 'addresses' is 'nested'. So "addresses.city" query MUST be wrapped in nested path="addresses".
        
        // This is complex for reflection. 
        // SOLUTION: If path contains ".", split it. Root is path.
        if (fieldPath.contains(".")) {
            String[] parts = fieldPath.split("\\.", 2);
            String rootPath = parts[0]; // "addresses"
            final String finalExactField = exactField; 
            
            // Construct the inner query
            Query innerQuery = buildStringQueryLogic(finalExactField, filter);
            
            // Wrap in nested
            return Query.of(q -> q.nested(n -> n.path(rootPath).query(innerQuery)));
        }

        return buildStringQueryLogic(searchField, filter);
    }

    private Query buildStringQueryLogic(String field, StringFilter filter) {
        BoolQuery.Builder b = new BoolQuery.Builder();
        boolean hasFilter = false;
        
        if (filter.eq() != null) {
            b.must(q -> q.term(t -> t.field(field).value(filter.eq())));
            hasFilter = true;
        }
        if (filter.contains() != null) {
            // Wildcard doesn't support .keyword well for tokenized text, but usually ok for simple contains
            b.must(q -> q.wildcard(w -> w.field(field).value("*" + filter.contains() + "*")));
            hasFilter = true;
        }
        if (filter.startsWith() != null) {
            b.must(q -> q.prefix(p -> p.field(field).value(filter.startsWith())));
            hasFilter = true;
        }
        if (filter.endsWith() != null) {
            b.must(q -> q.wildcard(w -> w.field(field).value("*" + filter.endsWith())));
            hasFilter = true;
        }
        if (filter.in() != null && !filter.in().isEmpty()) {
            List<co.elastic.clients.elasticsearch._types.FieldValue> values = filter.in().stream()
                .map(co.elastic.clients.elasticsearch._types.FieldValue::of)
                .collect(Collectors.toList());
            b.must(q -> q.terms(t -> t.field(field).terms(v -> v.value(values))));
            hasFilter = true;
        }
        return hasFilter ? Query.of(q -> q.bool(b.build())) : Query.of(q -> q.matchAll(m -> m));
    }

    private Query buildIntQuery(String field, IntFilter filter) {
        return Query.of(q -> q.range(r -> r.term(t -> {
            t.field(field);
            if (filter.eq() != null) t.gte(String.valueOf(filter.eq())).lte(String.valueOf(filter.eq()));
            if (filter.gt() != null) t.gt(String.valueOf(filter.gt()));
            if (filter.gte() != null) t.gte(String.valueOf(filter.gte()));
            if (filter.lt() != null) t.lt(String.valueOf(filter.lt()));
            if (filter.lte() != null) t.lte(String.valueOf(filter.lte()));
            return t;
        })));
    }

    private Query buildFloatQuery(String field, FloatFilter filter) {
        return Query.of(q -> q.range(r -> r.term(t -> {
            t.field(field);
            if (filter.eq() != null) t.gte(String.valueOf(filter.eq())).lte(String.valueOf(filter.eq()));
            if (filter.gt() != null) t.gt(String.valueOf(filter.gt()));
            if (filter.lt() != null) t.lt(String.valueOf(filter.lt()));
            return t;
        })));
    }

    private Query buildDateQuery(String field, DateFilter filter) {
        return Query.of(q -> q.range(r -> r.term(t -> {
            t.field(field);
            if (filter.eq() != null) t.gte(filter.eq()).lte(filter.eq());
            if (filter.gt() != null) t.gt(filter.gt());
            if (filter.gte() != null) t.gte(filter.gte());
            if (filter.lt() != null) t.lt(filter.lt());
            if (filter.lte() != null) t.lte(filter.lte());
            return t;
        })));
    }
}