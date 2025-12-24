package com.example.graphql.person.service;

import com.example.graphql.person.graphql.input.AddressInput;
import com.example.graphql.person.graphql.input.CreatePersonInput;
import com.example.graphql.person.graphql.input.UpdatePersonInput;
import com.example.graphql.person.model.Address;
import com.example.graphql.person.model.Person;
import com.example.graphql.person.repository.jpa.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonMergeService {

    private final PersonRepository personRepository;

    public PersonMergeService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Transactional(readOnly = true)
    public Person mergeCreate(CreatePersonInput input) {
        Person person = new Person();
        person.setName(input.name());
        person.setEmail(input.email());
        if (input.age() != null) person.setAge(input.age());
        
        if (input.addresses() != null) {
            for (AddressInput ai : input.addresses()) {
                addOrUpdateAddress(person, ai);
            }
        }
        return person;
    }

    @Transactional(readOnly = true)
    public Person mergeUpdate(UpdatePersonInput input) {
        Person person = personRepository.findById(input.id())
                .orElseThrow(() -> new RuntimeException("Person not found with ID: " + input.id()));

        if (input.name() != null) person.setName(input.name());
        if (input.email() != null) person.setEmail(input.email());
        if (input.age() != null) person.setAge(input.age());

        if (input.addresses() != null) {
            for (AddressInput ai : input.addresses()) {
                addOrUpdateAddress(person, ai);
            }
        }
        return person;
    }

    private void addOrUpdateAddress(Person person, AddressInput ai) {
        if (ai.id() != null) {
            person.getAddresses().stream()
                    .filter(a -> a.getId().equals(ai.id()))
                    .findFirst()
                    .ifPresent(existing -> {
                        if (ai.street() != null) existing.setStreet(ai.street());
                        if (ai.city() != null) existing.setCity(ai.city());
                        if (ai.country() != null) existing.setCountry(ai.country());
                    });
        } else {
            Address newAddr = new Address();
            newAddr.setStreet(ai.street());
            newAddr.setCity(ai.city());
            newAddr.setCountry(ai.country());
            newAddr.setPerson(person);
            person.getAddresses().add(newAddr);
        }
    }
}