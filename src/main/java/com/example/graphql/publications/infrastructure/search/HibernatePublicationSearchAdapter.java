package com.example.graphql.publications.infrastructure.search;

import com.example.graphql.publications.domain.model.Publication;
import com.example.graphql.publications.domain.repository.PublicationSearchRepository;
import com.example.graphql.platform.filter.SearchConditionInput;
import com.example.graphql.platform.search.UniversalQueryBuilder;
import com.example.graphql.platform.logging.QueryContext;
import com.example.graphql.platform.metadata.EntityCfg;
import com.example.graphql.platform.metadata.EntityCfgRepository;
import com.example.graphql.platform.metadata.PropertyCfg;
import com.example.graphql.platform.metadata.PropertyCfgRepository;
import com.example.graphql.platform.filter.SortDirection;
import com.example.graphql.publications.graphql.input.PublicationSortInput;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.hibernate.search.engine.search.aggregation.AggregationKey;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.sort.dsl.SearchSortFactory;
import org.hibernate.search.engine.search.sort.dsl.SortFinalStep;
import org.hibernate.search.engine.search.query.SearchResult;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public class HibernatePublicationSearchAdapter implements PublicationSearchRepository {

    private static final Logger log = LoggerFactory.getLogger(HibernatePublicationSearchAdapter.class);
    private final EntityManager entityManager;
    private final UniversalQueryBuilder queryBuilder;
    private final EntityCfgRepository entityCfgRepository;
    private final PropertyCfgRepository propertyCfgRepository;

    public HibernatePublicationSearchAdapter(EntityManager entityManager, 
                                   UniversalQueryBuilder queryBuilder,
                                   EntityCfgRepository entityCfgRepository,
                                   PropertyCfgRepository propertyCfgRepository) {
        this.entityManager = entityManager;
        this.queryBuilder = queryBuilder;
        this.entityCfgRepository = entityCfgRepository;
        this.propertyCfgRepository = propertyCfgRepository;
    }

    @Override
    public PublicationSearchInternalResponse search(String text, List<SearchConditionInput> filter, List<String> facetKeys, List<String> statsKeys, List<PublicationSortInput> sort, int page, int size) {
        SearchSession searchSession = Search.session(entityManager);
        EntityCfg rootEntity = entityCfgRepository.findByName("Publication").orElseThrow();

        var query = searchSession.search(Publication.class)
            .where(f -> f.bool(b -> {
                b.must(queryBuilder.build(f, filter, rootEntity));
                applyFullTextSearch(f, b, text, rootEntity);
            }))
            .sort(f -> applySort(f, sort, rootEntity));

        // Configure Facets with Type Awareness
        Map<String, AggregationKey<Map<?, Long>>> facetAggKeys = new HashMap<>();
        if (facetKeys != null) {
            for (String key : facetKeys) {
                PropertyCfg prop = findProperty(key, rootEntity);
                String path = prop.getDotPath(rootEntity);
                Class<?> clazz = getPropertyClass(prop);
                
                var aggKey = AggregationKey.<Map<?, Long>>of(key);
                facetAggKeys.put(key, aggKey);
                query.aggregation(aggKey, f -> f.terms().field(path, (Class)clazz).maxTermCount(20));
            }
        }

        // Configure Stats
        Map<String, AggregationKey<Map<?, Long>>> statsAggKeys = new HashMap<>();
        if (statsKeys != null) {
            for (String key : statsKeys) {
                PropertyCfg prop = findProperty(key, rootEntity);
                String path = prop.getDotPath(rootEntity);
                Class<?> clazz = getPropertyClass(prop);
                
                var aggKey = AggregationKey.<Map<?, Long>>of("stats_" + key);
                statsAggKeys.put(key, aggKey);
                query.aggregation(aggKey, f -> f.terms().field(path, (Class)clazz));
            }
        }

        QueryContext.set("Hibernate Search - Phase 2 (Entity Loading)");
        var result = query.fetch(page * size, size);
        
        return new PublicationSearchInternalResponse(
            result.hits(),
            mapFacetResults(result, facetAggKeys),
            mapStatsResults(result, statsAggKeys),
            result.total().hitCount(),
            (int) Math.ceil((double) result.total().hitCount() / size)
        );
    }

    private PropertyCfg findProperty(String propertyName, EntityCfg rootEntity) {
        return rootEntity.getProperties().stream()
            .filter(p -> p.getPropertyName().equalsIgnoreCase(propertyName))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Property not found: " + propertyName));
    }

    private Class<?> getPropertyClass(PropertyCfg prop) {
        String dataType = (prop.getDataType() != null) ? prop.getDataType().toUpperCase() : "STRING";
        return switch (dataType) {
            case "INT", "INTEGER" -> Integer.class;
            case "DOUBLE", "FLOAT" -> Double.class;
            case "BOOLEAN" -> Boolean.class;
            default -> String.class;
        };
    }

    private void applyFullTextSearch(SearchPredicateFactory f, BooleanPredicateClausesStep<?> bool, String text, EntityCfg rootEntity) {
        if (text == null || text.isBlank()) return;

        java.util.Set<String> fields = new java.util.HashSet<>();
        
        for (PropertyCfg prop : rootEntity.getProperties()) {
            if ("STRING".equalsIgnoreCase(prop.getDataType())) {
                fields.add(prop.getDotPath(rootEntity));
            }
        }
        
        if (!fields.isEmpty()) {
            var textQuery = f.simpleQueryString().fields(fields.toArray(new String[0]));
            bool.must(textQuery.matching(text));
        }
    }

    private SortFinalStep applySort(SearchSortFactory f, List<PublicationSortInput> sort, EntityCfg rootEntity) {
        if (sort == null || sort.isEmpty()) return f.score();
        
        var composite = f.composite();
        for (PublicationSortInput s : sort) {
            PropertyCfg prop = findProperty(s.field().name(), rootEntity);
            String path = prop.getDotPath(rootEntity);
            var fieldSort = f.field(path);
            if (s.direction() == SortDirection.DESC) fieldSort.desc(); else fieldSort.asc();
            composite.add(fieldSort);
        }
        return composite;
    }

    private Map<String, Map<?, Long>> mapFacetResults(SearchResult<Publication> result, Map<String, AggregationKey<Map<?, Long>>> aggKeys) {
        Map<String, Map<?, Long>> facetResults = new HashMap<>();
        for (var entry : aggKeys.entrySet()) {
            facetResults.put(entry.getKey(), result.aggregation(entry.getValue()));
        }
        return facetResults;
    }

    private Map<String, Object> mapStatsResults(SearchResult<Publication> result, Map<String, AggregationKey<Map<?, Long>>> statsAggKeys) {
        Map<String, Object> statsResults = new HashMap<>();
        for (var entry : statsAggKeys.entrySet()) {
            Map<?, Long> dist = result.aggregation(entry.getValue());
            statsResults.put(entry.getKey(), calculateNumericStats(dist));
        }
        return statsResults;
    }

    private Map<String, Object> calculateNumericStats(Map<?, Long> dist) {
        if (dist.isEmpty()) return Map.of();
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        double sum = 0;
        long count = 0;

        for (Map.Entry<?, Long> entry : dist.entrySet()) {
            try {
                double val = Double.parseDouble(entry.getKey().toString());
                long freq = entry.getValue();
                if (val < min) min = val;
                if (val > max) max = val;
                sum += (val * freq);
                count += freq;
            } catch (Exception e) { }
        }

        if (count == 0) return Map.of();
        Map<String, Object> s = new HashMap<>();
        s.put("min", min);
        s.put("max", max);
        s.put("avg", sum / count);
        s.put("sum", sum);
        s.put("count", count);
        return s;
    }
}
