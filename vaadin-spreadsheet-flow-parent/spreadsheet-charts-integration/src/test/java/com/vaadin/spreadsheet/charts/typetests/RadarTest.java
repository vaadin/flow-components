package com.vaadin.spreadsheet.charts.typetests;

import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.spreadsheet.charts.ChartTestBase;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class RadarTest extends ChartTestBase {

    private Number[][]chartData={
            {0, 0, 0, 0, 0, 0, 0, 1500,  5000, 8500,  3500,  500 },
            {2500, 5500, 9000, 6500, 3500, 0, 0, 0, 0, 0, 0, 0 },
            {500, 750, 1500, 2000, 5500, 7500, 8500, 7000, 3500, 2500, 500, 100 },
            {0,1500, 2500, 4000, 3500, 1500, 800, 550, 2500, 6000, 5500, 3000 }

    };
    @Test
    public void notFilledRadar() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "Type Sample - Radar.xlsx", "G14").getConfiguration();
        assertSeriesType(conf.getSeries(), ChartType.LINE);
        assertData(conf.getSeries(), chartData);
        Assert.assertTrue(conf.getChart().getPolar());
    }

    //This feature is not supported
    @Test
    @Ignore
    public void filledRadar() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "TypeSample - Filled Radar.xlsx", "F1").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.AREA);
        Assert.assertTrue(conf.getChart().getPolar());
    }
}
