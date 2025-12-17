package com.example.graphql.repository.jpa;

import com.example.graphql.Publication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicationRepository extends JpaRepository<Publication, Long> {
}
