package com.example.graphql.person.dto;

import java.util.List;

public record CreatePersonInput(String name, String email, Integer age, List<AddressInput> addresses) {}
