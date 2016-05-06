package com.vaadin.addon.spreadsheet.charts.converter.confwriter;

import com.vaadin.addon.charts.model.AbstractPlotOptions;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.PlotOptionsColumn;
import com.vaadin.addon.charts.model.Stacking;
import com.vaadin.addon.spreadsheet.charts.converter.Utils;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ColumnSeriesData;

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

        getPlotOptions().setStacking(
                Utils.getEnumValueOrDefault(Stacking.class, stacking,
                        Stacking.NONE));

        if (getSeriesData().is3d) {
            getPlotOptions().setPointPadding(0.2);
            getPlotOptions().setBorderWidth(0);
            getPlotOptions().setGroupZPadding(10);
        }
    }
}
