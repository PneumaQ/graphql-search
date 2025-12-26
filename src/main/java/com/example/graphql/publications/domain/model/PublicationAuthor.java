package com.example.graphql.publications.domain.model;

import com.example.graphql.person.domain.model.Person;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexingDependency;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicationAuthor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @GenericField
    private Integer rank;

    @GenericField
    private Boolean isCorresponding;

    @FullTextField(analyzer = "standard")
    private String affiliationAtTimeOfPublication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publication_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Publication publication;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "person_id")
    @IndexedEmbedded // This allows searching Publications by Author Name
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
    private Person person;
}