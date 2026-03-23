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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.chart.ChartEntry;
import com.vaadin.flow.component.ai.chart.ChartRenderer;
import com.vaadin.flow.component.ai.grid.GridEntry;
import com.vaadin.flow.component.ai.grid.GridRenderer;
import com.vaadin.flow.component.ai.orchestrator.AIController;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.DatabaseProviderAITools;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.grid.Grid;

/**
 * AI controller for managing a dashboard with multiple widget types.
 * <p>
 * This controller enables AI-powered dashboard management by providing tools
 * that allow the LLM to create and manage widgets containing charts or grids,
 * update widget properties, and query database data.
 * </p>
 * <p>
 * Chart state ({@link ChartEntry}) is stored directly on each {@link Chart}
 * instance via {@link ChartEntry#getOrCreate(Chart, String)}, and grid state
 * ({@link GridEntry}) is stored directly on each {@link Grid} instance via
 * {@link GridEntry#getOrCreate(Grid, String)}. Both use a component ID
 * parameter to target specific instances — resolved dynamically from the
 * dashboard widgets.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class DashboardAIController implements AIController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DashboardAIController.class);

    private static final String SYSTEM_PROMPT = """
            You are a dashboard assistant that helps users create and manage \
            data visualizations. You can create widgets containing charts or \
            data grids, update their properties, and query database data.

            AVAILABLE TOOLS:
            1. get_database_schema() - Get database schema to understand available data.
            2. getDashboardState() - Get current dashboard state with all widgets
            3. addChartWidget(title, query, config, colspan, rowspan) - Add a new chart widget with data
            4. addGridWidget(title, query, colspan, rowspan) - Add a new grid widget with data
            5. removeWidget(widgetId) - Remove a widget from the dashboard

            For EXISTING CHART widgets (use widgetId as chartId):
            6. dashboard_get_chart_state(chartId) - Get chart's current state
            7. dashboard_update_chart_data_source(chartId, queries) - Update chart data with SQL queries
            8. dashboard_update_chart_configuration(chartId, configuration) - Update chart configuration

            For EXISTING GRID widgets (use widgetId as gridId):
            9. dashboard_get_grid_state(gridId) - Get grid's current state
            10. dashboard_update_grid_data(gridId, query) - Update grid data with SQL query

            For ALL widgets:
            11. updateWidget(widgetId, title, colspan, rowspan) - Update widget layout properties
            12. reorderWidgets(widgetIds) - Reorder widgets by providing IDs in the desired order

            WORKFLOW:
            1. ALWAYS call getDashboardState() FIRST before doing anything else. Widget selections \
            can change between requests, so you must check the current state every time.
            2. Use get_database_schema() to understand available data
            3. Create widgets with addChartWidget() or addGridWidget() - ALWAYS provide the query \
            and config parameters to populate them with data immediately
            4. Use chart tools (dashboard_get_chart_state, dashboard_update_chart_data_source, dashboard_update_chart_configuration) \
            with the widget ID as chartId to update existing chart widgets
            5. Use grid tools (dashboard_get_grid_state, dashboard_update_grid_data) with the widget ID as gridId \
            to update existing grid widgets
            6. Use updateWidget() to adjust layout (title, size)

            IMPORTANT: When creating a widget, ALWAYS provide the query parameter (and config \
            for charts) so the widget is populated with data immediately. The chart and grid \
            tools are only available for widgets that already exist.

            """;

    private final Dashboard dashboard;
    private final DatabaseProvider databaseProvider;
    private final DashboardAITools dashboardTools;
    private final ChartRenderer chartRenderer;
    private final GridRenderer gridRenderer;

    /**
     * Creates a new AI dashboard controller with a database provider.
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
        this.chartRenderer = new ChartRenderer(databaseProvider);
        this.gridRenderer = new GridRenderer(databaseProvider);
        this.dashboardTools = new DashboardAITools(dashboard,
                databaseProvider::executeQuery, this::createChartWidget,
                this::createGridWidget);
    }

    /**
     * Returns the recommended system prompt for dashboard AI capabilities.
     *
     * @return the system prompt text
     */
    public static String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    @Override
    public List<LLMProvider.ToolSpec> getTools() {
        List<LLMProvider.ToolSpec> tools = new ArrayList<>();
        tools.add(DatabaseProviderAITools.getDatabaseSchema(databaseProvider));
        tools.addAll(dashboardTools.getTools());
        return tools;
    }

    @Override
    public void onRequestCompleted() {
        for (DashboardWidget widget : dashboard.getWidgets()) {
            if (widget.getContent() instanceof Chart chart) {
                try {
                    chartRenderer.applyPendingState(chart);
                } catch (Exception e) {
                    LOGGER.error("Error rendering chart for widget {}",
                            widget.getId().orElse("unknown"), e);
                }
            } else if (widget.getContent() instanceof Grid<?> grid) {
                try {
                    gridRenderer.applyPendingState(grid);
                } catch (Exception e) {
                    LOGGER.error("Error rendering grid for widget {}",
                            widget.getId().orElse("unknown"), e);
                }
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
            String widgetId;
            String type;
            List<String> queries = null;
            String configuration = null;

            if (widget.getContent() instanceof Chart chart) {
                ChartEntry entry = ChartEntry.get(chart);
                if (entry == null) {
                    continue;
                }
                widgetId = entry.getId();
                type = "chart";
                ChartEntry.ChartState chartState = ChartEntry.getState(chart);
                if (chartState != null) {
                    queries = chartState.queries();
                    configuration = chartState.configuration();
                }
            } else if (widget.getContent() instanceof Grid<?> grid) {
                GridEntry entry = GridEntry.get(grid);
                if (entry == null) {
                    continue;
                }
                widgetId = entry.getId();
                type = "grid";
                String sqlQuery = entry.getQuery();
                if (sqlQuery != null) {
                    queries = List.of(sqlQuery);
                }
            } else {
                continue;
            }

            widgetStates.add(new WidgetState(widgetId, widget.getTitle(), type,
                    widget.getColspan(), widget.getRowspan(), queries,
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
    @SuppressWarnings("unchecked")
    public void restoreState(DashboardState state) {
        dashboard.getUI().ifPresent(ui -> ui.access(() -> {
            dashboard.removeAll();
            dashboardTools.clearSelectionCheckboxes();

            for (WidgetState ws : state.widgets()) {
                if ("chart".equals(ws.type())) {
                    DashboardWidget widget = createChartWidget(ws.widgetId(),
                            ws.title(), ws.colspan(), ws.rowspan());
                    dashboardTools.addSelectionCheckbox(widget);
                    dashboard.add(widget);
                    if (ws.queries() != null && !ws.queries().isEmpty()) {
                        Chart chart = (Chart) widget.getContent();
                        ChartEntry entry = ChartEntry.getOrCreate(chart,
                                ws.widgetId());
                        entry.setQueries(ws.queries());
                        try {
                            chartRenderer.renderChart(chart, ws.queries(),
                                    ws.configuration());
                        } catch (Exception e) {
                            LOGGER.error("Failed to restore chart widget {}",
                                    ws.widgetId(), e);
                        }
                    }
                } else if ("grid".equals(ws.type())) {
                    DashboardWidget widget = createGridWidget(ws.widgetId(),
                            ws.title(), ws.colspan(), ws.rowspan());
                    dashboardTools.addSelectionCheckbox(widget);
                    dashboard.add(widget);
                    if (ws.queries() != null && !ws.queries().isEmpty()) {
                        Grid<Map<String, Object>> grid = (Grid<Map<String, Object>>) widget
                                .getContent();
                        GridEntry entry = GridEntry.get(grid);
                        entry.setQuery(ws.queries().get(0));
                        try {
                            gridRenderer.renderGrid(grid, ws.queries().get(0));
                        } catch (Exception e) {
                            LOGGER.error("Failed to restore grid widget {}",
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
            String configuration) implements java.io.Serializable {
    }

    // ===== Widget Creation =====

    private DashboardWidget createChartWidget(String widgetId, String title,
            int colspan, int rowspan) {
        Chart chart = new Chart();
        chart.setSizeFull();
        ChartEntry.getOrCreate(chart, widgetId);
        DashboardWidget widget = new DashboardWidget(title, chart);
        widget.setId(widgetId);
        widget.setColspan(Math.max(1, colspan));
        widget.setRowspan(Math.max(1, rowspan));
        return widget;
    }

    private DashboardWidget createGridWidget(String widgetId, String title,
            int colspan, int rowspan) {
        Grid<Map<String, Object>> grid = new Grid<>();
        grid.setSizeFull();
        GridEntry.getOrCreate(grid, widgetId);
        DashboardWidget widget = new DashboardWidget(title, grid);
        widget.setId(widgetId);
        widget.setColspan(Math.max(1, colspan));
        widget.setRowspan(Math.max(1, rowspan));
        return widget;
    }
}
