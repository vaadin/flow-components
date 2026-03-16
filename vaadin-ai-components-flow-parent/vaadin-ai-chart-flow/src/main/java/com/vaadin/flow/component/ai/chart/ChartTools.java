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
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ObjectNode;

/**
 * Provides LLM tool definitions for chart visualization operations.
 * <p>
 * This class encapsulates the tools that allow an LLM to query database schemas
 * and create/update chart visualizations. Each instance is bound to a specific
 * {@link Chart} component and {@link DatabaseProvider}.
 * </p>
 * <p>
 * The tools can be used by any controller type, not just
 * {@link ChartAIController}. For example, a dashboard controller managing
 * multiple charts can create a {@code ChartTools} instance per chart.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class ChartTools implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ChartTools.class);

    private final Chart chart;
    private final DatabaseProvider databaseProvider;
    private DataConverter chartDataConverter;
    private final ChartConfigurationApplier configurationApplier;

    private String currentSqlQuery;
    private String pendingDataQuery;
    private String pendingConfigJson;

    /**
     * Creates a new chart tools instance bound to the given chart and database
     * provider.
     *
     * @param chart
     *            the chart component to update, not {@code null}
     * @param databaseProvider
     *            the database provider for schema and query execution, not
     *            {@code null}
     */
    public ChartTools(Chart chart, DatabaseProvider databaseProvider) {
        this.chart = Objects.requireNonNull(chart, "Chart cannot be null");
        this.databaseProvider = Objects.requireNonNull(databaseProvider,
                "Database provider cannot be null");
        this.chartDataConverter = new DefaultDataConverter();
        this.configurationApplier = new ChartConfigurationApplier();
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
                1. getSchema() - Retrieves database schema (tables, columns, types)
                2. getCurrentState() - Returns current chart state (query and configuration)
                3. updateData(query) - Updates chart data with SQL SELECT query
                   - Query structure MUST match the chart type (see DATA REQUIREMENTS below)
                4. updateConfig(config) - Updates chart configuration (type, title, tooltip, etc.)
                   - Supports 31 chart types: line, spline, area, areaspline, bar, column, pie, scatter,
                     gauge, arearange, columnrange, areasplinerange, boxplot, errorbar, bubble, funnel,
                     waterfall, pyramid, solidgauge, heatmap, treemap, polygon, candlestick, flags,
                     timeline, ohlc, organization, sankey, xrange, gantt, bullet
                   - Config includes: chart model (dimensions, margins, spacing, borders, background),
                     axes (x, y, z, color), title, subtitle, tooltip, legend, credits, pane, exporting

                WORKFLOW:
                1. ALWAYS call getCurrentState() FIRST before making any changes
                2. Use getSchema() if you need to understand available data
                3. Use updateData() to change data source
                4. Use updateConfig() to change chart appearance

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
                - ALWAYS check getCurrentState() before making any modifications
                - This helps you understand what's already configured and make informed changes
                - NEVER include 'series' data in updateConfig() - chart data comes ONLY from updateData()
                - updateData() executes SQL and populates the chart series automatically
                - updateConfig() only handles visual appearance (type, styling, labels, etc.)
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
     * Returns the tool definitions for chart operations.
     *
     * @return list of tool definitions
     */
    public List<LLMProvider.ToolDefinition> getTools() {
        return List.of(createGetCurrentStateTool(), createUpdateDataTool(),
                createUpdateConfigTool());
    }

    /**
     * Sets a custom data converter for transforming query results into chart
     * data.
     *
     * @param dataConverter
     *            the data converter to use, not {@code null}
     */
    public void setDataConverter(DataConverter dataConverter) {
        this.chartDataConverter = Objects.requireNonNull(dataConverter,
                "Data converter cannot be null");
    }

    /**
     * Returns the chart component bound to this tools instance.
     *
     * @return the chart component
     */
    public Chart getChart() {
        return chart;
    }

    /**
     * Returns the data converter.
     *
     * @return the data converter
     */
    public DataConverter getDataConverter() {
        return chartDataConverter;
    }

    /**
     * Returns the configuration applier.
     *
     * @return the configuration applier
     */
    public ChartConfigurationApplier getConfigurationApplier() {
        return configurationApplier;
    }

    /**
     * Returns the current SQL query.
     *
     * @return the current SQL query, or {@code null} if no query has been
     *         executed
     */
    public String getCurrentSqlQuery() {
        return currentSqlQuery;
    }

    /**
     * Sets the current SQL query. Used when restoring state.
     *
     * @param sqlQuery
     *            the SQL query to set
     */
    public void setCurrentSqlQuery(String sqlQuery) {
        this.currentSqlQuery = sqlQuery;
    }

    /**
     * Returns the pending data query set by the updateData tool.
     *
     * @return the pending data query, or {@code null} if none
     */
    public String getPendingDataQuery() {
        return pendingDataQuery;
    }

    /**
     * Returns the pending config JSON set by the updateConfig tool.
     *
     * @return the pending config JSON, or {@code null} if none
     */
    public String getPendingConfigJson() {
        return pendingConfigJson;
    }

    /**
     * Clears pending data query and config JSON after rendering.
     */
    public void clearPending() {
        pendingDataQuery = null;
        pendingConfigJson = null;
    }

    // ===== Tool Implementations =====

    private LLMProvider.ToolDefinition createGetCurrentStateTool() {
        return new LLMProvider.ToolDefinition() {
            @Override
            public String getName() {
                return "getCurrentState";
            }

            @Override
            public String getDescription() {
                return "Returns the current state of the chart including the SQL query and configuration. Takes no parameters.";
            }

            @Override
            public String getParametersSchema() {
                return null;
            }

            @Override
            public String execute(String arguments) {
                if (currentSqlQuery == null) {
                  return "{\"status\":\"empty\",\"message\":\"No chart has been created yet\"}";
                }
                String configJson = ChartSerialization
                        .toJSON(chart.getConfiguration());
                try {
                    ObjectNode configNode = (ObjectNode) JacksonUtils
                            .readTree(configJson);
                    configNode.remove("series");
                    configJson = configNode.toString();
                } catch (Exception e) {
                    LOGGER.warn("Failed to remove series from config", e);
                }
                return "{\"query\":\""
                  + currentSqlQuery.replace("\"", "\\\"")
                  + "\",\"configuration\":" + configJson + "}";
            }
        };
    }

    private LLMProvider.ToolDefinition createUpdateDataTool() {
        return new LLMProvider.ToolDefinition() {
            @Override
            public String getName() {
                return "updateData";
            }

            @Override
            public String getDescription() {
                return """
                    Updates the chart data using a SQL SELECT query.

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

                    Parameters: query (string) - SQL SELECT query to retrieve data
                    """;
            }

            @Override
            public String getParametersSchema() {
                return null;
            }

            @Override
            public String execute(String arguments) {
                try {
                    ObjectNode node = (ObjectNode) JacksonUtils
                            .readTree(arguments);
                    String query = node.get("query").asString();

                    // Test the query
                    databaseProvider.executeQuery(query);

                    currentSqlQuery = query;

                    // Defer rendering until request completes
                    pendingDataQuery = query;

                    return "Chart data update queued successfully";
                } catch (Exception e) {
                    return "Error updating chart data: " + e.getMessage();
                }
            }
        };
    }

    private LLMProvider.ToolDefinition createUpdateConfigTool() {
        return new LLMProvider.ToolDefinition() {
            @Override
            public String getName() {
                return "updateConfig";
            }

            @Override
            public String getDescription() {
                return """
                    Updates the chart configuration (type, title, tooltip, etc.).

                    CRITICAL: ALWAYS specify the chart type in config.chart.type - this is essential for proper rendering.

                    Parameters: config (object) - Chart configuration object.

                    IMPORTANT: Do NOT include 'series' in the config - chart data is managed separately via updateData tool.
                    """;
            }

            @Override
            public String getParametersSchema() {
                return """
                        {
                          "type": "object",
                          "properties": {
                            "config": {
                              "type": "object",
                              "description": "Chart configuration object. CRITICAL: Always include chart.type. NOTE: Do NOT include 'series' - data is managed separately via updateData tool.",
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
                          "required": ["config"]
                        }
                        """;
            }

            @Override
            public String execute(String arguments) {
                try {
                    ObjectNode node = (ObjectNode) JacksonUtils
                            .readTree(arguments);
                    pendingConfigJson = node.has("config")
                            ? node.get("config").toString()
                            : node.toString();
                    return "Chart configuration update queued successfully";
                } catch (Exception e) {
                    return "Error updating chart configuration: "
                            + e.getMessage();
                }
            }
        };
    }

}
