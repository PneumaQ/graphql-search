package com.example.graphql.cfg.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lookup_cfg")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LookupCfg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;
    
    @Column(name = "lookup_type")
    private String lookupType; // e.g., "BRAND", "COLOR_PALETTE"
}
