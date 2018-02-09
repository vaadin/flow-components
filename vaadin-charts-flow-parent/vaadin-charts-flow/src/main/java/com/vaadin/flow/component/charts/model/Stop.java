package com.vaadin.flow.component.charts.model;

public class Stop extends AbstractConfigurationObject {
    private float position;

    public Stop(float position) {
        this.position = position;
    }

    public float getPosition() {
        return position;
    }

    public void setPosition(float position) {
        this.position = position;
    }

}
