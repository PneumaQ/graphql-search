package com.example.graphql.publications.domain.repository;

import com.example.graphql.publications.domain.model.Publication;
import java.util.List;
import java.util.Optional;

public interface PublicationRepository {
    Publication save(Publication publication);
    Optional<Publication> findById(Long id);
    List<Publication> findAll();
    void deleteById(Long id);
    void deleteAll();
}
