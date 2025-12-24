package com.example.graphql.publications.graphql.input;

import com.example.graphql.platform.filter.SortDirection;

public record PublicationSortInput(PublicationSortField field, SortDirection direction) {}
