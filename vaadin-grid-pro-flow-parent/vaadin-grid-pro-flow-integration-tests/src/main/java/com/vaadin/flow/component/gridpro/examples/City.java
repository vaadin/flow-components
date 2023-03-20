/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.gridpro.examples;

public class City {
    private int id;
    private String name;
    private Person person;

    public City() {
        super();
    }

    public City(String name, Person person) {
        super();
        this.name = name;
        this.person = person;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof City)) {
            return false;
        }
        City other = (City) obj;
        return id == other.id;
    }

    @Override
    public String toString() {
        return "City{" + "id=" + id + ", name='" + name + '\'' + ", person='"
                + person.getName() + "'}";
    }

    @Override
    public City clone() {
        try {
            return (City) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("The City object could not be cloned.",
                    e);
        }
    }
}
