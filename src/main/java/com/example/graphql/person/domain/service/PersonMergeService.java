package com.example.graphql.person.domain.service;

import com.example.graphql.person.domain.model.Address;
import com.example.graphql.person.domain.model.Person;
import com.example.graphql.person.graphql.input.AddressInput;
import com.example.graphql.person.graphql.input.CreatePersonInput;
import com.example.graphql.person.graphql.input.UpdatePersonInput;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PersonMergeService {

    public Person merge(CreatePersonInput input, Person person) {
        person.setName(input.name());
        person.setEmail(input.email());
        if (input.age() != null) person.setAge(input.age());
        
        if (input.addresses() != null) {
            for (AddressInput addrInput : input.addresses()) {
                Address addr = new Address();
                mapAddress(addrInput, addr);
                addr.setPerson(person);
                person.getAddresses().add(addr);
            }
        }
        return person;
    }

    public Person merge(UpdatePersonInput input, Person person) {
        if (input.name() != null) person.setName(input.name());
        if (input.email() != null) person.setEmail(input.email());
        if (input.age() != null) person.setAge(input.age());

        if (input.addresses() != null) {
            mergeAddresses(input.addresses(), person);
        }
        return person;
    }

    private void mergeAddresses(List<AddressInput> inputs, Person person) {
        Map<Long, Address> existingMap = person.getAddresses().stream()
            .filter(a -> a.getId() != null)
            .collect(Collectors.toMap(Address::getId, Function.identity()));

        List<Address> newList = new ArrayList<>();

        for (AddressInput input : inputs) {
            if (input.id() != null && existingMap.containsKey(Long.valueOf(input.id()))) {
                Address existing = existingMap.get(Long.valueOf(input.id()));
                mapAddress(input, existing);
                newList.add(existing);
            } else {
                Address newAddr = new Address();
                mapAddress(input, newAddr);
                newAddr.setPerson(person);
                newList.add(newAddr);
            }
        }
        
        person.getAddresses().clear();
        person.getAddresses().addAll(newList);
    }

    private void mapAddress(AddressInput input, Address entity) {
        if (input.street() != null) entity.setStreet(input.street());
        if (input.city() != null) entity.setCity(input.city());
        if (input.country() != null) entity.setCountry(input.country());
    }
}
