package com.example.graphql.product.repository.search;

import com.example.graphql.product.model.Product;
import com.example.graphql.product.filter.ProductFilterInput;
import com.example.graphql.product.filter.ProductAttributeFilterInput;
import com.example.graphql.platform.search.HibernateSearchQueryBuilder;
import com.example.graphql.platform.filter.StringFilter;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import java.util.List;

@Repository
public class ProductSearchRepository {

    private final EntityManager entityManager;

    public ProductSearchRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Product> search(ProductFilterInput filter) {
        SearchSession searchSession = Search.session(entityManager);
        
        return searchSession.search(Product.class)
            .where(f -> buildPredicate(f, filter))
            .fetchHits(20);
    }

    private org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep buildPredicate(
            SearchPredicateFactory f, ProductFilterInput filter) {
        
        if (filter == null) {
            return f.matchAll();
        }

        BooleanPredicateClausesStep<?> bool = f.bool();

        // Name filter
        if (filter.name() != null) {
            applyStringFilter(f, bool, "name", filter.name());
        }

        // Dynamic Attribute filters
        if (filter.attributes() != null) {
            for (ProductAttributeFilterInput attr : filter.attributes()) {
                // Construct the dynamic path: "attributes.{key}_keyword"
                String fieldPath = "attributes." + attr.key() + "_keyword";
                
                // We reuse the StringFilter logic but apply it to the dynamic field
                if (attr.value() != null) {
                    applyStringFilter(f, bool, fieldPath, attr.value());
                }
            }
        }

        return bool;
    }

    private void applyStringFilter(
            SearchPredicateFactory f, 
            BooleanPredicateClausesStep<?> bool, 
            String field, 
            StringFilter filter) {
        
        if (filter.eq() != null) {
            bool.must(f.match().field(field).matching(filter.eq()));
        }
        
        if (filter.contains() != null) {
            bool.must(f.wildcard().field(field).matching("*" + filter.contains() + "*"));
        }
        
        if (filter.startsWith() != null) {
            bool.must(f.wildcard().field(field).matching(filter.startsWith() + "*"));
        }
        
        if (filter.endsWith() != null) {
            bool.must(f.wildcard().field(field).matching("*" + filter.endsWith()));
        }
        
        if (filter.in() != null && !filter.in().isEmpty()) {
            bool.must(f.terms().field(field).matchingAny(filter.in()));
        }
    }
}
