package com.example.graphql.publications.service;

import com.example.graphql.publications.model.Publication;
import com.example.graphql.publications.model.PublicationAuthor;
import com.example.graphql.person.model.Person;
import com.example.graphql.publications.repository.jpa.PublicationRepository;
import com.example.graphql.publications.repository.search.PublicationSearchRepository;
import com.example.graphql.person.repository.jpa.PersonRepository;
import com.example.graphql.publications.graphql.input.PublicationFilterInput;
import com.example.graphql.publications.graphql.input.PublicationSortInput;
import com.example.graphql.publications.graphql.type.PublicationSearchResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    public PublicationSearchResult searchPublications(String text, PublicationFilterInput filter, List<PublicationSortInput> sort, Integer page, Integer size) {
        int pageNum = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 10;
        return publicationSearchRepository.searchWithFacets(text, filter, sort, pageNum, pageSize);
    }

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