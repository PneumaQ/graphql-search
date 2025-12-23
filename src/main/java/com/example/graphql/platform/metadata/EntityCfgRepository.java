package com.example.graphql.platform.metadata;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EntityCfgRepository extends JpaRepository<EntityCfg, Long> {
    Optional<EntityCfg> findByName(String name);
}
