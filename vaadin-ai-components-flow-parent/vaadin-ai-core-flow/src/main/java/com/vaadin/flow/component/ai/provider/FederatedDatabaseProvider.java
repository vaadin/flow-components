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
import java.util.function.Supplier;

import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.config.Lex;
import org.apache.calcite.interpreter.Bindables;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.Driver;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.RelHomogeneousShuttle;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.logical.LogicalProject;
import org.apache.calcite.rel.logical.LogicalSort;
import org.apache.calcite.rel.logical.LogicalTableScan;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rel.type.RelDataTypeSystemImpl;
import org.apache.calcite.rex.RexUtil;
import org.apache.calcite.runtime.CalciteContextException;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.validate.SqlValidatorException;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Programs;
import org.apache.calcite.tools.RelConversionException;
import org.apache.calcite.tools.RelRunner;
import org.apache.calcite.tools.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.provider.TableSource.Pushdown;

/**
 * A {@link DatabaseProvider} that lets the LLM write SQL over data that is not
 * in a SQL database. Each {@link TableSource} you give it, a REST collection, a
 * CSV file or any other listing of rows, and each {@link AdapterTable}, a table
 * one of Apache Calcite's own adapters reads, becomes one table of one schema,
 * and Calcite runs the SQL over them. Joins, aggregation, sorting and paging
 * work across tables, because the engine computes in memory whatever a backend
 * cannot do itself. A source declares what it can do on its own and receives
 * those parts of a query as a {@link Pushdown}: the equality filters, and for a
 * query it takes entirely, its page or its count, see {@link TableSource}. An
 * adapter pushes down whatever its backend supports.
 * <p>
 * The schema text lists the tables with their columns and SQL types, and quotes
 * a column name the LLM has to quote because it is a reserved word. Identifiers
 * are matched case-insensitively, so sources may keep the names of their
 * backend, {@code monthOrder} as well as {@code month_order}. Only
 * {@code SELECT} statements are accepted; the backends are never written to.
 * </p>
 * <p>
 * A grid reads its query several times when it renders: once for the columns,
 * once for the count, and once per page. Rows and counts read from a source are
 * therefore kept for a short while, see {@link #setCacheDuration(Duration)}, so
 * that a read with the same pushdown reaches the backend once. The provider
 * keeps one Calcite connection for its lifetime and runs one query at a time on
 * it.
 * </p>
 * <p>
 * Apache Calcite is an optional dependency of this module: add
 * {@code org.apache.calcite:calcite-core} to your project to use this class,
 * and the adapter module of each adapter table.
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

    /**
     * Replaces the logical scans of sources with bindable ones, which read the
     * source through the plan itself instead of looking the table up in the
     * schema, as Calcite's {@code RelRunners} does. Adapter tables are left to
     * their adapters.
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

    /**
     * Merges a limit directly over a sorted subquery, {@code SELECT * FROM
     * (... ORDER BY x) LIMIT n OFFSET m} as the grid writes it, into one sort
     * with the order, the offset and the limit. Kept as two operators, an
     * adapter's sort rule is free to drop the inner order, because SQL does not
     * promise it; Calcite's MongoDB rule does exactly that.
     */
    private static final RelHomogeneousShuttle MERGE_SORTS = new RelHomogeneousShuttle() {
        @Override
        public RelNode visit(RelNode other) {
            var visited = super.visit(other);
            if (!(visited instanceof LogicalSort outer)
                    || !outer.getCollation().getFieldCollations().isEmpty()
                    || (outer.offset == null && outer.fetch == null)) {
                return visited;
            }
            var input = outer.getInput();
            LogicalProject project = null;
            if (input instanceof LogicalProject candidate
                    && RexUtil.isIdentity(candidate.getProjects(),
                            candidate.getInput().getRowType())) {
                project = candidate;
                input = candidate.getInput();
            }
            if (!(input instanceof LogicalSort inner) || inner.offset != null
                    || inner.fetch != null) {
                return visited;
            }
            var merged = LogicalSort.create(inner.getInput(),
                    inner.getCollation(), outer.offset, outer.fetch);
            return project == null ? merged
                    : project.copy(project.getTraitSet(), List.of(merged));
        }
    };

    private final List<TableSource> sources;
    private final List<AdapterTable> adapterTables;
    private final String notes;
    private Duration cacheDuration = Duration.ofSeconds(10);

    private transient CalciteConnection connection;
    private transient SchemaPlus schema;
    private transient Map<String, String> descriptions;
    private transient Map<CacheKey, CacheEntry> cache;

    private record CacheKey(String source, String read, Pushdown pushdown) {
    }

    private record CacheEntry(Object value, long expiresAt) {
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
        this(sources, List.of(), notes);
    }

    /**
     * Creates a provider over the given sources and adapter tables, with notes
     * for the LLM appended to the schema.
     *
     * @param sources
     *            the sources, each becoming a table, not {@code null}
     * @param adapterTables
     *            the adapter tables, each becoming a table, not {@code null}
     * @param notes
     *            notes appended to the schema text, or {@code null} for none
     * @throws IllegalArgumentException
     *             if there are no tables at all, two share a name, or a source
     *             column has a type a source may not use
     */
    public FederatedDatabaseProvider(List<TableSource> sources,
            List<AdapterTable> adapterTables, String notes) {
        Objects.requireNonNull(sources, "sources must not be null");
        Objects.requireNonNull(adapterTables, "adapterTables must not be null");
        if (sources.isEmpty() && adapterTables.isEmpty()) {
            throw new IllegalArgumentException("At least one table is needed");
        }
        var names = new HashSet<String>();
        for (var source : sources) {
            var name = source.getName();
            requireUniqueName(names, name);
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
        for (var adapterTable : adapterTables) {
            requireUniqueName(names, adapterTable.getName());
        }
        this.sources = List.copyOf(sources);
        this.adapterTables = List.copyOf(adapterTables);
        this.notes = notes;
    }

    private static void requireUniqueName(HashSet<String> names, String name) {
        if (!names.add(name.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Two tables are named " + name);
        }
    }

    /**
     * Returns how long rows and counts read from a source are reused for later
     * queries with the same pushdown. The default is ten seconds.
     *
     * @return the cache duration, never {@code null}
     */
    public Duration getCacheDuration() {
        return cacheDuration;
    }

    /**
     * Sets how long rows and counts read from a source are reused for later
     * queries with the same pushdown. The default is ten seconds;
     * {@link Duration#ZERO} reads on every query. Adapter tables are not
     * cached; their adapters decide what reaches the backend.
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
    public synchronized String getSchema() {
        connect();
        var metadata = SqlParser.create(new StringReader(""), parserConfig())
                .getMetadata();
        var typeFactory = new JavaTypeFactoryImpl();
        var text = new StringBuilder("DATABASE SCHEMA:\n\n");
        for (var entry : descriptions.entrySet()) {
            var name = entry.getKey();
            text.append("TABLE ").append(name);
            if (entry.getValue() != null) {
                text.append("  (").append(entry.getValue()).append(')');
            }
            text.append('\n');
            var rowType = Objects.requireNonNull(schema.tables().get(name))
                    .getRowType(typeFactory);
            for (var field : rowType.getFieldList()) {
                var column = field.getName();
                if (metadata.isReservedWord(column.toUpperCase(Locale.ROOT))) {
                    column = '"' + column + '"';
                }
                text.append("  ").append(column).append(' ')
                        .append(field.getType().getSqlTypeName().getName())
                        .append('\n');
            }
            text.append('\n');
        }
        text.append("NOTES:\n").append(DIALECT_NOTES);
        if (notes != null) {
            text.append(notes);
        }
        return text.toString();
    }

    @Override
    public synchronized List<Map<String, Object>> executeQuery(String sql) {
        Objects.requireNonNull(sql, "sql must not be null");
        connect();
        var plan = plan(sql, false);
        try (var statement = connection.unwrap(RelRunner.class)
                .prepareStatement(plan);
                var resultSet = statement.executeQuery()) {
            return readRows(resultSet);
        } catch (SQLException e) {
            throw translate(e);
        }
    }

    /**
     * Returns the plan Calcite runs for a query, as a tree of relational
     * operators, which shows what reaches each backend: for example a
     * {@code MongoFilter} below a {@code MongoToEnumerableConverter} is a
     * filter MongoDB evaluates, and a {@code BindableTableScan} reads a source,
     * with the page or count the source answers named after the table. Meant
     * for looking at what a backend gets to do.
     *
     * @param sql
     *            the query, not {@code null}
     * @return the plan, one operator per line
     * @throws ToolException
     *             if the query does not parse or validate
     */
    public synchronized String explain(String sql) {
        Objects.requireNonNull(sql, "sql must not be null");
        connect();
        return RelOptUtil.toString(plan(sql, true));
    }

    /**
     * Parses, validates and converts the query over the tables, optimised into
     * the physical plan on request. Planning goes through the Frameworks API
     * rather than the connection, because the conversion has to keep an
     * {@code ORDER BY} inside a subquery: SQL does not promise that order and
     * Calcite drops it by default, but the grid sorts its rows exactly that
     * way.
     */
    private RelNode plan(String sql, boolean optimize) {
        var config = Frameworks.newConfigBuilder().parserConfig(parserConfig())
                .defaultSchema(schema).programs(Programs.standard())
                .typeSystem(TYPE_SYSTEM)
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
            var plan = planner.rel(validated).project().accept(MERGE_SORTS)
                    .accept(SourcePushdown.SHUTTLE).accept(BINDABLE_SCANS);
            if (!optimize) {
                return plan;
            }
            return planner.transform(0, planner.getEmptyTraitSet()
                    .replace(EnumerableConvention.INSTANCE), plan);
        } catch (SqlParseException e) {
            throw new ToolException(parseMessage(e), e);
        } catch (ValidationException e) {
            throw new ToolException(validationMessage(e), e);
        } catch (RelConversionException e) {
            LOGGER.warn("Query could not be converted", e);
            throw new IllegalStateException("Query failed", e);
        }
    }

    private static SqlParser.Config parserConfig() {
        return SqlParser.config().withLex(Lex.MYSQL_ANSI);
    }

    /**
     * Calcite's default types, except that a UNION or CASE of string literals
     * of different lengths has a VARCHAR type. By default Calcite pads them to
     * a common CHAR length, so {@code 'Rent'} next to {@code 'Marketing'}
     * becomes {@code 'Rent     '}, no longer equals the same value read from a
     * table, and a join on it silently drops the row.
     */
    private static final RelDataTypeSystem TYPE_SYSTEM = new RelDataTypeSystemImpl() {
        @Override
        public boolean shouldConvertRaggedUnionTypesToVarying() {
            return true;
        }
    };

    /**
     * Opens the connection and registers every table on the first call. The
     * connection and the adapters' schemas live as long as the provider.
     */
    private void connect() {
        if (connection != null) {
            return;
        }
        var properties = new Properties();
        properties.setProperty("lex", Lex.MYSQL_ANSI.name());
        CalciteConnection calcite;
        try {
            calcite = new Driver().connect("jdbc:calcite:", properties)
                    .unwrap(CalciteConnection.class);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not connect", e);
        }
        try {
            var root = calcite.getRootSchema();
            var federated = root.add(SCHEMA_NAME, new AbstractSchema());
            var tableDescriptions = new LinkedHashMap<String, String>();
            for (var source : sources) {
                federated.add(source.getName(),
                        new SourceTable(source,
                                pushdown -> fetch(source, pushdown),
                                pushdown -> count(source, pushdown)));
                tableDescriptions.put(source.getName(),
                        source.getDescription());
            }
            for (var adapterTable : adapterTables) {
                federated.add(adapterTable.getName(),
                        adapterTable.createTable(root));
                tableDescriptions.put(adapterTable.getName(),
                        adapterTable.getDescription());
            }
            calcite.setSchema(SCHEMA_NAME);
            schema = federated;
            descriptions = tableDescriptions;
            connection = calcite;
        } catch (SQLException | RuntimeException e) {
            try {
                calcite.close();
            } catch (SQLException closeException) {
                e.addSuppressed(closeException);
            }
            throw new IllegalStateException("Could not register the tables", e);
        }
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
     * {@link ToolException} a source threw, or a fixed hint when an adapter
     * could not implement its part of the plan, which happens when a query
     * applies a function the adapter cannot push to its backend. Anything else
     * stays a generic failure.
     */
    private static RuntimeException translate(SQLException exception) {
        for (Throwable cause = exception; cause != null; cause = cause
                .getCause()) {
            if (cause instanceof ToolException toolException) {
                return new ToolException(toolException.getMessage(), exception);
            }
            if (cause.getMessage() != null
                    && cause.getMessage().startsWith("Unable to implement")) {
                return new ToolException(
                        "A data source cannot evaluate an expression of the "
                                + "query. On its columns use only comparisons, "
                                + "arithmetic, CASE, CONCAT and SUBSTRING, and "
                                + "do other work on the result.",
                        exception);
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

    private List<Map<String, Object>> fetch(TableSource source,
            Pushdown pushdown) {
        return read(source, "rows", pushdown,
                () -> List.copyOf(source.fetch(pushdown)));
    }

    private long count(TableSource source, Pushdown pushdown) {
        return read(source, "count", pushdown, () -> source.count(pushdown));
    }

    /**
     * Reads from a source through the cache, so that the same read with the
     * same pushdown within the cache duration reaches the backend once.
     */
    @SuppressWarnings("unchecked")
    private <T> T read(TableSource source, String read, Pushdown pushdown,
            Supplier<T> reader) {
        if (cacheDuration.isZero()) {
            return reader.get();
        }
        var key = new CacheKey(source.getName(), read, pushdown);
        var now = System.nanoTime();
        var entry = cache().get(key);
        if (entry != null && now - entry.expiresAt() < 0) {
            LOGGER.info("Cache hit for {} of {} with {}, read skipped", read,
                    source.getName(), pushdown);
            return (T) entry.value();
        }
        var value = reader.get();
        cache().values().removeIf(other -> now - other.expiresAt() >= 0);
        cache().put(key, new CacheEntry(value, now + cacheDuration.toNanos()));
        return value;
    }

    private Map<CacheKey, CacheEntry> cache() {
        if (cache == null) {
            cache = new ConcurrentHashMap<>();
        }
        return cache;
    }
}
