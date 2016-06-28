package com.vaadin.addon.spreadsheet.charts.converter.confwriter;

/*
 * #%L
 * Vaadin Spreadsheet Charts Integration
 * %%
 * Copyright (C) 2016 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

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
