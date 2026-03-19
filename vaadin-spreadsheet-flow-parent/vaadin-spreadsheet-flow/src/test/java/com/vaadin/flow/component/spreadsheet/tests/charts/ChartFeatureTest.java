/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.charts;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.HorizontalAlign;
import com.vaadin.flow.component.charts.model.LayoutDirection;
import com.vaadin.flow.component.charts.model.VerticalAlign;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;

class ChartFeatureTest extends ChartTestBase {

    @Test
    void axisTitles_loadSampleB3_titlesAbsent() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Axis Title Options.xlsx", "B3")
                .getConfiguration();

        Assertions.assertEquals("", conf.getxAxis().getTitle().getText());
        Assertions.assertEquals("", conf.getyAxis().getTitle().getText());
    }

    @Test
    void axisTitles_loadSampleG3_titlesPresentAndCorrect() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Axis Title Options.xlsx", "G3")
                .getConfiguration();

        Assertions.assertEquals("horizontal title",
                conf.getyAxis().getTitle().getText());
        Assertions.assertEquals("Title below axis",
                conf.getxAxis().getTitle().getText());
    }

    @Test
    void chartTitle_loadSampleJ13_titlesEqualsCellValue() throws Exception {

        String fileName = "Tagetik 6.xlsx";
        Configuration conf = getChartFromSampleFile(fileName, "J13")
                .getConfiguration();
        Spreadsheet spreadsheet = TestHelper.createSpreadsheet(fileName);

        Assertions.assertEquals(spreadsheet.getCell("B14").getStringCellValue(),
                conf.getTitle().getText());
    }

    @Test
    void chartTitle_loadSampleA3_titlesAbsent() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Custom Title Position.xlsx", "A3")
                .getConfiguration();

        Assertions.assertEquals("", conf.getTitle().getText());
    }

    @Test
    void chartTitle_loadSampleE3_titlesPresentAbove() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Custom Title Position.xlsx", "E3")
                .getConfiguration();

        Assertions.assertEquals("Title above", conf.getTitle().getText());
        Assertions.assertEquals(Boolean.FALSE, conf.getTitle().getFloating());
    }

    @Test
    void chartTitle_loadSampleI3_titlesPresentFloating() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Custom Title Position.xlsx", "I3")
                .getConfiguration();

        Assertions.assertEquals("Title overlay", conf.getTitle().getText());
        Assertions.assertEquals(Boolean.TRUE, conf.getTitle().getFloating());
    }

    @Test
    void chartLegend_loadSampleA7_legendAbsent() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Legend Position.xlsx", "A7")
                .getConfiguration();

        Assertions.assertEquals(Boolean.FALSE, conf.getLegend().getEnabled());
    }

    @Test
    void chartLegend_loadSampleI7_legendOnTop() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Legend Position.xlsx", "I7")
                .getConfiguration();

        Assertions.assertEquals(VerticalAlign.TOP,
                conf.getLegend().getVerticalAlign());
        Assertions.assertEquals(HorizontalAlign.CENTER,
                conf.getLegend().getAlign());
        Assertions.assertEquals(LayoutDirection.HORIZONTAL,
                conf.getLegend().getLayout());
    }

    @Test
    void chartLegend_loadSampleR7_legendOnLeft() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Legend Position.xlsx", "R7")
                .getConfiguration();

        Assertions.assertEquals(VerticalAlign.MIDDLE,
                conf.getLegend().getVerticalAlign());
        Assertions.assertEquals(HorizontalAlign.LEFT,
                conf.getLegend().getAlign());
        Assertions.assertEquals(LayoutDirection.VERTICAL,
                conf.getLegend().getLayout());
    }

    @Test
    void chartLegend_loadSampleA25_legendOnTopRight() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Legend Position.xlsx", "A25")
                .getConfiguration();

        Assertions.assertEquals(VerticalAlign.TOP,
                conf.getLegend().getVerticalAlign());
        Assertions.assertEquals(HorizontalAlign.RIGHT,
                conf.getLegend().getAlign());
        Assertions.assertEquals(LayoutDirection.VERTICAL,
                conf.getLegend().getLayout());
    }

    @Test
    void chartLegend_loadSampleI25_legendOnBottom() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Legend Position.xlsx", "I25")
                .getConfiguration();

        Assertions.assertEquals(VerticalAlign.BOTTOM,
                conf.getLegend().getVerticalAlign());
        Assertions.assertEquals(HorizontalAlign.CENTER,
                conf.getLegend().getAlign());
        Assertions.assertEquals(LayoutDirection.HORIZONTAL,
                conf.getLegend().getLayout());
    }

    @Test
    void chartLegend_loadSampleR25_legendOnRight() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Legend Position.xlsx", "R25")
                .getConfiguration();

        Assertions.assertEquals(VerticalAlign.MIDDLE,
                conf.getLegend().getVerticalAlign());
        Assertions.assertEquals(HorizontalAlign.RIGHT,
                conf.getLegend().getAlign());
        Assertions.assertEquals(LayoutDirection.VERTICAL,
                conf.getLegend().getLayout());
    }

    /**
     * In Vaadin charts if both title and legend are aligned to top, the legend
     * overlaps the title. This test checks if a y-offset is set, a possible
     * workaround for this issue.
     */
    @Test
    void chartLegend_loadSampleI43_legendOnTopAndYOffsetIsSet()
            throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Legend Position.xlsx", "I43")
                .getConfiguration();

        Assertions.assertEquals(VerticalAlign.TOP,
                conf.getLegend().getVerticalAlign());
        Assertions.assertEquals(HorizontalAlign.CENTER,
                conf.getLegend().getAlign());
        Assertions.assertEquals(LayoutDirection.HORIZONTAL,
                conf.getLegend().getLayout());

        Assertions.assertTrue(conf.getLegend().getY() != null,
                "Vertical offset for legend is not set, overlapping might occur");
    }

    private static final Double[] ZEROS = { 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d,
            0d };

    private static final Double[] NULLS = { null, null, null, null, null, null,
            null, null, null, null };

    private static final Double[][] blanksToZerosData = { ZEROS, ZEROS, ZEROS,
            ZEROS, ZEROS, ZEROS,
            { 10d, 0d, 43d, 16d, 0d, 8d, 0d, 0d, 35d, 78d } };

    private static final Double[][] blanksToNullsData = { NULLS, NULLS, NULLS,
            NULLS, NULLS, NULLS,
            { 10d, null, 43d, 16d, null, 8d, null, null, 35d, 78d } };

    @Test
    void blanksAsZeros_loadSampleB14_blanksTreatedAsZeros() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Blanks as zeros.xlsm", "B14")
                .getConfiguration();

        assertData(conf.getSeries(), blanksToZerosData);
    }

    @Test
    void blanksAsZeros_loadSampleB29_blanksTreatedAsNulls() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Blanks as zeros.xlsm", "B29")
                .getConfiguration();

        assertData(conf.getSeries(), blanksToNullsData);
    }

    @Test
    void blanksAsZeros_loadSampleK14_blanksTreatedAsZeros() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Blanks as zeros.xlsm", "K14")
                .getConfiguration();

        assertData(conf.getSeries(), blanksToZerosData);
    }

    @Test
    void blanksAsZeros_loadSampleK29_blanksTreatedAsNulls() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "FeatureSample - Blanks as zeros.xlsm", "K29")
                .getConfiguration();

        assertData(conf.getSeries(), blanksToNullsData);
    }

    @Test
    void dualAxis_loadSampleA6_hasTwoAxesAndAssignedCorrectlyToSeries()
            throws Exception {
        Configuration conf = getChartFromSampleFile(
                "TypeSample - Combination (Column + Line + Dual Axis).xlsx",
                "A6").getConfiguration();

        Assertions.assertEquals(2, conf.getyAxes().getNumberOfAxes());

        Assertions.assertEquals(new Integer(0),
                ((DataSeries) conf.getSeries().get(0)).getyAxis());
        Assertions.assertEquals(new Integer(0),
                ((DataSeries) conf.getSeries().get(1)).getyAxis());
        Assertions.assertEquals(new Integer(0),
                ((DataSeries) conf.getSeries().get(2)).getyAxis());
        Assertions.assertEquals(new Integer(1),
                ((DataSeries) conf.getSeries().get(3)).getyAxis());
        Assertions.assertEquals(new Integer(1),
                ((DataSeries) conf.getSeries().get(4)).getyAxis());
    }

    @Test
    void categories_loadSampleE1_axisTypeCategory() throws Exception {
        Configuration conf = getChartFromSampleFile("numeric-categories.xlsx",
                "E1").getConfiguration();

        Assertions.assertEquals(AxisType.CATEGORY, conf.getxAxis().getType());
    }

    @Test
    void categories_loadSampleE1_axisTypeCategoryExplicitYAxisBounds()
            throws Exception {
        Configuration conf = getChartFromSampleFile(
                "numeric-categories-Explicit-Y-Axis-Bounds.xlsx", "E1")
                .getConfiguration();

        // min still set to auto scaling
        Assertions.assertNull(conf.getyAxis().getMin());
        // max set to an explicit value
        Assertions.assertNotNull(conf.getyAxis().getMax());
        Assertions.assertEquals(100d, conf.getyAxis().getMax().doubleValue(),
                0.0);
    }

    @Test
    void categories_loadSampleE1_categorySetAsPointName() throws Exception {
        Configuration conf = getChartFromSampleFile("numeric-categories.xlsx",
                "E1").getConfiguration();

        Assertions.assertEquals("2",
                ((DataSeries) conf.getSeries().get(0)).get(0).getName());
        Assertions.assertEquals("4",
                ((DataSeries) conf.getSeries().get(0)).get(1).getName());
        Assertions.assertEquals("8",
                ((DataSeries) conf.getSeries().get(0)).get(2).getName());
        Assertions.assertEquals("16",
                ((DataSeries) conf.getSeries().get(0)).get(3).getName());
        Assertions.assertEquals("32",
                ((DataSeries) conf.getSeries().get(0)).get(4).getName());
    }

    @Test
    void chartAndDataSeriesOnDifferentSheets_loadSample_chartHasSeries()
            throws Exception {
        Configuration conf = getChartFromSampleFile(
                "Chart_and_data_on_different_sheets.xlsx", "D5")
                .getConfiguration();
        final Double[] dataSeries = { 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d };
        assertData(conf.getSeries(), dataSeries);
    }

    @Test
    void multiLevelCategoryWithNoCache_loadSample_chartRendered()
            throws Exception {
        Configuration conf = getChartFromSampleFile(
                "MultilevelCategoriesWithNoCachedData.xlsm", "L2")
                .getConfiguration();
        Assertions.assertEquals(AxisType.CATEGORY, conf.getxAxis().getType());
    }
}
