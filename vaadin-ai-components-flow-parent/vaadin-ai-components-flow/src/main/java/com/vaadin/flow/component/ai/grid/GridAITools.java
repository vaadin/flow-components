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
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.internal.JacksonUtils;

/**
 * Factory for creating reusable grid {@link LLMProvider.ToolSpec} instances.
 * These tools can be used by any controller that works with a grid backed by
 * SQL queries.
 *
 * @author Vaadin Ltd
 */
public final class GridAITools implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GridAITools.class);

    private GridAITools() {
    }

    /**
     * Creates a tool that returns the current grid state including the SQL
     * query.
     *
     * @param querySupplier
     *            supplies the current SQL query, not {@code null}
     * @return the tool definition, never {@code null}
     */
    public static LLMProvider.ToolSpec getGridState(
            Supplier<String> querySupplier) {
        Objects.requireNonNull(querySupplier, "querySupplier must not be null");
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "getGridCurrentState";
            }

            @Override
            public String getDescription() {
                return "Returns the current grid state including the SQL "
                        + "query. Takes no parameters.";
            }

            @Override
            public String getParametersSchema() {
                return null;
            }

            @Override
            public String execute(String arguments) {
                LOGGER.info("getGridCurrentState called");
                var query = querySupplier.get();
                if (query == null) {
                    return "{\"status\":\"empty\",\"message\":"
                            + "\"No grid data has been loaded yet\"}";
                }
                return "{\"query\":\"" + query.replace("\"", "\\\"") + "\"}";
            }
        };
    }

    /**
     * Creates a tool that updates the grid data with a SQL query. If the
     * handler throws, the error is returned to the LLM.
     *
     * @param queryHandler
     *            handles the query, not {@code null}
     * @return the tool definition, never {@code null}
     */
    public static LLMProvider.ToolSpec updateGridData(
            Consumer<String> queryHandler) {
        Objects.requireNonNull(queryHandler, "queryHandler must not be null");
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "updateGridData";
            }

            @Override
            public String getDescription() {
                return """
                        Updates the grid data using a SQL SELECT query.
                        The grid automatically creates columns from query results.
                        Use SQL aliases (AS) for human-readable column headers.
                        Do NOT use LIMIT or OFFSET — the grid handles pagination.
                        Example: SELECT name AS "Name", salary AS "Salary" FROM employees
                        """;
            }

            @Override
            public String getParametersSchema() {
                return """
                        {
                          "type": "object",
                          "properties": {
                            "query": {
                              "type": "string",
                              "description": "SQL SELECT query without LIMIT/OFFSET"
                            }
                          },
                          "required": ["query"]
                        }
                        """;
            }

            @Override
            public String execute(String arguments) {
                try {
                    LOGGER.info("updateGridData called with: {}", arguments);
                    var query = JacksonUtils.readTree(arguments).get("query")
                            .asString();
                    LOGGER.info("updateGridData query: {}", query);
                    queryHandler.accept(query);
                    return "Grid data update queued successfully";
                } catch (Exception e) {
                    LOGGER.error("updateGridData failed", e);
                    return "Error updating grid data: " + e.getMessage();
                }
            }
        };
    }

    /**
     * Creates all grid tools for the given callbacks.
     *
     * @param querySupplier
     *            supplies the current SQL query, not {@code null}
     * @param queryHandler
     *            handles queries, not {@code null}
     * @return a list of all grid tools, never {@code null}
     */
    public static List<LLMProvider.ToolSpec> createAll(
            Supplier<String> querySupplier, Consumer<String> queryHandler) {
        return List.of(getGridState(querySupplier),
                updateGridData(queryHandler));
    }
}
