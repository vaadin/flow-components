/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter.confwriter;

import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsScatter;
import com.vaadin.flow.component.spreadsheet.charts.converter.chartdata.ScatterSeriesData;

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

        if (getSeriesData().lineWidth != null) {
            getPlotOptions().setLineWidth(getSeriesData().lineWidth);
        } else {
            getPlotOptions().setLineWidth(SCATTER_LINE_WIDTH);
        }
        getPlotOptions().setDashStyle(
                LineSeriesWriterUtils.getDashStyle(getSeriesData().dashStyle));

        if (!getSeriesData().markerSymbol.isEmpty())
            getPlotOptions().setMarker(LineSeriesWriterUtils
                    .getMarker(getSeriesData().markerSymbol));
    }
}
