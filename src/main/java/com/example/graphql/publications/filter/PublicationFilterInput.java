package com.example.graphql.publications.filter;

import com.example.graphql.platform.filter.*;
import java.util.List;

public record PublicationFilterInput(
    StringFilter id,
    List<PublicationFilterInput> and, 
    List<PublicationFilterInput> or, 
    PublicationFilterInput not,
    StringFilter title,
    StringFilter journalName,
    DateFilter publicationDate,
    StringFilter status,
    StringFilter doi
) {}
