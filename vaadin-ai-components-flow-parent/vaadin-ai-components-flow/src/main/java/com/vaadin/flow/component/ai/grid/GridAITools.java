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

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.provider.LLMProvider;

import tools.jackson.databind.JsonNode;

/**
 * Factory for creating reusable grid {@link LLMProvider.ToolSpec} instances.
 * <p>
 * The tools use a {@code gridId} parameter to identify which grid to operate
 * on, allowing a single set of tools to manage multiple grids (e.g., in a
 * dashboard). Callers provide a {@link Callbacks} implementation for state
 * retrieval and mutation, keeping this class decoupled from {@code Grid}.
 * </p>
 *
 * @author Vaadin Ltd
 */
public final class GridAITools {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GridAITools.class);

    private GridAITools() {
    }

    /**
     * Signals a validation failure whose message is safe to pass back to the
     * LLM. Unexpected runtime exceptions, by contrast, may carry internal
     * detail (SQL fragments, schema names, file paths) and must be replaced
     * with a generic message before being returned.
     */
    private static final class ValidationException extends RuntimeException {
        ValidationException(String message) {
            super(message);
        }
    }

    /**
     * Callback interface for grid state access and mutation.
     */
    public interface Callbacks extends Serializable {

        /**
         * Returns the current state of a grid as a JSON string. Should throw if
         * the grid is not found.
         *
         * @param gridId
         *            the grid ID
         * @return the grid state as JSON
         */
        String getState(String gridId);

        /**
         * Handles a SQL query for the given grid. Implementations should
         * validate the query and store it for deferred rendering. Should throw
         * if the grid is not found or the query is invalid.
         *
         * @param gridId
         *            the grid ID
         * @param query
         *            the SQL SELECT query
         */
        void updateData(String gridId, String query);

        /**
         * Returns the set of available grid IDs.
         *
         * @return the grid IDs, never {@code null}
         */
        Set<String> getGridIds();
    }

    /**
     * Resolves the grid ID from the tool arguments. If {@code gridId} is not
     * provided and there is exactly one grid, that grid's ID is used.
     */
    private static String resolveGridId(JsonNode args, Callbacks callbacks) {
        var idNode = args.get("gridId");
        if (idNode != null && !idNode.isNull()) {
            return idNode.asString();
        }
        var ids = callbacks.getGridIds();
        if (ids.size() == 1) {
            return ids.iterator().next();
        }
        if (ids.isEmpty()) {
            throw new ValidationException("No grids available.");
        }
        throw new ValidationException(
                "gridId is required when multiple grids exist. "
                        + "Available grid IDs: " + ids);
    }

    /**
     * Creates a tool that returns the current grid state.
     *
     * @param callbacks
     *            the callbacks for grid state access, not {@code null}
     * @return the tool definition, never {@code null}
     */
    public static LLMProvider.ToolSpec getGridState(Callbacks callbacks) {
        Objects.requireNonNull(callbacks, "callbacks must not be null");
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "get_grid_state";
            }

            @Override
            public String getDescription() {
                return "Returns the current grid state including the SQL "
                        + "query.";
            }

            @Override
            public String getParametersSchema() {
                return """
                        {
                          "type": "object",
                          "properties": {
                            "gridId": {
                              "type": "string",
                              "description": "The ID of the grid. Optional when there is only one grid."
                            }
                          }
                        }""";
            }

            @Override
            public String execute(JsonNode arguments) {
                try {
                    LOGGER.info("get_grid_state called");
                    var gridId = resolveGridId(arguments, callbacks);
                    return callbacks.getState(gridId);
                } catch (ValidationException e) {
                    LOGGER.warn("get_grid_state validation failed", e);
                    return "Error getting grid state: " + e.getMessage();
                } catch (Exception e) {
                    LOGGER.error("get_grid_state failed", e);
                    return "Error getting grid state.";
                }
            }
        };
    }

    /**
     * Creates a tool that updates the grid data with a SQL query. If the
     * handler throws, the error is returned to the LLM.
     *
     * @param callbacks
     *            the callbacks for grid mutation, not {@code null}
     * @return the tool definition, never {@code null}
     */
    public static LLMProvider.ToolSpec updateGridData(Callbacks callbacks) {
        Objects.requireNonNull(callbacks, "callbacks must not be null");
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "update_grid_data";
            }

            @Override
            public String getDescription() {
                return """
                        Updates the grid data using a SQL SELECT query. \
                        The grid creates columns from query result column names/aliases.

                        SQL RULES:
                        - ALWAYS list specific columns — NEVER use SELECT *
                        - ALWAYS give every column a human-readable AS alias
                        - Do NOT use LIMIT or OFFSET — the grid handles pagination
                        - Use double quotes for aliases with spaces or dots
                        Example: SELECT name AS "Employee Name", salary AS "Salary" FROM employees

                        COLUMN GROUPING (only when the user asks for grouping):
                        When the user mentions "grouped under X" in their request:
                        1. Select ONLY the columns mentioned for grouping
                        2. Alias each column as "X.ReadableName" (with the group prefix and a dot)
                        3. Do NOT include columns that are not part of a group
                        Example request: "product and category grouped under Product"
                        Correct SQL: SELECT product AS "Product.Name", category AS "Product.Category" FROM sales
                        Result: A "Product" header spanning both columns.
                        Do NOT use "X.Name" format unless the user asks for grouping.
                        """;
            }

            @Override
            public String getParametersSchema() {
                return """
                        {
                          "type": "object",
                          "properties": {
                            "gridId": {
                              "type": "string",
                              "description": "The ID of the grid. Optional when there is only one grid."
                            },
                            "query": {
                              "type": "string",
                              "description": "SQL SELECT query with specific columns and AS aliases. No LIMIT/OFFSET. No SELECT *."
                            }
                          },
                          "required": ["query"]
                        }""";
            }

            @Override
            public String execute(JsonNode arguments) {
                try {
                    LOGGER.info("update_grid_data called with: {}", arguments);
                    var gridId = resolveGridId(arguments, callbacks);
                    var query = arguments.get("query").asString();
                    LOGGER.info("update_grid_data gridId={} query={}", gridId,
                            query);
                    callbacks.updateData(gridId, query);
                    return "Grid '" + gridId
                            + "' data update queued successfully";
                } catch (ValidationException e) {
                    LOGGER.warn("update_grid_data validation failed", e);
                    return "Error updating grid data: " + e.getMessage();
                } catch (Exception e) {
                    LOGGER.error("update_grid_data failed", e);
                    return "Error updating grid data.";
                }
            }
        };
    }

    /**
     * Creates all grid tools for the given callbacks.
     *
     * @param callbacks
     *            the callbacks for grid state access and mutation, not
     *            {@code null}
     * @return a list of all grid tools, never {@code null}
     */
    public static List<LLMProvider.ToolSpec> createAll(Callbacks callbacks) {
        Objects.requireNonNull(callbacks, "callbacks must not be null");
        return List.of(getGridState(callbacks), updateGridData(callbacks));
    }
}
