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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.vaadin.flow.component.ai.common.QueryLanguage;
import com.vaadin.flow.component.ai.provider.QueryableProvider;
import com.vaadin.flow.component.ai.provider.ToolException;
import com.vaadin.flow.internal.JacksonUtils;

import graphql.introspection.IntrospectionQuery;
import graphql.introspection.IntrospectionResultToSchema;
import graphql.schema.idl.SchemaPrinter;

/**
 * A {@link QueryableProvider} over a GraphQL API: the LLM writes GraphQL, the
 * schema is the API's own SDL obtained by introspection, and the items of the
 * query's single root field become the rows, with nested objects flattened into
 * dotted column names. Paging and counting use the defaults, so each page runs
 * the whole query; the sample API is small.
 */
public class GraphQLQueryableProvider implements QueryableProvider {

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private final URI endpoint;
    private final String notes;

    /**
     * Creates a provider for the GraphQL API at the given endpoint.
     *
     * @param endpoint
     *            the URI accepting GraphQL POST requests
     * @param notes
     *            notes about the data appended to the schema, or {@code null}
     */
    public GraphQLQueryableProvider(URI endpoint, String notes) {
        this.endpoint = endpoint;
        this.notes = notes == null ? "" : notes;
    }

    @Override
    public QueryLanguage getQueryLanguage() {
        return GraphQLQueryLanguage.INSTANCE;
    }

    @Override
    public String getSchema() {
        var document = new IntrospectionResultToSchema().createSchemaDefinition(
                data(post(IntrospectionQuery.INTROSPECTION_QUERY)));
        var sdl = new SchemaPrinter(
                SchemaPrinter.Options.defaultOptions().includeDirectives(false))
                .print(document);
        var schema = new StringBuilder("GRAPHQL SCHEMA of the API at ")
                .append(endpoint).append(":\n\n").append(sdl);
        if (!notes.isBlank()) {
            schema.append("\nNOTES:\n").append(notes);
        }
        return schema.toString();
    }

    @Override
    public List<Map<String, Object>> executeQuery(String query) {
        var result = post(query);
        if (result.get("errors") instanceof List<?> errors
                && !errors.isEmpty()) {
            // GraphQL errors describe the query the LLM wrote, so the LLM
            // can correct it
            throw new ToolException(errors.stream()
                    .map(error -> error instanceof Map<?, ?> map
                            ? String.valueOf(map.get("message"))
                            : String.valueOf(error))
                    .collect(Collectors.joining("; ")));
        }
        var data = data(result);
        if (data.size() != 1) {
            throw new ToolException(
                    "Select exactly one field of the Query type; the query selected "
                            + data.size());
        }
        var root = data.entrySet().iterator().next();
        var rows = new ArrayList<Map<String, Object>>();
        if (root.getValue() instanceof List<?> items) {
            for (var item : items) {
                rows.add(item instanceof Map<?, ?> map ? flatten(map)
                        : singleColumn(root.getKey(), item));
            }
        } else if (root.getValue() instanceof Map<?, ?> map) {
            rows.add(flatten(map));
        } else {
            rows.add(singleColumn(root.getKey(), root.getValue()));
        }
        return rows;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> data(Map<String, Object> result) {
        return result.get("data") instanceof Map<?, ?> map
                ? (Map<String, Object>) map
                : Map.of();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String query) {
        try {
            var body = JacksonUtils.getMapper()
                    .writeValueAsString(Map.of("query", query));
            var request = HttpRequest.newBuilder(endpoint)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build();
            var response = HTTP.send(request,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "The GraphQL API returned " + response.statusCode());
            }
            return JacksonUtils.getMapper().readValue(response.body(),
                    Map.class);
        } catch (IOException e) {
            throw new IllegalStateException("GraphQL request failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("GraphQL request interrupted", e);
        }
    }

    private static Map<String, Object> singleColumn(String name, Object value) {
        var row = new LinkedHashMap<String, Object>();
        row.put(name, value);
        return row;
    }

    /**
     * Flattens nested objects into dotted keys, keeping the selection order.
     *
     * @param object
     *            the object to flatten
     * @return the row
     */
    static Map<String, Object> flatten(Map<?, ?> object) {
        var row = new LinkedHashMap<String, Object>();
        flattenInto(row, "", object);
        return row;
    }

    private static void flattenInto(Map<String, Object> row, String prefix,
            Map<?, ?> object) {
        object.forEach((key, value) -> {
            var name = prefix + key;
            if (value instanceof Map<?, ?> nested) {
                flattenInto(row, name + ".", nested);
            } else if (value instanceof List<?> list) {
                row.put(name,
                        list.stream()
                                .map(item -> item instanceof Map<?, ?> map
                                        ? JacksonUtils.getMapper()
                                                .writeValueAsString(map)
                                        : String.valueOf(item))
                                .collect(Collectors.joining(", ")));
            } else {
                row.put(name, value);
            }
        });
    }
}
