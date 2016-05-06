package com.vaadin.addon.spreadsheet.charts.converter.xssfreader;

import org.openxmlformats.schemas.drawingml.x2006.chart.CTAreaChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAreaSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.STGrouping;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.AreaSeriesData;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.Stacking;

public class AreaSeriesReader extends
        AbstractSeriesReader<CTAreaSer, AreaSeriesData> {

    public AreaSeriesReader(CTAreaChart ctChart, Spreadsheet spreadsheet) {
        super(ctChart, spreadsheet);
    }

    @Override
    protected AreaSeriesData createSeriesDataObject(CTAreaSer serie) {
        return new AreaSeriesData();
    }

    @Override
    protected void fillSeriesData(AreaSeriesData seriesData, CTAreaSer serie) {
        super.fillSeriesData(seriesData, serie);
        
        CTAreaChart chart = (CTAreaChart) getChart();
        if (chart.isSetGrouping()) {
            seriesData.stacking = getStacking(chart.getGrouping().getVal());
        }
    }

    private Stacking getStacking(STGrouping.Enum grouping) {
        if (grouping == STGrouping.PERCENT_STACKED) {
            return Stacking.PERCENT;
        } else if (grouping == STGrouping.STANDARD) {
            return Stacking.NONE;
        } else if (grouping == STGrouping.STACKED) {
            return Stacking.NORMAL;
        }

        return Stacking.NONE;
    }
}
