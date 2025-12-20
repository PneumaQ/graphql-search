package com.example.graphql.product.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.PropertyBinding;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.PropertyBinderRef;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import com.example.graphql.product.search.ProductAttributeBinder;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Entity
@Indexed(index = "product")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @FullTextField(analyzer = "standard")
    @KeywordField(name = "name_keyword", sortable = Sortable.YES, normalizer = "lowercase")
    private String name;

    @FullTextField(analyzer = "standard")
    @KeywordField(name = "sku_keyword", sortable = Sortable.YES, normalizer = "lowercase")
    private String sku;

    @KeywordField(name = "category_keyword", sortable = Sortable.YES, normalizer = "lowercase", aggregable = org.hibernate.search.engine.backend.types.Aggregable.YES)
    private String category;

    @org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField(sortable = Sortable.YES, aggregable = org.hibernate.search.engine.backend.types.Aggregable.YES)
    private Double price;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_attributes", columnDefinition = "json")
    @PropertyBinding(binder = @PropertyBinderRef(type = ProductAttributeBinder.class))
    private Map<String, String> custom_attributes = new HashMap<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @IndexedEmbedded
    private List<Review> reviews = new ArrayList<>();
}
