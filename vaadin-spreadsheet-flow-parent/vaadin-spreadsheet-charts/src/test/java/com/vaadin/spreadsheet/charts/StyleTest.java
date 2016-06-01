package com.vaadin.spreadsheet.charts;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.addon.charts.model.AbstractConfigurationObject;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.style.Color;
import com.vaadin.addon.charts.model.style.FontWeight;
import com.vaadin.addon.charts.model.style.GradientColor;
import com.vaadin.addon.charts.model.style.Style;
import com.vaadin.addon.charts.util.ChartSerialization;

public class StyleTest extends ChartTestBase {
    @Test
    public void axisTitles_loadSampleO4_axisTitleFont() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Axis Title Options.xlsx", "O4")
                .getConfiguration();

        Assert.assertEquals("Title below axis", conf.getxAxis().getTitle()
                .getText());
        Assert.assertEquals("Apple Chancery", conf.getxAxis().getTitle()
                .getStyle().getFontFamily());
    }

    @Test
    public void backgroundColor_loadSampleA2_greenBackgroundBlueBorder()
            throws Exception {
        Configuration conf = getChartFromSampleFile(
                "StyleSample - Background and Border.xlsx", "A2")
                .getConfiguration();

        Assert.assertEquals("rgba(112,173,71,1.00)", conf.getChart()
                .getBackgroundColor().toString());
        Assert.assertEquals("rgba(46,117,182,1.00)", conf.getChart()
                .getBorderColor().toString());
        Assert.assertEquals(2.25d, conf.getChart().getBorderWidth());
        Assert.assertEquals(8, conf.getChart().getBorderRadius());
    }

    @Test
    public void backgroundColor_loadSampleA17_brownBackgroundGreenBorder()
            throws Exception {
        Configuration conf = getChartFromSampleFile(
                "StyleSample - Background and Border.xlsx", "A17")
                .getConfiguration();

        Assert.assertEquals("rgba(197,90,17,0.30)", conf.getChart()
                .getBackgroundColor().toString());
        Assert.assertEquals("rgba(84,130,53,1.00)", conf.getChart()
                .getBorderColor().toString());
        Assert.assertEquals(0.5d, conf.getChart().getBorderWidth());
        Assert.assertEquals(0, conf.getChart().getBorderRadius());
    }

    @Test
    public void backgroundColor_loadSampleG17_contentIsGradient()
            throws Exception {

        Configuration conf = getChartFromSampleFile(
                "StyleSample - Background and Border.xlsx", "G17")
                .getConfiguration();

        Assert.assertEquals(GradientColor.class, conf.getChart()
                .getBackgroundColor().getClass());

        final String correctGradientJson = "{\"color\":"
                + "{\"stops\":"
                + "[[0.35,\"rgba(145,66,13,1.00)\"],[0.45,\"rgba(157,195,230,1.00)\"],"
                + "[0.51,\"rgba(169,209,142,0.38)\"]],"
                + "\"linearGradient\":{\"x1\":0.6710100716628344,"
                + "\"y1\":0.03015368960704584,\"x2\":0.32898992833716556,"
                + "\"y2\":0.9698463103929542}}}";

        final String actualGradientJson = colorToJson(conf.getChart()
                .getBackgroundColor());

        Assert.assertEquals(correctGradientJson, actualGradientJson);

        Assert.assertEquals("rgba(255,217,102,1.00)", conf.getChart()
                .getBorderColor().toString());
        Assert.assertEquals(2.25d, conf.getChart().getBorderWidth());
        Assert.assertEquals(0, conf.getChart().getBorderRadius());
    }

    @SuppressWarnings("serial")
    private String colorToJson(final Color c) {
        return ChartSerialization.toJSON(new AbstractConfigurationObject() {
            @SuppressWarnings("unused")
            Color color = c;
        });
    }

    @Test
    public void font_loadSampleC2_titleFontsAreCorrect()
            throws Exception {
        Configuration conf = getChartFromSampleFile(
                "StyleSample - Custom Font.xlsx", "C2").getConfiguration();

        String fontFamily = "Comic Sans MS";
        FontWeight fontWeight = FontWeight.BOLD;
        String textColor = "rgba(84,130,53,1.00)";
        String fontSize = "18.0pt";
        
        assertStyle(conf.getTitle().getStyle(), fontFamily, fontWeight, textColor, fontSize);
    }


    @Test
    public void font_loadSampleC2_legendFontsAreCorrect()
            throws Exception {
        Configuration conf = getChartFromSampleFile(
                "StyleSample - Custom Font.xlsx", "C2").getConfiguration();

        String fontFamily = "Times New Roman";
        FontWeight fontWeight = FontWeight.BOLD;
        String textColor = "rgba(89,89,89,1.00)";
        String fontSize = "24.0pt";
        
        assertStyle(conf.getLegend().getItemStyle(), fontFamily, fontWeight, textColor, fontSize);
    }


    @Test
    public void font_loadSampleC2_xAxisTitleFontsAreCorrect()
            throws Exception {
        Configuration conf = getChartFromSampleFile(
                "StyleSample - Custom Font.xlsx", "C2").getConfiguration();

        String fontFamily = "Constantia";
        FontWeight fontWeight = null;
        String textColor = "rgba(132,60,11,1.00)";
        String fontSize = "10.5pt";
        
        assertStyle(conf.getxAxis().getTitle().getStyle(), fontFamily, fontWeight, textColor, fontSize);
    }

    @Test
    public void font_loadSampleC2_yAxisTitleFontsAreCorrect()
            throws Exception {
        Configuration conf = getChartFromSampleFile(
                "StyleSample - Custom Font.xlsx", "C2").getConfiguration();

        String fontFamily = "Gabriola";
        FontWeight fontWeight = FontWeight.BOLD;
        String textColor = "rgba(89,89,89,1.00)";
        String fontSize = "17.5pt";
        
        assertStyle(conf.getyAxis().getTitle().getStyle(), fontFamily, fontWeight, textColor, fontSize);
    }

    private void assertStyle(Style style, String fontFamily,
            FontWeight fontWeight, String textColor, String fontSize) {
        Assert.assertEquals(fontFamily, style
                .getFontFamily());
        Assert.assertEquals(fontWeight, style
                .getFontWeight());
        Assert.assertEquals(fontSize, style.getFontSize());
        Assert.assertEquals(textColor, style
                .getColor().toString());
    }
}
