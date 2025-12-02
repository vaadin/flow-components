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
import com.vaadin.flow.internal.JacksonUtils;
import tools.jackson.databind.node.ObjectNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for applying JSON configuration to Vaadin Chart objects.
 * <p>
 * This class provides methods to parse JSON configuration and apply it to
 * chart components by setting individual properties via the Java API. Since
 * Vaadin Charts doesn't support direct JSON deserialization, this class
 * manually extracts fields from JSON and applies them using setter methods.
 * </p>
 *
 * @author Vaadin Ltd
 */
class ChartConfigurationApplier implements Serializable {

    /**
     * Applies JSON configuration to a chart.
     * <p>
     * Supports configuration for: chart model (type, background, borders, margins, spacing, etc),
     * title, subtitle, tooltip, legend, axes (x, y, z, color), credits, pane, and exporting.
     * </p>
     *
     * @param chart the chart to configure
     * @param configJson JSON string containing configuration
     */
    void applyConfiguration(Chart chart, String configJson) {
        try {
            ObjectNode configNode = (ObjectNode) JacksonUtils
                    .readTree(configJson);

            Configuration config = chart.getConfiguration();

            // Apply chart type if specified
            if (configNode.has("type")) {
                applyChartType(config, configNode.get("type").asString());
            }

            // Apply chart model options
            if (configNode.has("chart") && configNode.get("chart").isObject()) {
                applyChartModelConfig(config.getChart(), configNode.get("chart"));
            }

            // Apply title if specified
            if (configNode.has("title")) {
                applyTitleConfig(config, configNode.get("title"));
            }

            // Apply subtitle if specified
            if (configNode.has("subtitle")) {
                applySubtitleConfig(config, configNode.get("subtitle"));
            }

            // Apply tooltip configuration
            if (configNode.has("tooltip") && configNode.get("tooltip").isObject()) {
                applyTooltipConfig(config.getTooltip(), configNode.get("tooltip"));
            }

            // Apply legend configuration
            if (configNode.has("legend") && configNode.get("legend").isObject()) {
                applyLegendConfig(config.getLegend(), configNode.get("legend"));
            }

            // Apply xAxis configuration
            if (configNode.has("xAxis")) {
                applyAxisConfig(config.getxAxis(), configNode.get("xAxis"));
            }

            // Apply yAxis configuration
            if (configNode.has("yAxis")) {
                applyAxisConfig(config.getyAxis(), configNode.get("yAxis"));
            }

            // Apply zAxis configuration
            if (configNode.has("zAxis")) {
                applyAxisConfig(config.getzAxis(), configNode.get("zAxis"));
            }

            // Apply colorAxis configuration
            if (configNode.has("colorAxis")) {
                applyColorAxisConfig(config, configNode.get("colorAxis"));
            }

            // Apply credits configuration
            if (configNode.has("credits") && configNode.get("credits").isObject()) {
                applyCreditsConfig(config.getCredits(), configNode.get("credits"));
            }

            // Apply pane configuration
            if (configNode.has("pane") && configNode.get("pane").isObject()) {
                applyPaneConfig(config, configNode.get("pane"));
            }

            // Apply exporting configuration
            if (configNode.has("exporting") && configNode.get("exporting").isObject()) {
                applyExportingConfig(config, configNode.get("exporting"));
            }
        } catch (Exception e) {
            System.err.println("Error applying chart config: " + e.getMessage());
        }
    }

