package com.example.graphql.person.graphql.input;

import java.util.List;

public record UpdatePersonInput(Long id, String name, String email, Integer age, List<AddressInput> addresses) {}