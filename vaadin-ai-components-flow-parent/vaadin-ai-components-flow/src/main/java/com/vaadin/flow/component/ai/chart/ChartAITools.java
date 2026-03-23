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

import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;

/**
 * Factory for creating reusable chart {@link LLMProvider.ToolSpec} instances.
 * These tools are not tied to any specific controller and can be used by both
 * {@link ChartAIController} and {@code DashboardAIController}.
 * <p>
 * The tools use a {@code chartId} parameter to identify which chart to operate
 * on, allowing a single set of tools to manage multiple charts (e.g., in a
 * dashboard). Callers provide a {@link Callbacks} implementation for state
 * retrieval and mutation, keeping this class decoupled from {@code Chart} and
 * {@code ChartEntry}.
 * </p>
 *
 * @author Vaadin Ltd
 */
public final class ChartAITools {

    /**
     * Callback interface that chart tool consumers must implement to provide
     * chart state access and mutation operations.
     */
    public interface Callbacks extends Serializable {

        /**
         * Returns the current state of the chart as a JSON string suitable for
         * LLM tool responses. Should throw if the chart is not found.
         *
         * @param chartId
         *            the chart ID
         * @return the chart state as a JSON string
         */
        String getState(String chartId);

        /**
         * Updates the chart's pending configuration. Should throw if the chart
         * is not found.
         *
         * @param chartId
         *            the chart ID
         * @param configJson
         *            the configuration as a JSON string
         */
        void updateConfiguration(String chartId, String configJson);

