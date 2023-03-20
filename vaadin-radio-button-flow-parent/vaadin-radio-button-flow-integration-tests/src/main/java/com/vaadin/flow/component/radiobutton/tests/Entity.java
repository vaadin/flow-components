
package com.vaadin.flow.component.radiobutton.tests;

import javax.validation.constraints.NotNull;

public class Entity {

    @NotNull
    private String gender;

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGender() {
        return gender;
    }

    @Override
    public String toString() {
        return "Entity, gender: " + gender;
    }

}
