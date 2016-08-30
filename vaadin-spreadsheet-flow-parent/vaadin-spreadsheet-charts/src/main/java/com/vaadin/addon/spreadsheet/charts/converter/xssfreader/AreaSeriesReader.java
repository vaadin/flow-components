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

import org.openxmlformats.schemas.drawingml.x2006.chart.CTAreaChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAreaSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.STGrouping;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.AreaSeriesData;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.Stacking;

public class AreaSeriesReader extends
        AbstractSeriesReader<CTAreaSer, AreaSeriesData> {

    public AreaSeriesReader(CTAreaChart ctChart, Spreadsheet spreadsheet,
            boolean showDataInHiddenCells) {
        super(ctChart, spreadsheet, showDataInHiddenCells);
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
