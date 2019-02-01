package com.vaadin.flow.component.gridpro.vaadincom;

import java.io.Serializable;

public class Person implements Serializable {
    private int id;
    private int age;
    private String name;
    private boolean isSubscriber;
    private String email;
    private Department department;

    public Person() {
        super();
    }

    public Person(String name, boolean isSubscriber, String email, int age, Department department) {
        super();
        this.name = name;
        this.isSubscriber = isSubscriber;
        this.email = email;
        this.age = age;
        this.department = department;
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isSubscriber() {
        return isSubscriber;
    }

    public void setSubscriber(boolean isSubscriber) {
        this.isSubscriber = isSubscriber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Person)) {
            return false;
        }
        Person other = (Person) obj;
        return id == other.id;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Person clone() {
        try {
            return (Person) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(
                    "The Person object could not be cloned.", e);
        }
    }
}
