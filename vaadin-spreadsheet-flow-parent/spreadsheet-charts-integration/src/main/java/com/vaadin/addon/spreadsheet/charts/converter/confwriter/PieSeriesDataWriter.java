package com.vaadin.addon.spreadsheet.charts.converter.confwriter;

import com.vaadin.addon.charts.model.AbstractPlotOptions;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.PlotOptionsPie;
import com.vaadin.addon.charts.model.Series;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.AbstractSeriesData.SeriesPoint;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.PieSeriesData;

public class PieSeriesDataWriter extends AbstractSeriesDataWriter {

    public PieSeriesDataWriter(PieSeriesData series) {
        super(series);
    }

    @Override
    protected PieSeriesData getSeriesData() {
        return (PieSeriesData) super.getSeriesData();
    }
    
    @Override
    public Series convertSeries(boolean blanksAsZeros) {
        // Highcharts does not accept pie charts with nulls
        return super.convertSeries(true);
    }

    @Override
    protected DataSeriesItem createDataSeriesItem(SeriesPoint point,
            boolean blanksAsZeros) {
        DataSeriesItem item = super.createDataSeriesItem(point, true);

        if (getSeriesData().categories.size() > point.xValue.intValue())
            item.setName(getSeriesData().categories.get(point.xValue.intValue()));

        item.setSliced(getSeriesData().isExploded);
        return item;
    }

    @Override
    protected AbstractPlotOptions createPlotOptions() {
        PlotOptionsPie plotOptionsPie = new PlotOptionsPie();
        if (getSeriesData().is3d) {
            // is this a parameter in Excel?
            plotOptionsPie.setDepth(20);
        }
        return plotOptionsPie;
    }
}
