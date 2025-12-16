package com.example.graphql.filter;

import java.util.List;

public record StringFilter(String eq, String contains, List<String> in) {}