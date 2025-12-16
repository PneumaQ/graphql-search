package com.example.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import java.util.List;
import java.util.Optional;

import com.example.graphql.filter.PersonFilterInput;
import com.example.graphql.filter.PersonSort;
import com.example.graphql.filter.SortField;
import com.example.graphql.filter.SortDirection;

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

    @QueryMapping
    public List<NameFacet> nameFacets() {
        return personService.getNameFacets().entrySet().stream()
                .map(entry -> new NameFacet(entry.getKey(), entry.getValue().intValue()))
                .collect(java.util.stream.Collectors.toList());
    }

    @QueryMapping
    public PersonConnection searchWithFacets(
            @Argument String text, 
            @Argument com.example.graphql.filter.PersonFilterInput filter,
            @Argument Integer page,
            @Argument Integer size,
            @Argument PersonSort sort) {
        
        int pageNum = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 10;
        
        org.springframework.data.domain.Sort springSort = org.springframework.data.domain.Sort.unsorted();
        if (sort != null) {
            String fieldName = switch (sort.field()) {
                case NAME -> "name.keyword"; // Sort on keyword sub-field
                case AGE -> "age";
                case SALARY -> "salary";
                case BIRTH_DATE -> "birthDate";
            };
            
            org.springframework.data.domain.Sort.Direction direction = (sort.direction() == SortDirection.DESC) 
                    ? org.springframework.data.domain.Sort.Direction.DESC 
                    : org.springframework.data.domain.Sort.Direction.ASC;
            
            springSort = org.springframework.data.domain.Sort.by(direction, fieldName);
        }
        
        PersonService.PersonSearchResponse response = personService.searchWithFacets(text, filter, org.springframework.data.domain.PageRequest.of(pageNum, pageSize, springSort));
        
        List<FacetBucket> active = mapFacets(response.activeCounts());
        List<FacetBucket> country = mapFacets(response.countryCounts());
        List<FacetBucket> state = mapFacets(response.stateCounts());
        
        return new PersonConnection(
            response.items(), 
            new PersonFacets(active, country, state),
            (int) response.totalElements(),
            response.totalPages()
        );
    }

    private List<FacetBucket> mapFacets(java.util.Map<String, Long> counts) {
        return counts.entrySet().stream()
                .map(e -> new FacetBucket(e.getKey(), e.getValue().intValue()))
                .collect(java.util.stream.Collectors.toList());
    }

    public record NameFacet(String value, int count) {}
    public record PersonConnection(List<Person> items, PersonFacets facets, int totalElements, int totalPages) {}
    public record PersonFacets(List<FacetBucket> byActive, List<FacetBucket> byCountry, List<FacetBucket> byState) {}
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
