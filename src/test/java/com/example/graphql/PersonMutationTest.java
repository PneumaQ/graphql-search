package com.example.graphql;

import com.example.graphql.person.graphql.input.AddressInput;
import com.example.graphql.person.graphql.input.CreatePersonInput;
import com.example.graphql.person.graphql.input.UpdatePersonInput;
import com.example.graphql.person.model.Person;
import com.example.graphql.person.service.PersonMergeService;
import com.example.graphql.person.service.PersonService;
import com.example.graphql.person.repository.search.PersonSearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureGraphQlTester
class PersonMutationTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private PersonMergeService personMergeService;

    @MockitoBean
    private PersonService personService;

    @MockitoBean
    private PersonSearchRepository personSearchRepository;

    @MockitoBean
    private com.example.graphql.publications.repository.search.PublicationSearchRepository publicationSearchRepository;

    @MockitoBean
    private org.springframework.boot.CommandLineRunner commandLineRunner;

    @Test
    void shouldCreatePerson() {
        Person mockPerson = new Person();
        mockPerson.setId(99L);
        mockPerson.setName("Gregg");
        
        when(personMergeService.mergeCreate(any())).thenReturn(mockPerson);
        when(personService.savePerson(any())).thenReturn(mockPerson);

        String mutation = """
            mutation {
                createPerson(input: {
                    name: "Gregg",
                    email: "gregg@test.com",
                    age: 40
                }) {
                    id
                    name
                }
            }
        """;

        graphQlTester.document(mutation)
                .execute()
                .path("createPerson.id").entity(Long.class).isEqualTo(99L)
                .path("createPerson.name").entity(String.class).isEqualTo("Gregg");

        verify(personMergeService).mergeCreate(any(CreatePersonInput.class));
        verify(personService).savePerson(mockPerson);
    }

    @Test
    void shouldUpdatePerson() {
        Person mockPerson = new Person();
        mockPerson.setId(1L);
        mockPerson.setName("Updated Name");
        mockPerson.setAddresses(new ArrayList<>());

        when(personMergeService.mergeUpdate(any())).thenReturn(mockPerson);
        when(personService.savePerson(any())).thenReturn(mockPerson);

        String mutation = """
            mutation {
                updatePerson(input: {
                    id: "1",
                    name: "Updated Name"
                }) {
                    id
                    name
                }
            }
        """;

        graphQlTester.document(mutation)
                .execute()
                .path("updatePerson.name").entity(String.class).isEqualTo("Updated Name");

        verify(personMergeService).mergeUpdate(any(UpdatePersonInput.class));
        verify(personService).savePerson(mockPerson);
    }
}