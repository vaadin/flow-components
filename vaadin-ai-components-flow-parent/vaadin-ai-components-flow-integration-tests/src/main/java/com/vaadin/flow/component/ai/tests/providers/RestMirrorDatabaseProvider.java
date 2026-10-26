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
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.ai.tests.endpoints.SampleData;
import com.vaadin.flow.internal.JacksonUtils;

/**
 * A provider for an application whose data is only reachable through a REST API
 * with no query language of its own. The provider downloads every collection of
 * the API into a {@link SqlMirror} on first use and lets the LLM query the copy
 * with SQL.
 * <p>
 * Expects the API shape of the sample REST API: an index listing the
 * collections and paged collections with a {@code content} array.
 * </p>
 */
public class RestMirrorDatabaseProvider extends MirrorDatabaseProvider {

    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final int PAGE_SIZE = 100;

    private final URI apiUri;

    /**
     * Creates a provider for the REST API at the given URI.
     *
     * @param apiUri
     *            the URI of the API index listing the collections
     */
    public RestMirrorDatabaseProvider(URI apiUri) {
        this.apiUri = apiUri;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected SqlMirror copy() {
        var index = get(apiUri);
        var collections = (Map<String, String>) index.get("collections");
        var mirror = SqlMirror.create(
                "rest_mirror_" + Integer.toHexString(apiUri.hashCode()), false);
        collections.forEach((name, href) -> mirror.load(name,
                fetchAll(apiUri.resolve(href))));
        return mirror;
    }

    @Override
    protected String notes() {
        return """
                - This database is a snapshot of the REST API at %s. Each collection is a table and each JSON field a column, renamed to upper snake_case (monthOrder -> MONTH_ORDER).
                """
                .formatted(apiUri) + SampleData.NOTES;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchAll(URI collectionUri) {
        var rows = new ArrayList<Map<String, Object>>();
        int page = 0;
        int totalPages;
        do {
            var body = get(URI.create(
                    collectionUri + "?page=" + page + "&size=" + PAGE_SIZE));
            rows.addAll((List<Map<String, Object>>) body.get("content"));
            totalPages = ((Number) ((Map<String, Object>) body.get("page"))
                    .get("totalPages")).intValue();
            page++;
        } while (page < totalPages);
        return rows;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> get(URI uri) {
        try {
            var response = HTTP.send(HttpRequest.newBuilder(uri).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "GET " + uri + " returned " + response.statusCode());
            }
            return JacksonUtils.getMapper().readValue(response.body(),
                    Map.class);
        } catch (IOException e) {
            throw new IllegalStateException("GET " + uri + " failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("GET " + uri + " interrupted", e);
        }
    }
}
