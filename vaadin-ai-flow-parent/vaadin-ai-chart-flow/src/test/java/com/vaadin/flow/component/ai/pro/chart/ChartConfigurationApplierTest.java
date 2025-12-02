/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.pro.chart;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.flow.internal.JacksonUtils;
import org.junit.Before;
import org.junit.Test;
import tools.jackson.databind.node.ObjectNode;

import static org.junit.Assert.*;

/**
 * Comprehensive tests for {@link ChartConfigurationApplier}.
 * <p>
 * Tests the full cycle: configure chart → serialize → apply to new chart → verify configuration.
 * </p>
 */
public class ChartConfigurationApplierTest {

    private ChartConfigurationApplier applier;

    @Before
    public void setUp() {
        applier = new ChartConfigurationApplier();
    }

    @Test
    public void testChartType_pie_preservedAfterApply() throws Exception {
        // Configure source chart
        Chart sourceChart = new Chart();
        Configuration config = sourceChart.getConfiguration();
        config.getChart().setType(ChartType.PIE);
        config.setTitle("Pie Chart Title");

        // Serialize and clean
        String json = serializeAndClean(sourceChart);

        // Apply to new chart
        Chart targetChart = new Chart();
        applier.applyConfiguration(targetChart, json);

        // Verify chart type is preserved
        assertEquals("Chart type should be PIE", ChartType.PIE,
                targetChart.getConfiguration().getChart().getType());
        assertEquals("Title should be preserved", "Pie Chart Title",
                targetChart.getConfiguration().getTitle().getText());
    }

    @Test
    public void testChartType_allTypes_preservedAfterApply() throws Exception {
        ChartType[] typesToTest = {
                ChartType.LINE, ChartType.SPLINE, ChartType.AREA, ChartType.AREASPLINE,
                ChartType.BAR, ChartType.COLUMN, ChartType.PIE, ChartType.SCATTER,
                ChartType.GAUGE, ChartType.BUBBLE, ChartType.HEATMAP, ChartType.TREEMAP,
                ChartType.AREARANGE, ChartType.AREASPLINERANGE, ChartType.COLUMNRANGE,
                ChartType.BOXPLOT, ChartType.ERRORBAR, ChartType.FUNNEL, ChartType.WATERFALL,
                ChartType.PYRAMID, ChartType.SOLIDGAUGE, ChartType.POLYGON, ChartType.CANDLESTICK,
                ChartType.FLAGS, ChartType.TIMELINE, ChartType.OHLC, ChartType.ORGANIZATION,
                ChartType.SANKEY, ChartType.XRANGE, ChartType.GANTT, ChartType.BULLET
        };

        for (ChartType type : typesToTest) {
            Chart sourceChart = new Chart();
            sourceChart.getConfiguration().getChart().setType(type);

            String json = serializeAndClean(sourceChart);

            Chart targetChart = new Chart();
            applier.applyConfiguration(targetChart, json);

            assertEquals("Chart type " + type + " should be preserved", type,
                    targetChart.getConfiguration().getChart().getType());
        }
    }

    @Test
    public void testTitle_preservedAfterApply() throws Exception {
        // Configure source chart with title
        Chart sourceChart = new Chart();
        Configuration config = sourceChart.getConfiguration();
        config.setTitle("Revenue Analysis");
        config.setSubTitle("Q4 2024");

        // Serialize and apply
        String json = serializeAndClean(sourceChart);
        Chart targetChart = new Chart();
        applier.applyConfiguration(targetChart, json);

        // Verify title is preserved
        assertEquals("Title should be preserved", "Revenue Analysis",
                targetChart.getConfiguration().getTitle().getText());
        assertEquals("Subtitle should be preserved", "Q4 2024",
                targetChart.getConfiguration().getSubTitle().getText());
    }

    @Test
    public void testTooltip_preservedAfterApply() throws Exception {
        // Configure tooltip
        Chart sourceChart = new Chart();
        Configuration config = sourceChart.getConfiguration();
        Tooltip tooltip = config.getTooltip();
        tooltip.setPointFormat("Value: {point.y}");
        tooltip.setHeaderFormat("<b>{point.key}</b><br/>");
        tooltip.setShared(true);
        tooltip.setValueSuffix(" USD");
        tooltip.setValuePrefix("$");

        // Serialize and apply
        String json = serializeAndClean(sourceChart);
        Chart targetChart = new Chart();
        applier.applyConfiguration(targetChart, json);

        // Verify tooltip is preserved
        Tooltip restoredTooltip = targetChart.getConfiguration().getTooltip();
        assertEquals("Point format should be preserved", "Value: {point.y}",
                restoredTooltip.getPointFormat());
        assertEquals("Header format should be preserved", "<b>{point.key}</b><br/>",
                restoredTooltip.getHeaderFormat());
        assertEquals("Shared should be preserved", Boolean.TRUE, restoredTooltip.getShared());
        assertEquals("Value suffix should be preserved", " USD",
                restoredTooltip.getValueSuffix());
        assertEquals("Value prefix should be preserved", "$",
                restoredTooltip.getValuePrefix());
    }

