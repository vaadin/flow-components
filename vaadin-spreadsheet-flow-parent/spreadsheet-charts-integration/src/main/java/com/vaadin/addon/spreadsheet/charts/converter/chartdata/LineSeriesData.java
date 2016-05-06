package com.vaadin.addon.spreadsheet.charts.converter.chartdata;

import com.vaadin.addon.spreadsheet.charts.converter.confwriter.AbstractSeriesDataWriter;
import com.vaadin.addon.spreadsheet.charts.converter.confwriter.LineSeriesDataWriter;

public class LineSeriesData extends AbstractSeriesData {

    /*
     * This file should not have dependencies on "com.vaadin.addon.charts" and I
     * found it too clumsy creating (i.e. copy-pasting) the same enums here.
     */

    /**
     * Currently this string value corresponds to
     * com.vaadin.addon.charts.model.DashStyle.
     */
    public String dashStyle = "";

    /**
     * Currently this string value corresponds to
     * com.vaadin.addon.charts.model.MarkerSymbolEnum.
     */
    public String markerSymbol = "";

    @Override
    public AbstractSeriesDataWriter getSeriesDataWriter() {
        return new LineSeriesDataWriter(this);
    }
}
