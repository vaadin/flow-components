package com.vaadin.addon.spreadsheet.charts.converter.chartdata;

import com.vaadin.addon.spreadsheet.charts.converter.confwriter.AbstractSeriesDataWriter;
import com.vaadin.addon.spreadsheet.charts.converter.confwriter.PieSeriesDataWriter;

public class PieSeriesData extends AbstractSeriesData {

    public boolean isExploded;
    public boolean isDonut;
    public short donutHoleSizePercent;

    @Override
    public AbstractSeriesDataWriter getSeriesDataWriter() {
        return new PieSeriesDataWriter(this);
    }
}