    @Test
    public void testLegend_preservedAfterApply() throws Exception {
        // Configure legend
        Chart sourceChart = new Chart();
        Configuration config = sourceChart.getConfiguration();
        Legend legend = config.getLegend();
        legend.setEnabled(false);
        legend.setAlign(HorizontalAlign.RIGHT);
        legend.setVerticalAlign(VerticalAlign.TOP);
        legend.setLayout(LayoutDirection.VERTICAL);

        // Serialize and apply
        String json = serializeAndClean(sourceChart);
        Chart targetChart = new Chart();
        applier.applyConfiguration(targetChart, json);

        // Verify legend is preserved
        Legend restoredLegend = targetChart.getConfiguration().getLegend();
        assertEquals("Legend enabled should be preserved", Boolean.FALSE, restoredLegend.getEnabled());
        assertEquals("Legend align should be preserved", HorizontalAlign.RIGHT,
                restoredLegend.getAlign());
        assertEquals("Legend vertical align should be preserved", VerticalAlign.TOP,
                restoredLegend.getVerticalAlign());
        assertEquals("Legend layout should be preserved", LayoutDirection.VERTICAL,
                restoredLegend.getLayout());
    }

    @Test
    public void testAxis_preservedAfterApply() throws Exception {
        // Configure axes
        Chart sourceChart = new Chart();
        Configuration config = sourceChart.getConfiguration();
        XAxis xAxis = config.getxAxis();
        xAxis.setTitle(new AxisTitle("Month"));
        xAxis.setCategories("Jan", "Feb", "Mar", "Apr", "May");
        xAxis.setMin(0.0);
        xAxis.setMax(4.0);

        YAxis yAxis = config.getyAxis();
        yAxis.setTitle(new AxisTitle("Revenue"));
        yAxis.setMin(0.0);
        yAxis.setMax(10000.0);

        // Serialize and apply
        String json = serializeAndClean(sourceChart);
        Chart targetChart = new Chart();
        applier.applyConfiguration(targetChart, json);

        // Verify X axis is preserved
        XAxis restoredXAxis = targetChart.getConfiguration().getxAxis();
        assertEquals("X axis title should be preserved", "Month",
                restoredXAxis.getTitle().getText());
        assertArrayEquals("X axis categories should be preserved",
                new String[]{"Jan", "Feb", "Mar", "Apr", "May"},
                restoredXAxis.getCategories());
        assertEquals("X axis min should be preserved", 0.0,
                restoredXAxis.getMin().doubleValue(), 0.001);
        assertEquals("X axis max should be preserved", 4.0,
                restoredXAxis.getMax().doubleValue(), 0.001);

        // Verify Y axis is preserved
        YAxis restoredYAxis = targetChart.getConfiguration().getyAxis();
        assertEquals("Y axis title should be preserved", "Revenue",
                restoredYAxis.getTitle().getText());
        assertEquals("Y axis min should be preserved", 0.0,
                restoredYAxis.getMin().doubleValue(), 0.001);
        assertEquals("Y axis max should be preserved", 10000.0,
                restoredYAxis.getMax().doubleValue(), 0.001);
    }

    @Test
    public void testChartModel_dimensions_preservedAfterApply() throws Exception {
        // Configure chart dimensions
        Chart sourceChart = new Chart();
        ChartModel chartModel = sourceChart.getConfiguration().getChart();
        chartModel.setWidth(800);
        chartModel.setHeight("600px");

        // Serialize and apply
        String json = serializeAndClean(sourceChart);
        Chart targetChart = new Chart();
        applier.applyConfiguration(targetChart, json);

        // Verify dimensions are preserved
        ChartModel restoredModel = targetChart.getConfiguration().getChart();
        assertEquals("Width should be preserved", 800, restoredModel.getWidth());
        assertEquals("Height should be preserved", "600px", restoredModel.getHeight());
    }

