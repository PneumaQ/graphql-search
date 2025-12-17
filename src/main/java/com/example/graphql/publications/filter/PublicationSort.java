package com.example.graphql.publications.filter;

import com.example.graphql.platform.filter.SortDirection;

public record PublicationSort(PublicationSortField field, SortDirection direction) {}
