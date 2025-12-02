/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.pro.chart;

import com.vaadin.flow.component.ai.orchestrator.AiController;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.flow.internal.JacksonUtils;
import tools.jackson.databind.node.ObjectNode;

import java.util.*;

/**
 * AI controller for creating interactive chart visualizations from database data.
 * <p>
 * This controller enables AI-powered chart generation by providing tools that allow
 * the LLM to query database schemas and create/update chart visualizations
 * based on natural language requests.
 * </p>
 *
 * <h3>Example Usage:</h3>
 *
 * <pre>
 * Chart chart = new Chart();
 * DatabaseProvider dbProvider = new MyDatabaseProvider();
 * ChartAiController controller = new ChartAiController(chart, dbProvider);
 *
 * String systemPrompt = "You are a data visualization assistant. "
 *         + ChartAiController.getSystemPrompt();
 *
 * AiOrchestrator.builder(provider, systemPrompt)
 *         .withMessageList(messageList)
 *         .withInput(messageInput)
 *         .withController(controller)
 *         .build();
 *
 * // User can now say: "Show me monthly revenue as a line chart"
 * </pre>
 *
 * @author Vaadin Ltd
 */
public class ChartAiController implements AiController {

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
    private DataConverter chartDataConverter;
    private final ChartConfigurationApplier configurationApplier;

    // State tracking for persistence
    private String currentSqlQuery;

    // State change listeners
    private final List<ChartStateChangeListener> stateChangeListeners = new ArrayList<>();

    // Pending chart rendering (deferred until request completes)
    private PendingRender pendingRender;

    private static class PendingRender {
        final String sqlQuery;
        final String configJson;

        PendingRender(String sqlQuery, String configJson) {
            this.sqlQuery = sqlQuery;
            this.configJson = configJson;
        }
    }

    /**
     * Creates a new AI chart controller.
     *
     * @param chart
     *            the chart component to update
     * @param databaseProvider
     *            the database provider for schema and query execution
     */
    public ChartAiController(Chart chart, DatabaseProvider databaseProvider) {
        this.chart = Objects.requireNonNull(chart, "Chart cannot be null");
        this.databaseProvider = Objects.requireNonNull(databaseProvider,
                "Database provider cannot be null");
        this.chartDataConverter = new DefaultDataConverter();
        this.configurationApplier = new ChartConfigurationApplier();
    }

    /**
     * Sets a custom data converter for transforming query results into chart data.
     * <p>
     * Use this to customize how database query results are converted to chart series.
     * If not set, the default converter is used which assumes the first column is the
     * category/X-axis and remaining columns are values/Y-axis.
     * </p>
     *
     * @param dataConverter the data converter to use, cannot be null
     */
    public void setDataConverter(DataConverter dataConverter) {
        this.chartDataConverter = Objects.requireNonNull(dataConverter,
                "Data converter cannot be null");
    }

    /**
     * Adds a listener for chart state changes.
     * <p>
     * The listener will be notified whenever the chart's SQL query or configuration
     * is updated through the controller's tools.
     * </p>
     *
     * <h3>Example Usage:</h3>
     *
     * <pre>
     * controller.addStateChangeListener(event -> {
     *     ChartState state = event.getState();
     *     System.out.println("Chart updated - Query: " + state.sqlQuery());
     *
     *     // Save state to database
     *     persistState(state);
     * });
     * </pre>
     *
     * @param listener the listener to add, cannot be null
     */
    public void addStateChangeListener(ChartStateChangeListener listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        stateChangeListeners.add(listener);
    }

    /**
     * Removes a previously added state change listener.
     *
     * @param listener the listener to remove
     * @return true if the listener was found and removed, false otherwise
     */
    public boolean removeStateChangeListener(ChartStateChangeListener listener) {
        return stateChangeListeners.remove(listener);
    }

