package com.example.graphql;

import com.example.graphql.person.service.PersonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureGraphQlTester
class PersonDacIntegrationTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    @org.springframework.transaction.annotation.Transactional
    void shouldOnlyReturnUSAResidentsDueToDac() {
        // The demoData seeds:
        // 1. Gregg (USA)
        // 2. Jean (France)
        // 3. DAC: USA Residents Only (Active)

        String searchQuery = """
            query {
                searchPeople(text: "") {
                    results {
                        name
                        addresses {
                            country
                        }
                    }
                    totalElements
                }
            }
        """;

        graphQlTester.document(searchQuery)
                .execute()
                .path("searchPeople.results")
                .entityList(Object.class)
                .satisfies(results -> {
                    // Jean should be filtered out by DacService + UniversalQueryBuilder
                    assertEquals(1, results.size(), "Should only find 1 person (Gregg) due to USA DAC");
                })
                .path("searchPeople.results[0].name")
                .entity(String.class)
                .isEqualTo("Gregg")
                .path("searchPeople.results[0].addresses[0].country")
                .entity(String.class)
                .isEqualTo("USA");
    }
}
