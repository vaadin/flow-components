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
import com.vaadin.addon.charts.model.PlotOptionsLine;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.LineSeriesData;

public class LineSeriesDataWriter extends AbstractSeriesDataWriter {

    public LineSeriesDataWriter(LineSeriesData series) {
        super(series);
    }

    @Override
    protected LineSeriesData getSeriesData() {
        return (LineSeriesData) super.getSeriesData();
    }

    @Override
    protected AbstractPlotOptions createPlotOptions() {
        return new PlotOptionsLine();
    }

    @Override
    protected PlotOptionsLine getPlotOptions() {
        return (PlotOptionsLine) super.getPlotOptions();
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