    @Test
    public void testChartModel_margins_preservedAfterApply() throws Exception {
        // Configure chart margins
        Chart sourceChart = new Chart();
        ChartModel chartModel = sourceChart.getConfiguration().getChart();
        chartModel.setMarginTop(50);
        chartModel.setMarginRight(60);
        chartModel.setMarginBottom(70);
        chartModel.setMarginLeft(80);

        // Serialize and apply
        String json = serializeAndClean(sourceChart);
        Chart targetChart = new Chart();
        applier.applyConfiguration(targetChart, json);

        // Verify margins are preserved
        ChartModel restoredModel = targetChart.getConfiguration().getChart();
        assertEquals("Margin top should be preserved", 50, restoredModel.getMarginTop());
        assertEquals("Margin right should be preserved", 60, restoredModel.getMarginRight());
        assertEquals("Margin bottom should be preserved", 70, restoredModel.getMarginBottom());
        assertEquals("Margin left should be preserved", 80, restoredModel.getMarginLeft());
    }

    @Test
    public void testChartModel_spacing_preservedAfterApply() throws Exception {
        // Configure chart spacing
        Chart sourceChart = new Chart();
        ChartModel chartModel = sourceChart.getConfiguration().getChart();
        chartModel.setSpacingTop(10);
        chartModel.setSpacingRight(15);
        chartModel.setSpacingBottom(20);
        chartModel.setSpacingLeft(25);

        // Serialize and apply
        String json = serializeAndClean(sourceChart);
        Chart targetChart = new Chart();
        applier.applyConfiguration(targetChart, json);

        // Verify spacing is preserved
        ChartModel restoredModel = targetChart.getConfiguration().getChart();
        assertEquals("Spacing top should be preserved", 10, restoredModel.getSpacingTop());
        assertEquals("Spacing right should be preserved", 15, restoredModel.getSpacingRight());
        assertEquals("Spacing bottom should be preserved", 20, restoredModel.getSpacingBottom());
        assertEquals("Spacing left should be preserved", 25, restoredModel.getSpacingLeft());
    }

    @Test
    public void testChartModel_backgroundAndBorders_preservedAfterApply() throws Exception {
        // Configure background and borders
        Chart sourceChart = new Chart();
        ChartModel chartModel = sourceChart.getConfiguration().getChart();
        chartModel.setBackgroundColor(new SolidColor("#f0f0f0"));
        chartModel.setBorderColor(new SolidColor("#333333"));
        chartModel.setBorderWidth(2);
        chartModel.setBorderRadius(10);

        // Serialize and apply
        String json = serializeAndClean(sourceChart);
        Chart targetChart = new Chart();
        applier.applyConfiguration(targetChart, json);

        // Verify background and borders are preserved
        ChartModel restoredModel = targetChart.getConfiguration().getChart();
        assertEquals("Background color should be preserved", "#f0f0f0",
                ((SolidColor) restoredModel.getBackgroundColor()).toString());
        assertEquals("Border color should be preserved", "#333333",
                ((SolidColor) restoredModel.getBorderColor()).toString());
        assertEquals("Border width should be preserved", 2, restoredModel.getBorderWidth());
        assertEquals("Border radius should be preserved", 10, restoredModel.getBorderRadius());
    }

    @Test
    public void testChartModel_plotArea_preservedAfterApply() throws Exception {
        // Configure plot area
        Chart sourceChart = new Chart();
        ChartModel chartModel = sourceChart.getConfiguration().getChart();
        chartModel.setPlotBackgroundColor(new SolidColor("#ffffff"));
        chartModel.setPlotBorderColor(new SolidColor("#cccccc"));
        chartModel.setPlotBorderWidth(1);

        // Serialize and apply
        String json = serializeAndClean(sourceChart);
        Chart targetChart = new Chart();
        applier.applyConfiguration(targetChart, json);

        // Verify plot area is preserved
        ChartModel restoredModel = targetChart.getConfiguration().getChart();
        assertEquals("Plot background color should be preserved", "#ffffff",
                ((SolidColor) restoredModel.getPlotBackgroundColor()).toString());
        assertEquals("Plot border color should be preserved", "#cccccc",
                ((SolidColor) restoredModel.getPlotBorderColor()).toString());
        assertEquals("Plot border width should be preserved", 1,
                restoredModel.getPlotBorderWidth());
    }

