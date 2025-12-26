package com.example.graphql.product.infrastructure.persistence;

import com.example.graphql.product.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProductRepository extends JpaRepository<Product, Long> {
}
