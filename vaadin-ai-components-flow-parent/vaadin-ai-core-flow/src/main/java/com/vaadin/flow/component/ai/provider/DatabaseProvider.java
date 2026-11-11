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
import java.util.Objects;
import java.util.stream.Collectors;

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
 * A component that loads rows lazily, such as a grid, asks for one page of a
 * query's rows at a time with {@link #executeQuery(String, QueryPage)} and for
 * the size of the result with {@link #count(String)}. By default both add
 * {@code ORDER BY}, {@code LIMIT} and {@code OFFSET}, or {@code COUNT(*)}, to
 * the query and run the result through {@link #executeQuery(String)}. Override
 * them when your SQL dialect pages or counts differently, or when your provider
 * can do it more efficiently. A provider that delegates to another provider
 * should delegate these methods too, so that the other provider's own paging is
 * used.
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
    List<Map<String, Object>> executeQuery(String sql);

    /**
     * Executes the given SQL query and returns one page of its rows, for a
     * component that loads rows lazily. The default builds one of the following
     * statements and runs it through {@link #executeQuery(String)}:
     * <ul>
     * <li>With sort orders, {@code SELECT * FROM (sql) AS _t ORDER BY "column"
     * ASC LIMIT limit OFFSET offset}. The sort orders name result columns of
     * the query, written as quoted identifiers.</li>
     * <li>Without sort orders, the query followed by
     * {@code LIMIT limit OFFSET offset}, so that the rows keep the order of an
     * {@code ORDER BY} in the query.</li>
     * <li>Without sort orders, when the query has a row limit of its own,
     * {@code SELECT * FROM (sql) AS _limited LIMIT limit OFFSET offset}. The
     * query has a row limit of its own when the word {@code LIMIT},
     * {@code OFFSET} or {@code FETCH} appears in it, in any letter case,
     * outside of quoted identifiers, string literals and comments.</li>
     * </ul>
     * The query may end in a semicolon, which is left out, or in a line
     * comment.
     * <p>
     * Override this when your SQL dialect pages differently, for example with
     * {@code OFFSET ... FETCH}, or when your provider can page more
     * efficiently.
     * </p>
     *
     * @param sql
     *            the SQL query, not {@code null}
     * @param page
     *            the page to return, not {@code null}
     * @return the rows of the page, never {@code null} but may be empty
     * @throws NullPointerException
     *             if the query or the page is {@code null}
     * @throws ToolException
     *             if the query fails and the implementation chooses to tell the
     *             LLM why. The message reaches the LLM, and any other exception
     *             is reported to it as a generic error, only when the page is
     *             requested during a tool call. An exception thrown while a
     *             component loads rows later is not reported to the LLM.
     * @since 25.4
     */
    default List<Map<String, Object>> executeQuery(String sql, QueryPage page) {
        Objects.requireNonNull(sql, "sql must not be null");
        Objects.requireNonNull(page, "page must not be null");
        var query = asOpenQuery(sql);
        var limit = "LIMIT " + page.limit() + " OFFSET " + page.offset();
        if (!page.sortOrders().isEmpty()) {
            var orderBy = page.sortOrders().stream().map(sortOrder -> "\""
                    + sortOrder.getSorted().replace("\"", "\"\"") + "\" "
                    + (sortOrder.getDirection() == SortDirection.DESCENDING
                            ? "DESC"
                            : "ASC"))
                    .collect(Collectors.joining(", "));
            // The sort and the limit go on one level, since a SQL engine may
            // drop an ORDER BY of a sub-query that has no limit
            return executeQuery("SELECT * FROM (" + query + ") AS _t ORDER BY "
                    + orderBy + " " + limit);
        }
        if (limitsRows(query)) {
            // A sub-query with a limit keeps its rows and their order
            return executeQuery(
                    "SELECT * FROM (" + query + ") AS _limited " + limit);
        }
        // Appending keeps an ORDER BY of the query on the top level
        return executeQuery(query + limit);
    }

    /**
     * Returns the number of rows the given SQL query returns, for a component
     * that loads rows lazily and needs the size of the result. The default runs
     * {@code SELECT COUNT(*) FROM (sql) AS _counted} through
     * {@link #executeQuery(String)} and reads the first value of the first row.
     * The query may end in a semicolon, which is left out, or in a line
     * comment. The count is {@code 0} when that query returns no row, a row
     * without columns, or a value that is not a number.
     * <p>
     * Override this when your SQL dialect counts differently, or when your
     * provider can count more efficiently.
     * </p>
     *
     * @param sql
     *            the SQL query, not {@code null}
     * @return the number of rows
     * @throws NullPointerException
     *             if the query is {@code null}
     * @throws ToolException
     *             if the query fails and the implementation chooses to tell the
     *             LLM why. The message reaches the LLM, and any other exception
     *             is reported to it as a generic error, only when the count is
     *             requested during a tool call. An exception thrown while a
     *             component loads rows later is not reported to the LLM.
     * @since 25.4
     */
    default int count(String sql) {
        Objects.requireNonNull(sql, "sql must not be null");
        var result = executeQuery(
                "SELECT COUNT(*) FROM (" + asOpenQuery(sql) + ") AS _counted");
        if (result.isEmpty() || result.getFirst().isEmpty()) {
            return 0;
        }
        var first = result.getFirst().values().iterator().next();
        return first instanceof Number number ? number.intValue() : 0;
    }

    /**
     * Returns the given query in a form that more SQL can follow, either a
     * clause or the closing parenthesis of a sub-query: without semicolons and
     * whitespace at its end, and with a line break that keeps a line comment
     * that ends the query from reaching the SQL that follows.
     */
    private static String asOpenQuery(String sql) {
        var end = sql.length();
        while (end > 0 && (sql.charAt(end - 1) == ';'
                || Character.isWhitespace(sql.charAt(end - 1)))) {
            end--;
        }
        return sql.substring(0, end) + "\n";
    }

    /**
     * Returns whether the word {@code LIMIT}, {@code OFFSET} or {@code FETCH}
     * appears in the given query outside of quoted identifiers, string literals
     * and comments. This is a text check, which also finds a limit of a
     * sub-query in the query. Appending a limit to a query that has one is an
     * error, while wrapping it is not, so a doubtful case counts as a limit.
     */
    private static boolean limitsRows(String sql) {
        var start = 0;
        while (start < sql.length()) {
            var end = tokenEnd(sql, start);
            var token = sql.substring(start, end);
            if (token.equalsIgnoreCase("LIMIT")
                    || token.equalsIgnoreCase("OFFSET")
                    || token.equalsIgnoreCase("FETCH")) {
                return true;
            }
            start = end;
        }
        return false;
    }

    /**
     * Returns the end of the token that starts at the given index: a quoted
     * identifier, a string literal, a comment, a word, or any other single
     * character. An escaped quote, written as two quotes, ends a quoted token
     * and starts the next one right away, so it needs no handling of its own.
     */
    private static int tokenEnd(String sql, int start) {
        var first = sql.charAt(start);
        if (first == '"' || first == '\'' || first == '`') {
            return indexAfter(sql, String.valueOf(first), start + 1);
        }
        if (sql.startsWith("--", start)) {
            return indexAfter(sql, "\n", start + 2);
        }
        if (sql.startsWith("/*", start)) {
            return indexAfter(sql, "*/", start + 2);
        }
        var end = start + 1;
        if (isWordPart(first)) {
            while (end < sql.length() && isWordPart(sql.charAt(end))) {
                end++;
            }
        }
        return end;
    }

    /**
     * Returns the index after the next occurrence of the given text from the
     * given index on. When the text does not occur, returns the given index, so
     * that the text after an unclosed quote or comment is still checked.
     */
    private static int indexAfter(String sql, String text, int from) {
        var index = sql.indexOf(text, from);
        return index < 0 ? from : index + text.length();
    }

    private static boolean isWordPart(char character) {
        return Character.isLetterOrDigit(character) || character == '_';
    }
}
