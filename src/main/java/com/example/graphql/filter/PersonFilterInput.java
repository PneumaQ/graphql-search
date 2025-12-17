package com.example.graphql.filter;

import java.util.List;

public record PersonFilterInput(
    StringFilter id,
    List<PersonFilterInput> and, 
    List<PersonFilterInput> or, 
    PersonFilterInput not,
    StringFilter name, 
    StringFilter email, 
    IntFilter age, 
    FloatFilter salary, 
    DateFilter birthDate, 
    BooleanFilter isActive,
    StringFilter address_country,
    StringFilter address_state,
    StringFilter address_city
) {}