package com.example.graphql.person.infrastructure.persistence;

import com.example.graphql.person.domain.model.Person;
import com.example.graphql.person.domain.repository.PersonRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class JpaPersonAdapter implements PersonRepository {

    private final SpringDataPersonRepository springDataRepository;

    public JpaPersonAdapter(SpringDataPersonRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Person save(Person person) {
        return springDataRepository.save(person);
    }

    @Override
    public Optional<Person> findById(Long id) {
        return springDataRepository.findById(id);
    }

    @Override
    public List<Person> findAll() {
        return springDataRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        springDataRepository.deleteAll();
    }
}
