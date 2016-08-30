package com.vaadin.addon.spreadsheet.charts.converter.xssfreader;

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

import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineSer;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.LineSeriesData;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.SplineSeriesData;

public class LineSeriesReader extends
        AbstractSeriesReader<CTLineSer, LineSeriesData> {

    public LineSeriesReader(CTLineChart ctChart, Spreadsheet spreadsheet,
            boolean showDataInHiddenCells) {
        super(ctChart, spreadsheet, showDataInHiddenCells);
    }

    @Override
    protected LineSeriesData createSeriesDataObject(CTLineSer serie) {
        if (serie.getSmooth().getVal())
            return new SplineSeriesData();
        else
            return new LineSeriesData();
    }

    @Override
    protected void fillSeriesData(LineSeriesData seriesData, CTLineSer serie) {
        super.fillSeriesData(seriesData, serie);

        if (serie.getMarker() != null)
            LineSeriesReaderUtils.setMarkerForData(seriesData,
                    serie.getMarker());

        if (serie.getSpPr() != null)
            LineSeriesReaderUtils.setDashStyleForData(seriesData,
                    serie.getSpPr());
    }
}
