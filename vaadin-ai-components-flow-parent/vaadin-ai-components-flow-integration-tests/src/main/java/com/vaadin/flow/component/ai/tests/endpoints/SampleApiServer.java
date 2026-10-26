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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.vaadin.flow.internal.JacksonUtils;

/**
 * A small HTTP server on a loopback port that serves {@link SampleData} as a
 * REST API under {@code /api} and as a GraphQL API under {@code /graphql}, so
 * the web endpoints have a real network hop to make. Started on first use and
 * shared by every page of the application.
 */
public final class SampleApiServer {

    private static SampleApiServer instance;

    private final HttpServer server;

    private SampleApiServer() throws IOException {
        server = HttpServer.create(
                new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/api", new SampleRestApi());
        server.createContext("/graphql", new SampleGraphQLApi());
        server.setExecutor(Executors.newCachedThreadPool(runnable -> {
            var thread = new Thread(runnable, "sample-api-server");
            thread.setDaemon(true);
            return thread;
        }));
        server.start();
    }

    /**
     * Returns the running server, starting it on the first call.
     *
     * @return the server
     */
    public static synchronized SampleApiServer get() {
        if (instance == null) {
            try {
                instance = new SampleApiServer();
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Failed to start the sample API server", e);
            }
        }
        return instance;
    }

    /**
     * Returns the base URI of the REST API, the index listing the collections.
     *
     * @return the REST API URI
     */
    public URI restUri() {
        return baseUri().resolve("/api");
    }

    /**
     * Returns the URI of the GraphQL endpoint.
     *
     * @return the GraphQL URI
     */
    public URI graphQlUri() {
        return baseUri().resolve("/graphql");
    }

    private URI baseUri() {
        var address = server.getAddress();
        return URI.create(
                "http://" + address.getHostString() + ":" + address.getPort());
    }

    /**
     * Writes a JSON response and closes the exchange.
     *
     * @param exchange
     *            the exchange to respond to
     * @param status
     *            the HTTP status code
     * @param body
     *            the body, serialized as JSON
     * @throws IOException
     *             if writing fails
     */
    static void sendJson(HttpExchange exchange, int status, Object body)
            throws IOException {
        var bytes = JacksonUtils.getMapper().writeValueAsString(body)
                .getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type",
                "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    /**
     * Writes an error response with a JSON body of the form
     * {@code {"error": message}}.
     *
     * @param exchange
     *            the exchange to respond to
     * @param status
     *            the HTTP status code
     * @param message
     *            the error message
     * @throws IOException
     *             if writing fails
     */
    static void sendError(HttpExchange exchange, int status, String message)
            throws IOException {
        sendJson(exchange, status, Map.of("error", message));
    }

    /**
     * Parses the query string of a request into a map.
     *
     * @param exchange
     *            the request
     * @return the query parameters, empty if there are none
     */
    static Map<String, String> queryParameters(HttpExchange exchange) {
        var parameters = new LinkedHashMap<String, String>();
        var query = exchange.getRequestURI().getRawQuery();
        if (query == null || query.isEmpty()) {
            return parameters;
        }
        for (var pair : query.split("&")) {
            var separator = pair.indexOf('=');
            var name = separator < 0 ? pair : pair.substring(0, separator);
            var value = separator < 0 ? "" : pair.substring(separator + 1);
            parameters.put(URLDecoder.decode(name, StandardCharsets.UTF_8),
                    URLDecoder.decode(value, StandardCharsets.UTF_8));
        }
        return parameters;
    }
}
