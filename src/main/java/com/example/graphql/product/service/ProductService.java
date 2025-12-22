package com.example.graphql.product.service;

import com.example.graphql.product.model.Product;
import com.example.graphql.product.repository.ProductRepository;
import com.example.graphql.product.repository.search.ProductSearchRepository;
import com.example.graphql.product.filter.SearchCondition;
import com.example.graphql.product.filter.ProductSort;
import com.example.graphql.product.model.Review;
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
    public ProductSearchResponse searchProducts(String text, List<SearchCondition> filter, List<String> facetKeys, List<String> statsKeys, List<ProductSort> sort, Integer page, Integer size) {
        int pageNum = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 10;

        var repoResponse = productSearchRepository.search(text, filter, facetKeys, statsKeys, sort, pageNum, pageSize);
        
        return new ProductSearchResponse(
                repoResponse.results(), 
                repoResponse.facets(), 
                repoResponse.stats(), 
                (int) repoResponse.totalElements(), 
                repoResponse.totalPages()
        );
    }

    @Transactional
    public Product createProduct(String name, String sku, String category, Double price, Map<String, String> attributes) {
        Product product = new Product();
        product.setName(name);
        product.setInternalStockCode(sku);
        product.setCategory(category);
        product.setPrice(price);
        product.setCustom_attributes(attributes);
        return productRepository.save(product);
    }

    @Transactional
    public Product addReview(Long productId, String author, String comment, Integer rating) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Review review = new Review();
        review.setAuthor(author);
        review.setComment(comment);
        review.setRating(rating);
        review.setProduct(product);
        product.getReviews().add(review);
        return productRepository.save(product);
    }
    
    public record ProductSearchResponse(List<Product> results, Object facets, Object stats, int totalElements, int totalPages) {}
}