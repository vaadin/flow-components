package com.vaadin.addon.spreadsheet.charts.converter.chartdata;

import com.vaadin.addon.spreadsheet.charts.converter.confwriter.SplineSeriesDataWriter;

public class SplineSeriesData extends LineSeriesData {

    @Override
    public SplineSeriesDataWriter getSeriesDataWriter() {
        return new SplineSeriesDataWriter(this);
    }
}
