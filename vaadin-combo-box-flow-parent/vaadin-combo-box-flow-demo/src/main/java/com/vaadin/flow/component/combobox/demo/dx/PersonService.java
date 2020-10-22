package com.vaadin.flow.component.combobox.demo.dx;

import java.util.stream.Stream;

import com.vaadin.flow.component.combobox.demo.data.PersonData;
import com.vaadin.flow.component.combobox.demo.entity.Person;

class PersonService {
    private final PersonData personData;

    PersonService() {
        this.personData = new PersonData(1000);
    }

    PersonService(int personCount) {
        this.personData = new PersonData(personCount);
    }

    Stream<Person> getPersons(int pageSize, int page, Person filter) {
        return personData.getPersons().stream()
                .filter(person -> filter == null || person.toString()
                        .toLowerCase().contains(filter.toString()))
                .skip(page * pageSize).limit(pageSize);
    }

    int getPersonsCount(Person filter) {
        return (int) personData.getPersons().stream()
                .filter(person -> filter == null || person.toString()
                        .toLowerCase().contains(filter.toString())).count();
    }
}
