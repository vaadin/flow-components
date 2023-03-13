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
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.PlotOptionsPie;
import com.vaadin.flow.component.charts.model.Series;
import com.vaadin.flow.component.spreadsheet.charts.converter.chartdata.AbstractSeriesData.SeriesPoint;
import com.vaadin.flow.component.spreadsheet.charts.converter.chartdata.PieSeriesData;

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
