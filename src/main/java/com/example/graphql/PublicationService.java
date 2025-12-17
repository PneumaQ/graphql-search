package com.example.graphql;

import com.example.graphql.repository.jpa.PublicationRepository;
import com.example.graphql.repository.search.PublicationSearchRepository;
import com.example.graphql.repository.jpa.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import java.util.stream.Collectors;

@Service
public class PublicationService {

    private final PublicationRepository publicationRepository;
    private final PublicationSearchRepository publicationSearchRepository;
    private final PersonRepository personRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public PublicationService(PublicationRepository publicationRepository, 
                              PublicationSearchRepository publicationSearchRepository,
                              PersonRepository personRepository,
                              ElasticsearchOperations elasticsearchOperations) {
        this.publicationRepository = publicationRepository;
        this.publicationSearchRepository = publicationSearchRepository;
        this.personRepository = personRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public List<Publication> findAll() {
        return publicationRepository.findAll();
    }
    
    public List<Publication> searchPublications(String text) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .should(s -> s.match(m -> m.field("title").query(text)))
                        .should(s -> s.nested(n -> n
                                .path("authors")
                                .query(nq -> nq.bool(nb -> nb
                                    .should(ns -> ns.match(nm -> nm.field("authors.person.name").query(text))) // Assumes person is embedded
                                    .should(ns -> ns.match(nm -> nm.field("authors.affiliationAtTimeOfPublication").query(text)))
                                ))
                        ))
                ))
                .build();
        
        SearchHits<Publication> searchHits = elasticsearchOperations.search(query, Publication.class);
        return searchHits.stream().map(org.springframework.data.elasticsearch.core.SearchHit::getContent).collect(Collectors.toList());
    }

    @Transactional
    public Publication createPublication(String title, String journalName, LocalDate date, String status, String doi) {
        Publication pub = new Publication();
        pub.setTitle(title);
        pub.setJournalName(journalName);
        pub.setPublicationDate(date);
        pub.setStatus(status);
        pub.setDoi(doi);
        
        Publication saved = publicationRepository.save(pub);
        publicationSearchRepository.save(saved);
        return saved;
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
        
        Publication saved = publicationRepository.save(pub);
        
        // Critical: In a real system with Hibernate Search, this happens automatically. 
        // With Spring Data ES, we must explicitly re-save the Aggregate Root to update the index.
        publicationSearchRepository.save(saved);
        
        return saved;
    }
}
