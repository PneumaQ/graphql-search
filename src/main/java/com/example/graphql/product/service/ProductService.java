package com.example.graphql.product.service;

import com.example.graphql.product.model.Product;
import com.example.graphql.product.repository.ProductRepository;
import com.example.graphql.product.repository.search.ProductSearchRepository;
import com.example.graphql.product.filter.ProductFilterInput;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;

    public ProductService(ProductRepository productRepository, ProductSearchRepository productSearchRepository) {
        this.productRepository = productRepository;
        this.productSearchRepository = productSearchRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> searchProducts(ProductFilterInput filter) {
        return productSearchRepository.search(filter);
    }

    @Transactional
    public Product createProduct(String name, String sku, Map<String, String> attributes) {
        Product product = new Product();
        product.setName(name);
        product.setSku(sku);
        product.setAttributes(attributes);
        return productRepository.save(product);
    }
}
