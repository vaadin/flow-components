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
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.internal.JacksonUtils;

import graphql.introspection.IntrospectionQuery;

class SampleGraphQLApiTest {

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private final URI endpoint = SampleApiServer.get().graphQlUri();

    @Test
    @SuppressWarnings("unchecked")
    void filtersByArgumentAndHonorsAliases() throws Exception {
        var result = post(
                "{ sales(region: \"North\") { _name: month _y: revenue } }");
        Assertions.assertNull(result.get("errors"));
        var sales = (List<Map<String, Object>>) data(result).get("sales");
        Assertions.assertEquals(6, sales.size());
        Assertions.assertEquals(Map.of("_name", "January", "_y", 45000),
                sales.getFirst());
    }

    @Test
    @SuppressWarnings("unchecked")
    void resolvesRelations() throws Exception {
        var tasks = (List<Map<String, Object>>) data(post(
                "{ projectTasks(phase: \"Planning\") { taskName dependsOn { taskName } } }"))
                .get("projectTasks");
        Assertions.assertEquals(2, tasks.size());
        Assertions.assertNull(tasks.getFirst().get("dependsOn"));
        Assertions.assertEquals(Map.of("taskName", "Requirements"),
                tasks.get(1).get("dependsOn"));

        var members = (List<Map<String, Object>>) data(post(
                "{ orgMembers { empName manager { empName } reports { empName } } }"))
                .get("orgMembers");
        var ceo = members.getFirst();
        Assertions.assertNull(ceo.get("manager"));
        Assertions.assertEquals(2, ((List<?>) ceo.get("reports")).size());
        Assertions.assertEquals(Map.of("empName", "Sarah CEO"),
                members.get(1).get("manager"));

        var categories = (List<Map<String, Object>>) data(post(
                "{ expenseCategories(parent: \"Marketing\") { categoryName amount parent { categoryName } } }"))
                .get("expenseCategories");
        Assertions.assertEquals(3, categories.size());
        Assertions.assertEquals(18000.0, categories.getFirst().get("amount"));
        Assertions.assertEquals(Map.of("categoryName", "Marketing"),
                categories.getFirst().get("parent"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void datesAreIsoStrings() throws Exception {
        var prices = (List<Map<String, Object>>) data(
                post("{ stockPrices { tradeDate closePrice } }"))
                .get("stockPrices");
        Assertions.assertEquals(10, prices.size());
        Assertions.assertEquals("2025-01-06",
                prices.getFirst().get("tradeDate"));
        Assertions.assertEquals(147.8, prices.getFirst().get("closePrice"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void listsSortAndPage() throws Exception {
        var sales = (List<Map<String, Object>>) data(post(
                "{ sales(region: \"North\", orderBy: [{field: \"revenue\", direction: DESC}], offset: 1, limit: 2) { revenue } }"))
                .get("sales");
        var expected = SampleData.SALES.stream()
                .filter(sale -> sale.region().equals("North"))
                .map(SampleData.Sale::revenue)
                .sorted((a, b) -> Integer.compare(b, a)).skip(1).limit(2)
                .toList();
        Assertions.assertEquals(expected,
                sales.stream().map(sale -> sale.get("revenue")).toList());
    }

    @Test
    @SuppressWarnings("unchecked")
    void listsSortByAFieldThenAnother_withNullValuesLastWhenAscending()
            throws Exception {
        var items = (List<Map<String, Object>>) data(post(
                "{ budgetItems(orderBy: [{field: \"itemType\"}, {field: \"amount\", direction: DESC}]) { itemName itemType } }"))
                .get("budgetItems");
        Assertions.assertEquals("intermediate",
                items.getFirst().get("itemType"));
        Assertions.assertEquals("Revenue", items.get(2).get("itemName"));
        Assertions.assertNull(items.getLast().get("itemType"));
    }

    @Test
    void countsHonorTheFilterArgument() throws Exception {
        var counts = data(post(
                "{ all: salesCount north: salesCount(region: \"North\") }"));
        Assertions.assertEquals(SampleData.SALES.size(), counts.get("all"));
        Assertions.assertEquals(6, counts.get("north"));
    }

    @Test
    void unknownSortFieldIsReported() throws Exception {
        var result = post(
                "{ projectTasks(orderBy: [{field: \"dependsOn\"}]) { taskName } }");
        var errors = (List<?>) result.get("errors");
        Assertions.assertTrue(((Map<?, ?>) errors.getFirst()).get("message")
                .toString().contains("dependsOn"));
    }

    @Test
    void validationErrorsAreReported() throws Exception {
        var result = post("{ sales { revenue nonexistent } }");
        var errors = (List<?>) result.get("errors");
        Assertions.assertEquals(1, errors.size());
        Assertions.assertTrue(((Map<?, ?>) errors.getFirst()).get("message")
                .toString().contains("nonexistent"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void supportsIntrospection() throws Exception {
        var schema = (Map<String, Object>) data(
                post(IntrospectionQuery.INTROSPECTION_QUERY)).get("__schema");
        var types = (List<Map<String, Object>>) schema.get("types");
        Assertions.assertTrue(types.stream()
                .anyMatch(type -> "KpiMetric".equals(type.get("name"))));
    }

    @Test
    void supportsGetRequests() throws Exception {
        var response = HTTP.send(
                HttpRequest.newBuilder(URI.create(endpoint + "?query="
                        + URLEncoder.encode("{ kpiMetrics { metricName } }",
                                StandardCharsets.UTF_8)))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        Assertions
                .assertTrue(response.body().contains("Customer Satisfaction"));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> data(Map<String, Object> result) {
        Assertions.assertNull(result.get("errors"), result::toString);
        return (Map<String, Object>) result.get("data");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String query)
            throws IOException, InterruptedException {
        var body = JacksonUtils.getMapper()
                .writeValueAsString(Map.of("query", query));
        var response = HTTP.send(HttpRequest.newBuilder(endpoint)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        return JacksonUtils.getMapper().readValue(response.body(), Map.class);
    }
}
