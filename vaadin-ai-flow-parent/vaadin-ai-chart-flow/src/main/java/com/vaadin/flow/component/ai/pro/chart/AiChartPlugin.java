/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.pro.chart;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.orchestrator.AiOrchestrator;
import com.vaadin.flow.component.ai.orchestrator.AiPlugin;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.internal.JacksonUtils;
import tools.jackson.databind.node.ObjectNode;

import java.util.*;

/**
 * AI plugin for creating interactive chart visualizations from database data.
 * <p>
 * This plugin enables AI-powered chart generation by providing tools that allow
 * the LLM to query database schemas and create/update chart visualizations
 * based on natural language requests.
 * </p>
 *
 * <h3>Example Usage:</h3>
 *
 * <pre>
 * Chart chart = new Chart();
 * DatabaseProvider dbProvider = new MyDatabaseProvider();
 * AiChartPlugin plugin = new AiChartPlugin(chart, dbProvider);
 *
 * String systemPrompt = "You are a data visualization assistant. "
 *         + AiChartPlugin.getSystemPrompt();
 *
 * AiOrchestrator.builder(provider, systemPrompt)
 *         .withMessageList(messageList)
 *         .withInput(messageInput)
 *         .withPlugin(plugin)
 *         .build();
 *
 * // User can now say: "Show me monthly revenue as a line chart"
 * </pre>
 *
 * @author Vaadin Ltd
 */
public class AiChartPlugin implements AiPlugin {

    /**
     * Interface for converting database query results to chart-compatible data
     * structures.
     */
    public interface DataConverter {
        /**
         * Converts database query results to a DataSeries for chart rendering.
         *
         * @param queryResults
         *            the query results as a list of row maps
         * @return a DataSeries containing the converted data
         */
        DataSeries convertToDataSeries(List<Map<String, Object>> queryResults);
    }

    private final Chart chart;
    private final DatabaseProvider databaseProvider;
    private final DataConverter chartDataConverter;

    // State tracking for persistence
    private String currentSqlQuery;
    private String currentConfiguration;

    // UI reference for thread-safe updates
    private transient UI currentUI;

    /**
     * Creates a new AI chart plugin.
     *
     * @param chart
     *            the chart component to update
     * @param databaseProvider
     *            the database provider for schema and query execution
     */
    public AiChartPlugin(Chart chart, DatabaseProvider databaseProvider) {
        this.chart = Objects.requireNonNull(chart, "Chart cannot be null");
        this.databaseProvider = Objects.requireNonNull(databaseProvider,
                "Database provider cannot be null");
        this.chartDataConverter = new DefaultDataConverter();
    }

    /**
     * Returns the recommended system prompt for chart visualization
     * capabilities.
     * <p>
     * Use this when creating an orchestrator with the AiChartPlugin:
     * </p>
     *
     * <pre>
     * String systemPrompt = "You are a helpful assistant. "
     *         + AiChartPlugin.getSystemPrompt();
     *
     * AiOrchestrator orchestrator = AiOrchestrator.builder(provider, systemPrompt)
     *         .withPlugin(plugin)
     *         .build();
     * </pre>
     *
     * @return the system prompt text describing chart visualization capabilities
     */
    public static String getSystemPrompt() {
        return """
                You have access to chart visualization capabilities:

                TOOLS:
                1. getSchema() - Retrieves database schema (tables, columns, types)
                2. updateChart(query, config) - Creates/updates a chart visualization
                   - Supports: line, bar, column, pie, area charts
                   - Config includes chart type and Highcharts options

                WORKFLOW:
                1. Use getSchema() to understand available data
                2. Create appropriate SQL queries (SELECT only)
                3. Choose the right chart type based on user request and data

                GUIDELINES:
                - For trends over time: use line or area charts
                - For comparisons: use bar or column charts
                - For proportions: use pie charts
                """;
    }

    @Override
    public void onAttached(AiOrchestrator orchestrator) {
        // Capture UI context from the chart component
        chart.getUI().ifPresent(ui -> this.currentUI = ui);
    }

    @Override
    public void onDetached() {
        this.currentUI = null;
    }

    @Override
    public List<LLMProvider.Tool> getTools() {
        return List.of(createGetSchemaTool(), createUpdateChartTool());
    }

    @Override
    public Object captureState() {
        if (currentSqlQuery == null) {
            return null;
        }
        return new ChartState(currentSqlQuery, currentConfiguration);
    }

