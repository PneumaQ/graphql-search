package com.example.graphql.publications.graphql.controller;

import com.example.graphql.publications.domain.model.Publication;
import com.example.graphql.publications.domain.model.PublicationAuthor;
import com.example.graphql.publications.service.PublicationService;
import com.example.graphql.platform.filter.SearchConditionInput;
import com.example.graphql.publications.graphql.input.PublicationSortInput;
import com.example.graphql.publications.graphql.type.PublicationSearchResult;
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
    public PublicationSearchResult searchPublications(
            @Argument String text,
            @Argument List<SearchConditionInput> filter,
            @Argument List<PublicationSortInput> sort,
            @Argument Integer page,
            @Argument Integer size) {
        
        return publicationService.searchPublications(text, filter, sort, page, size);
    }
}