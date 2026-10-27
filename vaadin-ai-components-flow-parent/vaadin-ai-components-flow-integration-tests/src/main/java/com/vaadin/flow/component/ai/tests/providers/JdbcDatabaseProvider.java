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
package com.vaadin.flow.component.ai.tests.providers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import javax.sql.DataSource;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.ToolException;

/**
 * A {@link DatabaseProvider} over any JDBC {@link DataSource}: the schema is
 * read from the database metadata and queries run through plain JDBC. This is
 * the provider an application with a {@code DataSource} bean would write, and
 * it needs no Calcite, since the endpoint already speaks SQL.
 * <p>
 * Read-only access is the data source's job: pair this provider with a data
 * source that connects as a user with SELECT rights only. The provider itself
 * only insists that a query starts with SELECT or WITH.
 * </p>
 */
public class JdbcDatabaseProvider implements DatabaseProvider {

    private final transient Supplier<DataSource> dataSource;
    private final String dialect;
    private final String schemaName;
    private final String notes;

    /**
     * Creates a provider over one schema of a data source.
     *
     * @param dataSource
     *            supplies the data source to query; called on every use
     * @param dialect
     *            the SQL dialect to tell the LLM, for example
     *            {@code "PostgreSQL SQL"}
     * @param schemaName
     *            the schema to describe, as the database reports it, or
     *            {@code null} for the connection's current schema
     * @param notes
     *            free-form notes appended to the schema description, or
     *            {@code null}
     */
    public JdbcDatabaseProvider(Supplier<DataSource> dataSource, String dialect,
            String schemaName, String notes) {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.dialect = Objects.requireNonNull(dialect);
        this.schemaName = schemaName;
        this.notes = notes == null ? "" : notes;
    }

    @Override
    public String getSchema() {
        var schema = new StringBuilder("DATABASE SCHEMA (query it with ")
                .append(dialect).append("):\n\n");
        try (var connection = dataSource.get().getConnection()) {
            schema.append(JdbcSchemaDescriber.describe(connection,
                    schemaName == null ? connection.getSchema() : schemaName));
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to read the database schema", e);
        }
        if (!notes.isBlank()) {
            schema.append("NOTES:\n").append(notes);
        }
        return schema.toString();
    }

    @Override
    public List<Map<String, Object>> executeQuery(String sql) {
        var upper = sql.trim().toUpperCase(Locale.ROOT);
        if (!upper.startsWith("SELECT") && !upper.startsWith("WITH")) {
            throw new ToolException("Only SELECT queries are allowed.");
        }
        var results = new ArrayList<Map<String, Object>>();
        try (var connection = dataSource.get().getConnection();
                var statement = connection.createStatement();
                var resultSet = statement.executeQuery(sql)) {
            var metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                var row = new LinkedHashMap<String, Object>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
                }
                results.add(row);
            }
        } catch (SQLException e) {
            if (isSafeToRelay(e)) {
                throw new ToolException(e.getMessage(), e);
            }
            throw new IllegalStateException("Failed to execute query", e);
        }
        return results;
    }

    /**
     * Tells whether the message of a failed query may be relayed to the LLM. By
     * default this is the case for syntax errors and unknown tables or columns
     * (SQL state class 42), whose messages quote only the query itself, and for
     * rights violations (H2 state 90096), whose messages quote only the object
     * name.
     *
     * @param e
     *            the exception the query failed with
     * @return {@code true} to relay the message as a {@link ToolException}
     */
    protected boolean isSafeToRelay(SQLException e) {
        var state = e.getSQLState();
        return state != null
                && (state.startsWith("42") || state.equals("90096"));
    }
}
