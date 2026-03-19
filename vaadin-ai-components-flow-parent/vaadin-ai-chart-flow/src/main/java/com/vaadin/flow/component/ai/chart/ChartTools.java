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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Factory for creating reusable chart {@link LLMProvider.ToolDefinition}
 * instances that operate on a {@link ChartRegistry}. These tools are not tied
 * to any specific controller and can be used by both
 * {@link ChartAIController} and {@code DashboardAIController}.
 * <p>
 * The tools use a {@code chartId} parameter to identify which chart to operate
 * on, allowing a single set of tools to manage multiple charts (e.g., in a
 * dashboard).
 * </p>
 *
 * @author Vaadin Ltd
 */
public final class ChartTools {

    private static final String GET_CHART_STATE_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "chartId": {
                  "type": "string",
                  "description": "The ID of the chart. Optional when there is only one chart."
                }
              }
            }""";

    private static final String UPDATE_CHART_CONFIGURATION_SCHEMA = """
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

    private static final String UPDATE_CHART_DATA_SOURCE_SCHEMA = """
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

    private ChartTools() {
    }

    /**
     * Resolves the chart ID from the tool arguments. If {@code chartId} is not
     * provided and the registry contains exactly one chart, that chart's ID is
     * used as the default.
     */
    private static String resolveChartId(JsonNode args,
            ChartRegistry registry) {
        JsonNode idNode = args.get("chartId");
        if (idNode != null && !idNode.isNull()) {
            return idNode.asString();
        }
        var ids = registry.getChartIds();
        if (ids.size() == 1) {
            return ids.iterator().next();
        }
        throw new IllegalArgumentException(
                "chartId is required when multiple charts exist. "
                        + "Available chart IDs: " + ids);
    }

    /**
     * Returns the recommended system prompt for chart visualization
     * capabilities.
     *
     * @return the system prompt text
     */
    public static String getSystemPrompt() {
        return """
                You have access to chart visualization capabilities:

                TOOLS:
                1. get_database_schema() - Retrieves database schema (tables, columns, types)
                2. get_chart_state(chartId) - Returns current chart state (queries and configuration)
                3. update_chart_data_source(chartId, queries) - Updates chart data with SQL SELECT queries
                   - Query structure MUST match the chart type (see DATA REQUIREMENTS below)
                4. update_chart_configuration(chartId, configuration) - Updates chart configuration (type, title, tooltip, etc.)
                   - Supports 31 chart types: line, spline, area, areaspline, bar, column, pie, scatter,
                     gauge, arearange, columnrange, areasplinerange, boxplot, errorbar, bubble, funnel,
                     waterfall, pyramid, solidgauge, heatmap, treemap, polygon, candlestick, flags,
                     timeline, ohlc, organization, sankey, xrange, gantt, bullet
                   - Config includes: chart model (dimensions, margins, spacing, borders, background),
                     axes (x, y, z, color), title, subtitle, tooltip, legend, credits, pane, exporting

                WORKFLOW:
                1. ALWAYS call get_chart_state() FIRST before making any changes
                2. Use get_database_schema() if you need to understand available data
                3. Use update_chart_data_source() to change data source
                4. Use update_chart_configuration() to change chart appearance

                DATA REQUIREMENTS BY CHART TYPE:
                - Basic charts (line, bar, column, pie): SELECT category, value (2 columns)
                - Scatter: SELECT x, y (2 numeric columns)
                - Bubble: SELECT x, y, size (3 numeric columns)
                - Bullet: SELECT category, value, target (3 columns, name third 'target')
                - Range (arearange, columnrange): SELECT x, low, high (3 columns, name 'low'/'high')
                - BoxPlot: SELECT low, q1, median, q3, high (5 columns with these keywords)
                - OHLC/Candlestick: SELECT date, open, high, low, close (5 columns with these keywords)
                - Sankey: SELECT from, to, weight (3 columns with 'from'/'to' keywords)
                - Xrange/Gantt: SELECT start, end, y (3 columns with 'start'/'end' keywords)

                Column names matter! Use descriptive names matching the patterns above for automatic detection.

                IMPORTANT:
                - ALWAYS check get_chart_state() before making any modifications
                - This helps you understand what's already configured and make informed changes
                - NEVER include 'series' data in update_chart_configuration() - chart data comes ONLY from update_chart_data_source()
                - update_chart_data_source() executes SQL and populates the chart series automatically
                - update_chart_configuration() only handles visual appearance (type, styling, labels, etc.)
                - Chart type recommendations:
                  * Trends over time: line, spline, area, areaspline
                  * Comparisons: bar, column
                  * Proportions: pie, funnel
                  * Distributions: boxplot, errorbar
                  * Relationships: scatter, bubble
                  * Specialized: gauge, heatmap, treemap, waterfall, gantt, sankey
                - When changing chart types, ensure the data query matches the new type's requirements
                - You can update data and config independently
                """;
    }

    /**
     * Creates a tool that retrieves the current state of a chart, including its
     * Highcharts configuration and data source queries.
     *
     * @param registry
     *            the chart registry to read from, not {@code null}
     * @return the tool definition, never {@code null}
     */
    public static LLMProvider.ToolDefinition getChartState(
            ChartRegistry registry) {
        Objects.requireNonNull(registry, "registry must not be null");
        return new LLMProvider.ToolDefinition() {
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
                return GET_CHART_STATE_SCHEMA;
            }

            @Override
            public String execute(String arguments) {
                JsonNode args = JacksonUtils.readTree(arguments);
                String chartId = resolveChartId(args, registry);

                Chart chart = registry.getChart(chartId);
                ChartEntry entry = registry.getEntry(chartId);

                ObjectNode result = JacksonUtils.createObjectNode();
                result.put("chartId", chartId);

                String configJson = ChartSerialization
                        .toJSON(chart.getConfiguration());
                ObjectNode configNode = (ObjectNode) JacksonUtils
                        .readTree(configJson);
                configNode.remove("series");
                result.set("configuration", configNode);

                List<String> queries = entry.getQueries();
                if (!queries.isEmpty()) {
                    ArrayNode arr = result.putArray("queries");
                    queries.forEach(arr::add);
                }

                return result.toString();
            }
        };
    }

    /**
     * Creates a tool that updates a chart's Highcharts configuration. The full
     * configuration must be provided — this is a complete replacement, not a
     * diff. Changes are stored as pending state and applied when the request
     * completes.
     *
     * @param registry
     *            the chart registry to update, not {@code null}
     * @return the tool definition, never {@code null}
     */
    public static LLMProvider.ToolDefinition updateChartConfiguration(
            ChartRegistry registry) {
        Objects.requireNonNull(registry, "registry must not be null");
        return new LLMProvider.ToolDefinition() {
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
                return UPDATE_CHART_CONFIGURATION_SCHEMA;
            }

            @Override
            public String execute(String arguments) {
                JsonNode args = JacksonUtils.readTree(arguments);
                String chartId = resolveChartId(args, registry);

                registry.getChart(chartId);
                ChartEntry entry = registry.getEntry(chartId);

                JsonNode configNode = args.get("configuration");
                entry.setPendingConfigurationJson(configNode.toString());

                return "Chart '" + chartId
                        + "' configuration updated. Changes will be applied when the request completes.";
            }
        };
    }

    /**
     * Creates a tool that updates a chart's data source queries. Each query's
     * results populate one chart series. Changes are stored as pending state
     * and applied when the request completes.
     *
     * @param registry
     *            the chart registry to update, not {@code null}
     * @return the tool definition, never {@code null}
     */
    public static LLMProvider.ToolDefinition updateChartDataSource(
            ChartRegistry registry) {
        Objects.requireNonNull(registry, "registry must not be null");
        return new LLMProvider.ToolDefinition() {
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
                return UPDATE_CHART_DATA_SOURCE_SCHEMA;
            }

            @Override
            public String execute(String arguments) {
                try {
                    JsonNode args = JacksonUtils.readTree(arguments);
                    String chartId = resolveChartId(args, registry);

                    registry.getChart(chartId);
                    ChartEntry entry = registry.getEntry(chartId);

                    List<String> queries = new ArrayList<>();
                    for (JsonNode q : args.get("queries")) {
                        queries.add(q.asString());
                    }

                    for (String q : queries) {
                        registry.validateQuery(q);
                    }

                    entry.setPendingQueries(queries);

                    return "Chart '" + chartId
                            + "' data source updated. Changes will be applied when the request completes.";
                } catch (Exception e) {
                    return "Error updating chart data: "
                            + e.getMessage();
                }
            }
        };
    }

    /**
     * Creates all chart tools for the given registry.
     *
     * @param registry
     *            the chart registry, not {@code null}
     * @return a list of all chart tools, never {@code null}
     */
    public static List<LLMProvider.ToolDefinition> createAll(
            ChartRegistry registry) {
        return List.of(getChartState(registry),
                updateChartConfiguration(registry),
                updateChartDataSource(registry));
    }
}
