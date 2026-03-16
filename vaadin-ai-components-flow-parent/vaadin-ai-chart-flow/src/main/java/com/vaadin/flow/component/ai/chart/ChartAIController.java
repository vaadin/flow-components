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

    private final ChartTools chartTools;
    private final DatabaseProvider databaseProvider;
    private final List<ChartStateChangeListener> stateChangeListeners = new ArrayList<>();

    /**
     * Creates a new AI chart controller.
     *
     * @param chart
     *            the chart component to update
     * @param databaseProvider
     *            the database provider for schema and query execution
     */
    public ChartAIController(Chart chart, DatabaseProvider databaseProvider) {
        this.databaseProvider = Objects.requireNonNull(databaseProvider,
                "Database provider cannot be null");
        this.chartTools = new ChartTools(chart, databaseProvider);
    }

    /**
     * Returns the chart tools instance used by this controller.
     *
     * @return the chart tools
     */
    public ChartTools getChartTools() {
        return chartTools;
    }

    /**
     * Sets a custom data converter for transforming query results into chart
     * data.
     *
     * @param dataConverter
     *            the data converter to use, not {@code null}
     */
    public void setDataConverter(DataConverter dataConverter) {
        chartTools.setDataConverter(dataConverter);
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
        return ChartTools.getSystemPrompt();
    }

    @Override
    public List<LLMProvider.ToolDefinition> getTools() {
        List<LLMProvider.ToolDefinition> tools = new ArrayList<>();
        tools.add(databaseProvider.getSchemaTool());
        tools.addAll(chartTools.getTools());
        return tools;
    }

    @Override
    public void onRequestCompleted() {
        if (chartTools.getPendingDataQuery() == null
                && chartTools.getPendingConfigJson() == null) {
            return;
        }
        try {
            String sqlQuery = chartTools.getPendingDataQuery() != null
                    ? chartTools.getPendingDataQuery()
                    : chartTools.getCurrentSqlQuery();
            if (sqlQuery != null) {
                String configJson = chartTools.getPendingConfigJson() != null
                        ? chartTools.getPendingConfigJson()
                        : ChartSerialization.toJSON(
                                chartTools.getChart().getConfiguration());
                renderChart(sqlQuery, configJson);
            } else if (chartTools.getPendingConfigJson() != null) {
                applyChartConfig(chartTools.getChart(),
                        chartTools.getPendingConfigJson());
            }
            fireStateChangeEvent();
        } catch (Exception e) {
            LOGGER.error("Error rendering chart", e);
        } finally {
            chartTools.clearPending();
        }
    }

    /**
     * Gets the current chart state for persistence.
     *
     * @return the current state, or {@code null} if no chart has been created
     */
    public ChartState getState() {
        if (chartTools.getCurrentSqlQuery() == null) {
            return null;
        }
        String configJson = ChartSerialization
                .toJSON(chartTools.getChart().getConfiguration());
        try {
            ObjectNode configNode = (ObjectNode) JacksonUtils
                    .readTree(configJson);
            configNode.remove("series");
            configJson = configNode.toString();
        } catch (Exception e) {
            LOGGER.warn("Failed to remove series from config", e);
        }
        return new ChartState(chartTools.getCurrentSqlQuery(), configJson);
    }

    /**
     * Restores a previously saved chart state.
     *
     * @param state
     *            the state to restore
     */
    public void restoreState(ChartState state) {
        chartTools.setCurrentSqlQuery(state.sqlQuery);
        if (chartTools.getCurrentSqlQuery() != null
                && state.configuration != null) {
            try {
                renderChart(chartTools.getCurrentSqlQuery(),
                        state.configuration);
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
        DataSeries series = chartTools.getDataConverter()
                .convertToDataSeries(results);
        Chart chart = chartTools.getChart();

        chart.getUI().ifPresentOrElse(currentUI -> {
            currentUI.access(() -> {
                Configuration config = chart.getConfiguration();
                config.setSeries(series);
                chartTools.getConfigurationApplier()
                        .applyConfiguration(chart, configJson);
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
                chartTools.getConfigurationApplier()
                        .applyConfiguration(chart, configJson);
            });
        }, () -> {
            throw new IllegalStateException(
                    "Chart is not attached to a UI");
        });
    }
}
