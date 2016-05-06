package com.vaadin.addon.spreadsheet.charts.converter.chartdata;

import com.vaadin.addon.spreadsheet.charts.converter.confwriter.ScatterSeriesDataWriter;

public class ScatterSeriesData extends LineSeriesData {

    @Override
    public ScatterSeriesDataWriter getSeriesDataWriter() {
        return new ScatterSeriesDataWriter(this);
    }
}
