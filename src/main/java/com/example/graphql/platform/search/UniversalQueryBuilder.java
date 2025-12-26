package com.example.graphql.platform.search;

import com.example.graphql.platform.metadata.EntityCfg;
import com.example.graphql.platform.metadata.PropertyCfg;
import com.example.graphql.platform.metadata.PropertyCfgRepository;
import com.example.graphql.platform.filter.SearchConditionInput;
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
            SearchPredicateFactory f, List<SearchConditionInput> conditions, EntityCfg rootEntity) {
        
        if (conditions == null || conditions.isEmpty()) {
            return f.matchAll();
        }

        BooleanPredicateClausesStep<?> bool = f.bool();
        for (SearchConditionInput cond : conditions) {
            bool.must(buildRecursive(f, cond, rootEntity));
        }
        return bool;
    }

    private org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep buildRecursive(
            SearchPredicateFactory f, SearchConditionInput cond, EntityCfg rootEntity) {
        
        BooleanPredicateClausesStep<?> bool = f.bool();
        boolean hasClause = false;

        if (cond.getAnd() != null && !cond.getAnd().isEmpty()) {
            for (SearchConditionInput sub : cond.getAnd()) {
                bool.must(buildRecursive(f, sub, rootEntity));
            }
            hasClause = true;
        }

        if (cond.getOr() != null && !cond.getOr().isEmpty()) {
            BooleanPredicateClausesStep<?> orBool = f.bool();
            for (SearchConditionInput sub : cond.getOr()) {
                orBool.should(buildRecursive(f, sub, rootEntity));
            }
            bool.must(orBool);
            hasClause = true;
        }

        if (cond.getField() != null) {
            List<PropertyCfg> properties = propertyCfgRepository.findByPropertyName(cond.getField());
            
            Optional<PropertyCfg> propOpt = properties.stream()
                .filter(p -> p.getParentEntity().getName().equalsIgnoreCase(rootEntity.getName()) || 
                             rootEntity.getPropertyRepresentingEntity(p.getParentEntity()) != null)
                .findFirst();

            if (propOpt.isPresent()) {
                PropertyCfg prop = propOpt.get();
                String resolvedPath = prop.getDotPath(rootEntity);
                log.info("[Registry X-Ray] Resolved Logical Field '{}' -> Technical Path '{}' (DataType: {})", cond.getField(), resolvedPath, prop.getDataType());
                applyCondition(f, bool, resolvedPath, cond, prop);
            } else if (cond.getField().contains(".")) {
                log.info("[Registry X-Ray] Using Direct/Custom Path: '{}'", cond.getField());
                applyCondition(f, bool, cond.getField(), cond, null);
            }
            hasClause = true;
        }

        return hasClause ? bool : f.matchAll();
    }

    private void applyCondition(SearchPredicateFactory f, BooleanPredicateClausesStep<?> bool, String path, SearchConditionInput cond, PropertyCfg prop) {
        if (cond.getGt() != null) bool.must(f.range().field(path).greaterThan(convertValue(cond.getGt(), prop)));
        if (cond.getLt() != null) bool.must(f.range().field(path).lessThan(convertValue(cond.getLt(), prop)));
        if (cond.getGte() != null) bool.must(f.range().field(path).atLeast(convertValue(cond.getGte(), prop)));
        if (cond.getLte() != null) bool.must(f.range().field(path).atMost(convertValue(cond.getLte(), prop)));

        if (cond.getEq() != null) {
            bool.must(f.match().field(path).matching(convertValue(cond.getEq(), prop)));
        }

        if (cond.getStartsWith() != null) {
            bool.must(f.wildcard().field(path).matching(cond.getStartsWith().toLowerCase() + "*"));
        }

        if (cond.getIn() != null && !cond.getIn().isEmpty()) {
            List<Object> values = cond.getIn().stream().map(v -> convertValue(v, prop)).toList();
            bool.must(f.terms().field(path).matchingAny(values));
        }
        
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