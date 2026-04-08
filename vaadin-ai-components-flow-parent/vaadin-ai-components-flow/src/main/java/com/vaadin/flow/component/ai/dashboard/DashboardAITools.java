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
package com.vaadin.flow.component.ai.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Factory for creating reusable dashboard {@link LLMProvider.ToolSpec}
 * instances.
 * <p>
 * The tools manage dashboard layout operations: listing widgets, updating
 * widget properties, creating and removing widgets. Callers provide a
 * {@link Callbacks} implementation for state retrieval and mutation, keeping
 * this class decoupled from {@code Dashboard} and widget components.
 * </p>
 * <p>
 * Chart and grid data tools are not included here — they are created separately
 * via {@code ChartAITools} and {@code GridAITools}.
 * </p>
 *
 * @author Vaadin Ltd
 */
public final class DashboardAITools {

    private DashboardAITools() {
    }

    /**
     * Callback interface that dashboard tool consumers must implement to
     * provide dashboard state access and mutation operations.
     */
    public interface Callbacks extends Serializable {

        /**
         * Returns the current state of the dashboard as a JSON string,
         * including all widgets with their IDs, titles, types, sizes, and
         * selection state.
         *
         * @return the dashboard state as JSON
         */
        String getState();

        /**
         * Updates a widget's properties. Implementations should throw if the
         * widget is not found. Null parameters should be ignored (not updated).
         *
         * @param widgetId
         *            the ID of the widget to update
         * @param title
         *            new title, or {@code null} to leave unchanged
         * @param colspan
         *            new column span, or {@code null} to leave unchanged
         * @param rowspan
         *            new row span, or {@code null} to leave unchanged
         */
        void updateWidget(String widgetId, String title, Integer colspan,
                Integer rowspan);

        /**
         * Reorders the widgets on the dashboard. Implementations should throw
         * if any widget ID is not found.
         *
         * @param widgetIds
         *            widget IDs in the desired display order
         */
        void reorderWidgets(List<String> widgetIds);

        /**
         * Adds a new chart widget to the dashboard. Implementations should
         * validate queries if provided.
         *
         * @param title
         *            the widget title
         * @param colspan
         *            the column span
         * @param rowspan
         *            the row span
         * @param queries
         *            SQL queries to populate the chart, or {@code null}
         * @param configJson
         *            chart configuration JSON, or {@code null}
         * @return the assigned widget ID
         */
        String addChartWidget(String title, int colspan, int rowspan,
                List<String> queries, String configJson);

        /**
         * Adds a new grid widget to the dashboard. Implementations should
         * validate the query if provided.
         *
         * @param title
         *            the widget title
         * @param colspan
         *            the column span
         * @param rowspan
         *            the row span
         * @param query
         *            SQL query to populate the grid, or {@code null}
         * @return the assigned widget ID
         */
        String addGridWidget(String title, int colspan, int rowspan,
                String query);

        /**
         * Removes a widget from the dashboard. Implementations should throw if
         * the widget is not found.
         *
         * @param widgetId
         *            the ID of the widget to remove
         */
        void removeWidget(String widgetId);
    }

    /**
     * Creates all dashboard layout tools for the given callbacks.
     *
     * @param callbacks
     *            the callbacks for dashboard state access and mutation, not
     *            {@code null}
     * @return a list of all dashboard tools, never {@code null}
     */
    public static List<LLMProvider.ToolSpec> createAll(Callbacks callbacks) {
        Objects.requireNonNull(callbacks, "callbacks must not be null");
        return List.of(getDashboardState(callbacks), updateWidget(callbacks),
                reorderWidgets(callbacks), addChartWidget(callbacks),
                addGridWidget(callbacks), removeWidget(callbacks));
    }

    /**
     * Creates a tool that returns the current dashboard state.
     *
     * @param callbacks
     *            the callbacks for dashboard state access, not {@code null}
     * @return the tool definition, never {@code null}
     */
    public static LLMProvider.ToolSpec getDashboardState(Callbacks callbacks) {
        Objects.requireNonNull(callbacks, "callbacks must not be null");
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "getDashboardState";
            }

            @Override
            public String getDescription() {
                return "Returns the current state of the dashboard including all widgets with their IDs, titles, types, colspan, rowspan, and selection state. IMPORTANT: Always call this first in every request, as widget selections can change between requests. Takes no parameters.";
            }

            @Override
            public String getParametersSchema() {
                return null;
            }

