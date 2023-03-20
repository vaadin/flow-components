
package com.vaadin.flow.component.grid;

import java.io.Serializable;
import java.util.List;

public class Person implements Serializable {
    private String name;
    private final int born;

    private Person friend;

    private List<String> items;

    private int[] grades;

    public Person(String name, int born) {
        this.name = name;
        this.born = born;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBorn() {
        return born;
    }

    public Person getFriend() {
        return friend;
    }

    public void setFriend(Person friend) {
        this.friend = friend;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public int[] getGrades() {
        return grades;
    }

    public void setGrades(int[] grades) {
        this.grades = grades;
    }

    @Override
    public String toString() {
        return name + "(" + born + ")";
    }
}
