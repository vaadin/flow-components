/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.pro.chart;

import com.vaadin.flow.component.ai.orchestrator.AiPlugin;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.charts.util.ChartSerialization;
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
                2. getCurrentState() - Returns current chart state (query and configuration)
                3. updateData(query) - Updates chart data with SQL SELECT query
                4. updateConfig(config) - Updates chart configuration (type, title, tooltip, etc.)
                   - Supports: line, bar, column, pie, area charts
                   - Config includes chart type and Highcharts options

                WORKFLOW:
                1. ALWAYS call getCurrentState() FIRST before making any changes
                2. Use getSchema() if you need to understand available data
                3. Use updateData() to change data source
                4. Use updateConfig() to change chart appearance

                IMPORTANT:
                - ALWAYS check getCurrentState() before making any modifications
                - This helps you understand what's already configured and make informed changes
                - For trends over time: use line or area charts
                - For comparisons: use bar or column charts
                - For proportions: use pie charts
                - You can update data and config independently
                """;
    }

    @Override
    public List<LLMProvider.Tool> getTools() {
        return List.of(
                createGetSchemaTool(),
                createGetCurrentStateTool(),
                createUpdateDataTool(),
                createUpdateConfigTool()
        );
    }


    public ChartState getState() {
        if (currentSqlQuery == null) {
            return null;
        }
        // Get configuration from chart as JSON
        String configJson = ChartSerialization.toJSON(chart.getConfiguration());
        return new ChartState(currentSqlQuery, configJson);
    }


    public void restoreState(ChartState state) {
        this.currentSqlQuery = state.sqlQuery;

        if (currentSqlQuery != null && state.configuration != null) {
            try {
                renderChart(currentSqlQuery, state.configuration);
            } catch (Exception e) {
                System.err.println("Failed to restore chart: " + e.getMessage());
            }
        }
    }
    
    /**
     * State record for persistence.
     */
    public record ChartState(String sqlQuery,
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

    private LLMProvider.Tool createGetCurrentStateTool() {
        return new LLMProvider.Tool() {
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
                ChartState state = getState();

                if (state == null) {
                    return "{\"status\":\"empty\",\"message\":\"No chart has been created yet\"}";
                }

                StringBuilder result = new StringBuilder("{");
                result.append("\"query\":\"").append(state.sqlQuery().replace("\"", "\\\"")).append("\"");
                result.append(",\"configuration\":").append(state.configuration());
                result.append("}");

                return result.toString();
            }
        };
    }

    private LLMProvider.Tool createUpdateDataTool() {
        return new LLMProvider.Tool() {
            @Override
            public String getName() {
                return "updateData";
            }

            @Override
            public String getDescription() {
                return "Updates the chart data using a SQL SELECT query. Parameters: query (string) - SQL SELECT query to retrieve data.";
            }

            @Override
            public String getParametersSchema() {
                return null;
            }

            @Override
            public String execute(String arguments) {
                try {
                    ObjectNode node = (ObjectNode) JacksonUtils.readTree(arguments);
                    String query = node.get("query").asString();

                    currentSqlQuery = query;

                    // Get existing configuration from chart
                    String config = ChartSerialization.toJSON(chart.getConfiguration());
                    renderChart(query, config);

                    return "Chart data updated successfully";
                } catch (Exception e) {
                    return "Error updating chart data: " + e.getMessage();
                }
            }
        };
    }

    private LLMProvider.Tool createUpdateConfigTool() {
        return new LLMProvider.Tool() {
            @Override
            public String getName() {
                return "updateConfig";
            }

            @Override
            public String getDescription() {
                return "Updates the chart configuration (type, title, tooltip, etc.). Parameters: config (object) - Chart configuration with 'type' (line/bar/column/pie/area) and Highcharts options.";
            }

            @Override
            public String getParametersSchema() {
                return null;
            }

            @Override
            public String execute(String arguments) {
                try {
                    ObjectNode node = (ObjectNode) JacksonUtils.readTree(arguments);
                    String config = node.get("config").toString();

                    // Apply configuration directly to the chart
                    applyChartConfig(chart, config);

                    // If we have data, re-render to apply changes
                    if (currentSqlQuery != null) {
                        String currentConfig = ChartSerialization.toJSON(chart.getConfiguration());
                        renderChart(currentSqlQuery, currentConfig);
                        return "Chart configuration updated successfully";
                    } else {
                        return "Chart configuration saved. Use updateData to add data to the chart.";
                    }
                } catch (Exception e) {
                    return "Error updating chart configuration: " + e.getMessage();
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

        chart.getUI().ifPresentOrElse(currentUI -> {
            currentUI.access(() -> {
                 Configuration config = chart.getConfiguration();
                config.setSeries(series);

                applyChartConfig(chart, configJson);

                chart.drawChart();
            });
        }, () -> {
            throw new IllegalStateException(
                    "Chart is not attached to a UI");
        });
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
