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
        if(getSeriesData().isColorByPoint){
            getPlotOptions().setColorByPoint(true);
        }

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
