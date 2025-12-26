package com.example.graphql.person.domain.service;

import com.example.graphql.person.domain.model.Person;
import com.example.graphql.person.domain.repository.PersonRepository;
import com.example.graphql.person.domain.repository.PersonSearchRepository;
import com.example.graphql.platform.filter.SearchConditionInput;
import com.example.graphql.person.graphql.input.CreatePersonInput;
import com.example.graphql.person.graphql.input.UpdatePersonInput;
import com.example.graphql.person.graphql.type.PersonSearchResult;
import graphql.GraphQLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final PersonSearchRepository personSearchRepository;
    private final PersonMergeService personMergeService;

    public PersonService(PersonRepository personRepository, 
                         PersonSearchRepository personSearchRepository,
                         PersonMergeService personMergeService) {
        this.personRepository = personRepository;
        this.personSearchRepository = personSearchRepository;
        this.personMergeService = personMergeService;
    }

    @Transactional(readOnly = true)
    public PersonSearchResult searchPeople(String text, List<SearchConditionInput> filter, List<String> facetKeys, List<String> statsKeys, int page, int size, GraphQLContext context) {
        var response = personSearchRepository.search(text, filter, facetKeys, statsKeys, page, size);
        
        return new PersonSearchResult(
            response.results(),
            response.facets(),
            response.stats(),
            (int) response.totalElements(),
            response.totalPages()
        );
    }

    @Transactional
    public Person createPerson(CreatePersonInput input) {
        Person person = personMergeService.merge(input, new Person());
        return personRepository.save(person);
    }

    @Transactional
    public Person updatePerson(UpdatePersonInput input) {
        Person person = personRepository.findById(Long.valueOf(input.id()))
            .orElseThrow(() -> new RuntimeException("Person not found: " + input.id()));
        
        person = personMergeService.merge(input, person);
        return personRepository.save(person);
    }
}
