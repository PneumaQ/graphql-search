package com.example.graphql.product.graphql.filter;

import com.example.graphql.platform.filter.SortDirection;

public record ProductSort(String field, SortDirection direction) {}
