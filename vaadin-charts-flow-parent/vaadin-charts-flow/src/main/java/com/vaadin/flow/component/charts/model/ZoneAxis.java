package com.vaadin.flow.component.charts.model;

/**
 * Copyright (C) 2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * Defines the Axis on which the zones are applied.
 *
 * Defaults to y.
 */
public enum ZoneAxis implements ChartEnum {

    X("x"), Y("y");

    private final String axis;

    private ZoneAxis(String axis) {
        this.axis = axis;
    }

    @Override
    public String toString() {
        return axis;
    }
}
