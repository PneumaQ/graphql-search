package com.example.graphql;

import com.example.graphql.repository.search.PersonSearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureGraphQlTester
class PersonGraphqlTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private PersonSearchRepository personSearchRepository;

    @MockitoBean
    private ElasticsearchOperations elasticsearchOperations;

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

        // Get Persons
        String personsQuery = """
            query {
                persons {
                    id
                    name
                    age
                }
            }
        """;

        graphQlTester.document(personsQuery)
                .execute()
                .path("persons")
                .entityList(Person.class)
                .satisfies(persons -> {
                    assertTrue(persons.stream().anyMatch(p -> p.getId().equals(createdPerson.getId())));
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
        // Mock Elasticsearch operations response
        Person mockPerson = new Person(1L, null, "Search Match", 20, null, null, null, null, null, Collections.emptyList());
        
        SearchHits<Person> mockHits = mock(SearchHits.class);
        SearchHit<Person> mockHit = mock(SearchHit.class);
        when(mockHit.getContent()).thenReturn(mockPerson);
        when(mockHits.stream()).thenReturn(Stream.of(mockHit));
        
        when(elasticsearchOperations.search(any(Query.class), eq(Person.class))).thenReturn(mockHits);

        String searchQuery = """
            query {
                searchPeople(text: "Match") {
                    name
                    age
                }
            }
        """;

        graphQlTester.document(searchQuery)
                .execute()
                .path("searchPeople")
                .entityList(Person.class)
                .satisfies(persons -> {
                    assertFalse(persons.isEmpty());
                    assertEquals("Search Match", persons.get(0).getName());
                });
    }
}
