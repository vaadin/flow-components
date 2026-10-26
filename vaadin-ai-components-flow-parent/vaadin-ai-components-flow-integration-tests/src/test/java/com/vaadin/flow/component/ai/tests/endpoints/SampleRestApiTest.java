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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.internal.JacksonUtils;

class SampleRestApiTest {

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private final URI api = SampleApiServer.get().restUri();

    @Test
    @SuppressWarnings("unchecked")
    void indexListsEveryCollection() throws Exception {
        var index = get(api.toString());
        var collections = (Map<String, String>) index.body().get("collections");
        Assertions.assertEquals(SampleData.TABLES.size(), collections.size());
        Assertions.assertEquals("/api/stockPrices",
                collections.get("stockPrices"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void collectionsArePaged() throws Exception {
        var response = get(api + "/sales?size=5");
        var content = (List<Map<String, Object>>) response.body()
                .get("content");
        var page = (Map<String, Object>) response.body().get("page");
        Assertions.assertEquals(5, content.size());
        Assertions.assertEquals(12, page.get("totalElements"));
        Assertions.assertEquals(3, page.get("totalPages"));
        Assertions.assertEquals(0, page.get("number"));
        var lastPage = (List<?>) get(api + "/sales?size=5&page=2").body()
                .get("content");
        Assertions.assertEquals(2, lastPage.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void rowsAreCamelCaseJsonWithIsoDates() throws Exception {
        var content = (List<Map<String, Object>>) get(api + "/stockPrices")
                .body().get("content");
        var first = content.getFirst();
        Assertions.assertEquals("2025-01-06", first.get("tradeDate"));
        Assertions.assertEquals(142.5, first.get("openPrice"));
        Assertions.assertEquals(52000, first.get("volume"));
        var tasks = (List<Map<String, Object>>) get(api + "/projectTasks")
                .body().get("content");
        Assertions.assertTrue(tasks.getFirst().containsKey("dependsOn"));
        Assertions.assertNull(tasks.getFirst().get("dependsOn"));
        Assertions.assertEquals(1, tasks.get(1).get("dependsOn"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void collectionsFilterByFieldEquality() throws Exception {
        var response = get(api + "/sales?region=North");
        var content = (List<Map<String, Object>>) response.body()
                .get("content");
        Assertions.assertEquals(6, content.size());
        Assertions.assertTrue(content.stream()
                .allMatch(item -> "North".equals(item.get("region"))));
        var page = (Map<String, Object>) response.body().get("page");
        Assertions.assertEquals(6, page.get("totalElements"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void collectionsSortByOneField() throws Exception {
        var content = (List<Map<String, Object>>) get(
                api + "/employees?sort=salary,desc").body().get("content");
        Assertions.assertEquals("Bob Johnson", content.getFirst().get("name"));
        Assertions.assertEquals("Alice Williams",
                content.getLast().get("name"));
        var ascending = (List<Map<String, Object>>) get(
                api + "/employees?sort=salary,asc").body().get("content");
        Assertions.assertEquals("Alice Williams",
                ascending.getFirst().get("name"));
    }

    @Test
    void unknownFieldIs400() throws Exception {
        var response = get(api + "/sales?colour=red");
        Assertions.assertEquals(400, response.status());
        Assertions.assertTrue(
                response.body().get("error").toString().contains("colour"));
        Assertions.assertEquals(400,
                get(api + "/sales?sort=colour,asc").status());
    }

    @Test
    void unknownCollectionIs404() throws Exception {
        var response = get(api + "/invoices");
        Assertions.assertEquals(404, response.status());
        Assertions.assertTrue(
                response.body().get("error").toString().contains("invoices"));
    }

    record Response(int status, Map<String, Object> body) {
    }

    @SuppressWarnings("unchecked")
    static Response get(String uri) throws IOException, InterruptedException {
        var response = HTTP.send(
                HttpRequest.newBuilder(URI.create(uri)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        return new Response(response.statusCode(),
                JacksonUtils.getMapper().readValue(response.body(), Map.class));
    }
}
