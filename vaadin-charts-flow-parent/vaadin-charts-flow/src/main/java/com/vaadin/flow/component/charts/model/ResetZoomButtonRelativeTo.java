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
 * What frame the button should be placed related to. Can be either "plot" or
 * "chart". Defaults to plot.
 */
public enum ResetZoomButtonRelativeTo implements ChartEnum {

    PLOT("plot"), CHART("chart");

    ResetZoomButtonRelativeTo(String frame) {
        this.frame = frame;
    }

    private String frame;

    @Override
    public String toString() {
        return frame;
    }
}
