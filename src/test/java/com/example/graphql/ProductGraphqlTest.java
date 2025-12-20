package com.example.graphql;

import com.example.graphql.product.model.Product;
import com.example.graphql.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.List;
import java.util.Map;

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
    record ProductDto(String id, String name, Map<String, Object> custom_attributes) {}
    record ProductSearchResultDto(List<ProductDto> results, Object facets, Object stats, int totalElements, int totalPages) {}

    @Test
    void shouldSearchWithStats() throws InterruptedException {
        String p1Id = createProduct("Stats P1", "S1", List.of());
        addReview(p1Id, "A", "Good", 5);
        String p2Id = createProduct("Stats P2", "S2", List.of());
        addReview(p2Id, "B", "Bad", 3);

        Thread.sleep(2000);

        String statsQuery = "{ searchProducts(statsKeys: [\"rating\"]) { stats } }";
        graphQlTester.document(statsQuery).execute()
                .path("searchProducts.stats.rating")
                .matchesJson("{ \"min\": 3.0, \"max\": 5.0, \"avg\": 4.0, \"sum\": 8.0, \"count\": 2 }");
    }

    @Test
    void shouldPaginateProducts() throws InterruptedException {
        for (int i = 0; i < 15; i++) {
            createProduct("Item " + i, "SKU" + i, List.of());
        }
        Thread.sleep(2000);

        String page0Query = "{ searchProducts(page: 0, size: 10) { results { name }, totalElements, totalPages } }";
        graphQlTester.document(page0Query).execute()
                .path("searchProducts").entity(ProductSearchResultDto.class)
                .satisfies(res -> {
                    assertThat(res.results()).hasSize(10);
                    assertThat(res.totalElements()).isEqualTo(15);
                });
    }

    @Test
    void shouldCreateAndSearchProductWithDynamicAttributes() throws InterruptedException {
        String createMutation = "mutation { createProduct(name: \"Laptop\", sku: \"L1\", custom_attributes: [{ key: \"ram\", value: \"16GB\" }]) { id, custom_attributes } }";
        var created = graphQlTester.document(createMutation).execute().path("createProduct").entity(ProductDto.class).get();
        assertThat(created.custom_attributes()).containsEntry("ram", "16GB");
        
        Thread.sleep(2000);

        String searchQuery = "query { searchProducts(filter: [{ field: \"ram\", eq: \"16GB\" }]) { results { name } } }";
        graphQlTester.document(searchQuery).execute().path("searchProducts.results").entityList(ProductDto.class).hasSize(1);
    }

    @Test
    void shouldSearchWithFacets() throws InterruptedException {
        createProduct("P1", "SKU1", List.of(new ProductAttributeDto("color", "red")));
        createProduct("P2", "SKU2", List.of(new ProductAttributeDto("color", "red")));
        createProduct("P3", "SKU3", List.of(new ProductAttributeDto("color", "blue")));
        Thread.sleep(2000);

        String facetQuery = "{ searchProducts(facetKeys: [\"color\"]) { facets } }";
        graphQlTester.document(facetQuery).execute()
                .path("searchProducts.facets.color")
                .matchesJson("{ \"red\": 2, \"blue\": 1 }");
    }

    @Test
    void shouldSearchAndSortProducts() throws InterruptedException {
        createProduct("Unique-Apple", "S1", List.of(new ProductAttributeDto("price", "100")));
        createProduct("Unique-Cherry", "S3", List.of(new ProductAttributeDto("price", "300")));
        createProduct("Unique-Banana", "S2", List.of(new ProductAttributeDto("price", "200")));
        Thread.sleep(2000);

        // Sort by Name ASC
        String nameSortQuery = "query { searchProducts(filter: [{ field: \"name\", startsWith: \"Unique-\" }], sort: [{ field: \"name\", direction: ASC }]) { results { name } } }";
        var results = graphQlTester.document(nameSortQuery).execute().path("searchProducts.results").entityList(ProductDto.class).get();
        assertThat(results).extracting(ProductDto::name).containsExactly("Unique-Apple", "Unique-Banana", "Unique-Cherry");
    }

    @Test
    void shouldSearchProductsByReview() throws InterruptedException {
        String p1Id = createProduct("M1", "SKU1", List.of());
        addReview(p1Id, "User1", "Great", 5);
        Thread.sleep(2000);

        String ratingQuery = "{ searchProducts(filter: [{ field: \"rating\", eq: \"5\" }]) { results { name } } }";
        graphQlTester.document(ratingQuery).execute().path("searchProducts.results").entityList(ProductDto.class).hasSize(1);
    }

    @Test
    void shouldSearchProductsByFullTextAttribute() throws InterruptedException {
        createProduct("Shirt", "S1", List.of(new ProductAttributeDto("desc", "Navy Blue Cotton")));
        Thread.sleep(2000);

        String navyQuery = "{ searchProducts(text: \"Navy\") { results { name } } }";
        graphQlTester.document(navyQuery).execute().path("searchProducts.results").entityList(ProductDto.class).hasSize(1);
    }

    private String createProduct(String name, String sku, List<ProductAttributeDto> attrs) {
        StringBuilder attrString = new StringBuilder("[");
        for (ProductAttributeDto attr : attrs) {
            attrString.append(String.format("{ key: \"%s\", value: \"%s\" },", attr.key(), attr.value()));
        }
        if (!attrs.isEmpty()) attrString.setLength(attrString.length() - 1);
        attrString.append("]");

        String mutation = String.format("mutation { createProduct(name: \"%s\", sku: \"%s\", custom_attributes: %s) { id } }", name, sku, attrString);
        return graphQlTester.document(mutation).execute().path("createProduct.id").entity(String.class).get();
    }

    private void addReview(String productId, String author, String comment, int rating) {
        String mutation = String.format("mutation { addReview(productId: \"%s\", author: \"%s\", comment: \"%s\", rating: %d) { id } }", productId, author, comment, rating);
        graphQlTester.document(mutation).execute();
    }
}