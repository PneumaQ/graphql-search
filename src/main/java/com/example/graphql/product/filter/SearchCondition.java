package com.example.graphql.product.filter;

import java.util.List;

public record SearchCondition(
    String field,
    String eq,
    String contains,
    String startsWith,
    List<String> in,
    Double gt,
    Double lt,
    Double gte,
    Double lte,
    
    // Recursive logic
    List<SearchCondition> and,
    List<SearchCondition> or,
    SearchCondition not
) {}