    /**
     * Returns the recommended system prompt for chart visualization
     * capabilities.
     * <p>
     * Use this when creating an orchestrator with the ChartAiController:
     * </p>
     *
     * <pre>
     * String systemPrompt = "You are a helpful assistant. "
     *         + ChartAiController.getSystemPrompt();
     *
     * AiOrchestrator orchestrator = AiOrchestrator.builder(provider, systemPrompt)
     *         .withController(controller)
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
                   - Supports 31 chart types: line, spline, area, areaspline, bar, column, pie, scatter,
                     gauge, arearange, columnrange, areasplinerange, boxplot, errorbar, bubble, funnel,
                     waterfall, pyramid, solidgauge, heatmap, treemap, polygon, candlestick, flags,
                     timeline, ohlc, organization, sankey, xrange, gantt, bullet
                   - Config includes: chart model (dimensions, margins, spacing, borders, background),
                     axes (x, y, z, color), title, subtitle, tooltip, legend, credits, pane, exporting

                WORKFLOW:
                1. ALWAYS call getCurrentState() FIRST before making any changes
                2. Use getSchema() if you need to understand available data
                3. Use updateData() to change data source
                4. Use updateConfig() to change chart appearance

                IMPORTANT:
                - ALWAYS check getCurrentState() before making any modifications
                - This helps you understand what's already configured and make informed changes
                - NEVER include 'series' data in updateConfig() - chart data comes ONLY from updateData()
                - updateData() executes SQL and populates the chart series automatically
                - updateConfig() only handles visual appearance (type, styling, labels, etc.)
                - Chart type recommendations:
                  * Trends over time: line, spline, area, areaspline
                  * Comparisons: bar, column
                  * Proportions: pie, funnel
                  * Distributions: boxplot, errorbar
                  * Relationships: scatter, bubble
                  * Specialized: gauge, heatmap, treemap, waterfall, gantt, sankey
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

    @Override
    public void onRequestCompleted() {
        // Execute any pending chart render
        if (pendingRender != null) {
            try {
                // If we have data, render with data; otherwise just apply config
                if (pendingRender.sqlQuery != null) {
                    renderChart(pendingRender.sqlQuery, pendingRender.configJson);
                } else {
                    applyChartConfig(chart, pendingRender.configJson);
                }
                // Fire state change event after rendering
                fireStateChangeEvent();
            } catch (Exception e) {
                System.err.println("Error rendering chart: " + e.getMessage());
                e.printStackTrace();
            } finally {
                pendingRender = null;
            }
        }
    }

    public ChartState getState() {
        if (currentSqlQuery == null) {
            return null;
        }
        // Get configuration from chart as JSON and remove series field
        String configJson = ChartSerialization.toJSON(chart.getConfiguration());

        // Remove series from configuration JSON since it's data, not config
        try {
            ObjectNode configNode = (ObjectNode) JacksonUtils.readTree(configJson);
            configNode.remove("series");
            configJson = configNode.toString();
        } catch (Exception e) {
            System.err.println("Failed to remove series from config: " + e.getMessage());
        }

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

                    // Defer rendering until request completes
                    pendingRender = new PendingRender(query, config);

                    return "Chart data update queued successfully";
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
                return "Updates the chart configuration (type, title, tooltip, etc.). Parameters: config (object) - Chart configuration object. IMPORTANT: Do NOT include 'series' in the config - chart data is managed separately via updateData tool.";
            }

            @Override
            public String getParametersSchema() {
                return """
                    {
                      "type": "object",
                      "properties": {
                        "config": {
                          "type": "object",
                          "description": "Chart configuration object. NOTE: Do NOT include 'series' - data is managed separately via updateData tool.",
                          "properties": {
                            "chart": {
                              "type": "object",
                              "description": "Chart model options including type, dimensions, margins, spacing, borders, background",
                              "properties": {
                                "type": {
                                  "type": "string",
                                  "description": "Chart type - must be inside chart object to match Vaadin Charts structure",
                                  "enum": ["line", "spline", "area", "areaspline", "bar", "column", "pie", "scatter", "gauge", "arearange", "columnrange", "areasplinerange", "boxplot", "errorbar", "bubble", "funnel", "waterfall", "pyramid", "solidgauge", "heatmap", "treemap", "polygon", "candlestick", "flags", "timeline", "ohlc", "organization", "sankey", "xrange", "gantt", "bullet"]
                                },
                                "backgroundColor": { "type": "string", "description": "Background color (e.g., '#ffffff')" },
                                "borderColor": { "type": "string", "description": "Border color" },
                                "borderWidth": { "type": "number", "description": "Border width in pixels" },
                                "borderRadius": { "type": "number", "description": "Border radius in pixels" },
                                "width": { "type": "number", "description": "Chart width in pixels" },
                                "height": { "type": "string", "description": "Chart height (e.g., '400px', '100%')" },
                                "marginTop": { "type": "number" },
                                "marginRight": { "type": "number" },
                                "marginBottom": { "type": "number" },
                                "marginLeft": { "type": "number" },
                                "spacingTop": { "type": "number" },
                                "spacingRight": { "type": "number" },
                                "spacingBottom": { "type": "number" },
                                "spacingLeft": { "type": "number" },
                                "plotBackgroundColor": { "type": "string" },
                                "plotBorderColor": { "type": "string" },
                                "plotBorderWidth": { "type": "number" },
                                "inverted": { "type": "boolean", "description": "Invert axes" },
                                "polar": { "type": "boolean", "description": "Polar chart" },
                                "animation": { "type": "boolean" },
                                "styledMode": { "type": "boolean" },
                                "zoomType": { "type": "string", "enum": ["X", "Y", "XY"] }
                              }
                            },
                            "title": {
                              "oneOf": [
                                { "type": "string", "description": "Title text" },
                                { "type": "object", "properties": { "text": { "type": "string" } } }
                              ]
                            },
                            "subtitle": {
                              "oneOf": [
                                { "type": "string", "description": "Subtitle text" },
                                { "type": "object", "properties": { "text": { "type": "string" } } }
                              ]
                            },
                            "xAxis": {
                              "type": "object",
                              "description": "X-axis configuration",
                              "properties": {
                                "title": { "type": "object", "properties": { "text": { "type": "string" } } },
                                "categories": { "type": "array", "items": { "type": "string" } },
                                "min": { "type": "number" },
                                "max": { "type": "number" }
                              }
                            },
                            "yAxis": {
                              "type": "object",
                              "description": "Y-axis configuration",
                              "properties": {
                                "title": { "type": "object", "properties": { "text": { "type": "string" } } },
                                "min": { "type": "number" },
                                "max": { "type": "number" }
                              }
                            },
                            "zAxis": {
                              "type": "object",
                              "description": "Z-axis configuration (for 3D charts)",
                              "properties": {
                                "title": { "type": "object", "properties": { "text": { "type": "string" } } },
                                "min": { "type": "number" },
                                "max": { "type": "number" }
                              }
                            },
                            "colorAxis": {
                              "type": "object",
                              "description": "Color axis for heatmaps",
                              "properties": {
                                "min": { "type": "number" },
                                "max": { "type": "number" },
                                "minColor": { "type": "string" },
                                "maxColor": { "type": "string" }
                              }
                            },
                            "tooltip": {
                              "type": "object",
                              "description": "Tooltip configuration",
                              "properties": {
                                "pointFormat": { "type": "string" },
                                "headerFormat": { "type": "string" },
                                "shared": { "type": "boolean" },
                                "valueSuffix": { "type": "string" },
                                "valuePrefix": { "type": "string" }
                              }
                            },
                            "legend": {
                              "type": "object",
                              "description": "Legend configuration",
                              "properties": {
                                "enabled": { "type": "boolean" },
                                "align": { "type": "string", "enum": ["LEFT", "CENTER", "RIGHT"] },
                                "verticalAlign": { "type": "string", "enum": ["TOP", "MIDDLE", "BOTTOM"] },
                                "layout": { "type": "string", "enum": ["HORIZONTAL", "VERTICAL"] }
                              }
                            },
                            "credits": {
                              "type": "object",
                              "description": "Credits configuration",
                              "properties": {
                                "enabled": { "type": "boolean" },
                                "text": { "type": "string" },
                                "href": { "type": "string" }
                              }
                            },
                            "pane": {
                              "type": "object",
                              "description": "Pane configuration (for gauges and polar charts)",
                              "properties": {
                                "startAngle": { "type": "number" },
                                "endAngle": { "type": "number" },
                                "center": { "type": "array", "items": { "type": "string" }, "description": "Center position ['50%', '50%']" },
                                "size": { "type": "string", "description": "Size (e.g., '100%')" }
                              }
                            },
                            "exporting": {
                              "type": "object",
                              "description": "Export configuration",
                              "properties": {
                                "enabled": { "type": "boolean" },
                                "filename": { "type": "string" },
                                "sourceWidth": { "type": "number" },
                                "sourceHeight": { "type": "number" },
                                "scale": { "type": "number" }
                              }
                            }
                          }
                        }
                      },
                      "required": ["config"]
                    }
                    """;
            }

            @Override
            public String execute(String arguments) {
                try {
                    ObjectNode node = (ObjectNode) JacksonUtils.readTree(arguments);
                    // Extract the config object from the arguments wrapper
                    String config = node.has("config")
                        ? node.get("config").toString()
                        : node.toString();

                    // Queue the configuration update for deferred execution
                    pendingRender = new PendingRender(currentSqlQuery, config);

                    return "Chart configuration update queued successfully";
                } catch (Exception e) {
                    return "Error updating chart configuration: " + e.getMessage();
                }
            }
        };
    }

    // ===== Event Firing =====

    /**
     * Fires a state change event to all registered listeners.
     */
    private void fireStateChangeEvent() {
        if (stateChangeListeners.isEmpty()) {
            return;
        }

        ChartState state = getState();
        if (state != null) {
            ChartStateChangeEvent event = new ChartStateChangeEvent(this, state);
            for (ChartStateChangeListener listener : stateChangeListeners) {
                try {
                    listener.onStateChange(event);
                } catch (Exception e) {
                    System.err.println("Error in state change listener: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
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

                configurationApplier.applyConfiguration(chart, configJson);

                chart.drawChart();
            });
        }, () -> {
            throw new IllegalStateException(
                    "Chart is not attached to a UI");
        });
    }

    private void applyChartConfig(Chart chart, String configJson) {
        chart.getUI().ifPresentOrElse(currentUI -> {
            currentUI.access(() -> {
                configurationApplier.applyConfiguration(chart, configJson);
            });
        }, () -> {
            throw new IllegalStateException(
                    "Chart is not attached to a UI");
        });
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
