package com.vaadin.addon.spreadsheet.charts.converter.confwriter;

import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.PlotOptionsSpline;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.SplineSeriesData;

public class SplineSeriesDataWriter extends AbstractSeriesDataWriter {

    public SplineSeriesDataWriter(SplineSeriesData series) {
        super(series);
    }

    @Override
    protected SplineSeriesData getSeriesData() {
        return (SplineSeriesData) super.getSeriesData();
    }

    @Override
    protected PlotOptionsSpline createPlotOptions() {
        return new PlotOptionsSpline();
    }

    @Override
    protected PlotOptionsSpline getPlotOptions() {
        return (PlotOptionsSpline) super.getPlotOptions();
    }

    @Override
    protected void configureDataSeries(DataSeries dataSeriesForWriting) {
        super.configureDataSeries(dataSeriesForWriting);

        getPlotOptions().setDashStyle(
                LineSeriesWriterUtils.getDashStyle(getSeriesData().dashStyle));
        getPlotOptions().setMarker(
                LineSeriesWriterUtils.getMarker(getSeriesData().markerSymbol));
    }
}
