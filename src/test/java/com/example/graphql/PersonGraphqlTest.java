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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureGraphQlTester
class PersonGraphqlTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private PersonSearchRepository personSearchRepository;

    @MockitoBean
    private com.example.graphql.publications.repository.search.PublicationSearchRepository publicationSearchRepository;

    @MockitoBean
    private org.springframework.boot.CommandLineRunner commandLineRunner;

    @Test
    void shouldCreateAndGetPerson() {
        // Create Person
        String createMutation = """
            mutation {
                createPerson(name: "Test User", age: 25) {
                    id
                    name
                    age
                }
            }
        """;

        Person createdPerson = graphQlTester.document(createMutation)
                .execute()
                .path("createPerson")
                .entity(Person.class)
                .satisfies(person -> {
                    assertEquals("Test User", person.getName());
                    assertEquals(25, person.getAge());
                    assertNotNull(person.getId());
                })
                .get();

        // Verify with Get By ID
        String getQuery = String.format("""
            query {
                personById(id: "%s") {
                    id
                    name
                    age
                }
            }
        """, createdPerson.getId());

        graphQlTester.document(getQuery)
                .execute()
                .path("personById")
                .entity(Person.class)
                .satisfies(person -> {
                    assertEquals("Test User", person.getName());
                    assertEquals(createdPerson.getId(), person.getId());
                });
    }

    @Test
    void shouldGetPersonById() {
        // First create a person to ensure one exists
        String createMutation = """
            mutation {
                createPerson(name: "Find Me", age: 30) {
                    id
                }
            }
        """;

        Person created = graphQlTester.document(createMutation)
                .execute()
                .path("createPerson")
                .entity(Person.class)
                .get();

        String personByIdQuery = String.format("""
            query {
                personById(id: "%s") {
                    id
                    name
                    age
                }
            }
        """, created.getId());

        graphQlTester.document(personByIdQuery)
                .execute()
                .path("personById")
                .entity(Person.class)
                .satisfies(person -> {
                    assertEquals("Find Me", person.getName());
                    assertEquals(created.getId(), person.getId());
                });
    }

    @Test
    void shouldAddAddress() {
        // Create Person
        String createMutation = """
            mutation {
                createPerson(name: "Address User", age: 40) {
                    id
                }
            }
        """;

        Person created = graphQlTester.document(createMutation)
                .execute()
                .path("createPerson")
                .entity(Person.class)
                .get();

        // Add Address
        String addAddressMutation = String.format("""
            mutation {
                addAddress(personId: "%s", street: "123 Main St", city: "New York", state: "NY", zip: "10001") {
                    id
                    addresses {
                        street
                        city
                    }
                }
            }
        """, created.getId());

        graphQlTester.document(addAddressMutation)
                .execute()
                .path("addAddress.addresses[0]")
                .entity(Address.class)
                .satisfies(address -> {
                    assertEquals("123 Main St", address.getStreet());
                    assertEquals("New York", address.getCity());
                });
    }

    @Test
    void shouldSearchPeople() {
        // Mock PersonSearchRepository response
        Person mockPerson = new Person(1L, "Search Match", 20, null, null, null, null, null, Collections.emptyList());
        
        PersonService.PersonSearchResponse mockResponse = new PersonService.PersonSearchResponse(
            List.of(mockPerson),
            Map.of(), Map.of(), Map.of(), null, null, 1, 1
        );
        
        when(personSearchRepository.searchWithFacets(any(), any(), any())).thenReturn(mockResponse);

        String searchQuery = """
            query {
                searchPeople(text: "Match") {
                    results {
                        name
                        age
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

        

            @Test

            void shouldSearchPeopleWithSort() {

                // Mock Response

                Person mockPerson = new Person(1L, "Sorted Person", 30, null, null, null, null, 1000.0, Collections.emptyList());

                PersonService.PersonSearchResponse mockResponse = new PersonService.PersonSearchResponse(

                    List.of(mockPerson),

                    Map.of(), Map.of(), Map.of(), null, null, 1, 1

                );

                when(personSearchRepository.searchWithFacets(any(), any(), any())).thenReturn(mockResponse);

        

                String searchQuery = """

                    query {

                        searchPeople(text: "", sort: [{ field: "salary", direction: DESC }]) {

                            results {

                                name

                                salary

                            }

                        }

                    }

                """;

        

                graphQlTester.document(searchQuery)

                        .execute()

                        .path("searchPeople.results")

                        .entityList(Person.class)

                        .hasSize(1);

            }

        }

        