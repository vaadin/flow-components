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

import java.io.StringReader;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.calcite.config.Lex;
import org.apache.calcite.interpreter.Bindables;
import org.apache.calcite.jdbc.Driver;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelHomogeneousShuttle;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.logical.LogicalTableScan;
import org.apache.calcite.runtime.CalciteContextException;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.validate.SqlValidatorException;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.RelConversionException;
import org.apache.calcite.tools.RelRunner;
import org.apache.calcite.tools.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.provider.TableSource.Pushdown;

/**
 * A {@link DatabaseProvider} that lets the LLM write SQL over data that is not
 * in a SQL database: each {@link TableSource} you give it, a REST collection, a
 * CSV file or any other listing of rows, becomes one table of one schema, and
 * Apache Calcite runs the SQL over them. Joins, aggregation, sorting and paging
 * work across sources, because the engine computes in memory whatever a source
 * cannot do itself; a source that can filter declares it and receives the
 * filters of a query as a {@link Pushdown}.
 * <p>
 * The schema text lists the tables with their columns and SQL types, and quotes
 * a column name the LLM has to quote because it is a reserved word. Identifiers
 * are matched case-insensitively, so sources may keep the names of their
 * backend, {@code monthOrder} as well as {@code month_order}. Only
 * {@code SELECT} statements are accepted; the sources are never written to.
 * </p>
 * <p>
 * A query scans every source it reads from, and a grid reads its query several
 * times when it renders: once for the columns, once for the count, and once per
 * page. Rows fetched from a source are therefore kept for a short while, see
 * {@link #setCacheDuration(Duration)}, so that these reads reach the backend
 * once.
 * </p>
 * <p>
 * Apache Calcite is an optional dependency of this module: add
 * {@code org.apache.calcite:calcite-core} to your project to use this class.
 * </p>
 *
 * @author Vaadin Ltd
 * @since 25.4
 */
public class FederatedDatabaseProvider implements DatabaseProvider {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(FederatedDatabaseProvider.class);

    private static final String SCHEMA_NAME = "federated";

    private static final String DIALECT_NOTES = """
            - SQL dialect: Apache Calcite, standard SQL. JOIN, GROUP BY, subqueries, LIMIT and OFFSET are supported
            - Identifiers are case-insensitive. A column name shown in double quotes is a reserved word and must be written with the quotes
            - Compare DATE columns with DATE literals, for example tradeDate >= DATE '2024-01-01'
            - Only SELECT statements are accepted
            """;

    private final List<TableSource> sources;
    private final String notes;
    private Duration cacheDuration = Duration.ofSeconds(10);

    private transient Map<String, Table> tables;
    private transient Map<CacheKey, CacheEntry> cache;

    private record CacheKey(String source, Pushdown pushdown) {
    }

    private record CacheEntry(List<Map<String, Object>> rows, long expiresAt) {
    }

    /**
     * Creates a provider over the given sources.
     *
     * @param sources
     *            the sources, each becoming a table; at least one, with
     *            distinct names
     * @throws IllegalArgumentException
     *             if there are no sources, two share a name, or a column has a
     *             type a source may not use
     */
    public FederatedDatabaseProvider(List<TableSource> sources) {
        this(sources, null);
    }

