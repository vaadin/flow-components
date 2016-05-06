package com.vaadin.addon.spreadsheet.charts.converter.confwriter;

import com.vaadin.addon.charts.model.AbstractPlotOptions;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.PlotOptionsBar;
import com.vaadin.addon.charts.model.Stacking;
import com.vaadin.addon.spreadsheet.charts.converter.Utils;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.BarSeriesData;

public class BarSeriesDataWriter extends AbstractSeriesDataWriter {

    public BarSeriesDataWriter(BarSeriesData series) {
        super(series);
    }

    @Override
    protected AbstractPlotOptions createPlotOptions() {
        return new PlotOptionsBar();
    }

    @Override
    protected BarSeriesData getSeriesData() {
        return (BarSeriesData) super.getSeriesData();
    }

    @Override
    protected PlotOptionsBar getPlotOptions() {
        return (PlotOptionsBar) super.getPlotOptions();
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
