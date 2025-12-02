/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.pro.chart;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.orchestrator.AiPlugin;
import com.vaadin.flow.component.ai.orchestrator.AiOrchestrator;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.internal.JacksonUtils;
import tools.jackson.databind.node.ObjectNode;

import java.util.*;

/**
 * Plugin that adds data visualization capabilities to AI orchestrators.
 * <p>
 * This plugin enables AI-powered generation of:
 * </p>
 * <ul>
 * <li><b>Charts</b> - Line, bar, pie, column, area charts using Vaadin Charts</li>
 * <li><b>Grids</b> - Tables with sortable columns using Vaadin Grid</li>
 * <li><b>KPIs</b> - Key Performance Indicator cards showing single metrics</li>
 * </ul>
 * <p>
 * The plugin provides tools for the AI to query databases and render visualizations.
 * Users can interact naturally: "Show sales by region", "Convert to a table", etc.
 * </p>
 *
 * <h3>Example Usage:</h3>
 * <pre>
 * // Create the plugin
 * DataVisualizationPlugin plugin = DataVisualizationPlugin.create(databaseProvider)
 *     .withVisualizationContainer(chartDiv)
 *     .withChartDataConverter(customConverter)
 *     .build();
 *
 * // Add to orchestrator
 * AiOrchestrator orchestrator = AiOrchestrator.create(llmProvider)
 *     .withMessageList(messageList)
 *     .withInput(messageInput)
 *     .withPlugin(plugin)
 *     .build();
 *
 * // User can now say: "Show me monthly revenue as a chart"
 * </pre>
 *
 * @author Vaadin Ltd
 */
public class DataVisualizationPlugin implements AiPlugin {

    private final DatabaseProvider databaseProvider;
    private Component visualizationContainer;
    private DataConverter chartDataConverter;
    private VisualizationType currentType;

    // State tracking
    private String currentSqlQuery;
    private List<Map<String, Object>> currentQueryResults;
    private Map<String, Object> currentConfiguration = new HashMap<>();

    // UI reference for thread-safe updates
    private transient UI currentUI;
    private transient AiOrchestrator orchestrator;

    private DataVisualizationPlugin(DatabaseProvider databaseProvider) {
        this.databaseProvider = Objects.requireNonNull(databaseProvider,
                "Database provider cannot be null");
        this.chartDataConverter = new DefaultDataConverter();
        this.currentType = VisualizationType.CHART;
    }

    /**
     * Returns the recommended system prompt for data visualization capabilities.
     * <p>
     * Use this when creating an orchestrator with the DataVisualizationPlugin:
     * </p>
     * <pre>
     * String systemPrompt = "You are a helpful assistant. "
     *     + DataVisualizationPlugin.getSystemPrompt();
     *
     * AiOrchestrator orchestrator = AiOrchestrator.create(provider, systemPrompt)
     *     .withPlugin(plugin)
     *     .build();
     * </pre>
     *
     * @return the system prompt text describing data visualization capabilities
     */
    public static String getSystemPrompt() {
        return """
                You have access to data visualization capabilities:

                TOOLS:
                1. getSchema() - Retrieves database schema (tables, columns, types)
                2. updateChart(query, config) - Creates/updates a chart visualization
                   - Supports: line, bar, column, pie, area charts
                   - Config includes chart type and Highcharts options
                3. updateGrid(query) - Creates/updates a grid/table visualization
                   - Shows all columns from query results
                4. updateKpi(query, label, format) - Creates/updates a KPI card
                   - Shows single metric with optional formatting
                5. changeVisualizationType(type, config) - Changes visualization type
                   - Reuses current data, just changes display

                WORKFLOW:
                1. Use getSchema() to understand available data
                2. Create appropriate SQL queries (SELECT only)
                3. Choose the right visualization type based on user request and data
                4. User can ask to change types: "show this as a table"

                GUIDELINES:
                - For trends over time: use line or area charts
                - For comparisons: use bar or column charts
                - For proportions: use pie charts
                - For detailed data inspection: use grids
                - For single metrics: use KPIs
                """;
    }

    /**
     * Creates a new builder for DataVisualizationPlugin.
     *
     * @param databaseProvider
     *            the database provider for schema and query execution
     * @return a new builder
     */
    public static Builder create(DatabaseProvider databaseProvider) {
        return new Builder(databaseProvider);
    }

