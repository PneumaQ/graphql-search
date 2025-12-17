package com.example.graphql.person.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;
import org.hibernate.search.engine.backend.types.Aggregable;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @FullTextField(analyzer = "standard")
    private String street;
    
    @FullTextField(analyzer = "standard")
    private String city;

    @FullTextField(analyzer = "standard")
    @KeywordField(name = "state_keyword", aggregable = Aggregable.YES)
    private String state;

    @KeywordField
    private String zip;

    @FullTextField(analyzer = "standard")
    @KeywordField(name = "country_keyword", aggregable = Aggregable.YES)
    private String country;
    
    @GenericField
    private Boolean isPrimary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private Person person;
}