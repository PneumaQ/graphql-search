package com.example.graphql.person.graphql.filter;

import com.example.graphql.platform.filter.SortDirection;

public record PersonSort(String field, SortDirection direction) {}