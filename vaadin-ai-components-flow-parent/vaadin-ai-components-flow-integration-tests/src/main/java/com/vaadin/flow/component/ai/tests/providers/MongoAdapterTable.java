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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.calcite.adapter.mongodb.MongoSchemaFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.ViewTable;
import org.bson.Document;
import org.bson.types.Decimal128;

import com.mongodb.client.MongoClients;
import com.vaadin.flow.component.ai.provider.AdapterTable;
import com.vaadin.flow.component.ai.provider.TableSource.Column;

/**
 * An {@link AdapterTable} over one MongoDB collection, read by Apache Calcite's
 * own MongoDB adapter, which pushes filters, projections, sorts, limits and
 * aggregates into the database as pipeline stages.
 * <p>
 * The adapter presents a collection as a single {@code _MAP} column, so this
 * table is a view over it that casts each top-level field to a typed column,
 * the way Calcite's documentation shows. The types are inferred from a sample
 * of documents. Nested documents and arrays are left out: the adapter cannot
 * translate an access into them. The {@code _id} field is exposed as
 * {@code id}.
 * </p>
 */
public class MongoAdapterTable implements AdapterTable {

    private static final String MONGO_SCHEMA = "mongo";

    private static final int SAMPLE_SIZE = 100;

    private final String connectionString;
    private final String database;
    private final String collection;
    private final List<Column> columns;

    /**
     * Creates a table over a collection, sampling its documents to infer the
     * columns.
     *
     * @param connectionString
     *            the MongoDB connection string
     * @param database
     *            the database name
     * @param collection
     *            the collection name
     */
    public MongoAdapterTable(String connectionString, String database,
            String collection) {
        this.connectionString = connectionString;
        this.database = database;
        this.collection = collection;
        this.columns = inferColumns();
    }

    /**
     * Creates a table for every collection of a database.
     *
     * @param connectionString
     *            the MongoDB connection string
     * @param database
     *            the database name
     * @return the tables, in collection name order
     */
    public static List<MongoAdapterTable> discover(String connectionString,
            String database) {
        var names = new ArrayList<String>();
        try (var client = MongoClients.create(connectionString)) {
            client.getDatabase(database).listCollectionNames().into(names);
        }
        Collections.sort(names);
        return names.stream().map(
                name -> new MongoAdapterTable(connectionString, database, name))
                .toList();
    }

    @Override
    public String getName() {
        return collection;
    }

    @Override
    public String getDescription() {
        return "MongoDB collection " + collection;
    }

    /**
     * Returns the columns of the view, with their Java types.
     *
     * @return the columns
     */
    public List<Column> getColumns() {
        return columns;
    }

    @Override
    public Table createTable(SchemaPlus rootSchema) {
        if (rootSchema.subSchemas().get(MONGO_SCHEMA) == null) {
            rootSchema.add(MONGO_SCHEMA, new MongoSchemaFactory().create(
                    rootSchema, MONGO_SCHEMA,
                    Map.of("host", connectionString, "database", database)));
        }
        return ViewTable
                .viewMacro(rootSchema, viewSql(), List.of(), null, false)
                .apply(List.of());
    }

    /**
     * Returns the SQL of the view: one cast per column over the adapter's
     * {@code _MAP} column.
     *
     * @return the view SQL
     */
    String viewSql() {
        var casts = columns.stream()
                .map(column -> "CAST(\"_MAP\"['"
                        + fieldOf(column).replace("'", "''") + "'] AS "
                        + sqlType(column.type()) + ") AS \""
                        + column.name().replace("\"", "\"\"") + "\"")
                .collect(Collectors.joining(", "));
        return "SELECT " + casts + " FROM \"" + MONGO_SCHEMA + "\".\""
                + collection.replace("\"", "\"\"") + "\"";
    }

    private static String fieldOf(Column column) {
        return column.name().equals("id") ? "_id" : column.name();
    }

    private static String sqlType(Class<?> type) {
        if (type == Integer.class) {
            return "INTEGER";
        } else if (type == Long.class) {
            return "BIGINT";
        } else if (type == Double.class) {
            return "DOUBLE";
        } else if (type == BigDecimal.class) {
            return "DECIMAL(19, 4)";
        } else if (type == Boolean.class) {
            return "BOOLEAN";
        } else if (type == LocalDate.class) {
            return "DATE";
        } else if (type == LocalDateTime.class) {
            return "TIMESTAMP";
        }
        return "VARCHAR";
    }

    /**
     * Samples the collection and types each top-level field by the values it
     * holds; fields holding documents or arrays are left out, and fields with
     * mixed types become text.
     */
    private List<Column> inferColumns() {
        var types = new LinkedHashMap<String, Class<?>>();
        try (var client = MongoClients.create(connectionString)) {
            var documents = client.getDatabase(database)
                    .getCollection(collection).find().limit(SAMPLE_SIZE);
            for (var document : documents) {
                for (var entry : document.entrySet()) {
                    var type = typeOf(entry.getValue());
                    if (type == null) {
                        continue;
                    }
                    types.merge(entry.getKey(), type, MongoAdapterTable::merge);
                }
            }
        }
        var columns = new ArrayList<Column>();
        for (var entry : types.entrySet()) {
            var name = entry.getKey().equals("_id") ? "id" : entry.getKey();
            columns.add(new Column(name, entry.getValue()));
        }
        return columns;
    }

    private static Class<?> typeOf(Object value) {
        if (value == null || value instanceof Document
                || value instanceof List) {
            return null;
        } else if (value instanceof Integer) {
            return Integer.class;
        } else if (value instanceof Long) {
            return Long.class;
        } else if (value instanceof Double) {
            return Double.class;
        } else if (value instanceof Decimal128) {
            return BigDecimal.class;
        } else if (value instanceof Boolean) {
            return Boolean.class;
        } else if (value instanceof Date date) {
            return date.toInstant().atOffset(ZoneOffset.UTC).toLocalTime()
                    .toSecondOfDay() == 0 ? LocalDate.class
                            : LocalDateTime.class;
        }
        return String.class;
    }

    private static Class<?> merge(Class<?> a, Class<?> b) {
        if (a == b) {
            return a;
        }
        if (isNumber(a) && isNumber(b)) {
            if (a == BigDecimal.class || b == BigDecimal.class) {
                return BigDecimal.class;
            } else if (a == Double.class || b == Double.class) {
                return Double.class;
            }
            return Long.class;
        }
        if ((a == LocalDate.class || a == LocalDateTime.class)
                && (b == LocalDate.class || b == LocalDateTime.class)) {
            return LocalDateTime.class;
        }
        return String.class;
    }

    private static boolean isNumber(Class<?> type) {
        return type == Integer.class || type == Long.class
                || type == Double.class || type == BigDecimal.class;
    }
}
