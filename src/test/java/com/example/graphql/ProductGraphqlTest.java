package com.example.graphql;

import com.example.graphql.product.model.Product;
import com.example.graphql.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureGraphQlTester
class ProductGraphqlTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    record ProductAttributeDto(String key, String value) {}
    record ProductDto(String id, String name, List<ProductAttributeDto> attributes) {}

    @Test
    void shouldCreateAndSearchProductWithDynamicAttributes() throws InterruptedException {
        // 1. Create Product via Mutation
        String createMutation = """
            mutation {
                createProduct(
                    name: "Gaming Laptop",
                    sku: "GL-123",
                    attributes: [
                        { key: "color", value: "black" },
                        { key: "ram", value: "32GB" }
                    ]
                ) {
                    id
                    name
                    attributes {
                        key
                        value
                    }
                }
            }
        """;

        var createdProduct = graphQlTester.document(createMutation)
                .execute()
                .path("createProduct")
                .entity(ProductDto.class)
                .get();

        assertThat(createdProduct.name()).isEqualTo("Gaming Laptop");
        assertThat(createdProduct.attributes()).contains(new ProductAttributeDto("color", "black"));
        
        // Wait for Hibernate Search indexing
        Thread.sleep(1500);

        // 2. Search by Dynamic Attribute (color: black)
        String searchQuery = """
            query {
                searchProducts(filter: {
                    attributes: [
                        { key: "color", value: { eq: "black" } }
                    ]
                }) {
                    name
                    attributes {
                        key
                        value
                    }
                }
            }
        """;

        graphQlTester.document(searchQuery)
                .execute()
                .path("searchProducts")
                .entityList(ProductDto.class)
                .hasSize(1)
                .satisfies(products -> {
                    ProductDto p = products.get(0);
                    assertThat(p.name()).isEqualTo("Gaming Laptop");
                });
    }
}
