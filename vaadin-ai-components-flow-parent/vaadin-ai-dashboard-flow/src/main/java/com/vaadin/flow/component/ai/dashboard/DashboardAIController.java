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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.chart.ChartConfigurationApplier;
import com.vaadin.flow.component.ai.chart.ChartEntry;
import com.vaadin.flow.component.ai.chart.ChartRegistry;
import com.vaadin.flow.component.ai.chart.ChartTools;
import com.vaadin.flow.component.ai.chart.DataConverter;
import com.vaadin.flow.component.ai.chart.DefaultDataConverter;
import com.vaadin.flow.component.ai.orchestrator.AIController;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.DatabaseTools;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Series;
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
 * <p>
 * Chart tools use a {@link ChartRegistry} with dynamic chart resolution, so
 * the LLM uses a {@code chartId} parameter to target specific charts. There is
 * no need for per-widget tool prefixing for chart operations.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class DashboardAIController implements AIController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DashboardAIController.class);

    private static final String DEFAULT_DATA_SOURCE = "default";

    private final Dashboard dashboard;
    private final Map<String, DatabaseProvider> databaseProviders;
    private final DashboardTools dashboardTools;

    private final ChartRegistry chartRegistry;
    private final DataConverter dataConverter = new DefaultDataConverter();
    private final ChartConfigurationApplier configurationApplier = new ChartConfigurationApplier();

    private final Map<String, GridTools> gridToolsMap = new LinkedHashMap<>();
    private final Map<String, String> widgetDataSourceMap = new LinkedHashMap<>();

    private int widgetCounter = 0;

    /**
     * Creates a new AI dashboard controller with a single database provider.
     *
     * @param dashboard
     *            the dashboard component to control
     * @param databaseProvider
     *            the database provider for schema and query execution
     */
    public DashboardAIController(Dashboard dashboard,
            DatabaseProvider databaseProvider) {
        this(dashboard,
                Map.of(DEFAULT_DATA_SOURCE, Objects.requireNonNull(
                        databaseProvider, "Database provider cannot be null")));
    }

    /**
     * Creates a new AI dashboard controller with multiple named database
     * providers. Each provider represents a different data source that widgets
     * can use.
     *
     * @param dashboard
     *            the dashboard component to control
     * @param databaseProviders
     *            a map of data source names to database providers, must not be
     *            empty
     */
    public DashboardAIController(Dashboard dashboard,
            Map<String, DatabaseProvider> databaseProviders) {
        this.dashboard = Objects.requireNonNull(dashboard,
                "Dashboard cannot be null");
        Objects.requireNonNull(databaseProviders,
                "Database providers cannot be null");
        if (databaseProviders.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one database provider is required");
        }
        this.databaseProviders = new LinkedHashMap<>(databaseProviders);
        this.dashboardTools = new DashboardTools(dashboard);
        this.chartRegistry = new ChartRegistry(
                this::findChartById,
                this::getChartWidgetIds);
        this.chartRegistry.setQueryValidator(q -> databaseProviders.values()
                .iterator().next().executeQuery(q));
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
                1. get_database_schema() - Get database schema to understand available data.
                2. getDashboardState() - Get current dashboard state with all widgets
                3. addChartWidget(title, query, config, colspan, rowspan, dataSource) - Add a new chart widget with data
                4. addGridWidget(title, query, colspan, rowspan, dataSource) - Add a new grid widget with data
                5. removeWidget(widgetId) - Remove a widget from the dashboard

                For EXISTING CHART widgets (use widgetId as chartId):
                6. get_chart_state(chartId) - Get chart's current state
                7. update_chart_data_source(chartId, queries) - Update chart data with SQL queries
                8. update_chart_configuration(chartId, configuration) - Update chart configuration

                For EXISTING GRID widgets (use widgetId suffix in tool names):
                9. getGridCurrentState_{widgetId}() - Get grid's current state
                10. updateGridData_{widgetId}(query) - Update grid data with SQL query

                For ALL widgets:
                11. updateWidget(widgetId, title, colspan, rowspan) - Update widget layout properties
                12. reorderWidgets(widgetIds) - Reorder widgets by providing IDs in the desired order

                WORKFLOW:
                1. ALWAYS call getDashboardState() FIRST before doing anything else. Widget selections \
                can change between requests, so you must check the current state every time.
                2. Use get_database_schema() to understand available data
                3. Create widgets with addChartWidget() or addGridWidget() - ALWAYS provide the query \
                and config parameters to populate them with data immediately
                4. Use chart tools (get_chart_state, update_chart_data_source, update_chart_configuration) \
                with the widget ID as chartId to update existing chart widgets
                5. Use updateWidget() to adjust layout (title, size)

                IMPORTANT: When creating a widget, ALWAYS provide the query parameter (and config \
                for charts) so the widget is populated with data immediately. The chart and grid \
                tools are only available for widgets that already exist.

                """
                + ChartTools.getSystemPrompt() + "\n\n"
                + GridTools.getSystemPrompt();
    }

    @Override
    public List<LLMProvider.ToolSpec> getTools() {
        List<LLMProvider.ToolSpec> tools = new ArrayList<>();

        // Database schema tools
        if (databaseProviders.size() == 1) {
            tools.addAll(DatabaseTools.createAll(
                    databaseProviders.values().iterator().next()));
        } else {
            for (Map.Entry<String, DatabaseProvider> entry : databaseProviders
                    .entrySet()) {
                tools.add(createNamedSchemaTool(entry.getKey(),
                        entry.getValue()));
            }
        }

        // Dashboard layout tools
        tools.addAll(dashboardTools.getTools());

        // Widget creation/removal tools
        tools.add(createAddChartWidgetTool());
        tools.add(createAddGridWidgetTool());
        tools.add(createRemoveWidgetTool());

        // Chart tools (shared across all charts via registry)
        tools.addAll(ChartTools.createAll(chartRegistry));

        // Per-widget grid tools (still prefixed)
        for (Map.Entry<String, GridTools> entry : gridToolsMap.entrySet()) {
            String widgetId = entry.getKey();
            GridTools gridTools = entry.getValue();
            for (LLMProvider.ToolSpec tool : gridTools.getTools()) {
                tools.add(prefixTool(tool, widgetId));
            }
        }

        return tools;
    }

    @Override
    public void onRequestCompleted() {
        // Render pending chart updates via registry
        for (var mapEntry : chartRegistry.getEntries().entrySet()) {
            String chartId = mapEntry.getKey();
            ChartEntry entry = mapEntry.getValue();
            if (!entry.hasPendingState()) {
                continue;
            }
            try {
                Chart chart = chartRegistry.getChart(chartId);
                applyPendingChartState(chart, entry, chartId);
            } catch (IllegalArgumentException e) {
                // Chart was removed — skip silently
            } catch (Exception e) {
                LOGGER.error("Error rendering chart for widget {}",
                        chartId, e);
            } finally {
                entry.clearPendingState();
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
            List<String> queries = null;
            String configuration = null;

            if (widget.getContent() instanceof Chart chart) {
                type = "chart";
                ChartEntry entry = chartRegistry.getEntries().get(widgetId);
                if (entry != null && !entry.getQueries().isEmpty()) {
                    queries = entry.getQueries();
                    try {
                        String configJson = ChartSerialization
                                .toJSON(chart.getConfiguration());
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
                String sqlQuery = gridToolsMap.get(widgetId)
                        .getCurrentSqlQuery();
                if (sqlQuery != null) {
                    queries = List.of(sqlQuery);
                }
            } else {
                continue;
            }

            widgetStates.add(new WidgetState(widgetId, widget.getTitle(), type,
                    widget.getColspan(), widget.getRowspan(), queries,
                    configuration, widgetDataSourceMap.get(widgetId)));
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
        dashboard.getUI().ifPresent(ui -> ui.access(() -> {
            dashboard.removeAll();
            gridToolsMap.clear();
            widgetDataSourceMap.clear();
            dashboardTools.clearSelectionCheckboxes();

            for (WidgetState ws : state.widgets()) {
                if ("chart".equals(ws.type())) {
                    DashboardWidget widget = createChartWidget(ws.widgetId(),
                            ws.title(), ws.colspan(), ws.rowspan(),
                            ws.dataSource());
                    dashboard.add(widget);
                    if (ws.queries() != null && !ws.queries().isEmpty()) {
                        ChartEntry entry = chartRegistry
                                .getEntry(ws.widgetId());
                        entry.setQueries(ws.queries());
                        try {
                            renderChart(
                                    chartRegistry.getChart(ws.widgetId()),
                                    ws.queries(), ws.configuration(),
                                    ws.dataSource());
                        } catch (Exception e) {
                            LOGGER.error(
                                    "Failed to restore chart widget {}",
                                    ws.widgetId(), e);
                        }
                    }
                } else if ("grid".equals(ws.type())) {
                    DashboardWidget widget = createGridWidget(ws.widgetId(),
                            ws.title(), ws.colspan(), ws.rowspan(),
                            ws.dataSource());
                    dashboard.add(widget);
                    if (ws.queries() != null && !ws.queries().isEmpty()) {
                        GridTools gt = gridToolsMap.get(ws.widgetId());
                        gt.setCurrentSqlQuery(ws.queries().get(0));
                        try {
                            gt.renderGrid(ws.queries().get(0));
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
            int colspan, int rowspan, List<String> queries,
            String configuration,
            String dataSource) implements java.io.Serializable {
    }

    // ===== Widget Creation =====

    private DashboardWidget createChartWidget(String widgetId, String title,
            int colspan, int rowspan, String dataSource) {
        Chart chart = new Chart();
        chart.setSizeFull();
        DashboardWidget widget = new DashboardWidget(title, chart);
        widget.setId(widgetId);
        widget.setColspan(Math.max(1, colspan));
        widget.setRowspan(Math.max(1, rowspan));

        widgetDataSourceMap.put(widgetId, resolveDataSourceName(dataSource));
        dashboardTools.addSelectionCheckbox(widget);

        return widget;
    }

    private DashboardWidget createGridWidget(String widgetId, String title,
            int colspan, int rowspan, String dataSource) {
        Grid<Map<String, Object>> grid = new Grid<>();
        grid.setSizeFull();
        DashboardWidget widget = new DashboardWidget(title, grid);
        widget.setId(widgetId);
        widget.setColspan(Math.max(1, colspan));
        widget.setRowspan(Math.max(1, rowspan));

        DatabaseProvider provider = resolveProvider(dataSource);
        GridTools gridTools = new GridTools(grid, provider);
        gridToolsMap.put(widgetId, gridTools);
        widgetDataSourceMap.put(widgetId, resolveDataSourceName(dataSource));
        dashboardTools.addSelectionCheckbox(widget);

        return widget;
    }

    private DatabaseProvider resolveProvider(String dataSource) {
        if (dataSource == null || dataSource.isBlank()) {
            return databaseProviders.values().iterator().next();
        }
        DatabaseProvider provider = databaseProviders.get(dataSource);
        if (provider == null) {
            throw new IllegalArgumentException(
                    "Unknown data source: '" + dataSource
                            + "'. Available: " + databaseProviders.keySet());
        }
        return provider;
    }

    private String resolveDataSourceName(String dataSource) {
        if (dataSource == null || dataSource.isBlank()) {
            return databaseProviders.keySet().iterator().next();
        }
        return dataSource;
    }

    // ===== Chart Resolution =====

    private Chart findChartById(String widgetId) {
        for (DashboardWidget widget : dashboard.getWidgets()) {
            if (widget.getId().isPresent()
                    && widget.getId().get().equals(widgetId)
                    && widget.getContent() instanceof Chart chart) {
                return chart;
            }
        }
        return null;
    }

    private Set<String> getChartWidgetIds() {
        var ids = new java.util.LinkedHashSet<String>();
        for (DashboardWidget widget : dashboard.getWidgets()) {
            if (widget.getContent() instanceof Chart) {
                widget.getId().ifPresent(ids::add);
            }
        }
        return ids;
    }

    // ===== Rendering =====

    private void applyPendingChartState(Chart chart, ChartEntry entry,
            String widgetId) {
        String configJson = entry.getPendingConfigurationJson();

        entry.applyPendingQueries();
        List<String> effectiveQueries = entry.getQueries();

        if (!effectiveQueries.isEmpty()) {
            String dataSourceName = widgetDataSourceMap.get(widgetId);
            String effectiveConfig = configJson != null ? configJson
                    : ChartSerialization.toJSON(chart.getConfiguration());
            renderChart(chart, effectiveQueries, effectiveConfig,
                    dataSourceName);
        } else if (configJson != null) {
            chart.getUI().ifPresentOrElse(ui -> {
                ui.access(() -> configurationApplier
                        .applyConfiguration(chart, configJson));
            }, () -> {
                throw new IllegalStateException(
                        "Chart is not attached to a UI");
            });
        }
    }

    private void renderChart(Chart chart, List<String> queries,
            String configJson, String dataSourceName) {
        DatabaseProvider provider = resolveProvider(dataSourceName);
        chart.getUI().ifPresentOrElse(ui -> {
            ui.access(() -> {
                Configuration config = chart.getConfiguration();
                List<Series> allSeries = new ArrayList<>();
                for (String query : queries) {
                    var results = provider.executeQuery(query);
                    allSeries.addAll(dataConverter.convertToSeries(results));
                }
                config.setSeries(allSeries.toArray(new Series[0]));
                configurationApplier.applyConfiguration(chart, configJson);
                chart.drawChart();
            });
        }, () -> {
            throw new IllegalStateException(
                    "Chart is not attached to a UI");
        });
    }

    // ===== Tool Factory Methods =====

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
                    - dataSource (string, optional): Name of the data source to use
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
                            "rowspan": { "type": "integer", "description": "Row span", "minimum": 1, "default": 1 },
                            "dataSource": { "type": "string", "description": "Name of the data source to use" }
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
                    String dataSource = null;

                    if (arguments != null && !arguments.isBlank()) {
                        ObjectNode node = (ObjectNode) JacksonUtils
                                .readTree(arguments);
                        if (node.has("title") && !node.get("title").isNull()) {
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
                        if (node.has("dataSource")
                                && !node.get("dataSource").isNull()) {
                            dataSource = node.get("dataSource").asString();
                        }
                    }

                    // Validate queries if provided
                    DatabaseProvider provider = resolveProvider(dataSource);
                    if (queries != null) {
                        for (String q : queries) {
                            provider.executeQuery(q);
                        }
                    }

                    String widgetId = "chart-" + (++widgetCounter);
                    DashboardWidget widget = createChartWidget(widgetId, title,
                            colspan, rowspan, dataSource);

                    dashboard.getUI().ifPresentOrElse(ui -> {
                        ui.access(() -> dashboard.add(widget));
                    }, () -> {
                        dashboard.add(widget);
                    });

                    // Queue data and config for deferred rendering
                    if (queries != null) {
                        ChartEntry entry = chartRegistry.getEntry(widgetId);
                        entry.setQueries(queries);
                        entry.setPendingQueries(queries);
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
                    - dataSource (string, optional): Name of the data source to use
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
                            "rowspan": { "type": "integer", "description": "Row span", "minimum": 1, "default": 1 },
                            "dataSource": { "type": "string", "description": "Name of the data source to use" }
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
                    String dataSource = null;

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
                        if (node.has("dataSource")
                                && !node.get("dataSource").isNull()) {
                            dataSource = node.get("dataSource").asString();
                        }
                    }

                    // Validate query if provided
                    DatabaseProvider provider = resolveProvider(dataSource);
                    if (query != null) {
                        provider.executeQuery(query);
                    }

                    String widgetId = "grid-" + (++widgetCounter);
                    DashboardWidget widget = createGridWidget(widgetId, title,
                            colspan, rowspan, dataSource);

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

                    gridToolsMap.remove(widgetId);
                    widgetDataSourceMap.remove(widgetId);

                    return "Widget '" + widgetId + "' removed successfully";
                } catch (Exception e) {
                    return "Error removing widget: " + e.getMessage();
                }
            }
        };
    }

    // ===== Schema Tools =====

    private LLMProvider.ToolSpec createNamedSchemaTool(String name,
            DatabaseProvider provider) {
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "get_database_schema_" + name;
            }

            @Override
            public String getDescription() {
                return "Gets the database schema for the '"
                        + name
                        + "' data source, including table names, column names "
                        + "with their types, and the SQL dialect.";
            }

            @Override
            public String getParametersSchema() {
                return null;
            }

            @Override
            public String execute(String arguments) {
                return provider.getSchema();
            }
        };
    }

    // ===== Tool Prefixing (for GridTools) =====

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
