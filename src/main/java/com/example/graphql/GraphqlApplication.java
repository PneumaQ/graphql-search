package com.example.graphql;

import com.example.graphql.product.model.Product;
import com.example.graphql.product.model.Review;
import com.example.graphql.product.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class GraphqlApplication {

    public static void main(String[] args) {
        SpringApplication.run(GraphqlApplication.class, args);
    }

    @Bean
    public CommandLineRunner demoData(ProductRepository productRepository) {
        return args -> {
            productRepository.deleteAll();

            // 1. Electronics: Gaming Mouse
            Product p1 = new Product();
            p1.setName("Gaming Mouse X1");
            p1.setSku("GM-X1");
            p1.setCategory("Electronics");
            p1.setPrice(59.99);
            p1.getCustom_attributes().put("color", "black");
            p1.getCustom_attributes().put("dpi", "16000");
            p1.getCustom_attributes().put("wireless", "true");
            
            addReview(p1, "User A", "Amazing speed!", 5);
            addReview(p1, "User B", "A bit small", 3);
            productRepository.save(p1);

            // 2. Electronics: Wireless Keyboard
            Product p2 = new Product();
            p2.setName("Mechanical Keyboard");
            p2.setSku("KB-MECH");
            p2.setCategory("Electronics");
            p2.setPrice(120.00);
            p2.getCustom_attributes().put("color", "white");
            p2.getCustom_attributes().put("switches", "Cherry MX Blue");
            
            addReview(p2, "User C", "Loud but great", 4);
            productRepository.save(p2);

            // 3. Clothing: Cotton T-Shirt
            Product p3 = new Product();
            p3.setName("Premium Cotton T-Shirt");
            p3.setSku("TS-COT-BLU");
            p3.setCategory("Clothing");
            p3.setPrice(25.50);
            p3.getCustom_attributes().put("color", "navy blue");
            p3.getCustom_attributes().put("size", "L");
            p3.getCustom_attributes().put("material", "100% Cotton");
            
            addReview(p3, "User D", "Very soft", 5);
            productRepository.save(p3);

            // 4. Clothing: Silk Scarf
            Product p4 = new Product();
            p4.setName("Luxury Silk Scarf");
            p4.setSku("SC-SILK-RED");
            p4.setCategory("Clothing");
            p4.setPrice(85.00);
            p4.getCustom_attributes().put("color", "ruby red");
            p4.getCustom_attributes().put("material", "Silk");
            
            addReview(p4, "User E", "Beautiful color", 5);
            productRepository.save(p4);

            System.out.println("Seeded " + productRepository.count() + " products.");
        };
    }

    private void addReview(Product p, String author, String comment, int rating) {
        Review r = new Review();
        r.setAuthor(author);
        r.setComment(comment);
        r.setRating(rating);
        r.setProduct(p);
        p.getReviews().add(r);
    }
}
