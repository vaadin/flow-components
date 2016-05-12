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
import com.vaadin.addon.charts.model.PlotOptionsSpline;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.SplineSeriesData;

public class SplineSeriesDataWriter extends AbstractSeriesDataWriter {

    public SplineSeriesDataWriter(SplineSeriesData series) {
        super(series);
    }

    @Override
    protected SplineSeriesData getSeriesData() {
        return (SplineSeriesData) super.getSeriesData();
    }

    @Override
    protected PlotOptionsSpline createPlotOptions() {
        return new PlotOptionsSpline();
    }

    @Override
    protected PlotOptionsSpline getPlotOptions() {
        return (PlotOptionsSpline) super.getPlotOptions();
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
