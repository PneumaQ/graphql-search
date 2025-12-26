package com.example.graphql.publications.infrastructure.persistence;

import com.example.graphql.publications.domain.model.Publication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPublicationRepository extends JpaRepository<Publication, Long> {
}
