package com.example.graphql.person.domain.service;

import com.example.graphql.person.graphql.input.AddressInput;
import com.example.graphql.person.graphql.input.CreatePersonInput;
import com.example.graphql.person.graphql.input.UpdatePersonInput;
import com.example.graphql.person.domain.model.Address;
import com.example.graphql.person.domain.model.Person;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PersonMergeServiceTest {

    @InjectMocks
    private PersonMergeService personMergeService;

    @Test
    void shouldMergeCreateInput() {
        AddressInput addrInput = new AddressInput(null, "123 Main St", "NY", "USA");
        CreatePersonInput input = new CreatePersonInput("Gregg", "gregg@test.com", 40, List.of(addrInput));

        Person result = personMergeService.merge(input, new Person());

        assertEquals("Gregg", result.getName());
        assertEquals("gregg@test.com", result.getEmail());
        assertEquals(40, result.getAge());
        assertEquals(1, result.getAddresses().size());
        assertEquals("123 Main St", result.getAddresses().get(0).getStreet());
        assertEquals(result, result.getAddresses().get(0).getPerson()); 
    }

    @Test
    void shouldMergeUpdateInputWithExistingAddress() {
        Person existingPerson = new Person();
        existingPerson.setId(1L);
        existingPerson.setName("Old Name");
        
        Address existingAddr = new Address();
        existingAddr.setId(100L);
        existingAddr.setStreet("Old Street");
        existingAddr.setPerson(existingPerson);
        existingPerson.getAddresses().add(existingAddr);

        AddressInput updateAddr = new AddressInput(100L, "New Street", null, null);
        UpdatePersonInput input = new UpdatePersonInput(1L, "New Name", null, null, List.of(updateAddr));

        Person result = personMergeService.merge(input, existingPerson);

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

        AddressInput newAddrInput = new AddressInput(null, "New Way", "Paris", "France");
        UpdatePersonInput input = new UpdatePersonInput(1L, null, null, null, List.of(newAddrInput));

        Person result = personMergeService.merge(input, existingPerson);

        assertEquals(1, result.getAddresses().size());
        assertEquals("New Way", result.getAddresses().get(0).getStreet());
        assertEquals(result, result.getAddresses().get(0).getPerson());
    }
}
