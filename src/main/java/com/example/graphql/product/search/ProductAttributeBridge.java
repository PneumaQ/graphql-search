package com.example.graphql.product.search;

import org.hibernate.search.mapper.pojo.bridge.PropertyBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.PropertyBridgeWriteContext;
import org.hibernate.search.engine.backend.document.DocumentElement;

import java.util.Map;

public class ProductAttributeBridge implements PropertyBridge {

    @Override
    public void write(DocumentElement target, Object bridgedElement, PropertyBridgeWriteContext context) {
        @SuppressWarnings("unchecked")
        Map<String, String> attributes = (Map<String, String>) bridgedElement;
        
        if (attributes == null) {
            return;
        }

        // We assume "custom_attributes" object field is defined in the binder
        DocumentElement attributesObject = target.addObject("custom_attributes");

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            if (key == null || value == null) continue;

            // 1. Keyword field (for exact match, sorting, faceting)
            attributesObject.addValue(key + "_keyword", value);

            // 2. Text field (for full-text search)
            attributesObject.addValue(key + "_text", value);
        }
    }
}
