package com.example.graphql.product.service;

import com.example.graphql.product.model.Product;
import com.example.graphql.product.repository.ProductRepository;
import com.example.graphql.product.repository.search.ProductSearchRepository;
import com.example.graphql.platform.filter.SearchConditionInput;
import com.example.graphql.product.graphql.input.ProductSortInput;
import com.example.graphql.product.model.Review;
import com.example.graphql.product.graphql.type.ProductSearchResult;
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
    public ProductSearchResult searchProducts(String text, List<SearchConditionInput> userFilters, List<String> facetKeys, List<String> statsKeys, List<ProductSortInput> sort, Integer page, Integer size, GraphQLContext context) {
        
        List<SearchConditionInput> securityFilters = dacService.getSecurityConditions("Product");
        
        List<SearchConditionInput> allFilters = new java.util.ArrayList<>();
        if (userFilters != null) allFilters.addAll(userFilters);
        allFilters.addAll(securityFilters);

        if (context != null) {
            entityCfgRepository.findByName("Product").ifPresent(root -> {
                for (SearchConditionInput cond : allFilters) {
                    if (cond.getField() == null) continue;
                    
                    propertyCfgRepository.findByPropertyNameAndParentEntityName(cond.getField(), "Product")
                        .or(() -> propertyCfgRepository.findByPropertyNameAndParentEntityName(cond.getField(), "Review"))
                        .ifPresent(meta -> {
                                                        if (!meta.getParentEntity().getName().equalsIgnoreCase(root.getName())) {
                                                            String childKey = meta.getParentEntity().getName().toLowerCase();
                                                            String syncKey = childKey + "_minRating"; 
                                                            String dataType = meta.getDataType() != null ? meta.getDataType().toUpperCase() : "STRING";
                            
                                                            Object val = null;
                                                            try {
                                                                if ("INT".equals(dataType) || "INTEGER".equals(dataType) || "DOUBLE".equals(dataType)) {
                                                                    if (cond.getEq() != null) val = (int) Double.parseDouble(cond.getEq());
                                                                    else if (cond.getGte() != null) val = cond.getGte().intValue();
                                                                    else if (cond.getGt() != null) val = cond.getGt().intValue() + 1;
                                                                }
                                                                
                                                                if (val != null) context.put(syncKey, val);
                                                            } catch (Exception ignored) {
                                                            }
                                                        }                        });
                }
            });
        }


        int pageNum = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 10;

        var repoResponse = productSearchRepository.search(text, allFilters, facetKeys, statsKeys, sort, pageNum, pageSize);
        
        return new ProductSearchResult(
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
}