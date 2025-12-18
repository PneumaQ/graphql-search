package com.example.graphql.product.controller;

import com.example.graphql.product.model.Product;
import com.example.graphql.product.service.ProductService;
import com.example.graphql.product.filter.ProductFilterInput;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @QueryMapping
    public List<Product> searchProducts(@Argument ProductFilterInput filter) {
        return productService.searchProducts(filter);
    }

    @MutationMapping
    public Product createProduct(
            @Argument String name,
            @Argument String sku,
            @Argument List<ProductAttributeInput> attributes) {
        
        Map<String, String> attributeMap = new java.util.HashMap<>();
        if (attributes != null) {
            for (ProductAttributeInput input : attributes) {
                attributeMap.put(input.key(), input.value());
            }
        }
        
        return productService.createProduct(name, sku, attributeMap);
    }

    @SchemaMapping(typeName = "Product", field = "attributes")
    public List<ProductAttribute> getAttributes(Product product) {
        return product.getAttributes().entrySet().stream()
                .map(e -> new ProductAttribute(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public record ProductAttributeInput(String key, String value) {}
    public record ProductAttribute(String key, String value) {}
}