    private void applyChartType(Configuration config, String chartTypeStr) {
        ChartType chartType = switch (chartTypeStr.toLowerCase()) {
            case "area" -> ChartType.AREA;
            case "line" -> ChartType.LINE;
            case "spline" -> ChartType.SPLINE;
            case "areaspline" -> ChartType.AREASPLINE;
            case "bullet" -> ChartType.BULLET;
            case "column" -> ChartType.COLUMN;
            case "bar" -> ChartType.BAR;
            case "pie" -> ChartType.PIE;
            case "scatter" -> ChartType.SCATTER;
            case "gauge" -> ChartType.GAUGE;
            case "arearange" -> ChartType.AREARANGE;
            case "columnrange" -> ChartType.COLUMNRANGE;
            case "areasplinerange" -> ChartType.AREASPLINERANGE;
            case "boxplot" -> ChartType.BOXPLOT;
            case "errorbar" -> ChartType.ERRORBAR;
            case "bubble" -> ChartType.BUBBLE;
            case "funnel" -> ChartType.FUNNEL;
            case "waterfall" -> ChartType.WATERFALL;
            case "pyramid" -> ChartType.PYRAMID;
            case "solidgauge" -> ChartType.SOLIDGAUGE;
            case "heatmap" -> ChartType.HEATMAP;
            case "treemap" -> ChartType.TREEMAP;
            case "polygon" -> ChartType.POLYGON;
            case "candlestick" -> ChartType.CANDLESTICK;
            case "flags" -> ChartType.FLAGS;
            case "timeline" -> ChartType.TIMELINE;
            case "ohlc" -> ChartType.OHLC;
            case "organization" -> ChartType.ORGANIZATION;
            case "sankey" -> ChartType.SANKEY;
            case "xrange" -> ChartType.XRANGE;
            case "gantt" -> ChartType.GANTT;
            default -> ChartType.LINE;
        };
        config.getChart().setType(chartType);
    }

    private void applyChartModelConfig(ChartModel chartModel, tools.jackson.databind.JsonNode chartNode) {
        // Background and border
        if (chartNode.has("backgroundColor") && chartNode.get("backgroundColor").isString()) {
            chartModel.setBackgroundColor(new SolidColor(chartNode.get("backgroundColor").asString()));
        }
        if (chartNode.has("borderWidth") && chartNode.get("borderWidth").isNumber()) {
            chartModel.setBorderWidth(chartNode.get("borderWidth").asInt());
        }
        if (chartNode.has("borderColor") && chartNode.get("borderColor").isString()) {
            chartModel.setBorderColor(new SolidColor(chartNode.get("borderColor").asString()));
        }
        if (chartNode.has("borderRadius") && chartNode.get("borderRadius").isNumber()) {
            chartModel.setBorderRadius(chartNode.get("borderRadius").asInt());
        }

        // Dimensions
        if (chartNode.has("width") && chartNode.get("width").isNumber()) {
            chartModel.setWidth(chartNode.get("width").asInt());
        }
        if (chartNode.has("height") && chartNode.get("height").isString()) {
            chartModel.setHeight(chartNode.get("height").asString());
        }

        // Margins
        if (chartNode.has("marginTop") && chartNode.get("marginTop").isNumber()) {
            chartModel.setMarginTop(chartNode.get("marginTop").asInt());
        }
        if (chartNode.has("marginRight") && chartNode.get("marginRight").isNumber()) {
            chartModel.setMarginRight(chartNode.get("marginRight").asInt());
        }
        if (chartNode.has("marginBottom") && chartNode.get("marginBottom").isNumber()) {
            chartModel.setMarginBottom(chartNode.get("marginBottom").asInt());
        }
        if (chartNode.has("marginLeft") && chartNode.get("marginLeft").isNumber()) {
            chartModel.setMarginLeft(chartNode.get("marginLeft").asInt());
        }

        // Spacing
        if (chartNode.has("spacingTop") && chartNode.get("spacingTop").isNumber()) {
            chartModel.setSpacingTop(chartNode.get("spacingTop").asInt());
        }
        if (chartNode.has("spacingRight") && chartNode.get("spacingRight").isNumber()) {
            chartModel.setSpacingRight(chartNode.get("spacingRight").asInt());
        }
        if (chartNode.has("spacingBottom") && chartNode.get("spacingBottom").isNumber()) {
            chartModel.setSpacingBottom(chartNode.get("spacingBottom").asInt());
        }
        if (chartNode.has("spacingLeft") && chartNode.get("spacingLeft").isNumber()) {
            chartModel.setSpacingLeft(chartNode.get("spacingLeft").asInt());
        }

        // Plot area
        if (chartNode.has("plotBackgroundColor") && chartNode.get("plotBackgroundColor").isString()) {
            chartModel.setPlotBackgroundColor(new SolidColor(chartNode.get("plotBackgroundColor").asString()));
        }
        if (chartNode.has("plotBorderColor") && chartNode.get("plotBorderColor").isString()) {
            chartModel.setPlotBorderColor(new SolidColor(chartNode.get("plotBorderColor").asString()));
        }
        if (chartNode.has("plotBorderWidth") && chartNode.get("plotBorderWidth").isNumber()) {
            chartModel.setPlotBorderWidth(chartNode.get("plotBorderWidth").asInt());
        }

        // Boolean options
        if (chartNode.has("inverted") && chartNode.get("inverted").isBoolean()) {
            chartModel.setInverted(chartNode.get("inverted").asBoolean());
        }
        if (chartNode.has("polar") && chartNode.get("polar").isBoolean()) {
            chartModel.setPolar(chartNode.get("polar").asBoolean());
        }
        if (chartNode.has("animation") && chartNode.get("animation").isBoolean()) {
            chartModel.setAnimation(chartNode.get("animation").asBoolean());
        }
        if (chartNode.has("styledMode") && chartNode.get("styledMode").isBoolean()) {
            chartModel.setStyledMode(chartNode.get("styledMode").asBoolean());
        }

        // Zoom
        if (chartNode.has("zoomType") && chartNode.get("zoomType").isString()) {
            String zoomType = chartNode.get("zoomType").asString().toUpperCase();
            try {
                chartModel.setZoomType(Dimension.valueOf(zoomType));
            } catch (IllegalArgumentException e) {
                // Invalid zoom type, skip
            }
        }
    }

