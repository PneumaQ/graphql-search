package com.example.graphql.platform.security;

import com.example.graphql.platform.metadata.EntityCfg;
import com.example.graphql.platform.metadata.EntityCfgRepository;
import com.example.graphql.platform.metadata.PropertyCfg;
import com.example.graphql.platform.metadata.PropertyCfgRepository;
import com.example.graphql.platform.filter.SearchConditionInput;
import com.example.graphql.platform.search.UniversalQueryBuilder;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DacService {

    private final EntityCfgRepository entityCfgRepository;
    private final DacCfgRepository dacCfgRepository;
    private final UniversalQueryBuilder queryBuilder;

    public DacService(EntityCfgRepository entityCfgRepository, 
                      DacCfgRepository dacCfgRepository,
                      UniversalQueryBuilder queryBuilder) {
        this.entityCfgRepository = entityCfgRepository;
        this.dacCfgRepository = dacCfgRepository;
        this.queryBuilder = queryBuilder;
    }

    public SearchPredicate getSecurityPredicate(SearchPredicateFactory f, EntityCfg rootEntity) {
        List<SearchConditionInput> conditions = getSecurityConditions(rootEntity.getName());
        if (conditions.isEmpty()) {
            return f.matchAll().toPredicate();
        }
        return queryBuilder.build(f, conditions, rootEntity).toPredicate();
    }

    public List<SearchConditionInput> getSecurityConditions(String entityName) {
        List<DacCfg> dacs = dacCfgRepository.findAll().stream()
                .filter(d -> d.isActive() && d.getTargetEntity().getName().equalsIgnoreCase(entityName))
                .toList();

        List<SearchConditionInput> conditions = new ArrayList<>();
        for (DacCfg dac : dacs) {
            for (DacConditionCfg cond : dac.getConditions()) {
                SearchConditionInput searchCond = new SearchConditionInput();
                
                PropertyCfg prop = cond.getProperty();
                searchCond.setField(prop.getPropertyName());
                
                if ("EQ".equalsIgnoreCase(cond.getOperator())) {
                    searchCond.setEq(cond.getConditionValues().get(0));
                } else if ("IN".equalsIgnoreCase(cond.getOperator())) {
                    searchCond.setIn(cond.getConditionValues());
                }
                
                conditions.add(searchCond);
            }
        }
        return conditions;
    }
}