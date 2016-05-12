package com.vaadin.addon.spreadsheet.charts.converter;

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

import org.apache.poi.xssf.usermodel.XSSFChart;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.PointSelectEvent;
import com.vaadin.addon.charts.PointSelectListener;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.Series;
import com.vaadin.addon.spreadsheet.SheetChartWrapper;
import com.vaadin.addon.spreadsheet.SheetChartWrapper.ChartCreator;
import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData;
import com.vaadin.addon.spreadsheet.charts.converter.confwriter.ChartDataToVaadinConfigWriter;
import com.vaadin.addon.spreadsheet.charts.converter.confwriter.SelectListeningDataSeries;
import com.vaadin.addon.spreadsheet.charts.converter.xssfreader.XSSFChartReader;
import com.vaadin.ui.Component;

/**
 * initializeChartIntegration() has to be called in order for charts to appear correctly.
 * If the static block is not executed in a certain environment automatically, it has to be called explicitly.
 */
@SuppressWarnings("serial")
public class ChartConverter {
    private static ChartCreator chartCreator = new ChartCreator() {
        @Override
        public Component createChart(XSSFChart chartXml, Spreadsheet spreadsheet) {
            Chart chart = new Chart();

            Configuration conf = createVaadinChartConfigurationFromXSSFChart(
                    chartXml, spreadsheet);

            chart.setConfiguration(conf);

            chart.addPointSelectListener(new PointSelectListener() {
                @Override
                public void onSelect(PointSelectEvent event) {
                    Series series = event.getSeries();
                    if (series instanceof SelectListeningDataSeries) {
                        ((SelectListeningDataSeries) series)
                                .getSelectListener().selected();
                    }
                }
            });

            return chart;
        }
    };

    static {
        initSpreadsheetChartsIntegration();
    }

    public static void initSpreadsheetChartsIntegration() {
        SheetChartWrapper.setChartCreator(chartCreator);
    }

    public static Configuration createVaadinChartConfigurationFromXSSFChart(
            XSSFChart chart, Spreadsheet spreadsheet) {
        ChartData chartDefinition = new XSSFChartReader(spreadsheet, chart)
                .readXSSFChart();

        Configuration conf = new ChartDataToVaadinConfigWriter()
                .createConfigurationFromChartData(chartDefinition);

        return conf;
    }
}
