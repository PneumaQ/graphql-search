package com.example.graphql.publications.controller;

import com.example.graphql.publications.service.PublicationService;
import com.example.graphql.publications.model.Publication;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import java.time.LocalDate;
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
    public List<Publication> searchPublications(@Argument String text) {
        return publicationService.searchPublications(text);
    }

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
