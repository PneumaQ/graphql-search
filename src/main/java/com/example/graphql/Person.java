package com.example.graphql;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int age;

    @jakarta.persistence.OneToMany(mappedBy = "person", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Address> addresses = new java.util.ArrayList<>();
}
