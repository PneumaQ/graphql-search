package com.example.graphql.product.infrastructure.persistence;

import com.example.graphql.product.domain.model.CustomFieldDefinition;
import com.example.graphql.product.domain.repository.CustomFieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaCustomFieldAdapter implements CustomFieldRepository {
    private final SpringDataCustomFieldRepository springDataRepository;

    @Override public CustomFieldDefinition save(CustomFieldDefinition definition) { return springDataRepository.save(definition); }
    @Override public Optional<CustomFieldDefinition> findById(Long id) { return springDataRepository.findById(id); }
    @Override public List<CustomFieldDefinition> findAll() { return springDataRepository.findAll(); }
    @Override public List<CustomFieldDefinition> findByEntityType(String entityType) { return springDataRepository.findByEntityType(entityType); }
    @Override public void deleteById(Long id) { springDataRepository.deleteById(id); }
    @Override public void deleteAll() { springDataRepository.deleteAll(); }
}
