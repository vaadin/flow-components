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

import org.openxmlformats.schemas.drawingml.x2006.chart.CTRadarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTRadarSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.STRadarStyle;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.RadarSeriesData;

public class RadarSeriesReader extends
        AbstractSeriesReader<CTRadarSer, RadarSeriesData> {

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
    protected void fillSeriesData(RadarSeriesData seriesData, CTRadarSer serie) {
        super.fillSeriesData(seriesData, serie);

        if (serie.getMarker() != null)
            LineSeriesReaderUtils.setMarkerForData(seriesData,
                    serie.getMarker());

        if (serie.getSpPr() != null)
            LineSeriesReaderUtils.setDashStyleForData(seriesData,
                    serie.getSpPr());
    }
}
