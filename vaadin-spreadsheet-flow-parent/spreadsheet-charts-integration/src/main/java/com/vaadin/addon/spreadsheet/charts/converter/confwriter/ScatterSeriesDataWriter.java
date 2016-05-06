package com.vaadin.addon.spreadsheet.charts.converter.confwriter;

import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.PlotOptionsScatter;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ScatterSeriesData;

public class ScatterSeriesDataWriter extends AbstractSeriesDataWriter {

    private static final int SCATTER_LINE_WIDTH = 2;

    public ScatterSeriesDataWriter(ScatterSeriesData series) {
        super(series);
    }

    @Override
    protected ScatterSeriesData getSeriesData() {
        return (ScatterSeriesData) super.getSeriesData();
    }

    @Override
    protected PlotOptionsScatter createPlotOptions() {
        return new PlotOptionsScatter();
    }

    @Override
    protected PlotOptionsScatter getPlotOptions() {
        return (PlotOptionsScatter) super.getPlotOptions();
    }

    @Override
    protected void configureDataSeries(DataSeries dataSeriesForWriting) {
        super.configureDataSeries(dataSeriesForWriting);

        getPlotOptions().setLineWidth(SCATTER_LINE_WIDTH);
        getPlotOptions().setDashStyle(
                LineSeriesWriterUtils.getDashStyle(getSeriesData().dashStyle));

        if (!getSeriesData().markerSymbol.isEmpty())
            getPlotOptions().setMarker(
                    LineSeriesWriterUtils.getMarker(getSeriesData().markerSymbol));
    }
}