    private void applyTitleConfig(Configuration config, tools.jackson.databind.JsonNode titleNode) {
        if (titleNode.isObject() && titleNode.has("text")) {
            config.setTitle(titleNode.get("text").asString());
        } else if (titleNode.isString()) {
            config.setTitle(titleNode.asString());
        }
    }

    private void applySubtitleConfig(Configuration config, tools.jackson.databind.JsonNode subtitleNode) {
        if (subtitleNode.isObject() && subtitleNode.has("text")) {
            config.setSubTitle(subtitleNode.get("text").asString());
        } else if (subtitleNode.isString()) {
            config.setSubTitle(subtitleNode.asString());
        }
    }

    private void applyTooltipConfig(Tooltip tooltip, tools.jackson.databind.JsonNode tooltipNode) {
        if (tooltipNode.has("pointFormat")) {
            tooltip.setPointFormat(tooltipNode.get("pointFormat").asString());
        }
        if (tooltipNode.has("headerFormat")) {
            tooltip.setHeaderFormat(tooltipNode.get("headerFormat").asString());
        }
        if (tooltipNode.has("shared") && tooltipNode.get("shared").isBoolean()) {
            tooltip.setShared(tooltipNode.get("shared").asBoolean());
        }
        if (tooltipNode.has("valueSuffix")) {
            tooltip.setValueSuffix(tooltipNode.get("valueSuffix").asString());
        }
        if (tooltipNode.has("valuePrefix")) {
            tooltip.setValuePrefix(tooltipNode.get("valuePrefix").asString());
        }
    }

    private void applyLegendConfig(Legend legend, tools.jackson.databind.JsonNode legendNode) {
        if (legendNode.has("enabled") && legendNode.get("enabled").isBoolean()) {
            legend.setEnabled(legendNode.get("enabled").asBoolean());
        }
        if (legendNode.has("align") && legendNode.get("align").isString()) {
            String align = legendNode.get("align").asString().toUpperCase();
            try {
                legend.setAlign(HorizontalAlign.valueOf(align));
            } catch (IllegalArgumentException e) {
                // Invalid alignment, skip
            }
        }
        if (legendNode.has("verticalAlign") && legendNode.get("verticalAlign").isString()) {
            String vAlign = legendNode.get("verticalAlign").asString().toUpperCase();
            try {
                legend.setVerticalAlign(VerticalAlign.valueOf(vAlign));
            } catch (IllegalArgumentException e) {
                // Invalid alignment, skip
            }
        }
        if (legendNode.has("layout") && legendNode.get("layout").isString()) {
            String layout = legendNode.get("layout").asString().toUpperCase();
            try {
                legend.setLayout(LayoutDirection.valueOf(layout));
            } catch (IllegalArgumentException e) {
                // Invalid layout, skip
            }
        }
    }

