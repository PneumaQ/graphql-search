package com.example.graphql.person.service;

import com.example.graphql.person.graphql.input.AddressInput;
import com.example.graphql.person.graphql.input.CreatePersonInput;
import com.example.graphql.person.graphql.input.UpdatePersonInput;
import com.example.graphql.person.model.Address;
import com.example.graphql.person.model.Person;
import com.example.graphql.person.repository.jpa.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonMergeServiceTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonMergeService personMergeService;

    @Test
    void shouldMergeCreateInput() {
        AddressInput addrInput = new AddressInput(null, "123 Main St", "NY", "USA");
        CreatePersonInput input = new CreatePersonInput("Gregg", "gregg@test.com", 40, List.of(addrInput));

        Person result = personMergeService.mergeCreate(input);

        assertEquals("Gregg", result.getName());
        assertEquals("gregg@test.com", result.getEmail());
        assertEquals(40, result.getAge());
        assertEquals(1, result.getAddresses().size());
        assertEquals("123 Main St", result.getAddresses().get(0).getStreet());
        assertEquals(result, result.getAddresses().get(0).getPerson()); // DDD Check
    }

    @Test
    void shouldMergeUpdateInputWithExistingAddress() {
        // Setup existing entity
        Person existingPerson = new Person();
        existingPerson.setId(1L);
        existingPerson.setName("Old Name");
        
        Address existingAddr = new Address();
        existingAddr.setId(100L);
        existingAddr.setStreet("Old Street");
        existingAddr.setPerson(existingPerson);
        existingPerson.getAddresses().add(existingAddr);

        when(personRepository.findById(1L)).thenReturn(Optional.of(existingPerson));

        // Update input: Change name and update existing address
        AddressInput updateAddr = new AddressInput(100L, "New Street", null, null);
        UpdatePersonInput input = new UpdatePersonInput(1L, "New Name", null, null, List.of(updateAddr));

        Person result = personMergeService.mergeUpdate(input);

        assertEquals("New Name", result.getName());
        assertEquals(1, result.getAddresses().size());
        assertEquals("New Street", result.getAddresses().get(0).getStreet());
        assertEquals(100L, result.getAddresses().get(0).getId());
    }

    @Test
    void shouldAddNewAddressDuringUpdate() {
        Person existingPerson = new Person();
        existingPerson.setId(1L);
        existingPerson.setAddresses(new ArrayList<>());

        when(personRepository.findById(1L)).thenReturn(Optional.of(existingPerson));

        AddressInput newAddrInput = new AddressInput(null, "New Way", "Paris", "France");
        UpdatePersonInput input = new UpdatePersonInput(1L, null, null, null, List.of(newAddrInput));

        Person result = personMergeService.mergeUpdate(input);

        assertEquals(1, result.getAddresses().size());
        assertEquals("New Way", result.getAddresses().get(0).getStreet());
        assertEquals(result, result.getAddresses().get(0).getPerson());
    }
}