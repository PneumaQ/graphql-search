package com.example.graphql.platform.search;

import com.example.graphql.product.filter.SearchCondition;
import com.example.graphql.product.model.CustomFieldDefinition;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UniversalQueryBuilder {

    public org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep build(
            SearchPredicateFactory f, List<SearchCondition> conditions, List<CustomFieldDefinition> customFields) {
        
        if (conditions == null || conditions.isEmpty()) {
            return f.matchAll();
        }

        Map<String, CustomFieldDefinition> fieldMap = customFields.stream()
                .collect(Collectors.toMap(CustomFieldDefinition::getFieldKey, d -> d));

        BooleanPredicateClausesStep<?> bool = f.bool();
        for (SearchCondition cond : conditions) {
            bool.must(buildRecursive(f, cond, fieldMap));
        }
        return bool;
    }

    private org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep buildRecursive(
            SearchPredicateFactory f, SearchCondition cond, Map<String, CustomFieldDefinition> fieldMap) {
        
        BooleanPredicateClausesStep<?> bool = f.bool();
        boolean hasClause = false;

        // 1. Handle Recursive AND
        if (cond.and() != null && !cond.and().isEmpty()) {
            for (SearchCondition sub : cond.and()) {
                bool.must(buildRecursive(f, sub, fieldMap));
            }
            hasClause = true;
        }

        // 2. Handle Recursive OR
        if (cond.or() != null && !cond.or().isEmpty()) {
            BooleanPredicateClausesStep<?> orBool = f.bool();
            for (SearchCondition sub : cond.or()) {
                orBool.should(buildRecursive(f, sub, fieldMap));
            }
            bool.must(orBool);
            hasClause = true;
        }

        // 3. Handle Recursive NOT
        if (cond.not() != null) {
            bool.mustNot(buildRecursive(f, cond.not(), fieldMap));
            hasClause = true;
        }

        // 4. Handle Leaf Node (Field Condition)
        if (cond.field() != null) {
            CustomFieldDefinition def = fieldMap.get(cond.field());
            String path = getFieldPath(cond.field(), def);
            applyCondition(f, bool, path, cond, def);
            hasClause = true;
        }

        return hasClause ? bool : f.matchAll();
    }

    private String getFieldPath(String field, CustomFieldDefinition def) {
        String lower = field.toLowerCase();
        return switch (lower) {
            case "name" -> "name_keyword";
            case "sku" -> "sku_keyword";
            case "category" -> "category_keyword";
            case "price" -> "price";
            case "rating" -> "reviews.rating";
            default -> {
                if (def != null && def.getDataType() == CustomFieldDefinition.FieldDataType.STRING) {
                    yield "custom_attributes." + field + "_keyword";
                }
                yield "custom_attributes." + field;
            }
        };
    }

    private void applyCondition(SearchPredicateFactory f, BooleanPredicateClausesStep<?> bool, String path, SearchCondition cond, CustomFieldDefinition def) {
        // String/Equality Operators
        if (cond.eq() != null) {
            Object value = convertValue(cond.eq(), path, def);
            bool.must(f.match().field(path).matching(value));
        }
        
        // Only apply string-only operators if we have a string field
        if (isStringField(path, def)) {
            if (cond.contains() != null) {
                bool.must(f.wildcard().field(path).matching("*" + cond.contains().toLowerCase() + "*"));
            }
            if (cond.startsWith() != null) {
                bool.must(f.wildcard().field(path).matching(cond.startsWith().toLowerCase() + "*"));
            }
        }

        if (cond.in() != null && !cond.in().isEmpty()) {
            bool.must(f.terms().field(path).matchingAny(cond.in()));
        }

        // Range Operators
        if (cond.gt() != null) bool.must(f.range().field(path).greaterThan(cond.gt()));
        if (cond.lt() != null) bool.must(f.range().field(path).lessThan(cond.lt()));
        if (cond.gte() != null) bool.must(f.range().field(path).atLeast(cond.gte()));
        if (cond.lte() != null) bool.must(f.range().field(path).atMost(cond.lte()));
    }

    private boolean isStringField(String path, CustomFieldDefinition def) {
        if (path.contains("name") || path.contains("sku") || path.contains("category")) return true;
        return def != null && def.getDataType() == CustomFieldDefinition.FieldDataType.STRING;
    }

    private Object convertValue(String value, String path, CustomFieldDefinition def) {
        if (path.endsWith("rating")) return Integer.parseInt(value);
        if (path.equals("price")) return Double.parseDouble(value);
        
        if (def != null) {
            return switch (def.getDataType()) {
                case INT -> Integer.parseInt(value);
                case FLOAT -> Float.parseFloat(value);
                case DOUBLE -> Double.parseDouble(value);
                case BOOLEAN -> Boolean.parseBoolean(value);
                default -> value;
            };
        }
        return value;
    }
}
