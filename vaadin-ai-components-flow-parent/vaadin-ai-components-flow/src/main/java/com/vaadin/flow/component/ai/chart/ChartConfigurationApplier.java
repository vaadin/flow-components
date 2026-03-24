/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.ai.chart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.Axis;
import com.vaadin.flow.component.charts.model.AxisTitle;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartModel;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.ColorAxis;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Credits;
import com.vaadin.flow.component.charts.model.Dimension;
import com.vaadin.flow.component.charts.model.HorizontalAlign;
import com.vaadin.flow.component.charts.model.LayoutDirection;
import com.vaadin.flow.component.charts.model.Legend;
import com.vaadin.flow.component.charts.model.Pane;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.VerticalAlign;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Utility class for applying JSON configuration to Vaadin Chart objects.
 *
 * @author Vaadin Ltd
 */
public class ChartConfigurationApplier implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ChartConfigurationApplier.class);

    public void applyConfiguration(Chart chart, String configJson) {
        try {
            JsonNode parsedNode = JacksonUtils.getMapper().readTree(configJson);
            // Handle double-encoded JSON strings from LLM
            if (parsedNode.isTextual()) {
                parsedNode = JacksonUtils.getMapper()
                        .readTree(parsedNode.asString());
            }
            if (!(parsedNode instanceof ObjectNode configNode)) {
                LOGGER.warn("Expected JSON object for chart config but got: {}",
                        parsedNode.getNodeType());
                return;
            }
            Configuration config = chart.getConfiguration();

            String chartType = null;
            if (configNode.has("type")) {
                chartType = configNode.get("type").asString();
            } else if (configNode.has("chart")
                    && configNode.get("chart").isObject()) {
                JsonNode chartNode = configNode.get("chart");
                if (chartNode.has("type")) {
                    chartType = chartNode.get("type").asString();
                }
            }
            if (chartType != null) {
                applyChartType(config, chartType);
            }

            if (configNode.has("chart") && configNode.get("chart").isObject()) {
                applyChartModelConfig(config.getChart(),
                        configNode.get("chart"));
            }
            if (configNode.has("title")) {
                applyTitleConfig(config, configNode.get("title"));
            }
            if (configNode.has("subtitle")) {
                applySubtitleConfig(config, configNode.get("subtitle"));
            }
            if (configNode.has("tooltip")
                    && configNode.get("tooltip").isObject()) {
                applyTooltipConfig(config.getTooltip(),
                        configNode.get("tooltip"));
            }
            if (configNode.has("legend")
                    && configNode.get("legend").isObject()) {
                applyLegendConfig(config.getLegend(), configNode.get("legend"));
            }
            if (configNode.has("xAxis")) {
                applyAxisConfig(config.getxAxis(), configNode.get("xAxis"));
            }
            if (configNode.has("yAxis")) {
                applyAxisConfig(config.getyAxis(), configNode.get("yAxis"));
            }
            if (configNode.has("zAxis")) {
                applyAxisConfig(config.getzAxis(), configNode.get("zAxis"));
            }
            if (configNode.has("colorAxis")) {
                applyColorAxisConfig(config, configNode.get("colorAxis"));
            }
            if (configNode.has("credits")
                    && configNode.get("credits").isObject()) {
                applyCreditsConfig(config.getCredits(),
                        configNode.get("credits"));
            }
            if (configNode.has("pane") && configNode.get("pane").isObject()) {
                applyPaneConfig(config, configNode.get("pane"));
            }
        } catch (Exception e) {
            LOGGER.error("Error applying chart config", e);
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

    private void applyChartModelConfig(ChartModel chartModel,
            JsonNode chartNode) {
        if (chartNode.has("backgroundColor")
                && chartNode.get("backgroundColor").isString()) {
            chartModel.setBackgroundColor(new SolidColor(
                    chartNode.get("backgroundColor").asString()));
        }
        if (chartNode.has("borderWidth")
                && chartNode.get("borderWidth").isNumber()) {
            chartModel.setBorderWidth(chartNode.get("borderWidth").asInt());
        }
        if (chartNode.has("borderColor")
                && chartNode.get("borderColor").isString()) {
            chartModel.setBorderColor(
                    new SolidColor(chartNode.get("borderColor").asString()));
        }
        if (chartNode.has("borderRadius")
                && chartNode.get("borderRadius").isNumber()) {
            chartModel.setBorderRadius(chartNode.get("borderRadius").asInt());
        }
        if (chartNode.has("width") && chartNode.get("width").isNumber()) {
            chartModel.setWidth(chartNode.get("width").asInt());
        }
        if (chartNode.has("height") && chartNode.get("height").isString()) {
            chartModel.setHeight(chartNode.get("height").asString());
        }
        if (chartNode.has("marginTop")
                && chartNode.get("marginTop").isNumber()) {
            chartModel.setMarginTop(chartNode.get("marginTop").asInt());
        }
        if (chartNode.has("marginRight")
                && chartNode.get("marginRight").isNumber()) {
            chartModel.setMarginRight(chartNode.get("marginRight").asInt());
        }
        if (chartNode.has("marginBottom")
                && chartNode.get("marginBottom").isNumber()) {
            chartModel.setMarginBottom(chartNode.get("marginBottom").asInt());
        }
        if (chartNode.has("marginLeft")
                && chartNode.get("marginLeft").isNumber()) {
            chartModel.setMarginLeft(chartNode.get("marginLeft").asInt());
        }
        if (chartNode.has("spacingTop")
                && chartNode.get("spacingTop").isNumber()) {
            chartModel.setSpacingTop(chartNode.get("spacingTop").asInt());
        }
        if (chartNode.has("spacingRight")
                && chartNode.get("spacingRight").isNumber()) {
            chartModel.setSpacingRight(chartNode.get("spacingRight").asInt());
        }
        if (chartNode.has("spacingBottom")
                && chartNode.get("spacingBottom").isNumber()) {
            chartModel.setSpacingBottom(chartNode.get("spacingBottom").asInt());
        }
        if (chartNode.has("spacingLeft")
                && chartNode.get("spacingLeft").isNumber()) {
            chartModel.setSpacingLeft(chartNode.get("spacingLeft").asInt());
        }
        if (chartNode.has("plotBackgroundColor")
                && chartNode.get("plotBackgroundColor").isString()) {
            chartModel.setPlotBackgroundColor(new SolidColor(
                    chartNode.get("plotBackgroundColor").asString()));
        }
        if (chartNode.has("plotBorderColor")
                && chartNode.get("plotBorderColor").isString()) {
            chartModel.setPlotBorderColor(new SolidColor(
                    chartNode.get("plotBorderColor").asString()));
        }
        if (chartNode.has("plotBorderWidth")
                && chartNode.get("plotBorderWidth").isNumber()) {
            chartModel.setPlotBorderWidth(
                    chartNode.get("plotBorderWidth").asInt());
        }
        if (chartNode.has("inverted")
                && chartNode.get("inverted").isBoolean()) {
            chartModel.setInverted(chartNode.get("inverted").asBoolean());
        }
        if (chartNode.has("polar") && chartNode.get("polar").isBoolean()) {
            chartModel.setPolar(chartNode.get("polar").asBoolean());
        }
        if (chartNode.has("animation")
                && chartNode.get("animation").isBoolean()) {
            chartModel.setAnimation(chartNode.get("animation").asBoolean());
        }
        if (chartNode.has("styledMode")
                && chartNode.get("styledMode").isBoolean()) {
            chartModel.setStyledMode(chartNode.get("styledMode").asBoolean());
        }
        if (chartNode.has("zoomType") && chartNode.get("zoomType").isString()) {
            String zoomType = chartNode.get("zoomType").asString()
                    .toUpperCase();
            try {
                chartModel.setZoomType(Dimension.valueOf(zoomType));
            } catch (IllegalArgumentException e) {
                // Invalid zoom type, skip
            }
        }
    }

    private void applyTitleConfig(Configuration config, JsonNode titleNode) {
        if (titleNode.isObject() && titleNode.has("text")) {
            config.setTitle(titleNode.get("text").asString());
        } else if (titleNode.isString()) {
            config.setTitle(titleNode.asString());
        }
    }

    private void applySubtitleConfig(Configuration config,
            JsonNode subtitleNode) {
        if (subtitleNode.isObject() && subtitleNode.has("text")) {
            config.setSubTitle(subtitleNode.get("text").asString());
        } else if (subtitleNode.isString()) {
            config.setSubTitle(subtitleNode.asString());
        }
    }

    private void applyTooltipConfig(Tooltip tooltip, JsonNode tooltipNode) {
        if (tooltipNode.has("pointFormat")) {
            tooltip.setPointFormat(tooltipNode.get("pointFormat").asString());
        }
        if (tooltipNode.has("headerFormat")) {
            tooltip.setHeaderFormat(tooltipNode.get("headerFormat").asString());
        }
        if (tooltipNode.has("shared")
                && tooltipNode.get("shared").isBoolean()) {
            tooltip.setShared(tooltipNode.get("shared").asBoolean());
        }
        if (tooltipNode.has("valueSuffix")) {
            tooltip.setValueSuffix(tooltipNode.get("valueSuffix").asString());
        }
        if (tooltipNode.has("valuePrefix")) {
            tooltip.setValuePrefix(tooltipNode.get("valuePrefix").asString());
        }
    }

    private void applyLegendConfig(Legend legend, JsonNode legendNode) {
        if (legendNode.has("enabled")
                && legendNode.get("enabled").isBoolean()) {
            legend.setEnabled(legendNode.get("enabled").asBoolean());
        }
        if (legendNode.has("align") && legendNode.get("align").isString()) {
            try {
                legend.setAlign(HorizontalAlign.valueOf(
                        legendNode.get("align").asString().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // skip
            }
        }
        if (legendNode.has("verticalAlign")
                && legendNode.get("verticalAlign").isString()) {
            try {
                legend.setVerticalAlign(VerticalAlign.valueOf(legendNode
                        .get("verticalAlign").asString().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // skip
            }
        }
        if (legendNode.has("layout") && legendNode.get("layout").isString()) {
            try {
                legend.setLayout(LayoutDirection.valueOf(
                        legendNode.get("layout").asString().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // skip
            }
        }
    }

    private void applyAxisConfig(Axis axis, JsonNode axisNode) {
        if (axis == null || !axisNode.isObject()) {
            return;
        }
        if (axisNode.has("type") && axisNode.get("type").isString()) {
            try {
                axis.setType(AxisType.valueOf(
                        axisNode.get("type").asString().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Invalid axis type, skip
            }
        }
        if (axisNode.has("title") && axisNode.get("title").isObject()) {
            var titleNode = axisNode.get("title");
            if (titleNode.has("text")) {
                axis.setTitle(new AxisTitle(titleNode.get("text").asString()));
            }
        }
        if (axisNode.has("categories")
                && axisNode.get("categories").isArray()) {
            List<String> categories = new ArrayList<>();
            axisNode.get("categories")
                    .forEach(cat -> categories.add(cat.asString()));
            axis.setCategories(categories.toArray(new String[0]));
        }
        if (axisNode.has("min") && axisNode.get("min").isNumber()) {
            axis.setMin(axisNode.get("min").asDouble());
        }
        if (axisNode.has("max") && axisNode.get("max").isNumber()) {
            axis.setMax(axisNode.get("max").asDouble());
        }
    }

    private void applyCreditsConfig(Credits credits, JsonNode creditsNode) {
        if (creditsNode.has("enabled")
                && creditsNode.get("enabled").isBoolean()) {
            credits.setEnabled(creditsNode.get("enabled").asBoolean());
        }
        if (creditsNode.has("text")) {
            credits.setText(creditsNode.get("text").asString());
        }
        if (creditsNode.has("href")) {
            credits.setHref(creditsNode.get("href").asString());
        }
    }

    private void applyColorAxisConfig(Configuration config,
            JsonNode colorAxisNode) {
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
        if (colorAxisNode.has("minColor")
                && colorAxisNode.get("minColor").isString()) {
            colorAxis.setMinColor(
                    new SolidColor(colorAxisNode.get("minColor").asString()));
        }
        if (colorAxisNode.has("maxColor")
                && colorAxisNode.get("maxColor").isString()) {
            colorAxis.setMaxColor(
                    new SolidColor(colorAxisNode.get("maxColor").asString()));
        }
    }

    private void applyPaneConfig(Configuration config, JsonNode paneNode) {
        if (!paneNode.isObject()) {
            return;
        }
        Pane pane = new Pane();
        if (paneNode.has("startAngle")
                && paneNode.get("startAngle").isNumber()) {
            pane.setStartAngle(paneNode.get("startAngle").asInt());
        }
        if (paneNode.has("endAngle") && paneNode.get("endAngle").isNumber()) {
            pane.setEndAngle(paneNode.get("endAngle").asInt());
        }
        if (paneNode.has("center") && paneNode.get("center").isArray()) {
            var centerArray = paneNode.get("center");
            if (centerArray.size() >= 2) {
                pane.setCenter(new String[] { centerArray.get(0).asString(),
                        centerArray.get(1).asString() });
            }
        }
        if (paneNode.has("size") && paneNode.get("size").isString()) {
            pane.setSize(paneNode.get("size").asString());
        }
        config.addPane(pane);
    }

}
