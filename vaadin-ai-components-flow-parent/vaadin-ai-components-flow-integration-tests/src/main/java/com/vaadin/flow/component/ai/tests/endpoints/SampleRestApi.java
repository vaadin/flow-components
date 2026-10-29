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
package com.vaadin.flow.component.ai.tests.endpoints;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Serves {@link SampleData} as a paged JSON REST API in the shape Spring Data
 * REST and Spring MVC give a {@code Page}: {@code GET /api} lists the
 * collections, {@code GET /api/{collection}?page=0&size=20} returns one page as
 * {@code {"content": [...], "page": {"size", "number", "totalElements",
 * "totalPages"}}}. Fields are camelCase, dates are ISO strings and decimals are
 * numbers, as a typical JSON API has them.
 * <p>
 * Like most such APIs it filters and sorts, but only so much: any other query
 * parameter is an equality filter on a field of the collection
 * ({@code ?region=North}), and {@code sort=field,asc} or
 * {@code sort=field,desc} orders the items by a field, repeated for more than
 * one ({@code sort=region,asc&sort=revenue,desc}), with null values after all
 * others when ascending and before them when descending. There are no joins,
 * aggregations or computed values; an unknown field is a {@code 400} with an
 * {@code error} message.
 * </p>
 */
final class SampleRestApi implements HttpHandler {

    static final int DEFAULT_PAGE_SIZE = 20;
    static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> PAGING_PARAMETERS = Set.of("page", "size",
            "sort");

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            SampleApiServer.sendError(exchange, 405, "Only GET is supported");
            return;
        }
        var path = exchange.getRequestURI().getPath();
        var resource = path.replaceFirst("^/api/?", "");
        if (resource.isEmpty()) {
            SampleApiServer.sendJson(exchange, 200, index());
            return;
        }
        var table = SampleData.table(resource);
        if (table == null || !table.resourceName().equals(resource)) {
            SampleApiServer.sendError(exchange, 404,
                    "Unknown collection: " + resource);
            return;
        }
        var parameters = SampleApiServer.queryParameters(exchange);
        var fields = table.componentNames();
        var items = table.rowMaps();
        for (var parameter : parameters.entrySet()) {
            if (PAGING_PARAMETERS.contains(parameter.getKey())) {
                continue;
            }
            if (!fields.contains(parameter.getKey())) {
                SampleApiServer.sendError(exchange, 400, "Unknown field: "
                        + parameter.getKey() + ". Fields: " + fields);
                return;
            }
            var value = parameter.getValue();
            items = items.stream().filter(item -> String
                    .valueOf(item.get(parameter.getKey())).equals(value))
                    .toList();
        }
        Comparator<Map<String, Object>> order = null;
        for (var sort : SampleApiServer.queryParameterValues(exchange,
                "sort")) {
            if (sort.isBlank()) {
                continue;
            }
            var parts = sort.split(",", 2);
            if (!fields.contains(parts[0])) {
                SampleApiServer.sendError(exchange, 400, "Unknown sort field: "
                        + parts[0] + ". Fields: " + fields);
                return;
            }
            var descending = parts.length > 1
                    && parts[1].equalsIgnoreCase("desc");
            var byField = byField(parts[0], descending);
            order = order == null ? byField : order.thenComparing(byField);
        }
        if (order != null) {
            var sorted = new ArrayList<>(items);
            sorted.sort(order);
            items = sorted;
        }
        int page = parseInt(parameters.get("page"), 0);
        int size = Math.min(MAX_PAGE_SIZE, Math.max(1,
                parseInt(parameters.get("size"), DEFAULT_PAGE_SIZE)));
        SampleApiServer.sendJson(exchange, 200, page(items, page, size));
    }

    private static Map<String, Object> index() {
        var collections = new LinkedHashMap<String, String>();
        for (var table : SampleData.TABLES) {
            collections.put(table.resourceName(),
                    "/api/" + table.resourceName());
        }
        return Map.of("collections", collections);
    }

    private static Map<String, Object> page(List<Map<String, Object>> items,
            int page, int size) {
        int from = Math.min(items.size(), page * size);
        int to = Math.min(items.size(), from + size);
        var pageInfo = new LinkedHashMap<String, Object>();
        pageInfo.put("size", size);
        pageInfo.put("number", page);
        pageInfo.put("totalElements", items.size());
        pageInfo.put("totalPages", (items.size() + size - 1) / size);
        var body = new LinkedHashMap<String, Object>();
        body.put("content", List.copyOf(items.subList(from, to)));
        body.put("page", pageInfo);
        return body;
    }

    /**
     * Returns the order of items by one field, with null values after all
     * others when ascending and before them when descending.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    static Comparator<Map<String, Object>> byField(String field,
            boolean descending) {
        Comparator<Map<String, Object>> comparator = (a, b) -> {
            var x = a.get(field);
            var y = b.get(field);
            if (x == null || y == null) {
                return x == null ? (y == null ? 0 : 1) : -1;
            }
            if (x instanceof Number m && y instanceof Number n) {
                return new BigDecimal(m.toString())
                        .compareTo(new BigDecimal(n.toString()));
            }
            if (x instanceof Comparable c
                    && x.getClass().equals(y.getClass())) {
                return c.compareTo(y);
            }
            return String.valueOf(x).compareTo(String.valueOf(y));
        };
        return descending ? comparator.reversed() : comparator;
    }

    private static int parseInt(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
