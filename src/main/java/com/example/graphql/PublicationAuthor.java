package com.example.graphql;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicationAuthor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Field(type = FieldType.Integer)
    private Integer rank; // 1st author, 2nd author, etc.

    @Field(type = FieldType.Boolean)
    private Boolean isCorresponding;

    @Field(type = FieldType.Text)
    private String affiliationAtTimeOfPublication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publication_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @org.springframework.data.annotation.Transient
    private Publication publication;

    @ManyToOne(fetch = FetchType.EAGER) // We generally want the person details when fetching an author
    @JoinColumn(name = "person_id")
    @Field(type = FieldType.Object) // In ES, this will embed the Person object by default
    private Person person;
}
