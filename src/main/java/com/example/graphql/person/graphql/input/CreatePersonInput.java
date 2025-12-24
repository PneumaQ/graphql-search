package com.example.graphql.person.graphql.input;

import java.util.List;

public record CreatePersonInput(String name, String email, Integer age, List<AddressInput> addresses) {}