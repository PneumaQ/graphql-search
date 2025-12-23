package com.example.graphql.platform.security;

import com.example.graphql.platform.metadata.EntityCfg;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DacCfg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private boolean active;

    @ManyToOne
    @JoinColumn(name = "target_entity_id")
    private EntityCfg targetEntity;

    @OneToMany(mappedBy = "dac", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<DacConditionCfg> conditions;
}
