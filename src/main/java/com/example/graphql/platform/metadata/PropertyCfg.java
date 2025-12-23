package com.example.graphql.platform.metadata;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PropertyCfg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String propertyName; 
    private String dataType;     
    
    /**
     * The "Golden Key": This maps the Relational path to the Elasticsearch path.
     * e.g., for a Review rating, this might be "reviews.rating"
     */
    private String dotPath;

    // The name of the entity this property represents (if it's a relationship)
    private String representedEntityName; 

    @ManyToOne
    @JoinColumn(name = "parent_entity_id")
    private EntityCfg parentEntity;

    /**
     * DYNAMIC PATH RESOLUTION (Simulation of production logic)
     * Traverses from the root to this property to build the technical dot-path.
     */
    public String getDotPath(EntityCfg root) {
        String technicalPath = (dotPath != null && !dotPath.isEmpty()) ? dotPath : propertyName;

        if (parentEntity.getName().equalsIgnoreCase(root.getName())) {
            return technicalPath;
        }
        
        // Find the "Bridge" property on the root that leads to our parent
        PropertyCfg bridge = root.getPropertyRepresentingEntity(this.parentEntity);
        if (bridge != null) {
            return bridge.getDotPath(root) + "." + technicalPath;
        }
        
        return technicalPath;
    }
}
