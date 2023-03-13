/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter.confwriter;

import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsSpline;
import com.vaadin.flow.component.spreadsheet.charts.converter.chartdata.SplineSeriesData;

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
