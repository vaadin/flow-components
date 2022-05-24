package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
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
