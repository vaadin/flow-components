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
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.DatabaseProviderAITools;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.shared.Registration;

/**
 * AI controller for creating interactive chart visualizations from database
 * data.
 * <p>
 * This controller provides tools that allow the LLM to query database schemas
 * and create or update chart visualizations based on natural language requests.
 * Attach it to an {@link AIOrchestrator} via
 * {@link AIOrchestrator.Builder#withController(AIController)} to expose its
 * tools to the LLM. The controller overrides {@link #getSystemPrompt()}, so the
 * chart-specific tool-calling workflow instructions are automatically appended
 * to the orchestrator's system prompt on every request.
 * </p>
 *
 * <pre>
 * var controller = new ChartAIController(chart, databaseProvider);
 * AIOrchestrator orchestrator = AIOrchestrator.builder(llmProvider, null)
 *         .withController(controller).withMessageList(messageList).build();
 * </pre>
 * <p>
 * State changes requested by the LLM are deferred and applied in
 * {@link #onRequestCompleted()}, avoiding partial state and multiple redraws
 * during a multi-tool LLM turn. The chart state is stored directly on the
 * {@link Chart} component, so it survives serialization.
 * </p>
 * <p>
 * Data conversion from SQL query results to chart series is handled by a
 * {@link DataConverter}. A default implementation is used unless overridden via
 * {@link #setDataConverter(DataConverter)}.
 * </p>
 * <p>
 * <b>Serialization:</b> This controller is not serialized with the
 * orchestrator. After deserialization, create a new controller and restore
 * transient dependencies via {@link AIOrchestrator#reconnect(LLMProvider)
 * reconnect(provider)} {@code .withController(controller).apply()}. The chart
 * data can be captured via {@link #getState()} and re-applied via
 * {@link #restoreState(ChartState)}:
 * </p>
 *
 * <pre>
 * var controller = new ChartAIController(chart, databaseProvider);
 * orchestrator.reconnect(llmProvider).withController(controller).apply();
 * if (savedState != null) {
 *     controller.restoreState(savedState);
 * }
 * </pre>
 * <p>
 * Register a listener via {@link #addStateChangeListener(SerializableConsumer)}
 * to be notified when the chart state changes, for example to persist
 * {@link #getState()} after each successful AI request.
 * </p>
 *
 * @author Vaadin Ltd
 * @see ChartAITools
 * @see ChartRenderer
 * @see ChartState
 * @see DataConverter
 * @see DatabaseProviderAITools
 */
