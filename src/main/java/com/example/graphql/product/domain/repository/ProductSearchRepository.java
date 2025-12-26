package com.example.graphql.product.domain.repository;

import com.example.graphql.product.domain.model.Product;
import com.example.graphql.platform.filter.SearchConditionInput;
import com.example.graphql.product.graphql.input.ProductSortInput;
import java.util.List;
import java.util.Map;

public interface ProductSearchRepository {
    ProductSearchInternalResponse search(String text, List<SearchConditionInput> filter, List<String> facetKeys, List<String> statsKeys, List<ProductSortInput> sort, int page, int size);

    record ProductSearchInternalResponse(List<Product> results, Map<String, Map<?, Long>> facets, Map<String, Object> stats, long totalElements, int totalPages) {}
}
