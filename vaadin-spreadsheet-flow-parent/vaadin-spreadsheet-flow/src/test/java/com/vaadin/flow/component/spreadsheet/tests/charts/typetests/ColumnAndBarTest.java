/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.charts.typetests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.BarGrouping;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.XDDFBar3DChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.PlotOptionsBar;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.Stacking;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.tests.charts.ChartTestBase;

class ColumnAndBarTest extends ChartTestBase {

    protected Integer[][] columnAndBarData = { { 100, 200 }, { 200, 300 },
            { 400, 400 }, { 800, 500 }, { 1600, 600 } };

    @Test
    void columnClusteredChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Column.xlsx",
                "C1").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.COLUMN);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.NONE);
    }

    @Test
    void columnStuckedChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Column.xlsx",
                "I1").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.COLUMN);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.NORMAL);
    }

    @Test
    void columnPercentStuckChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Column.xlsx",
                "O1").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.COLUMN);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.PERCENT);
    }

    @Test
    void column3dClusteredChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Column.xlsx",
                "C20").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.COLUMN);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.NONE);
        assert3dEnabled(conf);
    }

    @Test
    void column3dStuckedChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Column.xlsx",
                "I20").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.COLUMN);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.NORMAL);
        assert3dEnabled(conf);
    }

    @Test
    void column3dPercentStuckChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Column.xlsx",
                "O20").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.COLUMN);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.PERCENT);
        assert3dEnabled(conf);
    }

    @Test
    void barClusteredChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Bar.xlsx",
                "C1").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.BAR);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.NONE);
    }

    @Test
    void barStuckedChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Bar.xlsx",
                "I1").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.BAR);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.NORMAL);
    }

    @Test
    void barPercentStuckChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Bar.xlsx",
                "O1").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.BAR);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.PERCENT);
    }

    @Test
    void bar3dClusteredChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Bar.xlsx",
                "C20").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.BAR);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.NONE);
        assert3dEnabled(conf);
    }

    @Test
    void bar3dStuckedChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Bar.xlsx",
                "I20").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.BAR);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.NORMAL);
        assert3dEnabled(conf);
    }

    @Test
    void bar3dPercentStuckChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Bar.xlsx",
                "O20").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.BAR);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.PERCENT);
        assert3dEnabled(conf);
    }

    @Test
    void pyramidCylinderConeCharts() throws Exception {
        testColumnChart("E2");
        testColumnChart("M2");
        testColumnChart("E19");
        testColumnChart("M19");
    }

    private void testColumnChart(String cell) throws Exception {
        Configuration conf = getChartFromSampleFile(
                "TypeSample - Pyramid, cylinder, cone.xlsx", cell)
                .getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.COLUMN);
        assertData(conf.getSeries(), columnAndBarData);
    }

    @Test
    void columnNotColorByPoint() throws Exception {
        Configuration conf = getChartFromSampleFile("ColorByPoint.xlsx", "K2")
                .getConfiguration();
        assertSeriesType(conf.getSeries(), ChartType.COLUMN);
        Assertions.assertNull(
                ((PlotOptionsColumn) conf.getSeries().get(0).getPlotOptions())
                        .getColorByPoint());
    }

    @Test
    void columnColorByPoint() throws Exception {
        Configuration conf = getChartFromSampleFile("ColorByPoint.xlsx", "D2")
                .getConfiguration();
        assertSeriesType(conf.getSeries(), ChartType.COLUMN);
        Assertions.assertTrue(
                ((PlotOptionsColumn) conf.getSeries().get(0).getPlotOptions())
                        .getColorByPoint());
    }

    @Test
    void barNotColorByPoint() throws Exception {
        Configuration conf = getChartFromSampleFile("ColorByPoint.xlsx", "K18")
                .getConfiguration();
        assertSeriesType(conf.getSeries(), ChartType.BAR);
        Assertions.assertNull(
                ((PlotOptionsBar) conf.getSeries().get(0).getPlotOptions())
                        .getColorByPoint());
    }

    @Test
    void barColorByPoint() throws Exception {
        Configuration conf = getChartFromSampleFile("ColorByPoint.xlsx", "D18")
                .getConfiguration();
        assertSeriesType(conf.getSeries(), ChartType.BAR);
        Assertions.assertTrue(
                ((PlotOptionsBar) conf.getSeries().get(0).getPlotOptions())
                        .getColorByPoint());
    }

    @Test
    void bar3DColorByPoint() throws Exception {
        XSSFSheet sheet = sheetWithThreeValues();
        XSSFChart chart = chartAtD2(sheet);
        XDDFBar3DChartData bars = (XDDFBar3DChartData) chart.createData(
                ChartTypes.BAR3D, chart.createCategoryAxis(AxisPosition.BOTTOM),
                chart.createValueAxis(AxisPosition.LEFT));
        bars.setBarDirection(BarDirection.BAR);
        bars.setBarGrouping(BarGrouping.CLUSTERED);
        bars.setVaryColors(true);
        bars.addSeries(categories(sheet), values(sheet));
        chart.plot(bars);

        Configuration conf = getChart(new Spreadsheet(sheet.getWorkbook()),
                "D2").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.BAR);
        Assertions.assertTrue(
                ((PlotOptionsBar) conf.getSeries().get(0).getPlotOptions())
                        .getColorByPoint());
    }

    @Test
    void columnWithoutAGrouping_isNotStacked() throws Exception {
        XSSFSheet sheet = sheetWithThreeValues();
        XSSFChart chart = chartAtD2(sheet);
        XDDFBarChartData bars = (XDDFBarChartData) chart.createData(
                ChartTypes.BAR, chart.createCategoryAxis(AxisPosition.BOTTOM),
                chart.createValueAxis(AxisPosition.LEFT));
        bars.setBarDirection(BarDirection.COL);
        bars.addSeries(categories(sheet), values(sheet));
        chart.plot(bars);

        Configuration conf = getChart(new Spreadsheet(sheet.getWorkbook()),
                "D2").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.COLUMN);
        assertStacking(conf.getSeries(), Stacking.NONE);
    }

    /** Three categories in A1:A3, with their values in B1:B3. */
    private static XSSFSheet sheetWithThreeValues() {
        XSSFSheet sheet = new XSSFWorkbook().createSheet();
        for (int i = 0; i < 3; i++) {
            XSSFRow row = sheet.createRow(i);
            row.createCell(0).setCellValue("Category " + i);
            row.createCell(1).setCellValue(10 * (i + 1));
        }
        return sheet;
    }

    private static XSSFChart chartAtD2(XSSFSheet sheet) {
        return sheet.createDrawingPatriarch()
                .createChart(new XSSFClientAnchor(0, 0, 0, 0, 3, 1, 10, 15));
    }

    private static XDDFDataSource<String> categories(XSSFSheet sheet) {
        return XDDFDataSourcesFactory.fromStringCellRange(sheet,
                CellRangeAddress.valueOf("A1:A3"));
    }

    private static XDDFNumericalDataSource<Double> values(XSSFSheet sheet) {
        return XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                CellRangeAddress.valueOf("B1:B3"));
    }

    @Test
    void barWithHiddenCol() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "HiddenColumnChartTest.xlsx", "D10").getConfiguration();
        assertSeriesType(conf.getSeries(), ChartType.COLUMN);
        List<DataSeriesItem> data = ((DataSeries) conf.getSeries().get(0))
                .getData();
        assertEquals(5, data.size());
        assertEquals("c", data.get(2).getName());

    }

}
