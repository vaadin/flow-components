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

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import com.vaadin.flow.component.ai.common.QueryLanguage;
import com.vaadin.flow.component.ai.common.QueryPage;
import com.vaadin.flow.data.provider.SortDirection;

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
 * This is the SQL form of {@link QueryableProvider}: the query language is
 * {@link QueryLanguage#SQL}, a page of a result is fetched by wrapping the
 * query in a subquery with {@code ORDER BY}, {@code LIMIT} and {@code OFFSET},
 * and the size of a result with a {@code COUNT(*)} subquery. A SQL dialect
 * without that syntax overrides {@link #executeQuery(String, QueryPage)} and
 * {@link #count(String)}; a data source that is not a SQL database implements
 * {@link QueryableProvider} directly.
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
 * @since 25.3
 */
public interface DatabaseProvider extends QueryableProvider {

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
    @Override
    String getSchema();

    /**
     * Executes the given SQL query and returns the results. Each row is
     * represented as a map from column name to column value.
     * <p>
     * Implementations should ensure that only read-only queries are executed.
     * </p>
     * <p>
     * When a query fails (e.g. references an unknown column), throw a
     * {@link ToolException} whose message describes the problem in terms safe
     * to expose — the message is passed to the LLM so it can correct the query
     * instead of retrying it unchanged. Any other exception is replaced with a
     * generic error message before reaching the LLM.
     * </p>
     *
     * @param sql
     *            the SQL query to execute, not {@code null}
     * @return the query results as a list of column-name-to-value maps, never
     *         {@code null} but may be empty
     * @throws NullPointerException
     *             if the query is {@code null}
     * @throws ToolException
     *             if the query fails and the implementation chooses to tell the
     *             LLM why; any other exception is reported to the LLM as a
     *             generic error
     */
    @Override
    List<Map<String, Object>> executeQuery(String sql);

    /**
     * Returns {@link QueryLanguage#SQL}.
     *
     * @return SQL
     * @since 25.4
     */
    @Override
    default QueryLanguage getQueryLanguage() {
        return QueryLanguage.SQL;
    }

    /**
     * Returns one page of a query's rows by wrapping the query in a subquery:
     * {@code SELECT * FROM (SELECT * FROM (sql) AS _t ORDER BY "column" ASC)
     * AS _limited LIMIT limit OFFSET offset}, without the {@code ORDER BY}
     * level when the page has no sort orders. The sort orders name result
     * columns of the query, quoted as identifiers. The wrapped query runs
     * through {@link #executeQuery(String)}.
     * <p>
     * Override this for a dialect that pages differently.
     * </p>
     *
     * @param sql
     *            the SQL query, not {@code null}
     * @param page
     *            the page to return, not {@code null}
     * @return the rows of the page, never {@code null} but may be empty
     * @throws ToolException
     *             if the query fails and the implementation chooses to tell the
     *             LLM why
     * @since 25.4
     */
    @Override
    default List<Map<String, Object>> executeQuery(String sql, QueryPage page) {
        var ordered = sql;
        if (!page.sortOrders().isEmpty()) {
            var orderBy = page.sortOrders().stream().map(sortOrder -> "\""
                    + sortOrder.getSorted() + "\" "
                    + (sortOrder.getDirection() == SortDirection.ASCENDING
                            ? "ASC"
                            : "DESC"))
                    .collect(Collectors.joining(", "));
            ordered = "SELECT * FROM (" + sql + ") AS _t ORDER BY " + orderBy;
        }
        return executeQuery("SELECT * FROM (" + ordered + ") AS _limited LIMIT "
                + page.limit() + " OFFSET " + page.offset());
    }

    /**
     * Returns the number of rows a query has by running
     * {@code SELECT COUNT(*) FROM (sql) AS _counted} through
     * {@link #executeQuery(String)} and reading the first value of the first
     * row. The count is {@code 0} when that query returns no row, a row without
     * columns, or a value that is not a number.
     * <p>
     * Override this for a dialect that counts differently.
     * </p>
     *
     * @param sql
     *            the SQL query, not {@code null}
     * @return the number of rows, never empty
     * @throws ToolException
     *             if the query fails and the implementation chooses to tell the
     *             LLM why
     * @since 25.4
     */
    @Override
    default OptionalInt count(String sql) {
        var result = executeQuery(
                "SELECT COUNT(*) FROM (" + sql + ") AS _counted");
        if (result.isEmpty() || result.getFirst().isEmpty()) {
            return OptionalInt.of(0);
        }
        var first = result.getFirst().values().iterator().next();
        return OptionalInt
                .of(first instanceof Number number ? number.intValue() : 0);
    }
}