    @Test
    public void testChartModel_booleanOptions_preservedAfterApply() throws Exception {
        // Configure boolean options
        Chart sourceChart = new Chart();
        ChartModel chartModel = sourceChart.getConfiguration().getChart();
        chartModel.setInverted(true);
        chartModel.setPolar(true);
        chartModel.setAnimation(false);
        chartModel.setStyledMode(true);

        // Serialize and apply
        String json = serializeAndClean(sourceChart);
        Chart targetChart = new Chart();
        applier.applyConfiguration(targetChart, json);

        // Verify boolean options are preserved
        ChartModel restoredModel = targetChart.getConfiguration().getChart();
        assertEquals("Inverted should be preserved", Boolean.TRUE, restoredModel.getInverted());
        assertEquals("Polar should be preserved", Boolean.TRUE, restoredModel.getPolar());
        assertEquals("Animation should be preserved", Boolean.FALSE, restoredModel.getAnimation());
        assertEquals("Styled mode should be preserved", Boolean.TRUE, restoredModel.getStyledMode());
    }

    @Test
    public void testCredits_preservedAfterApply() throws Exception {
        // Configure credits
        Chart sourceChart = new Chart();
        Configuration config = sourceChart.getConfiguration();
        Credits credits = config.getCredits();
        credits.setEnabled(false);
        credits.setText("Custom Credits");
        credits.setHref("https://example.com");

        // Serialize and apply
        String json = serializeAndClean(sourceChart);
        Chart targetChart = new Chart();
        applier.applyConfiguration(targetChart, json);

        // Verify credits are preserved
        Credits restoredCredits = targetChart.getConfiguration().getCredits();
        assertEquals("Credits enabled should be preserved", Boolean.FALSE, restoredCredits.getEnabled());
        assertEquals("Credits text should be preserved", "Custom Credits",
                restoredCredits.getText());
        assertEquals("Credits href should be preserved", "https://example.com",
                restoredCredits.getHref());
    }

    @Test
    public void testColorAxis_preservedAfterApply() throws Exception {
        // Configure color axis (for heatmaps)
        Chart sourceChart = new Chart();
        Configuration config = sourceChart.getConfiguration();
        ColorAxis colorAxis = config.getColorAxis();
        colorAxis.setMin(0.0);
        colorAxis.setMax(100.0);
        colorAxis.setMinColor(new SolidColor("#3060cf"));
        colorAxis.setMaxColor(new SolidColor("#c4463a"));

        // Serialize and apply
        String json = serializeAndClean(sourceChart);
        Chart targetChart = new Chart();
        applier.applyConfiguration(targetChart, json);

        // Verify color axis is preserved
        ColorAxis restoredColorAxis = targetChart.getConfiguration().getColorAxis();
        assertEquals("Color axis min should be preserved", 0.0,
                restoredColorAxis.getMin().doubleValue(), 0.001);
        assertEquals("Color axis max should be preserved", 100.0,
                restoredColorAxis.getMax().doubleValue(), 0.001);
        assertEquals("Color axis min color should be preserved", "#3060cf",
                ((SolidColor) restoredColorAxis.getMinColor()).toString());
        assertEquals("Color axis max color should be preserved", "#c4463a",
                ((SolidColor) restoredColorAxis.getMaxColor()).toString());
    }

    @Test
    public void testPane_preservedAfterApply() throws Exception {
        // Configure pane (for gauges and polar charts)
        Chart sourceChart = new Chart();
        Configuration config = sourceChart.getConfiguration();
        Pane pane = new Pane();
        pane.setStartAngle(-90);
        pane.setEndAngle(90);
        pane.setCenter(new String[]{"50%", "75%"});
        pane.setSize("110%");
        config.addPane(pane);

        // Serialize and apply
        String json = serializeAndClean(sourceChart);
        Chart targetChart = new Chart();
        applier.applyConfiguration(targetChart, json);

        // Verify pane is preserved
        Configuration restoredConfig = targetChart.getConfiguration();
        assertTrue("Pane should be present", restoredConfig.getPane() != null);
        Pane restoredPane = restoredConfig.getPane();
        assertEquals("Pane start angle should be preserved", -90,
                restoredPane.getStartAngle().intValue());
        assertEquals("Pane end angle should be preserved", 90,
                restoredPane.getEndAngle().intValue());
        assertArrayEquals("Pane center should be preserved",
                new String[]{"50%", "75%"}, restoredPane.getCenter());
        assertEquals("Pane size should be preserved", "110%", restoredPane.getSize());
    }

