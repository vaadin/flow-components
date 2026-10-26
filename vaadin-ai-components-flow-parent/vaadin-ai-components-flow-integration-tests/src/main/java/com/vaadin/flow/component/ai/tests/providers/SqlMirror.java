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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import javax.sql.DataSource;

import com.vaadin.flow.component.ai.tests.endpoints.SampleData;
import com.vaadin.flow.component.ai.tests.endpoints.SampleH2Database;

/**
 * An in-memory H2 database filled from rows that came from somewhere else: a
 * REST API, a document store, CSV files. The LLM then queries the mirror with
 * SQL. Column types are inferred from the values, which is where the guesswork
 * of such mirrors lies: JSON carries dates as strings, documents carry decimals
 * as doubles, and CSV carries everything as text.
 * <p>
 * Column names are derived from the row keys: camelCase and dotted paths become
 * upper snake_case ({@code monthOrder} and {@code prices.open} become
 * {@code MONTH_ORDER} and {@code PRICES_OPEN}), the form H2 gives unquoted
 * identifiers, so the LLM does not need to quote them.
 * </p>
 */
public final class SqlMirror {

    private static final Pattern INTEGER = Pattern.compile("-?\\d{1,18}");
    private static final Pattern DECIMAL = Pattern
            .compile("-?\\d{1,15}\\.\\d{1,10}");

    private final String url;
    private final boolean parseNumbers;
    private final List<String> tables = new ArrayList<>();

    private SqlMirror(String url, boolean parseNumbers) {
        this.url = url;
        this.parseNumbers = parseNumbers;
    }

    /**
     * Creates an empty mirror database.
     *
     * @param name
     *            the name of the in-memory database, unique per mirror
     * @param parseNumbers
     *            {@code true} to type string values that look like numbers or
     *            booleans as such, which text sources like CSV need;
     *            {@code false} to keep them as text. Strings that look like ISO
     *            dates are typed as dates either way.
     * @return the mirror
     */
    public static SqlMirror create(String name, boolean parseNumbers) {
        var mirror = new SqlMirror("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1",
                parseNumbers);
        try (var connection = mirror.connect();
                var statement = connection.createStatement()) {
            statement.execute("DROP ALL OBJECTS");
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to create mirror database " + name, e);
        }
        return mirror;
    }

