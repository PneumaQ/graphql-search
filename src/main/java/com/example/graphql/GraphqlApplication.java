package com.example.graphql;

import com.example.graphql.cfg.model.LookupCfg;
import com.example.graphql.cfg.repository.LookupCfgRepository;
import com.example.graphql.cfg.service.CfgCacheService;
import com.example.graphql.product.model.Product;
import com.example.graphql.product.model.Review;
import com.example.graphql.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@SpringBootApplication
public class GraphqlApplication {

    public static void main(String[] args) {
        SpringApplication.run(GraphqlApplication.class, args);
    }

    @Bean
    public CommandLineRunner demoData(
            ProductRepository productRepository, 
            com.example.graphql.product.repository.CustomFieldRepository customFieldRepository,
            LookupCfgRepository lookupCfgRepository,
            CfgCacheService cfgCacheService,
            EntityManager entityManager,
            org.springframework.context.ApplicationContext applicationContext,
            TransactionTemplate transactionTemplate) {
        return args -> transactionTemplate.execute(status -> {
            productRepository.deleteAll();
            customFieldRepository.deleteAll();
            lookupCfgRepository.deleteAll();

            // 1. Seed Metadata (Semantically Correct DDD)
            com.example.graphql.platform.metadata.EntityCfg productMeta = new com.example.graphql.platform.metadata.EntityCfg(null, "Product", true, null);
            com.example.graphql.platform.metadata.EntityCfg reviewMeta = new com.example.graphql.platform.metadata.EntityCfg(null, "Review", false, null);
            com.example.graphql.platform.metadata.EntityCfg personMeta = new com.example.graphql.platform.metadata.EntityCfg(null, "Person", true, null);
            com.example.graphql.platform.metadata.EntityCfg addressMeta = new com.example.graphql.platform.metadata.EntityCfg(null, "Address", false, null);
            
            entityManager.persist(productMeta);
            entityManager.persist(reviewMeta);
            entityManager.persist(personMeta);
            entityManager.persist(addressMeta);

            // Product Core Properties (id, logicalName, type, technicalPath, representedEntity, parent)
            entityManager.persist(new com.example.graphql.platform.metadata.PropertyCfg(null, "name", "STRING", "name_keyword", null, productMeta));
            com.example.graphql.platform.metadata.PropertyCfg categoryProp = new com.example.graphql.platform.metadata.PropertyCfg(null, "category", "STRING", "category_keyword", null, productMeta);
            entityManager.persist(categoryProp);
            entityManager.persist(new com.example.graphql.platform.metadata.PropertyCfg(null, "price", "DOUBLE", "price", null, productMeta));
            entityManager.persist(new com.example.graphql.platform.metadata.PropertyCfg(null, "sku", "STRING", "sku_keyword", null, productMeta));
            entityManager.persist(new com.example.graphql.platform.metadata.PropertyCfg(null, "brand", "STRING", "brand.name_keyword", null, productMeta));
            
            // Product -> Review Relationship
            entityManager.persist(new com.example.graphql.platform.metadata.PropertyCfg(null, "reviews", "ENTITY", null, "Review", productMeta));
            
            // Dynamic Attributes
            entityManager.persist(new com.example.graphql.platform.metadata.PropertyCfg(null, "color", "STRING", "custom_attributes.color_keyword", null, productMeta));
            entityManager.persist(new com.example.graphql.platform.metadata.PropertyCfg(null, "material", "STRING", "custom_attributes.material_keyword", null, productMeta));
            entityManager.persist(new com.example.graphql.platform.metadata.PropertyCfg(null, "ram", "STRING", "custom_attributes.ram_keyword", null, productMeta));
            entityManager.persist(new com.example.graphql.platform.metadata.PropertyCfg(null, "desc", "STRING", "custom_attributes.desc_keyword", null, productMeta));
            
            // Review Properties
            com.example.graphql.platform.metadata.PropertyCfg ratingProp = new com.example.graphql.platform.metadata.PropertyCfg(null, "rating", "INT", null, null, reviewMeta);
            entityManager.persist(ratingProp);
            entityManager.persist(new com.example.graphql.platform.metadata.PropertyCfg(null, "comment", "STRING", null, null, reviewMeta));

            // Person Properties
            entityManager.persist(new com.example.graphql.platform.metadata.PropertyCfg(null, "name", "STRING", "name_keyword", null, personMeta));
            entityManager.persist(new com.example.graphql.platform.metadata.PropertyCfg(null, "addresses", "ENTITY", null, "Address", personMeta));
            com.example.graphql.platform.metadata.PropertyCfg countryProp = new com.example.graphql.platform.metadata.PropertyCfg(null, "country", "STRING", "country_keyword", null, addressMeta);
            entityManager.persist(countryProp);

            // 2. Seed DAC Rule: "Only Electronics" (Disabled for initial demo)
            com.example.graphql.platform.security.DacCfg dac = new com.example.graphql.platform.security.DacCfg(null, "Electronics Only Access", false, productMeta, null);
            entityManager.persist(dac);
            entityManager.persist(new com.example.graphql.platform.security.DacConditionCfg(null, categoryProp, "EQ", List.of("Electronics"), dac));

            // 3. Seed DAC Rule: "Only USA People" (Active for demo)
            com.example.graphql.platform.security.DacCfg personDac = new com.example.graphql.platform.security.DacCfg(null, "USA Residents Only", true, personMeta, null);
            entityManager.persist(personDac);
            entityManager.persist(new com.example.graphql.platform.security.DacConditionCfg(null, countryProp, "EQ", List.of("USA"), personDac));

            // 4. Seed Lookups
            LookupCfg logitech = lookupCfgRepository.save(new LookupCfg(null, "Logitech", "High-performance peripherals", "BRAND"));
            LookupCfg razer = lookupCfgRepository.save(new LookupCfg(null, "Razer", "For Gamers. By Gamers.", "BRAND"));
            LookupCfg nike = lookupCfgRepository.save(new LookupCfg(null, "Nike", "Just Do It", "BRAND"));

            // 5. Seed Products
            Product p1 = new Product();
            p1.setName("Gaming Mouse X1");
            p1.setInternalStockCode("GM-X1");
            p1.setCategory("Electronics");
            p1.setPrice(59.99);
            p1.setBrand(cfgCacheService.getLookup(logitech.getId()));
            p1.getCustom_attributes().put("color", "black");
            p1.getCustom_attributes().put("material", "Plastic");
            addReview(p1, "User A", "Amazing speed!", 5);
            addReview(p1, "User B", "A bit small", 3);
            productRepository.save(p1);

            Product p2 = new Product();
            p2.setName("Premium Cotton T-Shirt");
            p2.setInternalStockCode("TS-COT-BLU");
            p2.setCategory("Clothing");
            p2.setPrice(25.50);
            p2.setBrand(cfgCacheService.getLookup(nike.getId()));
            p2.getCustom_attributes().put("color", "navy blue");
            p2.getCustom_attributes().put("material", "Cotton");
            addReview(p2, "Gregg", "Excellent navy color", 5);
            productRepository.save(p2);

            Product p3 = new Product();
            p3.setName("Mechanical Keyboard");
            p3.setInternalStockCode("KB-MECH");
            p3.setCategory("Electronics");
            p3.setPrice(120.00);
            p3.setBrand(cfgCacheService.getLookup(razer.getId()));
            p3.getCustom_attributes().put("color", "white");
            p3.getCustom_attributes().put("material", "Aluminum");
            addReview(p3, "Pro Gamer", "Best switches ever", 5);
            productRepository.save(p3);

            // 6. Seed People
            com.example.graphql.person.repository.jpa.PersonRepository personRepo = applicationContext.getBean(com.example.graphql.person.repository.jpa.PersonRepository.class);
            com.example.graphql.person.model.Person g = new com.example.graphql.person.model.Person();
            g.setName("Gregg");
            g.setEmail("gregg@example.com");
            addAddress(g, "123 Main St", "New York", "USA");
            personRepo.save(g);

            com.example.graphql.person.model.Person j = new com.example.graphql.person.model.Person();
            j.setName("Jean");
            j.setEmail("jean@example.fr");
            addAddress(j, "Rue de Rivoli", "Paris", "France");
            personRepo.save(j);

            cfgCacheService.primeCache();
            return null;
        });
    }

    private void addAddress(com.example.graphql.person.model.Person p, String street, String city, String country) {
        com.example.graphql.person.model.Address a = new com.example.graphql.person.model.Address();
        a.setStreet(street);
        a.setCity(city);
        a.setCountry(country);
        a.setPerson(p);
        p.getAddresses().add(a);
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
