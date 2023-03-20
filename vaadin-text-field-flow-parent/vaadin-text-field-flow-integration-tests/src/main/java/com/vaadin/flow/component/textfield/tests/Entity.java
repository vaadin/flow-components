
package com.vaadin.flow.component.textfield.tests;

import javax.validation.constraints.NotEmpty;

public class Entity {

    @NotEmpty
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Entity, name: " + name;
    }

}
