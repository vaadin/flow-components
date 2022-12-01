package com.vaadin.flow.component.charts.model;

/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * Whether to stack the values of each series on top of each other. Possible
 * values are null to disable, NORMAL to stack by value or PERCENT. Defaults to
 * null.
 */
public enum Stacking implements ChartEnum {

    NONE(""), NORMAL("normal"), PERCENT("percent");

    private Stacking(String type) {
        this.type = type;
    }

    private String type;

    @Override
    public String toString() {
        return type;
    }
}
