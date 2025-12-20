package com.example.graphql.product.controller;

import com.example.graphql.product.model.Product;
import com.example.graphql.product.service.ProductService;
import com.example.graphql.product.filter.SearchCondition;
import com.example.graphql.product.filter.ProductSort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @QueryMapping
    public ProductSearchResult searchProducts(
            @Argument String text,
            @Argument List<SearchCondition> filter,
            @Argument List<String> facetKeys,
            @Argument List<String> statsKeys,
            @Argument List<ProductSort> sort,
            @Argument Integer page,
            @Argument Integer size) {
        
        int pageNum = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 10;
        
        var response = productService.searchProducts(text, filter, facetKeys, statsKeys, sort, pageNum, pageSize);
        return new ProductSearchResult(response.results(), response.facets(), response.stats(), (int) response.totalElements(), response.totalPages());
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
    public record ProductSearchResult(List<Product> results, Object facets, Object stats, int totalElements, int totalPages) {}
}