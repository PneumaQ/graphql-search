package com.example.graphql.person.graphql.controller;

import com.example.graphql.person.graphql.input.CreatePersonInput;
import com.example.graphql.person.graphql.input.UpdatePersonInput;
import com.example.graphql.person.model.Person;
import com.example.graphql.person.model.Address;
import com.example.graphql.person.service.PersonMergeService;
import com.example.graphql.person.service.PersonService;
import com.example.graphql.platform.filter.SearchCondition;
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
        Person person = personMergeService.mergeCreate(input);
        return personService.savePerson(person);
    }

    @MutationMapping
    public Person updatePerson(@Argument UpdatePersonInput input) {
        Person person = personMergeService.mergeUpdate(input);
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