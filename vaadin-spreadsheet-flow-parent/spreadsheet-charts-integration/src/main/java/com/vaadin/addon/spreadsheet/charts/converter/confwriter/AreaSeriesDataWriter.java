package com.vaadin.addon.spreadsheet.charts.converter.confwriter;

import com.vaadin.addon.charts.model.AbstractPlotOptions;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.Marker;
import com.vaadin.addon.charts.model.PlotOptionsArea;
import com.vaadin.addon.charts.model.Stacking;
import com.vaadin.addon.spreadsheet.charts.converter.Utils;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.AreaSeriesData;

public class AreaSeriesDataWriter extends AbstractSeriesDataWriter {

    public AreaSeriesDataWriter(AreaSeriesData series) {
        super(series);
    }

    @Override
    protected AreaSeriesData getSeriesData() {
        return (AreaSeriesData) super.getSeriesData();
    }

    @Override
    protected PlotOptionsArea getPlotOptions() {
        return (PlotOptionsArea) super.getPlotOptions();
    }

    @Override
    protected AbstractPlotOptions createPlotOptions() {
        return new PlotOptionsArea();
    }

    @Override
    protected void configureDataSeries(DataSeries dataSeriesForWriting) {
        super.configureDataSeries(dataSeriesForWriting);
        
        String stacking = getSeriesData().stacking.toString();

        getPlotOptions().setStacking(
                Utils.getEnumValueOrDefault(Stacking.class,
                        stacking, Stacking.NONE));

        getPlotOptions().setMarker(new Marker(false));
    }
}
