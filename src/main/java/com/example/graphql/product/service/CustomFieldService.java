package com.example.graphql.product.service;

import com.example.graphql.product.model.CustomFieldDefinition;
import com.example.graphql.product.repository.CustomFieldRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomFieldService {

    private final CustomFieldRepository repository;
    private final Cache<String, List<CustomFieldDefinition>> cache;

    public CustomFieldService(CustomFieldRepository repository) {
        this.repository = repository;
        this.cache = Caffeine.newBuilder()
                .maximumSize(100)
                .build();
    }

    public List<CustomFieldDefinition> getFieldDefinitions(String entityType) {
        return cache.get(entityType, k -> {
            com.example.graphql.platform.logging.QueryContext.set("Metadata Discovery");
            System.out.println("DEBUG: Cache miss for " + entityType + ". Fetching from database...");
            var results = repository.findByEntityType(k);
            com.example.graphql.platform.logging.QueryContext.clear();
            return results;
        });
    }

    public void evictCache(String entityType) {
        cache.invalidate(entityType);
    }
    
    public void saveDefinition(CustomFieldDefinition definition) {
        repository.save(definition);
        evictCache(definition.getEntityType());
    }
}
