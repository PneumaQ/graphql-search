package com.example.graphql;

import com.example.graphql.repository.jpa.PersonRepository;
import com.example.graphql.repository.search.PersonSearchRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import co.elastic.clients.elasticsearch._types.aggregations.NestedAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;

import com.example.graphql.filter.ElasticsearchQueryBuilder;
import com.example.graphql.filter.PersonFilterInput;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final PersonSearchRepository personSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchQueryBuilder queryBuilder;

    public PersonService(PersonRepository personRepository, PersonSearchRepository personSearchRepository, ElasticsearchOperations elasticsearchOperations, ElasticsearchQueryBuilder queryBuilder) {
        this.personRepository = personRepository;
        this.personSearchRepository = personSearchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.queryBuilder = queryBuilder;
    }

    public PersonSearchResponse searchWithFacets(String text, PersonFilterInput filter, org.springframework.data.domain.Pageable pageable) {
        co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder boolBuilder = new co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder();

        // 1. Text Search Logic
        if (text != null && !text.trim().isEmpty()) {
            boolBuilder.should(s -> s.match(m -> m.field("allSearchContent").query(text)));
            boolBuilder.should(s -> s.nested(n -> n
                    .path("addresses")
                    .query(nq -> nq.match(m -> m.field("addresses.allSearchContent").query(text)))
            ));
            boolBuilder.minimumShouldMatch("1");
        } else {
            boolBuilder.must(m -> m.matchAll(ma -> ma));
        }

        // 2. Filter Logic (Using Generic Builder)
        if (filter != null) {
            co.elastic.clients.elasticsearch._types.query_dsl.Query filterQuery = queryBuilder.build(filter);
            if (filterQuery != null) {
                boolBuilder.filter(filterQuery);
            }
        }

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(boolBuilder.build()))
                .withPageable(pageable)
                .withAggregation("active_counts", Aggregation.of(a -> a.terms(t -> t.field("isActive"))))
                .withAggregation("address_aggs", Aggregation.of(a -> a.nested(n -> n.path("addresses"))
                        .aggregations("country_counts", sub -> sub.terms(t -> t.field("addresses.country.keyword").size(10)))
                        .aggregations("state_counts", sub -> sub.terms(t -> t.field("addresses.state.keyword").size(10)))
                ))
                .build();

        SearchHits<Person> searchHits = elasticsearchOperations.search(query, Person.class);
        List<Person> people = searchHits.stream().map(org.springframework.data.elasticsearch.core.SearchHit::getContent).collect(Collectors.toList());
        long totalElements = searchHits.getTotalHits();
        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());
        
        Map<String, Long> activeCounts = new HashMap<>();
        Map<String, Long> countryCounts = new HashMap<>();
        Map<String, Long> stateCounts = new HashMap<>();

        if (searchHits.getAggregations() instanceof ElasticsearchAggregations aggregations) {
            parseTerms(aggregations, "active_counts", activeCounts);
            
            ElasticsearchAggregation nestedAgg = (ElasticsearchAggregation) aggregations.aggregationsAsMap().get("address_aggs");
            if (nestedAgg != null) {
                NestedAggregate nestedBucket = nestedAgg.aggregation().getAggregate().nested();
                Map<String, Aggregate> subAggs = nestedBucket.aggregations();
                parseTermsFromMap(subAggs, "country_counts", countryCounts);
                parseTermsFromMap(subAggs, "state_counts", stateCounts);
            }
        }

        return new PersonSearchResponse(people, activeCounts, countryCounts, stateCounts, totalElements, totalPages);
    }

    private void parseTerms(ElasticsearchAggregations aggregations, String key, Map<String, Long> target) {
        ElasticsearchAggregation agg = (ElasticsearchAggregation) aggregations.aggregationsAsMap().get(key);
        if (agg != null) {
            populateMap(agg.aggregation().getAggregate(), target);
        }
    }

    private void parseTermsFromMap(Map<String, Aggregate> aggs, String key, Map<String, Long> target) {
        Aggregate agg = aggs.get(key);
        if (agg != null) {
            populateMap(agg, target);
        }
    }

    private void populateMap(Aggregate aggregate, Map<String, Long> target) {
        if (aggregate.isSterms()) {
            for (StringTermsBucket bucket : aggregate.sterms().buckets().array()) {
                target.put(bucket.key().stringValue(), bucket.docCount());
            }
        } else if (aggregate.isLterms()) {
             for (LongTermsBucket bucket : aggregate.lterms().buckets().array()) {
                target.put(bucket.keyAsString(), bucket.docCount());
            }
        }
    }

    public record PersonSearchResponse(List<Person> items, Map<String, Long> activeCounts, Map<String, Long> countryCounts, Map<String, Long> stateCounts, long totalElements, int totalPages) {}

    public Map<String, Long> getNameFacets() {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.matchAll(m -> m))
                .withAggregation("name_counts", Aggregation.of(a -> a
                        .terms(t -> t.field("name.keyword").size(10))
                ))
                .withMaxResults(0) // We only care about aggregations
                .build();

        SearchHits<Person> searchHits = elasticsearchOperations.search(query, Person.class);
        
        Map<String, Long> result = new HashMap<>();
        
        if (searchHits.getAggregations() != null) {
            if (searchHits.getAggregations() instanceof ElasticsearchAggregations aggregations) {
                ElasticsearchAggregation aggWrapper = (ElasticsearchAggregation) aggregations.aggregationsAsMap().get("name_counts");
                if (aggWrapper != null) {
                    Aggregate aggregate = aggWrapper.aggregation().getAggregate();
                    
                    if (aggregate.isSterms()) {
                        StringTermsAggregate termsAggregate = aggregate.sterms();
                        for (StringTermsBucket bucket : termsAggregate.buckets().array()) {
                            result.put(bucket.key().stringValue(), bucket.docCount());
                        }
                    }
                }
            }
        }
        
        return result;
    }

    public List<Person> findAll() {
        return personRepository.findAll();
    }

    public Optional<Person> findById(Long id) {
        return personRepository.findById(id);
    }
    
    public List<Person> searchByName(String text) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .should(s -> s.match(m -> m.field("allSearchContent").query(text)))
                        .should(s -> s.nested(n -> n
                                .path("addresses")
                                .query(nq -> nq.match(m -> m.field("addresses.allSearchContent").query(text)))
                        ))
                ))
                .build();
        
        SearchHits<Person> searchHits = elasticsearchOperations.search(query, Person.class);
        return searchHits.stream().map(org.springframework.data.elasticsearch.core.SearchHit::getContent).collect(Collectors.toList());
    }

    public List<Person> findByCity(String city) {
        return personRepository.findByAddresses_City(city);
    }

    public Person save(Person person) {
        Person saved = personRepository.save(person);
        personSearchRepository.save(saved);
        return saved;
    }

    @org.springframework.transaction.annotation.Transactional
    public Person addAddress(Long personId, String street, String city, String state, String zip, String country, Boolean isPrimary) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new RuntimeException("Person not found"));
        
        Address address = new Address();
        address.setStreet(street);
        address.setCity(city);
        address.setState(state);
        address.setZip(zip);
        address.setCountry(country);
        address.setIsPrimary(isPrimary);
        address.setPerson(person);
        
        person.getAddresses().add(address);
        Person saved = personRepository.save(person);
        personSearchRepository.save(saved);
        saved.getAddresses().size(); // Force initialization of the collection
        return saved;
    }
}