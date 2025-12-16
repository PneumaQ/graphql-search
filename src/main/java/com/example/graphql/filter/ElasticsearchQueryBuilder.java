package com.example.graphql.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ElasticsearchQueryBuilder {

    public Query build(PersonFilterInput filter) {
        if (filter == null) return null;

        BoolQuery.Builder bool = new BoolQuery.Builder();
        boolean hasClauses = false;

        // 1. Recursive Logic
        if (filter.and() != null) {
            for (PersonFilterInput sub : filter.and()) {
                Query q = build(sub);
                if (q != null) { bool.must(q); hasClauses = true; }
            }
        }
        if (filter.or() != null) {
            for (PersonFilterInput sub : filter.or()) {
                Query q = build(sub);
                if (q != null) { bool.should(q); hasClauses = true; }
            }
            if (hasClauses) bool.minimumShouldMatch("1");
        }
        if (filter.not() != null) {
            Query q = build(filter.not());
            if (q != null) { bool.mustNot(q); hasClauses = true; }
        }

        // 2. Root Fields
        if (filter.name() != null) {
            bool.must(buildStringQuery("name", "name.keyword", filter.name()));
            hasClauses = true;
        }
        if (filter.email() != null) {
            bool.must(buildStringQuery("email", "email.keyword", filter.email()));
            hasClauses = true;
        }
        if (filter.isActive() != null) {
            if (filter.isActive().eq() != null) {
                bool.must(q -> q.term(t -> t.field("isActive").value(filter.isActive().eq())));
                hasClauses = true;
            }
        }
        if (filter.age() != null) {
            bool.must(buildIntQuery("age", filter.age()));
            hasClauses = true;
        }
        if (filter.salary() != null) {
            bool.must(buildFloatQuery("salary", filter.salary()));
            hasClauses = true;
        }

        // 3. Nested Fields
        if (filter.address_country() != null) {
            bool.must(buildNestedQuery("addresses", "addresses.country", "addresses.country.keyword", filter.address_country()));
            hasClauses = true;
        }
        if (filter.address_state() != null) {
            bool.must(buildNestedQuery("addresses", "addresses.state", "addresses.state.keyword", filter.address_state()));
            hasClauses = true;
        }
        if (filter.address_city() != null) {
            // City has no keyword sub-field, so we target the text field directly for 'eq' (exact match on token) or 'contains'
            bool.must(buildNestedQuery("addresses", "addresses.city", "addresses.city", filter.address_city()));
            hasClauses = true;
        }

        return hasClauses ? Query.of(q -> q.bool(bool.build())) : null;
    }

    private Query buildStringQuery(String textField, String keywordField, StringFilter filter) {
        BoolQuery.Builder b = new BoolQuery.Builder();
        boolean hasFilter = false;
        
        if (filter.eq() != null) {
            b.must(q -> q.term(t -> t.field(keywordField != null ? keywordField : textField).value(filter.eq())));
            hasFilter = true;
        }
        if (filter.contains() != null) {
            b.must(q -> q.wildcard(w -> w.field(keywordField != null ? keywordField : textField).value("*" + filter.contains() + "*")));
            hasFilter = true;
        }
        if (filter.in() != null && !filter.in().isEmpty()) {
            List<co.elastic.clients.elasticsearch._types.FieldValue> values = filter.in().stream()
                .map(co.elastic.clients.elasticsearch._types.FieldValue::of)
                .collect(Collectors.toList());
            b.must(q -> q.terms(t -> t.field(keywordField != null ? keywordField : textField).terms(v -> v.value(values))));
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

    private Query buildNestedQuery(String path, String textField, String keywordField, StringFilter filter) {
        return Query.of(q -> q.nested(n -> n
            .path(path)
            .query(buildStringQuery(textField, keywordField, filter))
        ));
    }
}