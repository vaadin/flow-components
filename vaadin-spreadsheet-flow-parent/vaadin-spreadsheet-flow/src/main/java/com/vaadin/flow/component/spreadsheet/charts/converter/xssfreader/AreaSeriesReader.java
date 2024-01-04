/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter.xssfreader;

import org.openxmlformats.schemas.drawingml.x2006.chart.CTAreaChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAreaSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.STGrouping;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.charts.converter.chartdata.AreaSeriesData;
import com.vaadin.flow.component.spreadsheet.charts.converter.chartdata.Stacking;

public class AreaSeriesReader
        extends AbstractSeriesReader<CTAreaSer, AreaSeriesData> {

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
