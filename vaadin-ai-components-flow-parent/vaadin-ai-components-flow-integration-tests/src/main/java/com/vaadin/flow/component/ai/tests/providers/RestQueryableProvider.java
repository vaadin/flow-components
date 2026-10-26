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
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.TreeMap;

import com.vaadin.flow.component.ai.common.QueryLanguage;
import com.vaadin.flow.component.ai.common.QueryPage;
import com.vaadin.flow.component.ai.provider.QueryableProvider;
import com.vaadin.flow.component.ai.provider.ToolException;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.internal.JacksonUtils;

/**
 * A {@link QueryableProvider} over a REST API that lets the LLM ask for exactly
 * what the API can do, in the JSON language of {@link CollectionQuery}: items
 * of one collection, equality filters and a sort order, each mapped to the
 * API's query parameters. Paging and counting are pushed down as well, using
 * the API's page numbers and total count. Nothing is copied or computed on the
 * way, so the LLM cannot be promised a join or a sum the API does not offer.
 * Compare with {@link RestMirrorDatabaseProvider}, which copies the data into
 * SQL instead.
 * <p>
 * Expects the API shape of the sample REST API: an index listing the
 * collections and paged collections with a {@code content} array that accept
 * {@code field=value} filters and {@code sort=field,asc|desc}.
 * </p>
 */
public class RestQueryableProvider implements QueryableProvider {

    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final int PAGE_SIZE = 100;

    private final URI apiUri;
    private final String notes;
    private transient volatile Map<String, Map<String, String>> collections;

    /**
     * Creates a provider for the REST API at the given URI.
     *
     * @param apiUri
     *            the URI of the API index listing the collections
     * @param notes
     *            notes about the data appended to the schema, or {@code null}
     */
    public RestQueryableProvider(URI apiUri, String notes) {
        this.apiUri = apiUri;
        this.notes = notes == null ? "" : notes;
    }

    @Override
    public QueryLanguage getQueryLanguage() {
        return CollectionQueryLanguage.REST;
    }

    @Override
    public String getSchema() {
        var schema = new StringBuilder("REST API at ").append(apiUri).append(
                ". Collections and their fields, from a sample item of each:\n");
        collections().forEach((name, fields) -> {
            schema.append("- ").append(name).append(": ");
            var described = new ArrayList<String>();
            fields.forEach(
                    (field, type) -> described.add(field + " (" + type + ")"));
            schema.append(String.join(", ", described)).append('\n');
        });
        if (!notes.isBlank()) {
            schema.append("\nNOTES:\n").append(notes);
        }
        return schema.toString();
    }

    @Override
    public List<Map<String, Object>> executeQuery(String query) {
        var request = validate(CollectionQuery.parse(query));
        var rows = new ArrayList<Map<String, Object>>();
        int page = 0;
        int totalPages;
        do {
            var body = get(request, request.sort(), page, PAGE_SIZE);
            for (var item : content(body)) {
                rows.add(request.project(item));
            }
            totalPages = pageInfo(body, "totalPages");
            page++;
        } while (page < totalPages);
        return rows;
    }

    @Override
    public List<Map<String, Object>> executeQuery(String query,
            QueryPage page) {
        var request = validate(CollectionQuery.parse(query));
        var sort = request.sort();
        if (!page.sortOrders().isEmpty()) {
            // The component sorts by result columns; the API by fields
            var order = page.sortOrders().getFirst();
            var field = request.fieldOf(order.getSorted());
            if (field != null) {
                sort = List.of(new CollectionQuery.Sort(field,
                        order.getDirection() == SortDirection.DESCENDING));
            }
        }
        // The API pages by number; a page starting mid-way needs two
        int firstPage = page.offset() / page.limit();
        int skip = page.offset() % page.limit();
        var items = new ArrayList<>(
                content(get(request, sort, firstPage, page.limit())));
        if (skip > 0) {
            items.addAll(
                    content(get(request, sort, firstPage + 1, page.limit())));
        }
        var rows = new ArrayList<Map<String, Object>>();
        for (var item : items.subList(Math.min(skip, items.size()),
                Math.min(skip + page.limit(), items.size()))) {
            rows.add(request.project(item));
        }
        return rows;
    }

    @Override
    public OptionalInt count(String query) {
        var request = validate(CollectionQuery.parse(query));
        return OptionalInt
                .of(pageInfo(get(request, List.of(), 0, 1), "totalElements"));
    }

    private CollectionQuery validate(CollectionQuery query) {
        var fields = collections().get(query.source());
        if (fields == null) {
            throw new ToolException("Unknown collection " + query.source()
                    + ". Available: " + collections().keySet());
        }
        for (var field : query.fields()) {
            if (!fields.containsKey(field)) {
                throw new ToolException(
                        "Collection " + query.source() + " has no field "
                                + field + ". Fields: " + fields.keySet());
            }
        }
        return query;
    }

    /**
     * Returns the collections with their fields and types, sampled from the
     * first item of each on the first call.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Map<String, String>> collections() {
        var current = collections;
        if (current == null) {
            var result = new TreeMap<String, Map<String, String>>();
            var index = (Map<String, String>) getJson(apiUri)
                    .get("collections");
            index.forEach((name, href) -> {
                var fields = new LinkedHashMap<String, String>();
                var sample = content(
                        getJson(URI.create(apiUri.resolve(href) + "?size=1")));
                if (!sample.isEmpty()) {
                    sample.getFirst().forEach((field, value) -> fields
                            .put(field, typeName(value)));
                }
                result.put(name, fields);
            });
            collections = result;
            current = result;
        }
        return current;
    }

    private Map<String, Object> get(CollectionQuery query,
            List<CollectionQuery.Sort> sort, int page, int size) {
        var parameters = new StringBuilder("?page=" + page + "&size=" + size);
        query.filter()
                .forEach((field, value) -> parameters.append('&')
                        .append(encode(field)).append('=')
                        .append(encode(String.valueOf(value))));
        if (!sort.isEmpty()) {
            var key = sort.getFirst();
            parameters.append("&sort=").append(encode(key.field()))
                    .append(key.descending() ? ",desc" : ",asc");
        }
        return getJson(URI.create(apiUri.resolve("/api/" + query.source())
                + parameters.toString()));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> content(Map<String, Object> body) {
        return (List<Map<String, Object>>) body.get("content");
    }

    @SuppressWarnings("unchecked")
    private static int pageInfo(Map<String, Object> body, String key) {
        return ((Number) ((Map<String, Object>) body.get("page")).get(key))
                .intValue();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getJson(URI uri) {
        try {
            var response = HTTP.send(HttpRequest.newBuilder(uri).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            var body = JacksonUtils.getMapper().readValue(response.body(),
                    Map.class);
            if (response.statusCode() == 400 && body.get("error") != null) {
                // The API describes what is wrong with the request the LLM
                // made, so the LLM can correct it
                throw new ToolException(String.valueOf(body.get("error")));
            }
            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "GET " + uri + " returned " + response.statusCode());
            }
            return body;
        } catch (IOException e) {
            throw new IllegalStateException("GET " + uri + " failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("GET " + uri + " interrupted", e);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String typeName(Object value) {
        return switch (value) {
        case null -> "null";
        case Integer i -> "integer";
        case Long l -> "integer";
        case Double d -> "number";
        case Boolean b -> "boolean";
        case String s ->
            TextValues.parse(s, false) instanceof LocalDate ? "date, ISO string"
                    : "string";
        default -> value.getClass().getSimpleName().toLowerCase();
        };
    }
}
