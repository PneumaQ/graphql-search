package com.example.graphql.person.controller;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import java.util.List;
import java.util.Optional;

import com.example.graphql.person.filter.PersonFilterInput;
import com.example.graphql.person.filter.PersonSort;
import com.example.graphql.person.filter.PersonSortField;
import com.example.graphql.platform.filter.SortDirection;
import com.example.graphql.person.model.Person;
import com.example.graphql.person.service.PersonService;

@Controller
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @QueryMapping
    public Optional<Person> personById(@Argument Long id) {
        return personService.findById(id);
    }

    @QueryMapping
    public PersonConnection searchPeople(
            @Argument String text, 
            @Argument PersonFilterInput filter,
            @Argument Integer page,
            @Argument Integer size,
            @Argument List<PersonSort> sort) {
        
        int pageNum = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 10;
        
        org.springframework.data.domain.Sort springSort = org.springframework.data.domain.Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            List<org.springframework.data.domain.Sort.Order> orders = sort.stream()
                .map(s -> {
                    String fieldName = switch (s.field()) {
                        case NAME -> "name.keyword";
                        case AGE -> "age";
                        case SALARY -> "salary";
                        case BIRTH_DATE -> "birthDate";
                    };
                    org.springframework.data.domain.Sort.Direction direction = (s.direction() == SortDirection.DESC) 
                        ? org.springframework.data.domain.Sort.Direction.DESC 
                        : org.springframework.data.domain.Sort.Direction.ASC;
                    return new org.springframework.data.domain.Sort.Order(direction, fieldName);
                })
                .collect(java.util.stream.Collectors.toList());
            springSort = org.springframework.data.domain.Sort.by(orders);
        }
        
        PersonService.PersonSearchResponse response = personService.searchWithFacets(text, filter, org.springframework.data.domain.PageRequest.of(pageNum, pageSize, springSort));
        
        List<FacetBucket> active = mapFacets(response.activeCounts());
        List<FacetBucket> country = mapFacets(response.countryCounts());
        List<FacetBucket> state = mapFacets(response.stateCounts());
        
        PersonStats stats = new PersonStats(
            mapStats(response.salaryStats()),
            mapStats(response.ageStats())
        );

        return new PersonConnection(
            response.results(), 
            new PersonFacets(active, country, state),
            stats,
            (int) response.totalElements(),
            response.totalPages()
        );
    }

    private List<FacetBucket> mapFacets(java.util.Map<String, Long> counts) {
        return counts.entrySet().stream()
                .map(e -> new FacetBucket(e.getKey(), e.getValue().intValue()))
                .collect(java.util.stream.Collectors.toList());
    }
    
    private NumericStats mapStats(PersonService.NumericStats stats) {
        if (stats == null) return null;
        return new NumericStats(stats.min(), stats.max(), stats.avg(), stats.sum(), (int) stats.count());
    }

    public record NameFacet(String value, int count) {}
    public record PersonConnection(List<Person> results, PersonFacets facets, PersonStats stats, int totalElements, int totalPages) {}
    public record PersonFacets(List<FacetBucket> byActive, List<FacetBucket> byCountry, List<FacetBucket> byState) {}
    public record PersonStats(NumericStats salary, NumericStats age) {}
    public record NumericStats(Double min, Double max, Double avg, Double sum, Integer count) {}
    public record FacetBucket(String value, int count) {}

    @MutationMapping
    public Person createPerson(
            @Argument String name, 
            @Argument int age,
            @Argument String email,
            @Argument String phoneNumber,
            @Argument String birthDate,
            @Argument Boolean isActive,
            @Argument Double salary) {
        
        java.time.LocalDate parsedDate = (birthDate != null) ? java.time.LocalDate.parse(birthDate) : null;
        
        return personService.save(new Person(null, null, name, age, email, phoneNumber, parsedDate, isActive, salary, new java.util.ArrayList<>()));
    }

    @MutationMapping
    public Person addAddress(
            @Argument Long personId, 
            @Argument String street, 
            @Argument String city, 
            @Argument String state, 
            @Argument String zip,
            @Argument String country,
            @Argument Boolean isPrimary) {
        return personService.addAddress(personId, street, city, state, zip, country, isPrimary);
    }
}
