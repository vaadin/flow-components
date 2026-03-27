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

import static com.vaadin.flow.component.ai.chart.ColumnNames.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.internal.JacksonUtils;

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

    // @formatter:off
    private static final Map<String, String> COLUMN_PARAMS = Map.ofEntries(
            Map.entry("{PREFIX}",         PREFIX),
            Map.entry("{SERIES}",         SERIES),
            Map.entry("{X}",              X),
            Map.entry("{Y}",              Y),
            Map.entry("{Z}",              Z),
            Map.entry("{X2}",             X2),
            Map.entry("{NAME}",           NAME),
            Map.entry("{ID}",             ID),
            Map.entry("{PARENT}",         PARENT),
            Map.entry("{VALUE}",          VALUE),
            Map.entry("{COLOR}",          COLOR),
            Map.entry("{COLOR_VALUE}",    COLOR_VALUE),
            Map.entry("{OPEN}",           OPEN),
            Map.entry("{HIGH}",           HIGH),
            Map.entry("{LOW}",            LOW),
            Map.entry("{CLOSE}",          CLOSE),
            Map.entry("{Q1}",             Q1),
            Map.entry("{MEDIAN}",         MEDIAN),
            Map.entry("{Q3}",             Q3),
            Map.entry("{FROM}",           FROM),
            Map.entry("{TO}",             TO),
            Map.entry("{WEIGHT}",         WEIGHT),
            Map.entry("{LABEL}",          LABEL),
            Map.entry("{DESCRIPTION}",    DESCRIPTION),
            Map.entry("{TITLE}",          TITLE),
            Map.entry("{TEXT}",           TEXT),
            Map.entry("{START}",          START),
            Map.entry("{END}",            END),
            Map.entry("{DEPENDENCY}",     DEPENDENCY),
            Map.entry("{COMPLETED}",      COMPLETED),
            Map.entry("{TARGET}",         TARGET),
            Map.entry("{WATERFALL_TYPE}", WATERFALL_TYPE),
            Map.entry("{IMAGE}",          IMAGE)
    );
    // @formatter:on

    /**
     * Replaces {@code {NAME}} placeholders in the template with the
     * corresponding {@link ColumnNames} constant values.
     */
    private static String resolveColumnNames(String template) {
        return resolvePlaceholders(template, COLUMN_PARAMS);
    }

    // @formatter:off
    private static final Map<String, String> CONFIG_PARAMS = Map.ofEntries(
            // Top-level sections
            Map.entry("{c:CHART}",                ConfigurationKeys.CHART),
            Map.entry("{c:TITLE}",                ConfigurationKeys.TITLE),
            Map.entry("{c:SUBTITLE}",             ConfigurationKeys.SUBTITLE),
            Map.entry("{c:TOOLTIP}",              ConfigurationKeys.TOOLTIP),
            Map.entry("{c:LEGEND}",               ConfigurationKeys.LEGEND),
            Map.entry("{c:X_AXIS}",               ConfigurationKeys.X_AXIS),
            Map.entry("{c:Y_AXIS}",               ConfigurationKeys.Y_AXIS),
            Map.entry("{c:Z_AXIS}",               ConfigurationKeys.Z_AXIS),
            Map.entry("{c:COLOR_AXIS}",           ConfigurationKeys.COLOR_AXIS),
            Map.entry("{c:CREDITS}",              ConfigurationKeys.CREDITS),
            Map.entry("{c:PANE}",                 ConfigurationKeys.PANE),
            Map.entry("{c:PLOT_OPTIONS}",         ConfigurationKeys.PLOT_OPTIONS),
            // Common
            Map.entry("{c:TYPE}",                 ConfigurationKeys.TYPE),
            Map.entry("{c:TEXT}",                 ConfigurationKeys.TEXT),
            Map.entry("{c:ENABLED}",              ConfigurationKeys.ENABLED),
            Map.entry("{c:MIN}",                  ConfigurationKeys.MIN),
            Map.entry("{c:MAX}",                  ConfigurationKeys.MAX),
            Map.entry("{c:FORMAT}",               ConfigurationKeys.FORMAT),
            // Chart model
            Map.entry("{c:BACKGROUND_COLOR}",     ConfigurationKeys.BACKGROUND_COLOR),
            Map.entry("{c:BORDER_COLOR}",         ConfigurationKeys.BORDER_COLOR),
            Map.entry("{c:BORDER_WIDTH}",         ConfigurationKeys.BORDER_WIDTH),
            Map.entry("{c:BORDER_RADIUS}",        ConfigurationKeys.BORDER_RADIUS),
            Map.entry("{c:WIDTH}",                ConfigurationKeys.WIDTH),
            Map.entry("{c:HEIGHT}",               ConfigurationKeys.HEIGHT),
            Map.entry("{c:MARGIN_TOP}",           ConfigurationKeys.MARGIN_TOP),
            Map.entry("{c:MARGIN_RIGHT}",         ConfigurationKeys.MARGIN_RIGHT),
            Map.entry("{c:MARGIN_BOTTOM}",        ConfigurationKeys.MARGIN_BOTTOM),
            Map.entry("{c:MARGIN_LEFT}",          ConfigurationKeys.MARGIN_LEFT),
            Map.entry("{c:SPACING_TOP}",          ConfigurationKeys.SPACING_TOP),
            Map.entry("{c:SPACING_RIGHT}",        ConfigurationKeys.SPACING_RIGHT),
            Map.entry("{c:SPACING_BOTTOM}",       ConfigurationKeys.SPACING_BOTTOM),
            Map.entry("{c:SPACING_LEFT}",         ConfigurationKeys.SPACING_LEFT),
            Map.entry("{c:PLOT_BACKGROUND_COLOR}", ConfigurationKeys.PLOT_BACKGROUND_COLOR),
            Map.entry("{c:PLOT_BORDER_COLOR}",    ConfigurationKeys.PLOT_BORDER_COLOR),
            Map.entry("{c:PLOT_BORDER_WIDTH}",    ConfigurationKeys.PLOT_BORDER_WIDTH),
            Map.entry("{c:INVERTED}",             ConfigurationKeys.INVERTED),
            Map.entry("{c:POLAR}",                ConfigurationKeys.POLAR),
            Map.entry("{c:ANIMATION}",            ConfigurationKeys.ANIMATION),
            Map.entry("{c:STYLED_MODE}",          ConfigurationKeys.STYLED_MODE),
            Map.entry("{c:ZOOM_TYPE}",            ConfigurationKeys.ZOOM_TYPE),
            // Tooltip
            Map.entry("{c:POINT_FORMAT}",         ConfigurationKeys.POINT_FORMAT),
            Map.entry("{c:HEADER_FORMAT}",        ConfigurationKeys.HEADER_FORMAT),
            Map.entry("{c:SHARED}",               ConfigurationKeys.SHARED),
            Map.entry("{c:VALUE_SUFFIX}",         ConfigurationKeys.VALUE_SUFFIX),
            Map.entry("{c:VALUE_PREFIX}",         ConfigurationKeys.VALUE_PREFIX),
            // Legend
            Map.entry("{c:ALIGN}",               ConfigurationKeys.ALIGN),
            Map.entry("{c:VERTICAL_ALIGN}",      ConfigurationKeys.VERTICAL_ALIGN),
            Map.entry("{c:LAYOUT}",              ConfigurationKeys.LAYOUT),
            // Axis
            Map.entry("{c:CATEGORIES}",          ConfigurationKeys.CATEGORIES),
            // Color axis
            Map.entry("{c:MIN_COLOR}",           ConfigurationKeys.MIN_COLOR),
            Map.entry("{c:MAX_COLOR}",           ConfigurationKeys.MAX_COLOR),
            // Credits
            Map.entry("{c:HREF}",                ConfigurationKeys.HREF),
            // Pane
            Map.entry("{c:START_ANGLE}",         ConfigurationKeys.START_ANGLE),
            Map.entry("{c:END_ANGLE}",           ConfigurationKeys.END_ANGLE),
            Map.entry("{c:CENTER}",              ConfigurationKeys.CENTER),
            Map.entry("{c:SIZE}",                ConfigurationKeys.SIZE),
            // Plot options
            Map.entry("{c:SERIES}",              ConfigurationKeys.SERIES),
            Map.entry("{c:PIE}",                 ConfigurationKeys.PIE),
            Map.entry("{c:COLUMN}",              ConfigurationKeys.COLUMN),
            Map.entry("{c:BAR}",                 ConfigurationKeys.BAR),
            Map.entry("{c:STACKING}",            ConfigurationKeys.STACKING),
            Map.entry("{c:DATA_LABELS}",         ConfigurationKeys.DATA_LABELS),
            Map.entry("{c:MARKER}",              ConfigurationKeys.MARKER),
            Map.entry("{c:INNER_SIZE}",          ConfigurationKeys.INNER_SIZE)
    );
    // @formatter:on

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
     * Resolves the chart ID from the tool arguments. If {@code chartId} is not
     * provided and there is exactly one chart, that chart's ID is used as the
     * default.
     */
    private static String resolveChartId(JsonNode args, Callbacks callbacks) {
        JsonNode idNode = args.get("chartId");
        if (idNode != null && !idNode.isNull()) {
            return idNode.asString();
        }
        var ids = callbacks.getChartIds();
        if (ids.size() == 1) {
            return ids.iterator().next();
        }
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("No charts available.");
        }
        throw new IllegalArgumentException(
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
                updateChartDataSource(callbacks));
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
            public String execute(String arguments) {
                try {
                    LOGGER.info("get_chart_state called");
                    JsonNode args = JacksonUtils.readTree(arguments);
                    String chartId = resolveChartId(args, callbacks);
                    return callbacks.getState(chartId);
                } catch (Exception e) {
                    LOGGER.error("get_chart_state failed", e);
                    return "Error getting chart state: " + e.getMessage();
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
                        Updates the Highcharts configuration of a chart. The configuration \
                        object follows the same structure as the Highcharts options object \
                        (as returned by get_chart_state), supporting: chart (type, dimensions, \
                        margins, spacing, borders, background, inverted, polar, animation, \
                        zoomType), title, subtitle, xAxis, yAxis, zAxis, colorAxis, tooltip, \
                        legend, plotOptions (series defaults, stacking, dataLabels, markers, \
                        pie innerSize for donuts), credits, and pane.

                        CRITICAL: ALWAYS specify the chart type in configuration.chart.type - this is essential for proper rendering.

                        IMPORTANT: Do NOT include 'series' in the configuration - chart data is managed separately via update_chart_data_source tool.

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
                                    "{c:BACKGROUND_COLOR}": { "type": "string", "description": "Background color (e.g., '#ffffff')" },
                                    "{c:BORDER_COLOR}": { "type": "string", "description": "Border color" },
                                    "{c:BORDER_WIDTH}": { "type": "number", "description": "Border width in pixels" },
                                    "{c:BORDER_RADIUS}": { "type": "number", "description": "Border radius in pixels" },
                                    "{c:WIDTH}": { "type": "number", "description": "Chart width in pixels" },
                                    "{c:HEIGHT}": { "oneOf": [{ "type": "number", "description": "Height in pixels" }, { "type": "string", "description": "Height as string (e.g., '400px', '100%')" }] },
                                    "{c:MARGIN_TOP}": { "type": "number" },
                                    "{c:MARGIN_RIGHT}": { "type": "number" },
                                    "{c:MARGIN_BOTTOM}": { "type": "number" },
                                    "{c:MARGIN_LEFT}": { "type": "number" },
                                    "{c:SPACING_TOP}": { "type": "number" },
                                    "{c:SPACING_RIGHT}": { "type": "number" },
                                    "{c:SPACING_BOTTOM}": { "type": "number" },
                                    "{c:SPACING_LEFT}": { "type": "number" },
                                    "{c:PLOT_BACKGROUND_COLOR}": { "type": "string" },
                                    "{c:PLOT_BORDER_COLOR}": { "type": "string" },
                                    "{c:PLOT_BORDER_WIDTH}": { "type": "number" },
                                    "{c:INVERTED}": { "type": "boolean", "description": "Invert axes" },
                                    "{c:POLAR}": { "type": "boolean", "description": "Polar chart" },
                                    "{c:ANIMATION}": { "type": "boolean" },
                                    "{c:STYLED_MODE}": { "type": "boolean" },
                                    "{c:ZOOM_TYPE}": { "type": "string", "enum": ["x", "y", "xy"] }
                                  }
                                },
                                "{c:TITLE}": {
                                  "oneOf": [
                                    { "type": "string", "description": "Title text" },
                                    { "type": "object", "properties": { "{c:TEXT}": { "type": "string" } } }
                                  ]
                                },
                                "{c:SUBTITLE}": {
                                  "oneOf": [
                                    { "type": "string", "description": "Subtitle text" },
                                    { "type": "object", "properties": { "{c:TEXT}": { "type": "string" } } }
                                  ]
                                },
                                "{c:X_AXIS}": {
                                  "type": "object",
                                  "description": "X-axis configuration",
                                  "properties": {
                                    "{c:TYPE}": { "type": "string", "description": "Axis type", "enum": ["linear", "logarithmic", "datetime", "category"] },
                                    "{c:TITLE}": { "type": "object", "properties": { "{c:TEXT}": { "type": "string" } } },
                                    "{c:CATEGORIES}": { "type": "array", "items": { "type": "string" } },
                                    "{c:MIN}": { "type": "number" },
                                    "{c:MAX}": { "type": "number" }
                                  }
                                },
                                "{c:Y_AXIS}": {
                                  "type": "object",
                                  "description": "Y-axis configuration",
                                  "properties": {
                                    "{c:TYPE}": { "type": "string", "description": "Axis type", "enum": ["linear", "logarithmic", "datetime", "category"] },
                                    "{c:TITLE}": { "type": "object", "properties": { "{c:TEXT}": { "type": "string" } } },
                                    "{c:MIN}": { "type": "number" },
                                    "{c:MAX}": { "type": "number" }
                                  }
                                },
                                "{c:Z_AXIS}": {
                                  "type": "object",
                                  "description": "Z-axis configuration (for 3D and bubble charts)",
                                  "properties": {
                                    "{c:TYPE}": { "type": "string", "description": "Axis type", "enum": ["linear", "logarithmic", "datetime", "category"] },
                                    "{c:TITLE}": { "type": "object", "properties": { "{c:TEXT}": { "type": "string" } } },
                                    "{c:MIN}": { "type": "number" },
                                    "{c:MAX}": { "type": "number" }
                                  }
                                },
                                "{c:COLOR_AXIS}": {
                                  "type": "object",
                                  "description": "Color axis for heatmaps",
                                  "properties": {
                                    "{c:MIN}": { "type": "number" },
                                    "{c:MAX}": { "type": "number" },
                                    "{c:MIN_COLOR}": { "type": "string" },
                                    "{c:MAX_COLOR}": { "type": "string" }
                                  }
                                },
                                "{c:TOOLTIP}": {
                                  "type": "object",
                                  "description": "Tooltip configuration",
                                  "properties": {
                                    "{c:POINT_FORMAT}": { "type": "string" },
                                    "{c:HEADER_FORMAT}": { "type": "string" },
                                    "{c:SHARED}": { "type": "boolean" },
                                    "{c:VALUE_SUFFIX}": { "type": "string" },
                                    "{c:VALUE_PREFIX}": { "type": "string" }
                                  }
                                },
                                "{c:LEGEND}": {
                                  "type": "object",
                                  "description": "Legend configuration",
                                  "properties": {
                                    "{c:ENABLED}": { "type": "boolean" },
                                    "{c:ALIGN}": { "type": "string", "enum": ["left", "center", "right"] },
                                    "{c:VERTICAL_ALIGN}": { "type": "string", "enum": ["top", "middle", "bottom"] },
                                    "{c:LAYOUT}": { "type": "string", "enum": ["horizontal", "vertical"] }
                                  }
                                },
                                "{c:CREDITS}": {
                                  "type": "object",
                                  "description": "Credits configuration",
                                  "properties": {
                                    "{c:ENABLED}": { "type": "boolean" },
                                    "{c:TEXT}": { "type": "string" },
                                    "{c:HREF}": { "type": "string" }
                                  }
                                },
                                "{c:PANE}": {
                                  "type": "object",
                                  "description": "Pane configuration (for gauges and polar charts)",
                                  "properties": {
                                    "{c:START_ANGLE}": { "type": "number" },
                                    "{c:END_ANGLE}": { "type": "number" },
                                    "{c:CENTER}": { "type": "array", "items": { "type": "string" }, "description": "Center position ['50%', '50%']" },
                                    "{c:SIZE}": { "type": "string", "description": "Size (e.g., '100%')" }
                                  }
                                },
                                "{c:PLOT_OPTIONS}": {
                                  "type": "object",
                                  "description": "Default options for series types. Use 'series' key for options applying to all series, or a chart type key (e.g., 'pie', 'column') for type-specific options.",
                                  "properties": {
                                    "{c:SERIES}": {
                                      "type": "object",
                                      "description": "Default options for all series types",
                                      "properties": {
                                        "{c:STACKING}": { "type": "string", "enum": ["normal", "percent"], "description": "Stack series by value or percentage" },
                                        "{c:DATA_LABELS}": {
                                          "type": "object",
                                          "properties": {
                                            "{c:ENABLED}": { "type": "boolean" },
                                            "{c:FORMAT}": { "type": "string", "description": "Label format string" }
                                          }
                                        },
                                        "{c:MARKER}": {
                                          "type": "object",
                                          "properties": {
                                            "{c:ENABLED}": { "type": "boolean" }
                                          }
                                        }
                                      }
                                    },
                                    "{c:PIE}": {
                                      "type": "object",
                                      "description": "Default options for pie series",
                                      "properties": {
                                        "{c:INNER_SIZE}": { "type": "string", "description": "Inner diameter for donut charts (e.g., '50%')" },
                                        "{c:DATA_LABELS}": {
                                          "type": "object",
                                          "properties": {
                                            "{c:ENABLED}": { "type": "boolean" },
                                            "{c:FORMAT}": { "type": "string" }
                                          }
                                        }
                                      }
                                    },
                                    "{c:COLUMN}": {
                                      "type": "object",
                                      "description": "Default options for column series",
                                      "properties": {
                                        "{c:STACKING}": { "type": "string", "enum": ["normal", "percent"] },
                                        "{c:DATA_LABELS}": {
                                          "type": "object",
                                          "properties": {
                                            "{c:ENABLED}": { "type": "boolean" },
                                            "{c:FORMAT}": { "type": "string" }
                                          }
                                        },
                                        "{c:BORDER_RADIUS}": { "type": "number", "description": "Border radius for column corners" }
                                      }
                                    },
                                    "{c:BAR}": {
                                      "type": "object",
                                      "description": "Default options for bar series",
                                      "properties": {
                                        "{c:STACKING}": { "type": "string", "enum": ["normal", "percent"] },
                                        "{c:DATA_LABELS}": {
                                          "type": "object",
                                          "properties": {
                                            "{c:ENABLED}": { "type": "boolean" },
                                            "{c:FORMAT}": { "type": "string" }
                                          }
                                        },
                                        "{c:BORDER_RADIUS}": { "type": "number" }
                                      }
                                    }
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
            public String execute(String arguments) {
                try {
                    LOGGER.info("update_chart_configuration called with: {}",
                            arguments);
                    JsonNode args = JacksonUtils.readTree(arguments);
                    String chartId = resolveChartId(args, callbacks);

                    JsonNode configNode = args.get("configuration");
                    if (configNode == null || configNode.isNull()) {
                        return "Error updating chart configuration: 'configuration' parameter is required.";
                    }
                    callbacks.updateConfiguration(chartId,
                            configNode.toString());

                    return "Chart '" + chartId
                            + "' configuration updated. Changes will be applied when the request completes.";
                } catch (Exception e) {
                    LOGGER.error("update_chart_configuration failed", e);
                    return "Error updating chart configuration: "
                            + e.getMessage();
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
            public String execute(String arguments) {
                try {
                    LOGGER.info("update_chart_data_source called with: {}",
                            arguments);
                    JsonNode args = JacksonUtils.readTree(arguments);
                    String chartId = resolveChartId(args, callbacks);

                    List<String> queries = new ArrayList<>();
                    String validationError = validateQueries(
                            args.get("queries"), queries);
                    if (validationError != null) {
                        return validationError;
                    }

                    LOGGER.info(
                            "update_chart_data_source chartId={} queries={}",
                            chartId, queries);
                    callbacks.updateData(chartId, queries);

                    return "Chart '" + chartId
                            + "' data source updated. Changes will be applied when the request completes.";
                } catch (Exception e) {
                    LOGGER.error("update_chart_data_source failed", e);
                    return "Error updating chart data: " + e.getMessage();
                }
            }
        };
    }
}
