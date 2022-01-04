/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.data.bean;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Helper class used for generating stable random data for demo purposes.
 *
 * @author Vaadin Ltd.
 *
 */
public class PeopleGenerator extends BeanGenerator {

    private static final AtomicInteger treeIds = new AtomicInteger(0);

    public List<Person> generatePeople(int amount) {
        return IntStream.range(0, amount)
                .mapToObj(index -> createPerson(index + 1))
                .collect(Collectors.toList());
    }

    public List<PersonWithLevel> generatePeopleWithLevels(int amount,
            int level) {
        return IntStream.range(0, amount)
                .mapToObj(index -> createPersonWithLevel(index + 1, level))
                .collect(Collectors.toList());
    }

    private PersonWithLevel createPersonWithLevel(int index, int level) {
        PersonWithLevel person = createPerson(PersonWithLevel::new, index,
                treeIds.getAndIncrement());
        person.setLevel(level);
        return person;
    }

    public Person createPerson(int index) {
        return createPerson(Person::new, index, index);
    }

    private <T extends Person> T createPerson(Supplier<T> constructor,
            int index, int id) {
        boolean isSubscriber = getRandom("subscriber").nextBoolean();

        return createPerson(constructor, "Person " + index, id,
                13 + getRandom("age").nextInt(50), isSubscriber,
                isSubscriber ? generateEmail() : "",
                "Street " + generateChar(getRandom("street"), false),
                1 + getRandom("street").nextInt(50),
                10000 + getRandom("postalCode").nextInt(8999));
    }

    private <T extends Person> T createPerson(Supplier<T> constructor,
            String name, int id, int age, boolean subscriber, String email,
            String street, int addressNumber, int postalCode) {
        T person = constructor.get();
        person.setId(id);
        person.setFirstName(name);
        person.setAge(age);
        person.setSubscriber(subscriber);
        person.setEmail(email);

        Address address = new Address();
        address.setStreet(street);
        address.setNumber(addressNumber);
        address.setPostalCode(postalCode);

        person.setAddress(address);

        return person;
    }

    private String generateEmail() {
        StringBuilder builder = new StringBuilder("mail");
        builder.append(generateChar(getRandom("email"), true));
        builder.append(generateChar(getRandom("email"), true));
        builder.append("@example.com");
        return builder.toString();
    }

    private char generateChar(Random random, boolean lowerCase) {
        return ((char) ((lowerCase ? 'a' : 'A') + random.nextInt(26)));
    }

}
