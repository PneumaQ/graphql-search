package com.example.graphql.publications.controller;

import com.example.graphql.publications.service.PublicationService;
import com.example.graphql.publications.model.Publication;
import com.example.graphql.publications.filter.PublicationFilterInput;
import com.example.graphql.publications.filter.PublicationSort;
import com.example.graphql.platform.filter.SortDirection;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PublicationController {

    private final PublicationService publicationService;

    public PublicationController(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @QueryMapping
    public List<Publication> publications() {
        return publicationService.findAll();
    }

    @QueryMapping
    public PublicationSearchResult searchPublications(
            @Argument String text,
            @Argument PublicationFilterInput filter,
            @Argument Integer page,
            @Argument Integer size,
            @Argument List<PublicationSort> sort) {
        
        int pageNum = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 10;
        
        org.springframework.data.domain.Sort springSort = org.springframework.data.domain.Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            List<org.springframework.data.domain.Sort.Order> orders = sort.stream()
                .map(s -> {
                    String fieldName = switch (s.field()) {
                        case TITLE -> "title_keyword";
                        case PUBLICATION_DATE -> "publicationDate";
                        case JOURNAL -> "journalName_keyword";
                        case STATUS -> "status_keyword";
                    };
                    org.springframework.data.domain.Sort.Direction direction = (s.direction() == SortDirection.DESC) 
                        ? org.springframework.data.domain.Sort.Direction.DESC 
                        : org.springframework.data.domain.Sort.Direction.ASC;
                    return new org.springframework.data.domain.Sort.Order(direction, fieldName);
                })
                .collect(Collectors.toList());
            springSort = org.springframework.data.domain.Sort.by(orders);
        }
        
        PublicationService.PublicationSearchResponse response = publicationService.searchWithFacets(text, filter, org.springframework.data.domain.PageRequest.of(pageNum, pageSize, springSort));
        
        List<FacetBucket> status = mapFacets(response.statusCounts());
        List<FacetBucket> journal = mapFacets(response.journalCounts());
        
        return new PublicationSearchResult(
            response.results(), 
            new PublicationFacets(status, journal),
            (int) response.totalElements(),
            response.totalPages()
        );
    }

    private List<FacetBucket> mapFacets(java.util.Map<String, Long> counts) {
        if (counts == null) return java.util.Collections.emptyList();
        return counts.entrySet().stream()
                .map(e -> new FacetBucket(e.getKey(), e.getValue().intValue()))
                .collect(Collectors.toList());
    }

    public record PublicationSearchResult(List<Publication> results, PublicationFacets facets, int totalElements, int totalPages) {}
    public record PublicationFacets(List<FacetBucket> byStatus, List<FacetBucket> byJournal) {}
    public record FacetBucket(String value, int count) {}

    @MutationMapping
    public Publication createPublication(
            @Argument String title,
            @Argument String journalName,
            @Argument String publicationDate,
            @Argument String status,
            @Argument String doi) {
        
        LocalDate date = (publicationDate != null) ? LocalDate.parse(publicationDate) : null;
        return publicationService.createPublication(title, journalName, date, status, doi);
    }

    @MutationMapping
    public Publication addAuthorToPublication(
            @Argument Long publicationId,
            @Argument Long personId,
            @Argument Integer rank,
            @Argument Boolean isCorresponding,
            @Argument String affiliation) {
        return publicationService.addAuthor(publicationId, personId, rank, isCorresponding, affiliation);
    }
}
