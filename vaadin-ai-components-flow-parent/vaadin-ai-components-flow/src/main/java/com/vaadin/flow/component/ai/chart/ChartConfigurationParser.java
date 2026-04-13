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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.vaadin.flow.component.charts.model.AbstractPlotOptions;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.charts.model.style.Color;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.DeserializationProblemHandler;
import tools.jackson.databind.deser.ValueInstantiator;
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
            // Strip "type" — handled separately via applyChartType
            ObjectNode chartNode = ((ObjectNode) configNode.get(CHART))
                    .deepCopy();
            chartNode.remove(TYPE);
            mergeInto(config.getChart(), chartNode);
        }
        if (configNode.has(TITLE)) {
            JsonNode titleNode = configNode.get(TITLE);
            if (titleNode.isObject()) {
                mergeInto(config.getTitle(), titleNode);
            } else if (titleNode.isString()) {
                config.setTitle(titleNode.asString());
            }
        }
        if (configNode.has(SUBTITLE)) {
            JsonNode subtitleNode = configNode.get(SUBTITLE);
            if (subtitleNode.isObject()) {
                mergeInto(config.getSubTitle(), subtitleNode);
            } else if (subtitleNode.isString()) {
                config.setSubTitle(subtitleNode.asString());
            }
        }
        if (configNode.has(TOOLTIP) && configNode.get(TOOLTIP).isObject()) {
            mergeInto(config.getTooltip(), configNode.get(TOOLTIP));
        }
        if (configNode.has(LEGEND) && configNode.get(LEGEND).isObject()) {
            mergeInto(config.getLegend(), configNode.get(LEGEND));
        }
        if (configNode.has(X_AXIS) && configNode.get(X_AXIS).isObject()) {
            mergeInto(config.getxAxis(), configNode.get(X_AXIS));
        }
        if (configNode.has(Y_AXIS)) {
            applyYAxisConfig(config, configNode.get(Y_AXIS));
        }
        if (configNode.has(Z_AXIS) && configNode.get(Z_AXIS).isObject()) {
            mergeInto(config.getzAxis(), configNode.get(Z_AXIS));
        }
        if (configNode.has(COLOR_AXIS)
                && configNode.get(COLOR_AXIS).isObject()) {
            mergeInto(config.getColorAxis(), configNode.get(COLOR_AXIS));
        }
        if (configNode.has(CREDITS) && configNode.get(CREDITS).isObject()) {
            mergeInto(config.getCredits(), configNode.get(CREDITS));
        }
        if (configNode.has(PANE) && configNode.get(PANE).isObject()) {
            mergeInto(config.getPane(), configNode.get(PANE));
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

    // --- Jackson ObjectMapper for model deserialization ---

    /**
     * ObjectMapper configured for deserializing chart model objects from JSON.
     * Uses field-based access (matching ChartSerialization), case-insensitive
     * enums, and a custom Color deserializer.
     */
    private static final ObjectMapper READER;

    private static final class ColorDeserializer
            extends ValueDeserializer<Color> implements Serializable {
        @Override
        public Color deserialize(JsonParser p, DeserializationContext ctxt)
                throws JacksonException {
            return new SolidColor(p.getString());
        }
    }

    private static final class LenientDeserializationHandler
            extends DeserializationProblemHandler implements Serializable {
        @Override
        public Object handleWeirdStringValue(DeserializationContext ctxt,
                Class<?> targetType, String valueToConvert, String failureMsg) {
            if (targetType.isEnum()) {
                return null;
            }
            return NOT_HANDLED;
        }

        @Override
        public Object handleUnexpectedToken(DeserializationContext ctxt,
                JavaType targetType, JsonToken t, JsonParser p,
                String failureMsg) {
            p.skipChildren();
            return null;
        }

        @Override
        public Object handleMissingInstantiator(DeserializationContext ctxt,
                Class<?> instClass, ValueInstantiator valueInst, JsonParser p,
                String msg) {
            // Silently skip type-mismatched values (e.g. a string or
            // number where an object is expected) so that invalid input
            // is ignored rather than causing a hard failure.
            p.skipChildren();
            return null;
        }
    }

    static {
        var colorModule = new SimpleModule("ColorDeserializer");
        colorModule.addDeserializer(Color.class, new ColorDeserializer());

        READER = JsonMapper.builder()
                .changeDefaultVisibility(handler -> handler
                        .withVisibility(PropertyAccessor.ALL, Visibility.NONE)
                        .withVisibility(PropertyAccessor.FIELD, Visibility.ANY))
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addHandler(new LenientDeserializationHandler())
                .addModule(colorModule).build();
    }

    /**
     * Merges JSON properties into an existing object using the configured
     * {@link #READER}.
     */
    private static <T> void mergeInto(T target, JsonNode jsonNode) {
        READER.readerForUpdating(target).readValue(jsonNode);
    }

    // --- ChartType resolution ---

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

    // --- Y-axis (supports single object or array of axes) ---

    /**
     * Handles yAxis as either a single object or an array of axis configs. When
     * an array is provided, the first element configures the default y-axis and
     * additional elements create secondary axes via
     * {@link Configuration#addyAxis(YAxis)}.
     */
    private static void applyYAxisConfig(Configuration config,
            JsonNode yAxisNode) {
        if (yAxisNode.isObject()) {
            mergeInto(config.getyAxis(), yAxisNode);
        } else if (yAxisNode.isArray()) {
            config.removeyAxes();
            for (int i = 0; i < yAxisNode.size(); i++) {
                JsonNode element = yAxisNode.get(i);
                if (!element.isObject()) {
                    continue;
                }
                if (i == 0) {
                    mergeInto(config.getyAxis(), element);
                } else {
                    YAxis axis = new YAxis();
                    mergeInto(axis, element);
                    config.addyAxis(axis);
                }
            }
        }
    }

    // --- Plot options ---

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
            var plotOptions = READER.treeToValue(optionNode, clazz);
            config.addPlotOptions(plotOptions);
        }
    }

    // --- Per-series configuration ---

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

            if (entryNode.has(Y_AXIS) && entryNode.get(Y_AXIS).isNumber()) {
                series.setyAxis(entryNode.get(Y_AXIS).asInt());
            }

            if (entryNode.has(PLOT_OPTIONS)
                    && entryNode.get(PLOT_OPTIONS).isObject()) {
                AbstractPlotOptions plotOptions = deserializePlotOptions(type,
                        (ObjectNode) entryNode.get(PLOT_OPTIONS));
                if (plotOptions != null) {
                    series.setPlotOptions(plotOptions);
                }
            } else if (type != null) {
                AbstractPlotOptions plotOptions = deserializePlotOptions(type,
                        JacksonUtils.createObjectNode());
                if (plotOptions != null) {
                    series.setPlotOptions(plotOptions);
                }
            }

            config.addSeries(series);
        }
    }

    private static AbstractPlotOptions deserializePlotOptions(String type,
            ObjectNode plotOptionsNode) {
        String typeName = type != null ? type.toLowerCase(Locale.ENGLISH)
                : "series";
        Class<? extends AbstractPlotOptions> targetClass = PlotOptionsSchema
                .getPlotOptionsClass(typeName);
        if (targetClass == null) {
            targetClass = PlotOptionsSeries.class;
        }
        return READER.treeToValue(plotOptionsNode, targetClass);
    }

}
