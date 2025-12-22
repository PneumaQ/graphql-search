package com.example.graphql.product.controller;

import com.example.graphql.product.model.Product;
import com.example.graphql.product.model.Review;
import com.example.graphql.product.repository.ReviewRepository;
import com.example.graphql.product.service.ProductService;
import com.example.graphql.product.filter.SearchCondition;
import com.example.graphql.product.filter.ProductSort;
import graphql.GraphQLContext;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ProductController {

    private final ProductService productService;
    private final ReviewRepository reviewRepository;

    public ProductController(ProductService productService, ReviewRepository reviewRepository) {
        this.productService = productService;
        this.reviewRepository = reviewRepository;
    }

    @SchemaMapping(typeName = "Product", field = "sku")
    public String getSku(Product product) {
        return product.getInternalStockCode();
    }

    @BatchMapping
    public Map<Product, List<Review>> reviews(List<Product> products, GraphQLContext context) {
        com.example.graphql.platform.logging.QueryContext.set("Batch Fetching Reviews");
        
        // 1. Retrieve the "Synchronized Filter" from the context
        Integer minRating = context.get("review_minRating");
        List<Long> productIds = products.stream().map(Product::getId).toList();
        
        // 2. Apply the filter if it exists
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
        
        // 3. Capture 'rating' filters to synchronize with the nested reviews
        if (filter != null) {
            filter.stream()
                .filter(c -> "rating".equalsIgnoreCase(c.field()) && c.eq() != null)
                .findFirst()
                .ifPresent(c -> context.put("review_minRating", Integer.parseInt(c.eq())));
            
            // Also handle range filters (gt/gte)
            filter.stream()
                .filter(c -> "rating".equalsIgnoreCase(c.field()))
                .forEach(c -> {
                    if (c.gt() != null) context.put("review_minRating", c.gt().intValue() + 1);
                    if (c.gte() != null) context.put("review_minRating", c.gte().intValue());
                });
        }
        
        return productService.searchProducts(text, filter, facetKeys, statsKeys, sort, page, size);      
    }

    @QueryMapping
    public List<ProductReviewRow> searchProductReviewTable(@Argument String text) {
        var response = productService.searchProducts(text, null, null, null, null, 0, 100);        
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

    public record ProductReviewRow(String productName, String sku, String brandName, String author, String comment, int rating) {}
}