    private void applyAxisConfig(Axis axis, tools.jackson.databind.JsonNode axisNode) {
        if (axis == null || !axisNode.isObject()) {
            return;
        }

        if (axisNode.has("title") && axisNode.get("title").isObject()) {
            var titleNode = axisNode.get("title");
            if (titleNode.has("text")) {
                axis.setTitle(new AxisTitle(titleNode.get("text").asString()));
            }
        }
        if (axisNode.has("categories") && axisNode.get("categories").isArray()) {
            List<String> categories = new ArrayList<>();
            axisNode.get("categories").forEach(cat -> categories.add(cat.asString()));
            axis.setCategories(categories.toArray(new String[0]));
        }
        if (axisNode.has("min") && axisNode.get("min").isNumber()) {
            axis.setMin(axisNode.get("min").asDouble());
        }
        if (axisNode.has("max") && axisNode.get("max").isNumber()) {
            axis.setMax(axisNode.get("max").asDouble());
        }
    }

    private void applyCreditsConfig(Credits credits, tools.jackson.databind.JsonNode creditsNode) {
        if (creditsNode.has("enabled") && creditsNode.get("enabled").isBoolean()) {
            credits.setEnabled(creditsNode.get("enabled").asBoolean());
        }
        if (creditsNode.has("text")) {
            credits.setText(creditsNode.get("text").asString());
        }
        if (creditsNode.has("href")) {
            credits.setHref(creditsNode.get("href").asString());
        }
    }

    private void applyColorAxisConfig(Configuration config, tools.jackson.databind.JsonNode colorAxisNode) {
        if (!colorAxisNode.isObject()) {
            return;
        }

        ColorAxis colorAxis = config.getColorAxis();

        if (colorAxisNode.has("min") && colorAxisNode.get("min").isNumber()) {
            colorAxis.setMin(colorAxisNode.get("min").asDouble());
        }
        if (colorAxisNode.has("max") && colorAxisNode.get("max").isNumber()) {
            colorAxis.setMax(colorAxisNode.get("max").asDouble());
        }
        if (colorAxisNode.has("minColor") && colorAxisNode.get("minColor").isString()) {
            colorAxis.setMinColor(new SolidColor(colorAxisNode.get("minColor").asString()));
        }
        if (colorAxisNode.has("maxColor") && colorAxisNode.get("maxColor").isString()) {
            colorAxis.setMaxColor(new SolidColor(colorAxisNode.get("maxColor").asString()));
        }
    }

    private void applyPaneConfig(Configuration config, tools.jackson.databind.JsonNode paneNode) {
        if (!paneNode.isObject()) {
            return;
        }

        Pane pane = new Pane();

        if (paneNode.has("startAngle") && paneNode.get("startAngle").isNumber()) {
            pane.setStartAngle(paneNode.get("startAngle").asInt());
        }
        if (paneNode.has("endAngle") && paneNode.get("endAngle").isNumber()) {
            pane.setEndAngle(paneNode.get("endAngle").asInt());
        }
        if (paneNode.has("center") && paneNode.get("center").isArray()) {
            var centerArray = paneNode.get("center");
            if (centerArray.size() >= 2) {
                String[] center = new String[2];
                center[0] = centerArray.get(0).asString();
                center[1] = centerArray.get(1).asString();
                pane.setCenter(center);
            }
        }
        if (paneNode.has("size") && paneNode.get("size").isString()) {
            pane.setSize(paneNode.get("size").asString());
        }

        config.addPane(pane);
    }

    private void applyExportingConfig(Configuration config, tools.jackson.databind.JsonNode exportingNode) {
        if (!exportingNode.isObject()) {
            return;
        }

        Exporting exporting = config.getExporting();

        if (exportingNode.has("enabled") && exportingNode.get("enabled").isBoolean()) {
            exporting.setEnabled(exportingNode.get("enabled").asBoolean());
        }
        if (exportingNode.has("filename") && exportingNode.get("filename").isString()) {
            exporting.setFilename(exportingNode.get("filename").asString());
        }
        if (exportingNode.has("sourceWidth") && exportingNode.get("sourceWidth").isNumber()) {
            exporting.setSourceWidth(exportingNode.get("sourceWidth").asInt());
        }
        if (exportingNode.has("sourceHeight") && exportingNode.get("sourceHeight").isNumber()) {
            exporting.setSourceHeight(exportingNode.get("sourceHeight").asInt());
        }
        if (exportingNode.has("scale") && exportingNode.get("scale").isNumber()) {
            exporting.setScale(exportingNode.get("scale").asInt());
        }
    }
}
