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

    public Stream<Person> fetchPage(String filter, int page, int pageSize) {
        return personData.getPersons().stream()
                .filter(person -> filter == null || person.toString()
                        .toLowerCase().startsWith(filter.toLowerCase()))
                .skip(page * pageSize).limit(pageSize);
    }

    public int count() {
        return personData.getPersons().size();
    }

    public List<Person> fetchAll() {
        return personData.getPersons();
    }
}
