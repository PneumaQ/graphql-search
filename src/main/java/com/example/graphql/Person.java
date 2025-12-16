package com.example.graphql;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Entity
@Document(indexName = "person")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Catch-all field for full-text search
    @Field(type = FieldType.Text, analyzer = "standard")
    @org.springframework.data.annotation.Transient // Not stored in DB
    private String allSearchContent;

    @org.springframework.data.elasticsearch.annotations.MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "standard", copyTo = "allSearchContent"),
            otherFields = { @org.springframework.data.elasticsearch.annotations.InnerField(suffix = "keyword", type = FieldType.Keyword) }
    )
    private String name;
    
    @Field(type = FieldType.Integer)
    private int age;

    @org.springframework.data.elasticsearch.annotations.MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "standard", copyTo = "allSearchContent"),
            otherFields = { @org.springframework.data.elasticsearch.annotations.InnerField(suffix = "keyword", type = FieldType.Keyword) }
    )
    private String email;

    @Field(type = FieldType.Keyword, copyTo = "allSearchContent")
    private String phoneNumber;

    @Field(type = FieldType.Date, format = org.springframework.data.elasticsearch.annotations.DateFormat.date)
    private java.time.LocalDate birthDate;

    @Field(type = FieldType.Boolean)
    private Boolean isActive;

    @Field(type = FieldType.Double)
    private Double salary;

    @jakarta.persistence.OneToMany(mappedBy = "person", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @Field(type = FieldType.Nested)
    private java.util.List<Address> addresses = new java.util.ArrayList<>();
}
