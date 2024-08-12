/*
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
