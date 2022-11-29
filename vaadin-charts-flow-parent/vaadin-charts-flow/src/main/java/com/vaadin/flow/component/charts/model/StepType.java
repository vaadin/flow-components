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
 * Defines different step line types. Configurable in {@link PlotOptionsLine}.
 */
public enum StepType implements ChartEnum {
    RIGHT("right"), CENTER("center"), LEFT("left"), NONE("");

    private String highchartName;

    private StepType(String n) {
        this.highchartName = n;
    }

    public String toString() {
        return highchartName;
    }

}
