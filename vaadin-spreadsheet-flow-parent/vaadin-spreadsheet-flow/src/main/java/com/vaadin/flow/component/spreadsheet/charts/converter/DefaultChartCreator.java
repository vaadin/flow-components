/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter;

import org.apache.poi.xssf.usermodel.XSSFChart;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Series;
import com.vaadin.flow.component.spreadsheet.ChartCreator;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.charts.converter.chartdata.ChartData;
import com.vaadin.flow.component.spreadsheet.charts.converter.confwriter.ChartDataToVaadinConfigWriter;
import com.vaadin.flow.component.spreadsheet.charts.converter.confwriter.SelectListeningDataSeries;
import com.vaadin.flow.component.spreadsheet.charts.converter.xssfreader.XSSFChartReader;
import com.vaadin.flow.component.Component;

public class DefaultChartCreator implements ChartCreator {

    private ChartDataToVaadinConfigWriter chartDataToVaadinConfigWriter;

    public DefaultChartCreator() {
        chartDataToVaadinConfigWriter = new ChartDataToVaadinConfigWriter();
    }

    @Override
    public Component createChart(XSSFChart chartXml, Spreadsheet spreadsheet) {

        ChartData chartDefinition = new XSSFChartReader(spreadsheet, chartXml)
                .readXSSFChart();

        Configuration conf = chartDataToVaadinConfigWriter
                .createConfigurationFromChartData(chartDefinition);

        Chart chart = new Chart();
        chart.setConfiguration(conf);

        chart.addPointSelectListener(event -> {
            Series series = event.getSeries();
            if (series instanceof SelectListeningDataSeries) {
                ((SelectListeningDataSeries) series).getSelectListener()
                        .selected();
            }
        });
        return chart;
    }
}
