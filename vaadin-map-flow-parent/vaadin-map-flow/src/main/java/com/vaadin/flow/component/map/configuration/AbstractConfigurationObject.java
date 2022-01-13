package com.vaadin.flow.component.map.configuration;

import java.io.Serializable;

public abstract class AbstractConfigurationObject implements Serializable {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public abstract String getType();
}
