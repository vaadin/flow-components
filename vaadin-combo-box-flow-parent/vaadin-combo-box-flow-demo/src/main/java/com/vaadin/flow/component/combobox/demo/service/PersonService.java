/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.demo.service;

import com.vaadin.flow.component.combobox.demo.data.PersonData;
import com.vaadin.flow.component.combobox.demo.entity.Person;

import java.util.List;
import java.util.stream.Stream;

public class PersonService {
    private PersonData personData = new PersonData();

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

    public int count() {
        return personData.getPersons().size();
    }

    public List<Person> fetchAll() {
        return personData.getPersons();
    }
}
