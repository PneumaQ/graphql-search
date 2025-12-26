package com.example.graphql.product.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "custom_field_definition")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomFieldDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityType; // e.g., "PRODUCT"

    @Column(nullable = false)
    private String fieldKey; // e.g., "color"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FieldDataType dataType;

    private boolean searchable = true;

    public enum FieldDataType {
        STRING, INT, FLOAT, DOUBLE, BOOLEAN, DATE
    }
}
