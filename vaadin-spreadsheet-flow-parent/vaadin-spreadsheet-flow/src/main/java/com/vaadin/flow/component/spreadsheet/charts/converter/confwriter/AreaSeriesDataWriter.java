/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter.confwriter;

import com.vaadin.flow.component.charts.model.AbstractPlotOptions;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsArea;
import com.vaadin.flow.component.charts.model.Stacking;
import com.vaadin.flow.component.spreadsheet.charts.converter.Utils;
import com.vaadin.flow.component.spreadsheet.charts.converter.chartdata.AreaSeriesData;

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

        getPlotOptions().setStacking(Utils.getEnumValueOrDefault(Stacking.class,
                stacking, Stacking.NONE));

        getPlotOptions().setDashStyle(
                LineSeriesWriterUtils.getDashStyle(getSeriesData().dashStyle));
        getPlotOptions().setMarker(
                LineSeriesWriterUtils.getMarker(getSeriesData().markerSymbol));

    }
}
