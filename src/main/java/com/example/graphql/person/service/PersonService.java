package com.example.graphql.person.service;

import com.example.graphql.person.repository.jpa.PersonRepository;
import com.example.graphql.person.repository.search.PersonSearchRepository;
import com.example.graphql.person.model.Person;
import com.example.graphql.person.model.Address;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import com.example.graphql.person.filter.PersonFilterInput;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final PersonSearchRepository personSearchRepository;

    public PersonService(PersonRepository personRepository, PersonSearchRepository personSearchRepository) {
        this.personRepository = personRepository;
        this.personSearchRepository = personSearchRepository;
    }

    public PersonSearchResponse searchWithFacets(String text, PersonFilterInput filter, org.springframework.data.domain.Pageable pageable) {
        return personSearchRepository.searchWithFacets(text, filter, pageable);
    }

    public record PersonSearchResponse(List<Person> results, Map<String, Long> activeCounts, Map<String, Long> countryCounts, Map<String, Long> stateCounts, NumericStats ageStats, NumericStats salaryStats, long totalElements, int totalPages) {}
    
    public record NumericStats(double min, double max, double avg, double sum, long count) {}

    public Map<String, Long> getNameFacets() {
        return personSearchRepository.getNameFacets();
    }

    public List<Person> findAll() {
        return personRepository.findAll();
    }

    public Optional<Person> findById(Long id) {
        return personRepository.findById(id);
    }
    
    public List<Person> searchByName(String text) {
        return personSearchRepository.searchByName(text);
    }

    public List<Person> findByCity(String city) {
        return personRepository.findByAddresses_City(city);
    }

    @Transactional
    public Person save(Person person) {
        return personRepository.save(person);
    }

    @Transactional
    public Person addAddress(Long personId, String street, String city, String state, String zip, String country, Boolean isPrimary) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new RuntimeException("Person not found"));
        
        Address address = new Address();
        address.setStreet(street);
        address.setCity(city);
        address.setState(state);
        address.setZip(zip);
        address.setCountry(country);
        address.setIsPrimary(isPrimary);
        address.setPerson(person);
        
        person.getAddresses().add(address);
        return personRepository.save(person);
    }
}
