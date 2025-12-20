package com.example.graphql.product.filter;

import com.example.graphql.platform.filter.SortDirection;

public record ProductSort(String field, SortDirection direction) {}