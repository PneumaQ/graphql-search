package com.example.graphql.publications.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Document(indexName = "publication")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Publication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Keyword)
    private String journalName;

    @Field(type = FieldType.Date, format = org.springframework.data.elasticsearch.annotations.DateFormat.date)
    private LocalDate publicationDate;

    @Field(type = FieldType.Keyword)
    private String status; // e.g., "SUBMITTED", "PUBLISHED"

    @Field(type = FieldType.Keyword)
    private String doi;

    @OneToMany(mappedBy = "publication", cascade = CascadeType.ALL, orphanRemoval = true)
    @Field(type = FieldType.Nested)
    private List<PublicationAuthor> authors = new ArrayList<>();
}
