package com.example.graphql;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Catch-all field for nested address search
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text, analyzer = "standard")
    @org.springframework.data.annotation.Transient
    private String allSearchContent;

    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text, copyTo = "allSearchContent")
    private String street;
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text, copyTo = "allSearchContent")
    private String city;

    @org.springframework.data.elasticsearch.annotations.MultiField(
            mainField = @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text, copyTo = "allSearchContent"),
            otherFields = { @org.springframework.data.elasticsearch.annotations.InnerField(suffix = "keyword", type = org.springframework.data.elasticsearch.annotations.FieldType.Keyword) }
    )
    private String state;

    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text, copyTo = "allSearchContent")
    private String zip;

    @org.springframework.data.elasticsearch.annotations.MultiField(
            mainField = @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text, copyTo = "allSearchContent"),
            otherFields = { @org.springframework.data.elasticsearch.annotations.InnerField(suffix = "keyword", type = org.springframework.data.elasticsearch.annotations.FieldType.Keyword) }
    )
    private String country;
    
    private Boolean isPrimary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    @org.springframework.data.annotation.Transient
    private Person person;
}
