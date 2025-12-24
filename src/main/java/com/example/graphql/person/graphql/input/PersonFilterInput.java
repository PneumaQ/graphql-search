package com.example.graphql.person.graphql.input;

import java.util.List;

public record PersonFilterInput(
    String name,
    Integer age,
    String email,
    List<PersonFilterInput> and,
    List<PersonFilterInput> or,
    PersonFilterInput not
) {}