package com.example.graphql.repository.search;

import com.example.graphql.Publication;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PublicationSearchRepository extends ElasticsearchRepository<Publication, Long> {
}
