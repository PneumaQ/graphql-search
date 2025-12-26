package com.example.graphql;

import com.example.graphql.person.domain.model.Person;
import com.example.graphql.person.domain.repository.PersonRepository;
import com.example.graphql.person.domain.repository.PersonSearchRepository;
import com.example.graphql.person.graphql.input.CreatePersonInput;
import com.example.graphql.person.graphql.input.UpdatePersonInput;
import com.example.graphql.person.domain.service.PersonService;
import com.example.graphql.publications.domain.repository.PublicationSearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureGraphQlTester
class PersonMutationTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockBean
    private PersonService personService;

    @MockBean
    private PersonRepository personRepository;

    @MockBean
    private PersonSearchRepository personSearchRepository;

    @MockBean
    private com.example.graphql.platform.security.DacService dacService;

    @MockBean
    private PublicationSearchRepository publicationSearchRepository;

    @MockBean
    private org.springframework.boot.CommandLineRunner commandLineRunner;

    @Test
    void shouldCreatePerson() {
        Person mockPerson = new Person();
        mockPerson.setId(99L);
        mockPerson.setName("Gregg");
        
        when(personService.createPerson(any())).thenReturn(mockPerson);

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

        verify(personService).createPerson(any(CreatePersonInput.class));
    }

    @Test
    void shouldUpdatePerson() {
        Person mockPerson = new Person();
        mockPerson.setId(1L);
        mockPerson.setName("Updated Name");
        mockPerson.setAddresses(new ArrayList<>());

        when(personService.updatePerson(any())).thenReturn(mockPerson);

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

        verify(personService).updatePerson(any(UpdatePersonInput.class));
    }
}