package com.example.graphql.publications.repository.search;

import com.example.graphql.publications.model.Publication;
import com.example.graphql.publications.filter.PublicationFilterInput;
import com.example.graphql.platform.search.HibernateSearchQueryBuilder;
import com.example.graphql.publications.service.PublicationService.PublicationSearchResponse;

import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.engine.search.aggregation.AggregationKey;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class PublicationSearchRepository {

    private final EntityManager entityManager;
    private final HibernateSearchQueryBuilder queryBuilder;

    public PublicationSearchRepository(EntityManager entityManager, HibernateSearchQueryBuilder queryBuilder) {
        this.entityManager = entityManager;
        this.queryBuilder = queryBuilder;
    }

    public PublicationSearchResponse searchWithFacets(String text, PublicationFilterInput filter, Pageable pageable) {
        SearchSession searchSession = Search.session(entityManager);
        
        AggregationKey<Map<String, Long>> statusKey = AggregationKey.of("status_counts");
        AggregationKey<Map<String, Long>> journalKey = AggregationKey.of("journal_counts");
        
        SearchResult<Publication> result = searchSession.search(Publication.class)
            .where(f -> {
                var root = f.bool();
                
                if (filter != null) {
                    root.filter(queryBuilder.build(f, filter));
                }

                if (text != null && !text.trim().isEmpty()) {
                     root.must(f.bool()
                        .should(f.match().field("title").matching(text))
                        .should(f.match().field("authors.affiliationAtTimeOfPublication").matching(text))
                        .should(f.match().field("authors.person.name_keyword").matching(text)) // Updated to name_keyword just in case, or match standard analyzer field "authors.person.name"
                     );
                     // Let's stick to standard match on full text fields
                     // "authors.person.name" is the @FullTextField
                } else {
                    root.must(f.matchAll());
                }
                return root;
            })
            .sort(f -> {
                if (pageable.getSort().isSorted()) {
                    var composite = f.composite();
                    for (Sort.Order order : pageable.getSort()) {
                        var fieldSort = f.field(order.getProperty());
                        if (order.isDescending()) fieldSort.desc(); else fieldSort.asc();
                        composite.add(fieldSort);
                    }
                    return composite;
                }
                return f.score();
            })
            .aggregation(statusKey, f -> f.terms().field("status_keyword", String.class))
            .aggregation(journalKey, f -> f.terms().field("journalName_keyword", String.class))
            
            .fetch((int) pageable.getOffset(), pageable.getPageSize());

        long total = result.total().hitCount();
        int totalPages = (int) Math.ceil((double) total / pageable.getPageSize());
        
        return new PublicationSearchResponse(
            result.hits(),
            result.aggregation(statusKey),
            result.aggregation(journalKey),
            total,
            totalPages
        );
    }

    public List<Publication> searchPublications(String text) {
        SearchSession searchSession = Search.session(entityManager);
        return searchSession.search(Publication.class)
            .where(f -> f.bool(b -> {
                b.should(f.match().field("title").matching(text));
                b.should(f.match().field("authors.affiliationAtTimeOfPublication").matching(text));
                b.should(f.match().field("authors.person.name").matching(text));
            }))
            .fetchHits(20);
    }
}