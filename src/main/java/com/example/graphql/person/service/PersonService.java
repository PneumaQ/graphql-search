package com.example.graphql.person.service;

import com.example.graphql.person.model.Person;
import com.example.graphql.person.repository.search.PersonSearchRepository;
import com.example.graphql.platform.security.DacService;
import com.example.graphql.product.filter.SearchCondition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PersonService {

    private final PersonSearchRepository personSearchRepository;
    private final DacService dacService;

    public PersonService(PersonSearchRepository personSearchRepository, DacService dacService) {
        this.personSearchRepository = personSearchRepository;
        this.dacService = dacService;
    }

    @Transactional(readOnly = true)
    public PersonSearchResponse searchPeople(String text, List<SearchCondition> userFilters, List<String> facetKeys, List<String> statsKeys, Integer page, Integer size) {
        
        // 1. DYNAMIC SECURITY INJECTION (Person DACs!)
        List<SearchCondition> securityFilters = dacService.getSecurityConditions("Person");
        
        List<SearchCondition> allFilters = new java.util.ArrayList<>();
        if (userFilters != null) allFilters.addAll(userFilters);
        allFilters.addAll(securityFilters);

        int pageNum = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 10;

        var repoResponse = personSearchRepository.search(text, allFilters, facetKeys, statsKeys, pageNum, pageSize);
        
        return new PersonSearchResponse(
                repoResponse.results(), 
                repoResponse.facets(),
                repoResponse.stats(),
                (int) repoResponse.totalElements(), 
                repoResponse.totalPages()
        );
    }

    public record PersonSearchResponse(List<Person> results, Object facets, Object stats, int totalElements, int totalPages) {}
}