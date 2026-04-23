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
 * Provider for database schema information and SQL query execution on behalf of
 * an LLM. This interface enables AI-powered components to interact with
 * application databases for features.
 * <p>
 * Applications implement this interface with their own database connection. The
 * provider exposes the schema so the LLM can generate valid SQL, and executes
 * the resulting queries.
 * </p>
 * <p>
 * <b>Note:</b> For security, the implementation should use a database account
 * with read-only access limited to the relevant tables or views. This prevents
 * the LLM from inadvertently modifying or deleting data.
 * </p>
 *
 * <pre>
 * public class MyDatabaseProvider implements DatabaseProvider {
 *
 *     private final DataSource readOnlyDataSource;
 *
 *     public MyDatabaseProvider(DataSource readOnlyDataSource) {
 *         this.readOnlyDataSource = readOnlyDataSource;
 *     }
 *
 *     &#064;Override
 *     public String getSchema() {
 *         return "Tables: employees(id INT, name VARCHAR, dept VARCHAR), "
 *                 + "departments(id INT, name VARCHAR). Dialect: PostgreSQL.";
 *     }
 *
 *     &#064;Override
 *     public List&lt;Map&lt;String, Object&gt;&gt; executeQuery(String sql) {
 *         try (var connection = readOnlyDataSource.getConnection();
 *                 var statement = connection.prepareStatement(sql);
 *                 var resultSet = statement.executeQuery()) {
 *             // Convert to List&lt;Map&lt;String, Object&gt;&gt;
 *         }
 *     }
 * }
 * </pre>
 *
 * <p>
 * <b>Dynamic schema retrieval:</b> Hand-writing the schema string works for
 * small fixed schemas, but for larger schemas {@link java.sql.DatabaseMetaData}
 * builds the description at runtime from a truly read-only connection and works
 * consistently across H2, MySQL, and PostgreSQL. Including primary and foreign
 * keys helps the LLM pick correct joins rather than guessing them from column
 * names:
 * </p>
 *
 * <pre>
 * &#064;Override
 * public String getSchema() {
 *     try (var connection = readOnlyDataSource.getConnection()) {
 *         var meta = connection.getMetaData();
 *         var catalog = connection.getCatalog();
 *         var schemaName = connection.getSchema();
 *         var schema = new StringBuilder();
 *         try (var tables = meta.getTables(catalog, schemaName, "%",
 *                 new String[] { "TABLE" })) {
 *             while (tables.next()) {
 *                 var table = tables.getString("TABLE_NAME");
 *                 // Append columns via meta.getColumns(catalog, schemaName,
 *                 // table, "%")
 *                 // Append primary keys via meta.getPrimaryKeys(catalog,
 *                 // schemaName, table)
 *                 // Append foreign keys via meta.getImportedKeys(catalog,
 *                 // schemaName, table)
 *             }
 *         }
 *         return schema.toString();
 *     } catch (SQLException e) {
 *         throw new RuntimeException(e);
 *     }
 * }
 * </pre>
 *
 * @author Vaadin Ltd
 */
public interface DatabaseProvider extends Serializable {

    /**
     * Returns a text description of the database schema available to the LLM.
     * The description should include table names, column names with their
     * types, and optionally the SQL dialect (e.g., PostgreSQL, MySQL). The LLM
     * uses this information to generate valid SQL queries.
     * <p>
     * See the class-level "Dynamic schema retrieval" section for a
     * {@link java.sql.DatabaseMetaData}-based example that builds this
     * description at runtime.
     * </p>
     *
     * @return a text description of the database schema, never {@code null}
     */
    String getSchema();

    /**
     * Executes the given SQL query and returns the results. Each row is
     * represented as a map from column name to column value.
     * <p>
     * Implementations should ensure that only read-only queries are executed.
     * </p>
     *
     * @param sql
     *            the SQL query to execute, not {@code null}
     * @return the query results as a list of column-name-to-value maps, never
     *         {@code null} but may be empty
     * @throws NullPointerException
     *             if the query is {@code null}
     * @throws IllegalArgumentException
     *             if the query is invalid
     */
    List<Map<String, Object>> executeQuery(String sql);
}
