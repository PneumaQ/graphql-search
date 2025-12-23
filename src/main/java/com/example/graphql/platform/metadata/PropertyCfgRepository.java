package com.example.graphql.platform.metadata;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PropertyCfgRepository extends JpaRepository<PropertyCfg, Long> {
    Optional<PropertyCfg> findByPropertyNameAndParentEntityName(String propertyName, String entityName);
    
    // Find by logical name anywhere in the registry
    List<PropertyCfg> findByPropertyName(String propertyName);
}