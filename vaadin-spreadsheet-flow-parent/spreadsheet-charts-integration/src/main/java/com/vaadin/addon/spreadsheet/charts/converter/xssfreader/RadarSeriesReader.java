package com.vaadin.addon.spreadsheet.charts.converter.xssfreader;

import org.openxmlformats.schemas.drawingml.x2006.chart.CTRadarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTRadarSer;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.RadarSeriesData;

public class RadarSeriesReader extends
        AbstractSeriesReader<CTRadarSer, RadarSeriesData> {

    public RadarSeriesReader(CTRadarChart ctChart, Spreadsheet spreadsheet) {
        super(ctChart, spreadsheet);
    }

    @Override
    protected RadarSeriesData createSeriesDataObject(CTRadarSer serie) {
        return new RadarSeriesData();
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
