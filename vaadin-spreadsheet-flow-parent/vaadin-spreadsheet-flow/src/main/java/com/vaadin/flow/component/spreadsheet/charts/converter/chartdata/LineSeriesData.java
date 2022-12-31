/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter.chartdata;

import com.vaadin.flow.component.spreadsheet.charts.converter.confwriter.AbstractSeriesDataWriter;
import com.vaadin.flow.component.spreadsheet.charts.converter.confwriter.LineSeriesDataWriter;

public class LineSeriesData extends AbstractSeriesData {

    /*
     * This file should not have dependencies on
     * "com.vaadin.flow.component.charts" and I found it too clumsy creating
     * (i.e. copy-pasting) the same enums here.
     */

    /**
     * Currently this string value corresponds to
     * com.vaadin.flow.component.charts.model.DashStyle.
     */
    public String dashStyle = "";

    public Integer lineWidth;

    /**
     * Currently this string value corresponds to
     * com.vaadin.flow.component.charts.model.MarkerSymbolEnum.
     */
    public String markerSymbol = "";

    @Override
    public AbstractSeriesDataWriter getSeriesDataWriter() {
        return new LineSeriesDataWriter(this);
    }
}
