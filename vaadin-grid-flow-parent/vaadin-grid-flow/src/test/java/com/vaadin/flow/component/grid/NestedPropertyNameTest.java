/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import org.junit.Test;

public class NestedPropertyNameTest {

    @Test
    public void nestedProperty_sameNameCanBeAdded() {
        Grid<Person> grid = new Grid<>(Person.class);
        grid.addColumn("street.name");
    }

    private class Person {
        String name;
        Street street;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Street getStreet() {
            return street;
        }

        public void setStreet(Street street) {
            this.street = street;
        }

    }

    private class Street {
        String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
