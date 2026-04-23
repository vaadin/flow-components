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
package com.vaadin.flow.component.ai.grid;

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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.shared.Registration;

import tools.jackson.databind.JsonNode;

/**
 * AI controller for populating a {@link Grid Grid&lt;AIDataRow&gt;} with
 * database data via LLM tool calls. Attach it to an {@link AIOrchestrator} via
 * {@link AIOrchestrator.Builder#withController(AIController)} to expose its
 * tools to the LLM. Workflow instructions are delivered through the description
 * of the {@code get_grid_instructions} tool, which the LLM reads as part of the
 * tool manifest.
 *
 * <pre>
 * var grid = new Grid&lt;AIDataRow&gt;();
 * var controller = new GridAIController(grid, databaseProvider);
 * AIOrchestrator orchestrator = AIOrchestrator
 *         .builder(llmProvider, systemPrompt).withController(controller)
 *         .withMessageList(messageList).build();
 * </pre>
 * <p>
 * The grid uses {@link AIDataRow} as its item type. Row instances are created
 * internally when query results are rendered and are not intended to be
 * constructed or inspected by application code.
 * </p>
 * <p>
 * The grid automatically creates columns from query results with:
 * </p>
 * <ul>
 * <li>Lazy loading with SQL {@code LIMIT}/{@code OFFSET}</li>
 * <li>Built-in renderers for dates and numbers</li>
 * <li>Right-alignment for numeric columns</li>
 * <li>Resizable, sortable columns with auto-width</li>
 * <li>Column grouping from dot-separated aliases</li>
 * </ul>
 * <p>
 * State changes requested by the LLM are deferred and applied in
 * {@link #onRequestCompleted()}, avoiding partial state and multiple redraws
 * during a multi-tool LLM turn. The grid state is stored directly on the
 * {@link Grid} component, so it survives serialization.
 * </p>
 * <p>
 * <b>Serialization:</b> This controller is not serialized with the
 * orchestrator. After deserialization, create a new controller and restore
 * transient dependencies via {@link AIOrchestrator#reconnect(LLMProvider)
 * reconnect(provider)} {@code .withController(controller).apply()}. The grid
 * data can be captured via {@link #getState()} and re-applied via
 * {@link #restoreState(GridState)}:
 * </p>
 *
 * <pre>
 * var controller = new GridAIController(grid, databaseProvider);
 * orchestrator.reconnect(llmProvider).withController(controller).apply();
 * if (savedState != null) {
 *     controller.restoreState(savedState);
 * }
 * </pre>
 * <p>
 * Register a listener via {@link #addStateChangeListener(SerializableConsumer)}
 * to be notified when the grid state changes, for example to persist
 * {@link #getState()} after each successful AI request.
 * </p>
 *
 * @author Vaadin Ltd
 * @see AIDataRow
 * @see GridAITools
 * @see GridRenderer
 * @see GridState
 * @see DatabaseProviderAITools
 */
