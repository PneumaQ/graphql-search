package com.example.graphql.repository.search;

import com.example.graphql.Person;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface PersonSearchRepository extends ElasticsearchRepository<Person, Long> {
    List<Person> findByNameContaining(String name);
}
