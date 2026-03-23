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
public final class ChartTools {

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

    private ChartTools() {
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
                JsonNode args = JacksonUtils.readTree(arguments);
                String chartId = resolveChartId(args, callbacks);
                return callbacks.getState(chartId);
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
                JsonNode args = JacksonUtils.readTree(arguments);
                String chartId = resolveChartId(args, callbacks);

                JsonNode configNode = args.get("configuration");
                callbacks.updateConfiguration(chartId, configNode.toString());

                return "Chart '" + chartId
                        + "' configuration updated. Changes will be applied when the request completes.";
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
                return """
                        Updates the chart data using SQL SELECT queries (one per series).

                        IMPORTANT: Query structure must match the chart type:

                        Basic charts (line, bar, column, pie):
                        - 2 columns: category/name, value
                        - Example: SELECT month, revenue FROM sales

                        Scatter plot:
                        - 2 numeric columns: x, y
                        - Example: SELECT temperature, sales FROM data

                        Bubble chart:
                        - 3 numeric columns: x, y, size
                        - Example: SELECT gdp_per_capita, life_expectancy, population FROM countries

                        Bullet chart:
                        - 3 columns with 'target': category, value, target
                        - Example: SELECT quarter, revenue, target FROM sales

                        Range charts (arearange, columnrange, areasplinerange):
                        - 3 columns: x/category, low/min, high/max
                        - Example: SELECT month, temp_low, temp_high FROM weather

                        BoxPlot:
                        - 5 columns: low, q1, median, q3, high (column names should include these keywords)
                        - Example: SELECT min_val, lower_quartile, median, upper_quartile, max_val FROM stats

                        OHLC/Candlestick:
                        - 5 columns: x/date, open, high, low, close (column names must include these keywords)
                        - Example: SELECT date, open, high, low, close FROM stock_prices

                        Sankey diagram:
                        - 3 columns: from/source, to/target, weight/value
                        - Example: SELECT source, destination, flow FROM energy_flow

                        Xrange/Gantt:
                        - 3 columns: start/x, end/x2, y/category
                        - Example: SELECT start_date, end_date, task_id FROM project_tasks

                        Column names are important for automatic detection. Use descriptive names that match the patterns above.

                        Parameters:
                        - chartId (string, required): The ID of the chart to update
                        - queries (array of strings, required): SQL SELECT queries, one per series

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

                    List<String> queries = new ArrayList<>();
                    for (JsonNode q : args.get("queries")) {
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
