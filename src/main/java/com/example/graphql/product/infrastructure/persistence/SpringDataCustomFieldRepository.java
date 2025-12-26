package com.example.graphql.product.infrastructure.persistence;

import com.example.graphql.product.domain.model.CustomFieldDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SpringDataCustomFieldRepository extends JpaRepository<CustomFieldDefinition, Long> {
    List<CustomFieldDefinition> findByEntityType(String entityType);
}
