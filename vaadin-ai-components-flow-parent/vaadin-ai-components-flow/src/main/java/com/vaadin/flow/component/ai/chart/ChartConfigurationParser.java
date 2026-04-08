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

import static com.vaadin.flow.component.ai.chart.ConfigurationKeys.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.vaadin.flow.component.charts.model.AbstractPlotOptions;
import com.vaadin.flow.component.charts.model.Axis;
import com.vaadin.flow.component.charts.model.AxisTitle;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartModel;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.ColorAxis;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Credits;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.Dimension;
import com.vaadin.flow.component.charts.model.HorizontalAlign;
import com.vaadin.flow.component.charts.model.LayoutDirection;
import com.vaadin.flow.component.charts.model.Legend;
import com.vaadin.flow.component.charts.model.Pane;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.VerticalAlign;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.charts.model.style.Color;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.DeserializationProblemHandler;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.node.ObjectNode;

/**
 * Parses a Highcharts JSON configuration string into a Vaadin
 * {@link Configuration} object.
 *
 * @author Vaadin Ltd
 */
public final class ChartConfigurationParser implements Serializable {

    private ChartConfigurationParser() {
    }

    /**
     * Parses a Highcharts JSON configuration string into a new
     * {@link Configuration} object.
     *
     * @param configJson
     *            the Highcharts JSON configuration string to parse
     * @return a new {@link Configuration} populated with the parsed values
     * @throws IllegalArgumentException
     *             if the JSON string is invalid or not an object
     */
    public static Configuration parse(String configJson) {
        Configuration config = new Configuration();
        merge(configJson, config);
        return config;
    }

    /**
     * Parses a JSON configuration string and applies the values onto the given
     * {@link Configuration}. Only properties present in the JSON are modified;
     * existing properties not mentioned in the JSON are preserved.
     *
     * @param configJson
     *            the Highcharts JSON configuration string to parse
     * @param config
     *            the existing {@link Configuration} to merge the parsed values
     *            into
     * @throws IllegalArgumentException
     *             if the JSON string is invalid or not an object
     */
    public static void merge(String configJson, Configuration config) {
        ObjectNode configNode = parseJsonToNode(configJson);

        String chartType = null;
        if (configNode.has(TYPE)) {
            chartType = configNode.get(TYPE).asString();
        } else if (configNode.has(CHART) && configNode.get(CHART).isObject()) {
            JsonNode chartNode = configNode.get(CHART);
            if (chartNode.has(TYPE)) {
                chartType = chartNode.get(TYPE).asString();
            }
        }
        if (chartType != null) {
            applyChartType(config, chartType);
        }

        if (configNode.has(CHART) && configNode.get(CHART).isObject()) {
            applyChartModelConfig(config.getChart(), configNode.get(CHART));
        }
        if (configNode.has(TITLE)) {
            applyTitleConfig(config, configNode.get(TITLE));
        }
        if (configNode.has(SUBTITLE)) {
            applySubtitleConfig(config, configNode.get(SUBTITLE));
        }
        if (configNode.has(TOOLTIP) && configNode.get(TOOLTIP).isObject()) {
            applyTooltipConfig(config.getTooltip(), configNode.get(TOOLTIP));
        }
        if (configNode.has(LEGEND) && configNode.get(LEGEND).isObject()) {
            applyLegendConfig(config.getLegend(), configNode.get(LEGEND));
        }
        if (configNode.has(X_AXIS)) {
            applyAxisConfig(config.getxAxis(), configNode.get(X_AXIS));
        }
        if (configNode.has(Y_AXIS)) {
            applyYAxisConfig(config, configNode.get(Y_AXIS));
        }
        if (configNode.has(Z_AXIS)) {
            applyAxisConfig(config.getzAxis(), configNode.get(Z_AXIS));
        }
        if (configNode.has(COLOR_AXIS)) {
            applyColorAxisConfig(config, configNode.get(COLOR_AXIS));
        }
        if (configNode.has(CREDITS) && configNode.get(CREDITS).isObject()) {
            applyCreditsConfig(config.getCredits(), configNode.get(CREDITS));
        }
        if (configNode.has(PANE) && configNode.get(PANE).isObject()) {
            applyPaneConfig(config, configNode.get(PANE));
        }
        if (configNode.has(PLOT_OPTIONS)
                && configNode.get(PLOT_OPTIONS).isObject()) {
            applyPlotOptionsConfig(config, configNode.get(PLOT_OPTIONS));
        }
        if (configNode.has(SERIES) && configNode.get(SERIES).isArray()) {
            applySeriesConfig(config, configNode.get(SERIES));
        }
    }

