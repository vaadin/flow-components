/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter.xssfreader;

import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineSer;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.charts.converter.chartdata.LineSeriesData;
import com.vaadin.flow.component.spreadsheet.charts.converter.chartdata.SplineSeriesData;

public class LineSeriesReader
        extends AbstractSeriesReader<CTLineSer, LineSeriesData> {

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
