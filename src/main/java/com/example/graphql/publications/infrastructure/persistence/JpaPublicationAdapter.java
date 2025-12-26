package com.example.graphql.publications.infrastructure.persistence;

import com.example.graphql.publications.domain.model.Publication;
import com.example.graphql.publications.domain.repository.PublicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaPublicationAdapter implements PublicationRepository {
    private final SpringDataPublicationRepository springDataRepository;

    @Override public Publication save(Publication publication) { return springDataRepository.save(publication); }
    @Override public Optional<Publication> findById(Long id) { return springDataRepository.findById(id); }
    @Override public List<Publication> findAll() { return springDataRepository.findAll(); }
    @Override public void deleteById(Long id) { springDataRepository.deleteById(id); }
    @Override public void deleteAll() { springDataRepository.deleteAll(); }
}
