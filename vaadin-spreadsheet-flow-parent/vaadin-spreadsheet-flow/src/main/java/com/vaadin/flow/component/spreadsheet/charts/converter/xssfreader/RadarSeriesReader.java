/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter.xssfreader;

import org.openxmlformats.schemas.drawingml.x2006.chart.CTRadarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTRadarSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.STRadarStyle;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.charts.converter.chartdata.RadarSeriesData;

public class RadarSeriesReader
        extends AbstractSeriesReader<CTRadarSer, RadarSeriesData> {

    public RadarSeriesReader(CTRadarChart ctChart, Spreadsheet spreadsheet,
            boolean showDataInHiddenCells) {
        super(ctChart, spreadsheet, showDataInHiddenCells);
    }

    @Override
    protected RadarSeriesData createSeriesDataObject(CTRadarSer serie) {
        RadarSeriesData result = new RadarSeriesData();
        CTRadarChart radarChart = (CTRadarChart) getChart();
        if (radarChart.getRadarStyle() != null
                && radarChart.getRadarStyle().getVal() != STRadarStyle.FILLED) {
            result.filled = false;
        }
        return result;
    }

    @Override
    protected void fillSeriesData(RadarSeriesData seriesData,
            CTRadarSer serie) {
        super.fillSeriesData(seriesData, serie);

        if (serie.getMarker() != null)
            LineSeriesReaderUtils.setMarkerForData(seriesData,
                    serie.getMarker());

        if (serie.getSpPr() != null)
            LineSeriesReaderUtils.setDashStyleForData(seriesData,
                    serie.getSpPr());
    }
}
