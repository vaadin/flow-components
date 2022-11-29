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
 * Irregular time unit used to define point interval unit.
 */
public enum IntervalUnit implements ChartEnum {

    DAY("day"), MONTH("month"), YEAR("year");

    private IntervalUnit(String unit) {
        this.unit = unit;
    }

    private String unit;

    @Override
    public String toString() {
        return unit;
    }
}
