package com.example.graphql.person.controller;

import com.example.graphql.person.dto.CreatePersonInput;
import com.example.graphql.person.dto.UpdatePersonInput;
import com.example.graphql.person.model.Person;
import com.example.graphql.person.model.Address;
import com.example.graphql.person.service.PersonMergeService;
import com.example.graphql.person.service.PersonService;
import com.example.graphql.product.filter.SearchCondition;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class PersonController {

    private final PersonService personService;
    private final PersonMergeService personMergeService;

    public PersonController(PersonService personService, PersonMergeService personMergeService) {
        this.personService = personService;
        this.personMergeService = personMergeService;
    }

    @QueryMapping
    public PersonService.PersonSearchResponse searchPeople(
            @Argument String text,
            @Argument List<SearchCondition> filter,
            @Argument List<String> facetKeys,
            @Argument List<String> statsKeys,
            @Argument Integer page,
            @Argument Integer size) {
        
        return personService.searchPeople(text, filter, facetKeys, statsKeys, page, size);
    }

    @MutationMapping
    public Person createPerson(@Argument CreatePersonInput input) {
        // 1. HYDRATION: Call the merge service to get a JPA entity from the input
        Person person = personMergeService.mergeCreate(input);
        
        // 2. BUSINESS LOGIC: Perform any aggregate-level domain logic here
        // e.g. person.validateEmailDomain();
        
        // 3. PERSISTENCE: Pass the fully prepared entity to the domain service for saving
        return personService.savePerson(person);
    }

    @MutationMapping
    public Person updatePerson(@Argument UpdatePersonInput input) {
        // 1. HYDRATION: The merge service handles findById and applying the delta
        Person person = personMergeService.mergeUpdate(input);
        
        // 2. BUSINESS LOGIC: Decide if you want to allow this specific update
        // if (person.isLocked()) throw new RuntimeException("...");
        
        // 3. PERSISTENCE
        return personService.savePerson(person);
    }

    @BatchMapping
    public Map<Person, List<Address>> addresses(List<Person> people) {
        return people.stream().collect(Collectors.toMap(
            p -> p,
            Person::getAddresses
        ));
    }
}