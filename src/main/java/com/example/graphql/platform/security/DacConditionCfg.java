package com.example.graphql.platform.security;

import com.example.graphql.platform.metadata.PropertyCfg;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DacConditionCfg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "property_id")
    private PropertyCfg property;

    private String operator; // e.g., "EQ", "GTE"

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dac_condition_values", joinColumns = @JoinColumn(name = "dac_condition_id"))
    @Column(name = "condition_value")
    private List<String> conditionValues;

    @ManyToOne
    @JoinColumn(name = "dac_id")
    private DacCfg dac;
}