    /**
     * Builder for DataVisualizationPlugin.
     */
    public static class Builder {
        private final DatabaseProvider databaseProvider;
        private Component visualizationContainer;
        private DataConverter chartDataConverter = new DefaultDataConverter();
        private VisualizationType initialType = VisualizationType.CHART;

        private Builder(DatabaseProvider databaseProvider) {
            this.databaseProvider = databaseProvider;
        }

        /**
         * Sets the container where visualizations will be rendered.
         *
         * @param container
         *            the container component (Div, Chart, etc.)
         * @return this builder
         */
        public Builder withVisualizationContainer(Component container) {
            this.visualizationContainer = container;
            return this;
        }

        /**
         * Sets a custom data converter for chart visualizations.
         * Grid and KPI visualizations work directly with raw query results.
         *
         * @param converter
         *            the data converter
         * @return this builder
         */
        public Builder withChartDataConverter(DataConverter converter) {
            this.chartDataConverter = converter;
            return this;
        }

        /**
         * Sets the initial visualization type.
         *
         * @param type
         *            the initial type
         * @return this builder
         */
        public Builder withInitialType(VisualizationType type) {
            this.initialType = type;
            return this;
        }

        /**
         * Builds the plugin.
         *
         * @return the configured plugin
         */
        public DataVisualizationPlugin build() {
            DataVisualizationPlugin plugin = new DataVisualizationPlugin(
                    databaseProvider);
            plugin.visualizationContainer = visualizationContainer;
            plugin.chartDataConverter = chartDataConverter;
            plugin.currentType = initialType;
            return plugin;
        }
    }

    @Override
    public void onAttached(AiOrchestrator orchestrator) {
        this.orchestrator = orchestrator;

        // Get UI context - try multiple sources
        // 1. From visualization container
        if (visualizationContainer != null) {
            visualizationContainer.getUI().ifPresent(ui -> this.currentUI = ui);
        }

        // 2. Fallback to orchestrator's input component
        if (this.currentUI == null
                && orchestrator.getInput() instanceof Component) {
            ((Component) orchestrator.getInput()).getUI()
                    .ifPresent(ui -> this.currentUI = ui);
        }
    }

    @Override
    public void onDetached() {
        this.orchestrator = null;
        this.currentUI = null;
    }

    @Override
    public List<LLMProvider.Tool> getTools() {
        List<LLMProvider.Tool> tools = new ArrayList<>();
        tools.add(createGetSchemaTool());
        tools.add(createUpdateChartTool());
        tools.add(createUpdateGridTool());
        tools.add(createUpdateKpiTool());
        tools.add(createChangeVisualizationTypeTool());
        return tools;
    }

    @Override
    public Object captureState() {
        if (currentSqlQuery == null) {
            return null;
        }
        return VisualizationState.of(currentType, currentSqlQuery,
                currentConfiguration);
    }