    /**
     * Creates a table from the given rows and inserts them. The columns are the
     * union of the row keys, in order of first appearance.
     *
     * @param tableName
     *            the table name, converted like a column name
     * @param rows
     *            the rows to load
     */
    public void load(String tableName, List<Map<String, Object>> rows) {
        var table = toSqlName(tableName);
        var columns = new LinkedHashMap<String, ColumnType>();
        var converted = new ArrayList<Map<String, Object>>(rows.size());
        for (var row : rows) {
            var convertedRow = new LinkedHashMap<String, Object>();
            for (var entry : row.entrySet()) {
                var column = toSqlName(entry.getKey());
                var value = convert(entry.getValue());
                convertedRow.put(column, value);
                columns.merge(column, ColumnType.of(value), ColumnType::merge);
            }
            converted.add(convertedRow);
        }
        var definitions = new ArrayList<String>();
        columns.forEach((column, type) -> definitions
                .add("\"" + column + "\" " + type.sqlType()));
        var columnList = String.join(", ",
                columns.keySet().stream().map(c -> "\"" + c + "\"").toList());
        var placeholders = String.join(", ",
                columns.keySet().stream().map(c -> "?").toList());
        try (var connection = connect()) {
            try (var statement = connection.createStatement()) {
                statement.execute("CREATE TABLE \"" + table + "\" ("
                        + String.join(", ", definitions) + ")");
            }
            try (var statement = connection
                    .prepareStatement("INSERT INTO \"" + table + "\" ("
                            + columnList + ") VALUES (" + placeholders + ")")) {
                for (var row : converted) {
                    int index = 1;
                    for (var column : columns.keySet()) {
                        statement.setObject(index++, row.get(column));
                    }
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to load mirror table " + table, e);
        }
        tables.add(table);
    }

    /**
     * Returns a data source for the mirror database.
     *
     * @return the data source, connecting as the admin user
     */
    public DataSource dataSource() {
        return SampleH2Database.dataSource(url, SampleH2Database.ADMIN_USER,
                SampleH2Database.ADMIN_PASSWORD);
    }

    /**
     * Returns the names of the loaded tables.
     *
     * @return the table names, in load order
     */
    public List<String> tables() {
        return List.copyOf(tables);
    }

    /**
     * Converts a camelCase or dotted key to the upper snake_case form H2 gives
     * unquoted identifiers. A leading underscore, as in {@code _id}, is
     * dropped.
     *
     * @param key
     *            the key to convert
     * @return the SQL name
     */
    public static String toSqlName(String key) {
        var flat = key.replace('.', '_');
        // A name without lower-case letters is already snake_case (or a
        // database upper-cased it) and must not get an underscore per letter
        var name = flat.equals(flat.toUpperCase(Locale.ROOT)) ? flat
                : SampleData.toSnakeCase(flat);
        if (name.startsWith("_")) {
            name = name.substring(1);
        }
        return name.toUpperCase(Locale.ROOT);
    }

    private Connection connect() throws SQLException {
        return dataSource().getConnection();
    }

    private Object convert(Object value) {
        if (value instanceof String string) {
            return convertString(string);
        }
        if (value instanceof Date date) {
            var dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(date.getTime()), ZoneOffset.UTC);
            return dateTime.toLocalTime().equals(LocalTime.MIDNIGHT)
                    ? dateTime.toLocalDate()
                    : dateTime;
        }
        if (value instanceof Instant instant) {
            return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        }
        if (value instanceof Double || value instanceof Float) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        if (value instanceof Map || value instanceof List) {
            return value.toString();
        }
        return value;
    }

    private Object convertString(String string) {
        if (string.isEmpty()) {
            return null;
        }
        if (string.length() == 10 && string.charAt(4) == '-') {
            try {
                return LocalDate.parse(string);
            } catch (DateTimeParseException e) {
                // Not a date after all, keep the text
            }
        }
        if (string.length() >= 19 && string.charAt(10) == 'T') {
            try {
                return LocalDateTime.parse(string);
            } catch (DateTimeParseException e) {
                // Not a timestamp after all, keep the text
            }
        }
        if (parseNumbers) {
            if (INTEGER.matcher(string).matches()) {
                return Long.parseLong(string);
            }
            if (DECIMAL.matcher(string).matches()) {
                return new BigDecimal(string);
            }
            if (string.equalsIgnoreCase("true")
                    || string.equalsIgnoreCase("false")) {
                return Boolean.parseBoolean(string);
            }
        }
        return string;
    }

    /** The inferred type of a column, widened as values are seen. */
    private record ColumnType(Kind kind, int length) {

        private enum Kind {
            UNKNOWN, BOOLEAN, INTEGER, DECIMAL, DATE, TIMESTAMP, TEXT
        }

        static ColumnType of(Object value) {
            return switch (value) {
            case null -> new ColumnType(Kind.UNKNOWN, 0);
            case Boolean b -> new ColumnType(Kind.BOOLEAN, 0);
            case Integer i -> new ColumnType(Kind.INTEGER, 0);
            case Long l -> new ColumnType(Kind.INTEGER, 0);
            case Short s -> new ColumnType(Kind.INTEGER, 0);
            case Byte b -> new ColumnType(Kind.INTEGER, 0);
            case BigInteger b -> new ColumnType(Kind.INTEGER, 0);
            case BigDecimal d -> new ColumnType(Kind.DECIMAL, 0);
            case LocalDate d -> new ColumnType(Kind.DATE, 0);
            case LocalDateTime t -> new ColumnType(Kind.TIMESTAMP, 0);
            case Timestamp t -> new ColumnType(Kind.TIMESTAMP, 0);
            default -> new ColumnType(Kind.TEXT, value.toString().length());
            };
        }

        ColumnType merge(ColumnType other) {
            if (kind == other.kind || other.kind == Kind.UNKNOWN) {
                return new ColumnType(kind, Math.max(length, other.length));
            }
            if (kind == Kind.UNKNOWN) {
                return other;
            }
            if (isNumeric() && other.isNumeric()) {
                return new ColumnType(Kind.DECIMAL, 0);
            }
            if (isTemporal() && other.isTemporal()) {
                return new ColumnType(Kind.TIMESTAMP, 0);
            }
            return new ColumnType(Kind.TEXT, Math.max(length, other.length));
        }

        private boolean isNumeric() {
            return kind == Kind.INTEGER || kind == Kind.DECIMAL;
        }

        private boolean isTemporal() {
            return kind == Kind.DATE || kind == Kind.TIMESTAMP;
        }

        String sqlType() {
            return switch (kind) {
            case BOOLEAN -> "BOOLEAN";
            case INTEGER -> "BIGINT";
            case DECIMAL -> "DECIMAL(19, 4)";
            case DATE -> "DATE";
            case TIMESTAMP -> "TIMESTAMP";
            case TEXT, UNKNOWN -> "VARCHAR(" + Math.max(255, length) + ")";
            };
        }
    }
}
