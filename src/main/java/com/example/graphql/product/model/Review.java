package com.example.graphql.product.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.engine.backend.types.Aggregable;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @FullTextField(analyzer = "standard")
    @KeywordField(name = "author_keyword", sortable = Sortable.YES, normalizer = "lowercase")
    private String author;

    @FullTextField(analyzer = "standard")
    private String comment;

    @GenericField(sortable = Sortable.YES, aggregable = Aggregable.YES)
    private Integer rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
}
