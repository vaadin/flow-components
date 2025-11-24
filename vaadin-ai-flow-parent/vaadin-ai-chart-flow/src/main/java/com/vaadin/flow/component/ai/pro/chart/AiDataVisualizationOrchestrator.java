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
import com.vaadin.flow.component.ai.orchestrator.BaseAiOrchestrator;
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
 * Orchestrator for AI-powered data visualization generation.
 * <p>
 * This class connects a visualization container, {@link com.vaadin.flow.component.ai.input.AiInput},
 * {@link LLMProvider}, and {@link DatabaseProvider} to enable users to generate
 * and modify visualizations using natural language. The orchestrator supports:
 * </p>
 * <ul>
 * <li>Charts (line, bar, pie, column, area, etc.)</li>
 * <li>Grids/Tables with sortable columns</li>
 * <li>KPI cards showing single metrics</li>
 * </ul>
 * <p>
 * Users can dynamically switch between visualization types (e.g., "show this as a table")
 * without recreating the orchestrator.
 * </p>
 * <p>
 * <strong>Security Notice:</strong> Always use read-only database credentials
 * with access restricted to only necessary tables and views.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class AiDataVisualizationOrchestrator extends BaseAiOrchestrator {

    private final DatabaseProvider databaseProvider;
    private Component visualizationContainer;
    private DataConverter chartDataConverter;

    private String currentUserRequest;
    private UI currentUI;

    // State tracking for capture/restore
    private VisualizationType currentType;
    private String currentSqlQuery;
    private List<Map<String, Object>> currentQueryResults;
    private Map<String, Object> currentConfiguration = new HashMap<>();

    // Event listeners
    private final List<VisualizationStateChangeListener> stateChangeListeners = new ArrayList<>();

    /**
     * Creates a new AI data visualization orchestrator.
     *
     * @param llmProvider
     *            the LLM provider for natural language processing
     * @param databaseProvider
     *            the database provider for schema and query execution
     */
    private AiDataVisualizationOrchestrator(LLMProvider llmProvider,
            DatabaseProvider databaseProvider) {
        super(llmProvider);
        Objects.requireNonNull(databaseProvider,
                "Database provider cannot be null");
        this.databaseProvider = databaseProvider;
        this.chartDataConverter = new DefaultDataConverter();
    }

    /**
     * Creates a new builder for AiDataVisualizationOrchestrator.
     *
     * @param llmProvider
     *            the LLM provider
     * @param databaseProvider
     *            the database provider
     * @return a new builder
     */
    public static Builder create(LLMProvider llmProvider,
            DatabaseProvider databaseProvider) {
        return new Builder(llmProvider, databaseProvider);
    }

    /**
     * Builder for AiDataVisualizationOrchestrator.
     */
    public static class Builder extends BaseBuilder<AiDataVisualizationOrchestrator, Builder> {
        private final DatabaseProvider databaseProvider;
        private Component visualizationContainer;
        private DataConverter chartDataConverter = new DefaultDataConverter();
        private VisualizationType initialType = VisualizationType.CHART;

        private Builder(LLMProvider llmProvider,
                DatabaseProvider databaseProvider) {
            super(llmProvider);
            this.databaseProvider = databaseProvider;
        }

        /**
         * Sets the visualization container component.
         * This can be any Component where visualizations will be rendered.
         *
         * @param container
         *            the container component
         * @return this builder
         */
        public Builder withVisualizationContainer(Component container) {
            this.visualizationContainer = container;
            return this;
        }

        /**
         * Sets the chart component (for backward compatibility).
         * This is equivalent to withVisualizationContainer and sets initial type to CHART.
         *
         * @param chart
         *            the chart component
         * @return this builder
         */
        public Builder withChart(Chart chart) {
            this.visualizationContainer = chart;
            this.initialType = VisualizationType.CHART;
            return this;
        }

        /**
         * Sets a custom data converter for chart visualizations only.
         * Grid and KPI visualizations work directly with raw query results.
         *
         * @param dataConverter
         *            the data converter
         * @return this builder
         */
        public Builder withChartDataConverter(DataConverter dataConverter) {
            this.chartDataConverter = dataConverter;
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
         * Builds the orchestrator.
         *
         * @return the configured orchestrator
         */
        @Override
        public AiDataVisualizationOrchestrator build() {
            AiDataVisualizationOrchestrator orchestrator = new AiDataVisualizationOrchestrator(
                    provider, databaseProvider);
            orchestrator.visualizationContainer = visualizationContainer;
            orchestrator.chartDataConverter = chartDataConverter;
            orchestrator.currentType = initialType;

            // Apply common configuration from base builder
            applyCommonConfiguration(orchestrator);

            return orchestrator;
        }
    }

    /**
     * Gets the visualization container component.
     *
     * @return the container
     */
    public Component getVisualizationContainer() {
        return visualizationContainer;
    }

    /**
     * Gets the database provider.
     *
     * @return the database provider
     */
    public DatabaseProvider getDatabaseProvider() {
        return databaseProvider;
    }

    /**
     * Gets the chart data converter.
     *
     * @return the data converter
     */
    public DataConverter getChartDataConverter() {
        return chartDataConverter;
    }

    /**
     * Gets the current visualization type.
     *
     * @return the current type
     */
    public VisualizationType getCurrentType() {
        return currentType;
    }

    /**
     * Captures the current state of the visualization.
     *
     * @return the current state, or null if no state exists
     */
    public VisualizationState captureState() {
        if (currentSqlQuery == null) {
            return null;
        }
        return VisualizationState.of(currentType, currentSqlQuery,
                currentConfiguration);
    }

    /**
     * Restores the visualization to a previously captured state.
     *
     * @param state
     *            the state to restore
     * @throws IllegalArgumentException
     *             if state is null
     * @throws IllegalStateException
     *             if no UI context is available
     */
    public void restoreState(VisualizationState state) {
        Objects.requireNonNull(state, "Visualization state cannot be null");

        UI ui = validateUiContext();

        if (state.getSqlQuery() != null && !state.getSqlQuery().isEmpty()) {
            try {
                List<Map<String, Object>> results = databaseProvider
                        .executeQuery(state.getSqlQuery());

                // Update internal state
                this.currentSqlQuery = state.getSqlQuery();
                this.currentQueryResults = results;
                this.currentType = state.getType();
                this.currentConfiguration = new HashMap<>(
                        state.getConfiguration());

                // Render visualization
                ui.access(() -> {
                    renderVisualization(state.getType(), results,
                            state.getConfiguration());
                });

            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to restore visualization: " + e.getMessage(),
                        e);
            }
        }
    }

    /**
     * Adds a listener for visualization state change events.
     *
     * @param listener
     *            the listener to add
     */
    public void addStateChangeListener(
            VisualizationStateChangeListener listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        stateChangeListeners.add(listener);
    }

    /**
     * Removes a previously registered state change listener.
     *
     * @param listener
     *            the listener to remove
     */
    public void removeStateChangeListener(
            VisualizationStateChangeListener listener) {
        stateChangeListeners.remove(listener);
    }

    /**
     * Fires a state change event to all registered listeners.
     */
    private void fireStateChangeEvent() {
        if (!stateChangeListeners.isEmpty()) {
            VisualizationState state = captureState();
            if (state != null) {
                VisualizationStateChangeEvent event = new VisualizationStateChangeEvent(
                        this, state);
                for (VisualizationStateChangeListener listener : new ArrayList<>(
                        stateChangeListeners)) {
                    listener.onStateChange(event);
                }
            }
        }
    }

    @Override
    protected void processUserInput(String userMessage) {
        this.currentUserRequest = userMessage;

        UI ui = validateUiContext();
        this.currentUI = ui;

        // Use base class implementation for the rest
        super.processUserInput(userMessage);
    }

    @Override
    protected String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    @Override
    protected void onProcessingComplete() {
        System.out.println(
                "Data visualization orchestrator: Streaming complete");
    }

    @Override
    protected LLMProvider.Tool[] createTools() {
        System.out.println(
                "Data visualization orchestrator: Creating tools for LLM");
        List<LLMProvider.Tool> toolsList = new ArrayList<>();

        // Tool 1: getSchema
        toolsList.add(createGetSchemaTool());

        // Tool 2: updateChart
        toolsList.add(createUpdateChartTool());

        // Tool 3: updateGrid
        toolsList.add(createUpdateGridTool());

        // Tool 4: updateKpi
        toolsList.add(createUpdateKpiTool());

        // Tool 5: changeVisualizationType
        toolsList.add(createChangeVisualizationTypeTool());

        LLMProvider.Tool[] tools = toolsList
                .toArray(new LLMProvider.Tool[0]);
        System.out.println("Data visualization orchestrator: Created "
                + tools.length + " tools for LLM");
        return tools;
    }

    private LLMProvider.Tool createGetSchemaTool() {
        return new LLMProvider.Tool() {
            @Override
            public String getName() {
                return "getSchema";
            }

            @Override
            public String getDescription() {
                return "Retrieves the database schema including tables, columns, and data types. Takes no parameters - call as getSchema()";
            }

            @Override
            public String getParametersSchema() {
                return null;
            }

            @Override
            public String execute(String arguments) {
                System.out.println("Tool 'getSchema' called");
                String schema = databaseProvider.getSchema();
                System.out.println("Tool 'getSchema' returned schema with length: "
                        + schema.length());
                return schema;
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
                        + "query (string) - SQL SELECT query to fetch data, "
                        + "config (object, optional) - Chart configuration including 'type' (line/bar/column/pie/area) and Highcharts options. "
                        + "Example: updateChart({\"query\": \"SELECT name, value FROM data\", \"config\": {\"type\": \"column\", \"title\": {\"text\": \"My Chart\"}}})";
            }

            @Override
            public String getParametersSchema() {
                return null;
            }

            @Override
            public String execute(String arguments) {
                System.out.println(
                        "Tool 'updateChart' called with arguments: "
                                + arguments);
                try {
                    ObjectNode node = (ObjectNode) JacksonUtils
                            .readTree(arguments);
                    String query = node.get("query").asString();

                    // Execute query
                    List<Map<String, Object>> results = databaseProvider
                            .executeQuery(query);

                    // Parse config if provided
                    Map<String, Object> config = new HashMap<>();
                    if (node.has("config") && node.get("config").isObject()) {
                        config.put("chartConfig",
                                node.get("config").toString());
                    }

                    // Update state
                    currentSqlQuery = query;
                    currentQueryResults = results;
                    currentType = VisualizationType.CHART;
                    currentConfiguration = config;

                    // Render
                    if (currentUI != null) {
                        currentUI.access(() -> {
                            renderVisualization(VisualizationType.CHART,
                                    results, config);
                        });
                    }

                    fireStateChangeEvent();

                    return "Chart updated successfully with " + results.size()
                            + " data points";
                } catch (Exception e) {
                    String error = "Error updating chart: " + e.getMessage();
                    System.err.println(error);
                    e.printStackTrace();
                    return error;
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
                        + "query (string) - SQL SELECT query to fetch data. The grid will show all columns returned by the query. "
                        + "Example: updateGrid({\"query\": \"SELECT * FROM customers LIMIT 100\"})";
            }

            @Override
            public String getParametersSchema() {
                return null;
            }

            @Override
            public String execute(String arguments) {
                System.out.println(
                        "Tool 'updateGrid' called with arguments: "
                                + arguments);
                try {
                    ObjectNode node = (ObjectNode) JacksonUtils
                            .readTree(arguments);
                    String query = node.get("query").asString();

                    // Execute query
                    List<Map<String, Object>> results = databaseProvider
                            .executeQuery(query);

                    // Update state
                    currentSqlQuery = query;
                    currentQueryResults = results;
                    currentType = VisualizationType.GRID;
                    currentConfiguration = new HashMap<>();

                    // Render
                    if (currentUI != null) {
                        currentUI.access(() -> {
                            renderVisualization(VisualizationType.GRID, results,
                                    currentConfiguration);
                        });
                    }

                    fireStateChangeEvent();

                    return "Grid updated successfully with " + results.size()
                            + " rows";
                } catch (Exception e) {
                    String error = "Error updating grid: " + e.getMessage();
                    System.err.println(error);
                    e.printStackTrace();
                    return error;
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
                return "Creates or updates a KPI (Key Performance Indicator) card showing a single metric. Parameters: "
                        + "query (string) - SQL SELECT query that returns a single value, "
                        + "label (string, optional) - Label for the KPI, "
                        + "format (string, optional) - Format string (e.g., '$%.2f', '%d%%'). "
                        + "Example: updateKpi({\"query\": \"SELECT SUM(revenue) FROM sales\", \"label\": \"Total Revenue\", \"format\": \"$%.2f\"})";
            }

            @Override
            public String getParametersSchema() {
                return null;
            }

            @Override
            public String execute(String arguments) {
                System.out.println("Tool 'updateKpi' called with arguments: "
                        + arguments);
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

                    // Execute query
                    List<Map<String, Object>> results = databaseProvider
                            .executeQuery(query);

                    // Store config
                    Map<String, Object> config = new HashMap<>();
                    if (label != null)
                        config.put("label", label);
                    if (format != null)
                        config.put("format", format);

                    // Update state
                    currentSqlQuery = query;
                    currentQueryResults = results;
                    currentType = VisualizationType.KPI;
                    currentConfiguration = config;

                    // Render
                    if (currentUI != null) {
                        currentUI.access(() -> {
                            renderVisualization(VisualizationType.KPI, results,
                                    config);
                        });
                    }

                    fireStateChangeEvent();

                    return "KPI updated successfully";
                } catch (Exception e) {
                    String error = "Error updating KPI: " + e.getMessage();
                    System.err.println(error);
                    e.printStackTrace();
                    return error;
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
                return "Changes the visualization type while keeping the current data query. Parameters: "
                        + "type (string) - New visualization type: 'chart', 'grid', or 'kpi', "
                        + "config (object, optional) - Type-specific configuration. "
                        + "Example: changeVisualizationType({\"type\": \"grid\"})";
            }

            @Override
            public String getParametersSchema() {
                return null;
            }

            @Override
            public String execute(String arguments) {
                System.out.println(
                        "Tool 'changeVisualizationType' called with arguments: "
                                + arguments);
                try {
                    if (currentQueryResults == null) {
                        return "No data available. Please execute a query first using updateChart, updateGrid, or updateKpi.";
                    }

                    ObjectNode node = (ObjectNode) JacksonUtils
                            .readTree(arguments);
                    String typeStr = node.get("type").asString().toUpperCase();
                    VisualizationType newType = VisualizationType
                            .valueOf(typeStr);

                    // Parse config if provided
                    Map<String, Object> config = new HashMap<>();
                    if (node.has("config") && node.get("config").isObject()) {
                        ObjectNode configNode = (ObjectNode) node
                                .get("config");
                        for (String key : configNode.propertyNames()) {
                            config.put(key, configNode.get(key).asString());
                        }
                    }

                    // Update state
                    currentType = newType;
                    currentConfiguration = config;

                    // Re-render with same data
                    if (currentUI != null) {
                        currentUI.access(() -> {
                            renderVisualization(newType, currentQueryResults,
                                    config);
                        });
                    }

                    fireStateChangeEvent();

                    return "Visualization type changed to " + newType;
                } catch (Exception e) {
                    String error = "Error changing visualization type: "
                            + e.getMessage();
                    System.err.println(error);
                    e.printStackTrace();
                    return error;
                }
            }
        };
    }

    /**
     * Renders the visualization based on type and data.
     */
    private void renderVisualization(VisualizationType type,
            List<Map<String, Object>> queryResults,
            Map<String, Object> config) {
        if (visualizationContainer == null) {
            System.err.println(
                    "Warning: visualizationContainer is null, cannot render");
            return;
        }

        try {
            switch (type) {
            case CHART -> renderChart(queryResults, config);
            case GRID -> renderGrid(queryResults, config);
            case KPI -> renderKpi(queryResults, config);
            }
        } catch (Exception e) {
            System.err.println("Error rendering visualization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void renderChart(List<Map<String, Object>> queryResults,
            Map<String, Object> config) {
        // Convert data to DataSeries
        DataSeries series = chartDataConverter
                .convertToDataSeries(queryResults);

        Chart chart;
        if (visualizationContainer instanceof Chart) {
            chart = (Chart) visualizationContainer;
        } else {
            // Create new chart if container is not a Chart
            chart = new Chart();
            if (visualizationContainer instanceof Div) {
                ((Div) visualizationContainer).removeAll();
                ((Div) visualizationContainer).add(chart);
            }
        }

        Configuration chartConfig = chart.getConfiguration();
        chartConfig.setSeries(series);

        // Apply config if provided
        String chartConfigJson = (String) config.get("chartConfig");
        if (chartConfigJson != null && !chartConfigJson.isEmpty()) {
            applyChartConfig(chart, chartConfigJson);
        }

        chart.drawChart();
    }

    private void renderGrid(List<Map<String, Object>> queryResults,
            Map<String, Object> config) {
        if (queryResults.isEmpty()) {
            System.err.println("No data to display in grid");
            return;
        }

        Grid<Map<String, Object>> grid = new Grid<>();
        grid.setWidthFull();
        grid.setHeightFull();

        // Get columns from first row
        Map<String, Object> firstRow = queryResults.get(0);
        for (String columnName : firstRow.keySet()) {
            grid.addColumn(row -> {
                Object value = row.get(columnName);
                return value != null ? value.toString() : "";
            }).setHeader(columnName).setSortable(true).setResizable(true);
        }

        grid.setItems(queryResults);

        // Replace container content
        if (visualizationContainer instanceof Div) {
            ((Div) visualizationContainer).removeAll();
            ((Div) visualizationContainer).add(grid);
        } else if (visualizationContainer instanceof Chart) {
            System.err.println(
                    "Cannot render grid in Chart component. Please use a Div or generic Component as visualizationContainer.");
        }
    }

    private void renderKpi(List<Map<String, Object>> queryResults,
            Map<String, Object> config) {
        if (queryResults.isEmpty()) {
            System.err.println("No data for KPI");
            return;
        }

        // Extract value from first row, first column
        Map<String, Object> firstRow = queryResults.get(0);
        Object value = firstRow.values().iterator().next();

        // Format value
        String format = (String) config.get("format");
        String formattedValue;
        if (format != null && value instanceof Number) {
            formattedValue = String.format(format, value);
        } else {
            formattedValue = value != null ? value.toString() : "N/A";
        }

        // Create KPI card
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

        // Replace container content
        if (visualizationContainer instanceof Div) {
            ((Div) visualizationContainer).removeAll();
            ((Div) visualizationContainer).add(kpiCard);
        }
    }

    private void applyChartConfig(Chart chart, String configJson) {
        var configurationNode = (ObjectNode) JacksonUtils.readTree(configJson);

        // Remove 'series' property to prevent overwriting data
        configurationNode.remove("series");

        // Apply the configuration via additionalOptions
        if (chart.getElement() != null) {
            chart.getElement().setPropertyJson("additionalOptions",
                    configurationNode);
        }
    }

    private static final String SYSTEM_PROMPT = """
            You are a data visualization assistant. Your role is to help users create and modify
            data visualizations (charts, grids, KPIs) based on their database data.

            You have access to these tools:
            1. getSchema() - Retrieves the database schema
            2. updateChart(query, config) - Creates/updates a chart visualization
            3. updateGrid(query) - Creates/updates a grid/table visualization
            4. updateKpi(query, label, format) - Creates/updates a KPI card
            5. changeVisualizationType(type, config) - Changes visualization type while keeping current data

            WORKFLOW:
            1. Discovery Phase:
               - Use getSchema() to understand available tables and columns

            2. Visualization Creation:
               - For charts: Call updateChart with SQL query and optional config (chart type, title, etc.)
               - For tables: Call updateGrid with SQL query (will show all returned columns)
               - For KPIs: Call updateKpi with SQL query that returns a single value

            3. Type Switching:
               - User can say "show this as a table" - use changeVisualizationType
               - Reuses current data, just changes how it's displayed

            SQL REQUIREMENTS:
            - Always use SELECT queries only
            - For charts: Query should return data suitable for conversion (typically 2 columns)
            - For grids: Query can return any number of columns
            - For KPIs: Query should return a single value (use aggregation like SUM, AVG, COUNT)

            CHART CONFIGURATION:
            - Available chart types: line, bar, column, pie, area
            - Config example: {"type": "column", "title": {"text": "Sales by Month"}}
            - Use Highcharts JSON format for advanced configuration

            Be helpful and suggest appropriate visualization types based on the data and user's request.
            """;
}
