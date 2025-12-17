package com.example.graphql;

import com.example.graphql.filter.PersonFilterInput;
import com.example.graphql.filter.PersonSort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureGraphQlTester
class PersonGraphqlFacetTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private PersonService personService;

    @MockitoBean
    private org.springframework.boot.CommandLineRunner commandLineRunner;

    @Test
    void shouldReturnFacetsAndStats() {
        // Mock Response
        Person person = new Person(1L, null, "John Doe", 30, "john@example.com", null, null, true, 50000.0, Collections.emptyList());
        
        PersonService.NumericStats ageStats = new PersonService.NumericStats(20.0, 40.0, 30.0, 300.0, 10);
        PersonService.NumericStats salaryStats = new PersonService.NumericStats(40000.0, 60000.0, 50000.0, 500000.0, 10);
        
        PersonService.PersonSearchResponse mockResponse = new PersonService.PersonSearchResponse(
            List.of(person),
            Map.of("true", 10L), // activeCounts
            Map.of("USA", 5L),   // countryCounts
            Map.of("NY", 5L),    // stateCounts
            ageStats,
            salaryStats,
            1,
            1
        );

        when(personService.searchWithFacets(any(), any(), any())).thenReturn(mockResponse);

        String query = """
            query {
                searchPeople(text: "John", page: 0, size: 10) {
                    results {
                        name
                        age
                    }
                    stats {
                        age {
                            min
                            max
                            avg
                            count
                        }
                        salary {
                            sum
                            avg
                        }
                    }
                    facets {
                        byActive {
                            value
                            count
                        }
                    }
                }
            }
        """;

        graphQlTester.document(query)
                .execute()
                .path("searchPeople.results[0].name").entity(String.class).isEqualTo("John Doe")
                .path("searchPeople.stats.age.min").entity(Double.class).isEqualTo(20.0)
                .path("searchPeople.stats.age.count").entity(Integer.class).isEqualTo(10)
                .path("searchPeople.stats.salary.sum").entity(Double.class).isEqualTo(500000.0)
                .path("searchPeople.facets.byActive[0].value").entity(String.class).isEqualTo("true");
    }
}
