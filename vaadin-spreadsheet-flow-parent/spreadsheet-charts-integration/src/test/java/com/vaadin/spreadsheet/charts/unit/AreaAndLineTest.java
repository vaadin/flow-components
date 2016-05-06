package com.vaadin.spreadsheet.charts.unit;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DashStyle;
import com.vaadin.addon.charts.model.MarkerSymbolEnum;
import com.vaadin.addon.charts.model.PlotOptionsLine;
import com.vaadin.addon.charts.model.Stacking;

public class AreaAndLineTest extends ChartBaseTest {

    @Test
    public void lineChart() throws Exception {
        Configuration conf = getChartFromSampleFile("TypeSample - Line.xlsx",
                "I10").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.LINE);
        assertData(conf.getSeries(), data);

        assertLineSeriesMarker((PlotOptionsLine) conf.getSeries().get(0)
                .getPlotOptions(), MarkerSymbolEnum.TRIANGLE);
        assertLineSeriesMarker((PlotOptionsLine) conf.getSeries().get(2)
                .getPlotOptions(), null);

        assertLineSeriesDash((PlotOptionsLine) conf.getSeries().get(3)
                .getPlotOptions(), DashStyle.LONGDASHDOT);
        assertLineSeriesDash((PlotOptionsLine) conf.getSeries().get(0)
                .getPlotOptions(), DashStyle.SOLID);
    }

    private void assertLineSeriesDash(PlotOptionsLine plotOptions,
            DashStyle longdashdot) {
        Assert.assertEquals("Wrong dash for line", longdashdot,
                plotOptions.getDashStyle());
    }

    private void assertLineSeriesMarker(PlotOptionsLine plotOptionsLine,
            MarkerSymbolEnum symbol) {
        Assert.assertEquals("Wrong marker symbol", symbol, plotOptionsLine
                .getMarker().getSymbol());
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
}
