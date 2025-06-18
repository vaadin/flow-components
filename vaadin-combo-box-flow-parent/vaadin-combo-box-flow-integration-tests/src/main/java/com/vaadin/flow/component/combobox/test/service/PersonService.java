/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.component.combobox.test.data.PersonData;
import com.vaadin.flow.component.combobox.test.entity.Person;

public class PersonService {
    private final PersonData personData;

    public PersonService() {
        this.personData = new PersonData();
    }

    public PersonService(int personCount) {
        this.personData = new PersonData(personCount);
    }

    public Stream<Person> fetch(String filter, int offset, int limit) {
        return personData.getPersons().stream()
                .filter(person -> filter == null || person.toString()
                        .toLowerCase().startsWith(filter.toLowerCase()))
                .skip(offset).limit(limit);
    }

    public int count(String filter) {
        return (int) personData.getPersons().stream()
                .filter(person -> filter == null || person.toString()
                        .toLowerCase().startsWith(filter.toLowerCase()))
                .count();
    }

    public Stream<Person> fetchPage(String filter, int page, int pageSize) {
        return fetch(filter, page * pageSize, pageSize);
    }

    public int count() {
        return personData.getPersons().size();
    }

    public List<Person> fetchAll() {
        List<Person> persons = personData.getPersons();
        Collections.shuffle(persons);
        return persons;
    }

    public Stream<Person> fetchOlderThan(Integer ageFilter, int offset,
            int limit) {
        return personData.getPersons().stream().filter(
                person -> ageFilter == null || person.getAge() > ageFilter)
                .skip(offset).limit(limit);
    }
}
