package com.example.graphql.publications.repository.jpa;

import com.example.graphql.publications.model.Publication;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicationRepository extends JpaRepository<Publication, Long> {
}
