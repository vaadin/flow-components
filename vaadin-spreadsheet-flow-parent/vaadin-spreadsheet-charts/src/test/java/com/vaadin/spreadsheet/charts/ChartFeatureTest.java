package com.vaadin.spreadsheet.charts;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.addon.charts.model.AxisType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.HorizontalAlign;
import com.vaadin.addon.charts.model.LayoutDirection;
import com.vaadin.addon.charts.model.VerticalAlign;
import com.vaadin.addon.spreadsheet.Spreadsheet;

public class ChartFeatureTest extends ChartTestBase {

    @Test
    public void axisTitles_loadSampleB3_titlesAbsent() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Axis Title Options.xlsx", "B3")
                .getConfiguration();

        Assert.assertEquals("", conf.getxAxis().getTitle().getText());
        Assert.assertEquals("", conf.getyAxis().getTitle().getText());
    }

    @Test
    public void axisTitles_loadSampleG3_titlesPresentAndCorrect()
            throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Axis Title Options.xlsx", "G3")
                .getConfiguration();

        Assert.assertEquals("horizontal title", conf.getyAxis().getTitle()
                .getText());
        Assert.assertEquals("Title below axis", conf.getxAxis().getTitle()
                .getText());
    }

    @Test
    public void chartTitle_loadSampleJ13_titlesEqualsCellValue()
            throws Exception {
        String fileName = "Tagetik 6.xlsx";
        Configuration conf = getChartFromSampleFile(fileName, "J13")
                .getConfiguration();
        Spreadsheet spreadsheet = new Spreadsheet(getSampleFile(fileName));

        Assert.assertEquals(spreadsheet.getCell("B14").getStringCellValue(),
                conf.getTitle().getText());
    }

    @Test
    public void chartTitle_loadSampleA3_titlesAbsent() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Custom Title Position.xlsx", "A3")
                .getConfiguration();

        Assert.assertEquals("", conf.getTitle().getText());
    }

    @Test
    public void chartTitle_loadSampleE3_titlesPresentAbove() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Custom Title Position.xlsx", "E3")
                .getConfiguration();

        Assert.assertEquals("Title above", conf.getTitle().getText());
        Assert.assertEquals(Boolean.FALSE, conf.getTitle().getFloating());
    }

    @Test
    public void chartTitle_loadSampleI3_titlesPresentFloating()
            throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Custom Title Position.xlsx", "I3")
                .getConfiguration();

        Assert.assertEquals("Title overlay", conf.getTitle().getText());
        Assert.assertEquals(Boolean.TRUE, conf.getTitle().getFloating());
    }

    @Test
    public void chartLegend_loadSampleA7_legendAbsent() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Legend Position.xlsx", "A7")
                .getConfiguration();

        Assert.assertEquals(Boolean.FALSE, conf.getLegend().getEnabled());
    }

    @Test
    public void chartLegend_loadSampleI7_legendOnTop() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Legend Position.xlsx", "I7")
                .getConfiguration();

        Assert.assertEquals(VerticalAlign.TOP, conf.getLegend()
                .getVerticalAlign());
        Assert.assertEquals(HorizontalAlign.CENTER, conf.getLegend().getAlign());
        Assert.assertEquals(LayoutDirection.HORIZONTAL, conf.getLegend()
                .getLayout());
    }

    @Test
    public void chartLegend_loadSampleR7_legendOnLeft() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Legend Position.xlsx", "R7")
                .getConfiguration();

        Assert.assertEquals(VerticalAlign.MIDDLE, conf.getLegend()
                .getVerticalAlign());
        Assert.assertEquals(HorizontalAlign.LEFT, conf.getLegend().getAlign());
        Assert.assertEquals(LayoutDirection.VERTICAL, conf.getLegend()
                .getLayout());
    }

    @Test
    public void chartLegend_loadSampleA25_legendOnTopRight() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Legend Position.xlsx", "A25")
                .getConfiguration();

        Assert.assertEquals(VerticalAlign.TOP, conf.getLegend()
                .getVerticalAlign());
        Assert.assertEquals(HorizontalAlign.RIGHT, conf.getLegend().getAlign());
        Assert.assertEquals(LayoutDirection.VERTICAL, conf.getLegend()
                .getLayout());
    }

    @Test
    public void chartLegend_loadSampleI25_legendOnBottom() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Legend Position.xlsx", "I25")
                .getConfiguration();

        Assert.assertEquals(VerticalAlign.BOTTOM, conf.getLegend()
                .getVerticalAlign());
        Assert.assertEquals(HorizontalAlign.CENTER, conf.getLegend().getAlign());
        Assert.assertEquals(LayoutDirection.HORIZONTAL, conf.getLegend()
                .getLayout());
    }

    @Test
    public void chartLegend_loadSampleR25_legendOnRight() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Legend Position.xlsx", "R25")
                .getConfiguration();

        Assert.assertEquals(VerticalAlign.MIDDLE, conf.getLegend()
                .getVerticalAlign());
        Assert.assertEquals(HorizontalAlign.RIGHT, conf.getLegend().getAlign());
        Assert.assertEquals(LayoutDirection.VERTICAL, conf.getLegend()
                .getLayout());
    }

    /**
     * In Vaadin charts if both title and legend are aligned to top, the legend
     * overlaps the title. This test checks if a y-offset is set, a possible
     * workaround for this issue.
     */
    @Test
    public void chartLegend_loadSampleI43_legendOnTopAndYOffsetIsSet()
            throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Legend Position.xlsx", "I43")
                .getConfiguration();

        Assert.assertEquals(VerticalAlign.TOP, conf.getLegend()
                .getVerticalAlign());
        Assert.assertEquals(HorizontalAlign.CENTER, conf.getLegend().getAlign());
        Assert.assertEquals(LayoutDirection.HORIZONTAL, conf.getLegend()
                .getLayout());

        Assert.assertTrue(
                "Vertical offset for legend is not set, overlapping might occur",
                conf.getLegend().getY() != null);
    }

    private static final Double[] ZEROS = { 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d,
            0d };

    private static final Double[] NULLS = { null, null, null, null, null, null, null, null, null, null };

    private static final Double[][] blanksToZerosData = { ZEROS, ZEROS, ZEROS, ZEROS, ZEROS, ZEROS,
            { 10d, 0d, 43d, 16d, 0d, 8d, 0d, 0d, 35d, 78d } };

    private static final Double[][] blanksToNullsData = { NULLS, NULLS, NULLS, NULLS, NULLS, NULLS,
            { 10d, null, 43d, 16d, null, 8d, null, null, 35d, 78d } };

    @Test
    public void blanksAsZeros_loadSampleB14_blanksTreatedAsZeros()
            throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Blanks as zeros.xlsm", "B14")
                .getConfiguration();

        assertData(conf.getSeries(), blanksToZerosData);
    }

    @Test
    public void blanksAsZeros_loadSampleB29_blanksTreatedAsNulls()
            throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Blanks as zeros.xlsm", "B29")
                .getConfiguration();

        assertData(conf.getSeries(), blanksToNullsData);
    }

    @Test
    public void blanksAsZeros_loadSampleK14_blanksTreatedAsZeros()
            throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Blanks as zeros.xlsm", "K14")
                .getConfiguration();

        assertData(conf.getSeries(), blanksToZerosData);
    }

    @Test
    public void blanksAsZeros_loadSampleK29_blanksTreatedAsNulls()
            throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Blanks as zeros.xlsm", "K29")
                .getConfiguration();

        assertData(conf.getSeries(), blanksToNullsData);
    }

    @Test
    public void dualAxis_loadSampleA6_hasTwoAxesAndAssignedCorrectlyToSeries()
            throws Exception {
        Configuration conf = getChartFromSampleFile(
                "TypeSample - Combination (Column + Line + Dual Axis).xlsx", "A6")
                .getConfiguration();

        Assert.assertEquals(2, conf.getyAxes().getNumberOfAxes());
        
        Assert.assertEquals(new Integer(0), ((DataSeries)conf.getSeries().get(0)).getyAxis());
        Assert.assertEquals(new Integer(0), ((DataSeries)conf.getSeries().get(1)).getyAxis());
        Assert.assertEquals(new Integer(0), ((DataSeries)conf.getSeries().get(2)).getyAxis());
        Assert.assertEquals(new Integer(1), ((DataSeries)conf.getSeries().get(3)).getyAxis());
        Assert.assertEquals(new Integer(1), ((DataSeries)conf.getSeries().get(4)).getyAxis());
    }

    @Test
    public void categories_loadSampleE1_axisTypeCategory()
            throws Exception {
        Configuration conf = getChartFromSampleFile("numeric-categories.xlsx",
                "E1").getConfiguration();

        Assert.assertEquals(AxisType.CATEGORY, conf.getxAxis().getType());
    }

    @Test
    public void categories_loadSampleE1_axisTypeCategoryExplicitYAxisBounds()
            throws Exception {
        Configuration conf = getChartFromSampleFile("numeric-categories-Explicit-Y-Axis-Bounds.xlsx",
                "E1").getConfiguration();

        // min still set to auto scaling
        Assert.assertNull(conf.getyAxis().getMin());
        // max set to an explicit value
        Assert.assertNotNull(conf.getyAxis().getMax());
        Assert.assertEquals(100d, conf.getyAxis().getMax().doubleValue(), 0.0);
    }

    @Test
    public void categories_loadSampleE1_categorySetAsPointName()
            throws Exception {
        Configuration conf = getChartFromSampleFile("numeric-categories.xlsx",
                "E1").getConfiguration();

        Assert.assertEquals("2",
                ((DataSeries) conf.getSeries().get(0)).get(0).getName());
        Assert.assertEquals("4",
                ((DataSeries) conf.getSeries().get(0)).get(1).getName());
        Assert.assertEquals("8",
                ((DataSeries) conf.getSeries().get(0)).get(2).getName());
        Assert.assertEquals("16",
                ((DataSeries) conf.getSeries().get(0)).get(3).getName());
        Assert.assertEquals("32",
                ((DataSeries) conf.getSeries().get(0)).get(4).getName());
    }

    @Test
    public void chartAndDataSeriesOnDifferentSheets_loadSample_chartHasSeries()
            throws Exception {
        Configuration conf = getChartFromSampleFile("Chart_and_data_on_different_sheets.xlsx", "D5")
                .getConfiguration();
        final Double[] dataSeries = { 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d };
        assertData(conf.getSeries(), dataSeries);
    }
}
