package com.example.graphql.person.domain.repository;

import com.example.graphql.person.domain.model.Person;
import com.example.graphql.platform.filter.SearchConditionInput;
import java.util.List;
import java.util.Map;

public interface PersonSearchRepository {
    PersonSearchInternalResponse search(String text, List<SearchConditionInput> filter, List<String> facetKeys, List<String> statsKeys, int page, int size);

    record PersonSearchInternalResponse(
        List<Person> results, 
        Map<String, Map<?, Long>> facets, 
        Map<String, Object> stats, 
        long totalElements, 
        int totalPages
    ) {}
}
