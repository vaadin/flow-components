package com.vaadin.spreadsheet.charts.unit;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.spreadsheet.charts.unit.ChartBaseTest;

public class StyleTests extends ChartBaseTest {
    @Test
    public void axisTitles_loadSampleO4_axisTitleFont() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "StyleSample - Axis Title Options.xlsx", "O4")
                .getConfiguration();

        Assert.assertEquals("Title below axis", conf.getxAxis().getTitle()
                .getText());
        Assert.assertEquals("Apple Chancery", conf.getxAxis().getTitle()
                .getStyle().getFontFamily());
    }
}
