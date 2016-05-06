package com.vaadin.addon.spreadsheet.charts.converter.chartdata;

import com.vaadin.addon.spreadsheet.charts.converter.confwriter.AbstractSeriesDataWriter;
import com.vaadin.addon.spreadsheet.charts.converter.confwriter.AreaSeriesDataWriter;

public class AreaSeriesData extends AbstractSeriesData {

    public Stacking stacking = Stacking.NONE;

    @Override
    public AbstractSeriesDataWriter getSeriesDataWriter() {
        return new AreaSeriesDataWriter(this);
    }
}
