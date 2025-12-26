package com.example.graphql.publications.service;

import com.example.graphql.publications.domain.model.Publication;
import com.example.graphql.publications.domain.repository.PublicationRepository;
import com.example.graphql.publications.domain.repository.PublicationSearchRepository;
import com.example.graphql.platform.filter.SearchConditionInput;
import com.example.graphql.publications.graphql.input.PublicationSortInput;
import com.example.graphql.publications.graphql.type.PublicationSearchResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PublicationService {

    private final PublicationRepository publicationRepository;
    private final PublicationSearchRepository publicationSearchRepository;

    public PublicationService(PublicationRepository publicationRepository, PublicationSearchRepository publicationSearchRepository) {
        this.publicationRepository = publicationRepository;
        this.publicationSearchRepository = publicationSearchRepository;
    }

    @Transactional(readOnly = true)
    public List<Publication> findAll() {
        return publicationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PublicationSearchResult searchPublications(String text, List<SearchConditionInput> filter, List<PublicationSortInput> sort, Integer page, Integer size) {
        int pageNum = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 10;

        var repoResponse = publicationSearchRepository.search(text, filter, null, null, sort, pageNum, pageSize);
        
        return new PublicationSearchResult(
                repoResponse.results(), 
                repoResponse.facets(), 
                repoResponse.stats(), 
                (int) repoResponse.totalElements(), 
                repoResponse.totalPages()
        );
    }
}
