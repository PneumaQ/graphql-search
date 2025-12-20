package com.example.graphql.product.repository.search;

import com.example.graphql.product.model.Product;
import com.example.graphql.product.filter.SearchCondition;
import com.example.graphql.platform.search.UniversalQueryBuilder;
import com.example.graphql.product.filter.ProductSort;
import com.example.graphql.platform.filter.SortDirection;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.hibernate.search.engine.search.aggregation.AggregationKey;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

@Repository
public class ProductSearchRepository {

    private final EntityManager entityManager;
    private final UniversalQueryBuilder queryBuilder;

    public ProductSearchRepository(EntityManager entityManager, UniversalQueryBuilder queryBuilder) {
        this.entityManager = entityManager;
        this.queryBuilder = queryBuilder;
    }

    public ProductSearchResponse search(String text, List<SearchCondition> filter, List<String> facetKeys, List<String> statsKeys, List<ProductSort> sort, int page, int size) {
        SearchSession searchSession = Search.session(entityManager);
        
        Set<String> dynamicTextFields = fetchDynamicTextFields(entityManager);

        var query = searchSession.search(Product.class)
            .where(f -> f.bool(b -> {
                b.must(queryBuilder.build(f, filter));
                
                if (text != null && !text.isBlank()) {
                    var textQuery = f.simpleQueryString()
                        .field("name")
                        .field("sku")
                        .field("category_keyword"); // Changed to category_keyword for Lucene
                    for (String field : dynamicTextFields) {
                        textQuery.field(field);
                    }
                    b.must(textQuery.matching(text));
                }
            }))
            .sort(f -> {
                if (sort == null || sort.isEmpty()) return f.score();
                var composite = f.composite();
                for (ProductSort s : sort) {
                    String path = getSortPath(s.field());
                    var fieldSort = f.field(path);
                    if (s.direction() == SortDirection.DESC) fieldSort.desc(); else fieldSort.asc();
                    composite.add(fieldSort);
                }
                return composite;
            });

        // Dynamic Facets
        Map<String, AggregationKey<Map<String, Long>>> aggKeys = new HashMap<>();
        if (facetKeys != null) {
            for (String key : facetKeys) {
                String path = getFacetPath(key);
                var aggKey = AggregationKey.<Map<String, Long>>of(key);
                aggKeys.put(key, aggKey);
                query.aggregation(aggKey, f -> f.terms().field(path, String.class).maxTermCount(20));
            }
        }

        // Dynamic Stats (Manual calculation for Lucene compatibility)
        Map<String, AggregationKey<Map<Object, Long>>> statsAggKeys = new HashMap<>();
        if (statsKeys != null) {
            for (String key : statsKeys) {
                String path = getStatsPath(key);
                var aggKey = AggregationKey.<Map<Object, Long>>of("stats_" + key);
                statsAggKeys.put(key, aggKey);
                // Use terms to get distribution for manual min/max/avg
                query.aggregation(aggKey, f -> f.terms().field(path, Object.class));
            }
        }

        var result = query.fetch(page * size, size);
        
        // Map Facets
        Map<String, Map<String, Long>> facetResults = new HashMap<>();
        for (var entry : aggKeys.entrySet()) {
            facetResults.put(entry.getKey(), result.aggregation(entry.getValue()));
        }

        // Map Stats
        Map<String, Object> statsResults = new HashMap<>();
        for (var entry : statsAggKeys.entrySet()) {
            Map<Object, Long> dist = result.aggregation(entry.getValue());
            statsResults.put(entry.getKey(), calculateNumericStats(dist));
        }

        return new ProductSearchResponse(result.hits(), facetResults, statsResults, result.total().hitCount(), (int) Math.ceil((double) result.total().hitCount() / size));
    }

    private String getSortPath(String field) {
        return switch (field.toLowerCase()) {
            case "name" -> "name_keyword";
            case "sku" -> "sku_keyword";
            case "category" -> "category_keyword";
            case "price" -> "price";
            default -> "custom_attributes." + field + "_keyword";
        };
    }

    private String getFacetPath(String field) {
        return switch (field.toLowerCase()) {
            case "category" -> "category_keyword";
            default -> "custom_attributes." + field + "_keyword";
        };
    }

    private String getStatsPath(String field) {
        return switch (field.toLowerCase()) {
            case "price" -> "price";
            case "rating" -> "reviews.rating";
            default -> "custom_attributes." + field; // Custom attributes are strings, so stats won't work well without conversion
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

    public record ProductSearchResponse(List<Product> results, Map<String, Map<String, Long>> facets, Map<String, Object> stats, long totalElements, int totalPages) {}

    private Set<String> fetchDynamicTextFields(EntityManager em) {
        try {
            jakarta.persistence.Query query = em.createNativeQuery("SELECT custom_attributes FROM PRODUCT");
            List<Object> results = query.getResultList();
            Set<String> fieldPaths = new HashSet<>();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            for (Object res : results) {
                try {
                    Map<String, Object> map = mapper.readValue(res.toString(), Map.class);
                    for (String key : map.keySet()) fieldPaths.add("custom_attributes." + key + "_text");
                } catch (Exception ignored) {}
            }
            return fieldPaths;
        } catch (Exception e) { return Set.of(); }
    }
}
