package com.example.graphql.cfg.repository;

import com.example.graphql.cfg.model.LookupCfg;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LookupCfgRepository extends JpaRepository<LookupCfg, Long> {
}