    private static ObjectNode parseJsonToNode(String configJson) {
        try {
            JsonNode parsedNode = JacksonUtils.getMapper().readTree(configJson);
            if (parsedNode.isString()) {
                parsedNode = JacksonUtils.getMapper()
                        .readTree(parsedNode.asString());
            }
            if (parsedNode instanceof ObjectNode objectNode) {
                return objectNode;
            }
            throw new IllegalArgumentException(
                    "Expected JSON object for chart config but got: "
                            + parsedNode.getNodeType());
        } catch (JacksonException e) {
            throw new IllegalArgumentException(
                    "Invalid chart configuration JSON: " + e.getMessage(), e);
        }
    }

    private static final Map<String, ChartType> CHART_TYPES_BY_NAME;

    static {
        var map = new HashMap<String, ChartType>();
        for (Field field : ChartType.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())
                    && Modifier.isFinal(field.getModifiers())
                    && field.getType() == ChartType.class) {
                try {
                    ChartType value = (ChartType) field.get(null);
                    map.put(value.toString(), value);
                } catch (IllegalAccessException e) {
                    throw new ExceptionInInitializerError(e);
                }
            }
        }
        CHART_TYPES_BY_NAME = Map.copyOf(map);
    }

    private static void applyChartType(Configuration config,
            String chartTypeStr) {
        ChartType chartType = CHART_TYPES_BY_NAME
                .getOrDefault(chartTypeStr.toLowerCase(), ChartType.LINE);
        config.getChart().setType(chartType);
    }

    private static void applyChartModelConfig(ChartModel chartModel,
            JsonNode chartNode) {
        if (chartNode.has(BACKGROUND_COLOR)
                && chartNode.get(BACKGROUND_COLOR).isString()) {
            chartModel.setBackgroundColor(
                    new SolidColor(chartNode.get(BACKGROUND_COLOR).asString()));
        }
        if (chartNode.has(BORDER_WIDTH)
                && chartNode.get(BORDER_WIDTH).isNumber()) {
            chartModel.setBorderWidth(chartNode.get(BORDER_WIDTH).asInt());
        }
        if (chartNode.has(BORDER_COLOR)
                && chartNode.get(BORDER_COLOR).isString()) {
            chartModel.setBorderColor(
                    new SolidColor(chartNode.get(BORDER_COLOR).asString()));
        }
        if (chartNode.has(BORDER_RADIUS)
                && chartNode.get(BORDER_RADIUS).isNumber()) {
            chartModel.setBorderRadius(chartNode.get(BORDER_RADIUS).asInt());
        }
        if (chartNode.has(WIDTH) && chartNode.get(WIDTH).isNumber()) {
            chartModel.setWidth(chartNode.get(WIDTH).asInt());
        }
        if (chartNode.has(HEIGHT)) {
            if (chartNode.get(HEIGHT).isString()) {
                chartModel.setHeight(chartNode.get(HEIGHT).asString());
            } else if (chartNode.get(HEIGHT).isNumber()) {
                chartModel.setHeight(
                        String.valueOf(chartNode.get(HEIGHT).asInt()));
            }
        }
        if (chartNode.has(MARGIN_TOP) && chartNode.get(MARGIN_TOP).isNumber()) {
            chartModel.setMarginTop(chartNode.get(MARGIN_TOP).asInt());
        }
        if (chartNode.has(MARGIN_RIGHT)
                && chartNode.get(MARGIN_RIGHT).isNumber()) {
            chartModel.setMarginRight(chartNode.get(MARGIN_RIGHT).asInt());
        }
        if (chartNode.has(MARGIN_BOTTOM)
                && chartNode.get(MARGIN_BOTTOM).isNumber()) {
            chartModel.setMarginBottom(chartNode.get(MARGIN_BOTTOM).asInt());
        }
        if (chartNode.has(MARGIN_LEFT)
                && chartNode.get(MARGIN_LEFT).isNumber()) {
            chartModel.setMarginLeft(chartNode.get(MARGIN_LEFT).asInt());
        }
        if (chartNode.has(SPACING_TOP)
                && chartNode.get(SPACING_TOP).isNumber()) {
            chartModel.setSpacingTop(chartNode.get(SPACING_TOP).asInt());
        }
        if (chartNode.has(SPACING_RIGHT)
                && chartNode.get(SPACING_RIGHT).isNumber()) {
            chartModel.setSpacingRight(chartNode.get(SPACING_RIGHT).asInt());
        }
        if (chartNode.has(SPACING_BOTTOM)
                && chartNode.get(SPACING_BOTTOM).isNumber()) {
            chartModel.setSpacingBottom(chartNode.get(SPACING_BOTTOM).asInt());
        }
        if (chartNode.has(SPACING_LEFT)
                && chartNode.get(SPACING_LEFT).isNumber()) {
            chartModel.setSpacingLeft(chartNode.get(SPACING_LEFT).asInt());
        }
        if (chartNode.has(PLOT_BACKGROUND_COLOR)
                && chartNode.get(PLOT_BACKGROUND_COLOR).isString()) {
            chartModel.setPlotBackgroundColor(new SolidColor(
                    chartNode.get(PLOT_BACKGROUND_COLOR).asString()));
        }
        if (chartNode.has(PLOT_BORDER_COLOR)
                && chartNode.get(PLOT_BORDER_COLOR).isString()) {
            chartModel.setPlotBorderColor(new SolidColor(
                    chartNode.get(PLOT_BORDER_COLOR).asString()));
        }
        if (chartNode.has(PLOT_BORDER_WIDTH)
                && chartNode.get(PLOT_BORDER_WIDTH).isNumber()) {
            chartModel.setPlotBorderWidth(
                    chartNode.get(PLOT_BORDER_WIDTH).asInt());
        }
        if (chartNode.has(INVERTED) && chartNode.get(INVERTED).isBoolean()) {
            chartModel.setInverted(chartNode.get(INVERTED).asBoolean());
        }
        if (chartNode.has(POLAR) && chartNode.get(POLAR).isBoolean()) {
            chartModel.setPolar(chartNode.get(POLAR).asBoolean());
        }
        if (chartNode.has(ANIMATION) && chartNode.get(ANIMATION).isBoolean()) {
            chartModel.setAnimation(chartNode.get(ANIMATION).asBoolean());
        }
        if (chartNode.has(STYLED_MODE)
                && chartNode.get(STYLED_MODE).isBoolean()) {
            chartModel.setStyledMode(chartNode.get(STYLED_MODE).asBoolean());
        }
        if (chartNode.has(ZOOM_TYPE) && chartNode.get(ZOOM_TYPE).isString()) {
            String zoomType = chartNode.get(ZOOM_TYPE).asString().toUpperCase();
            try {
                chartModel.setZoomType(Dimension.valueOf(zoomType));
            } catch (IllegalArgumentException e) {
                // Invalid zoom type, skip
            }
        }
    }

    private static void applyTitleConfig(Configuration config,
            JsonNode titleNode) {
        if (titleNode.isObject() && titleNode.has(TEXT)) {
            config.setTitle(titleNode.get(TEXT).asString());
        } else if (titleNode.isString()) {
            config.setTitle(titleNode.asString());
        }
    }

    private static void applySubtitleConfig(Configuration config,
            JsonNode subtitleNode) {
        if (subtitleNode.isObject() && subtitleNode.has(TEXT)) {
            config.setSubTitle(subtitleNode.get(TEXT).asString());
        } else if (subtitleNode.isString()) {
            config.setSubTitle(subtitleNode.asString());
        }
    }

    private static void applyTooltipConfig(Tooltip tooltip,
            JsonNode tooltipNode) {
        if (tooltipNode.has(POINT_FORMAT)) {
            tooltip.setPointFormat(tooltipNode.get(POINT_FORMAT).asString());
        }
        if (tooltipNode.has(HEADER_FORMAT)) {
            tooltip.setHeaderFormat(tooltipNode.get(HEADER_FORMAT).asString());
        }
        if (tooltipNode.has(SHARED) && tooltipNode.get(SHARED).isBoolean()) {
            tooltip.setShared(tooltipNode.get(SHARED).asBoolean());
        }
        if (tooltipNode.has(VALUE_SUFFIX)) {
            tooltip.setValueSuffix(tooltipNode.get(VALUE_SUFFIX).asString());
        }
        if (tooltipNode.has(VALUE_PREFIX)) {
            tooltip.setValuePrefix(tooltipNode.get(VALUE_PREFIX).asString());
        }
    }

    private static void applyLegendConfig(Legend legend, JsonNode legendNode) {
        if (legendNode.has(ENABLED) && legendNode.get(ENABLED).isBoolean()) {
            legend.setEnabled(legendNode.get(ENABLED).asBoolean());
        }
        if (legendNode.has(ALIGN) && legendNode.get(ALIGN).isString()) {
            try {
                legend.setAlign(HorizontalAlign.valueOf(
                        legendNode.get(ALIGN).asString().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // skip
            }
        }
        if (legendNode.has(VERTICAL_ALIGN)
                && legendNode.get(VERTICAL_ALIGN).isString()) {
            try {
                legend.setVerticalAlign(VerticalAlign.valueOf(legendNode
                        .get(VERTICAL_ALIGN).asString().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // skip
            }
        }
        if (legendNode.has(LAYOUT) && legendNode.get(LAYOUT).isString()) {
            try {
                legend.setLayout(LayoutDirection.valueOf(
                        legendNode.get(LAYOUT).asString().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // skip
            }
        }
    }

    /**
     * Handles yAxis as either a single object or an array of axis configs. When
     * an array is provided, the first element configures the default y-axis and
     * additional elements create secondary axes via
     * {@link Configuration#addyAxis(YAxis)}.
     */
    private static void applyYAxisConfig(Configuration config,
            JsonNode yAxisNode) {
        if (yAxisNode.isObject()) {
            applyAxisConfig(config.getyAxis(), yAxisNode);
        } else if (yAxisNode.isArray()) {
            for (int i = 0; i < yAxisNode.size(); i++) {
                JsonNode element = yAxisNode.get(i);
                if (!element.isObject()) {
                    continue;
                }
                if (i == 0) {
                    applyAxisConfig(config.getyAxis(), element);
                } else {
                    config.addyAxis(createSecondaryYAxis(element));
                }
            }
        }
    }

    private static YAxis createSecondaryYAxis(JsonNode element) {
        YAxis axis = new YAxis();
        applyAxisConfig(axis, element);
        if (element.has(OPPOSITE) && element.get(OPPOSITE).isBoolean()) {
            axis.setOpposite(element.get(OPPOSITE).asBoolean());
        }
        return axis;
    }

    private static void applyAxisConfig(Axis axis, JsonNode axisNode) {
        if (axis == null || !axisNode.isObject()) {
            return;
        }
        if (axisNode.has(TYPE) && axisNode.get(TYPE).isString()) {
            try {
                axis.setType(AxisType
                        .valueOf(axisNode.get(TYPE).asString().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Invalid axis type, skip
            }
        }
        if (axisNode.has(TITLE) && axisNode.get(TITLE).isObject()) {
            var titleNode = axisNode.get(TITLE);
            if (titleNode.has(TEXT)) {
                axis.setTitle(new AxisTitle(titleNode.get(TEXT).asString()));
            }
        }
        if (axisNode.has(CATEGORIES) && axisNode.get(CATEGORIES).isArray()) {
            List<String> categories = new ArrayList<>();
            axisNode.get(CATEGORIES)
                    .forEach(cat -> categories.add(cat.asString()));
            axis.setCategories(categories.toArray(new String[0]));
        }
        if (axisNode.has(MIN) && axisNode.get(MIN).isNumber()) {
            axis.setMin(axisNode.get(MIN).asDouble());
        }
        if (axisNode.has(MAX) && axisNode.get(MAX).isNumber()) {
            axis.setMax(axisNode.get(MAX).asDouble());
        }
    }

    private static void applyCreditsConfig(Credits credits,
            JsonNode creditsNode) {
        if (creditsNode.has(ENABLED) && creditsNode.get(ENABLED).isBoolean()) {
            credits.setEnabled(creditsNode.get(ENABLED).asBoolean());
        }
        if (creditsNode.has(TEXT)) {
            credits.setText(creditsNode.get(TEXT).asString());
        }
        if (creditsNode.has(HREF)) {
            credits.setHref(creditsNode.get(HREF).asString());
        }
    }

    private static void applyColorAxisConfig(Configuration config,
            JsonNode colorAxisNode) {
        if (!colorAxisNode.isObject()) {
            return;
        }
        ColorAxis colorAxis = config.getColorAxis();
        if (colorAxisNode.has(MIN) && colorAxisNode.get(MIN).isNumber()) {
            colorAxis.setMin(colorAxisNode.get(MIN).asDouble());
        }
        if (colorAxisNode.has(MAX) && colorAxisNode.get(MAX).isNumber()) {
            colorAxis.setMax(colorAxisNode.get(MAX).asDouble());
        }
        if (colorAxisNode.has(MIN_COLOR)
                && colorAxisNode.get(MIN_COLOR).isString()) {
            colorAxis.setMinColor(
                    new SolidColor(colorAxisNode.get(MIN_COLOR).asString()));
        }
        if (colorAxisNode.has(MAX_COLOR)
                && colorAxisNode.get(MAX_COLOR).isString()) {
            colorAxis.setMaxColor(
                    new SolidColor(colorAxisNode.get(MAX_COLOR).asString()));
        }
    }

    private static void applyPaneConfig(Configuration config,
            JsonNode paneNode) {
        if (!paneNode.isObject()) {
            return;
        }
        // Use the existing pane instead of adding a new one to avoid
        // pane accumulation across repeated renders.
        Pane pane = config.getPane();
        if (paneNode.has(START_ANGLE) && paneNode.get(START_ANGLE).isNumber()) {
            pane.setStartAngle(paneNode.get(START_ANGLE).asInt());
        }
        if (paneNode.has(END_ANGLE) && paneNode.get(END_ANGLE).isNumber()) {
            pane.setEndAngle(paneNode.get(END_ANGLE).asInt());
        }
        if (paneNode.has(CENTER) && paneNode.get(CENTER).isArray()) {
            var centerArray = paneNode.get(CENTER);
            if (centerArray.size() >= 2) {
                pane.setCenter(new String[] { centerArray.get(0).asString(),
                        centerArray.get(1).asString() });
            }
        }
        if (paneNode.has(SIZE) && paneNode.get(SIZE).isString()) {
            pane.setSize(paneNode.get(SIZE).asString());
        }
    }

    /**
     * Applies global plot options from the {@code plotOptions} configuration
     * section. Each key (e.g. "series", "column", "pie") maps to a chart type
     * whose PlotOptions class is resolved and deserialized.
     */
    private static void applyPlotOptionsConfig(Configuration config,
            JsonNode plotOptionsNode) {
        for (var entry : plotOptionsNode.properties()) {
            String typeName = entry.getKey();
            JsonNode optionNode = entry.getValue();
            if (!optionNode.isObject()) {
                continue;
            }
            Class<? extends AbstractPlotOptions> clazz = PlotOptionsSchema
                    .getPlotOptionsClass(typeName);
            if (clazz == null) {
                continue;
            }
            var plotOptions = PLOT_OPTIONS_READER.treeToValue(optionNode,
                    clazz);
            config.addPlotOptions(plotOptions);
        }
    }

    /**
     * ObjectMapper configured for deserializing plot options from JSON. Uses
     * field-based access (matching ChartSerialization), case-insensitive enums,
     * and ignores unknown properties.
     */
    private static final ObjectMapper PLOT_OPTIONS_READER;

    private static final class ColorDeserializer
            extends ValueDeserializer<Color> implements Serializable {
        @Override
        public Color deserialize(JsonParser p, DeserializationContext ctxt)
                throws JacksonException {
            return new SolidColor(p.getString());
        }
    }

    private static final class LenientEnumHandler
            extends DeserializationProblemHandler implements Serializable {
        @Override
        public Object handleWeirdStringValue(DeserializationContext ctxt,
                Class<?> targetType, String valueToConvert, String failureMsg) {
            if (targetType.isEnum()) {
                return null;
            }
            return NOT_HANDLED;
        }
    }

    static {
        var colorModule = new SimpleModule("ColorDeserializer");
        colorModule.addDeserializer(Color.class, new ColorDeserializer());

        PLOT_OPTIONS_READER = JsonMapper.builder()
                .changeDefaultVisibility(handler -> handler
                        .withVisibility(PropertyAccessor.ALL, Visibility.NONE)
                        .withVisibility(PropertyAccessor.FIELD, Visibility.ANY))
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addHandler(new LenientEnumHandler()).addModule(colorModule)
                .build();
    }

    // --- Per-series configuration ---

    private static final String Y_AXIS_KEY = "yAxis";
    private static final String NAME = "name";
    private static final String SERIES = "series";

    /**
     * Parses series entries from the JSON array and adds them to the
     * configuration as {@link DataSeries} with name, plot options, and y-axis
     * binding set (but no data). These act as configuration templates that the
     * renderer applies to data series matched by name.
     */
    private static void applySeriesConfig(Configuration config,
            JsonNode seriesArray) {
        for (var entryNode : seriesArray) {
            if (!entryNode.isObject() || !entryNode.has(NAME)) {
                continue;
            }
            String seriesName = entryNode.get(NAME).asString();
            if (seriesName == null || seriesName.isEmpty()) {
                continue;
            }

            var series = new DataSeries();
            series.setName(seriesName);

            String type = entryNode.has(TYPE) && entryNode.get(TYPE).isString()
                    ? entryNode.get(TYPE).asString()
                    : null;

            if (entryNode.has(Y_AXIS_KEY)
                    && entryNode.get(Y_AXIS_KEY).isNumber()) {
                series.setyAxis(entryNode.get(Y_AXIS_KEY).asInt());
            }

            AbstractPlotOptions plotOptions = deserializePlotOptions(type,
                    (ObjectNode) entryNode);
            if (plotOptions != null) {
                series.setPlotOptions(plotOptions);
            }

            config.addSeries(series);
        }
    }

    private static AbstractPlotOptions deserializePlotOptions(String type,
            ObjectNode optionsNode) {
        String typeName = type != null ? type.toLowerCase(Locale.ENGLISH)
                : "series";
        Class<? extends AbstractPlotOptions> targetClass = PlotOptionsSchema
                .getPlotOptionsClass(typeName);
        if (targetClass == null) {
            targetClass = PlotOptionsSeries.class;
        }

        // Remove non-PlotOptions fields before deserialization
        ObjectNode plotNode = optionsNode.deepCopy();
        plotNode.remove(TYPE);
        plotNode.remove(Y_AXIS_KEY);
        plotNode.remove(NAME);
        plotNode.remove("data");

        return PLOT_OPTIONS_READER.treeToValue(plotNode, targetClass);
    }

}
