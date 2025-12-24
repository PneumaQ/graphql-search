package com.example.graphql.publications.graphql.filter;

import java.util.List;

public record PublicationFilterInput(
    String title,
    String journalName,
    String status,
    List<PublicationFilterInput> and,
    List<PublicationFilterInput> or,
    PublicationFilterInput not
) {}