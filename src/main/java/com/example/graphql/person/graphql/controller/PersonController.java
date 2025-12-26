package com.example.graphql.person.graphql.controller;

import com.example.graphql.person.domain.model.Person;
import com.example.graphql.person.domain.service.PersonService;
import com.example.graphql.platform.filter.SearchConditionInput;
import com.example.graphql.person.graphql.input.CreatePersonInput;
import com.example.graphql.person.graphql.input.PersonSortInput;
import com.example.graphql.person.graphql.input.UpdatePersonInput;
import com.example.graphql.person.graphql.type.PersonSearchResult;
import graphql.GraphQLContext;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @QueryMapping
    public PersonSearchResult searchPeople(
            @Argument String text,
            @Argument List<SearchConditionInput> filter,
            @Argument List<String> facetKeys,
            @Argument List<String> statsKeys,
            @Argument List<PersonSortInput> sort,
            @Argument Integer page,
            @Argument Integer size,
            GraphQLContext context) {
        
        return personService.searchPeople(
            text, 
            filter, 
            facetKeys, 
            statsKeys, 
            page != null ? page : 0, 
            size != null ? size : 10, 
            context
        );
    }

    @MutationMapping
    public Person createPerson(@Argument CreatePersonInput input) {
        return personService.createPerson(input);
    }

    @MutationMapping
    public Person updatePerson(@Argument UpdatePersonInput input) {
        return personService.updatePerson(input);
    }
}
