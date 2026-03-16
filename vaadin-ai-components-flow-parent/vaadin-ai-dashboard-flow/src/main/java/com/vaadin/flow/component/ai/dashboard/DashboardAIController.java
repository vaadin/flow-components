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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.chart.ChartTools;
import com.vaadin.flow.component.ai.orchestrator.AIController;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ObjectNode;

/**
 * AI controller for managing a dashboard with multiple widget types.
 * <p>
 * This controller enables AI-powered dashboard management by providing tools
 * that allow the LLM to create and manage widgets containing charts or grids,
 * update widget properties, and query database data.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class DashboardAIController implements AIController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DashboardAIController.class);

    private final Dashboard dashboard;
    private final DatabaseProvider databaseProvider;
    private final DashboardTools dashboardTools;

    private final Map<String, ChartTools> chartToolsMap = new LinkedHashMap<>();
    private final Map<String, GridTools> gridToolsMap = new LinkedHashMap<>();

    private int widgetCounter = 0;

    /**
     * Creates a new AI dashboard controller.
     *
     * @param dashboard
     *            the dashboard component to control
     * @param databaseProvider
     *            the database provider for schema and query execution
     */
    public DashboardAIController(Dashboard dashboard,
            DatabaseProvider databaseProvider) {
        this.dashboard = Objects.requireNonNull(dashboard,
                "Dashboard cannot be null");
        this.databaseProvider = Objects.requireNonNull(databaseProvider,
                "Database provider cannot be null");
        this.dashboardTools = new DashboardTools(dashboard);
    }

    /**
     * Returns the recommended system prompt for dashboard AI capabilities.
     *
     * @return the system prompt text
     */
    public static String getSystemPrompt() {
        return """
                You are a dashboard assistant that helps users create and manage \
                data visualizations. You can create widgets containing charts or \
                data grids, update their properties, and query database data.

                AVAILABLE TOOLS:
                1. getSchema() - Get database schema to understand available data
                2. getDashboardState() - Get current dashboard state with all widgets
                3. addChartWidget(title, query, config, colspan, rowspan) - Add a new chart widget with data
                4. addGridWidget(title, query, colspan, rowspan) - Add a new grid widget with data
                5. removeWidget(widgetId) - Remove a widget from the dashboard

                For EXISTING CHART widgets (use widgetId suffix in tool names):
                6. getCurrentState_{widgetId}() - Get chart's current state
                7. updateData_{widgetId}(query) - Update chart data with SQL query
                8. updateConfig_{widgetId}(config) - Update chart configuration

                For EXISTING GRID widgets (use widgetId suffix in tool names):
                9. getGridCurrentState_{widgetId}() - Get grid's current state
                10. updateGridData_{widgetId}(query) - Update grid data with SQL query

                For ALL widgets:
                11. updateWidget(widgetId, title, colspan, rowspan) - Update widget layout properties
                12. reorderWidgets(widgetIds) - Reorder widgets by providing IDs in the desired order

                WORKFLOW:
                1. Use getSchema() to understand available data
                2. Create widgets with addChartWidget() or addGridWidget() - ALWAYS provide the query \
                and config parameters to populate them with data immediately
                3. Use getDashboardState() to see existing widgets if needed
                4. Use widget-specific tools (suffixed with widgetId) to update existing widgets
                5. Use updateWidget() to adjust layout (title, size)

                IMPORTANT: When creating a widget, ALWAYS provide the query parameter (and config \
                for charts) so the widget is populated with data immediately. The widget-specific \
                tools (e.g. updateData_{widgetId}) are only available for widgets that already exist.

                """
                + ChartTools.getSystemPrompt() + "\n\n"
                + GridTools.getSystemPrompt();
    }

    @Override
    public List<LLMProvider.ToolDefinition> getTools() {
        List<LLMProvider.ToolDefinition> tools = new ArrayList<>();

        // Database schema tool
        tools.add(databaseProvider.getSchemaTool());

        // Dashboard layout tools
        tools.addAll(dashboardTools.getTools());

        // Widget creation/removal tools
        tools.add(createAddChartWidgetTool());
        tools.add(createAddGridWidgetTool());
        tools.add(createRemoveWidgetTool());

        // Per-widget content tools
        for (Map.Entry<String, ChartTools> entry : chartToolsMap.entrySet()) {
            String widgetId = entry.getKey();
            ChartTools chartTools = entry.getValue();
            for (LLMProvider.ToolDefinition tool : chartTools.getTools()) {
                tools.add(prefixTool(tool, widgetId));
            }
        }
        for (Map.Entry<String, GridTools> entry : gridToolsMap.entrySet()) {
            String widgetId = entry.getKey();
            GridTools gridTools = entry.getValue();
            for (LLMProvider.ToolDefinition tool : gridTools.getTools()) {
                tools.add(prefixTool(tool, widgetId));
            }
        }

        return tools;
    }

    @Override
    public void onRequestCompleted() {
        // Render pending chart updates
        for (Map.Entry<String, ChartTools> entry : chartToolsMap.entrySet()) {
            ChartTools chartTools = entry.getValue();
            if (chartTools.getPendingDataQuery() == null
                    && chartTools.getPendingConfigJson() == null) {
                continue;
            }
            try {
                String sqlQuery = chartTools.getPendingDataQuery() != null
                        ? chartTools.getPendingDataQuery()
                        : chartTools.getCurrentSqlQuery();
                if (sqlQuery != null) {
                    String configJson = chartTools
                            .getPendingConfigJson() != null
                                    ? chartTools.getPendingConfigJson()
                                    : ChartSerialization.toJSON(chartTools
                                            .getChart().getConfiguration());
                    chartTools.renderChart(sqlQuery, configJson);
                } else if (chartTools.getPendingConfigJson() != null) {
                    chartTools.applyConfig(
                            chartTools.getPendingConfigJson());
                }
            } catch (Exception e) {
                LOGGER.error("Error rendering chart for widget {}",
                        entry.getKey(), e);
            } finally {
                chartTools.clearPending();
            }
        }

        // Render pending grid updates
        for (Map.Entry<String, GridTools> entry : gridToolsMap.entrySet()) {
            GridTools gridTools = entry.getValue();
            if (gridTools.getPendingDataQuery() == null) {
                continue;
            }
            try {
                gridTools.renderGrid(gridTools.getPendingDataQuery());
            } catch (Exception e) {
                LOGGER.error("Error rendering grid for widget {}",
                        entry.getKey(), e);
            } finally {
                gridTools.clearPending();
            }
        }
    }

    /**
     * Gets the current dashboard state for persistence.
     *
     * @return the current state
     */
    public DashboardState getState() {
        List<WidgetState> widgetStates = new ArrayList<>();
        for (DashboardWidget widget : dashboard.getWidgets()) {
            String widgetId = widget.getId().orElse(null);
            if (widgetId == null) {
                continue;
            }
            String type;
            String sqlQuery = null;
            String configuration = null;

            if (chartToolsMap.containsKey(widgetId)) {
                type = "chart";
                ChartTools ct = chartToolsMap.get(widgetId);
                sqlQuery = ct.getCurrentSqlQuery();
                if (sqlQuery != null) {
                    try {
                        String configJson = ChartSerialization
                                .toJSON(ct.getChart().getConfiguration());
                        ObjectNode configNode = (ObjectNode) JacksonUtils
                                .readTree(configJson);
                        configNode.remove("series");
                        configuration = configNode.toString();
                    } catch (Exception e) {
                        LOGGER.warn(
                                "Failed to serialize chart config for widget {}",
                                widgetId, e);
                    }
                }
            } else if (gridToolsMap.containsKey(widgetId)) {
                type = "grid";
                sqlQuery = gridToolsMap.get(widgetId).getCurrentSqlQuery();
            } else {
                continue;
            }

            widgetStates.add(new WidgetState(widgetId, widget.getTitle(), type,
                    widget.getColspan(), widget.getRowspan(), sqlQuery,
                    configuration));
        }
        return new DashboardState(widgetStates);
    }

    /**
     * Restores a previously saved dashboard state.
     *
     * @param state
     *            the state to restore
     */
    public void restoreState(DashboardState state) {
        // Clear existing widgets
        dashboard.getUI().ifPresent(ui -> ui.access(() -> {
            dashboard.removeAll();
            chartToolsMap.clear();
            gridToolsMap.clear();
            dashboardTools.clearSelectionCheckboxes();

            for (WidgetState ws : state.widgets()) {
                if ("chart".equals(ws.type())) {
                    DashboardWidget widget = createChartWidget(ws.widgetId(),
                            ws.title(), ws.colspan(), ws.rowspan());
                    dashboard.add(widget);
                    if (ws.sqlQuery() != null) {
                        ChartTools ct = chartToolsMap.get(ws.widgetId());
                        ct.setCurrentSqlQuery(ws.sqlQuery());
                        try {
                            ct.renderChart(ws.sqlQuery(), ws.configuration());
                        } catch (Exception e) {
                            LOGGER.error(
                                    "Failed to restore chart widget {}",
                                    ws.widgetId(), e);
                        }
                    }
                } else if ("grid".equals(ws.type())) {
                    DashboardWidget widget = createGridWidget(ws.widgetId(),
                            ws.title(), ws.colspan(), ws.rowspan());
                    dashboard.add(widget);
                    if (ws.sqlQuery() != null) {
                        GridTools gt = gridToolsMap.get(ws.widgetId());
                        gt.setCurrentSqlQuery(ws.sqlQuery());
                        try {
                            gt.renderGrid(ws.sqlQuery());
                        } catch (Exception e) {
                            LOGGER.error(
                                    "Failed to restore grid widget {}",
                                    ws.widgetId(), e);
                        }
                    }
                }
            }
        }));
    }

    /**
     * State record for persistence.
     */
    public record DashboardState(
            List<WidgetState> widgets) implements java.io.Serializable {
    }

    /**
     * State record for a single widget.
     */
    public record WidgetState(String widgetId, String title, String type,
            int colspan, int rowspan, String sqlQuery,
            String configuration) implements java.io.Serializable {
    }

    // ===== Widget Creation =====

    private DashboardWidget createChartWidget(String widgetId, String title,
            int colspan, int rowspan) {
        Chart chart = new Chart();
        chart.setSizeFull();
        DashboardWidget widget = new DashboardWidget(title, chart);
        widget.setId(widgetId);
        widget.setColspan(Math.max(1, colspan));
        widget.setRowspan(Math.max(1, rowspan));

        ChartTools chartTools = new ChartTools(chart, databaseProvider);
        chartToolsMap.put(widgetId, chartTools);
        dashboardTools.addSelectionCheckbox(widget);

        return widget;
    }

    private DashboardWidget createGridWidget(String widgetId, String title,
            int colspan, int rowspan) {
        Grid<Map<String, Object>> grid = new Grid<>();
        grid.setSizeFull();
        DashboardWidget widget = new DashboardWidget(title, grid);
        widget.setId(widgetId);
        widget.setColspan(Math.max(1, colspan));
        widget.setRowspan(Math.max(1, rowspan));

        GridTools gridTools = new GridTools(grid, databaseProvider);
        gridToolsMap.put(widgetId, gridTools);
        dashboardTools.addSelectionCheckbox(widget);

        return widget;
    }

    // ===== Tool Factory Methods =====

    private LLMProvider.ToolDefinition createAddChartWidgetTool() {
        return new LLMProvider.ToolDefinition() {
            @Override
            public String getName() {
                return "addChartWidget";
            }

            @Override
            public String getDescription() {
                return """
                    Adds a new chart widget to the dashboard, optionally populated with data.
                    IMPORTANT: Always provide query and config to create a widget with data immediately.

                    Parameters:
                    - title (string, optional): Widget title
                    - query (string, optional): SQL SELECT query to populate the chart with data
                    - config (object, optional): Chart configuration (type, title, axes, etc.). \
                    CRITICAL: Always include chart.type. Do NOT include 'series' - data comes from query.
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
                            "query": { "type": "string", "description": "SQL SELECT query to populate the chart" },
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
                    String query = null;
                    String configJson = null;

                    if (arguments != null && !arguments.isBlank()) {
                        ObjectNode node = (ObjectNode) JacksonUtils
                                .readTree(arguments);
                        if (node.has("title") && !node.get("title").isNull()) {
                            title = node.get("title").asString();
                        }
                        if (node.has("query") && !node.get("query").isNull()) {
                            query = node.get("query").asString();
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

                    // Validate query if provided
                    if (query != null) {
                        databaseProvider.executeQuery(query);
                    }

                    String widgetId = "chart-" + (++widgetCounter);
                    DashboardWidget widget = createChartWidget(widgetId, title,
                            colspan, rowspan);

                    dashboard.getUI().ifPresentOrElse(ui -> {
                        ui.access(() -> dashboard.add(widget));
                    }, () -> {
                        dashboard.add(widget);
                    });

                    // Queue data and config for deferred rendering
                    if (query != null) {
                        ChartTools ct = chartToolsMap.get(widgetId);
                        ct.setCurrentSqlQuery(query);
                        ct.setPendingDataQuery(query);
                        if (configJson != null) {
                            ct.setPendingConfigJson(configJson);
                        }
                    }

                    return "{\"widgetId\":\"" + widgetId
                            + "\",\"type\":\"chart\",\"title\":\""
                            + title.replace("\"", "\\\"")
                            + "\",\"message\":\"Chart widget added"
                            + (query != null ? " with data" : "") + ".\"}";
                } catch (Exception e) {
                    return "Error adding chart widget: " + e.getMessage();
                }
            }
        };
    }

    private LLMProvider.ToolDefinition createAddGridWidgetTool() {
        return new LLMProvider.ToolDefinition() {
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

                    // Validate query if provided
                    if (query != null) {
                        databaseProvider.executeQuery(query);
                    }

                    String widgetId = "grid-" + (++widgetCounter);
                    DashboardWidget widget = createGridWidget(widgetId, title,
                            colspan, rowspan);

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

    private LLMProvider.ToolDefinition createRemoveWidgetTool() {
        return new LLMProvider.ToolDefinition() {
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

                    DashboardWidget targetWidget = null;
                    for (DashboardWidget widget : dashboard.getWidgets()) {
                        if (widget.getId().isPresent()
                                && widget.getId().get().equals(widgetId)) {
                            targetWidget = widget;
                            break;
                        }
                    }

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

                    chartToolsMap.remove(widgetId);
                    gridToolsMap.remove(widgetId);

                    return "Widget '" + widgetId + "' removed successfully";
                } catch (Exception e) {
                    return "Error removing widget: " + e.getMessage();
                }
            }
        };
    }

    // ===== Tool Prefixing =====

    private LLMProvider.ToolDefinition prefixTool(
            LLMProvider.ToolDefinition original, String widgetId) {
        return new LLMProvider.ToolDefinition() {
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
