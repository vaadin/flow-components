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
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.vaadin.flow.component.ai.provider.TableSource;
import com.vaadin.flow.component.ai.provider.ToolException;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;

/**
 * A {@link TableSource} over one collection of the sample REST API. The column
 * types are inferred from the values of the first page, which is where the
 * guesswork of such sources lies: JSON carries dates as strings and does not
 * tell an integer from a decimal unless the value has a fraction.
 * <p>
 * Equality filters are pushed down as the API's {@code field=value} query
 * parameters; everything else the engine does. Every page of the collection is
 * fetched, so the API's own paging stays invisible to the engine.
 * </p>
 */
public class RestTableSource implements TableSource {

    private static final int PAGE_SIZE = 100;

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private final URI collectionUri;
    private final String name;
    private final List<Column> columns;

    private RestTableSource(URI collectionUri, String name,
            List<Column> columns) {
        this.collectionUri = collectionUri;
        this.name = name;
        this.columns = columns;
    }

    /**
     * Creates a source for every collection the API's index lists.
     *
     * @param api
     *            the API root, whose index lists the collections
     * @return the sources, in index order
     */
    public static List<RestTableSource> discover(URI api) {
        var index = getJson(api, null);
        var sources = new ArrayList<RestTableSource>();
        for (var collection : index.get("collections").properties()) {
            sources.add(of(api, collection.getKey()));
        }
        return sources;
    }

    /**
     * Creates a source for one collection, reading its first page to infer the
     * columns.
     *
     * @param api
     *            the API root
     * @param collection
     *            the collection name
     * @return the source
     */
    public static RestTableSource of(URI api, String collection) {
        var collectionUri = URI.create(api + "/" + collection);
        var page = getJson(collectionUri, "size=" + PAGE_SIZE);
        return new RestTableSource(collectionUri, collection,
                inferColumns(page.get("content")));
    }

    @Override
    public String getName() {
        return name;
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
        return "REST collection " + collectionUri.getPath();
    }

    @Override
    public List<Map<String, Object>> fetch(Pushdown pushdown) {
        var filters = new StringBuilder();
        for (var filter : pushdown.equalityFilters().entrySet()) {
            filters.append('&').append(encode(filter.getKey())).append('=')
                    .append(encode(String.valueOf(filter.getValue())));
        }
        var rows = new ArrayList<Map<String, Object>>();
        var pageNumber = 0;
        while (true) {
            var page = getJson(collectionUri,
                    "size=" + PAGE_SIZE + "&page=" + pageNumber + filters);
            for (var item : page.get("content")) {
                rows.add(toRow(item));
            }
            var totalPages = page.get("page").get("totalPages").intValue();
            if (++pageNumber >= totalPages) {
                return rows;
            }
        }
    }

    private Map<String, Object> toRow(JsonNode item) {
        var row = new LinkedHashMap<String, Object>();
        for (var column : columns) {
            row.put(column.name(),
                    toValue(item.get(column.name()), column.type()));
        }
        return row;
    }

    private static Object toValue(JsonNode node, Class<?> type) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (type == Integer.class) {
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
    static List<Column> inferColumns(JsonNode items) {
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
