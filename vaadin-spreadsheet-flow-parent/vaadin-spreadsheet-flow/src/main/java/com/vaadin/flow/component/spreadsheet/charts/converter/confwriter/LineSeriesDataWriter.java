/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter.confwriter;

import com.vaadin.flow.component.charts.model.AbstractPlotOptions;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsLine;
import com.vaadin.flow.component.spreadsheet.charts.converter.chartdata.LineSeriesData;

public class LineSeriesDataWriter extends AbstractSeriesDataWriter {

    public LineSeriesDataWriter(LineSeriesData series) {
        super(series);
    }

    @Override
    protected LineSeriesData getSeriesData() {
        return (LineSeriesData) super.getSeriesData();
    }

    @Override
    protected AbstractPlotOptions createPlotOptions() {
        return new PlotOptionsLine();
    }

    @Override
    protected PlotOptionsLine getPlotOptions() {
        return (PlotOptionsLine) super.getPlotOptions();
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
