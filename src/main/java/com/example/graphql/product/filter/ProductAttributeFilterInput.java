package com.example.graphql.product.filter;

import com.example.graphql.platform.filter.StringFilter;

public record ProductAttributeFilterInput(String key, StringFilter value) {}
