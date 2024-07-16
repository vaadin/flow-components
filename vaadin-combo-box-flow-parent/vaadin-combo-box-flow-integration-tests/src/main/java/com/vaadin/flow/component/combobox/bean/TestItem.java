/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.bean;

import java.util.Objects;

public class TestItem {
    private int id;
    private String name;
    private String comments;

    public TestItem(int id) {
        this.id = id;
    }

    public TestItem(int id, String name, String comments) {
        this.id = id;
        this.name = name;
        this.comments = comments;
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

    @Override
    public String toString() {
        return name + ", " + comments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TestItem testItem = (TestItem) o;
        return Objects.equals(name, testItem.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
