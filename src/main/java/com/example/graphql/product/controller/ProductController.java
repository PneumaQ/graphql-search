package com.example.graphql.product.controller;

import com.example.graphql.product.model.Product;
import com.example.graphql.product.service.ProductService;
import com.example.graphql.product.filter.SearchCondition;
import com.example.graphql.product.filter.ProductSort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @SchemaMapping(typeName = "Product", field = "sku")
    public String getSku(Product product) {
        // This is our Firewall! We map the internal domain name to the public API name.
        return product.getInternalStockCode();
    }

    @QueryMapping
    public ProductService.ProductSearchResponse searchProducts(
            @Argument String text,
            @Argument List<SearchCondition> filter,
            @Argument List<String> facetKeys,
            @Argument List<String> statsKeys,
            @Argument List<ProductSort> sort,
            @Argument Integer page,
            @Argument Integer size) {
        
        return productService.searchProducts(text, filter, facetKeys, statsKeys, sort, page, size);
    }

    @QueryMapping
    public List<ProductReviewRow> searchProductReviewTable(@Argument String text) {
        // 1. Use the existing service to get the domain entities
        var response = productService.searchProducts(text, null, null, null, null, 0, 100);
        
        // 2. "Explode" the graph: One row per Review
        return response.results().stream()
            .flatMap(product -> product.getReviews().stream()
                .map(review -> new ProductReviewRow(
                    product.getName(),
                    product.getInternalStockCode(), // Domain name mapping
                    product.getBrand() != null ? product.getBrand().name() : "N/A", // From Caffeine Cache
                    review.getAuthor(),
                    review.getComment(),
                    review.getRating()
                ))
            )
            .toList();
    }

    @MutationMapping
    public Product createProduct(
            @Argument String name,
            @Argument String sku,
            @Argument String category,
            @Argument Double price,
            @Argument List<ProductAttributeInput> custom_attributes) {
        
        Map<String, String> attributeMap = new java.util.HashMap<>();
        if (custom_attributes != null) {
            for (ProductAttributeInput input : custom_attributes) {
                attributeMap.put(input.key(), input.value());
            }
        }
        return productService.createProduct(name, sku, category, price, attributeMap);
    }

    @MutationMapping
    public Product addReview(
            @Argument Long productId,
            @Argument String author,
            @Argument String comment,
            @Argument Integer rating) {
        return productService.addReview(productId, author, comment, rating);
    }

    public record ProductAttributeInput(String key, String value) {}
    public record ProductReviewRow(String productName, String sku, String brandName, String author, String comment, int rating) {}
}