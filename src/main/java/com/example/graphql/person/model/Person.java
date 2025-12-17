package com.example.graphql.person.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

@Entity
@Indexed(index = "person")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @FullTextField(analyzer = "standard")
    @KeywordField(name = "name_keyword", sortable = Sortable.YES, aggregable = Aggregable.YES) // Creates name.keyword
    private String name;
    
    @GenericField(sortable = Sortable.YES)
    private int age;

    @FullTextField(analyzer = "standard")
    @KeywordField(name = "email_keyword", aggregable = Aggregable.YES) // Creates email.keyword
    private String email;

    @KeywordField
    private String phoneNumber;

    @GenericField(sortable = Sortable.YES)
    private java.time.LocalDate birthDate;

    @GenericField(aggregable = Aggregable.YES)
    private Boolean isActive;

    @GenericField(sortable = Sortable.YES)
    private Double salary;

    @jakarta.persistence.OneToMany(mappedBy = "person", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @IndexedEmbedded
    @BatchSize(size = 20)
    private java.util.List<Address> addresses = new java.util.ArrayList<>();
}