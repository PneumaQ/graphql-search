package com.example.graphql.product.model;

import com.example.graphql.cfg.converter.LookupCfgAttributeConverter;
import com.example.graphql.cfg.model.LookupCfgRecord;
import com.example.graphql.product.search.ProductAttributeBinder;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.PropertyBinding;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.PropertyBinderRef;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @KeywordField(name = "name_keyword", sortable = Sortable.YES, normalizer = "lowercase", aggregable = org.hibernate.search.engine.backend.types.Aggregable.YES)
    private String name;

    @FullTextField(analyzer = "standard")
    @KeywordField(name = "sku_keyword", sortable = org.hibernate.search.engine.backend.types.Sortable.YES, normalizer = "lowercase", aggregable = org.hibernate.search.engine.backend.types.Aggregable.YES)
    private String internalStockCode;

    @KeywordField(name = "category_keyword", sortable = Sortable.YES, normalizer = "lowercase", aggregable = org.hibernate.search.engine.backend.types.Aggregable.YES)
    private String category;

    @org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField(sortable = Sortable.YES, aggregable = org.hibernate.search.engine.backend.types.Aggregable.YES)
    private Double price;

    @Convert(converter = LookupCfgAttributeConverter.class)
    @Column(name = "brand_id")
    @IndexedEmbedded(includePaths = {"name", "name_keyword", "description"})
    private LookupCfgRecord brand;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_attributes", columnDefinition = "json")
    @PropertyBinding(binder = @PropertyBinderRef(type = ProductAttributeBinder.class))
    private Map<String, String> custom_attributes = new HashMap<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @IndexedEmbedded
    private List<Review> reviews = new ArrayList<>();
}