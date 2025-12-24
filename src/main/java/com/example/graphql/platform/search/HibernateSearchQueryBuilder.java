package com.example.graphql.platform.search;

import com.example.graphql.publications.graphql.input.PublicationFilterInput;
import com.example.graphql.platform.filter.SearchConditionInput;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HibernateSearchQueryBuilder {

    public org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep build(SearchPredicateFactory f, PublicationFilterInput filter) {
        BooleanPredicateClausesStep<?> bool = f.bool();
        boolean hasClause = false;

        if (filter.and() != null) {
            for (PublicationFilterInput sub : filter.and()) {
                bool.must(build(f, sub));
            }
            hasClause = true;
        }

        if (filter.or() != null) {
            BooleanPredicateClausesStep<?> orBool = f.bool();
            for (PublicationFilterInput sub : filter.or()) {
                orBool.should(build(f, sub));
            }
            bool.must(orBool);
            hasClause = true;
        }

        if (filter.title() != null) {
            bool.must(f.match().field("title").matching(filter.title()));
            hasClause = true;
        }
        
        if (filter.journalName() != null) {
            bool.must(f.match().field("journalName").matching(filter.journalName()));
            hasClause = true;
        }

        if (filter.status() != null) {
            bool.must(f.match().field("status_keyword").matching(filter.status()));
            hasClause = true;
        }

        return hasClause ? bool : f.matchAll();
    }
}