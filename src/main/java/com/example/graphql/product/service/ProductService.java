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

import com.example.graphql.platform.security.DacService;
import com.example.graphql.platform.metadata.PropertyCfgRepository;
import com.example.graphql.platform.metadata.PropertyCfg;
import com.example.graphql.platform.metadata.EntityCfgRepository;
import graphql.GraphQLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;
    private final DacService dacService;
    private final PropertyCfgRepository propertyCfgRepository;
    private final EntityCfgRepository entityCfgRepository;

    public ProductService(ProductRepository productRepository, 
                          ProductSearchRepository productSearchRepository,
                          DacService dacService,
                          PropertyCfgRepository propertyCfgRepository,
                          EntityCfgRepository entityCfgRepository) {
        this.productRepository = productRepository;
        this.productSearchRepository = productSearchRepository;
        this.dacService = dacService;
        this.propertyCfgRepository = propertyCfgRepository;
        this.entityCfgRepository = entityCfgRepository;
    }

    @Transactional(readOnly = true)
    public ProductSearchResponse searchProducts(String text, List<SearchCondition> userFilters, List<String> facetKeys, List<String> statsKeys, List<ProductSort> sort, Integer page, Integer size, GraphQLContext context) {
        
        // 1. DYNAMIC SECURITY INJECTION
        List<SearchCondition> securityFilters = dacService.getSecurityConditions("Product");
        if (!securityFilters.isEmpty()) {
            log.info("Applying Security DACs: {}", securityFilters.stream().map(s -> s.getField() + " " + s.getEq()).collect(java.util.stream.Collectors.joining(", ")));
        }
        
        // 2. MERGE FILTERS
        List<SearchCondition> allFilters = new java.util.ArrayList<>();
        if (userFilters != null) allFilters.addAll(userFilters);
        allFilters.addAll(securityFilters);

        // 3. DYNAMIC FILTER SYNCHRONIZATION
        if (context != null) {
            entityCfgRepository.findByName("Product").ifPresent(root -> {
                for (SearchCondition cond : allFilters) {
                    if (cond.getField() == null) continue;
                    
                    propertyCfgRepository.findByPropertyNameAndParentEntityName(cond.getField(), "Product")
                        .or(() -> propertyCfgRepository.findByPropertyNameAndParentEntityName(cond.getField(), "Review"))
                        .ifPresent(meta -> {
                            // Only synchronize if the property belongs to a child entity (e.g., Review)
                            // and NOT the root entity (Product). This prevents JSON attributes from being misidentified.
                            if (!meta.getParentEntity().getName().equalsIgnoreCase(root.getName())) {
                                String childKey = meta.getParentEntity().getName().toLowerCase();
                                // For this POC, we map 'rating' to 'minRating' for the batch loader
                                String syncKey = childKey + "_minRating"; 
                                
                                Object val = null;
                                try {
                                    if (cond.getEq() != null) val = (int) Double.parseDouble(cond.getEq());
                                    else if (cond.getGte() != null) val = cond.getGte().intValue();
                                    else if (cond.getGt() != null) val = cond.getGt().intValue() + 1;
                                    
                                    if (val != null) context.put(syncKey, val);
                                } catch (Exception ignored) {
                                    // If conversion fails, we don't sync this specific filter
                                }
                            }
                        });
                }
            });
        }


        int pageNum = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 10;

        var repoResponse = productSearchRepository.search(text, allFilters, facetKeys, statsKeys, sort, pageNum, pageSize);
        
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