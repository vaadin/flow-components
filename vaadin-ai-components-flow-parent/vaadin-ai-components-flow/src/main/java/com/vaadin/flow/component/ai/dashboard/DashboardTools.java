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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import com.vaadin.flow.component.ai.chart.ChartEntry;
import com.vaadin.flow.component.ai.chart.ChartTools;
import com.vaadin.flow.component.ai.grid.GridTools;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Provides LLM tool definitions for dashboard operations: listing widgets,
 * updating widget properties, creating and removing widgets, and managing
 * chart/grid data tools.
 *
 * @author Vaadin Ltd
 */
public class DashboardTools {

    /**
     * Callback for creating dashboard widgets.
     */
    @FunctionalInterface
    interface WidgetCreator {
        DashboardWidget create(String widgetId, String title, int colspan,
                int rowspan);
    }

    private final Dashboard dashboard;
    private final Consumer<String> queryValidator;
    private final WidgetCreator chartWidgetCreator;
    private final WidgetCreator gridWidgetCreator;
    private final Map<String, GridTools> gridToolsMap;
    private final Map<String, Checkbox> widgetCheckboxes = new LinkedHashMap<>();
    private int widgetCounter = 0;

    /**
     * Creates a new dashboard tools instance.
     *
     * @param dashboard
     *            the dashboard component to manage, not {@code null}
     * @param queryValidator
     *            validates SQL queries before accepting them, not {@code null}
     * @param chartWidgetCreator
     *            creates chart widgets, not {@code null}
     * @param gridWidgetCreator
     *            creates grid widgets, not {@code null}
     * @param gridToolsMap
     *            shared map of grid widget ID to {@link GridTools}, not
     *            {@code null}
     */
    DashboardTools(Dashboard dashboard, Consumer<String> queryValidator,
            WidgetCreator chartWidgetCreator,
            WidgetCreator gridWidgetCreator,
            Map<String, GridTools> gridToolsMap) {
        this.dashboard = Objects.requireNonNull(dashboard,
                "dashboard must not be null");
        this.queryValidator = Objects.requireNonNull(queryValidator,
                "queryValidator must not be null");
        this.chartWidgetCreator = Objects.requireNonNull(chartWidgetCreator,
                "chartWidgetCreator must not be null");
        this.gridWidgetCreator = Objects.requireNonNull(gridWidgetCreator,
                "gridWidgetCreator must not be null");
        this.gridToolsMap = Objects.requireNonNull(gridToolsMap,
                "gridToolsMap must not be null");
    }

    /**
     * Returns all tool definitions for dashboard operations.
     *
     * @return list of tool definitions
     */
    public List<LLMProvider.ToolSpec> getTools() {
        List<LLMProvider.ToolSpec> tools = new ArrayList<>();

        // Dashboard layout tools
        tools.add(createGetDashboardStateTool());
        tools.add(createUpdateWidgetTool());
        tools.add(createReorderWidgetsTool());

        // Widget creation/removal tools
        tools.add(createAddChartWidgetTool());
        tools.add(createAddGridWidgetTool());
        tools.add(createRemoveWidgetTool());

        // Chart tools (shared across all charts, resolved from dashboard)
        tools.addAll(ChartTools.createAll(
                this::findChartById,
                this::getChartWidgetIds,
                queryValidator));

        // Per-widget grid tools (prefixed with widget ID)
        for (Map.Entry<String, GridTools> entry : gridToolsMap.entrySet()) {
            String widgetId = entry.getKey();
            GridTools gridTools = entry.getValue();
            for (LLMProvider.ToolSpec tool : gridTools.getTools()) {
                tools.add(prefixTool(tool, widgetId));
            }
        }

        return tools;
    }

    /**
     * Adds a selection checkbox to the widget's header content. The checkbox
     * state is included in the dashboard state reported to the LLM, allowing
     * users to select which widgets an AI action should target.
     *
     * @param widget
     *            the widget to add a checkbox to
     */
    void addSelectionCheckbox(DashboardWidget widget) {
        String widgetId = widget.getId().orElse(null);
        if (widgetId == null || widgetCheckboxes.containsKey(widgetId)) {
            return;
        }
        var checkbox = new Checkbox();
        checkbox.getElement().setAttribute("title",
                "Select for AI actions");
        widgetCheckboxes.put(widgetId, checkbox);
        widget.setHeaderContent(checkbox);
    }

    /**
     * Clears all tracked selection checkboxes. Should be called when the
     * dashboard is cleared (e.g. during state restore).
     */
    void clearSelectionCheckboxes() {
        widgetCheckboxes.clear();
    }

