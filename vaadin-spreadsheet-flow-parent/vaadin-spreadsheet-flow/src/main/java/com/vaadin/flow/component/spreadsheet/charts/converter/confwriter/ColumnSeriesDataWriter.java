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
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.Stacking;
import com.vaadin.flow.component.spreadsheet.charts.converter.Utils;
import com.vaadin.flow.component.spreadsheet.charts.converter.chartdata.ColumnSeriesData;

public class ColumnSeriesDataWriter extends AbstractSeriesDataWriter {

    public ColumnSeriesDataWriter(ColumnSeriesData series) {
        super(series);
    }

    @Override
    protected AbstractPlotOptions createPlotOptions() {
        return new PlotOptionsColumn();
    }

    @Override
    protected ColumnSeriesData getSeriesData() {
        return (ColumnSeriesData) super.getSeriesData();
    }

    @Override
    protected PlotOptionsColumn getPlotOptions() {
        return (PlotOptionsColumn) super.getPlotOptions();
    }

    @Override
    protected void configureDataSeries(DataSeries dataSeries) {
        super.configureDataSeries(dataSeries);

        String stacking = getSeriesData().stacking.toString();
        if (getSeriesData().isColorByPoint) {
            getPlotOptions().setColorByPoint(true);
        }

        getPlotOptions().setStacking(Utils.getEnumValueOrDefault(Stacking.class,
                stacking, Stacking.NONE));

        if (getSeriesData().is3d) {
            getPlotOptions().setPointPadding(0.2);
            getPlotOptions().setBorderWidth(0);
            getPlotOptions().setGroupZPadding(10);
        }
    }
}
