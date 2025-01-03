/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.PlotOptionsTimeline;

/**
 * The ChartMode configures how the data is presented in the chart.
 */
public enum ChartMode {

    /**
     * Normal chart mode, for any series type (see {@link ChartType}).
     */
    NORMAL("normal"),

    /**
     * Chart timeline mode: the chart allows selecting different time ranges for
     * which to display the chart data, as well as navigating between such
     * ranges. The following chart types do not support timeline mode:
     * <ul>
     * <li>ChartType.PIE</li>
     * <li>ChartType.GAUGE</li>
     * <li>ChartType.SOLIDGAUGE</li>
     * <li>ChartType.PYRAMID</li>
     * <li>ChartType.FUNNEL</li>
     * <li>ChartType.ORGANIZATION</li>
     * </ul>
     * Enabling timeline mode in these unsupported chart types results in an
     * <code>IllegalArgumentException</code>
     * <p>
     * Note: for Timeline chart type see {@link ChartType#TIMELINE} and
     * {@link PlotOptionsTimeline}.
     */
    TIMELINE("timeline"),

    /**
     * Gantt chart mode: the chart series data will be presented as a Gantt
     * chart. Only the following chart series types are supported while in Gantt
     * chart mode:
     * <ul>
     * <li>ChartType.GANTT</li>
     * <li>ChartType.XRANGE</li>
     * </ul>
     * Enabling Gantt chart mode with unsupported chart types results in an
     * <code>IllegalArgumentException</code>
     * <p>
     * Note: for Gantt chart type see
     * {@link com.vaadin.flow.component.charts.model.ChartType#GANTT} and
     * {@link com.vaadin.flow.component.charts.model.PlotOptionsGantt}.
     */
    GANTT("gantt");

    private final String mode;

    ChartMode(String mode) {
        this.mode = mode;
    }

    public String getModeName() {
        return mode;
    }

    public static ChartMode getForName(String name) {
        for (ChartMode mode : values()) {
            if (mode.getModeName().equals(name)) {
                return mode;
            }
        }
        return ChartMode.NORMAL;
    }
}
