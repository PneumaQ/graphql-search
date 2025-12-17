package com.example.graphql.publications.service;

import com.example.graphql.publications.repository.jpa.PublicationRepository;
import com.example.graphql.publications.repository.search.PublicationSearchRepository;
import com.example.graphql.person.repository.jpa.PersonRepository;
import com.example.graphql.publications.model.Publication;
import com.example.graphql.publications.model.PublicationAuthor;
import com.example.graphql.person.model.Person;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.graphql.publications.filter.PublicationFilterInput;
import org.springframework.data.domain.Pageable;
import java.util.Map;
import java.time.LocalDate;
import java.util.List;

@Service
public class PublicationService {

    private final PublicationRepository publicationRepository;
    private final PublicationSearchRepository publicationSearchRepository;
    private final PersonRepository personRepository;

    public PublicationService(PublicationRepository publicationRepository, 
                              PublicationSearchRepository publicationSearchRepository,
                              PersonRepository personRepository) {
        this.publicationRepository = publicationRepository;
        this.publicationSearchRepository = publicationSearchRepository;
        this.personRepository = personRepository;
    }

    public List<Publication> findAll() {
        return publicationRepository.findAll();
    }
    
    public List<Publication> searchPublications(String text) {
        return publicationSearchRepository.searchPublications(text);
    }

    public PublicationSearchResponse searchWithFacets(String text, PublicationFilterInput filter, Pageable pageable) {
        return publicationSearchRepository.searchWithFacets(text, filter, pageable);
    }

    public record PublicationSearchResponse(
        List<Publication> results,
        Map<String, Long> statusCounts,
        Map<String, Long> journalCounts,
        long totalElements,
        int totalPages
    ) {}

    @Transactional
    public Publication createPublication(String title, String journalName, LocalDate date, String status, String doi) {
        Publication pub = new Publication();
        pub.setTitle(title);
        pub.setJournalName(journalName);
        pub.setPublicationDate(date);
        pub.setStatus(status);
        pub.setDoi(doi);
        
        return publicationRepository.save(pub);
    }

    @Transactional
    public Publication addAuthor(Long publicationId, Long personId, Integer rank, Boolean isCorresponding, String affiliation) {
        Publication pub = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new RuntimeException("Publication not found"));
        
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new RuntimeException("Person not found"));

        PublicationAuthor author = new PublicationAuthor();
        author.setPublication(pub);
        author.setPerson(person);
        author.setRank(rank);
        author.setIsCorresponding(isCorresponding);
        author.setAffiliationAtTimeOfPublication(affiliation);

        pub.getAuthors().add(author);
        
        return publicationRepository.save(pub);
    }
}