package com.example.graphql.product.graphql.type;

import com.example.graphql.product.model.Product;
import java.util.List;

public record ProductSearchResult(List<Product> results, Object facets, Object stats, int totalElements, int totalPages) {}
