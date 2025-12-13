package com.example.graphql;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GraphqlApplication {

    public static void main(String[] args) {
        SpringApplication.run(GraphqlApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(PersonService personService) {
        return args -> {
            personService.save(new Person(null, "John Doe", 30, new java.util.ArrayList<>()));
            personService.save(new Person(null, "Jane Smith", 25, new java.util.ArrayList<>()));
        };
    }

}
