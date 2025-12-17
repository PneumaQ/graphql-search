package com.example.graphql.person.repository.jpa;

import com.example.graphql.person.model.Person;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    java.util.List<Person> findByAddresses_City(String city);
}
