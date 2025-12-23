package com.example.graphql.cfg.model;

import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;
import org.hibernate.search.engine.backend.types.Sortable;

public record LookupCfgRecord(
    Long id,
    
    @FullTextField(analyzer = "standard")
    @KeywordField(name = "name_keyword", sortable = Sortable.YES, normalizer = "lowercase", aggregable = org.hibernate.search.engine.backend.types.Aggregable.YES)
    String name,
    
    @FullTextField(analyzer = "standard")
    String description,
    
    String lookupType
) {}
