package com.example.graphql.person.repository.search;

import com.example.graphql.person.model.Person;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface PersonSearchRepository extends ElasticsearchRepository<Person, Long> {
    List<Person> findByNameContaining(String name);
}