        /**
         * Validates and stores the chart's data source queries. Should throw if
         * the chart is not found or if any query is invalid.
         *
         * @param chartId
         *            the chart ID
         * @param queries
         *            the SQL queries, one per series
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
        var result = template;
        for (var entry : COLUMN_PARAMS.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
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
                    JsonNode args = JacksonUtils.readTree(arguments);
                    String chartId = resolveChartId(args, callbacks);
                    return callbacks.getState(chartId);
                } catch (Exception e) {
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
                        Updates the chart configuration (type, title, tooltip, etc.).

                        CRITICAL: ALWAYS specify the chart type in configuration.chart.type - this is essential for proper rendering.

                        IMPORTANT: Do NOT include 'series' in the configuration - chart data is managed separately via update_chart_data_source tool.

                        Parameters:
                        - chartId (string, required): The ID of the chart to update
                        - configuration (object, required): Chart configuration object

                        Changes are applied when the request completes.""";
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
                            "configuration": {
                              "type": "object",
                              "description": "Chart configuration object. CRITICAL: Always include chart.type. NOTE: Do NOT include 'series' - data is managed separately via update_chart_data_source tool.",
                              "properties": {
                                "chart": {
                                  "type": "object",
                                  "description": "Chart model options - MUST include 'type' property. Also supports dimensions, margins, spacing, borders, background",
                                  "properties": {
                                    "type": {
                                      "type": "string",
                                      "description": "REQUIRED: Chart type - ALWAYS specify this property. Must be inside chart object to match Vaadin Charts structure",
                                      "enum": ["line", "spline", "area", "areaspline", "bar", "column", "pie", "scatter", "gauge", "arearange", "columnrange", "areasplinerange", "boxplot", "errorbar", "bubble", "funnel", "waterfall", "pyramid", "solidgauge", "heatmap", "treemap", "polygon", "candlestick", "flags", "timeline", "ohlc", "organization", "sankey", "xrange", "gantt", "bullet"]
                                    },
                                    "backgroundColor": { "type": "string", "description": "Background color (e.g., '#ffffff')" },
                                    "borderColor": { "type": "string", "description": "Border color" },
                                    "borderWidth": { "type": "number", "description": "Border width in pixels" },
                                    "borderRadius": { "type": "number", "description": "Border radius in pixels" },
                                    "width": { "type": "number", "description": "Chart width in pixels" },
                                    "height": { "type": "string", "description": "Chart height (e.g., '400px', '100%')" },
                                    "marginTop": { "type": "number" },
                                    "marginRight": { "type": "number" },
                                    "marginBottom": { "type": "number" },
                                    "marginLeft": { "type": "number" },
                                    "spacingTop": { "type": "number" },
                                    "spacingRight": { "type": "number" },
                                    "spacingBottom": { "type": "number" },
                                    "spacingLeft": { "type": "number" },
                                    "plotBackgroundColor": { "type": "string" },
                                    "plotBorderColor": { "type": "string" },
                                    "plotBorderWidth": { "type": "number" },
                                    "inverted": { "type": "boolean", "description": "Invert axes" },
                                    "polar": { "type": "boolean", "description": "Polar chart" },
                                    "animation": { "type": "boolean" },
                                    "styledMode": { "type": "boolean" },
                                    "zoomType": { "type": "string", "enum": ["X", "Y", "XY"] }
                                  }
                                },
                                "title": {
                                  "oneOf": [
                                    { "type": "string", "description": "Title text" },
                                    { "type": "object", "properties": { "text": { "type": "string" } } }
                                  ]
                                },
                                "subtitle": {
                                  "oneOf": [
                                    { "type": "string", "description": "Subtitle text" },
                                    { "type": "object", "properties": { "text": { "type": "string" } } }
                                  ]
                                },
                                "xAxis": {
                                  "type": "object",
                                  "description": "X-axis configuration",
                                  "properties": {
                                    "title": { "type": "object", "properties": { "text": { "type": "string" } } },
                                    "categories": { "type": "array", "items": { "type": "string" } },
                                    "min": { "type": "number" },
                                    "max": { "type": "number" }
                                  }
                                },
                                "yAxis": {
                                  "type": "object",
                                  "description": "Y-axis configuration",
                                  "properties": {
                                    "title": { "type": "object", "properties": { "text": { "type": "string" } } },
                                    "min": { "type": "number" },
                                    "max": { "type": "number" }
                                  }
                                },
                                "zAxis": {
                                  "type": "object",
                                  "description": "Z-axis configuration (for 3D charts)",
                                  "properties": {
                                    "title": { "type": "object", "properties": { "text": { "type": "string" } } },
                                    "min": { "type": "number" },
                                    "max": { "type": "number" }
                                  }
                                },
                                "colorAxis": {
                                  "type": "object",
                                  "description": "Color axis for heatmaps",
                                  "properties": {
                                    "min": { "type": "number" },
                                    "max": { "type": "number" },
                                    "minColor": { "type": "string" },
                                    "maxColor": { "type": "string" }
                                  }
                                },
                                "tooltip": {
                                  "type": "object",
                                  "description": "Tooltip configuration",
                                  "properties": {
                                    "pointFormat": { "type": "string" },
                                    "headerFormat": { "type": "string" },
                                    "shared": { "type": "boolean" },
                                    "valueSuffix": { "type": "string" },
                                    "valuePrefix": { "type": "string" }
                                  }
                                },
                                "legend": {
                                  "type": "object",
                                  "description": "Legend configuration",
                                  "properties": {
                                    "enabled": { "type": "boolean" },
                                    "align": { "type": "string", "enum": ["LEFT", "CENTER", "RIGHT"] },
                                    "verticalAlign": { "type": "string", "enum": ["TOP", "MIDDLE", "BOTTOM"] },
                                    "layout": { "type": "string", "enum": ["HORIZONTAL", "VERTICAL"] }
                                  }
                                },
                                "credits": {
                                  "type": "object",
                                  "description": "Credits configuration",
                                  "properties": {
                                    "enabled": { "type": "boolean" },
                                    "text": { "type": "string" },
                                    "href": { "type": "string" }
                                  }
                                },
                                "pane": {
                                  "type": "object",
                                  "description": "Pane configuration (for gauges and polar charts)",
                                  "properties": {
                                    "startAngle": { "type": "number" },
                                    "endAngle": { "type": "number" },
                                    "center": { "type": "array", "items": { "type": "string" }, "description": "Center position ['50%', '50%']" },
                                    "size": { "type": "string", "description": "Size (e.g., '100%')" }
                                  }
                                },
                                "exporting": {
                                  "type": "object",
                                  "description": "Export configuration",
                                  "properties": {
                                    "enabled": { "type": "boolean" },
                                    "filename": { "type": "string" },
                                    "sourceWidth": { "type": "number" },
                                    "sourceHeight": { "type": "number" },
                                    "scale": { "type": "number" }
                                  }
                                }
                              }
                            }
                          },
                          "required": ["configuration"]
                        }""";
            }

            @Override
            public String execute(String arguments) {
                try {
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
                                - Columns: {OPEN}, {HIGH}, {LOW}, {CLOSE} (optionally {X})
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
                                - chartId (string, required): The ID of the chart to update
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
                    JsonNode args = JacksonUtils.readTree(arguments);
                    String chartId = resolveChartId(args, callbacks);

                    JsonNode queriesNode = args.get("queries");
                    if (queriesNode == null || queriesNode.isNull()) {
                        return "Error updating chart data: 'queries' parameter is required.";
                    }
                    if (!queriesNode.isArray()) {
                        return "Error updating chart data: 'queries' must be an array.";
                    }
                    List<String> queries = new ArrayList<>();
                    for (JsonNode q : queriesNode) {
                        queries.add(q.asString());
                    }

                    callbacks.updateData(chartId, queries);

                    return "Chart '" + chartId
                            + "' data source updated. Changes will be applied when the request completes.";
                } catch (Exception e) {
                    return "Error updating chart data: " + e.getMessage();
                }
            }
        };
    }
}
