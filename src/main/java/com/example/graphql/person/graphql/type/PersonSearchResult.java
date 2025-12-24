package com.example.graphql.person.graphql.type;

import com.example.graphql.person.model.Person;
import java.util.List;

public record PersonSearchResult(List<Person> results, Object facets, Object stats, int totalElements, int totalPages) {}
