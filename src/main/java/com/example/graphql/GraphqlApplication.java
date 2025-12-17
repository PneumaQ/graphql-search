package com.example.graphql;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import com.example.graphql.person.service.PersonService;
import com.example.graphql.publications.service.PublicationService;
import com.example.graphql.person.model.Person;
import com.example.graphql.publications.model.Publication;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"com.example.graphql.person.repository.jpa", "com.example.graphql.publications.repository.jpa"})
public class GraphqlApplication {

    public static void main(String[] args) {
        SpringApplication.run(GraphqlApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(PersonService personService, PublicationService publicationService) {
        return args -> {
            // Person 1: 1 Address
            Person p1 = new Person(null, "John Doe", 30, "john.doe@example.com", "555-0101", java.time.LocalDate.of(1993, 1, 15), true, 75000.00, new java.util.ArrayList<>());
            Person savedP1 = personService.save(p1);
            personService.addAddress(savedP1.getId(), "123 Main St", "New York", "NY", "10001", "USA", true);

            // Person 2: 2 Addresses
            Person p2 = new Person(null, "Jane Smith", 25, "jane.smith@example.com", "555-0102", java.time.LocalDate.of(1998, 5, 20), true, 82000.50, new java.util.ArrayList<>());
            Person savedP2 = personService.save(p2);
            personService.addAddress(savedP2.getId(), "456 Oak Ave", "Los Angeles", "CA", "90001", "USA", true);
            personService.addAddress(savedP2.getId(), "789 Pine Ln", "San Diego", "CA", "92101", "USA", false);

            // Person 3: 3 Addresses
            Person p3 = new Person(null, "Bob Johnson", 40, "bob.johnson@example.com", "555-0103", java.time.LocalDate.of(1983, 11, 30), false, 120000.00, new java.util.ArrayList<>());
            Person savedP3 = personService.save(p3);
            personService.addAddress(savedP3.getId(), "101 Maple Dr", "Austin", "TX", "73301", "USA", true);
            personService.addAddress(savedP3.getId(), "202 Cedar Blvd", "Houston", "TX", "77001", "USA", false);
            personService.addAddress(savedP3.getId(), "303 Birch Rd", "Dallas", "TX", "75001", "USA", false);
            
            // Create Publications
            Publication pub1 = publicationService.createPublication("Advanced Clinical Trials for Generic Search", "Journal of Medical AI", java.time.LocalDate.now().minusMonths(3), "PUBLISHED", "10.1001/jma.2023.001");
            
            // Add Authors (Cross-Aggregate)
            publicationService.addAuthor(pub1.getId(), savedP1.getId(), 1, true, "Harvard Medical School"); // John Doe
            publicationService.addAuthor(pub1.getId(), savedP2.getId(), 2, false, "Johns Hopkins University"); // Jane Smith
            
            Publication pub2 = publicationService.createPublication("GraphQL in Healthcare", "Tech Medicine Review", java.time.LocalDate.now().minusMonths(1), "SUBMITTED", "10.1002/tmr.2023.055");
            publicationService.addAuthor(pub2.getId(), savedP3.getId(), 1, true, "Mayo Clinic"); // Bob Johnson
        };
    }

}