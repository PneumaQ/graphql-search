package com.example.graphql.platform.metadata.graphql.controller;

import com.example.graphql.platform.metadata.PropertyCfgRepository;
import com.example.graphql.platform.metadata.PropertyMetadata;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class MetadataController {

    private final PropertyCfgRepository propertyCfgRepository;

    public MetadataController(PropertyCfgRepository propertyCfgRepository) {
        this.propertyCfgRepository = propertyCfgRepository;
    }

    @QueryMapping
    public List<PropertyMetadata> metadata(@Argument String entityName) {
        return propertyCfgRepository.findAll().stream()
            .filter(p -> p.getParentEntity().getName().equalsIgnoreCase(entityName))
            .map(p -> new PropertyMetadata(p.getPropertyName(), p.getDataType(), p.getDotPath(p.getParentEntity())))
            .toList();
    }
}
