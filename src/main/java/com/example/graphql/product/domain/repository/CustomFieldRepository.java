package com.example.graphql.product.domain.repository;

import com.example.graphql.product.domain.model.CustomFieldDefinition;
import java.util.List;
import java.util.Optional;

public interface CustomFieldRepository {
    CustomFieldDefinition save(CustomFieldDefinition definition);
    Optional<CustomFieldDefinition> findById(Long id);
    List<CustomFieldDefinition> findAll();
    List<CustomFieldDefinition> findByEntityType(String entityType);
    void deleteById(Long id);
    void deleteAll();
}
