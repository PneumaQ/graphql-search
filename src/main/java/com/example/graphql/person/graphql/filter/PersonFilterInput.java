package com.example.graphql.person.graphql.filter;

import com.example.graphql.platform.filter.SearchCondition;
import java.util.List;

public record PersonFilterInput(
    String name,
    Integer age,
    String email,
    List<PersonFilterInput> and,
    List<PersonFilterInput> or,
    PersonFilterInput not
) {}
