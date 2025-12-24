package com.example.graphql.platform.security;

import com.example.graphql.platform.metadata.EntityCfg;
import com.example.graphql.platform.metadata.EntityCfgRepository;
import com.example.graphql.platform.metadata.PropertyCfg;
import com.example.graphql.platform.metadata.PropertyCfgRepository;
import com.example.graphql.platform.filter.SearchCondition;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

@Service
public class DacService {

    private final EntityManager entityManager;

    public DacService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<SearchCondition> getSecurityConditions(String entityName) {
        // 1. Fetch the Root Entity Metadata
        EntityCfg root = entityManager.createQuery(
            "SELECT e FROM EntityCfg e WHERE e.name = :name", EntityCfg.class)
            .setParameter("name", entityName)
            .getSingleResult();

        // 2. Fetch all active DACs for this entity
        List<DacCfg> activeDacs = entityManager.createQuery(
            "SELECT d FROM DacCfg d WHERE d.targetEntity.name = :name AND d.active = true", DacCfg.class)
            .setParameter("name", entityName)
            .getResultList();

        List<SearchCondition> securityFilters = new ArrayList<>();

        for (DacCfg dac : activeDacs) {
            for (DacConditionCfg cond : dac.getConditions()) {
                PropertyCfg prop = cond.getProperty();
                String logicalName = prop.getPropertyName(); // Use logical name instead of dotPath
                String val = cond.getConditionValues().get(0);

                SearchCondition.SearchConditionBuilder builder = SearchCondition.builder().field(logicalName);

                // Map operators explicitly to avoid constructor errors
                switch (cond.getOperator().toUpperCase()) {
                    case "EQ" -> builder.eq(val);
                    case "GTE" -> builder.gte(Double.parseDouble(val));
                    case "GT" -> builder.gt(Double.parseDouble(val));
                    case "LTE" -> builder.lte(Double.parseDouble(val));
                    case "LT" -> builder.lt(Double.parseDouble(val));
                }

                securityFilters.add(builder.build());
            }
        }

        return securityFilters;
    }
}
