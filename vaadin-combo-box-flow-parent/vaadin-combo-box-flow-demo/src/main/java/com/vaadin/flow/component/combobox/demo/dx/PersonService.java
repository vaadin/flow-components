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

    /**
     * Provides a persons for a given page, and with a given age.
     */
    Stream<Person> getPersons(int pageSize, int page, Integer ageFilter) {
        return personData.getPersons().stream()
                .filter(person -> ageFilter == null
                        || person.getAge() == ageFilter)
                .skip(page * pageSize).limit(pageSize);
    }

    /**
     * Provides a number of persons with a given age.
     */
    int getPersonsCount(Integer ageFilter) {
        return (int) personData.getPersons().stream()
                .filter(person -> ageFilter == null
                        || person.getAge() == ageFilter)
                .count();
    }
}
