package com.example.graphql.person.domain.repository;

import com.example.graphql.person.domain.model.Person;
import java.util.List;
import java.util.Optional;

public interface PersonRepository {
    Person save(Person person);
    Optional<Person> findById(Long id);
    List<Person> findAll();
    void deleteById(Long id);
    void deleteAll();
}
