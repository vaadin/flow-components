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
package com.vaadin.flow.component.ai.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.orchestrator.AIController;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ObjectNode;

/**
 * AI controller for creating interactive chart visualizations from database
 * data.
 * <p>
 * This controller enables AI-powered chart generation by providing tools that
 * allow the LLM to query database schemas and create/update chart
 * visualizations based on natural language requests.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class ChartAIController implements AIController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ChartAIController.class);

    private final Chart chart;
    private final DatabaseProvider databaseProvider;
    private DataConverter chartDataConverter;
    private final ChartConfigurationApplier configurationApplier;

    private String currentSqlQuery;
    private final List<ChartStateChangeListener> stateChangeListeners = new ArrayList<>();
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
    public ChartAIController(Chart chart, DatabaseProvider databaseProvider) {
        this.chart = Objects.requireNonNull(chart, "Chart cannot be null");
        this.databaseProvider = Objects.requireNonNull(databaseProvider,
                "Database provider cannot be null");
        this.chartDataConverter = new DefaultDataConverter();
        this.configurationApplier = new ChartConfigurationApplier();
    }

    /**
     * Sets a custom data converter for transforming query results into chart
     * data.
     *
     * @param dataConverter
     *            the data converter to use, not {@code null}
     */
    public void setDataConverter(DataConverter dataConverter) {
        this.chartDataConverter = Objects.requireNonNull(dataConverter,
                "Data converter cannot be null");
    }

    /**
     * Adds a listener for chart state changes.
     *
     * @param listener
     *            the listener to add, not {@code null}
     */
    public void addStateChangeListener(ChartStateChangeListener listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        stateChangeListeners.add(listener);
    }

    /**
     * Removes a previously added state change listener.
     *
     * @param listener
     *            the listener to remove
     * @return true if the listener was found and removed
     */
    public boolean removeStateChangeListener(
            ChartStateChangeListener listener) {
        return stateChangeListeners.remove(listener);
    }

    /**
     * Returns the recommended system prompt for chart visualization
     * capabilities.
     *
     * @return the system prompt text
     */
    public static String getSystemPrompt() {
        return """
                You have access to chart visualization capabilities:

                TOOLS:
                1. getSchema() - Retrieves database schema (tables, columns, types)
                2. getCurrentState() - Returns current chart state (query and configuration)
                3. updateData(query) - Updates chart data with SQL SELECT query
                4. updateConfig(config) - Updates chart configuration (type, title, tooltip, etc.)

                WORKFLOW:
                1. ALWAYS call getCurrentState() FIRST before making any changes
                2. Use getSchema() if you need to understand available data
                3. Use updateData() to change data source
                4. Use updateConfig() to change chart appearance

                DATA REQUIREMENTS BY CHART TYPE:
                - Basic charts (line, bar, column, pie): SELECT category, value (2 columns)
                - Scatter: SELECT x, y (2 numeric columns)
                - Bubble: SELECT x, y, size (3 numeric columns)
                - Bullet: SELECT category, value, target (3 columns, name third 'target')
                - Range (arearange, columnrange): SELECT x, low, high (3 columns)
                - BoxPlot: SELECT low, q1, median, q3, high (5 columns)
                - OHLC/Candlestick: SELECT date, open, high, low, close (5 columns)
                - Sankey: SELECT from, to, weight (3 columns)
                - Xrange/Gantt: SELECT start, end, y (3 columns)

                IMPORTANT:
                - ALWAYS check getCurrentState() before making any modifications
                - NEVER include 'series' data in updateConfig() - data comes ONLY from updateData()
                - When changing chart types, ensure the data query matches the new type's requirements
                """;
    }

    @Override
    public List<LLMProvider.ToolDefinition> getTools() {
        return List.of(createGetSchemaTool(), createGetCurrentStateTool(),
                createUpdateDataTool(), createUpdateConfigTool());
    }

    @Override
    public void onRequestCompleted() {
        if (pendingRender != null) {
            try {
                if (pendingRender.sqlQuery != null) {
                    renderChart(pendingRender.sqlQuery,
                            pendingRender.configJson);
                } else {
                    applyChartConfig(chart, pendingRender.configJson);
                }
                fireStateChangeEvent();
            } catch (Exception e) {
                LOGGER.error("Error rendering chart", e);
            } finally {
                pendingRender = null;
            }
        }
    }

    /**
     * Gets the current chart state for persistence.
     *
     * @return the current state, or {@code null} if no chart has been created
     */
    public ChartState getState() {
        if (currentSqlQuery == null) {
            return null;
        }
        String configJson = ChartSerialization
                .toJSON(chart.getConfiguration());
        try {
            ObjectNode configNode = (ObjectNode) JacksonUtils
                    .readTree(configJson);
            configNode.remove("series");
            configJson = configNode.toString();
        } catch (Exception e) {
            LOGGER.warn("Failed to remove series from config", e);
        }
        return new ChartState(currentSqlQuery, configJson);
    }

    /**
     * Restores a previously saved chart state.
     *
     * @param state
     *            the state to restore
     */
    public void restoreState(ChartState state) {
        this.currentSqlQuery = state.sqlQuery;
        if (currentSqlQuery != null && state.configuration != null) {
            try {
                renderChart(currentSqlQuery, state.configuration);
            } catch (Exception e) {
                LOGGER.error("Failed to restore chart", e);
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

    private LLMProvider.ToolDefinition createGetSchemaTool() {
        return new LLMProvider.ToolDefinition() {
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

    private LLMProvider.ToolDefinition createGetCurrentStateTool() {
        return new LLMProvider.ToolDefinition() {
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
                return "{\"query\":\""
                        + state.sqlQuery().replace("\"", "\\\"")
                        + "\",\"configuration\":" + state.configuration()
                        + "}";
            }
        };
    }

    private LLMProvider.ToolDefinition createUpdateDataTool() {
        return new LLMProvider.ToolDefinition() {
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
                    ObjectNode node = (ObjectNode) JacksonUtils
                            .readTree(arguments);
                    String query = node.get("query").asString();
                    databaseProvider.executeQuery(query);
                    currentSqlQuery = query;
                    String config = ChartSerialization
                            .toJSON(chart.getConfiguration());
                    pendingRender = new PendingRender(query, config);
                    return "Chart data update queued successfully";
                } catch (Exception e) {
                    return "Error updating chart data: " + e.getMessage();
                }
            }
        };
    }

    private LLMProvider.ToolDefinition createUpdateConfigTool() {
        return new LLMProvider.ToolDefinition() {
            @Override
            public String getName() {
                return "updateConfig";
            }

            @Override
            public String getDescription() {
                return "Updates the chart configuration (type, title, tooltip, etc.). "
                        + "CRITICAL: ALWAYS specify chart type in config.chart.type. "
                        + "Do NOT include 'series' - data is managed via updateData. "
                        + "Parameters: config (object) - Chart configuration object.";
            }

            @Override
            public String getParametersSchema() {
                return """
                        {
                          "type": "object",
                          "properties": {
                            "config": {
                              "type": "object",
                              "description": "Chart configuration object"
                            }
                          },
                          "required": ["config"]
                        }
                        """;
            }

            @Override
            public String execute(String arguments) {
                try {
                    ObjectNode node = (ObjectNode) JacksonUtils
                            .readTree(arguments);
                    String config;
                    if (node.has("config")
                            && node.get("config").isObject()) {
                        config = node.get("config").toString();
                    } else if (node.has("config")
                            && node.get("config").isString()) {
                        // LLM passed config as a JSON string; parse
                        // it to verify it's valid JSON
                        config = node.get("config").asString();
                    } else {
                        config = node.toString();
                    }
                    pendingRender = new PendingRender(currentSqlQuery,
                            config);
                    return "Chart configuration update queued successfully";
                } catch (Exception e) {
                    return "Error updating chart configuration: "
                            + e.getMessage();
                }
            }
        };
    }

    // ===== Event Firing =====

    private void fireStateChangeEvent() {
        if (stateChangeListeners.isEmpty()) {
            return;
        }
        ChartState state = getState();
        if (state != null) {
            ChartStateChangeEvent event = new ChartStateChangeEvent(this,
                    state);
            for (ChartStateChangeListener listener : stateChangeListeners) {
                try {
                    listener.onStateChange(event);
                } catch (Exception e) {
                    LOGGER.error("Error in state change listener", e);
                }
            }
        }
    }

    // ===== Rendering Methods =====

    private void renderChart(String sqlQuery, String configJson)
            throws Exception {
        List<java.util.Map<String, Object>> results = databaseProvider
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
}