            @Override
            public String execute(String arguments) {
                try {
                    return callbacks.getState();
                } catch (Exception e) {
                    return "Error getting dashboard state: " + e.getMessage();
                }
            }
        };
    }

    /**
     * Creates a tool that updates a widget's properties.
     *
     * @param callbacks
     *            the callbacks for widget mutation, not {@code null}
     * @return the tool definition, never {@code null}
     */
    public static LLMProvider.ToolSpec updateWidget(Callbacks callbacks) {
        Objects.requireNonNull(callbacks, "callbacks must not be null");
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "updateWidget";
            }

            @Override
            public String getDescription() {
                return """
                        Updates a dashboard widget's properties.
                        Use getDashboardState() first to get the widget IDs.

                        Parameters:
                        - widgetId (string, required): The ID of the widget to update
                        - title (string, optional): New title for the widget
                        - colspan (integer, optional): Number of columns the widget spans (minimum: 1)
                        - rowspan (integer, optional): Number of rows the widget spans (minimum: 1)
                        """;
            }

            @Override
            public String getParametersSchema() {
                return """
                        {
                          "type": "object",
                          "properties": {
                            "widgetId": {
                              "type": "string",
                              "description": "The ID of the widget to update"
                            },
                            "title": {
                              "type": "string",
                              "description": "New title for the widget"
                            },
                            "colspan": {
                              "type": "integer",
                              "description": "Number of columns the widget spans (minimum: 1)",
                              "minimum": 1
                            },
                            "rowspan": {
                              "type": "integer",
                              "description": "Number of rows the widget spans (minimum: 1)",
                              "minimum": 1
                            }
                          },
                          "required": ["widgetId"]
                        }
                        """;
            }

            @Override
            public String execute(String arguments) {
                try {
                    ObjectNode node = (ObjectNode) JacksonUtils
                            .readTree(arguments);
                    String widgetId = node.get("widgetId").asString();

                    String title = null;
                    JsonNode titleNode = node.get("title");
                    if (titleNode != null && !titleNode.isNull()) {
                        title = titleNode.asString();
                    }

                    Integer colspan = null;
                    JsonNode colspanNode = node.get("colspan");
                    if (colspanNode != null && !colspanNode.isNull()) {
                        colspan = Math.max(1, colspanNode.asInt());
                    }

                    Integer rowspan = null;
                    JsonNode rowspanNode = node.get("rowspan");
                    if (rowspanNode != null && !rowspanNode.isNull()) {
                        rowspan = Math.max(1, rowspanNode.asInt());
                    }

                    callbacks.updateWidget(widgetId, title, colspan, rowspan);
                    return "Widget '" + widgetId + "' updated successfully";
                } catch (Exception e) {
                    return "Error updating widget: " + e.getMessage();
                }
            }
        };
    }

    /**
     * Creates a tool that reorders dashboard widgets.
     *
     * @param callbacks
     *            the callbacks for widget reordering, not {@code null}
     * @return the tool definition, never {@code null}
     */
    public static LLMProvider.ToolSpec reorderWidgets(Callbacks callbacks) {
        Objects.requireNonNull(callbacks, "callbacks must not be null");
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "reorderWidgets";
            }

            @Override
            public String getDescription() {
                return """
                        Reorders the widgets on the dashboard. Provide the widget IDs \
                        as a comma-separated string in the desired display order. \
                        Use getDashboardState() first to get the current widget IDs. \
                        Example: "grid-3,chart-1,chart-2"

                        Parameters:
                        - widgetIds (string, required): Comma-separated widget IDs \
                        in the desired order, e.g. "grid-3,chart-1,chart-2"
                        """;
            }

            @Override
            public String getParametersSchema() {
                return """
                        {
                          "type": "object",
                          "properties": {
                            "widgetIds": {
                              "type": "string",
                              "description": "Comma-separated widget IDs in the desired display order, e.g. 'grid-3,chart-1,chart-2'"
                            }
                          },
                          "required": ["widgetIds"]
                        }
                        """;
            }

            @Override
            public String execute(String arguments) {
                try {
                    ObjectNode node = (ObjectNode) JacksonUtils
                            .readTree(arguments);
                    JsonNode idsNode = node.get("widgetIds");
                    if (idsNode == null || idsNode.isNull()) {
                        return "Error: widgetIds is required";
                    }

                    String[] ids = idsNode.asString().split(",");
                    List<String> widgetIds = new ArrayList<>();
                    for (String id : ids) {
                        widgetIds.add(id.trim());
                    }

                    callbacks.reorderWidgets(widgetIds);
                    return "Widgets reordered successfully";
                } catch (Exception e) {
                    return "Error reordering widgets: " + e.getMessage();
                }
            }
        };
    }

    /**
     * Creates a tool that adds a new chart widget to the dashboard.
     *
     * @param callbacks
     *            the callbacks for widget creation, not {@code null}
     * @return the tool definition, never {@code null}
     */
    public static LLMProvider.ToolSpec addChartWidget(Callbacks callbacks) {
        Objects.requireNonNull(callbacks, "callbacks must not be null");
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "addChartWidget";
            }

            @Override
            public String getDescription() {
                return """
                        Adds a new chart widget to the dashboard, optionally populated with data.
                        IMPORTANT: Always provide queries and config to create a widget with data immediately.

                        Parameters:
                        - title (string, optional): Widget title
                        - queries (array of strings, optional): SQL SELECT queries to populate the chart (one per series). \
                        Use this for multi-series charts.
                        - query (string, optional): Single SQL SELECT query (shorthand for one-series charts). \
                        If both query and queries are provided, queries takes precedence.
                        - config (object, optional): Chart configuration (type, title, axes, etc.). \
                        CRITICAL: Always include chart.type. Do NOT include 'series' - data comes from queries.
                        - colspan (integer, optional): Column span (default: 1)
                        - rowspan (integer, optional): Row span (default: 1)
                        """;
            }

            @Override
            public String getParametersSchema() {
                return """
                        {
                          "type": "object",
                          "properties": {
                            "title": { "type": "string", "description": "Widget title" },
                            "queries": { "type": "array", "items": { "type": "string" }, "description": "SQL SELECT queries to populate the chart, one per series" },
                            "query": { "type": "string", "description": "Single SQL SELECT query (shorthand for one-series charts)" },
                            "config": {
                              "type": "object",
                              "description": "Chart configuration. CRITICAL: Always include chart.type. Do NOT include 'series'.",
                              "properties": {
                                "chart": {
                                  "type": "object",
                                  "properties": {
                                    "type": {
                                      "type": "string",
                                      "description": "REQUIRED: Chart type",
                                      "enum": ["line", "spline", "area", "areaspline", "bar", "column", "pie", "scatter", "gauge", "arearange", "columnrange", "areasplinerange", "boxplot", "errorbar", "bubble", "funnel", "waterfall", "pyramid", "solidgauge", "heatmap", "treemap", "polygon", "candlestick", "flags", "timeline", "ohlc", "organization", "sankey", "xrange", "gantt", "bullet"]
                                    }
                                  }
                                },
                                "title": { "oneOf": [{ "type": "string" }, { "type": "object", "properties": { "text": { "type": "string" } } }] },
                                "subtitle": { "oneOf": [{ "type": "string" }, { "type": "object", "properties": { "text": { "type": "string" } } }] },
                                "xAxis": { "type": "object", "properties": { "title": { "type": "object", "properties": { "text": { "type": "string" } } } } },
                                "yAxis": { "type": "object", "properties": { "title": { "type": "object", "properties": { "text": { "type": "string" } } } } },
                                "tooltip": { "type": "object" },
                                "legend": { "type": "object" },
                                "credits": { "type": "object" }
                              }
                            },
                            "colspan": { "type": "integer", "description": "Column span", "minimum": 1, "default": 1 },
                            "rowspan": { "type": "integer", "description": "Row span", "minimum": 1, "default": 1 }
                          }
                        }
                        """;
            }

            @Override
            public String execute(String arguments) {
                try {
                    String title = "Chart";
                    int colspan = 1;
                    int rowspan = 1;
                    List<String> queries = null;
                    String configJson = null;

                    if (arguments != null && !arguments.isBlank()) {
                        ObjectNode node = (ObjectNode) JacksonUtils
                                .readTree(arguments);
                        if (node.has("title") && !node.get("title").isNull()) {
                            title = node.get("title").asString();
                        }
                        if (node.has("queries") && !node.get("queries").isNull()
                                && node.get("queries").isArray()) {
                            queries = new ArrayList<>();
                            for (var q : node.get("queries")) {
                                queries.add(q.asString());
                            }
                        } else if (node.has("query")
                                && !node.get("query").isNull()) {
                            queries = List.of(node.get("query").asString());
                        }
                        if (node.has("config")
                                && !node.get("config").isNull()) {
                            configJson = node.get("config").toString();
                        }
                        if (node.has("colspan")
                                && node.get("colspan").isNumber()) {
                            colspan = node.get("colspan").asInt();
                        }
                        if (node.has("rowspan")
                                && node.get("rowspan").isNumber()) {
                            rowspan = node.get("rowspan").asInt();
                        }
                    }

                    String widgetId = callbacks.addChartWidget(title, colspan,
                            rowspan, queries, configJson);

                    return "{\"widgetId\":\"" + widgetId
                            + "\",\"type\":\"chart\",\"title\":\""
                            + title.replace("\"", "\\\"")
                            + "\",\"message\":\"Chart widget added"
                            + (queries != null ? " with data" : "") + ".\"}";
                } catch (Exception e) {
                    return "Error adding chart widget: " + e.getMessage();
                }
            }
        };
    }

    /**
     * Creates a tool that adds a new grid widget to the dashboard.
     *
     * @param callbacks
     *            the callbacks for widget creation, not {@code null}
     * @return the tool definition, never {@code null}
     */
    public static LLMProvider.ToolSpec addGridWidget(Callbacks callbacks) {
        Objects.requireNonNull(callbacks, "callbacks must not be null");
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "addGridWidget";
            }

            @Override
            public String getDescription() {
                return """
                        Adds a new data grid widget to the dashboard, optionally populated with data.
                        IMPORTANT: Always provide query to create a widget with data immediately.
                        The grid automatically creates columns based on query result columns.
                        Use SQL aliases (AS) to provide human-readable column headers.

                        Parameters:
                        - title (string, optional): Widget title
                        - query (string, optional): SQL SELECT query to populate the grid
                        - colspan (integer, optional): Column span (default: 1)
                        - rowspan (integer, optional): Row span (default: 1)
                        """;
            }

            @Override
            public String getParametersSchema() {
                return """
                        {
                          "type": "object",
                          "properties": {
                            "title": { "type": "string", "description": "Widget title" },
                            "query": { "type": "string", "description": "SQL SELECT query to populate the grid" },
                            "colspan": { "type": "integer", "description": "Column span", "minimum": 1, "default": 1 },
                            "rowspan": { "type": "integer", "description": "Row span", "minimum": 1, "default": 1 }
                          }
                        }
                        """;
            }

            @Override
            public String execute(String arguments) {
                try {
                    String title = "Data Grid";
                    int colspan = 1;
                    int rowspan = 1;
                    String query = null;

                    if (arguments != null && !arguments.isBlank()) {
                        ObjectNode node = (ObjectNode) JacksonUtils
                                .readTree(arguments);
                        if (node.has("title") && !node.get("title").isNull()) {
                            title = node.get("title").asString();
                        }
                        if (node.has("query") && !node.get("query").isNull()) {
                            query = node.get("query").asString();
                        }
                        if (node.has("colspan")
                                && node.get("colspan").isNumber()) {
                            colspan = node.get("colspan").asInt();
                        }
                        if (node.has("rowspan")
                                && node.get("rowspan").isNumber()) {
                            rowspan = node.get("rowspan").asInt();
                        }
                    }

                    String widgetId = callbacks.addGridWidget(title, colspan,
                            rowspan, query);

                    return "{\"widgetId\":\"" + widgetId
                            + "\",\"type\":\"grid\",\"title\":\""
                            + title.replace("\"", "\\\"")
                            + "\",\"message\":\"Grid widget added"
                            + (query != null ? " with data" : "") + ".\"}";
                } catch (Exception e) {
                    return "Error adding grid widget: " + e.getMessage();
                }
            }
        };
    }

    /**
     * Creates a tool that removes a widget from the dashboard.
     *
     * @param callbacks
     *            the callbacks for widget removal, not {@code null}
     * @return the tool definition, never {@code null}
     */
    public static LLMProvider.ToolSpec removeWidget(Callbacks callbacks) {
        Objects.requireNonNull(callbacks, "callbacks must not be null");
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "removeWidget";
            }

            @Override
            public String getDescription() {
                return """
                        Removes a widget from the dashboard.
                        Use getDashboardState() first to get the widget IDs.

                        Parameters:
                        - widgetId (string, required): The ID of the widget to remove
                        """;
            }

            @Override
            public String getParametersSchema() {
                return """
                        {
                          "type": "object",
                          "properties": {
                            "widgetId": {
                              "type": "string",
                              "description": "The ID of the widget to remove"
                            }
                          },
                          "required": ["widgetId"]
                        }
                        """;
            }

            @Override
            public String execute(String arguments) {
                try {
                    ObjectNode node = (ObjectNode) JacksonUtils
                            .readTree(arguments);
                    String widgetId = node.get("widgetId").asString();
                    callbacks.removeWidget(widgetId);
                    return "Widget '" + widgetId + "' removed successfully";
                } catch (Exception e) {
                    return "Error removing widget: " + e.getMessage();
                }
            }
        };
    }
}
