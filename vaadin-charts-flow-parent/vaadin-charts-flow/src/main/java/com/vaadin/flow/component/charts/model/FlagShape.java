/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * The name of a symbol to use for the border in {@link ChartType#FLAGS} series.
 */
public enum FlagShape implements ChartEnum {

    CALLOUT("callout"), FLAG("flag"), CIRCLEPIN("circlepin"), SQUAREPIN(
            "squarepin");

    FlagShape(String type) {
        this.type = type;
    }

    private String type;

    @Override
    public String toString() {
        return type;
    }
}