    @Test
    public void testExporting_preservedAfterApply() throws Exception {
        // Configure exporting
        Chart sourceChart = new Chart();
        Configuration config = sourceChart.getConfiguration();
        Exporting exporting = config.getExporting();
        exporting.setEnabled(true);
        exporting.setFilename("my-chart");
        exporting.setSourceWidth(1200);
        exporting.setSourceHeight(800);
        exporting.setScale(2);

        // Serialize and apply
        String json = serializeAndClean(sourceChart);
        Chart targetChart = new Chart();
        applier.applyConfiguration(targetChart, json);

        // Verify exporting is preserved
        Exporting restoredExporting = targetChart.getConfiguration().getExporting();
        assertEquals("Exporting enabled should be preserved", Boolean.TRUE, restoredExporting.getEnabled());
        assertEquals("Exporting filename should be preserved", "my-chart",
                restoredExporting.getFilename());
        assertEquals("Exporting source width should be preserved", 1200,
                restoredExporting.getSourceWidth().intValue());
        assertEquals("Exporting source height should be preserved", 800,
                restoredExporting.getSourceHeight().intValue());
        assertEquals("Exporting scale should be preserved", 2,
                restoredExporting.getScale().intValue());
    }

    @Test
    public void testComplexConfiguration_allAspects_preservedAfterApply() throws Exception {
        // Configure multiple aspects at once
        Chart sourceChart = new Chart();
        Configuration config = sourceChart.getConfiguration();

        // Chart type and title
        config.getChart().setType(ChartType.COLUMN);
        config.setTitle("Sales Report");
        config.setSubTitle("Year 2024");

        // Axes
        config.getxAxis().setTitle(new AxisTitle("Quarter"));
        config.getxAxis().setCategories("Q1", "Q2", "Q3", "Q4");
        config.getyAxis().setTitle(new AxisTitle("Sales ($)"));
        config.getyAxis().setMin(0.0);

        // Tooltip
        config.getTooltip().setValueSuffix(" USD");
        config.getTooltip().setShared(true);

        // Legend
        config.getLegend().setAlign(HorizontalAlign.CENTER);
        config.getLegend().setVerticalAlign(VerticalAlign.BOTTOM);

        // Chart model
        config.getChart().setHeight("400px");
        config.getChart().setMarginTop(30);
        config.getChart().setBackgroundColor(new SolidColor("#f9f9f9"));

        // Serialize and apply
        String json = serializeAndClean(sourceChart);
        Chart targetChart = new Chart();
        applier.applyConfiguration(targetChart, json);

        // Verify all aspects are preserved
        Configuration restoredConfig = targetChart.getConfiguration();

        assertEquals("Chart type should be preserved", ChartType.COLUMN,
                restoredConfig.getChart().getType());
        assertEquals("Title should be preserved", "Sales Report",
                restoredConfig.getTitle().getText());
        assertEquals("Subtitle should be preserved", "Year 2024",
                restoredConfig.getSubTitle().getText());
        assertEquals("X axis title should be preserved", "Quarter",
                restoredConfig.getxAxis().getTitle().getText());
        assertEquals("Y axis title should be preserved", "Sales ($)",
                restoredConfig.getyAxis().getTitle().getText());
        assertEquals("Tooltip value suffix should be preserved", " USD",
                restoredConfig.getTooltip().getValueSuffix());
        assertEquals("Legend align should be preserved", HorizontalAlign.CENTER,
                restoredConfig.getLegend().getAlign());
        assertEquals("Chart height should be preserved", "400px",
                restoredConfig.getChart().getHeight());
    }

    @Test
    public void testSeriesNotIncludedInJSON() throws Exception {
        // Add series data to source chart
        Chart sourceChart = new Chart();
        Configuration config = sourceChart.getConfiguration();
        DataSeries series = new DataSeries("Test Series");
        series.add(new DataSeriesItem("Point 1", 100));
        series.add(new DataSeriesItem("Point 2", 200));
        config.setSeries(series);

        // Serialize
        String json = serializeAndClean(sourceChart);

        // Verify series is not in the JSON
        assertFalse("Configuration should not contain 'series'",
                json.contains("\"series\""));
    }

    // Helper methods

    /**
     * Serializes a chart configuration and removes the series field,
     * mimicking what ChartAiController does.
     */
    private String serializeAndClean(Chart chart) throws Exception {
        String json = ChartSerialization.toJSON(chart.getConfiguration());
        ObjectNode node = (ObjectNode) JacksonUtils.readTree(json);
        node.remove("series");
        return node.toString();
    }
}
