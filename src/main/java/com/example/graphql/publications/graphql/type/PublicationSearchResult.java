package com.example.graphql.publications.graphql.type;

import com.example.graphql.publications.domain.model.Publication;
import java.util.List;
import java.util.Map;

public record PublicationSearchResult(List<Publication> results, Object facets, Object stats, int totalElements, int totalPages) {}
