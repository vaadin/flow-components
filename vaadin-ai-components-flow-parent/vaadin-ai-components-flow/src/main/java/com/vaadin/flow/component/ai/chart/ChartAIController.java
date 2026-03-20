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
import com.vaadin.flow.component.ai.provider.DatabaseProviderTools;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Series;
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
 * <p>
 * The chart tools are built on top of {@link ChartRegistry} and
 * {@link ChartTools}, which are reusable building blocks that can also be used
 * by other controllers (e.g., a DashboardAIController).
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

    private final Chart chart;
    private final ChartRegistry registry;
    private final DatabaseProvider databaseProvider;
    private DataConverter dataConverter;
    private final ChartConfigurationApplier configurationApplier;
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
        this.dataConverter = new DefaultDataConverter();
        this.configurationApplier = new ChartConfigurationApplier();
        this.registry = new ChartRegistry(
                id -> chart,
                () -> Set.of(DEFAULT_CHART_ID));
        // TODO: Why is this necessary in ChartAIController but not in DashboardAIController?
        this.registry.setQueryValidator(databaseProvider::executeQuery);
    }

    /**
     * Returns the chart registry used by this controller.
     *
     * @return the chart registry, never {@code null}
     */
    public ChartRegistry getRegistry() {
        return registry;
    }

    /**
     * Sets a custom data converter for transforming query results into chart
     * data.
     *
     * @param dataConverter
     *            the data converter to use, not {@code null}
     */
    public void setDataConverter(DataConverter dataConverter) {
        this.dataConverter = Objects.requireNonNull(dataConverter,
                "Data converter cannot be null");
    }

    /**
     * Returns the data converter.
     *
     * @return the data converter
     */
    public DataConverter getDataConverter() {
        return dataConverter;
    }

    /**
     * Returns the configuration applier.
     *
     * @return the configuration applier
     */
    public ChartConfigurationApplier getConfigurationApplier() {
        return configurationApplier;
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
    public List<LLMProvider.ToolSpec> getTools() {
        List<LLMProvider.ToolSpec> tools = new ArrayList<>();
        tools.addAll(DatabaseProviderTools.createAll(databaseProvider));
        tools.addAll(ChartTools.createAll(registry));
        return tools;
    }

    @Override
    public void onRequestCompleted() {
        ChartEntry entry = registry.getEntries().get(DEFAULT_CHART_ID);
        if (entry == null || !entry.hasPendingState()) {
            return;
        }
        try {
            applyPendingState(entry);
            fireStateChangeEvent();
        } catch (Exception e) {
            LOGGER.error("Error rendering chart", e);
        } finally {
            entry.clearPendingState();
        }
    }

    /**
     * Gets the current chart state for persistence.
     *
     * @return the current state, or {@code null} if no chart has been created
     */
    public ChartState getState() {
        ChartEntry entry = registry.getEntries().get(DEFAULT_CHART_ID);
        List<String> queries = entry != null ? entry.getQueries()
                : List.of();
        if (queries.isEmpty()) {
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
        return new ChartState(queries, configJson);
    }

    /**
     * Restores a previously saved chart state.
     *
     * @param state
     *            the state to restore
     */
    public void restoreState(ChartState state) {
        ChartEntry entry = registry.getEntry(DEFAULT_CHART_ID);
        entry.setQueries(state.queries());
        if (!state.queries().isEmpty() && state.configuration() != null) {
            try {
                renderChart(state.queries(), state.configuration());
            } catch (Exception e) {
                LOGGER.error("Failed to restore chart", e);
            }
        }
    }

    /**
     * State record for persistence.
     */
    public record ChartState(List<String> queries,
            String configuration) implements java.io.Serializable {
    }

    // ===== Rendering Methods =====

    private void applyPendingState(ChartEntry entry) {
        String configJson = entry.getPendingConfigurationJson();
        List<String> pendingQueries = entry.getPendingQueries();

        // Determine effective queries
        entry.applyPendingQueries();
        List<String> effectiveQueries = entry.getQueries();

        if (!effectiveQueries.isEmpty()) {
            // We have queries — render with data
            String effectiveConfig = configJson != null ? configJson
                    : ChartSerialization.toJSON(chart.getConfiguration());
            renderChart(effectiveQueries, effectiveConfig);
        } else if (configJson != null) {
            // Config-only update, no data
            applyChartConfig(configJson);
        }
    }

    private void renderChart(List<String> queries, String configJson) {
        chart.getUI().ifPresentOrElse(currentUI -> {
            currentUI.access(() -> {
                Configuration config = chart.getConfiguration();
                List<Series> allSeries = new ArrayList<>();
                for (String query : queries) {
                    var results = databaseProvider.executeQuery(query);
                    allSeries.addAll(dataConverter.convertToSeries(results));
                }
                config.setSeries(allSeries.toArray(new Series[0]));
                configurationApplier.applyConfiguration(chart, configJson);
                chart.drawChart();
            });
        }, () -> {
            throw new IllegalStateException(
                    "Chart is not attached to a UI");
        });
    }

    private void applyChartConfig(String configJson) {
        chart.getUI().ifPresentOrElse(currentUI -> {
            currentUI.access(() -> {
                configurationApplier.applyConfiguration(chart, configJson);
            });
        }, () -> {
            throw new IllegalStateException(
                    "Chart is not attached to a UI");
        });
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
}
