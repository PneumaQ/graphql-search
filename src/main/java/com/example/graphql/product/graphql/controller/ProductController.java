package com.example.graphql.product.graphql.controller;

import com.example.graphql.product.model.Product;
import com.example.graphql.product.model.Review;
import com.example.graphql.product.repository.ReviewRepository;
import com.example.graphql.product.service.ProductService;
import com.example.graphql.platform.filter.SearchCondition;
import com.example.graphql.product.graphql.filter.ProductSort;
import graphql.GraphQLContext;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.graphql.platform.security.DacService;
import com.example.graphql.platform.metadata.PropertyMetadata;
import com.example.graphql.platform.metadata.PropertyCfgRepository;

@Controller
public class ProductController {

    private final ProductService productService;
    private final ReviewRepository reviewRepository;
    private final DacService dacService;
    private final PropertyCfgRepository propertyCfgRepository;

    public ProductController(ProductService productService, 
                             ReviewRepository reviewRepository, 
                             DacService dacService,
                             PropertyCfgRepository propertyCfgRepository) {
        this.productService = productService;
        this.reviewRepository = reviewRepository;
        this.dacService = dacService;
        this.propertyCfgRepository = propertyCfgRepository;
    }

    @QueryMapping
    public List<PropertyMetadata> metadata(@Argument String entityName) {
        return propertyCfgRepository.findAll().stream()
            .filter(p -> p.getParentEntity().getName().equalsIgnoreCase(entityName))
            .map(p -> new PropertyMetadata(p.getPropertyName(), p.getDataType(), p.getDotPath(p.getParentEntity())))
            .toList();
    }

    @SchemaMapping(typeName = "Product", field = "sku")
    public String getSku(Product product) {
        return product.getInternalStockCode();
    }

    @BatchMapping
    public Map<Product, List<Review>> reviews(List<Product> products, GraphQLContext context) {
        com.example.graphql.platform.logging.QueryContext.set("Batch Fetching Reviews");
        
        Integer minRating = context.get("review_minRating");
        List<Long> productIds = products.stream().map(Product::getId).toList();
        
        List<Review> allReviews = (minRating != null) 
            ? reviewRepository.findByProductIdInAndRatingGreaterThanEqual(productIds, minRating)
            : reviewRepository.findByProductIdIn(productIds);
        
        com.example.graphql.platform.logging.QueryContext.clear();
        return allReviews.stream().collect(Collectors.groupingBy(Review::getProduct));
    }

    @QueryMapping
    public ProductService.ProductSearchResponse searchProducts(
            @Argument String text,
            @Argument List<SearchCondition> filter,
            @Argument List<String> facetKeys,
            @Argument List<String> statsKeys,
            @Argument List<ProductSort> sort,
            @Argument Integer page,
            @Argument Integer size,
            GraphQLContext context) {
        
        return productService.searchProducts(text, filter, facetKeys, statsKeys, sort, page, size, context);      
    }

    @QueryMapping
    public List<ProductReviewRow> searchProductReviewTable(@Argument String text) {
        var response = productService.searchProducts(text, null, null, null, null, 0, 100, null);        
        return response.results().stream()
            .flatMap(product -> product.getReviews().stream()
                .map(review -> new ProductReviewRow(
                    product.getName(),
                    product.getInternalStockCode(),
                    product.getBrand() != null ? product.getBrand().name() : "N/A",
                    review.getAuthor(),
                    review.getComment(),
                    review.getRating()
                ))
            )
            .toList();
    }

    @org.springframework.graphql.data.method.annotation.MutationMapping
    public Product createProduct(@Argument String name, @Argument String sku, @Argument String category, @Argument Double price, @Argument List<ProductAttributeInput> custom_attributes) {
        Map<String, String> attrs = new java.util.HashMap<>();
        if (custom_attributes != null) {
            for (ProductAttributeInput input : custom_attributes) {
                attrs.put(input.key(), input.value());
            }
        }
        return productService.createProduct(name, sku, category, price, attrs);
    }

    @org.springframework.graphql.data.method.annotation.MutationMapping
    public Product addReview(@Argument Long productId, @Argument String author, @Argument String comment, @Argument Integer rating) {
        return productService.addReview(productId, author, comment, rating);
    }

    public record ProductAttributeInput(String key, String value) {}
    public record ProductReviewRow(String productName, String sku, String brandName, String author, String comment, int rating) {}
}