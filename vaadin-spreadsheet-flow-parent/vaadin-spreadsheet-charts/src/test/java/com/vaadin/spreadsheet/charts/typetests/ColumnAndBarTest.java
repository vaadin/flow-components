package com.vaadin.spreadsheet.charts.typetests;

import static org.junit.Assert.assertEquals;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.PlotOptionsArea;
import com.vaadin.addon.charts.model.PlotOptionsBar;
import com.vaadin.addon.charts.model.PlotOptionsColumn;
import com.vaadin.addon.charts.model.Stacking;
import com.vaadin.spreadsheet.charts.ChartTestBase;

public class ColumnAndBarTest extends ChartTestBase {

    protected Integer[][] columnAndBarData = { { 100, 200 }, { 200, 300 },
            { 400, 400 }, { 800, 500 }, { 1600, 600 } };

    @Test
    public void columnClusteredChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Column.xlsx",
                "C1").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.COLUMN);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.NONE);
    }

    @Test
    public void columnStuckedChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Column.xlsx",
                "I1").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.COLUMN);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.NORMAL);
    }

    @Test
    public void columnPercentStuckChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Column.xlsx",
                "O1").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.COLUMN);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.PERCENT);
    }

    @Test
    public void column3dClusteredChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Column.xlsx",
                "C20").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.COLUMN);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.NONE);
        assert3dEnabled(conf);
    }

    @Test
    public void column3dStuckedChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Column.xlsx",
                "I20").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.COLUMN);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.NORMAL);
        assert3dEnabled(conf);
    }

    @Test
    public void column3dPercentStuckChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Column.xlsx",
                "O20").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.COLUMN);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.PERCENT);
        assert3dEnabled(conf);
    }

    @Test
    public void barClusteredChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Bar.xlsx",
                "C1").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.BAR);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.NONE);
    }

    @Test
    public void barStuckedChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Bar.xlsx",
                "I1").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.BAR);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.NORMAL);
    }

    @Test
    public void barPercentStuckChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Bar.xlsx",
                "O1").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.BAR);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.PERCENT);
    }

    @Test
    public void bar3dClusteredChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Bar.xlsx",
                "C20").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.BAR);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.NONE);
        assert3dEnabled(conf);
    }

    @Test
    public void bar3dStuckedChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Bar.xlsx",
                "I20").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.BAR);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.NORMAL);
        assert3dEnabled(conf);
    }

    @Test
    public void bar3dPercentStuckChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Bar.xlsx",
                "O20").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.BAR);
        assertData(conf.getSeries(), columnAndBarData);
        assertStacking(conf.getSeries(), Stacking.PERCENT);
        assert3dEnabled(conf);
    }

    @Test
    public void pyramidCylinderConeCharts() throws Exception {
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
    public void columnNotColorByPoint() throws Exception {
        Configuration conf = getChartFromSampleFile("ColorByPoint.xlsx",
                "K2").getConfiguration();
        assertSeriesType(conf.getSeries(), ChartType.COLUMN);
        Assert.assertNull(((PlotOptionsColumn) conf.getSeries().get(0)
                .getPlotOptions()).getColorByPoint());
    }

    @Test
    public void columnColorByPoint() throws Exception {
        Configuration conf = getChartFromSampleFile("ColorByPoint.xlsx",
                "D2").getConfiguration();
        assertSeriesType(conf.getSeries(), ChartType.COLUMN);
        Assert.assertTrue(((PlotOptionsColumn) conf.getSeries().get(0)
                .getPlotOptions()).getColorByPoint());
    }

    @Test
    public void barNotColorByPoint() throws Exception {
        Configuration conf = getChartFromSampleFile("ColorByPoint.xlsx",
                "K18").getConfiguration();
        assertSeriesType(conf.getSeries(), ChartType.BAR);
        Assert.assertNull(((PlotOptionsBar) conf.getSeries().get(0)
                .getPlotOptions()).getColorByPoint());
    }

    @Test
    public void barColorByPoint() throws Exception {
        Configuration conf = getChartFromSampleFile("ColorByPoint.xlsx",
                "D18").getConfiguration();
        assertSeriesType(conf.getSeries(), ChartType.BAR);
        Assert.assertTrue(((PlotOptionsBar) conf.getSeries().get(0)
                .getPlotOptions()).getColorByPoint());
    }

    @Test
    public void barWithHiddenCol() throws Exception {
        Configuration conf = getChartFromSampleFile(
            "HiddenColumnChartTest.xlsx", "D10").getConfiguration();
        assertSeriesType(conf.getSeries(), ChartType.COLUMN);
        List<DataSeriesItem> data = ((DataSeries) conf.getSeries().get(0))
            .getData();
        assertEquals(5, data.size());
        assertEquals("c", data.get(2).getName());

    }

}
