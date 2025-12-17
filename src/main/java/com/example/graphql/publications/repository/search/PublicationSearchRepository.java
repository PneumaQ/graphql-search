package com.example.graphql.publications.repository.search;

import com.example.graphql.publications.model.Publication;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PublicationSearchRepository extends ElasticsearchRepository<Publication, Long> {
}
