package com.vaadin.addon.spreadsheet.charts.converter;

import org.apache.poi.xssf.usermodel.XSSFChart;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.PointSelectEvent;
import com.vaadin.addon.charts.PointSelectListener;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.Series;
import com.vaadin.addon.spreadsheet.ChartCreator;
import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData;
import com.vaadin.addon.spreadsheet.charts.converter.confwriter.ChartDataToVaadinConfigWriter;
import com.vaadin.addon.spreadsheet.charts.converter.confwriter.SelectListeningDataSeries;
import com.vaadin.addon.spreadsheet.charts.converter.xssfreader.XSSFChartReader;
import com.vaadin.ui.Component;

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

        chart.addPointSelectListener(new PointSelectListener() {
            @Override
            public void onSelect(PointSelectEvent event) {
                Series series = event.getSeries();
                if (series instanceof SelectListeningDataSeries) {
                    ((SelectListeningDataSeries) series).getSelectListener()
                            .selected();
                }
            }
        });
        return chart;
    }
}
