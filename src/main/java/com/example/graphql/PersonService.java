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

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final PersonSearchRepository personSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public PersonService(PersonRepository personRepository, PersonSearchRepository personSearchRepository, ElasticsearchOperations elasticsearchOperations) {
        this.personRepository = personRepository;
        this.personSearchRepository = personSearchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

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
    
    public List<Person> searchByName(String name) {
        return personSearchRepository.findByNameContaining(name);
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
    public Person addAddress(Long personId, String street, String city, String state, String zip) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new RuntimeException("Person not found"));
        
        Address address = new Address();
        address.setStreet(street);
        address.setCity(city);
        address.setState(state);
        address.setZip(zip);
        address.setPerson(person);
        
        person.getAddresses().add(address);
        Person saved = personRepository.save(person);
        personSearchRepository.save(saved);
        saved.getAddresses().size(); // Force initialization of the collection
        return saved;
    }
}