public class GridAIController implements AIController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GridAIController.class);

    private static final String GRID_ID = "grid";

    private static final String INSTRUCTIONS_TOOL_NAME = "get_grid_instructions";

    private static final String INSTRUCTIONS_TEXT = """
            Grid data display workflow. Follow this for every grid request:

            TOOLS:
            1. get_database_schema() - Retrieves database schema (tables, columns, types)
            2. get_grid_state() - Returns current grid state (query)
            3. update_grid_data(query) - Updates grid data with SQL SELECT query

            WORKFLOW:
            Complete the user's request in a SINGLE response by calling all needed tools.
            1. Call get_grid_state() to see what's already configured
            2. Call get_database_schema() to learn the exact table and column names
            3. Call update_grid_data() with a SQL SELECT query using only columns from the schema

            IMPORTANT:
            - Call get_grid_state() and update_grid_data() in the SAME response
            - Do NOT stop after get_grid_state()
            """;

    private final Grid<AIDataRow> grid;
    private final DatabaseProvider databaseProvider;
    private final List<SerializableConsumer<GridState>> stateChangeListeners = new ArrayList<>();

    /**
     * Creates a new grid AI controller.
     *
     * @param grid
     *            the grid to populate, not {@code null}
     * @param databaseProvider
     *            the database provider for schema and query execution, not
     *            {@code null}
     */
    public GridAIController(Grid<AIDataRow> grid,
            DatabaseProvider databaseProvider) {
        this.grid = Objects.requireNonNull(grid, "Grid must not be null");
        this.databaseProvider = Objects.requireNonNull(databaseProvider,
                "DatabaseProvider must not be null");
    }

    @Override
    public List<LLMProvider.ToolSpec> getTools() {
        var tools = new ArrayList<LLMProvider.ToolSpec>();
        tools.add(createInstructionsTool());
        tools.addAll(DatabaseProviderAITools.createAll(databaseProvider));
        tools.addAll(GridAITools.createAll(new GridAITools.Callbacks() {
            @Override
            public String getState(String gridId) {
                return GridEntry.getStateAsJson(grid, gridId);
            }

            @Override
            public void updateData(String gridId, String query) {
                // Validate eagerly so invalid SQL propagates back
                // to the LLM as a tool error it can fix.
                databaseProvider.executeQuery(
                        "SELECT * FROM (" + query + ") AS _v LIMIT 1");
                GridEntry.getOrCreate(grid, gridId).setPendingQuery(query);
            }

            @Override
            public Set<String> getGridIds() {
                return Set.of(GRID_ID);
            }
        }));
        return tools;
    }

    private LLMProvider.ToolSpec createInstructionsTool() {
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return INSTRUCTIONS_TOOL_NAME;
            }

            @Override
            public String getDescription() {
                return """
                        Read this before using any grid or database tool.
                        Calling this tool returns these same instructions —
                        normally unnecessary since you are already reading them here.

                        """
                        + INSTRUCTIONS_TEXT;
            }

            @Override
            public String getParametersSchema() {
                return null;
            }

            @Override
            public String execute(JsonNode arguments) {
                return INSTRUCTIONS_TEXT;
            }
        };
    }

    @Override
    public void onRequestCompleted() {
        var entry = GridEntry.get(grid);
        if (entry == null || !entry.hasPendingState()) {
            LOGGER.debug("onRequestCompleted: no pending query");
            return;
        }
        var query = entry.getPendingQuery();
        LOGGER.info("onRequestCompleted: applying query: {}", query);
        // Render synchronously so exceptions propagate to the orchestrator,
        // which runs this on the UI thread under session lock. Attachment
        // is not required: grid state (columns, data provider) is
        // server-side and syncs to the client on attach.
        render(entry, query, true);
        LOGGER.info("Grid updated successfully");
    }

    /**
     * Returns the current grid state for persistence, or {@code null} if no
     * data has been loaded yet.
     *
     * @return the current state, or {@code null}
     */
    public GridState getState() {
        var entry = GridEntry.get(grid);
        if (entry == null || entry.getCurrentQuery() == null) {
            return null;
        }
        return new GridState(entry.getCurrentQuery());
    }

    /**
     * Restores a previously saved grid state. Re-executes the stored query and
     * renders the grid.
     * <p>
     * Does not fire state change listeners.
     * </p>
     *
     * @param state
     *            the state to restore, not {@code null}
     */
    public void restoreState(GridState state) {
        Objects.requireNonNull(state, "State must not be null");
        if (state.query() == null) {
            return;
        }
        var entry = GridEntry.getOrCreate(grid, GRID_ID);
        try {
            render(entry, state.query(), false);
        } catch (Exception e) {
            LOGGER.error("Error updating grid during state restore", e);
        }
    }

    private void render(GridEntry entry, String query, boolean fireListeners) {
        try {
            GridRenderer.renderGrid(grid, databaseProvider, query);
            entry.setCurrentQuery(query);
            if (fireListeners) {
                fireStateChangeListeners();
            }
        } finally {
            entry.clearPendingState();
        }
    }

    /**
     * Adds a listener that is notified when the grid state changes after an AI
     * request completes successfully. This is typically used to persist the
     * grid state — for example by calling {@link #getState()} and saving the
     * result so that it can be reapplied with {@link #restoreState(GridState)}
     * after deserialization.
     * <p>
     * The listener is not fired by {@link #restoreState(GridState)}.
     * </p>
     *
     * @param listener
     *            the listener, not {@code null}
     * @return a registration for removing the listener
     */
    public Registration addStateChangeListener(
            SerializableConsumer<GridState> listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        stateChangeListeners.add(listener);
        return () -> stateChangeListeners.remove(listener);
    }

    private void fireStateChangeListeners() {
        if (stateChangeListeners.isEmpty()) {
            return;
        }
        var state = getState();
        if (state != null) {
            stateChangeListeners.forEach(listener -> {
                try {
                    listener.accept(state);
                } catch (Exception e) {
                    LOGGER.error("State change listener failed", e);
                }
            });
        }
    }
}
