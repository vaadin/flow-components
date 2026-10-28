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

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.apache.calcite.DataContext;
import org.apache.calcite.config.Lex;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.Driver;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rel.type.RelDataTypeSystemImpl;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.rex.RexUtil;
import org.apache.calcite.runtime.CalciteContextException;
import org.apache.calcite.schema.FilterableTable;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql.validate.SqlValidatorException;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.RelConversionException;
import org.apache.calcite.tools.RelRunner;
import org.apache.calcite.tools.ValidationException;
import org.apache.calcite.util.DateString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.ToolException;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;

/**
 * A {@link DatabaseProvider} over the sample REST API written against Apache
 * Calcite directly, without {@code FederatedDatabaseProvider} or
 * {@code TableSource}, to see what an application has to write on its own. It
 * does what the {@code rest} provider does: one table per collection with types
 * inferred from the first page, equality filters pushed down as query
 * parameters, every page fetched, rows cached for a short while, the grid's
 * paging wrappers honoured, {@code SELECT} only, and errors relayed to the LLM
 * when they are safe.
 */
public class RestCalciteDatabaseProvider implements DatabaseProvider {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RestCalciteDatabaseProvider.class);

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private static final int PAGE_SIZE = 100;

    private static final long CACHE_NANOS = 10_000_000_000L;

    private static final String SCHEMA_NAME = "rest";

    private static final String DIALECT_NOTES = """
            - SQL dialect: Apache Calcite, standard SQL. JOIN, GROUP BY, subqueries, LIMIT and OFFSET are supported
            - Identifiers are case-insensitive. A column name shown in double quotes is a reserved word and must be written with the quotes
            - Compare DATE columns with DATE literals, for example tradeDate >= DATE '2024-01-01'
            - Only SELECT statements are accepted
            """;

    /** A column of a collection, typed by the values of its first page. */
    private record Column(String name,
            Class<?> type) implements java.io.Serializable {
    }

    private record CacheEntry(List<Map<String, Object>> rows, long expiresAt) {
    }

    private final String api;
    private final String notes;
    private final Map<String, List<Column>> collections;

    private transient CalciteConnection connection;
    private transient SchemaPlus schema;
    private transient Map<String, CacheEntry> cache;

    /**
     * Creates a provider over the API, reading its index and the first page of
     * every collection.
     *
     * @param api
     *            the API root, whose index lists the collections
     * @param notes
     *            notes appended to the schema text, or {@code null}
     */
    public RestCalciteDatabaseProvider(URI api, String notes) {
        this.api = api.toString();
        this.notes = notes;
        this.collections = new LinkedHashMap<>();
        for (var collection : getJson(api, null).get("collections")
                .properties()) {
            var page = getJson(collectionUri(collection.getKey()),
                    "size=" + PAGE_SIZE);
            collections.put(collection.getKey(),
                    inferColumns(page.get("content")));
        }
    }

    // --- The DatabaseProvider contract ---

    @Override
    public synchronized String getSchema() {
        var metadata = SqlParser.create(new StringReader(""), parserConfig())
                .getMetadata();
        var text = new StringBuilder("DATABASE SCHEMA:\n\n");
        for (var collection : collections.entrySet()) {
            text.append("TABLE ").append(collection.getKey())
                    .append("  (REST collection /api/")
                    .append(collection.getKey()).append(")\n");
            for (var column : collection.getValue()) {
                var name = column.name();
                if (metadata.isReservedWord(name.toUpperCase(Locale.ROOT))) {
                    name = '"' + name + '"';
                }
                text.append("  ").append(name).append(' ')
                        .append(sqlType(column.type()).getName()).append('\n');
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
        // Planning goes through the Frameworks API, not the connection: the
        // conversion must keep an ORDER BY inside a subquery, which Calcite
        // drops by default and which the grid's sort wrapper relies on
        var config = Frameworks.newConfigBuilder().parserConfig(parserConfig())
                .defaultSchema(schema).typeSystem(TYPE_SYSTEM)
                .sqlToRelConverterConfig(
                        SqlToRelConverter.config().withTrimUnusedFields(true)
                                .withRemoveSortInSubQuery(false))
                .build();
        try (var planner = Frameworks.getPlanner(config)) {
            var statement = planner.parse(sql);
            if (!statement.getKind().belongsTo(SqlKind.QUERY)) {
                throw new ToolException("Only SELECT queries are allowed.");
            }
            var plan = planner.rel(planner.validate(statement)).project();
            try (var prepared = connection.unwrap(RelRunner.class)
                    .prepareStatement(plan);
                    var resultSet = prepared.executeQuery()) {
                return readRows(resultSet);
            }
        } catch (SqlParseException e) {
            throw new ToolException(parseMessage(e), e);
        } catch (ValidationException e) {
            throw new ToolException(validationMessage(e), e);
        } catch (RelConversionException e) {
            LOGGER.warn("Query could not be converted", e);
            throw new IllegalStateException("Query failed", e);
        } catch (SQLException e) {
            throw translate(e);
        }
    }

    // --- Calcite wiring ---

    private static SqlParser.Config parserConfig() {
        return SqlParser.config().withLex(Lex.MYSQL_ANSI);
    }

    // A UNION or CASE of string literals of different lengths gets a VARCHAR
    // type; by default Calcite pads them to one CHAR length, and a join on a
    // padded value silently drops rows
    private static final RelDataTypeSystem TYPE_SYSTEM = new RelDataTypeSystemImpl() {
        @Override
        public boolean shouldConvertRaggedUnionTypesToVarying() {
            return true;
        }
    };

    private void connect() {
        if (connection != null) {
            return;
        }
        var properties = new Properties();
        properties.setProperty("lex", Lex.MYSQL_ANSI.name());
        try {
            connection = new Driver().connect("jdbc:calcite:", properties)
                    .unwrap(CalciteConnection.class);
            schema = connection.getRootSchema().add(SCHEMA_NAME,
                    new AbstractSchema());
            for (var collection : collections.entrySet()) {
                schema.add(collection.getKey(), new CollectionTable(
                        collection.getKey(), collection.getValue()));
            }
            connection.setSchema(SCHEMA_NAME);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not connect", e);
        }
    }

    /**
     * One collection as a Calcite table that takes equality filters. Calcite
     * applies the filters again to the rows it gets, so pushing them is an
     * optimisation only.
     */
    private final class CollectionTable extends AbstractTable
            implements FilterableTable {

        private final String name;
        private final List<Column> columns;

        private CollectionTable(String name, List<Column> columns) {
            this.name = name;
            this.columns = columns;
        }

        @Override
        public RelDataType getRowType(RelDataTypeFactory typeFactory) {
            var builder = typeFactory.builder();
            for (var column : columns) {
                var sqlType = sqlType(column.type());
                var type = sqlType == SqlTypeName.DECIMAL
                        ? typeFactory.createSqlType(sqlType, 19, 4)
                        : typeFactory.createSqlType(sqlType);
                builder.add(column.name(),
                        typeFactory.createTypeWithNullability(type, true));
            }
            return builder.build();
        }

        @Override
        public Enumerable<Object[]> scan(DataContext root,
                List<RexNode> filters) {
            var query = new StringBuilder();
            for (var filter : takeEqualityFilters(root, filters).entrySet()) {
                query.append('&').append(encode(filter.getKey())).append('=')
                        .append(encode(filter.getValue()));
            }
            var rows = cachedRows(name, query.toString());
            var result = new ArrayList<Object[]>(rows.size());
            for (var row : rows) {
                var values = new Object[columns.size()];
                for (int i = 0; i < values.length; i++) {
                    var column = columns.get(i);
                    values[i] = toInternal(row.get(column.name()),
                            column.type());
                }
                result.add(values);
            }
            return Linq4j.asEnumerable(result);
        }

        /**
         * Removes the filters of the form {@code column = literal} from the
         * list and returns them as query parameter values. Calcite folds a
         * comparison into a {@code SEARCH} call and joins several into one
         * {@code AND}, so both are taken apart first; a filter stays in the
         * list unless every part of it was taken, because Calcite checks that
         * what remains are its own nodes.
         */
        private Map<String, String> takeEqualityFilters(DataContext root,
                List<RexNode> filters) {
            var result = new LinkedHashMap<String, String>();
            var rexBuilder = new RexBuilder(root.getTypeFactory());
            var iterator = filters.iterator();
            while (iterator.hasNext()) {
                var expanded = RexUtil.expandSearch(rexBuilder, null,
                        iterator.next());
                var handled = true;
                for (var conjunct : RelOptUtil.conjunctions(expanded)) {
                    var equality = equality(conjunct);
                    if (equality == null
                            || result.containsKey(equality.getKey())) {
                        handled = false;
                    } else {
                        result.put(equality.getKey(), equality.getValue());
                    }
                }
                if (handled) {
                    iterator.remove();
                }
            }
            return result;
        }

        private Map.Entry<String, String> equality(RexNode node) {
            if (!(node instanceof RexCall call)
                    || call.getKind() != SqlKind.EQUALS) {
                return null;
            }
            var left = call.getOperands().get(0);
            var right = call.getOperands().get(1);
            RexInputRef ref;
            RexLiteral literal;
            if (left instanceof RexInputRef r
                    && right instanceof RexLiteral l) {
                ref = r;
                literal = l;
            } else if (right instanceof RexInputRef r
                    && left instanceof RexLiteral l) {
                ref = r;
                literal = l;
            } else {
                return null;
            }
            var value = literalText(literal);
            return value == null ? null
                    : Map.entry(columns.get(ref.getIndex()).name(), value);
        }
    }

    /**
     * Renders a literal the way the API compares it: as the text of the JSON
     * value.
     */
    private static String literalText(RexLiteral literal) {
        if (literal.isNull()) {
            return null;
        }
        return switch (literal.getTypeName()) {
        case CHAR, VARCHAR -> literal.getValueAs(String.class);
        case BOOLEAN -> String.valueOf(literal.getValueAs(Boolean.class));
        case DATE -> String.valueOf(literal.getValueAs(DateString.class));
        case DECIMAL, INTEGER, BIGINT, SMALLINT, TINYINT, DOUBLE, FLOAT, REAL ->
            literal.getValueAs(BigDecimal.class).stripTrailingZeros()
                    .toPlainString();
        default -> null;
        };
    }

    private static SqlTypeName sqlType(Class<?> type) {
        if (type == Integer.class) {
            return SqlTypeName.INTEGER;
        } else if (type == Long.class) {
            return SqlTypeName.BIGINT;
        } else if (type == BigDecimal.class) {
            return SqlTypeName.DECIMAL;
        } else if (type == Boolean.class) {
            return SqlTypeName.BOOLEAN;
        } else if (type == LocalDate.class) {
            return SqlTypeName.DATE;
        } else if (type == LocalDateTime.class) {
            return SqlTypeName.TIMESTAMP;
        }
        return SqlTypeName.VARCHAR;
    }

    /**
     * Converts a value to Calcite's internal representation of its SQL type:
     * dates are days since the epoch, timestamps milliseconds.
     */
    private static Object toInternal(Object value, Class<?> type) {
        if (value == null) {
            return null;
        } else if (type == LocalDate.class) {
            return (int) ((LocalDate) value).toEpochDay();
        } else if (type == LocalDateTime.class) {
            return ((LocalDateTime) value).toInstant(ZoneOffset.UTC)
                    .toEpochMilli();
        }
        return value;
    }

    private static List<Map<String, Object>> readRows(ResultSet resultSet)
            throws SQLException {
        var metaData = resultSet.getMetaData();
        var rows = new ArrayList<Map<String, Object>>();
        while (resultSet.next()) {
            var row = new LinkedHashMap<String, Object>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                var value = resultSet.getObject(i);
                if (value instanceof Date date) {
                    value = date.toLocalDate();
                } else if (value instanceof Timestamp timestamp) {
                    value = timestamp.toLocalDateTime();
                }
                row.put(metaData.getColumnLabel(i), value);
            }
            rows.add(row);
        }
        return rows;
    }

    // --- Errors the LLM may see ---

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

    // --- The REST API ---

    private URI collectionUri(String collection) {
        return URI.create(api + "/" + collection);
    }

    /**
     * Returns the rows of a collection with the given filter parameters,
     * fetched once and reused for ten seconds: a grid runs the same query for
     * its columns, its count and each page.
     */
    private synchronized List<Map<String, Object>> cachedRows(String collection,
            String filters) {
        if (cache == null) {
            cache = new LinkedHashMap<>();
        }
        var key = collection + "?" + filters;
        var now = System.nanoTime();
        var entry = cache.get(key);
        if (entry != null && now - entry.expiresAt() < 0) {
            LOGGER.info("Cache hit for {}?size={}{}: {} items, fetch skipped",
                    collectionUri(collection), PAGE_SIZE, filters,
                    entry.rows().size());
            return entry.rows();
        }
        var rows = fetchRows(collection, filters);
        cache.values().removeIf(other -> now - other.expiresAt() >= 0);
        cache.put(key, new CacheEntry(rows, now + CACHE_NANOS));
        return rows;
    }

    private List<Map<String, Object>> fetchRows(String collection,
            String filters) {
        var columns = collections.get(collection);
        var rows = new ArrayList<Map<String, Object>>();
        var pageNumber = 0;
        while (true) {
            var page = getJson(collectionUri(collection),
                    "size=" + PAGE_SIZE + "&page=" + pageNumber + filters);
            for (var item : page.get("content")) {
                var row = new LinkedHashMap<String, Object>();
                for (var column : columns) {
                    row.put(column.name(),
                            toValue(item.get(column.name()), column.type()));
                }
                rows.add(row);
            }
            if (++pageNumber >= page.get("page").get("totalPages").intValue()) {
                return rows;
            }
        }
    }

    private static Object toValue(JsonNode node, Class<?> type) {
        if (node == null || node.isNull()) {
            return null;
        } else if (type == Integer.class) {
            return node.intValue();
        } else if (type == Long.class) {
            return node.longValue();
        } else if (type == BigDecimal.class) {
            return node.decimalValue();
        } else if (type == Boolean.class) {
            return node.booleanValue();
        } else if (type == LocalDate.class) {
            return LocalDate.parse(node.stringValue());
        } else if (type == LocalDateTime.class) {
            return LocalDateTime.parse(node.stringValue());
        }
        return node.isString() ? node.stringValue() : node.toString();
    }

    /**
     * Infers the columns from the items of a page: the keys of the first item
     * in order, each typed by the values every item has for it.
     */
    private static List<Column> inferColumns(JsonNode items) {
        var columns = new ArrayList<Column>();
        if (items == null || items.isEmpty()) {
            return columns;
        }
        for (var property : items.get(0).properties()) {
            var values = new ArrayList<JsonNode>();
            for (var item : items) {
                var value = item.get(property.getKey());
                if (value != null && !value.isNull()) {
                    values.add(value);
                }
            }
            columns.add(new Column(property.getKey(), inferType(values)));
        }
        return columns;
    }

    private static Class<?> inferType(List<JsonNode> values) {
        if (values.isEmpty()) {
            return String.class;
        }
        if (values.stream().allMatch(JsonNode::isBoolean)) {
            return Boolean.class;
        }
        if (values.stream().allMatch(JsonNode::isNumber)) {
            if (values.stream().allMatch(JsonNode::isIntegralNumber)) {
                return values.stream().allMatch(JsonNode::canConvertToInt)
                        ? Integer.class
                        : Long.class;
            }
            return BigDecimal.class;
        }
        if (values.stream().allMatch(JsonNode::isString)) {
            if (values.stream().allMatch(
                    value -> parses(value.stringValue(), LocalDate.class))) {
                return LocalDate.class;
            }
            if (values.stream().allMatch(value -> parses(value.stringValue(),
                    LocalDateTime.class))) {
                return LocalDateTime.class;
            }
        }
        return String.class;
    }

    private static boolean parses(String text, Class<?> type) {
        try {
            if (type == LocalDate.class) {
                LocalDate.parse(text);
            } else {
                LocalDateTime.parse(text);
            }
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private static String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    /**
     * Gets a JSON document. A {@code 400} carries the API's own description of
     * what was wrong with the request, which names only fields, so it is
     * relayed to the LLM.
     */
    private static JsonNode getJson(URI uri, String query) {
        var target = query == null ? uri : URI.create(uri + "?" + query);
        try {
            var response = HTTP.send(
                    HttpRequest.newBuilder(target).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            var body = JacksonUtils.readTree(response.body());
            if (response.statusCode() == 400) {
                throw new ToolException(Objects.requireNonNullElse(
                        body.path("error").stringValue(), "Bad request"));
            }
            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "GET " + target + " returned " + response.statusCode());
            }
            return body;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted", e);
        }
    }
}