    @Override
    public void restoreState(Object state) {
        if (state instanceof ChartState chartState) {
            this.currentSqlQuery = chartState.sqlQuery;
            this.currentConfiguration = chartState.configuration;

            if (currentSqlQuery != null && currentConfiguration != null) {
                try {
                    renderChart(currentSqlQuery, currentConfiguration);
                } catch (Exception e) {
                    System.err.println("Failed to restore chart: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public String getPluginId() {
        return "AiChart";
    }

    /**
     * State record for persistence.
     */
    private record ChartState(String sqlQuery,
            String configuration) implements java.io.Serializable {
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

                    // Store configuration for persistence
                    String config = node.has("config")
                            ? node.get("config").toString()
                            : "{}";

                    currentSqlQuery = query;
                    currentConfiguration = config;

                    renderChart(query, config);

                    return "Chart updated successfully";
                } catch (Exception e) {
                    return "Error updating chart: " + e.getMessage();
                }
            }
        };
    }

    // ===== Rendering Methods =====

    private void renderChart(String sqlQuery, String configJson)
            throws Exception {
        List<Map<String, Object>> results = databaseProvider
                .executeQuery(sqlQuery);

        DataSeries series = chartDataConverter.convertToDataSeries(results);

        // Update chart on UI thread
        Runnable updateChart = () -> {
            Configuration config = chart.getConfiguration();
            config.setSeries(series);

            applyChartConfig(chart, configJson);

            chart.drawChart();
        };

        if (currentUI != null) {
            currentUI.access(() -> updateChart.run());
        } else {
            updateChart.run();
        }
    }

    private void applyChartConfig(Chart chart, String configJson) {
        try {
            ObjectNode configNode = (ObjectNode) JacksonUtils
                    .readTree(configJson);

            Configuration config = chart.getConfiguration();

            // Apply chart type if specified
            if (configNode.has("type")) {
                String chartTypeStr = configNode.get("type").asString()
                        .toLowerCase();
                ChartType chartType = switch (chartTypeStr) {
                case "line" -> ChartType.LINE;
                case "bar" -> ChartType.BAR;
                case "column" -> ChartType.COLUMN;
                case "pie" -> ChartType.PIE;
                case "area" -> ChartType.AREA;
                default -> ChartType.LINE;
                };
                config.getChart().setType(chartType);
            }

            // Apply title if specified
            if (configNode.has("title")) {
                var titleNode = configNode.get("title");
                if (titleNode.isObject() && titleNode.has("text")) {
                    config.setTitle(titleNode.get("text").asString());
                } else if (titleNode.isString()) {
                    config.setTitle(titleNode.asString());
                }
            }

            // Apply tooltip configuration
            if (configNode.has("tooltip") && configNode.get("tooltip").isObject()) {
                var tooltipNode = configNode.get("tooltip");
                if (tooltipNode.has("pointFormat")) {
                    config.getTooltip()
                            .setPointFormat(tooltipNode.get("pointFormat").asString());
                }
            }
        } catch (Exception e) {
            System.err.println("Error applying chart config: " + e.getMessage());
        }
    }

    /**
     * Default implementation of DataConverter. Assumes first column is X-axis
     * categories and remaining columns are Y-axis series.
     */
    private static class DefaultDataConverter implements DataConverter {
        @Override
        public DataSeries convertToDataSeries(
                List<Map<String, Object>> queryResults) {
            if (queryResults.isEmpty()) {
                return new DataSeries();
            }

            DataSeries series = new DataSeries();
            Map<String, Object> firstRow = queryResults.get(0);
            List<String> columnNames = new ArrayList<>(firstRow.keySet());

            // If we have 2 columns: first is category, second is value
            if (columnNames.size() == 2) {
                for (Map<String, Object> row : queryResults) {
                    String category = String
                            .valueOf(row.get(columnNames.get(0)));
                    Object value = row.get(columnNames.get(1));
                    Number numValue = value instanceof Number ? (Number) value
                            : Integer.valueOf(0);
                    DataSeriesItem item = new DataSeriesItem();
                    item.setName(category);
                    item.setY(numValue);
                    series.add(item);
                }
            } else {
                // For other cases, use row values directly
                for (Map<String, Object> row : queryResults) {
                    for (Object value : row.values()) {
                        if (value instanceof Number) {
                            DataSeriesItem item = new DataSeriesItem();
                            item.setY((Number) value);
                            series.add(item);
                        }
                    }
                }
            }

            return series;
        }
    }
}
