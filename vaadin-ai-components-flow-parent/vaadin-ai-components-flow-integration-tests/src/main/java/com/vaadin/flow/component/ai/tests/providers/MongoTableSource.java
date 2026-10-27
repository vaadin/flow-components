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
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Filters;
import com.vaadin.flow.component.ai.provider.TableSource;

/**
 * A {@link TableSource} over one MongoDB collection, read with the MongoDB
 * driver. The column types are inferred from a sample of documents; nested
 * documents and arrays are left out, fields with mixed types become text, and
 * the {@code _id} field is exposed as {@code id}. Equality filters are pushed
 * down as a {@code find} filter; everything else the engine does.
 */
public class MongoTableSource implements TableSource {

    private static final int SAMPLE_SIZE = 100;

    private final String connectionString;
    private final String database;
    private final String collection;
    private final List<Column> columns;

    private transient MongoClient client;

    /**
     * Creates a source over a collection, sampling its documents to infer the
     * columns.
     *
     * @param connectionString
     *            the MongoDB connection string
     * @param database
     *            the database name
     * @param collection
     *            the collection name
     */
    public MongoTableSource(String connectionString, String database,
            String collection) {
        this.connectionString = connectionString;
        this.database = database;
        this.collection = collection;
        this.columns = inferColumns();
    }

    /**
     * Creates a source for every collection of a database.
     *
     * @param connectionString
     *            the MongoDB connection string
     * @param database
     *            the database name
     * @return the sources, in collection name order
     */
    public static List<MongoTableSource> discover(String connectionString,
            String database) {
        var names = new ArrayList<String>();
        try (var client = MongoClients.create(connectionString)) {
            client.getDatabase(database).listCollectionNames().into(names);
        }
        Collections.sort(names);
        return names.stream().map(
                name -> new MongoTableSource(connectionString, database, name))
                .toList();
    }

    @Override
    public String getName() {
        return collection;
    }

    @Override
    public List<Column> getColumns() {
        return columns;
    }

    @Override
    public Set<Capability> getCapabilities() {
        return Set.of(Capability.EQUALITY_FILTER);
    }

    @Override
    public String getDescription() {
        return "MongoDB collection " + collection;
    }

    @Override
    public List<Map<String, Object>> fetch(Pushdown pushdown) {
        var filters = new ArrayList<Bson>();
        for (var filter : pushdown.equalityFilters().entrySet()) {
            filters.add(Filters.eq(fieldOf(filter.getKey()),
                    toBson(filter.getValue())));
        }
        var documents = client().getDatabase(database).getCollection(collection)
                .find(filters.isEmpty() ? new Document()
                        : Filters.and(filters));
        var rows = new ArrayList<Map<String, Object>>();
        for (var document : documents) {
            var row = new LinkedHashMap<String, Object>();
            for (var column : columns) {
                row.put(column.name(), toValue(
                        document.get(fieldOf(column.name())), column.type()));
            }
            rows.add(row);
        }
        return rows;
    }

    private synchronized MongoClient client() {
        if (client == null) {
            client = MongoClients.create(connectionString);
        }
        return client;
    }

    private static String fieldOf(String column) {
        return column.equals("id") ? "_id" : column;
    }

    /**
     * Converts a filter value of a column's Java type to what the driver
     * compares with the stored BSON value.
     */
    private static Object toBson(Object value) {
        if (value instanceof LocalDate date) {
            return Date.from(date.atStartOfDay(ZoneOffset.UTC).toInstant());
        } else if (value instanceof LocalDateTime dateTime) {
            return Date.from(dateTime.toInstant(ZoneOffset.UTC));
        } else if (value instanceof BigDecimal decimal) {
            return new Decimal128(decimal);
        }
        return value;
    }

    private static Object toValue(Object value, Class<?> type) {
        if (value == null) {
            return null;
        }
        if (type == Integer.class) {
            return ((Number) value).intValue();
        } else if (type == Long.class) {
            return ((Number) value).longValue();
        } else if (type == Double.class) {
            return ((Number) value).doubleValue();
        } else if (type == BigDecimal.class) {
            return value instanceof Decimal128 decimal
                    ? decimal.bigDecimalValue()
                    : new BigDecimal(value.toString());
        } else if (type == Boolean.class) {
            return value;
        } else if (type == LocalDate.class) {
            return ((Date) value).toInstant().atOffset(ZoneOffset.UTC)
                    .toLocalDate();
        } else if (type == LocalDateTime.class) {
            return ((Date) value).toInstant().atOffset(ZoneOffset.UTC)
                    .toLocalDateTime();
        }
        return String.valueOf(value);
    }

    /**
     * Samples the collection and types each top-level field by the values it
     * holds.
     */
    private List<Column> inferColumns() {
        var types = new LinkedHashMap<String, Class<?>>();
        for (var document : client().getDatabase(database)
                .getCollection(collection).find().limit(SAMPLE_SIZE)) {
            for (var entry : document.entrySet()) {
                var type = typeOf(entry.getValue());
                if (type != null) {
                    types.merge(entry.getKey(), type, MongoTableSource::merge);
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
