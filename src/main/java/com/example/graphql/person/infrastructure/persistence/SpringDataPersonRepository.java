package com.example.graphql.person.infrastructure.persistence;

import com.example.graphql.person.domain.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataPersonRepository extends JpaRepository<Person, Long> {
}