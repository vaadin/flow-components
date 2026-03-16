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
package com.vaadin.flow.component.ai.provider;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Interface for database access providers.
 * <p>
 * Implementations of this interface provide access to database schemas and
 * query execution capabilities for AI-based components.
 * </p>
 * <p>
 * <strong>Security Notice:</strong> Always use read-only database credentials
 * with access restricted to only necessary tables and views. Never use
 * credentials with write, update, or delete permissions.
 * </p>
 *
 * @author Vaadin Ltd
 */
public interface DatabaseProvider extends Serializable {

    /**
     * Retrieves the database schema information.
     * <p>
     * The schema should include table names, column names, data types, and
     * relationships that are relevant for generating queries.
     * </p>
     *
     * @return a string representation of the database schema
     */
    String getSchema();

    /**
     * Returns an LLM tool definition that retrieves the database schema.
     * <p>
     * This tool can be used by any controller that needs to expose database
     * schema information to the LLM.
     * </p>
     *
     * @return a tool definition for retrieving the schema
     */
    default LLMProvider.ToolDefinition getSchemaTool() {
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
                return getSchema();
            }
        };
    }

    /**
     * Executes a SQL query and returns the results.
     * <p>
     * <strong>Security Notice:</strong> This method should only execute SELECT
     * queries. Implementations must validate that the query is read-only and
     * reject any attempts to modify data.
     * </p>
     *
     * @param query
     *            the SQL query to execute
     * @return a list of rows, where each row is represented as a map of column
     *         names to values
     * @throws IllegalArgumentException
     *             if the query is not a valid SELECT query
     * @throws RuntimeException
     *             if query execution fails
     */
    List<Map<String, Object>> executeQuery(String query);
}
