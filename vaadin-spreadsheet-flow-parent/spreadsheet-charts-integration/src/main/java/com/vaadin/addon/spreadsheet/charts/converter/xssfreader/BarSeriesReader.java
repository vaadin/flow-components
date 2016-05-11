package com.vaadin.addon.spreadsheet.charts.converter.xssfreader;

import org.openxmlformats.schemas.drawingml.x2006.chart.CTBar3DChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarDir;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarGrouping;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.STBarDir;
import org.openxmlformats.schemas.drawingml.x2006.chart.STBarGrouping;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.charts.converter.Utils;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.BarSeriesData;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ColumnSeriesData;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.Stacking;

public class BarSeriesReader extends
        AbstractSeriesReader<CTBarSer, ColumnSeriesData> {

    public BarSeriesReader(CTBarChart ctChart, Spreadsheet spreadsheet) {
        super(ctChart, spreadsheet);
    }

    public BarSeriesReader(CTBar3DChart ctChart, Spreadsheet spreadsheet) {
        super(ctChart, spreadsheet, true);
    }

    private CTBarDir getBarDir() {
        return (CTBarDir) Utils.callMethodUsingReflection(getChart(),
                "getBarDir");
    }

    private CTBarGrouping getGrouping() {
        return (CTBarGrouping) Utils.callMethodUsingReflection(getChart(),
                "getGrouping");
    }

    @Override
    protected ColumnSeriesData createSeriesDataObject(CTBarSer serie) {
        if (getBarDir().getVal() == STBarDir.BAR)
            return new BarSeriesData();
        else
            return new ColumnSeriesData();
    }

    @Override
    protected void fillSeriesData(ColumnSeriesData seriesData, CTBarSer serie) {
        super.fillSeriesData(seriesData, serie);

        seriesData.stacking = getStacking(getGrouping().getVal());

        // I need this trick because for some reason "reversed x-axis" doesn't
        // work in Highcharts
        
        // FIXME: commented it out because with the latest version it breaks column and bar charts
//        reverseSeriesData(seriesData.seriesData);
//        if (seriesData.categories != null)
//            Collections.reverse(seriesData.categories);
    }

    // private void reverseSeriesData(List<SeriesPoint> seriesData) {
    // final int size = seriesData.size();
    // for (int i = 0; i < seriesData.size(); i++) {
    // seriesData.get(i).xValue = size - 1 - i;
    // }
    // }

    private Stacking getStacking(STBarGrouping.Enum grouping) {
        if (grouping == STBarGrouping.PERCENT_STACKED) {
            return Stacking.PERCENT;
        } else if (grouping == STBarGrouping.CLUSTERED) {
            // default
        } else if (grouping == STBarGrouping.STANDARD) {
            // what's that?
        } else if (grouping == STBarGrouping.STACKED) {
            return Stacking.NORMAL;
        }

        return Stacking.NONE;
    }
}
