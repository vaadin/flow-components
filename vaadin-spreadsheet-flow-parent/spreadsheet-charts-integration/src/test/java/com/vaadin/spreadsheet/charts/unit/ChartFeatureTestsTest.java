package com.vaadin.spreadsheet.charts.unit;

import com.vaadin.addon.charts.model.Configuration;
import org.junit.Assert;
import org.junit.Test;

public class ChartFeatureTestsTest extends ChartBaseTest {

    @Test
    public void axisTitles_loadSampleB3_titlesAbsent() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "StyleSample - Axis Title Options.xlsx", "B3")
                .getConfiguration();

        Assert.assertEquals("", conf.getxAxis().getTitle().getText());
        Assert.assertEquals("", conf.getyAxis().getTitle().getText());
    }

    @Test
    public void axisTitles_loadSampleG3_titlesPresentAndCorrect()
            throws Exception {
        Configuration conf = getChartFromSampleFile(
                "StyleSample - Axis Title Options.xlsx", "G3")
                .getConfiguration();

        Assert.assertEquals("horizontal title", conf.getyAxis().getTitle()
                .getText());
        Assert.assertEquals("Title below axis", conf.getxAxis().getTitle()
                .getText());
    }

}
