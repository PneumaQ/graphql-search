package com.example.graphql.product.repository;

import com.example.graphql.product.model.CustomFieldDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomFieldRepository extends JpaRepository<CustomFieldDefinition, Long> {
    List<CustomFieldDefinition> findByEntityType(String entityType);
}
