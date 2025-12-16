package com.example.graphql.filter;

public record IntFilter(Integer eq, Integer gt, Integer lt, Integer gte, Integer lte) {}