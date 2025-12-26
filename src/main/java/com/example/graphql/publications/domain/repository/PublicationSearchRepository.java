package com.example.graphql.publications.domain.repository;

import com.example.graphql.publications.domain.model.Publication;
import com.example.graphql.platform.filter.SearchConditionInput;
import com.example.graphql.publications.graphql.input.PublicationSortInput;
import java.util.List;
import java.util.Map;

public interface PublicationSearchRepository {
    PublicationSearchInternalResponse search(String text, List<SearchConditionInput> filter, List<String> facetKeys, List<String> statsKeys, List<PublicationSortInput> sort, int page, int size);

    record PublicationSearchInternalResponse(List<Publication> results, Map<String, Map<?, Long>> facets, Map<String, Object> stats, long totalElements, int totalPages) {}
}
