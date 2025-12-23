package com.example.graphql.platform.metadata;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class EntityCfg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; 
    private boolean aggregateRoot; // e.g. Product=true, Review=false

    @OneToMany(mappedBy = "parentEntity", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<PropertyCfg> properties;

    /**
     * Finds a property that represents a link to another entity.
     * In a real system, this would use RelationType (OneToMany, etc.)
     */
    public PropertyCfg getPropertyRepresentingEntity(EntityCfg child) {
        return properties.stream()
            .filter(p -> child.getName().equalsIgnoreCase(p.getRepresentedEntityName()))
            .findFirst()
            .orElse(null);
    }
}