    @Override
    public void restoreState(Object state) {
        if (state instanceof VisualizationState visualizationState) {
            if (visualizationState.getSqlQuery() != null) {
                try {
                    List<Map<String, Object>> results = databaseProvider
                            .executeQuery(visualizationState.getSqlQuery());

                    this.currentSqlQuery = visualizationState.getSqlQuery();
                    this.currentQueryResults = results;
                    this.currentType = visualizationState.getType();
                    this.currentConfiguration = new HashMap<>(
                            visualizationState.getConfiguration());

                    // Try to render with UI access, or directly if no UI
                    if (currentUI != null) {
                        currentUI.access(() -> {
                            renderVisualization(visualizationState.getType(),
                                    results,
                                    visualizationState.getConfiguration());
                        });
                    } else {
                        // Try to get UI from visualization container at execution time
                        if (visualizationContainer != null) {
                            visualizationContainer.getUI().ifPresentOrElse(
                                    ui -> ui.access(() -> renderVisualization(
                                            visualizationState.getType(), results,
                                            visualizationState.getConfiguration())),
                                    () -> renderVisualization(
                                            visualizationState.getType(), results,
                                            visualizationState.getConfiguration()));
                        } else {
                            renderVisualization(visualizationState.getType(), results,
                                    visualizationState.getConfiguration());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Failed to restore visualization: "
                            + e.getMessage());
                }
            }
        }
    }

    @Override
    public String getPluginId() {
        return "DataVisualization";
    }

    // ===== Tool Implementations =====

    private LLMProvider.Tool createGetSchemaTool() {
        return new LLMProvider.Tool() {
            @Override
            public String getName() {
                return "getSchema";
            }

            @Override
            public String getDescription() {
                return "Retrieves the database schema including tables, columns, and data types. Takes no parameters.";
            }

            @Override
            public String getParametersSchema() {
                return null;
            }

            @Override
            public String execute(String arguments) {
                return databaseProvider.getSchema();
            }
        };
    }

    private LLMProvider.Tool createUpdateChartTool() {
        return new LLMProvider.Tool() {
            @Override
            public String getName() {
                return "updateChart";
            }

            @Override
            public String getDescription() {
                return "Creates or updates a chart visualization. Parameters: "
                        + "query (string) - SQL SELECT query, "
                        + "config (object, optional) - Chart configuration with 'type' (line/bar/column/pie/area) and Highcharts options.";
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

                    List<Map<String, Object>> results = databaseProvider
                            .executeQuery(query);

                    Map<String, Object> config = new HashMap<>();
                    if (node.has("config") && node.get("config").isObject()) {
                        config.put("chartConfig",
                                node.get("config").toString());
                    }

                    currentSqlQuery = query;
                    currentQueryResults = results;
                    currentType = VisualizationType.CHART;
                    currentConfiguration = config;

                    // Try to render with UI access, or directly if no UI
                    if (currentUI != null) {
                        currentUI.access(() -> {
                            renderVisualization(VisualizationType.CHART,
                                    results, config);
                        });
                    } else {
                        // Try to get UI from visualization container at execution time
                        if (visualizationContainer != null) {
                            visualizationContainer.getUI().ifPresentOrElse(
                                    ui -> ui.access(() -> renderVisualization(
                                            VisualizationType.CHART, results,
                                            config)),
                                    () -> renderVisualization(
                                            VisualizationType.CHART, results,
                                            config));
                        } else {
                            renderVisualization(VisualizationType.CHART, results,
                                    config);
                        }
                    }

                    return "Chart updated successfully with " + results.size()
                            + " data points";
                } catch (Exception e) {
                    return "Error updating chart: " + e.getMessage();
                }
            }
        };
    }