    // ===== Chart Resolution =====

    private Chart findChartById(String chartId) {
        for (DashboardWidget widget : dashboard.getWidgets()) {
            if (widget.getContent() instanceof Chart chart) {
                ChartEntry entry = ChartEntry.get(chart);
                if (entry != null && chartId.equals(entry.getId())) {
                    return chart;
                }
            }
        }
        return null;
    }

    private Set<String> getChartWidgetIds() {
        var ids = new LinkedHashSet<String>();
        for (DashboardWidget widget : dashboard.getWidgets()) {
            if (widget.getContent() instanceof Chart chart) {
                ChartEntry entry = ChartEntry.get(chart);
                if (entry != null) {
                    ids.add(entry.getId());
                }
            }
        }
        return ids;
    }

    // ===== Tool Implementations =====

    private LLMProvider.ToolSpec createGetDashboardStateTool() {
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
                List<DashboardWidget> widgets = dashboard.getWidgets();
                if (widgets.isEmpty()) {
                    return "{\"status\":\"empty\",\"message\":\"Dashboard has no widgets\",\"widgets\":[]}";
                }
                StringBuilder sb = new StringBuilder("{\"widgets\":[");
                for (int i = 0; i < widgets.size(); i++) {
                    DashboardWidget widget = widgets.get(i);
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append("{\"widgetId\":\"").append(widget.getId()
                            .orElse("widget-" + i)).append("\"");
                    sb.append(",\"title\":\"")
                            .append(widget.getTitle() != null
                                    ? widget.getTitle().replace("\"", "\\\"")
                                    : "")
                            .append("\"");
                    sb.append(",\"colspan\":").append(widget.getColspan());
                    sb.append(",\"rowspan\":").append(widget.getRowspan());
                    String contentType = "unknown";
                    if (widget.getContent() instanceof Chart) {
                        contentType = "chart";
                    } else if (widget.getContent() instanceof Grid) {
                        contentType = "grid";
                    }
                    sb.append(",\"contentType\":\"").append(contentType)
                            .append("\"");
                    boolean selected = widget.getId()
                            .map(id -> {
                                Checkbox cb = widgetCheckboxes.get(id);
                                return cb != null && cb.getValue();
                            }).orElse(false);
                    sb.append(",\"selected\":").append(selected);
                    sb.append("}");
                }
                sb.append("]}");
                return sb.toString();
            }
        };
    }

    private LLMProvider.ToolSpec createUpdateWidgetTool() {
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

                    DashboardWidget targetWidget = findWidgetById(widgetId);
                    if (targetWidget == null) {
                        return "Error: Widget with ID '" + widgetId
                                + "' not found";
                    }

                    dashboard.getUI().ifPresentOrElse(ui -> {
                        ui.access(() -> {
                            JsonNode titleNode = node.get("title");
                            if (titleNode != null && !titleNode.isNull()) {
                                targetWidget
                                        .setTitle(titleNode.asString());
                            }
                            JsonNode colspanNode = node.get("colspan");
                            if (colspanNode != null
                                    && !colspanNode.isNull()) {
                                targetWidget.setColspan(
                                        Math.max(1, colspanNode.asInt()));
                            }
                            JsonNode rowspanNode = node.get("rowspan");
                            if (rowspanNode != null
                                    && !rowspanNode.isNull()) {
                                targetWidget.setRowspan(
                                        Math.max(1, rowspanNode.asInt()));
                            }
                        });
                    }, () -> {
                        throw new IllegalStateException(
                                "Dashboard is not attached to a UI");
                    });

                    return "Widget '" + widgetId + "' updated successfully";
                } catch (Exception e) {
                    return "Error updating widget: " + e.getMessage();
                }
            }
        };
    }

    private LLMProvider.ToolSpec createReorderWidgetsTool() {
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
                    List<DashboardWidget> orderedWidgets = new ArrayList<>();
                    for (String id : ids) {
                        String trimmedId = id.trim();
                        DashboardWidget widget = findWidgetById(trimmedId);
                        if (widget == null) {
                            return "Error: Widget with ID '" + trimmedId
                                    + "' not found";
                        }
                        orderedWidgets.add(widget);
                    }

                    dashboard.getUI().ifPresentOrElse(ui -> {
                        ui.access(() -> {
                            dashboard.remove(orderedWidgets);
                            for (DashboardWidget widget : orderedWidgets) {
                                dashboard.add(widget);
                            }
                        });
                    }, () -> {
                        throw new IllegalStateException(
                                "Dashboard is not attached to a UI");
                    });

                    return "Widgets reordered successfully";
                } catch (Exception e) {
                    return "Error reordering widgets: " + e.getMessage();
                }
            }
        };
    }

    private LLMProvider.ToolSpec createAddChartWidgetTool() {
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
                        if (node.has("title")
                                && !node.get("title").isNull()) {
                            title = node.get("title").asString();
                        }
                        if (node.has("queries")
                                && !node.get("queries").isNull()
                                && node.get("queries").isArray()) {
                            queries = new ArrayList<>();
                            for (var q : node.get("queries")) {
                                queries.add(q.asString());
                            }
                        } else if (node.has("query")
                                && !node.get("query").isNull()) {
                            queries = List.of(
                                    node.get("query").asString());
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

                    // Validate queries if provided
                    if (queries != null) {
                        for (String q : queries) {
                            queryValidator.accept(q);
                        }
                    }

                    String widgetId = "chart-" + (++widgetCounter);
                    DashboardWidget widget = chartWidgetCreator
                            .create(widgetId, title, colspan, rowspan);
                    addSelectionCheckbox(widget);

                    dashboard.getUI().ifPresentOrElse(ui -> {
                        ui.access(() -> dashboard.add(widget));
                    }, () -> {
                        dashboard.add(widget);
                    });

                    // Queue data and config for deferred rendering
                    if (queries != null) {
                        Chart chart = (Chart) widget.getContent();
                        ChartEntry entry = ChartEntry.getOrCreate(chart,
                                widgetId);
                        entry.setQueries(queries);
                        entry.setPendingDataUpdate(true);
                        if (configJson != null) {
                            entry.setPendingConfigurationJson(configJson);
                        }
                    }

                    return "{\"widgetId\":\"" + widgetId
                            + "\",\"type\":\"chart\",\"title\":\""
                            + title.replace("\"", "\\\"")
                            + "\",\"message\":\"Chart widget added"
                            + (queries != null ? " with data" : "")
                            + ".\"}";
                } catch (Exception e) {
                    return "Error adding chart widget: " + e.getMessage();
                }
            }
        };
    }

    private LLMProvider.ToolSpec createAddGridWidgetTool() {
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
                        if (node.has("title")
                                && !node.get("title").isNull()) {
                            title = node.get("title").asString();
                        }
                        if (node.has("query")
                                && !node.get("query").isNull()) {
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

                    // Validate query if provided
                    if (query != null) {
                        queryValidator.accept(query);
                    }

                    String widgetId = "grid-" + (++widgetCounter);
                    DashboardWidget widget = gridWidgetCreator
                            .create(widgetId, title, colspan, rowspan);
                    addSelectionCheckbox(widget);

                    dashboard.getUI().ifPresentOrElse(ui -> {
                        ui.access(() -> dashboard.add(widget));
                    }, () -> {
                        dashboard.add(widget);
                    });

                    // Queue data for deferred rendering
                    if (query != null) {
                        GridTools gt = gridToolsMap.get(widgetId);
                        gt.setCurrentSqlQuery(query);
                        gt.setPendingDataQuery(query);
                    }

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

    private LLMProvider.ToolSpec createRemoveWidgetTool() {
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

                    DashboardWidget targetWidget = findWidgetById(widgetId);

                    if (targetWidget == null) {
                        return "Error: Widget with ID '" + widgetId
                                + "' not found";
                    }

                    final DashboardWidget toRemove = targetWidget;
                    dashboard.getUI().ifPresentOrElse(ui -> {
                        ui.access(() -> dashboard.remove(toRemove));
                    }, () -> {
                        dashboard.remove(toRemove);
                    });

                    gridToolsMap.remove(widgetId);

                    return "Widget '" + widgetId + "' removed successfully";
                } catch (Exception e) {
                    return "Error removing widget: " + e.getMessage();
                }
            }
        };
    }

    // ===== Helpers =====

    private DashboardWidget findWidgetById(String widgetId) {
        for (DashboardWidget widget : dashboard.getWidgets()) {
            if (widget.getId().isPresent()
                    && widget.getId().get().equals(widgetId)) {
                return widget;
            }
        }
        return null;
    }

    private LLMProvider.ToolSpec prefixTool(
            LLMProvider.ToolSpec original, String widgetId) {
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return original.getName() + "_" + widgetId;
            }

            @Override
            public String getDescription() {
                return "[Widget: " + widgetId + "] "
                        + original.getDescription();
            }

            @Override
            public String getParametersSchema() {
                return original.getParametersSchema();
            }

            @Override
            public String execute(String arguments) {
                return original.execute(arguments);
            }
        };
    }
}
