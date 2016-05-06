package com.vaadin.addon.spreadsheet.charts.converter.chartdata;

import com.vaadin.addon.spreadsheet.charts.converter.confwriter.LineSeriesDataWriter;
import com.vaadin.addon.spreadsheet.charts.converter.confwriter.RadarSeriesWriter;

public class RadarSeriesData extends LineSeriesData {

    @Override
    public LineSeriesDataWriter getSeriesDataWriter() {
        return new RadarSeriesWriter(this);
    }

}