    private LLMProvider.Tool createUpdateGridTool() {
        return new LLMProvider.Tool() {
            @Override
            public String getName() {
                return "updateGrid";
            }

            @Override
            public String getDescription() {
                return "Creates or updates a grid/table visualization. Parameters: "
                        + "query (string) - SQL SELECT query. All returned columns will be shown.";
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

                    List<Map<String, Object>> results = databaseProvider
                            .executeQuery(query);

                    currentSqlQuery = query;
                    currentQueryResults = results;
                    currentType = VisualizationType.GRID;
                    currentConfiguration = new HashMap<>();

                    // Try to render with UI access, or directly if no UI
                    if (currentUI != null) {
                        currentUI.access(() -> {
                            renderVisualization(VisualizationType.GRID, results,
                                    currentConfiguration);
                        });
                    } else {
                        // Try to get UI from visualization container at execution time
                        if (visualizationContainer != null) {
                            visualizationContainer.getUI().ifPresentOrElse(
                                    ui -> ui.access(() -> renderVisualization(
                                            VisualizationType.GRID, results,
                                            currentConfiguration)),
                                    () -> renderVisualization(
                                            VisualizationType.GRID, results,
                                            currentConfiguration));
                        } else {
                            renderVisualization(VisualizationType.GRID, results,
                                    currentConfiguration);
                        }
                    }

                    return "Grid updated successfully with " + results.size()
                            + " rows";
                } catch (Exception e) {
                    return "Error updating grid: " + e.getMessage();
                }
            }
        };
    }

    private LLMProvider.Tool createUpdateKpiTool() {
        return new LLMProvider.Tool() {
            @Override
            public String getName() {
                return "updateKpi";
            }

            @Override
            public String getDescription() {
                return "Creates or updates a KPI card. Parameters: "
                        + "query (string) - SQL SELECT query returning a single value, "
                        + "label (string, optional) - Label for the KPI, "
                        + "format (string, optional) - Format string (e.g., '$%.2f').";
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
                    String label = node.has("label")
                            ? node.get("label").asString()
                            : null;
                    String format = node.has("format")
                            ? node.get("format").asString()
                            : null;

                    List<Map<String, Object>> results = databaseProvider
                            .executeQuery(query);

                    Map<String, Object> config = new HashMap<>();
                    if (label != null)
                        config.put("label", label);
                    if (format != null)
                        config.put("format", format);

                    currentSqlQuery = query;
                    currentQueryResults = results;
                    currentType = VisualizationType.KPI;
                    currentConfiguration = config;

                    // Try to render with UI access, or directly if no UI
                    if (currentUI != null) {
                        currentUI.access(() -> {
                            renderVisualization(VisualizationType.KPI, results,
                                    config);
                        });
                    } else {
                        // Try to get UI from visualization container at execution time
                        if (visualizationContainer != null) {
                            visualizationContainer.getUI().ifPresentOrElse(
                                    ui -> ui.access(() -> renderVisualization(
                                            VisualizationType.KPI, results,
                                            config)),
                                    () -> renderVisualization(
                                            VisualizationType.KPI, results,
                                            config));
                        } else {
                            renderVisualization(VisualizationType.KPI, results,
                                    config);
                        }
                    }

                    return "KPI updated successfully";
                } catch (Exception e) {
                    return "Error updating KPI: " + e.getMessage();
                }
            }
        };
    }

    private LLMProvider.Tool createChangeVisualizationTypeTool() {
        return new LLMProvider.Tool() {
            @Override
            public String getName() {
                return "changeVisualizationType";
            }

            @Override
            public String getDescription() {
                return "Changes the visualization type while keeping current data. Parameters: "
                        + "type (string) - New type: 'chart', 'grid', or 'kpi', "
                        + "config (object, optional) - Type-specific configuration.";
            }

            @Override
            public String getParametersSchema() {
                return null;
            }

            @Override
            public String execute(String arguments) {
                try {
                    if (currentQueryResults == null) {
                        return "No data available. Please execute a query first.";
                    }

                    ObjectNode node = (ObjectNode) JacksonUtils
                            .readTree(arguments);
                    String typeStr = node.get("type").asString().toUpperCase();
                    VisualizationType newType = VisualizationType
                            .valueOf(typeStr);

                    Map<String, Object> config = new HashMap<>();
                    if (node.has("config") && node.get("config").isObject()) {
                        ObjectNode configNode = (ObjectNode) node
                                .get("config");
                        for (String key : configNode.propertyNames()) {
                            config.put(key, configNode.get(key).asString());
                        }
                    }

                    currentType = newType;
                    currentConfiguration = config;

                    // Try to render with UI access, or directly if no UI
                    if (currentUI != null) {
                        currentUI.access(() -> {
                            renderVisualization(newType, currentQueryResults,
                                    config);
                        });
                    } else {
                        // Try to get UI from visualization container at execution time
                        if (visualizationContainer != null) {
                            visualizationContainer.getUI().ifPresentOrElse(
                                    ui -> ui.access(() -> renderVisualization(
                                            newType, currentQueryResults,
                                            config)),
                                    () -> renderVisualization(newType,
                                            currentQueryResults, config));
                        } else {
                            renderVisualization(newType, currentQueryResults,
                                    config);
                        }
                    }

                    return "Visualization type changed to " + newType;
                } catch (Exception e) {
                    return "Error changing visualization type: "
                            + e.getMessage();
                }
            }
        };
    }

    // ===== Rendering Methods =====

    private void renderVisualization(VisualizationType type,
            List<Map<String, Object>> queryResults,
            Map<String, Object> config) {
        if (visualizationContainer == null) {
            return;
        }

        try {
            switch (type) {
            case CHART -> renderChart(queryResults, config);
            case GRID -> renderGrid(queryResults, config);
            case KPI -> renderKpi(queryResults, config);
            }
        } catch (Exception e) {
            System.err.println(
                    "Error rendering visualization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void renderChart(List<Map<String, Object>> queryResults,
            Map<String, Object> config) {
        DataSeries series = chartDataConverter
                .convertToDataSeries(queryResults);

        Chart chart;
        if (visualizationContainer instanceof Chart) {
            chart = (Chart) visualizationContainer;
        } else {
            chart = new Chart();
            chart.setSizeFull();
            if (visualizationContainer instanceof Div) {
                ((Div) visualizationContainer).removeAll();
                ((Div) visualizationContainer).add(chart);
            }
        }

        Configuration chartConfig = chart.getConfiguration();
        chartConfig.setSeries(series);

        String chartConfigJson = (String) config.get("chartConfig");
        if (chartConfigJson != null && !chartConfigJson.isEmpty()) {
            applyChartConfig(chart, chartConfigJson);
        }

        chart.drawChart();
    }

    private void renderGrid(List<Map<String, Object>> queryResults,
            Map<String, Object> config) {
        if (queryResults.isEmpty()) {
            return;
        }

        Grid<Map<String, Object>> grid = new Grid<>();
        grid.setWidthFull();
        grid.setHeightFull();

        Map<String, Object> firstRow = queryResults.get(0);
        for (String columnName : firstRow.keySet()) {
            grid.addColumn(row -> {
                Object value = row.get(columnName);
                return value != null ? value.toString() : "";
            }).setHeader(columnName).setSortable(true).setResizable(true);
        }

        grid.setItems(queryResults);

        if (visualizationContainer instanceof Div) {
            ((Div) visualizationContainer).removeAll();
            ((Div) visualizationContainer).add(grid);
        }
    }

    private void renderKpi(List<Map<String, Object>> queryResults,
            Map<String, Object> config) {
        if (queryResults.isEmpty()) {
            return;
        }

        Map<String, Object> firstRow = queryResults.get(0);
        Object value = firstRow.values().iterator().next();

        String format = (String) config.get("format");
        String formattedValue;
        if (format != null && value instanceof Number) {
            formattedValue = String.format(format, value);
        } else {
            formattedValue = value != null ? value.toString() : "N/A";
        }

        VerticalLayout kpiCard = new VerticalLayout();
        kpiCard.setSpacing(false);
        kpiCard.setPadding(true);
        kpiCard.setAlignItems(VerticalLayout.Alignment.CENTER);
        kpiCard.setJustifyContentMode(
                VerticalLayout.JustifyContentMode.CENTER);
        kpiCard.setWidthFull();
        kpiCard.setHeightFull();

        String label = (String) config.get("label");
        if (label != null) {
            H3 labelComponent = new H3(label);
            labelComponent.getStyle().set("margin", "0").set("color",
                    "var(--lumo-secondary-text-color)");
            kpiCard.add(labelComponent);
        }

        Span valueComponent = new Span(formattedValue);
        valueComponent.getStyle().set("font-size", "3em").set("font-weight",
                "bold").set("margin", "0.5em 0");
        kpiCard.add(valueComponent);

        if (visualizationContainer instanceof Div) {
            ((Div) visualizationContainer).removeAll();
            ((Div) visualizationContainer).add(kpiCard);
        }
    }

    private void applyChartConfig(Chart chart, String configJson) {
        try {
            var configurationNode = (ObjectNode) JacksonUtils
                    .readTree(configJson);

            Configuration config = chart.getConfiguration();

            // Apply chart type if specified
            if (configurationNode.has("type")) {
                String chartTypeStr = configurationNode.get("type").asString()
                        .toLowerCase();
                switch (chartTypeStr) {
                case "line":
                    config.getChart().setType(ChartType.LINE);
                    break;
                case "bar":
                    config.getChart().setType(ChartType.BAR);
                    break;
                case "column":
                    config.getChart().setType(ChartType.COLUMN);
                    break;
                case "pie":
                    config.getChart().setType(ChartType.PIE);
                    break;
                case "area":
                    config.getChart().setType(ChartType.AREA);
                    break;
                }
            }

            // Apply title if specified
            if (configurationNode.has("title")
                    && configurationNode.get("title").isObject()) {
                var titleNode = (ObjectNode) configurationNode.get("title");
                if (titleNode.has("text")) {
                    config.setTitle(titleNode.get("text").asString());
                }
            }

            // Apply tooltip configuration
            if (configurationNode.has("tooltip")
                    && configurationNode.get("tooltip").isObject()) {
                var tooltipNode = (ObjectNode) configurationNode.get("tooltip");
                if (tooltipNode.has("pointFormat")) {
                    config.getTooltip().setPointFormat(
                            tooltipNode.get("pointFormat").asString());
                }
            }

            // Remove properties that were already applied
            configurationNode.remove("series");
            configurationNode.remove("type");
            configurationNode.remove("title");
            configurationNode.remove("tooltip");

            // Apply any remaining options as additional options
            if (!configurationNode.isEmpty()) {
                if (chart.getElement() != null) {
                    chart.getElement().setPropertyJson("additionalOptions",
                            configurationNode);
                }
            }
        } catch (Exception e) {
            System.err.println("Error applying chart config: " + e.getMessage());
        }
    }
}
