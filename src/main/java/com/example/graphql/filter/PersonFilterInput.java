package com.example.graphql.filter;

import java.util.List;

public record PersonFilterInput(
    List<PersonFilterInput> and,
    List<PersonFilterInput> or,
    PersonFilterInput not,
    StringFilter name,
    StringFilter email,
    IntFilter age,
    FloatFilter salary,
    BooleanFilter isActive,
    StringFilter address_country,
    StringFilter address_state,
    StringFilter address_city
) {}