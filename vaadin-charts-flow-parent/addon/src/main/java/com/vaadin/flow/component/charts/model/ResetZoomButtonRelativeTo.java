package com.vaadin.flow.component.charts.model;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2014 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
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
