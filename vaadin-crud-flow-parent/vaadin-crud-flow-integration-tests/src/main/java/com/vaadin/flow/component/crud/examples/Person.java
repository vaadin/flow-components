package com.vaadin.flow.component.crud.examples;

import com.vaadin.flow.component.crud.annotation.Order;

public class Person {

    @Order
    private Integer id;

    @Order(1)
    private String name;

    private static String staticProp = "This should not show";

    public Person() {
    }

    public Person(Integer id, String name) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public static String getStaticProp() {
        return staticProp;
    }

    public static void setStaticProp(String staticProp) {
        Person.staticProp = staticProp;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
