package com.vaadin.addon.spreadsheet.charts.converter.chartdata;

import com.vaadin.addon.spreadsheet.charts.converter.confwriter.AbstractSeriesDataWriter;
import com.vaadin.addon.spreadsheet.charts.converter.confwriter.BarSeriesDataWriter;

public class BarSeriesData extends ColumnSeriesData {

    @Override
    public AbstractSeriesDataWriter getSeriesDataWriter() {
        return new BarSeriesDataWriter(this);
    }
}
