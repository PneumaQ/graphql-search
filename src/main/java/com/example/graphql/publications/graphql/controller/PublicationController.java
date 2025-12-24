package com.example.graphql.publications.graphql.controller;

import com.example.graphql.publications.model.Publication;
import com.example.graphql.publications.service.PublicationService;
import com.example.graphql.publications.graphql.filter.PublicationFilterInput;
import com.example.graphql.publications.graphql.filter.PublicationSort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

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
    public PublicationService.PublicationSearchResponse searchPublications(
            @Argument String text,
            @Argument PublicationFilterInput filter,
            @Argument List<PublicationSort> sort,
            @Argument Integer page,
            @Argument Integer size) {
        
        return publicationService.searchPublications(text, filter, sort, page, size);
    }
}