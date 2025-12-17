package com.example.graphql.platform.search;

import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep;
import org.springframework.stereotype.Component;
import java.lang.reflect.RecordComponent;
import java.util.List;
import com.example.graphql.platform.filter.*;

@Component
public class HibernateSearchQueryBuilder {

    public PredicateFinalStep build(SearchPredicateFactory f, Object filter) {
        if (filter == null) return f.matchAll();

        var bool = f.bool();
        boolean hasClauses = false;

        try {
            Class<?> clazz = filter.getClass();
            if (!clazz.isRecord()) {
                throw new IllegalArgumentException("Filter object must be a Java Record");
            }

            for (RecordComponent component : clazz.getRecordComponents()) {
                String fieldName = component.getName();
                Object value = component.getAccessor().invoke(filter);

                if (value == null) continue;

                if (fieldName.equals("and") && value instanceof List<?> list) {
                    for (Object sub : list) {
                        bool.must(build(f, sub));
                        hasClauses = true; 
                    }
                } else if (fieldName.equals("or") && value instanceof List<?> list) {
                    var orBool = f.bool();
                    for (Object sub : list) {
                         orBool.should(build(f, sub));
                    }
                    if (!list.isEmpty()) {
                        bool.must(orBool);
                        hasClauses = true;
                    }
                } else if (fieldName.equals("not")) {
                    bool.mustNot(build(f, value));
                    hasClauses = true;
                } 
                else if (value instanceof StringFilter sf) {
                    bool.must(buildStringQuery(f, resolveField(fieldName), sf));
                    hasClauses = true;
                } else if (value instanceof IntFilter inf) {
                    bool.must(buildIntQuery(f, resolveField(fieldName), inf));
                    hasClauses = true;
                } else if (value instanceof FloatFilter ff) {
                    bool.must(buildFloatQuery(f, resolveField(fieldName), ff));
                    hasClauses = true;
                } else if (value instanceof DateFilter df) {
                    bool.must(buildDateQuery(f, resolveField(fieldName), df));
                    hasClauses = true;
                } else if (value instanceof BooleanFilter bf) {
                    if (bf.eq() != null) {
                        bool.must(f.match().field(resolveField(fieldName)).matching(bf.eq()));
                        hasClauses = true;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error building query", e);
        }

        return hasClauses ? bool : f.matchAll();
    }

    private String resolveField(String inputName) {
        return inputName.replace("_", ".");
    }

    private PredicateFinalStep buildStringQuery(SearchPredicateFactory f, String field, StringFilter filter) {
        var bool = f.bool();
        boolean hasClauses = false;
        
        // For exact match, prefer keyword field if available.
        // Heuristic: If field is not "id" and doesn't end in "_keyword", append "_keyword"
        String exactField = (field.equals("id") || field.endsWith("_keyword")) ? field : field + "_keyword";

        if (filter.eq() != null) {
            bool.must(f.match().field(exactField).matching(filter.eq()));
            hasClauses = true;
        }
        if (filter.contains() != null) {
            bool.must(f.wildcard().field(field).matching("*" + filter.contains() + "*"));
            hasClauses = true;
        }
        if (filter.startsWith() != null) {
            bool.must(f.wildcard().field(field).matching(filter.startsWith() + "*"));
            hasClauses = true;
        }
        if (filter.endsWith() != null) {
            bool.must(f.wildcard().field(field).matching("*" + filter.endsWith()));
            hasClauses = true;
        }
        if (filter.in() != null && !filter.in().isEmpty()) {
            bool.must(f.terms().field(exactField).matchingAny(filter.in()));
            hasClauses = true;
        }
        
        return hasClauses ? bool : f.matchAll();
    }

    private PredicateFinalStep buildIntQuery(SearchPredicateFactory f, String field, IntFilter filter) {
        var bool = f.bool();
        boolean hasClauses = false;
        
        if (filter.eq() != null) {
            bool.must(f.match().field(field).matching(filter.eq()));
            hasClauses = true;
        }
        if (filter.gt() != null) {
            bool.must(f.range().field(field).greaterThan(filter.gt()));
            hasClauses = true;
        }
        if (filter.gte() != null) {
            bool.must(f.range().field(field).atLeast(filter.gte()));
            hasClauses = true;
        }
        if (filter.lt() != null) {
            bool.must(f.range().field(field).lessThan(filter.lt()));
            hasClauses = true;
        }
        if (filter.lte() != null) {
            bool.must(f.range().field(field).atMost(filter.lte()));
            hasClauses = true;
        }
        return hasClauses ? bool : f.matchAll();
    }

    private PredicateFinalStep buildFloatQuery(SearchPredicateFactory f, String field, FloatFilter filter) {
        var bool = f.bool();
        boolean hasClauses = false;
        
        if (filter.eq() != null) {
            bool.must(f.match().field(field).matching(filter.eq()));
            hasClauses = true;
        }
        if (filter.gt() != null) {
            bool.must(f.range().field(field).greaterThan(filter.gt()));
            hasClauses = true;
        }
        if (filter.lt() != null) {
            bool.must(f.range().field(field).lessThan(filter.lt()));
            hasClauses = true;
        }
        return hasClauses ? bool : f.matchAll();
    }

    private PredicateFinalStep buildDateQuery(SearchPredicateFactory f, String field, DateFilter filter) {
        var bool = f.bool();
        boolean hasClauses = false;
        
        if (filter.eq() != null) {
            bool.must(f.match().field(field).matching(java.time.LocalDate.parse(filter.eq())));
            hasClauses = true;
        }
        if (filter.gt() != null) {
            bool.must(f.range().field(field).greaterThan(java.time.LocalDate.parse(filter.gt())));
            hasClauses = true;
        }
        if (filter.gte() != null) {
            bool.must(f.range().field(field).atLeast(java.time.LocalDate.parse(filter.gte())));
            hasClauses = true;
        }
        if (filter.lt() != null) {
            bool.must(f.range().field(field).lessThan(java.time.LocalDate.parse(filter.lt())));
            hasClauses = true;
        }
        if (filter.lte() != null) {
            bool.must(f.range().field(field).atMost(java.time.LocalDate.parse(filter.lte())));
            hasClauses = true;
        }
        return hasClauses ? bool : f.matchAll();
    }
}
