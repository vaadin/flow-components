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
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.component.ai.provider.TableSource;
import com.vaadin.flow.component.ai.provider.ToolException;
import com.vaadin.flow.internal.JacksonUtils;

import graphql.introspection.IntrospectionQuery;
import tools.jackson.databind.JsonNode;

/**
 * A {@link TableSource} over one list field of a GraphQL API, the way an
 * adapter over GraphQL would map the API: the schema is introspected, every
 * root field returning a list of objects becomes a table, the object's scalar
 * fields become columns, and an object-valued field with an {@code id} becomes
 * an {@code <field>Id} column holding that id. Arguments of the root field that
 * share a name and type with a column are pushed down as equality filters.
 * Where the API offers them, the conventional {@code orderBy}, {@code offset}
 * and {@code limit} arguments take a sort by scalar fields and a page, and a
 * root field {@code <field>Count} with the same filter arguments takes a count.
 * Everything else the engine does. The schema is read with graphql-java's
 * standard introspection query.
 * <p>
 * GraphQL has no date scalar, so a string column whose values all parse as ISO
 * dates is typed as a date, decided on a first fetch at discovery.
 * </p>
 */
public class GraphQLTableSource implements TableSource {

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private final URI endpoint;
    private final String field;
    private final List<Column> columns;
    private final Map<String, String> selections;
    private final Set<String> arguments;
    private final Set<Capability> capabilities;

    /**
     * An argument value written into the query as it is, such as an input
     * object or an enum value, rather than as a JSON value.
     */
    record Literal(String text) {
    }

    private GraphQLTableSource(URI endpoint, String field, List<Column> columns,
            Map<String, String> selections, Set<String> arguments,
            Set<Capability> capabilities) {
        this.endpoint = endpoint;
        this.field = field;
        this.columns = columns;
        this.selections = selections;
        this.arguments = arguments;
        this.capabilities = capabilities;
    }

    /**
     * Creates a source for every root field of the API that returns a list of
     * objects.
     *
     * @param endpoint
     *            the GraphQL endpoint
     * @return the sources, in schema order
     */
    public static List<GraphQLTableSource> discover(URI endpoint) {
        var schema = post(endpoint, IntrospectionQuery.INTROSPECTION_QUERY)
                .get("__schema");
        var types = new HashMap<String, JsonNode>();
        for (var type : schema.get("types")) {
            types.put(type.get("name").stringValue(), type);
        }
        var queryType = types
                .get(schema.get("queryType").get("name").stringValue());
        var countFields = new HashSet<String>();
        for (var rootField : queryType.get("fields")) {
            if ("Int".equals(
                    named(rootField.get("type")).get("name").stringValue())) {
                countFields.add(rootField.get("name").stringValue());
            }
        }
        var sources = new ArrayList<GraphQLTableSource>();
        for (var rootField : queryType.get("fields")) {
            var listOf = listOf(rootField.get("type"));
            if (listOf == null) {
                continue;
            }
            var type = types.get(listOf);
            if (type == null
                    || !"OBJECT".equals(type.get("kind").stringValue())) {
                continue;
            }
            sources.add(of(endpoint, rootField, type, types, countFields));
        }
        return sources;
    }

