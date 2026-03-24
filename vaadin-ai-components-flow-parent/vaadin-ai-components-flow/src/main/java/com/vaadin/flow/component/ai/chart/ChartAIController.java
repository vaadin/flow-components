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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.orchestrator.AIController;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.DatabaseProviderAITools;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;

/**
 * AI controller for creating interactive chart visualizations from database
 * data.
 * <p>
 * This controller enables AI-powered chart generation by providing tools that
 * allow the LLM to query database schemas and create/update chart
 * visualizations based on natural language requests.
 * </p>
 * <p>
 * Chart state ({@link ChartEntry}) is stored directly on the {@link Chart}
 * instance via {@link ChartEntry#getOrCreate(Chart)}, so there is no separate
 * registry to maintain.
 * </p>
 * <p>
 * State changes requested by the LLM through the update tools are deferred and
 * applied in {@link #onRequestCompleted()}, avoiding partial state and multiple
 * redraws during a multi-tool LLM turn.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class ChartAIController implements AIController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ChartAIController.class);

    private static final String DEFAULT_CHART_ID = "chart";

    private static final String SYSTEM_PROMPT = """
            You have chart visualization tools. Follow this workflow:

            1. ALWAYS call get_chart_state() FIRST before making changes
            2. Call get_database_schema() to understand available data
            3. Call update_chart_data_source() and update_chart_configuration() \
            as needed — they can be called independently

            Data and configuration are separate concerns:
            - update_chart_data_source() populates chart series from SQL queries
            - update_chart_configuration() controls visual appearance (type, styling, axes, etc.)
            - NEVER include 'series' in configuration — data comes only from queries
            - When changing chart type, ensure the query column aliases match the new type
            """;

    private final Chart chart;
    private final DatabaseProvider databaseProvider;
    private final ChartRenderer chartRenderer;
    private final List<ChartStateChangeListener> stateChangeListeners = new ArrayList<>();

    /**
     * Creates a new AI chart controller.
     *
     * @param chart
     *            the chart component to update, not {@code null}
     * @param databaseProvider
     *            the database provider for schema and query execution, not
     *            {@code null}
     */
    public ChartAIController(Chart chart, DatabaseProvider databaseProvider) {
        this.chart = Objects.requireNonNull(chart, "Chart cannot be null");
        this.databaseProvider = Objects.requireNonNull(databaseProvider,
                "Database provider cannot be null");
        this.chartRenderer = new ChartRenderer(databaseProvider);
    }

    /**
     * Sets a custom data converter for transforming query results into chart
     * data.
     *
     * @param dataConverter
     *            the data converter to use, not {@code null}
     */
    public void setDataConverter(DataConverter dataConverter) {
        chartRenderer.setDataConverter(dataConverter);
    }

    /**
     * Returns the data converter.
     *
     * @return the data converter
     */
    public DataConverter getDataConverter() {
        return chartRenderer.getDataConverter();
    }

    /**
     * Returns the configuration applier.
     *
     * @return the configuration applier
     */
    public ChartConfigurationApplier getConfigurationApplier() {
        return chartRenderer.getConfigurationApplier();
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
        return SYSTEM_PROMPT;
    }

    @Override
    public List<LLMProvider.ToolSpec> getTools() {
        List<LLMProvider.ToolSpec> tools = new ArrayList<>();
        tools.addAll(DatabaseProviderAITools.createAll(databaseProvider));
        tools.addAll(ChartAITools.createAll(new ChartAITools.Callbacks() {
            @Override
            public String getState(String chartId) {
                return ChartEntry.getStateAsJson(chart, chartId);
            }

            @Override
            public void updateConfiguration(String chartId, String configJson) {
                ChartEntry.getOrCreate(chart, chartId)
                        .setPendingConfigurationJson(configJson);
            }

            @Override
            public void updateData(String chartId, List<String> queries) {
                for (String q : queries) {
                    databaseProvider.executeQuery(q);
                }
                ChartEntry entry = ChartEntry.getOrCreate(chart, chartId);
                entry.setQueries(queries);
                entry.setPendingDataUpdate(true);
            }

            @Override
            public Set<String> getChartIds() {
                return Set.of(DEFAULT_CHART_ID);
            }
        }));
        return tools;
    }

    @Override
    public void onRequestCompleted() {
        try {
            chartRenderer.applyPendingState(chart);
            fireStateChangeEvent();
        } catch (Exception e) {
            LOGGER.error("Error rendering chart", e);
        }
    }

    /**
     * Gets the current chart state for persistence.
     *
     * @return the current state, or {@code null} if no chart has been created
     */
    public ChartEntry.ChartState getState() {
        return ChartEntry.getState(chart);
    }

    /**
     * Restores a previously saved chart state.
     *
     * @param state
     *            the state to restore
     */
    public void restoreState(ChartEntry.ChartState state) {
        ChartEntry entry = ChartEntry.getOrCreate(chart, DEFAULT_CHART_ID);
        entry.setQueries(state.queries());
        if (!state.queries().isEmpty() && state.configuration() != null) {
            try {
                chartRenderer.renderChart(chart, state.queries(),
                        state.configuration());
            } catch (Exception e) {
                LOGGER.error("Failed to restore chart", e);
            }
        }
    }

    // ===== Event Firing =====

    private void fireStateChangeEvent() {
        if (stateChangeListeners.isEmpty()) {
            return;
        }
        ChartEntry.ChartState state = getState();
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
}
