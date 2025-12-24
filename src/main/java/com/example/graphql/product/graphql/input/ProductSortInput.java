package com.example.graphql.product.graphql.input;

import com.example.graphql.platform.filter.SortDirection;

public record ProductSortInput(String field, SortDirection direction) {}