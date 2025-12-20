package com.example.graphql.product.search;

import org.hibernate.search.engine.backend.document.IndexFieldReference;
import org.hibernate.search.engine.backend.document.model.dsl.IndexSchemaElement;
import org.hibernate.search.engine.backend.document.model.dsl.IndexSchemaObjectField;
import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.bridge.binding.PropertyBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.PropertyBinder;

import java.util.Map;

public class ProductAttributeBinder implements PropertyBinder {

        @Override

        public void bind(PropertyBindingContext context) {

            context.dependencies().useRootOnly();

    

            IndexSchemaElement schemaElement = context.indexSchemaElement();

    

            // define object field "custom_attributes"

            IndexSchemaObjectField attributesField = schemaElement.objectField("custom_attributes");

    

                    // Define a template for dynamic fields ending in "_keyword" inside "custom_attributes" object.

    

                    // They will be mapped as Keyword (normalized string), sortable and aggregable.

    

                    attributesField.fieldTemplate("attributeKeywordTemplate", f -> f.asString().normalizer("lowercase").sortable(Sortable.YES).aggregable(Aggregable.YES))

    

                            .matchingPathGlob("*_keyword");

    

            

    

                    // Define a second template for full-text search fields ending in "_text".

    

                    // They will be analyzed (broken into words) for Google-like search.

    

                    attributesField.fieldTemplate("attributeTextTemplate", f -> f.asString().analyzer("standard"))

    

                            .matchingPathGlob("*_text");

    

            

    

                    attributesField.toReference();

    

            

    

            context.bridge(Map.class, new ProductAttributeBridge());

        }

    }

    