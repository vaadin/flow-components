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
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.vaadin.flow.component.ai.provider.ToolException;
import com.vaadin.flow.internal.JacksonUtils;

/**
 * A query in the small JSON language of the collection-like endpoints, a REST
 * API or a directory of CSV files: one source, the fields to show under the
 * result column names, equality filters and a sort order. That is all such an
 * endpoint can do, so that is all the language can express.
 *
 * @param source
 *            the collection or file to read
 * @param columns
 *            result column name to field, in column order, at least one
 * @param filter
 *            field to the value it must equal, possibly empty
 * @param sort
 *            the fields to order by, possibly empty
 */
public record CollectionQuery(String source, Map<String, String> columns,
        Map<String, Object> filter, List<Sort> sort) {

    /**
     * One sort key.
     *
     * @param field
     *            the field to order by
     * @param descending
     *            {@code true} for descending order
     */
    public record Sort(String field, boolean descending) {
    }

    /**
     * What a field holds, as far as a filter value needs to match it.
     */
    public enum FieldKind {
        NUMBER, DATE, BOOLEAN, TEXT;

        /**
         * Tells the kind from a value of the field.
         *
         * @param sample
         *            a value the field holds, or {@code null} if none is known
         * @return the kind, or {@code null} for a {@code null} sample
         */
        public static FieldKind of(Object sample) {
            return switch (sample) {
            case null -> null;
            case Number n -> NUMBER;
            case Boolean b -> BOOLEAN;
            case Temporal t -> DATE;
            case String s ->
                TextValues.parse(s, false) instanceof LocalDate ? DATE : TEXT;
            default -> TEXT;
            };
        }

        private boolean canEqual(Object value) {
            return switch (this) {
            case NUMBER -> value instanceof Number
                    || value instanceof String s && isNumber(s);
            case DATE -> value instanceof String s && isDate(s);
            case BOOLEAN -> value instanceof Boolean
                    || value instanceof String s && (s.equalsIgnoreCase("true")
                            || s.equalsIgnoreCase("false"));
            case TEXT -> true;
            };
        }

        private static boolean isNumber(String text) {
            try {
                new BigDecimal(text.trim());
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        private static boolean isDate(String text) {
            return TextValues.parse(text, false) instanceof LocalDate;
        }
    }

    /**
     * Parses a query. Every problem with its shape is reported as a
     * {@link ToolException}, so the LLM can correct the query.
     *
     * @param json
     *            the query JSON
     * @return the query
     * @throws ToolException
     *             if the JSON is not a query
     */
    public static CollectionQuery parse(String json) {
        Object parsed;
        try {
            parsed = JacksonUtils.getMapper().readValue(json.trim(),
                    Object.class);
        } catch (RuntimeException e) {
            throw new ToolException("The query must be a JSON object: "
                    + e.getMessage().lines().findFirst().orElse(""), e);
        }
        if (!(parsed instanceof Map<?, ?> object)) {
            throw new ToolException(
                    "The query must be a JSON object with \"source\" and \"columns\"");
        }
        if (!(object.get("source") instanceof String source)
                || source.isBlank()) {
            throw new ToolException(
                    "\"source\" must name the collection to read");
        }
        if (!(object.get("columns") instanceof Map<?, ?> columnsObject)
                || columnsObject.isEmpty()) {
            throw new ToolException(
                    "\"columns\" must be an object mapping each result column name to a field");
        }
        var columns = new LinkedHashMap<String, String>();
        columnsObject.forEach((name, field) -> {
            if (!(field instanceof String fieldName) || fieldName.isBlank()) {
                throw new ToolException("Column \"" + name
                        + "\" must name a field, not an expression");
            }
            columns.put(String.valueOf(name), fieldName);
        });
        var filter = new LinkedHashMap<String, Object>();
        var filterObject = object.get("filter");
        if (filterObject != null) {
            if (!(filterObject instanceof Map<?, ?> filterMap)) {
                throw new ToolException(
                        "\"filter\" must be an object mapping fields to the values they must equal");
            }
            filterMap.forEach((field, value) -> {
                if (value instanceof Map || value instanceof List) {
                    throw new ToolException("Filter value of \"" + field
                            + "\" must be a single value; only equality is supported");
                }
                filter.put(String.valueOf(field), value);
            });
        }
        var sort = new ArrayList<Sort>();
        var sortObject = object.get("sort");
        if (sortObject != null) {
            if (!(sortObject instanceof List<?> sortList)) {
                throw new ToolException(
                        "\"sort\" must be an array of {\"field\": ..., \"direction\": \"asc\" or \"desc\"}");
            }
            for (var entry : sortList) {
                if (!(entry instanceof Map<?, ?> sortMap)
                        || !(sortMap.get("field") instanceof String field)) {
                    throw new ToolException(
                            "Each sort entry must be {\"field\": ..., \"direction\": \"asc\" or \"desc\"}");
                }
                sort.add(new Sort(field, "desc".equalsIgnoreCase(
                        String.valueOf(sortMap.get("direction")))));
            }
        }
        return new CollectionQuery(source, columns, filter, sort);
    }

    /**
     * Rejects a filter value no value of the field can equal: a comparison
     * written as text, such as {@code ">100"}, or a value of another kind than
     * the field holds. Without the check such a filter matches nothing, and the
     * LLM takes the empty result for the answer.
     *
     * @param field
     *            the filtered field
     * @param value
     *            the filter value
     * @param kind
     *            what the field holds, or {@code null} if unknown, which
     *            accepts every value that is not a comparison
     * @throws ToolException
     *             if no value of the field can equal the filter value
     */
    public static void checkFilterValue(String field, Object value,
            FieldKind kind) {
        if (value instanceof String text && text.trim().matches("[<>=!].*")) {
            throw new ToolException("Filter value \"" + text + "\" of " + field
                    + " is a comparison; only equality is supported, so give the exact value the field must have");
        }
        if (kind != null && value != null && !kind.canEqual(value)) {
            throw new ToolException("Filter value " + describe(value) + " of "
                    + field + " cannot match: the field holds "
                    + kind.name().toLowerCase(Locale.ROOT)
                    + " values and only equality with one of them is supported");
        }
    }

    private static String describe(Object value) {
        return value instanceof String s ? "\"" + s + "\""
                : String.valueOf(value);
    }

    /**
     * Returns every field the query refers to, in order of first use.
     *
     * @return the fields
     */
    public List<String> fields() {
        var fields = new LinkedHashSet<>(columns.values());
        fields.addAll(filter.keySet());
        sort.forEach(key -> fields.add(key.field()));
        return List.copyOf(fields);
    }

    /**
     * Returns the field a result column shows.
     *
     * @param column
     *            the result column name
     * @return the field, or {@code null} if the column is not in the query
     */
    public String fieldOf(String column) {
        return columns.get(column);
    }

    /**
     * Builds the row of an item: the fields under their result column names.
     *
     * @param item
     *            the item, as field to value
     * @return the row
     */
    public Map<String, Object> project(Map<String, Object> item) {
        var row = new LinkedHashMap<String, Object>();
        columns.forEach((name, field) -> row.put(name, item.get(field)));
        return row;
    }

    /**
     * Tells whether an item passes the filter: every filtered field equals its
     * value, compared as the values themselves or as text.
     *
     * @param item
     *            the item, as field to value
     * @return {@code true} if the item passes
     */
    public boolean matches(Map<String, Object> item) {
        return filter.entrySet().stream().allMatch(entry -> {
            var value = item.get(entry.getKey());
            return Objects.equals(value, entry.getValue()) || String
                    .valueOf(value).equals(String.valueOf(entry.getValue()));
        });
    }

    /**
     * Returns a comparator ordering items by the query's sort keys.
     *
     * @return the comparator, ordering nothing when the query has no sort
     */
    public Comparator<Map<String, Object>> comparator() {
        Comparator<Map<String, Object>> comparator = (a, b) -> 0;
        for (var key : sort) {
            comparator = comparator
                    .thenComparing((a, b) -> compareValues(a.get(key.field()),
                            b.get(key.field()), key.descending()));
        }
        return comparator;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static int compareValues(Object a, Object b, boolean descending) {
        if (a == null || b == null) {
            return a == null ? (b == null ? 0 : 1) : -1;
        }
        int result;
        if (a instanceof Number x && b instanceof Number y) {
            result = new BigDecimal(x.toString())
                    .compareTo(new BigDecimal(y.toString()));
        } else if (a instanceof Comparable comparable
                && a.getClass().equals(b.getClass())) {
            result = comparable.compareTo(b);
        } else {
            result = String.valueOf(a).compareTo(String.valueOf(b));
        }
        return descending ? -result : result;
    }
}
