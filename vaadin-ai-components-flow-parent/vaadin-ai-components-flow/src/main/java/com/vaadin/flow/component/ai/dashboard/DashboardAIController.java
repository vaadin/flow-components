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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.util.ChartSerialization;

import com.vaadin.flow.component.ai.chart.ChartAITools;
import com.vaadin.flow.component.ai.chart.ChartConfigurationParser;
import com.vaadin.flow.component.ai.chart.ChartEntry;
import com.vaadin.flow.component.ai.chart.ChartRenderer;
import com.vaadin.flow.component.ai.chart.ChartState;
import com.vaadin.flow.component.ai.chart.DataConverter;
import com.vaadin.flow.component.ai.chart.DefaultDataConverter;
import com.vaadin.flow.component.ai.grid.GridAITools;
import com.vaadin.flow.component.ai.grid.GridEntry;
import com.vaadin.flow.component.ai.grid.GridRenderer;
import com.vaadin.flow.component.ai.grid.GridState;
import com.vaadin.flow.component.ai.orchestrator.AIController;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.DatabaseProviderAITools;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.checkbox.Checkbox;
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
            You manage a dashboard of chart and grid widgets. Follow this workflow:

            1. ALWAYS call getDashboardState() FIRST — widget selections change \
            between requests, so check the current state every time
            2. Call get_database_schema() to understand available data
            3. When creating widgets, ALWAYS provide query (and config for charts) \
            to populate them with data immediately

            To modify existing widgets, use their widgetId as chartId/gridId \
            with the corresponding chart or grid tools.
            """;

    private final Dashboard dashboard;
    private final DatabaseProvider databaseProvider;
    private final DataConverter dataConverter;
    private final Map<String, Checkbox> widgetCheckboxes = new LinkedHashMap<>();
    private int widgetCounter = 0;

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
        this.dataConverter = new DefaultDataConverter();
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

        // Database schema tool
        tools.add(DatabaseProviderAITools.getDatabaseSchema(databaseProvider));

        // Dashboard layout tools
        tools.addAll(
                DashboardAITools.createAll(new DashboardAITools.Callbacks() {
                    @Override
                    public String getState() {
                        return getDashboardStateJson();
                    }

                    @Override
                    public void updateWidget(String widgetId, String title,
                            Integer colspan, Integer rowspan) {
                        DashboardWidget widget = resolveWidget(widgetId);
                        dashboard.getElement().getNode()
                                .runWhenAttached(ui -> ui.access(() -> {
                                    if (title != null) {
                                        widget.setTitle(title);
                                    }
                                    if (colspan != null) {
                                        widget.setColspan(colspan);
                                    }
                                    if (rowspan != null) {
                                        widget.setRowspan(rowspan);
                                    }
                                }));
                    }

                    @Override
                    public void reorderWidgets(List<String> widgetIds) {
                        List<DashboardWidget> orderedWidgets = new ArrayList<>();
                        for (String id : widgetIds) {
                            orderedWidgets.add(resolveWidget(id));
                        }
                        dashboard.getElement().getNode()
                                .runWhenAttached(ui -> ui.access(() -> {
                                    dashboard.remove(orderedWidgets);
                                    for (DashboardWidget w : orderedWidgets) {
                                        dashboard.add(w);
                                    }
                                }));
                    }

                    @Override
                    public String addChartWidget(String title, int colspan,
                            int rowspan, List<String> queries,
                            String configJson) {
                        if (queries != null) {
                            for (String q : queries) {
                                databaseProvider.executeQuery(q);
                            }
                        }

                        String widgetId = "chart-" + (++widgetCounter);
                        DashboardWidget widget = createChartWidget(widgetId,
                                title, colspan, rowspan);
                        addSelectionCheckbox(widget);
                        addWidgetToDashboard(widget);

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

                        return widgetId;
                    }

                    @Override
                    public String addGridWidget(String title, int colspan,
                            int rowspan, String query) {
                        if (query != null) {
                            databaseProvider.executeQuery(query);
                        }

                        String widgetId = "grid-" + (++widgetCounter);
                        DashboardWidget widget = createGridWidget(widgetId,
                                title, colspan, rowspan);
                        addSelectionCheckbox(widget);
                        addWidgetToDashboard(widget);

                        if (query != null) {
                            Grid<?> grid = (Grid<?>) widget.getContent();
                            GridEntry entry = GridEntry.getOrCreate(grid,
                                    widgetId);
                            entry.setPendingQuery(query);
                        }

                        return widgetId;
                    }

                    @Override
                    public void removeWidget(String widgetId) {
                        DashboardWidget widget = resolveWidget(widgetId);
                        dashboard.getElement().getNode()
                                .runWhenAttached(ui -> ui.access(
                                        () -> dashboard.remove(widget)));
                    }
                }));

        // Chart tools (shared across all charts, resolved from dashboard)
        tools.addAll(ChartAITools.createAll(new ChartAITools.Callbacks() {
            @Override
            public String getState(String chartId) {
                return ChartEntry.getStateAsJson(resolveChart(chartId),
                        chartId);
            }

            @Override
            public void updateConfiguration(String chartId, String configJson) {
                ChartEntry.getOrCreate(resolveChart(chartId), chartId)
                        .setPendingConfigurationJson(configJson);
            }

            @Override
            public void updateData(String chartId, List<String> queries) {
                for (String q : queries) {
                    databaseProvider.executeQuery(q);
                }
                Chart chart = resolveChart(chartId);
                ChartEntry entry = ChartEntry.getOrCreate(chart, chartId);
                entry.setQueries(queries);
                entry.setPendingDataUpdate(true);
            }

            @Override
            public Set<String> getChartIds() {
                return getChartWidgetIds();
            }
        }));

        // Grid tools (shared across all grids, resolved from dashboard)
        tools.addAll(GridAITools.createAll(new GridAITools.Callbacks() {
            @Override
            public String getState(String gridId) {
                return GridEntry.getStateAsJson(resolveGrid(gridId), gridId);
            }

            @Override
            public void updateData(String gridId, String query) {
                databaseProvider.executeQuery(
                        "SELECT * FROM (" + query + ") AS _v LIMIT 1");
                GridEntry.getOrCreate(resolveGrid(gridId), gridId)
                        .setPendingQuery(query);
            }

            @Override
            public Set<String> getGridIds() {
                return getGridWidgetIds();
            }
        }));

        return tools;
    }

    @Override
    public void onRequestCompleted() {
        for (DashboardWidget widget : dashboard.getWidgets()) {
            if (widget.getContent() instanceof Chart chart) {
                ChartEntry entry = ChartEntry.get(chart);
                if (entry == null || !entry.hasPendingState()) {
                    continue;
                }
                List<String> queries = entry.getQueries();
                if (queries.isEmpty()) {
                    entry.setPendingDataUpdate(false);
                    continue;
                }
                String configJson = entry.getPendingConfigurationJson();
                chart.getElement().getNode()
                        .runWhenAttached(ui -> ui.access(() -> {
                            try {
                                ChartRenderer.renderChart(chart,
                                        databaseProvider, dataConverter,
                                        queries, configJson);
                            } catch (Exception e) {
                                LOGGER.error(
                                        "Error rendering chart for widget {}",
                                        widget.getId().orElse("unknown"), e);
                            } finally {
                                entry.clearPendingState();
                            }
                        }));
            } else if (widget.getContent() instanceof Grid<?> grid) {
                GridEntry entry = GridEntry.get(grid);
                if (entry == null || !entry.hasPendingState()) {
                    continue;
                }
                String query = entry.getPendingQuery();
                grid.getElement().getNode()
                        .runWhenAttached(ui -> ui.access(() -> {
                            try {
                                GridRenderer.renderGrid(
                                        (Grid<Map<String, Object>>) grid,
                                        databaseProvider, query);
                                entry.setCurrentQuery(query);
                            } catch (Exception e) {
                                LOGGER.error(
                                        "Error rendering grid for widget {}",
                                        widget.getId().orElse("unknown"), e);
                            } finally {
                                entry.clearPendingState();
                            }
                        }));
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
            ChartState chartState = null;
            GridState gridState = null;

            if (widget.getContent() instanceof Chart chart) {
                ChartEntry entry = ChartEntry.get(chart);
                if (entry == null) {
                    continue;
                }
                widgetId = entry.getId();
                if (!entry.getQueries().isEmpty()) {
                    chartState = new ChartState(entry.getQueries(),
                            copyConfiguration(chart.getConfiguration()));
                }
            } else if (widget.getContent() instanceof Grid<?> grid) {
                GridEntry entry = GridEntry.get(grid);
                if (entry == null) {
                    continue;
                }
                widgetId = entry.getId();
                if (entry.getCurrentQuery() != null) {
                    gridState = new GridState(entry.getCurrentQuery());
                }
            } else {
                continue;
            }

            widgetStates.add(new WidgetState(widgetId, widget.getTitle(),
                    widget.getColspan(), widget.getRowspan(), chartState,
                    gridState));
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
            widgetCheckboxes.clear();

            for (WidgetState ws : state.widgets()) {
                if (ws.chartState() != null) {
                    DashboardWidget widget = createChartWidget(ws.widgetId(),
                            ws.title(), ws.colspan(), ws.rowspan());
                    addSelectionCheckbox(widget);
                    dashboard.add(widget);
                    Chart chart = (Chart) widget.getContent();
                    chart.setConfiguration(copyConfiguration(
                            ws.chartState().configuration()));
                    ChartEntry entry = ChartEntry.getOrCreate(chart,
                            ws.widgetId());
                    entry.setQueries(ws.chartState().queries());
                    try {
                        ChartRenderer.renderChart(chart, databaseProvider,
                                dataConverter, ws.chartState().queries(),
                                null);
                    } catch (Exception e) {
                        LOGGER.error("Failed to restore chart widget {}",
                                ws.widgetId(), e);
                    }
                } else if (ws.gridState() != null) {
                    DashboardWidget widget = createGridWidget(ws.widgetId(),
                            ws.title(), ws.colspan(), ws.rowspan());
                    addSelectionCheckbox(widget);
                    dashboard.add(widget);
                    Grid<Map<String, Object>> grid = (Grid<Map<String, Object>>) widget
                            .getContent();
                    GridEntry entry = GridEntry.getOrCreate(grid,
                            ws.widgetId());
                    try {
                        GridRenderer.renderGrid(grid, databaseProvider,
                                ws.gridState().query());
                        entry.setCurrentQuery(ws.gridState().query());
                    } catch (Exception e) {
                        LOGGER.error("Failed to restore grid widget {}",
                                ws.widgetId(), e);
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
    public record WidgetState(String widgetId, String title, int colspan,
            int rowspan, ChartState chartState,
            GridState gridState) implements java.io.Serializable {
    }

    // ===== Dashboard State JSON =====

    private String getDashboardStateJson() {
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
            sb.append("{\"widgetId\":\"")
                    .append(widget.getId().orElse("widget-" + i)).append("\"");
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
            sb.append(",\"contentType\":\"").append(contentType).append("\"");
            boolean selected = widget.getId().map(id -> {
                Checkbox cb = widgetCheckboxes.get(id);
                return cb != null && cb.getValue();
            }).orElse(false);
            sb.append(",\"selected\":").append(selected);
            sb.append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    // ===== Selection Checkboxes =====

    private void addSelectionCheckbox(DashboardWidget widget) {
        String widgetId = widget.getId().orElse(null);
        if (widgetId == null || widgetCheckboxes.containsKey(widgetId)) {
            return;
        }
        var checkbox = new Checkbox();
        checkbox.getElement().setAttribute("title", "Select for AI actions");
        widgetCheckboxes.put(widgetId, checkbox);
        widget.setHeaderContent(checkbox);
    }

    // ===== Widget Resolution =====

    private DashboardWidget resolveWidget(String widgetId) {
        for (DashboardWidget widget : dashboard.getWidgets()) {
            if (widget.getId().isPresent()
                    && widget.getId().get().equals(widgetId)) {
                return widget;
            }
        }
        throw new IllegalArgumentException(
                "Widget with ID '" + widgetId + "' not found");
    }

    private Chart resolveChart(String chartId) {
        for (DashboardWidget widget : dashboard.getWidgets()) {
            if (widget.getContent() instanceof Chart chart) {
                ChartEntry entry = ChartEntry.get(chart);
                if (entry != null && chartId.equals(entry.getId())) {
                    return chart;
                }
            }
        }
        throw new IllegalArgumentException(
                "No chart found with ID '" + chartId + "'");
    }

    private Grid<?> resolveGrid(String gridId) {
        for (DashboardWidget widget : dashboard.getWidgets()) {
            if (widget.getContent() instanceof Grid<?> grid) {
                GridEntry entry = GridEntry.get(grid);
                if (entry != null && gridId.equals(entry.getId())) {
                    return grid;
                }
            }
        }
        throw new IllegalArgumentException(
                "No grid found with ID '" + gridId + "'");
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

    private Set<String> getGridWidgetIds() {
        var ids = new LinkedHashSet<String>();
        for (DashboardWidget widget : dashboard.getWidgets()) {
            if (widget.getContent() instanceof Grid<?> grid) {
                GridEntry entry = GridEntry.get(grid);
                if (entry != null) {
                    ids.add(entry.getId());
                }
            }
        }
        return ids;
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

    private static Configuration copyConfiguration(
            Configuration configuration) {
        String json = ChartSerialization.toJSON(configuration);
        return ChartConfigurationParser.parse(json);
    }

    private void addWidgetToDashboard(DashboardWidget widget) {
        dashboard.getUI().ifPresentOrElse(ui -> {
            ui.access(() -> dashboard.add(widget));
        }, () -> {
            dashboard.add(widget);
        });
    }
}
