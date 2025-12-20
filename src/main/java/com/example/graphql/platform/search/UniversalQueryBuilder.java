package com.example.graphql.platform.search;

import com.example.graphql.product.filter.SearchCondition;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UniversalQueryBuilder {

    public org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep build(
            SearchPredicateFactory f, List<SearchCondition> conditions) {
        
        if (conditions == null || conditions.isEmpty()) {
            return f.matchAll();
        }

        BooleanPredicateClausesStep<?> bool = f.bool();

        for (SearchCondition cond : conditions) {
            String fieldPath = getFieldPath(cond.field());
            applyCondition(f, bool, fieldPath, cond);
        }

        return bool;
    }

    private String getFieldPath(String field) {
        String lower = field.toLowerCase();
        return switch (lower) {
            case "name" -> "name_keyword";
            case "sku" -> "sku_keyword";
            case "category" -> "category_keyword";
            case "price" -> "price";
            case "rating" -> "reviews.rating";
            default -> "custom_attributes." + field + "_keyword";
        };
    }

    private void applyCondition(SearchPredicateFactory f, BooleanPredicateClausesStep<?> bool, String path, SearchCondition cond) {
        // String Operators
        if (cond.eq() != null) {
            Object value = cond.eq();
            if (path.endsWith("rating")) {
                value = Integer.parseInt(cond.eq());
            } else if (path.equals("price")) {
                value = Double.parseDouble(cond.eq());
            }
            bool.must(f.match().field(path).matching(value));
        }
        if (cond.contains() != null) {
            bool.must(f.wildcard().field(path).matching("*" + cond.contains().toLowerCase() + "*"));
        }
        if (cond.startsWith() != null) {
            bool.must(f.wildcard().field(path).matching(cond.startsWith().toLowerCase() + "*"));
        }
        if (cond.in() != null && !cond.in().isEmpty()) {
            bool.must(f.terms().field(path).matchingAny(cond.in()));
        }

        // Numeric Operators
        if (cond.gt() != null) {
            bool.must(f.range().field(path).greaterThan(cond.gt()));
        }
        if (cond.lt() != null) {
            bool.must(f.range().field(path).lessThan(cond.lt()));
        }
        if (cond.gte() != null) {
            bool.must(f.range().field(path).atLeast(cond.gte()));
        }
        if (cond.lte() != null) {
            bool.must(f.range().field(path).atMost(cond.lte()));
        }
        
        // Handle Numeric strings passed via SearchCondition (if needed)
        // Note: Our SearchCondition uses Double for gt/lt already, 
        // but if we were using strings for values, we'd parse them here.
    }
}