    private static GraphQLTableSource of(URI endpoint, JsonNode rootField,
            JsonNode type, Map<String, JsonNode> types,
            Set<String> countFields) {
        var name = rootField.get("name").stringValue();
        var columns = new ArrayList<Column>();
        var selections = new LinkedHashMap<String, String>();
        for (var objectField : type.get("fields")) {
            var fieldName = objectField.get("name").stringValue();
            var fieldType = objectField.get("type");
            if (listOf(fieldType) != null) {
                continue;
            }
            var named = named(fieldType);
            var kind = named.get("kind").stringValue();
            if ("SCALAR".equals(kind) || "ENUM".equals(kind)) {
                columns.add(new Column(fieldName,
                        javaType(named.get("name").stringValue())));
                selections.put(fieldName, fieldName);
            } else if ("OBJECT".equals(kind)) {
                var idType = idType(types.get(named.get("name").stringValue()));
                if (idType != null) {
                    columns.add(new Column(fieldName + "Id", idType));
                    selections.put(fieldName + "Id", fieldName + " { id }");
                }
            }
        }
        var arguments = new LinkedHashSet<String>();
        var argumentNames = new HashSet<String>();
        for (var argument : rootField.get("args")) {
            var argumentName = argument.get("name").stringValue();
            argumentNames.add(argumentName);
            var argumentType = named(argument.get("type"));
            columns.stream()
                    .filter(column -> column.name().equals(argumentName)
                            && column.type() == javaType(
                                    argumentType.get("name").stringValue()))
                    .findFirst()
                    .ifPresent(column -> arguments.add(argumentName));
        }
        var capabilities = EnumSet.noneOf(Capability.class);
        if (!arguments.isEmpty()) {
            capabilities.add(Capability.EQUALITY_FILTER);
        }
        if (argumentNames.contains("orderBy")) {
            capabilities.add(Capability.SORT);
        }
        if (argumentNames.contains("offset")
                && argumentNames.contains("limit")) {
            capabilities.add(Capability.LIMIT);
        }
        if (countFields.contains(name + "Count")) {
            capabilities.add(Capability.COUNT);
        }
        var items = post(endpoint, query(name, Map.of(), selections)).get(name);
        return new GraphQLTableSource(endpoint, name,
                refineDates(columns, items, selections), selections, arguments,
                Set.copyOf(capabilities));
    }

    /**
     * Returns the name of the object type a field lists, or {@code null} if the
     * field does not return a list.
     */
    private static String listOf(JsonNode type) {
        var unwrapped = type;
        while ("NON_NULL".equals(unwrapped.get("kind").stringValue())) {
            unwrapped = unwrapped.get("ofType");
        }
        if (!"LIST".equals(unwrapped.get("kind").stringValue())) {
            return null;
        }
        return named(unwrapped.get("ofType")).get("name").stringValue();
    }

    private static JsonNode named(JsonNode type) {
        var unwrapped = type;
        while (unwrapped.get("ofType") != null
                && !unwrapped.get("ofType").isNull()) {
            unwrapped = unwrapped.get("ofType");
        }
        return unwrapped;
    }

    private static Class<?> idType(JsonNode type) {
        if (type == null || type.get("fields") == null) {
            return null;
        }
        for (var field : type.get("fields")) {
            if ("id".equals(field.get("name").stringValue())) {
                var named = named(field.get("type"));
                return "SCALAR".equals(named.get("kind").stringValue())
                        ? javaType(named.get("name").stringValue())
                        : null;
            }
        }
        return null;
    }

    private static Class<?> javaType(String scalar) {
        return switch (scalar) {
        case "Int" -> Integer.class;
        case "Float" -> BigDecimal.class;
        case "Boolean" -> Boolean.class;
        default -> String.class;
        };
    }

