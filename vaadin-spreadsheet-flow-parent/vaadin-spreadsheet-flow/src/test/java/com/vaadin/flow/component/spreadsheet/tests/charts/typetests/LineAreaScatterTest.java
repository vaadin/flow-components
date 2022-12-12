package com.vaadin.flow.component.spreadsheet.tests.charts.typetests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DashStyle;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.MarkerSymbol;
import com.vaadin.flow.component.charts.model.MarkerSymbolEnum;
import com.vaadin.flow.component.charts.model.PlotOptionsLine;
import com.vaadin.flow.component.charts.model.PlotOptionsScatter;
import com.vaadin.flow.component.charts.model.Stacking;
import com.vaadin.flow.component.spreadsheet.tests.charts.ChartTestBase;

public class LineAreaScatterTest extends ChartTestBase {

    @Test
    public void lineChartWithOrWithoutMarker() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Line.xlsx",
                "I10").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.LINE);
        assertDashStyle(conf.getSeries(), DashStyle.SOLID, DashStyle.SOLID,
                DashStyle.SOLID, DashStyle.LONGDASHDOT, DashStyle.SOLID);
        assertData(conf.getSeries(), data);

        assertLineSeriesMarker(
                ((PlotOptionsLine) conf.getSeries().get(0).getPlotOptions())
                        .getMarker().getSymbol(),
                MarkerSymbolEnum.TRIANGLE);
        assertLineSeriesMarker(
                ((PlotOptionsLine) conf.getSeries().get(2).getPlotOptions())
                        .getMarker().getSymbol(),
                null);

        assertLineSeriesDash(
                ((PlotOptionsLine) conf.getSeries().get(3).getPlotOptions())
                        .getDashStyle(),
                DashStyle.LONGDASHDOT);
        assertLineSeriesDash(
                ((PlotOptionsLine) conf.getSeries().get(0).getPlotOptions())
                        .getDashStyle(),
                DashStyle.SOLID);
    }

    private void assertLineSeriesDash(DashStyle actual, DashStyle expected) {
        Assert.assertEquals("Wrong dash for line", expected, actual);
    }

    private void assertLineSeriesMarker(MarkerSymbol foundSymbol,
            MarkerSymbolEnum expectedSymbol) {
        Assert.assertEquals("Wrong marker symbol", expectedSymbol, foundSymbol);
    }

    @Test
    public void areaChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Area.xlsx",
                "A7").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.AREA);
        assertData(conf.getSeries(), data);
        assertStacking(conf.getSeries(), Stacking.NONE);
        assertDashStyle(conf.getSeries(), DashStyle.SOLID);
    }

    @Test
    public void area100StackChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Area.xlsx",
                "O7").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.AREA);
        assertData(conf.getSeries(), data);
        assertStacking(conf.getSeries(), Stacking.PERCENT);
    }

    @Test
    public void areaStackChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Area.xlsx",
                "H7").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.AREA);
        assertData(conf.getSeries(), data);
        assertStacking(conf.getSeries(), Stacking.NORMAL);
    }

    @Test
    public void scatterChartWithOrWithoutMarkerAndLine() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Scatter.xlsx",
                "A3").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.SCATTER);
        assertDashStyle(conf.getSeries(), DashStyle.LONGDASHDOT);
        assertDataXY((DataSeries) conf.getSeries().get(0),
                new Double[][] { data[0], data[1] });

        assertLineSeriesMarker(
                ((PlotOptionsScatter) conf.getSeries().get(0).getPlotOptions())
                        .getMarker().getSymbol(),
                MarkerSymbolEnum.TRIANGLE);
        assertLineSeriesDash(
                ((PlotOptionsScatter) conf.getSeries().get(0).getPlotOptions())
                        .getDashStyle(),
                DashStyle.LONGDASHDOT);
    }
}
