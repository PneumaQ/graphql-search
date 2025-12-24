package com.example.graphql;

import com.example.graphql.person.repository.search.PersonSearchRepository;
import com.example.graphql.person.model.Person;
import com.example.graphql.person.model.Address;
import com.example.graphql.person.service.PersonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureGraphQlTester
class PersonGraphqlTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private PersonSearchRepository personSearchRepository;

    @MockitoBean
    private com.example.graphql.platform.security.DacService dacService;

    @MockitoBean
    private com.example.graphql.publications.repository.search.PublicationSearchRepository publicationSearchRepository;

    @MockitoBean
    private org.springframework.boot.CommandLineRunner commandLineRunner;

    @Test
    void shouldSearchPeople() {
        // Mock PersonSearchRepository response
        Person mockPerson = new Person();
        mockPerson.setId(1L);
        mockPerson.setName("Search Match");
        mockPerson.setAddresses(new ArrayList<>());
        
        PersonSearchRepository.PersonSearchInternalResponse mockInternalResponse = 
            new PersonSearchRepository.PersonSearchInternalResponse(
                List.of(mockPerson),
                java.util.Map.of(),
                java.util.Map.of(),
                1L,
                1
            );
        
        when(personSearchRepository.search(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(mockInternalResponse);

        String searchQuery = """
            query {
                searchPeople(text: "Match") {
                    results {
                        name
                    }
                }
            }
        """;

        graphQlTester.document(searchQuery)
                .execute()
                .path("searchPeople.results")
                .entityList(Person.class)
                .satisfies(persons -> {
                    assertFalse(persons.isEmpty());
                    assertEquals("Search Match", persons.get(0).getName());
                });
    }
}