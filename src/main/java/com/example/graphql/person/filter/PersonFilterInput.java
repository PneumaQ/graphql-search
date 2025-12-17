package com.example.graphql.person.filter;

import com.example.graphql.platform.filter.*;
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
    StringFilter addresses_country,
    StringFilter addresses_state,
    StringFilter addresses_city
) {}