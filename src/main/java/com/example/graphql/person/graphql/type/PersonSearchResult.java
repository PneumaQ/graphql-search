package com.example.graphql.person.graphql.type;

import com.example.graphql.person.domain.model.Person;
import java.util.List;
import java.util.Map;

public record PersonSearchResult(
    List<Person> results,
    Object facets,
    Object stats,
    int totalElements,
    int totalPages
) {}