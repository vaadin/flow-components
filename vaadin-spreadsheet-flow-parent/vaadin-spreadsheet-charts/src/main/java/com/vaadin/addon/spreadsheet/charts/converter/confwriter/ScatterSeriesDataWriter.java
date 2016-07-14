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

import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.PlotOptionsScatter;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ScatterSeriesData;

public class ScatterSeriesDataWriter extends AbstractSeriesDataWriter {

    private static final int SCATTER_LINE_WIDTH = 2;

    public ScatterSeriesDataWriter(ScatterSeriesData series) {
        super(series);
    }

    @Override
    protected ScatterSeriesData getSeriesData() {
        return (ScatterSeriesData) super.getSeriesData();
    }

    @Override
    protected PlotOptionsScatter createPlotOptions() {
        return new PlotOptionsScatter();
    }

    @Override
    protected PlotOptionsScatter getPlotOptions() {
        return (PlotOptionsScatter) super.getPlotOptions();
    }

    @Override
    protected void configureDataSeries(DataSeries dataSeriesForWriting) {
        super.configureDataSeries(dataSeriesForWriting);

        if (getSeriesData().lineWidth != null) {
            getPlotOptions().setLineWidth(getSeriesData().lineWidth);
        } else {
            getPlotOptions().setLineWidth(SCATTER_LINE_WIDTH);
        }
        getPlotOptions().setDashStyle(
                LineSeriesWriterUtils.getDashStyle(getSeriesData().dashStyle));

        if (!getSeriesData().markerSymbol.isEmpty())
            getPlotOptions().setMarker(
                    LineSeriesWriterUtils.getMarker(getSeriesData().markerSymbol));
    }
}
