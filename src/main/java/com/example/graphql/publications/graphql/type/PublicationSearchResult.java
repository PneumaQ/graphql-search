package com.example.graphql.publications.graphql.type;

import com.example.graphql.publications.model.Publication;
import java.util.List;
import java.util.Map;

public record PublicationSearchResult(
    List<Publication> results,
    Map<String, Long> statusCounts,
    Map<String, Long> journalCounts,
    long totalElements,
    int totalPages
) {}
