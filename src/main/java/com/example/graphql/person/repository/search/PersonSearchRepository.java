package com.example.graphql.person.repository.search;

import com.example.graphql.person.model.Person;
import com.example.graphql.person.filter.PersonFilterInput;
import com.example.graphql.platform.search.HibernateSearchQueryBuilder;
import com.example.graphql.person.service.PersonService.PersonSearchResponse;
import com.example.graphql.person.service.PersonService.NumericStats;

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
public class PersonSearchRepository {

    private final EntityManager entityManager;
    private final HibernateSearchQueryBuilder queryBuilder;

    public PersonSearchRepository(EntityManager entityManager, HibernateSearchQueryBuilder queryBuilder) {
        this.entityManager = entityManager;
        this.queryBuilder = queryBuilder;
    }

    public PersonSearchResponse searchWithFacets(String text, PersonFilterInput filter, Pageable pageable) {
        SearchSession searchSession = Search.session(entityManager);
        
        AggregationKey<Map<Boolean, Long>> activeKey = AggregationKey.of("active_counts");
        AggregationKey<Map<String, Long>> countryKey = AggregationKey.of("country_counts");
        AggregationKey<Map<String, Long>> stateKey = AggregationKey.of("state_counts");
        
        SearchResult<Person> result = searchSession.search(Person.class)
            .where(f -> {
                var root = f.bool();
                
                if (filter != null) {
                    root.filter(queryBuilder.build(f, filter));
                }

                if (text != null && !text.trim().isEmpty()) {
                     root.must(f.bool()
                        .should(f.match().field("name").matching(text))
                        .should(f.match().field("addresses.street").matching(text))
                        .should(f.match().field("addresses.city").matching(text))
                     );
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
            .aggregation(activeKey, f -> f.terms().field("isActive", Boolean.class))
            .aggregation(countryKey, f -> f.terms().field("addresses.country_keyword", String.class))
            .aggregation(stateKey, f -> f.terms().field("addresses.state_keyword", String.class))
            
            .fetch((int) pageable.getOffset(), pageable.getPageSize());

        long total = result.total().hitCount();
        int totalPages = (int) Math.ceil((double) total / pageable.getPageSize());
        
        Map<String, Long> activeCounts = result.aggregation(activeKey).entrySet().stream()
            .collect(Collectors.toMap(e -> String.valueOf(e.getKey()), Map.Entry::getValue));

        return new PersonSearchResponse(
            result.hits(),
            activeCounts,
            result.aggregation(countryKey),
            result.aggregation(stateKey),
            new NumericStats(0.0, 0.0, 0.0, 0.0, total), 
            new NumericStats(0.0, 0.0, 0.0, 0.0, total), 
            total,
            totalPages
        );
    }
    
    public List<Person> searchByName(String text) {
        SearchSession searchSession = Search.session(entityManager);
        return searchSession.search(Person.class)
            .where(f -> f.bool()
                .should(f.match().field("name").matching(text))
                .should(f.match().field("addresses.street").matching(text))
            ) // Defaults to minShouldMatch(1) as no must/filter
            .fetchHits(20);
    }

    public Map<String, Long> getNameFacets() {
        SearchSession searchSession = Search.session(entityManager);
        AggregationKey<Map<String, Long>> nameKey = AggregationKey.of("name_counts");
        
        return searchSession.search(Person.class)
            .where(f -> f.matchAll())
            .aggregation(nameKey, f -> f.terms().field("name_keyword", String.class).maxTermCount(10))
            .fetch(0).aggregation(nameKey);
    }
}
