package com.example.graphql.product.infrastructure.persistence;

import com.example.graphql.product.domain.model.Product;
import com.example.graphql.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaProductAdapter implements ProductRepository {
    private final SpringDataProductRepository springDataRepository;

    @Override public Product save(Product product) { return springDataRepository.save(product); }
    @Override public Optional<Product> findById(Long id) { return springDataRepository.findById(id); }
    @Override public List<Product> findAll() { return springDataRepository.findAll(); }
    @Override public void deleteById(Long id) { springDataRepository.deleteById(id); }
    @Override public void deleteAll() { springDataRepository.deleteAll(); }
}
