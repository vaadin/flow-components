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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.provider.LLMProvider;

import tools.jackson.databind.JsonNode;

/**
 * Factory for creating reusable chart {@link LLMProvider.ToolSpec} instances.
 * <p>
 * The tools use a {@code chartId} parameter to identify which chart to operate
 * on, allowing a single set of tools to manage multiple charts (e.g., in a
 * dashboard). Callers provide a {@link Callbacks} implementation for state
 * retrieval and mutation.
 * </p>
 *
 * @author Vaadin Ltd
 */
public final class ChartAITools {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ChartAITools.class);

    /**
     * Callback interface that chart tool consumers must implement to provide
     * chart state access and mutation operations.
     */
    public interface Callbacks extends Serializable {

        /**
         * Returns the current state of a chart including its Highcharts
         * configuration and SQL queries. The returned JSON string should
         * contain the chart configuration and the SQL queries used to populate
         * the chart series. Should throw if the chart is not found.
         *
         * @param chartId
         *            the chart ID
         * @return the chart state as a JSON string containing the Highcharts
         *         configuration and SQL queries
         */
        String getState(String chartId);

        /**
         * Updates the chart's Highcharts configuration (type, title, tooltip,
         * axes, legend, etc.). The configuration should not include series
         * data, as that is managed separately via
         * {@link #updateData(String, List)}. Changes are applied when the LLM
         * request completes. Should throw if the chart is not found.
         *
         * @param chartId
         *            the chart ID
         * @param configJson
         *            the Highcharts configuration as a JSON string, excluding
         *            series data
         */
        void updateConfiguration(String chartId, String configJson);

        /**
         * Validates and executes SQL SELECT queries that populate the chart
         * series data. Each query corresponds to one chart series. Should throw
         * if the chart is not found or if any query is invalid.
         *
         * @param chartId
         *            the chart ID
         * @param queries
         *            SQL SELECT queries, one per chart series
         */
        void updateData(String chartId, List<String> queries);

        /**
         * Returns the set of available chart IDs.
         *
         * @return the chart IDs, never {@code null}
         */
        Set<String> getChartIds();
    }

    private static final Map<String, String> COLUMN_PARAMS = buildPlaceholderMap(
            ColumnNames.class, "");
    private static final Map<String, String> CONFIG_PARAMS = buildPlaceholderMap(
            ConfigurationKeys.class, "c:");

    /**
     * Builds a placeholder map from all {@code public static final String}
     * fields of the given class. Each field named {@code FOO} produces an entry
     * {@code {<prefix>FOO} → fieldValue}.
     */
    private static Map<String, String> buildPlaceholderMap(Class<?> clazz,
            String prefix) {
        var map = new HashMap<String, String>();
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())
                    && Modifier.isFinal(field.getModifiers())
                    && field.getType() == String.class) {
                try {
                    String value = (String) field.get(null);
                    map.put("{" + prefix + field.getName() + "}", value);
                } catch (IllegalAccessException e) {
                    throw new ExceptionInInitializerError(e);
                }
            }
        }
        return Map.copyOf(map);
    }

    /**
     * Replaces {@code {NAME}} placeholders in the template with the
     * corresponding {@link ColumnNames} constant values.
     */
    private static String resolveColumnNames(String template) {
        return resolvePlaceholders(template, COLUMN_PARAMS);
    }

    /**
     * Replaces {@code {c:NAME}} placeholders in the template with the
     * corresponding {@link ConfigurationKeys} constant values.
     */
    private static String resolveConfigKeys(String template) {
        return resolvePlaceholders(template, CONFIG_PARAMS);
    }

    private static String resolvePlaceholders(String template,
            Map<String, String> params) {
        var result = template;
        for (var entry : params.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Parses and validates the queries array from the tool arguments. Returns
     * an error message if validation fails, or {@code null} if all queries are
     * valid SELECT statements.
     */
    private static String validateQueries(JsonNode queriesNode,
            List<String> out) {
        if (queriesNode == null || queriesNode.isNull()) {
            return "Error updating chart data: 'queries' parameter is required.";
        }
        if (!queriesNode.isArray()) {
            return "Error updating chart data: 'queries' must be an array.";
        }
        for (JsonNode q : queriesNode) {
            if (q == null || q.isNull()) {
                return "Error updating chart data: 'queries' must not contain null elements.";
            }
            String query = q.asString().strip();
            if (query.isEmpty()) {
                return "Error updating chart data: 'queries' must not contain empty strings.";
            }
            out.add(query);
        }
        return null;
    }

    private ChartAITools() {
    }

    /**
     * Signals a validation failure whose message is safe to pass back to the
     * LLM. Unexpected runtime exceptions, by contrast, may carry internal
     * detail (SQL fragments, schema names, file paths) and must be replaced
     * with a generic message before being returned.
     */
    private static final class ValidationException extends RuntimeException {
        ValidationException(String message) {
            super(message);
        }
    }

    /**
     * Resolves the chart ID from the tool arguments. If {@code chartId} is not
     * provided and there is exactly one chart, that chart's ID is used as the
     * default.
     */
    private static String resolveChartId(JsonNode args, Callbacks callbacks) {
        var ids = callbacks.getChartIds();
        if (ids.isEmpty()) {
            throw new ValidationException("No charts available.");
        }
        if (ids.size() == 1) {
            return ids.iterator().next();
        }
        var idNode = args.get("chartId");
        if (idNode != null && ids.contains(idNode.asString())) {
            return idNode.asString();
        }
        throw new ValidationException(
                "chartId is required when multiple charts exist. "
                        + "Available chart IDs: " + ids);
    }

    /**
     * Creates all chart tools for the given callbacks.
     *
     * @param callbacks
     *            the callbacks for chart state access and mutation; not
     *            {@code null}
     * @return a list of all chart tools, never {@code null}
     */
    public static List<LLMProvider.ToolSpec> createAll(Callbacks callbacks) {
        Objects.requireNonNull(callbacks, "callbacks must not be null");
        return List.of(getChartState(callbacks),
                updateChartConfiguration(callbacks),
                updateChartDataSource(callbacks), getPlotOptionsSchema());
    }

    public static LLMProvider.ToolSpec getChartState(Callbacks callbacks) {
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "get_chart_state";
            }

            @Override
            public String getDescription() {
                return "Gets the current state of a chart including its "
                        + "Highcharts configuration and SQL queries. Returns "
                        + "the chart configuration as JSON and the SQL "
                        + "queries used to populate the chart series.";
            }

            @Override
            public String getParametersSchema() {
                return """
                        {
                          "type": "object",
                          "properties": {
                            "chartId": {
                              "type": "string",
                              "description": "The ID of the chart. Optional when there is only one chart."
                            }
                          }
                        }""";
            }

            @Override
            public String execute(JsonNode arguments) {
                try {
                    LOGGER.info("get_chart_state called");
                    String chartId = resolveChartId(arguments, callbacks);
                    return callbacks.getState(chartId);
                } catch (ValidationException e) {
                    LOGGER.warn("get_chart_state validation failed", e);
                    return "Error getting chart state: " + e.getMessage();
                } catch (Exception e) {
                    LOGGER.error("get_chart_state failed", e);
                    return "Error getting chart state.";
                }
            }
        };
    }

    public static LLMProvider.ToolSpec updateChartConfiguration(
            Callbacks callbacks) {
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "update_chart_configuration";
            }

            @Override
            public String getDescription() {
                return """
                        Updates the Highcharts configuration of a chart. Only include the \
                        properties you want to change — existing properties are preserved. \
                        Changing chart.type resets the entire configuration.

                        CRITICAL: ALWAYS specify the chart type in configuration.chart.type \
                        when creating a new chart or changing chart type.

                        IMPORTANT: Do NOT include series data in the configuration — chart data \
                        is managed separately via update_chart_data_source tool.

                        Parameters:
                        - chartId (string, optional): The ID of the chart to update. Required when multiple charts exist.
                        - configuration (object, required): Highcharts configuration object (excluding series)

                        Changes are applied when the request completes.""";
            }

            @Override
            public String getParametersSchema() {
                var template = """
                        {
                          "type": "object",
                          "properties": {
                            "chartId": {
                              "type": "string",
                              "description": "The ID of the chart. Optional when there is only one chart."
                            },
                            "configuration": {
                              "type": "object",
                              "description": "Highcharts configuration object. Follows the same structure as the Highcharts options object (as returned by get_chart_state), excluding series data which is managed via update_chart_data_source. CRITICAL: Always include chart.type.",
                              "properties": {
                                "{c:CHART}": {
                                  "type": "object",
                                  "description": "Chart model options - MUST include 'type' property. Also supports dimensions, margins, spacing, borders, background",
                                  "properties": {
                                    "{c:TYPE}": {
                                      "type": "string",
                                      "description": "REQUIRED: Chart type - ALWAYS specify this property",
                                      "enum": ["line", "spline", "area", "areaspline", "bar", "column", "pie", "scatter", "gauge", "arearange", "columnrange", "areasplinerange", "boxplot", "errorbar", "bubble", "funnel", "waterfall", "pyramid", "solidgauge", "heatmap", "treemap", "polygon", "candlestick", "flags", "timeline", "ohlc", "organization", "sankey", "xrange", "gantt", "bullet"]
                                    },
                                    "{c:BACKGROUND_COLOR}": { "type": "string", "description": "Background color or gradient for the outer chart area (e.g., '#ffffff')" },
                                    "{c:BORDER_COLOR}": { "type": "string", "description": "Color of the outer chart border" },
                                    "{c:BORDER_WIDTH}": { "type": "number", "description": "Pixel width of the outer chart border" },
                                    "{c:BORDER_RADIUS}": { "type": "number", "description": "Corner radius of the outer chart border" },
                                    "{c:WIDTH}": { "type": "number", "description": "Chart width in pixels" },
                                    "{c:HEIGHT}": { "anyOf": [{ "type": "number", "description": "Height in pixels" }, { "type": "string", "description": "Height as string (e.g., '400px', '100%')" }] },
                                    "{c:MARGIN_TOP}": { "type": "number", "description": "Fixed pixel margin between the top outer edge of the chart and the plot area" },
                                    "{c:MARGIN_RIGHT}": { "type": "number", "description": "Fixed pixel margin between the right outer edge of the chart and the plot area" },
                                    "{c:MARGIN_BOTTOM}": { "type": "number", "description": "Fixed pixel margin between the bottom outer edge of the chart and the plot area" },
                                    "{c:MARGIN_LEFT}": { "type": "number", "description": "Fixed pixel margin between the left outer edge of the chart and the plot area" },
                                    "{c:SPACING_TOP}": { "type": "number", "description": "Space between the top edge of the chart and the content" },
                                    "{c:SPACING_RIGHT}": { "type": "number", "description": "Space between the right edge of the chart and the content" },
                                    "{c:SPACING_BOTTOM}": { "type": "number", "description": "Space between the bottom edge of the chart and the content" },
                                    "{c:SPACING_LEFT}": { "type": "number", "description": "Space between the left edge of the chart and the content" },
                                    "{c:PLOT_BACKGROUND_COLOR}": { "type": "string", "description": "Background color or gradient for the plot area" },
                                    "{c:PLOT_BORDER_COLOR}": { "type": "string", "description": "Color of the inner chart or plot area border" },
                                    "{c:PLOT_BORDER_WIDTH}": { "type": "number", "description": "Pixel width of the plot area border" },
                                    "{c:INVERTED}": { "type": "boolean", "description": "Whether to invert the axes so that the x axis is vertical and y axis is horizontal" },
                                    "{c:POLAR}": { "type": "boolean", "description": "When true, cartesian charts are transformed into the polar coordinate system" },
                                    "{c:ANIMATION}": { "type": "boolean", "description": "Overall animation for all chart updating. Does not affect initial series animation." },
                                    "{c:STYLED_MODE}": { "type": "boolean", "description": "Whether to apply styled mode. When enabled, no presentational attributes or CSS are applied to the chart SVG." },
                                    "{c:ZOOM_TYPE}": { "type": "string", "description": "Decides in what dimensions the user can zoom by dragging the mouse", "enum": ["x", "y", "xy"] },
                                    "{c:OPTIONS_3D}": {
                                      "type": "object",
                                      "description": "Options to render the chart in 3D. Required for 3D charts and for the zAxis to take effect.",
                                      "properties": {
                                        "{c:ENABLED}": { "type": "boolean", "description": "Whether to render the chart using 3D functionality" },
                                        "{c:ALPHA}": { "type": "number", "description": "One of the two rotation angles for the chart" },
                                        "{c:BETA}": { "type": "number", "description": "One of the two rotation angles for the chart" },
                                        "{c:DEPTH}": { "type": "number", "description": "The total depth of the chart" },
                                        "{c:VIEW_DISTANCE}": { "type": "number", "description": "The distance the viewer is standing in front of the chart, used for perspective effect" },
                                        "{c:FRAME}": {
                                          "type": "object",
                                          "description": "Defines the 3D frame panels around the chart",
                                          "properties": {
                                            "{c:BACK}": { "type": "object", "description": "Back panel of the frame", "properties": { "{c:COLOR}": { "type": "string", "description": "The color of the panel" }, "{c:SIZE}": { "type": "number", "description": "Thickness of the panel" } } },
                                            "{c:BOTTOM}": { "type": "object", "description": "Bottom panel of the frame", "properties": { "{c:COLOR}": { "type": "string", "description": "The color of the panel" }, "{c:SIZE}": { "type": "number", "description": "Thickness of the panel" } } },
                                            "{c:SIDE}": { "type": "object", "description": "Side panel of the frame", "properties": { "{c:COLOR}": { "type": "string", "description": "The color of the panel" }, "{c:SIZE}": { "type": "number", "description": "Thickness of the panel" } } },
                                            "{c:TOP}": { "type": "object", "description": "Top panel of the frame", "properties": { "{c:COLOR}": { "type": "string", "description": "The color of the panel" }, "{c:SIZE}": { "type": "number", "description": "Thickness of the panel" } } }
                                          }
                                        }
                                      }
                                    }
                                  }
                                },
                                "{c:TITLE}": {
                                  "anyOf": [
                                    { "type": "string", "description": "Title text" },
                                    { "type": "object", "properties": { "{c:TEXT}": { "type": "string", "description": "The title text" } } }
                                  ]
                                },
                                "{c:SUBTITLE}": {
                                  "anyOf": [
                                    { "type": "string", "description": "Subtitle text" },
                                    { "type": "object", "properties": { "{c:TEXT}": { "type": "string", "description": "The subtitle text" } } }
                                  ]
                                },
                                "{c:X_AXIS}": {
                                  "type": "object",
                                  "description": "X-axis configuration",
                                  "properties": {
                                    "{c:TYPE}": { "type": "string", "description": "Axis type", "enum": ["linear", "logarithmic", "datetime", "category"] },
                                    "{c:TITLE}": { "type": "object", "properties": { "{c:TEXT}": { "type": "string", "description": "The axis title text" } } },
                                    "{c:CATEGORIES}": { "type": "array", "items": { "type": "string" }, "description": "Category names to use instead of numbers for the axis" },
                                    "{c:MIN}": { "type": "number", "description": "Minimum value of the axis. Auto-calculated if null." },
                                    "{c:MAX}": { "type": "number", "description": "Maximum value of the axis. Auto-calculated if null." }
                                  }
                                },
                                "{c:Y_AXIS}": {
                                  "anyOf": [
                                    {
                                      "type": "object",
                                      "description": "Single Y-axis configuration",
                                      "properties": {
                                        "{c:TYPE}": { "type": "string", "description": "Axis type", "enum": ["linear", "logarithmic", "datetime", "category"] },
                                        "{c:TITLE}": { "type": "object", "properties": { "{c:TEXT}": { "type": "string", "description": "The axis title text" } } },
                                        "{c:MIN}": { "type": "number", "description": "Minimum value of the axis. Auto-calculated if null." },
                                        "{c:MAX}": { "type": "number", "description": "Maximum value of the axis. Auto-calculated if null." }
                                      }
                                    },
                                    {
                                      "type": "array",
                                      "description": "Multiple Y-axes for dual-axis charts. First element is primary, second is secondary.",
                                      "items": {
                                        "type": "object",
                                        "properties": {
                                          "{c:TYPE}": { "type": "string", "description": "Axis type", "enum": ["linear", "logarithmic", "datetime", "category"] },
                                          "{c:TITLE}": { "type": "object", "properties": { "{c:TEXT}": { "type": "string", "description": "The axis title text" } } },
                                          "{c:MIN}": { "type": "number", "description": "Minimum value of the axis. Auto-calculated if null." },
                                          "{c:MAX}": { "type": "number", "description": "Maximum value of the axis. Auto-calculated if null." },
                                          "{c:OPPOSITE}": { "type": "boolean", "description": "Whether to display the axis on the opposite side of the normal (right for vertical axes, top for horizontal)" }
                                        }
                                      }
                                    }
                                  ]
                                },
                                "{c:Z_AXIS}": {
                                  "type": "object",
                                  "description": "Z-axis configuration (for 3D charts)",
                                  "properties": {
                                    "{c:TYPE}": { "type": "string", "description": "Axis type", "enum": ["linear", "logarithmic", "datetime", "category"] },
                                    "{c:TITLE}": { "type": "object", "properties": { "{c:TEXT}": { "type": "string", "description": "The axis title text" } } },
                                    "{c:MIN}": { "type": "number", "description": "Minimum value of the axis. Auto-calculated if null." },
                                    "{c:MAX}": { "type": "number", "description": "Maximum value of the axis. Auto-calculated if null." }
                                  }
                                },
                                "{c:COLOR_AXIS}": {
                                  "type": "object",
                                  "description": "Color axis for heatmaps",
                                  "properties": {
                                    "{c:MIN}": { "type": "number", "description": "Minimum value of the color axis" },
                                    "{c:MAX}": { "type": "number", "description": "Maximum value of the color axis" },
                                    "{c:MIN_COLOR}": { "type": "string", "description": "Color to represent the minimum value" },
                                    "{c:MAX_COLOR}": { "type": "string", "description": "Color to represent the maximum value" }
                                  }
                                },
                                "{c:TOOLTIP}": {
                                  "type": "object",
                                  "description": "Tooltip configuration",
                                  "properties": {
                                    "{c:POINT_FORMAT}": { "type": "string", "description": "HTML for the point's line in the tooltip. Variables like {point.y}, {series.name} are enclosed in curly brackets." },
                                    "{c:HEADER_FORMAT}": { "type": "string", "description": "HTML of the tooltip header line. Variables like {point.key}, {series.name} are enclosed in curly brackets." },
                                    "{c:SHARED}": { "type": "boolean", "description": "When true, the entire plot area captures mouse movement and tooltip texts for all series are shown in a single bubble" },
                                    "{c:VALUE_SUFFIX}": { "type": "string", "description": "A string to append to each series' y value in the tooltip" },
                                    "{c:VALUE_PREFIX}": { "type": "string", "description": "A string to prepend to each series' y value in the tooltip" }
                                  }
                                },
                                "{c:LEGEND}": {
                                  "type": "object",
                                  "description": "Legend configuration",
                                  "properties": {
                                    "{c:ENABLED}": { "type": "boolean", "description": "Enable or disable the legend" },
                                    "{c:ALIGN}": { "type": "string", "description": "Horizontal alignment of the legend box within the chart area", "enum": ["left", "center", "right"] },
                                    "{c:VERTICAL_ALIGN}": { "type": "string", "description": "Vertical alignment of the legend box", "enum": ["top", "middle", "bottom"] },
                                    "{c:LAYOUT}": { "type": "string", "description": "The layout of the legend items", "enum": ["horizontal", "vertical"] }
                                  }
                                },
                                "{c:CREDITS}": {
                                  "type": "object",
                                  "description": "Credits configuration",
                                  "properties": {
                                    "{c:ENABLED}": { "type": "boolean", "description": "Whether to show the credits text" },
                                    "{c:TEXT}": { "type": "string", "description": "The text for the credits label" },
                                    "{c:HREF}": { "type": "string", "description": "The URL for the credits label" }
                                  }
                                },
                                "{c:PANE}": {
                                  "type": "object",
                                  "description": "Pane configuration (for gauges and polar charts)",
                                  "properties": {
                                    "{c:START_ANGLE}": { "type": "number", "description": "Start angle of the polar X axis or gauge axis in degrees where 0 is north" },
                                    "{c:END_ANGLE}": { "type": "number", "description": "End angle of the polar X axis or gauge value axis in degrees where 0 is north" },
                                    "{c:CENTER}": { "type": "array", "items": { "type": "string" }, "description": "Center position ['50%', '50%']" },
                                    "{c:SIZE}": { "type": "string", "description": "Size (e.g., '100%')" }
                                  }
                                },
                                "{c:PLOT_OPTIONS}": {
                                  "type": "object",
                                  "description": "Default options for series types. Use 'series' key for options applying to all series, or a chart type key (e.g. 'pie', 'column', 'line') for type-specific defaults. Call get_plot_options_schema(chartType) to discover available properties.",
                                  "additionalProperties": { "type": "object" }
                                },
                                "{c:SERIES}": {
                                  "type": "array",
                                  "description": "Per-series configuration. Each entry must have a 'name' matching the series name. Only include properties you want to change. Call get_plot_options_schema(chartType) for available properties.",
                                  "items": {
                                    "type": "object",
                                    "properties": {
                                      "{c:NAME}": { "type": "string", "description": "Series name to configure" },
                                      "{c:TYPE}": { "type": "string", "description": "Chart type override (e.g. 'column', 'line', 'areaspline')" },
                                      "{c:Y_AXIS}": { "type": "integer", "description": "Y-axis index (0=primary, 1=secondary)" },
                                      "{c:PLOT_OPTIONS}": { "type": "object", "description": "Series-specific styling. Call get_plot_options_schema(chartType) for available properties.", "additionalProperties": true }
                                    },
                                    "required": ["{c:NAME}"]
                                  }
                                }
                              }
                            }
                          },
                          "required": ["configuration"]
                        }""";

                return resolveConfigKeys(template);
            }

            @Override
            public String execute(JsonNode arguments) {
                try {
                    LOGGER.info("update_chart_configuration called with: {}",
                            arguments);
                    String chartId = resolveChartId(arguments, callbacks);

                    JsonNode configNode = arguments.get("configuration");
                    if (configNode == null || configNode.isNull()) {
                        return "Error updating chart configuration: 'configuration' parameter is required.";
                    }
                    callbacks.updateConfiguration(chartId,
                            configNode.toString());

                    return "Chart '" + chartId
                            + "' configuration updated. Changes will be applied when the request completes.";
                } catch (ValidationException e) {
                    LOGGER.warn("update_chart_configuration validation failed",
                            e);
                    return "Error updating chart configuration: "
                            + e.getMessage();
                } catch (Exception e) {
                    LOGGER.error("update_chart_configuration failed", e);
                    return "Error updating chart configuration.";
                }
            }
        };
    }

    public static LLMProvider.ToolSpec updateChartDataSource(
            Callbacks callbacks) {
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "update_chart_data_source";
            }

            @Override
            public String getDescription() {
                return resolveColumnNames(
                        """
                                Updates the chart data using SQL SELECT queries (one per series).

                                IMPORTANT: Column names control how data is mapped to series. \
                                Use the exact aliases below (prefixed with '{PREFIX}') in your SELECT statements.

                                Multi-series: add a {SERIES} column to group rows into separate named series. \
                                It is removed before chart type detection.

                                Basic charts (line, bar, column, area, spline, pie):
                                - 2 columns: category, value (no special aliases needed)
                                - Example: SELECT month AS category, SUM(revenue) AS value FROM sales GROUP BY month

                                Scatter plot:
                                - 2 numeric columns: {X}, {Y}
                                - Example: SELECT temperature AS {X}, sales AS {Y} FROM data

                                Bubble chart:
                                - 3 numeric columns: {X}, {Y}, {Z}
                                - Example: SELECT gdp AS {X}, life_exp AS {Y}, population AS {Z} FROM countries

                                Bullet chart:
                                - Columns: {Y}, {TARGET} (optionally {X})
                                - Example: SELECT revenue AS {Y}, goal AS {TARGET} FROM sales

                                Range charts (arearange, columnrange, areasplinerange):
                                - Columns: {LOW}, {HIGH} (optionally {X})
                                - Example: SELECT temp_low AS {LOW}, temp_high AS {HIGH} FROM weather

                                BoxPlot:
                                - 5 columns: {LOW}, {Q1}, {MEDIAN}, {Q3}, {HIGH}
                                - Example: SELECT min_val AS {LOW}, lower_q AS {Q1}, med AS {MEDIAN}, upper_q AS {Q3}, max_val AS {HIGH} FROM stats

                                OHLC/Candlestick:
                                - Columns: {X}, {OPEN}, {HIGH}, {LOW}, {CLOSE} ({X} is required for proper date axis)
                                - Example: SELECT date AS {X}, open AS {OPEN}, high AS {HIGH}, low AS {LOW}, close AS {CLOSE} FROM stock_prices
                                - When adding a volume series alongside OHLC/candlestick data, use a separate query \
                                with {X}, {Y}, and {SERIES} aliases (e.g. SELECT date AS {X}, volume AS {Y}, 'Volume' AS {SERIES} \
                                FROM stock_prices). The {SERIES} alias names the series so it can be configured via \
                                update_chart_configuration() with type "column" and yAxis 1 on a dual y-axis setup.

                                Sankey diagram:
                                - 3 columns: {FROM}, {TO}, {WEIGHT}
                                - Example: SELECT source AS {FROM}, destination AS {TO}, flow AS {WEIGHT} FROM energy_flow

                                Xrange:
                                - 3 columns: {X}, {X2}, {Y}
                                - Example: SELECT start_ts AS {X}, end_ts AS {X2}, task_id AS {Y} FROM tasks

                                Gantt:
                                - Columns: {NAME}, {START}, {END} (optionally {ID}, {PARENT}, {DEPENDENCY}, {COMPLETED}, {COLOR})
                                - {START} and {END} must be date/timestamp values
                                - Example: SELECT task AS {NAME}, start_date AS {START}, end_date AS {END} FROM project_tasks

                                Timeline:
                                - Columns: {NAME}, {LABEL}, {DESCRIPTION} (optionally {X})
                                - Example: SELECT event AS {NAME}, short AS {LABEL}, detail AS {DESCRIPTION} FROM events

                                Flags:
                                - Columns: {TITLE} (optionally {TEXT}, {X})
                                - Example: SELECT flag_title AS {TITLE}, note AS {TEXT} FROM flags

                                Organization:
                                - Columns: {ID}, {NAME}, {PARENT}, {TITLE} (optionally {DESCRIPTION}, {IMAGE}, {COLOR})
                                - Example: SELECT emp_id AS {ID}, full_name AS {NAME}, mgr_id AS {PARENT}, role AS {TITLE} FROM employees

                                Treemap:
                                - Columns: {ID}, {PARENT}, {VALUE} (optionally {NAME}, {COLOR_VALUE})
                                - Example: SELECT cat_id AS {ID}, parent_id AS {PARENT}, amount AS {VALUE} FROM categories

                                Heatmap:
                                - 3 columns: {X}, {Y}, {VALUE}
                                - Example: SELECT day AS {X}, hour AS {Y}, count AS {VALUE} FROM activity

                                Waterfall:
                                - Columns: {Y}, {WATERFALL_TYPE} (optionally {NAME}); type values: 'sum', 'intermediate', or null for data points
                                - Example: SELECT label AS {NAME}, amount AS {Y}, wf_type AS {WATERFALL_TYPE} FROM waterfall_data

                                Optional: any pattern supporting DataSeriesItem accepts a {COLOR} column for per-point coloring.

                                Parameters:
                                - chartId (string, optional): The ID of the chart to update. Required when multiple charts exist.
                                - queries (array of strings, required): SQL SELECT queries, one per series

                                Changes are applied when the request completes.""");
            }

            @Override
            public String getParametersSchema() {
                return """
                        {
                          "type": "object",
                          "properties": {
                            "chartId": {
                              "type": "string",
                              "description": "The ID of the chart. Optional when there is only one chart."
                            },
                            "queries": {
                              "type": "array",
                              "items": { "type": "string" },
                              "description": "SQL queries to execute against the database, one per chart series"
                            }
                          },
                          "required": ["queries"]
                        }""";
            }

            @Override
            public String execute(JsonNode arguments) {
                try {
                    LOGGER.info("update_chart_data_source called with: {}",
                            arguments);
                    String chartId = resolveChartId(arguments, callbacks);

                    List<String> queries = new ArrayList<>();
                    String validationError = validateQueries(
                            arguments.get("queries"), queries);
                    if (validationError != null) {
                        return validationError;
                    }

                    LOGGER.info(
                            "update_chart_data_source chartId={} queries={}",
                            chartId, queries);
                    callbacks.updateData(chartId, queries);

                    return "Chart '" + chartId
                            + "' data source updated. Changes will be applied when the request completes.";
                } catch (ValidationException e) {
                    LOGGER.warn("update_chart_data_source validation failed",
                            e);
                    return "Error updating chart data: " + e.getMessage();
                } catch (Exception e) {
                    LOGGER.error("update_chart_data_source failed", e);
                    return "Error updating chart data.";
                }
            }
        };
    }

    /**
     * Tool that returns the JSON schema for a specific chart type's plot
     * options. Stateless — no callbacks needed.
     */
    public static LLMProvider.ToolSpec getPlotOptionsSchema() {
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "get_plot_options_schema";
            }

            @Override
            public String getDescription() {
                return """
                        Returns the JSON schema for plot options of a specific chart type.
                        Use this to discover available styling properties (dataLabels, stacking, marker, lineWidth, etc.)
                        before setting plotOptions or seriesOptions in update_chart_configuration.
                        Use 'series' as the type for base options that apply to all chart types.""";
            }

            @Override
            public String getParametersSchema() {
                return """
                        {
                          "type": "object",
                          "properties": {
                            "chartType": {
                              "type": "string",
                              "description": "The chart type (e.g. 'column', 'line', 'bar', 'area', 'pie', 'series')"
                            }
                          },
                          "required": ["chartType"]
                        }""";
            }

            @Override
            public String execute(JsonNode arguments) {
                try {
                    LOGGER.info("get_plot_options_schema called with: {}",
                            arguments);
                    JsonNode typeNode = arguments.get("chartType");
                    if (typeNode == null || typeNode.isNull()) {
                        return "Error: 'chartType' parameter is required.";
                    }
                    String chartType = typeNode.asString();
                    String schema = PlotOptionsSchema.getSchema(chartType);
                    if (schema == null) {
                        return "Error: unknown chart type '" + chartType
                                + "'. Supported types: " + String.join(", ",
                                        PlotOptionsSchema.supportedTypes());
                    }
                    return schema;
                } catch (Exception e) {
                    LOGGER.error("get_plot_options_schema failed", e);
                    return "Error getting plot options schema.";
                }
            }
        };
    }
}
