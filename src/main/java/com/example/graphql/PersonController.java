package com.example.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import java.util.List;
import java.util.Optional;

@Controller
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @QueryMapping
    public List<Person> persons() {
        return personService.findAll();
    }

    @QueryMapping
    public Optional<Person> personById(@Argument Long id) {
        return personService.findById(id);
    }

    @QueryMapping
    public List<Person> personsByCity(@Argument String city) {
        return personService.findByCity(city);
    }

    @QueryMapping
    public List<Person> searchPeople(@Argument String text) {
        return personService.searchByName(text);
    }

    @MutationMapping
    public Person createPerson(@Argument String name, @Argument int age) {
        return personService.save(new Person(null, name, age, new java.util.ArrayList<>()));
    }

    @MutationMapping
    public Person addAddress(@Argument Long personId, @Argument String street, @Argument String city, @Argument String state, @Argument String zip) {
        return personService.addAddress(personId, street, city, state, zip);
    }
}
