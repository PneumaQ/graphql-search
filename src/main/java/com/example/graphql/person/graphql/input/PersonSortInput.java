package com.example.graphql.person.graphql.input;

import com.example.graphql.platform.filter.SortDirection;

public record PersonSortInput(String field, SortDirection direction) {}
