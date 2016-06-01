package com.vaadin.spreadsheet.charts.typetests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DashStyle;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.MarkerSymbol;
import com.vaadin.addon.charts.model.MarkerSymbolEnum;
import com.vaadin.addon.charts.model.PlotOptionsLine;
import com.vaadin.addon.charts.model.PlotOptionsScatter;
import com.vaadin.addon.charts.model.Stacking;
import com.vaadin.spreadsheet.charts.ChartTestBase;

public class LineAreaScatterTest extends ChartTestBase {

    @Test
    public void lineChartWithOrWithoutMarker() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Line.xlsx",
                "I10").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.LINE);
        assertData(conf.getSeries(), data);

        assertLineSeriesMarker(((PlotOptionsLine) conf.getSeries().get(0)
                .getPlotOptions()).getMarker().getSymbol(), MarkerSymbolEnum.TRIANGLE);
        assertLineSeriesMarker(((PlotOptionsLine) conf.getSeries().get(2)
                .getPlotOptions()).getMarker().getSymbol(), null);

        assertLineSeriesDash(((PlotOptionsLine) conf.getSeries().get(3)
                .getPlotOptions()).getDashStyle(), DashStyle.LONGDASHDOT);
        assertLineSeriesDash(((PlotOptionsLine) conf.getSeries().get(0)
                .getPlotOptions()).getDashStyle(), DashStyle.SOLID);
    }

    private void assertLineSeriesDash(DashStyle actual,
            DashStyle expected) {
        Assert.assertEquals("Wrong dash for line", expected,
                actual);
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
        assertDataXY((DataSeries) conf.getSeries().get(0), new Double[][] { data[0], data[1] });

        assertLineSeriesMarker(((PlotOptionsScatter) conf.getSeries().get(0)
                .getPlotOptions()).getMarker().getSymbol(), MarkerSymbolEnum.TRIANGLE);
        assertLineSeriesDash(((PlotOptionsScatter) conf.getSeries().get(0)
                .getPlotOptions()).getDashStyle(), DashStyle.LONGDASHDOT);
    }

}