    /**
     * Creates a provider over the given sources, with notes for the LLM
     * appended to the schema, for example which column to order by for a
     * chronological series.
     *
     * @param sources
     *            the sources, each becoming a table; at least one, with
     *            distinct names
     * @param notes
     *            notes appended to the schema text, or {@code null} for none
     * @throws IllegalArgumentException
     *             if there are no sources, two share a name, or a column has a
     *             type a source may not use
     */
    public FederatedDatabaseProvider(List<TableSource> sources, String notes) {
        Objects.requireNonNull(sources, "sources must not be null");
        if (sources.isEmpty()) {
            throw new IllegalArgumentException("At least one source is needed");
        }
        var names = new HashSet<String>();
        for (var source : sources) {
            var name = source.getName();
            if (!names.add(name.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException(
                        "Two sources are named " + name);
            }
            if (source.getColumns().isEmpty()) {
                throw new IllegalArgumentException(
                        "Source " + name + " has no columns");
            }
            for (var column : source.getColumns()) {
                if (SourceTable.sqlType(column.type()) == null) {
                    throw new IllegalArgumentException("Column " + column.name()
                            + " of source " + name + " has unsupported type "
                            + column.type().getName());
                }
            }
        }
        this.sources = List.copyOf(sources);
        this.notes = notes;
    }

    /**
     * Returns how long rows fetched from a source are reused for later queries
     * with the same pushdown. The default is ten seconds.
     *
     * @return the cache duration, never {@code null}
     */
    public Duration getCacheDuration() {
        return cacheDuration;
    }

    /**
     * Sets how long rows fetched from a source are reused for later queries
     * with the same pushdown. The default is ten seconds; {@link Duration#ZERO}
     * fetches on every query.
     *
     * @param cacheDuration
     *            the cache duration, not {@code null} or negative
     */
    public void setCacheDuration(Duration cacheDuration) {
        Objects.requireNonNull(cacheDuration, "cacheDuration must not be null");
        if (cacheDuration.isNegative()) {
            throw new IllegalArgumentException(
                    "cacheDuration must not be negative");
        }
        this.cacheDuration = cacheDuration;
    }

    @Override
    public String getSchema() {
        var metadata = SqlParser.create(new StringReader(""), parserConfig())
                .getMetadata();
        var schema = new StringBuilder("DATABASE SCHEMA:\n\n");
        for (var source : sources) {
            schema.append("TABLE ").append(source.getName());
            if (source.getDescription() != null) {
                schema.append("  (").append(source.getDescription())
                        .append(')');
            }
            schema.append('\n');
            for (var column : source.getColumns()) {
                var name = column.name();
                if (metadata.isReservedWord(name.toUpperCase(Locale.ROOT))) {
                    name = '"' + name + '"';
                }
                schema.append("  ").append(name).append(' ')
                        .append(SourceTable.sqlType(column.type()).getName())
                        .append('\n');
            }
            schema.append('\n');
        }
        schema.append("NOTES:\n").append(DIALECT_NOTES);
        if (notes != null) {
            schema.append(notes);
        }
        return schema.toString();
    }

    @Override
    public List<Map<String, Object>> executeQuery(String sql) {
        Objects.requireNonNull(sql, "sql must not be null");
        var plan = plan(sql);
        try (var connection = new Driver().connect("jdbc:calcite:",
                new Properties());
                var statement = connection.unwrap(RelRunner.class)
                        .prepareStatement(plan);
                var resultSet = statement.executeQuery()) {
            return readRows(resultSet);
        } catch (SQLException e) {
            throw translate(e);
        }
    }

    /**
     * Parses, validates and converts the query over the sources. Planning goes
     * through the Frameworks API rather than a JDBC connection, because the
     * conversion has to keep an {@code ORDER BY} inside a subquery: SQL does
     * not promise that order and Calcite drops it by default, but the grid
     * sorts its rows exactly that way.
     */
    private RelNode plan(String sql) {
        var rootSchema = Frameworks.createRootSchema(true);
        var schema = rootSchema.add(SCHEMA_NAME, new AbstractSchema() {
            @Override
            protected Map<String, Table> getTableMap() {
                return sourceTables();
            }
        });
        var config = Frameworks.newConfigBuilder().parserConfig(parserConfig())
                .defaultSchema(schema)
                .sqlToRelConverterConfig(
                        SqlToRelConverter.config().withTrimUnusedFields(true)
                                .withRemoveSortInSubQuery(false))
                .build();
        try (var planner = Frameworks.getPlanner(config)) {
            var statement = planner.parse(sql);
            if (!statement.getKind().belongsTo(SqlKind.QUERY)) {
                throw new ToolException("Only SELECT queries are allowed.");
            }
            var validated = planner.validate(statement);
            return planner.rel(validated).project().accept(BINDABLE_SCANS);
        } catch (SqlParseException e) {
            throw new ToolException(parseMessage(e), e);
        } catch (ValidationException e) {
            throw new ToolException(validationMessage(e), e);
        } catch (RelConversionException e) {
            LOGGER.warn("Query could not be converted", e);
            throw new IllegalStateException("Query failed", e);
        }
    }

    /**
     * Replaces the logical scans with bindable ones, which read the sources
     * through the plan itself instead of looking the tables up in the schema of
     * the executing connection, as {@code RelRunners} does.
     */
    private static final RelHomogeneousShuttle BINDABLE_SCANS = new RelHomogeneousShuttle() {
        @Override
        public RelNode visit(TableScan scan) {
            RelOptTable table = scan.getTable();
            if (scan instanceof LogicalTableScan
                    && Bindables.BindableTableScan.canHandle(table)) {
                return Bindables.BindableTableScan.create(scan.getCluster(),
                        table);
            }
            return super.visit(scan);
        }
    };

    private static SqlParser.Config parserConfig() {
        return SqlParser.config().withLex(Lex.MYSQL_ANSI);
    }

    private static List<Map<String, Object>> readRows(ResultSet resultSet)
            throws SQLException {
        var metaData = resultSet.getMetaData();
        var columnCount = metaData.getColumnCount();
        var rows = new ArrayList<Map<String, Object>>();
        while (resultSet.next()) {
            var row = new LinkedHashMap<String, Object>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnLabel(i),
                        toJavaTime(resultSet.getObject(i)));
            }
            rows.add(row);
        }
        return rows;
    }

    private static Object toJavaTime(Object value) {
        if (value instanceof Date date) {
            return date.toLocalDate();
        } else if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        } else if (value instanceof Time time) {
            return time.toLocalTime();
        }
        return value;
    }

    /**
     * Turns an execution failure into what the LLM may see: the message of a
     * {@link ToolException} a source threw. Anything else stays a generic
     * failure.
     */
    private static RuntimeException translate(SQLException exception) {
        for (Throwable cause = exception; cause != null; cause = cause
                .getCause()) {
            if (cause instanceof ToolException toolException) {
                return new ToolException(toolException.getMessage(), exception);
            }
        }
        LOGGER.warn("Query failed", exception);
        return new IllegalStateException("Query failed", exception);
    }

    /**
     * Returns the validator's own message, which names only what the query and
     * the schema already contain, without the position prefix Calcite adds
     * around it.
     */
    private static String validationMessage(ValidationException exception) {
        String message = null;
        for (Throwable cause = exception; cause != null; cause = cause
                .getCause()) {
            if (cause instanceof SqlValidatorException) {
                return cause.getMessage();
            } else if (cause instanceof CalciteContextException) {
                message = cause.getMessage();
            }
        }
        return message != null ? message : "The query is not valid.";
    }

    /**
     * Keeps the position and the unexpected token of a parse error and drops
     * the list of expected tokens, which is long and does not help the LLM.
     */
    private static String parseMessage(SqlParseException exception) {
        var message = exception.getMessage();
        var expecting = message.indexOf("\nWas expecting");
        if (expecting > 0) {
            message = message.substring(0, expecting);
        }
        var position = exception.getPos();
        if (position != null && !message.contains("line ")) {
            message += " (line " + position.getLineNum() + ", column "
                    + position.getColumnNum() + ")";
        }
        return message;
    }

    private synchronized Map<String, Table> sourceTables() {
        if (tables == null) {
            var map = new LinkedHashMap<String, Table>();
            for (var source : sources) {
                map.put(source.getName(), new SourceTable(source,
                        pushdown -> fetch(source, pushdown)));
            }
            tables = map;
        }
        return tables;
    }

    private List<Map<String, Object>> fetch(TableSource source,
            Pushdown pushdown) {
        if (cacheDuration.isZero()) {
            return source.fetch(pushdown);
        }
        var key = new CacheKey(source.getName(), pushdown);
        var now = System.nanoTime();
        var entry = cache().get(key);
        if (entry != null && now - entry.expiresAt() < 0) {
            return entry.rows();
        }
        var rows = List.copyOf(source.fetch(pushdown));
        cache().values().removeIf(other -> now - other.expiresAt() >= 0);
        cache().put(key, new CacheEntry(rows, now + cacheDuration.toNanos()));
        return rows;
    }

    private synchronized Map<CacheKey, CacheEntry> cache() {
        if (cache == null) {
            cache = new ConcurrentHashMap<>();
        }
        return cache;
    }
}