    /**
     * Retypes a string column as a date when every value of the first fetch
     * parses as one.
     */
    private static List<Column> refineDates(List<Column> columns,
            JsonNode items, Map<String, String> selections) {
        var refined = new ArrayList<Column>();
        for (var column : columns) {
            var type = column.type();
            if (type == String.class) {
                var values = new ArrayList<String>();
                for (var item : items) {
                    var value = item.get(column.name());
                    if (value != null && !value.isNull()) {
                        values.add(value.stringValue());
                    }
                }
                if (!values.isEmpty() && values.stream()
                        .allMatch(value -> parses(value, LocalDate.class))) {
                    type = LocalDate.class;
                } else if (!values.isEmpty() && values.stream().allMatch(
                        value -> parses(value, LocalDateTime.class))) {
                    type = LocalDateTime.class;
                }
            }
            refined.add(new Column(column.name(), type));
        }
        return refined;
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

    @Override
    public String getName() {
        return field;
    }

    @Override
    public List<Column> getColumns() {
        return columns;
    }

    @Override
    public Set<Capability> getCapabilities() {
        return capabilities;
    }

    /**
     * Filters only by the columns the root field has an argument for.
     */
    @Override
    public boolean supportsEqualityFilter(String column) {
        return arguments.contains(column);
    }

    /**
     * Sorts only by scalar fields; an id column of an object field has no field
     * of its own to order by.
     */
    @Override
    public boolean supportsSort(List<SortKey> sort) {
        return sort.stream().allMatch(
                key -> key.column().equals(selections.get(key.column())));
    }

    @Override
    public String getDescription() {
        return "GraphQL field " + field;
    }

    /**
     * Returns the root field arguments used as equality filters.
     *
     * @return the argument names
     */
    public Set<String> getArguments() {
        return arguments;
    }

    @Override
    public List<Map<String, Object>> fetch(Pushdown pushdown) {
        var pushed = filterArguments(pushdown);
        if (!pushdown.sort().isEmpty()) {
            pushed.put("orderBy", new Literal(pushdown.sort().stream()
                    .map(key -> "{field: \"" + key.column() + "\", direction: "
                            + (key.descending() ? "DESC" : "ASC") + "}")
                    .collect(Collectors.joining(", ", "[", "]"))));
        }
        if (pushdown.offset() > 0) {
            pushed.put("offset", pushdown.offset());
        }
        if (pushdown.limit() != null) {
            pushed.put("limit", Math.min(pushdown.limit(), Integer.MAX_VALUE));
        }
        var items = post(endpoint, query(field, pushed, selections)).get(field);
        var rows = new ArrayList<Map<String, Object>>();
        for (var item : items) {
            var row = new LinkedHashMap<String, Object>();
            for (var column : columns) {
                row.put(column.name(), toValue(item, column));
            }
            rows.add(row);
        }
        return rows;
    }

    @Override
    public long count(Pushdown pushdown) {
        var countField = field + "Count";
        var query = query(countField, filterArguments(pushdown), Map.of());
        return post(endpoint, query).get(countField).longValue();
    }

    private Map<String, Object> filterArguments(Pushdown pushdown) {
        var pushed = new LinkedHashMap<String, Object>();
        pushdown.equalityFilters().forEach((column, value) -> {
            if (arguments.contains(column)) {
                pushed.put(column, value);
            }
        });
        return pushed;
    }

    private Object toValue(JsonNode item, Column column) {
        var selection = selections.get(column.name());
        JsonNode node;
        if (selection.endsWith("{ id }")) {
            var object = item
                    .get(selection.substring(0, selection.indexOf(' ')));
            node = object == null || object.isNull() ? null : object.get("id");
        } else {
            node = item.get(column.name());
        }
        if (node == null || node.isNull()) {
            return null;
        }
        var type = column.type();
        if (type == Integer.class) {
            return node.intValue();
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
     * Renders the query for a root field: its arguments as literals and the
     * selections of every column, or no selection set for a field of a scalar
     * type such as a count.
     */
    static String query(String field, Map<String, Object> arguments,
            Map<String, String> selections) {
        var query = new StringBuilder("{ ").append(field);
        if (!arguments.isEmpty()) {
            query.append('(').append(arguments.entrySet().stream()
                    .map(argument -> argument.getKey() + ": "
                            + (argument.getValue() instanceof Literal literal
                                    ? literal.text()
                                    : JacksonUtils.getMapper()
                                            .writeValueAsString(
                                                    argument.getValue())))
                    .collect(Collectors.joining(", "))).append(')');
        }
        if (!selections.isEmpty()) {
            query.append(" { ").append(String.join(" ", selections.values()))
                    .append(" }");
        }
        return query.append(" }").toString();
    }

    /**
     * Posts a query and returns its {@code data}. The API's own error messages
     * name only fields and arguments, so they are relayed to the LLM.
     */
    private static JsonNode post(URI endpoint, String query) {
        var body = JacksonUtils.getMapper()
                .writeValueAsString(Map.of("query", query));
        try {
            var response = HTTP.send(HttpRequest.newBuilder(endpoint)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                    HttpResponse.BodyHandlers.ofString());
            var result = JacksonUtils.readTree(response.body());
            var errors = result.get("errors");
            if (errors != null && !errors.isNull() && !errors.isEmpty()) {
                throw new ToolException(
                        errors.get(0).path("message").stringValue());
            }
            if (response.statusCode() != 200) {
                throw new IllegalStateException("POST " + endpoint
                        + " returned " + response.statusCode());
            }
            return result.get("data");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted", e);
        }
    }
}
