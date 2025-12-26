package com.example.graphql.publications.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Indexed(index = "publication")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Publication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @FullTextField(analyzer = "standard")
    @KeywordField(name = "title_keyword", sortable = Sortable.YES, aggregable = Aggregable.YES) // For sorting
    private String title;

    @KeywordField(name = "journalName_keyword", sortable = Sortable.YES, aggregable = Aggregable.YES)
    private String journalName;

    @GenericField(sortable = Sortable.YES)
    private LocalDate publicationDate;

    @KeywordField(name = "status_keyword", sortable = Sortable.YES, aggregable = Aggregable.YES)
    private String status; // e.g., "SUBMITTED", "PUBLISHED"

    @KeywordField(name = "doi_keyword", aggregable = Aggregable.YES)
    private String doi;

    @OneToMany(mappedBy = "publication", cascade = CascadeType.ALL, orphanRemoval = true)
    @IndexedEmbedded
    @BatchSize(size = 20)
    private List<PublicationAuthor> authors = new ArrayList<>();
}