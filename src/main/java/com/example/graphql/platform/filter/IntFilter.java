package com.example.graphql.platform.filter;

public record IntFilter(Integer eq, Integer gt, Integer lt, Integer gte, Integer lte) {}