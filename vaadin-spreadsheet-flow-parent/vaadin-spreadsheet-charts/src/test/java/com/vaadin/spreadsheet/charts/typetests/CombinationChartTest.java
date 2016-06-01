package com.vaadin.spreadsheet.charts.typetests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.spreadsheet.charts.ChartTestBase;

public class CombinationChartTest extends ChartTestBase {
    @Test
    public void columnAndLineTest() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "TypeSample - Combination (Column + Line + Dual Axis).xlsx", "A6")
                .getConfiguration();

        assertCombinationChartSeriesType(conf);

        Assert.assertEquals("", conf.getxAxis().getTitle().getText());
        Assert.assertEquals("", conf.getyAxes().getAxes().get(0).getTitle()
                .getText());
        Assert.assertEquals("", conf.getyAxes().getAxes().get(1).getTitle()
                .getText());
    }

    private void assertCombinationChartSeriesType(Configuration conf) {
        Assert.assertEquals("Wrong series number", 5, conf.getSeries().size());
        assertSingleSeriesType(conf.getSeries().get(0), ChartType.COLUMN);
        assertSingleSeriesType(conf.getSeries().get(1), ChartType.COLUMN);
        assertSingleSeriesType(conf.getSeries().get(2), ChartType.COLUMN);
        assertSingleSeriesType(conf.getSeries().get(3), ChartType.LINE);
        assertSingleSeriesType(conf.getSeries().get(4), ChartType.LINE);
    }

    @Test
    public void columnAndLineWithAxisTitlesTest() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "TypeSample - Combination (Column + Line + Dual Axis).xlsx", "A24")
                .getConfiguration();

        assertCombinationChartSeriesType(conf);

        Assert.assertEquals("Title on the left",
                conf.getyAxes().getAxes().get(0).getTitle().getText());
        
        Assert.assertEquals("Title on the right", conf.getyAxes().getAxes()
                .get(1).getTitle().getText());
    }
}