public class ChartAIController implements AIController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ChartAIController.class);

    private static final String CHART_ID = "chart";

    private static final String SYSTEM_PROMPT = """
            You have chart visualization tools. Follow this workflow when working with Charts:

            1. ALWAYS call get_chart_state() FIRST before making changes
            2. Call get_database_schema() to understand available data
            3. Call update_chart_data_source() and update_chart_configuration() \
            as needed — they can be called independently

            Data and configuration are separate concerns:
            - update_chart_data_source() populates chart series from SQL queries
            - update_chart_configuration() controls visual appearance (type, styling, axes, etc.)
            - NEVER include series data in configuration — data comes only from queries
            - When changing chart type, ensure the query column aliases match the new type

            Per-series styling and mixed chart types:
            - Use the series array in update_chart_configuration() to override chart type or \
            styling for specific series, matched by name
            - Call get_plot_options_schema(chartType) to discover available properties
            - Example: {"series": [{"name": "South", "type": "column", "yAxis": 1}]}
            """;

    private final Chart chart;
    private final DatabaseProvider databaseProvider;
    private final List<SerializableConsumer<ChartState>> stateChangeListeners = new ArrayList<>();
    private DataConverter dataConverter;

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
    }

    /**
     * Sets a custom data converter for transforming query results into chart
     * series data. Replaces the default converter used to produce series points
     * from the rows returned by the configured SQL queries.
     *
     * @param dataConverter
     *            the data converter to use, not {@code null}
     */
    public void setDataConverter(DataConverter dataConverter) {
        this.dataConverter = Objects.requireNonNull(dataConverter,
                "Data converter cannot be null");
    }

    /**
     * Returns the chart-specific tool-calling workflow instructions that the
     * LLM needs to use this controller's tools effectively. Contributed to the
     * orchestrator automatically via {@link AIController}; applications do not
     * need to pass it to the orchestrator builder explicitly.
     *
     * @return the system prompt text, never {@code null}
     */
    @Override
    public String getSystemPrompt() {
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
                // Parse eagerly to validate. If the JSON contains
                // invalid values, the exception propagates back to the
                // LLM as an error so it can fix the configuration.
                ChartConfigurationParser.parse(configJson);
                ChartEntry.getOrCreate(chart, chartId)
                        .setPendingConfigurationJson(configJson);
            }

            @Override
            public void updateData(String chartId, List<String> queries) {
                // Execute queries eagerly to validate them. If a query
                // is invalid, the exception propagates back to the LLM
                // as an error so it can fix the query. Results are
                // discarded here; they will be re-executed at render
                // time in ChartRenderer.
                for (String q : queries) {
                    databaseProvider.executeQuery(q);
                }
                ChartEntry entry = ChartEntry.getOrCreate(chart, chartId);
                entry.setQueries(queries);
                entry.setPendingDataUpdate(true);
            }

            @Override
            public Set<String> getChartIds() {
                return Set.of(CHART_ID);
            }
        }));
        return tools;
    }

    /**
     * Returns the current chart state, including the SQL queries and
     * configuration. Returns {@code null} if the chart has no data queries.
     *
     * @return the current state, or {@code null}
     */
    public ChartState getState() {
        ChartEntry entry = ChartEntry.get(chart);
        if (entry == null || entry.getQueries().isEmpty()) {
            return null;
        }
        return new ChartState(entry.getQueries(),
                copyConfiguration(chart.getConfiguration()));
    }

    /**
     * Restores a previously saved chart state. Applies the configuration, sets
     * the queries, and re-renders the chart.
     * <p>
     * Does not fire state change listeners.
     * </p>
     *
     * @param state
     *            the state to restore, not {@code null}
     */
    public void restoreState(ChartState state) {
        Objects.requireNonNull(state, "State cannot be null");
        chart.setConfiguration(copyConfiguration(state.configuration()));
        ChartEntry entry = ChartEntry.getOrCreate(chart, CHART_ID);
        entry.setQueries(state.queries());
        deferRender(entry, state.queries(), null, false);
    }

    /**
     * Adds a listener that is notified when the chart state changes after an AI
     * request completes successfully. This is typically used to persist the
     * chart state — for example by calling {@link #getState()} and saving the
     * result so that it can be reapplied with {@link #restoreState(ChartState)}
     * after deserialization.
     * <p>
     * The listener is not fired by {@link #restoreState(ChartState)}.
     * </p>
     *
     * @param listener
     *            the listener, not {@code null}
     * @return a registration for removing the listener
     */
    public Registration addStateChangeListener(
            SerializableConsumer<ChartState> listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        stateChangeListeners.add(listener);
        return () -> stateChangeListeners.remove(listener);
    }

    @Override
    public void onRequestCompleted() {
        ChartEntry entry = ChartEntry.get(chart);
        if (entry == null || !entry.hasPendingState()) {
            return;
        }

        List<String> queries = entry.getQueries();
        if (queries.isEmpty()) {
            // Config-only: no queries to render yet. Clear only the
            // data flag but keep pendingConfigurationJson so it's used
            // when data arrives in a later request.
            entry.setPendingDataUpdate(false);
            return;
        }

        String configJson = entry.getPendingConfigurationJson();
        deferRender(entry, queries, configJson, true);
    }

    private void deferRender(ChartEntry entry, List<String> queries,
            String configJson, boolean fireListeners) {
        chart.getElement().getNode().runWhenAttached(ui -> ui.access(() -> {
            try {
                ChartRenderer.renderChart(chart, databaseProvider,
                        dataConverter, queries, configJson);
                if (fireListeners) {
                    fireStateChangeListeners();
                }
            } catch (Exception e) {
                LOGGER.error("Rendering failed", e);
            } finally {
                entry.clearPendingState();
            }
        }));
    }

    private void fireStateChangeListeners() {
        if (stateChangeListeners.isEmpty()) {
            return;
        }
        ChartState state = getState();
        if (state != null) {
            for (var listener : List.copyOf(stateChangeListeners)) {
                try {
                    listener.accept(state);
                } catch (Exception e) {
                    LOGGER.error("State change listener failed", e);
                }
            }
        }
    }

    private static Configuration copyConfiguration(
            Configuration configuration) {
        String json = ChartSerialization.toJSON(configuration);
        return ChartConfigurationParser.parse(json);
    }

}
