package com.example.graphql;

import com.example.graphql.platform.security.DacCfg;
import com.example.graphql.platform.security.DacCfgRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureGraphQlTester
class PersonDacIntegrationTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private DacCfgRepository dacCfgRepository;

    @Test
    @Transactional
    void shouldOnlyReturnUSAResidentsDueToDac() {
        // Explicitly enable the DAC for this test
        DacCfg dac = dacCfgRepository.findAll().stream()
                .filter(d -> d.getName().equals("USA Residents Only"))
                .findFirst()
                .orElseThrow();
        dac.setActive(true);
        dacCfgRepository.saveAndFlush(dac);

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
                    // Jean (France) filtered out. Gregg (USA), Sarah (Canada), Akira (Japan) remain.
                    // Wait, the DAC is "USA Residents Only" and active=true. 
                    // Let me re-read the DAC condition. 
                    // It is country == "USA". 
                    // So Sarah and Akira SHOULD be filtered out too.
                    // Only 1 person (Gregg) should be found.
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