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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Serves {@link SampleData} as a paged JSON REST API in the shape Spring Data
 * REST and Spring MVC give a {@code Page}: {@code GET /api} lists the
 * collections, {@code GET /api/{collection}?page=0&size=20} returns one page as
 * {@code {"content": [...], "page": {"size", "number", "totalElements",
 * "totalPages"}}}. Fields are camelCase, dates are ISO strings and decimals are
 * numbers, as a typical JSON API has them.
 */
final class SampleRestApi implements HttpHandler {

    static final int DEFAULT_PAGE_SIZE = 20;
    static final int MAX_PAGE_SIZE = 100;

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
        int page = parseInt(parameters.get("page"), 0);
        int size = Math.min(MAX_PAGE_SIZE, Math.max(1,
                parseInt(parameters.get("size"), DEFAULT_PAGE_SIZE)));
        SampleApiServer.sendJson(exchange, 200, page(table, page, size));
    }

    private static Map<String, Object> index() {
        var collections = new LinkedHashMap<String, String>();
        for (var table : SampleData.TABLES) {
            collections.put(table.resourceName(),
                    "/api/" + table.resourceName());
        }
        return Map.of("collections", collections);
    }

    private static Map<String, Object> page(SampleData.Table<?> table, int page,
            int size) {
        var rows = table.rows();
        int from = Math.min(rows.size(), page * size);
        int to = Math.min(rows.size(), from + size);
        var pageInfo = new LinkedHashMap<String, Object>();
        pageInfo.put("size", size);
        pageInfo.put("number", page);
        pageInfo.put("totalElements", rows.size());
        pageInfo.put("totalPages", (rows.size() + size - 1) / size);
        var body = new LinkedHashMap<String, Object>();
        body.put("content", List.copyOf(rows.subList(from, to)));
        body.put("page", pageInfo);
        return body;
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
