package com.example.graphql.platform.search;

import com.example.graphql.platform.metadata.EntityCfg;
import com.example.graphql.platform.metadata.PropertyCfg;
import com.example.graphql.platform.metadata.PropertyCfgRepository;
import com.example.graphql.platform.filter.SearchCondition;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class UniversalQueryBuilder {

    private static final Logger log = LoggerFactory.getLogger(UniversalQueryBuilder.class);
    private final PropertyCfgRepository propertyCfgRepository;

    public UniversalQueryBuilder(PropertyCfgRepository propertyCfgRepository) {
        this.propertyCfgRepository = propertyCfgRepository;
    }

    public org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep build(
            SearchPredicateFactory f, List<SearchCondition> conditions, EntityCfg rootEntity) {
        
        if (conditions == null || conditions.isEmpty()) {
            return f.matchAll();
        }

        BooleanPredicateClausesStep<?> bool = f.bool();
        for (SearchCondition cond : conditions) {
            bool.must(buildRecursive(f, cond, rootEntity));
        }
        return bool;
    }

    private org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep buildRecursive(
            SearchPredicateFactory f, SearchCondition cond, EntityCfg rootEntity) {
        
        BooleanPredicateClausesStep<?> bool = f.bool();
        boolean hasClause = false;

        // 1. Handle Recursive AND
        if (cond.getAnd() != null && !cond.getAnd().isEmpty()) {
            for (SearchCondition sub : cond.getAnd()) {
                bool.must(buildRecursive(f, sub, rootEntity));
            }
            hasClause = true;
        }

        // 2. Handle Recursive OR
        if (cond.getOr() != null && !cond.getOr().isEmpty()) {
            BooleanPredicateClausesStep<?> orBool = f.bool();
            for (SearchCondition sub : cond.getOr()) {
                orBool.should(buildRecursive(f, sub, rootEntity));
            }
            bool.must(orBool);
            hasClause = true;
        }

        // 3. Handle Leaf Node (Field Condition)
        if (cond.getField() != null) {
            // Find property globally, then check if it's reachable from this root
            List<PropertyCfg> properties = propertyCfgRepository.findByPropertyName(cond.getField());
            
            // Find the one that belongs to our aggregate (direct parent or traversable)
            Optional<PropertyCfg> propOpt = properties.stream()
                .filter(p -> p.getParentEntity().getName().equalsIgnoreCase(rootEntity.getName()) || 
                             rootEntity.getPropertyRepresentingEntity(p.getParentEntity()) != null)
                .findFirst();

            if (propOpt.isPresent()) {
                PropertyCfg prop = propOpt.get();
                String resolvedPath = prop.getDotPath(rootEntity);
                log.info("Resolved Filter: {} -> {}", cond.getField(), resolvedPath);
                applyCondition(f, bool, resolvedPath, cond, prop);
            } else if (cond.getField().contains(".")) {
                // Fallback for direct paths
                log.info("Using Direct Path: {}", cond.getField());
                applyCondition(f, bool, cond.getField(), cond, null);
            }
            hasClause = true;
        }

        return hasClause ? bool : f.matchAll();
    }

    private void applyCondition(SearchPredicateFactory f, BooleanPredicateClausesStep<?> bool, String path, SearchCondition cond, PropertyCfg prop) {
        // Range Operators
        if (cond.getGt() != null) bool.must(f.range().field(path).greaterThan(convertValue(cond.getGt(), prop)));
        if (cond.getLt() != null) bool.must(f.range().field(path).lessThan(convertValue(cond.getLt(), prop)));
        if (cond.getGte() != null) bool.must(f.range().field(path).atLeast(convertValue(cond.getGte(), prop)));
        if (cond.getLte() != null) bool.must(f.range().field(path).atMost(convertValue(cond.getLte(), prop)));

        // Equality
        if (cond.getEq() != null) {
            bool.must(f.match().field(path).matching(convertValue(cond.getEq(), prop)));
        }

        // StartsWith
        if (cond.getStartsWith() != null) {
            bool.must(f.wildcard().field(path).matching(cond.getStartsWith().toLowerCase() + "*"));
        }

        // In
        if (cond.getIn() != null && !cond.getIn().isEmpty()) {
            List<Object> values = cond.getIn().stream().map(v -> convertValue(v, prop)).toList();
            bool.must(f.terms().field(path).matchingAny(values));
        }
        
        // Full-Text
        if (cond.getContains() != null) {
            String textPath = path.endsWith("_keyword") ? path.replace("_keyword", "_text") : path;
            bool.must(f.wildcard().field(textPath).matching("*" + cond.getContains().toLowerCase() + "*"));
        }
    }

    private Object convertValue(Object rawValue, PropertyCfg prop) {
        if (rawValue == null) return null;
        if (prop == null) return rawValue;

        String stringValue = rawValue.toString();
        String dataType = (prop.getDataType() != null) ? prop.getDataType().toUpperCase() : "STRING";
        
        return switch (dataType) {
            case "INT", "INTEGER" -> (int) Double.parseDouble(stringValue);
            case "DOUBLE", "FLOAT" -> Double.parseDouble(stringValue);
            case "BOOLEAN" -> Boolean.parseBoolean(stringValue);
            default -> stringValue;
        };
    }
}
