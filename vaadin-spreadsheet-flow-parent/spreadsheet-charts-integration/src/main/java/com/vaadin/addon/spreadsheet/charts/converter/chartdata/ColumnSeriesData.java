package com.vaadin.addon.spreadsheet.charts.converter.chartdata;

import com.vaadin.addon.spreadsheet.charts.converter.confwriter.AbstractSeriesDataWriter;
import com.vaadin.addon.spreadsheet.charts.converter.confwriter.ColumnSeriesDataWriter;

public class ColumnSeriesData extends AbstractSeriesData {

    public Stacking stacking = Stacking.NONE;

    @Override
    public AbstractSeriesDataWriter getSeriesDataWriter() {
        return new ColumnSeriesDataWriter(this);
    }
}
