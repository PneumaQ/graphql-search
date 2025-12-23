package com.example.graphql.person.repository.search;

import com.example.graphql.person.model.Person;
import com.example.graphql.product.filter.SearchCondition;
import com.example.graphql.platform.search.UniversalQueryBuilder;
import com.example.graphql.platform.metadata.EntityCfg;
import com.example.graphql.platform.metadata.EntityCfgRepository;
import com.example.graphql.platform.logging.QueryContext;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import java.util.List;

@Repository
public class PersonSearchRepository {

    private final EntityManager entityManager;
    private final UniversalQueryBuilder queryBuilder;
    private final EntityCfgRepository entityCfgRepository;

    public PersonSearchRepository(EntityManager entityManager, 
                                  UniversalQueryBuilder queryBuilder,
                                  EntityCfgRepository entityCfgRepository) {
        this.entityManager = entityManager;
        this.queryBuilder = queryBuilder;
        this.entityCfgRepository = entityCfgRepository;
    }

    public PersonSearchInternalResponse search(String text, List<SearchCondition> filter, int page, int size) {
        SearchSession searchSession = Search.session(entityManager);
        EntityCfg rootEntity = entityCfgRepository.findByName("Person").orElseThrow();

        var query = searchSession.search(Person.class)
            .where(f -> f.bool(b -> {
                b.must(queryBuilder.build(f, filter, rootEntity));
                if (text != null && !text.isBlank()) {
                    b.must(f.simpleQueryString().field("name").field("email").matching(text));
                }
            }));

        QueryContext.set("Hibernate Search - Person Loading");
        var result = query.fetch(page * size, size);
        
        return new PersonSearchInternalResponse(
            result.hits(),
            result.total().hitCount(),
            (int) Math.ceil((double) result.total().hitCount() / size)
        );
    }

    public record PersonSearchInternalResponse(List<Person> results, long totalElements, int totalPages) {}
}
