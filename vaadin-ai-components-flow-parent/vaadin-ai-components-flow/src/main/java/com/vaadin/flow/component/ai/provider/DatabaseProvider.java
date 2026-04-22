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
 * small fixed schemas, but most databases expose introspection commands you can
 * run on the read-only connection to build the description at runtime. Pick the
 * idiom that matches your database:
 * </p>
 * <ul>
 * <li><b>H2 (2.x, including in-memory):</b> run
 * {@code SCRIPT NODATA NOSETTINGS} — returns the DDL needed to recreate the
 * schema, one statement per row. This includes user, role, sequence, and grant
 * statements alongside the table DDL, which may be more detail than the LLM
 * needs. Note that H2 requires admin rights to execute {@code SCRIPT}, so the
 * account used for introspection cannot be a true read-only role; in typical
 * in-memory setups the default {@code sa} user is admin and this just
 * works.</li>
 * <li><b>MySQL (8.x, including 8.4 LTS):</b> iterate rows of
 * {@code SHOW TABLES} and call {@code SHOW CREATE TABLE `<name>`} for each
 * table to get its DDL (returned in the {@code Create Table} column). Use
 * {@code SHOW FULL TABLES WHERE Table_type = 'BASE TABLE'} instead if you want
 * to skip views.</li>
 * <li><b>PostgreSQL:</b> query {@code information_schema.columns} and assemble
 * a per-table description from {@code table_name}, {@code column_name}, and
 * {@code data_type}. Filter by {@code table_schema} to the schema(s) you want
 * exposed — {@code 'public'} for default installs, or
 * {@code table_schema = ANY(current_schemas(false))} to follow the connection's
 * {@code search_path}.</li>
 * </ul>
 * <p>
 * For example, an H2 implementation of {@link #getSchema()} can be as simple
 * as:
 * </p>
 *
 * <pre>
 * &#064;Override
 * public String getSchema() {
 *     try (var connection = readOnlyDataSource.getConnection();
 *             var statement = connection.createStatement();
 *             var rs = statement.executeQuery("SCRIPT NODATA NOSETTINGS")) {
 *         var schema = new StringBuilder();
 *         while (rs.next()) {
 *             schema.append(rs.getString(1)).append('\n');
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
     * See the class-level "Dynamic schema retrieval" section for idioms that
     * build this description at runtime from H2, MySQL, and PostgreSQL
     * introspection commands.
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
