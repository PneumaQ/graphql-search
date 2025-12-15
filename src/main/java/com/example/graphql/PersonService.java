package com.example.graphql;

import com.example.graphql.repository.jpa.PersonRepository;
import com.example.graphql.repository.search.PersonSearchRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final PersonSearchRepository personSearchRepository;

    public PersonService(PersonRepository personRepository, PersonSearchRepository personSearchRepository) {
        this.personRepository = personRepository;
        this.personSearchRepository = personSearchRepository;
    }

    public List<Person> findAll() {
        return personRepository.findAll();
    }

    public Optional<Person> findById(Long id) {
        return personRepository.findById(id);
    }
    
    public List<Person> searchByName(String name) {
        return personSearchRepository.findByNameContaining(name);
    }

    public List<Person> findByCity(String city) {
        return personRepository.findByAddresses_City(city);
    }

    public Person save(Person person) {
        Person saved = personRepository.save(person);
        personSearchRepository.save(saved);
        return saved;
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
        Person saved = personRepository.save(person);
        personSearchRepository.save(saved);
        saved.getAddresses().size(); // Force initialization of the collection
        return saved;
    }
}
