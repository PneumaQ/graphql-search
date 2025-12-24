package com.example.graphql.publications.repository.search;

import com.example.graphql.publications.model.Publication;
import com.example.graphql.publications.graphql.input.PublicationFilterInput;
import com.example.graphql.publications.graphql.input.PublicationSortInput;
import com.example.graphql.publications.graphql.input.PublicationSortField;
import com.example.graphql.platform.filter.SortDirection;
import com.example.graphql.platform.search.HibernateSearchQueryBuilder;
import com.example.graphql.publications.graphql.type.PublicationSearchResult;

import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.engine.search.aggregation.AggregationKey;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;

@Repository
public class PublicationSearchRepository {

    private final EntityManager entityManager;
    private final HibernateSearchQueryBuilder queryBuilder;

    public PublicationSearchRepository(EntityManager entityManager, HibernateSearchQueryBuilder queryBuilder) {
        this.entityManager = entityManager;
        this.queryBuilder = queryBuilder;
    }

    public PublicationSearchResult searchWithFacets(String text, PublicationFilterInput filter, List<PublicationSortInput> sort, int page, int size) {
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
                        .should(f.match().field("authors.person.name").matching(text))
                     );
                } else {
                    root.must(f.matchAll());
                }
                return root;
            })
            .sort(f -> {
                if (sort != null && !sort.isEmpty()) {
                    var composite = f.composite();
                    for (PublicationSortInput s : sort) {
                        String fieldPath = switch (s.field()) {
                            case TITLE -> "title_keyword";
                            case JOURNAL_NAME -> "journalName_keyword";
                            case PUBLICATION_DATE -> "publicationDate";
                            case STATUS -> "status_keyword";
                        };
                        var fieldSort = f.field(fieldPath);
                        if (s.direction() == SortDirection.DESC) fieldSort.desc(); else fieldSort.asc();
                        composite.add(fieldSort);
                    }
                    return composite;
                }
                return f.score();
            })
            .aggregation(statusKey, f -> f.terms().field("status_keyword", String.class))
            .aggregation(journalKey, f -> f.terms().field("journalName_keyword", String.class))
            .fetch(page * size, size);

        long total = result.total().hitCount();
        int totalPages = (int) Math.ceil((double) total / size);
        
        return new PublicationSearchResult(
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