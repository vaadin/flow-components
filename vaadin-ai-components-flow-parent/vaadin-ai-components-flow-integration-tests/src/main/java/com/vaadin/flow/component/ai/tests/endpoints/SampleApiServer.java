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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.vaadin.flow.internal.JacksonUtils;

/**
 * A small HTTP server on a loopback port that serves {@link SampleData} as a
 * REST API under {@code /api} and as a GraphQL API under {@code /graphql}, so
 * the web endpoints have a real network hop to make. Started on first use and
 * shared by every page of the application. Every call is logged at {@code INFO}
 * with what was asked and what came back.
 */
public final class SampleApiServer {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SampleApiServer.class);

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
     * Returns the address of the server that received a request, for example
     * {@code http://127.0.0.1:54321}, so that a logged call reads as a URL that
     * can be opened.
     *
     * @param exchange
     *            the request
     * @return the scheme, host and port of the server
     */
    static String serverUrl(HttpExchange exchange) {
        var address = exchange.getLocalAddress();
        return "http://" + address.getHostString() + ":" + address.getPort();
    }

    /**
     * Writes a JSON response, logs the call with the request method and the
     * full URL, and closes the exchange.
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
        sendJson(
                exchange, exchange.getRequestMethod() + " "
                        + serverUrl(exchange) + exchange.getRequestURI(),
                status, body);
    }

    /**
     * Writes a JSON response, logs the call with the given description of the
     * request, and closes the exchange.
     *
     * @param exchange
     *            the exchange to respond to
     * @param request
     *            what was asked, for the log
     * @param status
     *            the HTTP status code
     * @param body
     *            the body, serialized as JSON
     * @throws IOException
     *             if writing fails
     */
    static void sendJson(HttpExchange exchange, String request, int status,
            Object body) throws IOException {
        LOGGER.info("{} -> {}", request, describe(status, body));
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
     * Describes a response for the log: the error of a failed call, the number
     * of items of a REST page or index, or the number of items of each field of
     * a GraphQL result.
     */
    private static String describe(int status, Object body) {
        if (!(body instanceof Map<?, ?> map)) {
            return "HTTP " + status;
        }
        if (map.get("error") instanceof String error) {
            return "HTTP " + status + " " + error;
        }
        if (map.get("content") instanceof List<?> content
                && map.get("page") instanceof Map<?, ?> page) {
            return content.size() + " items of " + page.get("totalElements");
        }
        if (map.get("collections") instanceof Map<?, ?> collections) {
            return collections.size() + " collections";
        }
        if (map.get("errors") instanceof List<?> errors && !errors.isEmpty()) {
            return "error: " + (errors.get(0) instanceof Map<?, ?> error
                    ? error.get("message")
                    : errors.get(0));
        }
        if (map.get("data") instanceof Map<?, ?> data) {
            return describeFields(data);
        }
        return "HTTP " + status;
    }

    private static String describeFields(Map<?, ?> data) {
        var counts = new ArrayList<String>();
        for (var field : data.entrySet()) {
            counts.add(field.getKey() + ": "
                    + (field.getValue() instanceof List<?> items
                            ? items.size() + " items"
                            : "object"));
        }
        return String.join(", ", counts);
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
     * Returns every value of a query parameter that a request repeats, such as
     * {@code sort=a,asc&sort=b,desc}, in request order.
     *
     * @param exchange
     *            the request
     * @param name
     *            the parameter name
     * @return the values, empty if the parameter is absent
     */
    static List<String> queryParameterValues(HttpExchange exchange,
            String name) {
        var values = new ArrayList<String>();
        var query = exchange.getRequestURI().getRawQuery();
        if (query == null || query.isEmpty()) {
            return values;
        }
        for (var pair : query.split("&")) {
            var separator = pair.indexOf('=');
            var key = separator < 0 ? pair : pair.substring(0, separator);
            if (URLDecoder.decode(key, StandardCharsets.UTF_8).equals(name)) {
                values.add(separator < 0 ? ""
                        : URLDecoder.decode(pair.substring(separator + 1),
                                StandardCharsets.UTF_8));
            }
        }
        return values;
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
