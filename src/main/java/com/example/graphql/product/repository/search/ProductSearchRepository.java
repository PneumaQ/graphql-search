package com.example.graphql.product.repository.search;

import com.example.graphql.product.model.Product;
import com.example.graphql.product.model.CustomFieldDefinition;
import com.example.graphql.product.filter.SearchCondition;
import com.example.graphql.platform.search.UniversalQueryBuilder;
import com.example.graphql.product.filter.ProductSort;
import com.example.graphql.platform.filter.SortDirection;
import com.example.graphql.product.service.CustomFieldService;
import com.example.graphql.platform.logging.QueryContext;
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
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public class ProductSearchRepository {

    private static final Logger log = LoggerFactory.getLogger(ProductSearchRepository.class);
    private final EntityManager entityManager;
    private final UniversalQueryBuilder queryBuilder;
    private final CustomFieldService customFieldService;

    public ProductSearchRepository(EntityManager entityManager, UniversalQueryBuilder queryBuilder, CustomFieldService customFieldService) {
        this.entityManager = entityManager;
        this.queryBuilder = queryBuilder;
        this.customFieldService = customFieldService;
    }

    public ProductSearchInternalResponse search(String text, List<SearchCondition> filter, List<String> facetKeys, List<String> statsKeys, List<ProductSort> sort, int page, int size) {
        SearchSession searchSession = Search.session(entityManager);
        List<CustomFieldDefinition> customFields = customFieldService.getFieldDefinitions("PRODUCT");

        var query = searchSession.search(Product.class)
            .where(f -> f.bool(b -> {
                b.must(queryBuilder.build(f, filter, customFields));
                applyFullTextSearch(f, b, text, customFields);
            }))
            .sort(f -> applySort(f, sort, customFields));

        // Configure Facets
        Map<String, AggregationKey<Map<String, Long>>> facetAggKeys = new HashMap<>();
        if (facetKeys != null) {
            for (String key : facetKeys) {
                String path = getFacetPath(key, customFields);
                var aggKey = AggregationKey.<Map<String, Long>>of(key);
                facetAggKeys.put(key, aggKey);
                query.aggregation(aggKey, f -> f.terms().field(path, String.class).maxTermCount(20));
            }
        }

        // Configure Stats
        Map<String, AggregationKey<Map<Object, Long>>> statsAggKeys = new HashMap<>();
        if (statsKeys != null) {
            for (String key : statsKeys) {
                String path = getStatsPath(key, customFields);
                var aggKey = AggregationKey.<Map<Object, Long>>of("stats_" + key);
                statsAggKeys.put(key, aggKey);
                query.aggregation(aggKey, f -> f.terms().field(path, Object.class));
            }
        }

        QueryContext.set("Hibernate Search - Phase 2 (Entity Loading)");
        var result = query.fetch(page * size, size);
        QueryContext.set("Mapping Results");

        return new ProductSearchInternalResponse(
            result.hits(),
            mapFacetResults(result, facetAggKeys),
            mapStatsResults(result, statsAggKeys),
            result.total().hitCount(),
            (int) Math.ceil((double) result.total().hitCount() / size)
        );
    }

    private void applyFullTextSearch(SearchPredicateFactory f, BooleanPredicateClausesStep<?> bool, String text, List<CustomFieldDefinition> customFields) {
        if (text == null || text.isBlank()) return;

        Set<String> dynamicTextFields = customFields.stream()
                .filter(CustomFieldDefinition::isSearchable)
                .filter(d -> d.getDataType() == CustomFieldDefinition.FieldDataType.STRING)
                .map(d -> "custom_attributes." + d.getFieldKey() + "_text")
                .collect(Collectors.toSet());

        var textQuery = f.simpleQueryString()
                .field("name")
                .field("internalStockCode")
                .field("category_keyword")
                .field("brand.name")
                .field("reviews.comment");
        
        for (String fieldPath : dynamicTextFields) {
            textQuery.field(fieldPath);
        }
        
        bool.must(textQuery.matching(text));
    }

    private SortFinalStep applySort(SearchSortFactory f, List<ProductSort> sort, List<CustomFieldDefinition> customFields) {
        if (sort == null || sort.isEmpty()) return f.score();
        
        var composite = f.composite();
        for (ProductSort s : sort) {
            String path = getSortPath(s.field(), customFields);
            var fieldSort = f.field(path);
            if (s.direction() == SortDirection.DESC) fieldSort.desc(); else fieldSort.asc();
            composite.add(fieldSort);
        }
        return composite;
    }

    private Map<String, Map<String, Long>> mapFacetResults(SearchResult<Product> result, Map<String, AggregationKey<Map<String, Long>>> aggKeys) {
        Map<String, Map<String, Long>> facetResults = new HashMap<>();
        for (var entry : aggKeys.entrySet()) {
            facetResults.put(entry.getKey(), result.aggregation(entry.getValue()));
        }
        return facetResults;
    }

    private Map<String, Object> mapStatsResults(SearchResult<Product> result, Map<String, AggregationKey<Map<Object, Long>>> statsAggKeys) {
        Map<String, Object> statsResults = new HashMap<>();
        for (var entry : statsAggKeys.entrySet()) {
            Map<Object, Long> dist = result.aggregation(entry.getValue());
            statsResults.put(entry.getKey(), calculateNumericStats(dist));
        }
        return statsResults;
    }

    private String getSortPath(String field, List<CustomFieldDefinition> customFields) {
        String lower = field.toLowerCase();
        return switch (lower) {
            case "name" -> "name_keyword";
            case "sku" -> "sku_keyword";
            case "category" -> "category_keyword";
            case "price" -> "price";
            default -> {
                var def = customFields.stream().filter(d -> d.getFieldKey().equalsIgnoreCase(field)).findFirst().orElse(null);
                if (def != null && def.getDataType() == CustomFieldDefinition.FieldDataType.STRING) {
                    yield "custom_attributes." + field + "_keyword";
                }
                yield "custom_attributes." + field;
            }
        };
    }

    private String getFacetPath(String field, List<CustomFieldDefinition> customFields) {
        if (field.equalsIgnoreCase("category")) return "category_keyword";
        if (field.equalsIgnoreCase("sku")) return "sku_keyword";
        var def = customFields.stream().filter(d -> d.getFieldKey().equalsIgnoreCase(field)).findFirst().orElse(null);
        if (def != null && def.getDataType() == CustomFieldDefinition.FieldDataType.STRING) {
            return "custom_attributes." + field + "_keyword";
        }
        return "custom_attributes." + field;
    }

    private String getStatsPath(String field, List<CustomFieldDefinition> customFields) {
        return switch (field.toLowerCase()) {
            case "price" -> "price";
            case "rating" -> "reviews.rating";
            default -> "custom_attributes." + field;
        };
    }

    private Map<String, Object> calculateNumericStats(Map<Object, Long> dist) {
        if (dist.isEmpty()) return Map.of();
        
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;
        long count = 0;

        for (Map.Entry<Object, Long> entry : dist.entrySet()) {
            double val = Double.parseDouble(entry.getKey().toString());
            long freq = entry.getValue();
            if (val < min) min = val;
            if (val > max) max = val;
            sum += (val * freq);
            count += freq;
        }

        Map<String, Object> s = new HashMap<>();
        s.put("min", min);
        s.put("max", max);
        s.put("avg", count > 0 ? sum / count : 0);
        s.put("sum", sum);
        s.put("count", count);
        return s;
    }

    public record ProductSearchInternalResponse(List<Product> results, Map<String, Map<String, Long>> facets, Map<String, Object> stats, long totalElements, int totalPages) {}
}