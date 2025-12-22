package com.example.graphql.cfg.service;

import com.example.graphql.cfg.model.LookupCfg;
import com.example.graphql.cfg.model.LookupCfgRecord;
import com.example.graphql.cfg.repository.LookupCfgRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

@Service
public class CfgCacheService {

    private final LookupCfgRepository repository;
    private final Cache<Long, LookupCfgRecord> cache;

    public CfgCacheService(LookupCfgRepository repository) {
        this.repository = repository;
        this.cache = Caffeine.newBuilder()
                .maximumSize(1000)
                .build();
    }

    public LookupCfgRecord getLookup(Long id) {
        if (id == null) return null;
        return cache.get(id, k -> {
            System.out.println("DEBUG: CfgCache miss for Lookup ID: " + k);
            return repository.findById(k)
                    .map(this::mapToRecord)
                    .orElse(null);
        });
    }

    public void primeCache() {
        System.out.println("DEBUG: Priming CfgCache with Records...");
        repository.findAll().forEach(lookup -> cache.put(lookup.getId(), mapToRecord(lookup)));
    }

    private LookupCfgRecord mapToRecord(LookupCfg entity) {
        return new LookupCfgRecord(entity.getId(), entity.getName(), entity.getDescription(), entity.getLookupType());
    }
    
    public void evict(Long id) {
        cache.invalidate(id);
    }
}
