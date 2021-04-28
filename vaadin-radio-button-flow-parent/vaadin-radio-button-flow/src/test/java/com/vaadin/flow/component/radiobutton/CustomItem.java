package com.vaadin.flow.component.radiobutton;

import java.util.Objects;

public class CustomItem {
    private Long id;
    private String name;

    public CustomItem(Long id) {
        this(id, null);
    }

    public CustomItem(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CustomItem))
            return false;
        CustomItem that = (CustomItem) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}