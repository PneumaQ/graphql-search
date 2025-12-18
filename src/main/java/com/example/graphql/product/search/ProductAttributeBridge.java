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

        // We assume "attributes" object field is defined in the binder
        DocumentElement attributesObject = target.addObject("attributes");

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            if (key == null || value == null) continue;

            // We follow the _keyword convention
            // This will trigger the template defined in the binder matching "*_keyword"
            String fieldName = key + "_keyword";
            attributesObject.addValue(fieldName, value);
        }
    }
}
