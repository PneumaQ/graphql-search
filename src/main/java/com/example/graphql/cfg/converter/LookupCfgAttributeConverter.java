package com.example.graphql.cfg.converter;

import com.example.graphql.cfg.model.LookupCfgRecord;
import com.example.graphql.cfg.service.CfgCacheService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Converter
@Component
public class LookupCfgAttributeConverter implements AttributeConverter<LookupCfgRecord, Long> {

    private static CfgCacheService cacheService;

    @Autowired
    public void setCacheService(CfgCacheService service) {
        LookupCfgAttributeConverter.cacheService = service;
    }

    @Override
    public Long convertToDatabaseColumn(LookupCfgRecord attribute) {
        return (attribute == null) ? null : attribute.id();
    }

    @Override
    public LookupCfgRecord convertToEntityAttribute(Long dbData) {
        if (dbData == null || cacheService == null) {
            return null;
        }
        return cacheService.getLookup(dbData);
    }
}
