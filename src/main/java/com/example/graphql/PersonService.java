package com.example.graphql;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public List<Person> findAll() {
        return personRepository.findAll();
    }

    public Optional<Person> findById(Long id) {
        return personRepository.findById(id);
    }

    public List<Person> findByCity(String city) {
        return personRepository.findByAddresses_City(city);
    }

    public Person save(Person person) {
        return personRepository.save(person);
    }

    @org.springframework.transaction.annotation.Transactional
    public Person addAddress(Long personId, String street, String city, String state, String zip) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new RuntimeException("Person not found"));
        
        Address address = new Address();
        address.setStreet(street);
        address.setCity(city);
        address.setState(state);
        address.setZip(zip);
        address.setPerson(person);
        
        person.getAddresses().add(address);
        return personRepository.save(person);
    }
}
