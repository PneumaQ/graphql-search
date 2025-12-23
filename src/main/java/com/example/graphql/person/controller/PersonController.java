package com.example.graphql.person.controller;

import com.example.graphql.person.model.Person;
import com.example.graphql.person.model.Address;
import com.example.graphql.person.service.PersonService;
import com.example.graphql.product.filter.SearchCondition;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @QueryMapping
    public PersonService.PersonSearchResponse searchPeople(
            @Argument String text,
            @Argument List<SearchCondition> filter,
            @Argument Integer page,
            @Argument Integer size) {
        
        return personService.searchPeople(text, filter, page, size);
    }

    @BatchMapping
    public Map<Person, List<Address>> addresses(List<Person> people) {
        // Simple N+1 resolution for demo. In a real system, we'd use a Repository.
        // For the POC, we just return the already-loaded collection.
        return people.stream().collect(Collectors.toMap(
            p -> p,
            Person::getAddresses
        ));
    }
}