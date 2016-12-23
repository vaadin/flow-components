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

    public BarSeriesReader(CTBarChart ctChart, Spreadsheet spreadsheet, boolean showDataInHiddenCells) {
        super(ctChart, spreadsheet, showDataInHiddenCells);
    }

    public BarSeriesReader(CTBar3DChart ctChart, Spreadsheet spreadsheet, boolean showDataInHiddenCells) {
        super(ctChart, spreadsheet, true, showDataInHiddenCells);
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
        if (getBarDir().getVal() == STBarDir.BAR) {
            return new BarSeriesData();
        } else {
            return new ColumnSeriesData();
        }
    }

    @Override
    protected void fillSeriesData(ColumnSeriesData seriesData, CTBarSer serie) {
        super.fillSeriesData(seriesData, serie);
        if(getChart() instanceof CTBarChart) {
            CTBarChart chart = (CTBarChart) getChart();
            if (chart.getVaryColors() != null
                    && chart.getVaryColors().getVal()) {
                seriesData.isColorByPoint = true;
            }
        } else if(getChart() instanceof CTBar3DChart){
            CTBar3DChart chart = (CTBar3DChart) getChart();
            if (chart.getVaryColors() != null
                    && chart.getVaryColors().getVal()) {
                seriesData.isColorByPoint = true;
            }
        }

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
