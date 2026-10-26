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

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.common.QueryPage;
import com.vaadin.flow.component.ai.provider.QueryableProvider;
import com.vaadin.flow.component.ai.provider.ToolException;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

class RestQueryableProviderTest {

    private static final String NORTH_SALES = """
            {"source": "sales", "columns": {"_name": "month", "_y": "revenue"},
             "filter": {"region": "North"}, "sort": [{"field": "monthOrder", "direction": "asc"}]}""";

    private final QueryableProvider provider = SampleProviders.get("rest");

    @Test
    void language_describesOnlyWhatTheApiDoes() {
        var language = provider.getQueryLanguage();
        Assertions.assertEquals("REST query", language.getName());
        Assertions.assertEquals(
                "{\"source\":\"data\",\"columns\":{\"_x\":\"temperature\",\"_y\":\"sales\"}}",
                language.exampleQuery("data", List.of("temperature", "sales"),
                        List.of("_x", "_y")));
        Assertions.assertTrue(language.getRules().contains("no aggregation"),
                language.getRules());
        Assertions.assertTrue(language.getRules().contains("collection"));
    }

    @Test
    void schema_listsCollectionsWithSampledFields() {
        var schema = provider.getSchema();
        Assertions.assertTrue(schema.contains(
                "- sales: id (integer), month (string), monthOrder (integer), revenue (integer), region (string)"),
                schema);
        Assertions.assertTrue(schema.contains("tradeDate (date, ISO string)"),
                schema);
        Assertions.assertTrue(schema.contains("NOTES:"), schema);
    }

    @Test
    void query_filtersSortsAndRenamesThroughTheApi() {
        var rows = provider.executeQuery(NORTH_SALES);
        Assertions.assertEquals(6, rows.size());
        Assertions.assertEquals(Map.of("_name", "January", "_y", 45000),
                rows.getFirst());
        Assertions.assertEquals("June", rows.getLast().get("_name"));
    }

    @Test
    void query_rejectsFiltersNoItemCanEqual() {
        var comparison = Assertions.assertThrows(ToolException.class,
                () -> provider.executeQuery(
                        "{\"source\": \"products\", \"columns\": {\"Name\": \"productName\"}, \"filter\": {\"price\": \">100\"}}"));
        Assertions.assertTrue(comparison.getMessage().contains("equality"),
                comparison.getMessage());
        var text = Assertions.assertThrows(ToolException.class,
                () -> provider.executeQuery(
                        "{\"source\": \"products\", \"columns\": {\"Name\": \"productName\"}, \"filter\": {\"price\": \"cheap\"}}"));
        Assertions.assertTrue(text.getMessage().contains("number"),
                text.getMessage());
        Assertions.assertEquals(1, provider.executeQuery(
                "{\"source\": \"products\", \"columns\": {\"Name\": \"productName\"}, \"filter\": {\"price\": 899.99}}")
                .size());
    }

    @Test
    void paging_usesTheApisPagesAndSort() {
        var page = provider.executeQuery(NORTH_SALES, new QueryPage(1, 2,
                List.of(new QuerySortOrder("_y", SortDirection.DESCENDING))));
        Assertions.assertEquals(List.of("April", "March"),
                page.stream().map(row -> row.get("_name")).toList());
        Assertions.assertEquals(OptionalInt.of(6), provider.count(NORTH_SALES));
    }

    @Test
    void unknownNames_areRelayedAsToolExceptions() {
        var e = Assertions.assertThrows(ToolException.class,
                () -> provider.executeQuery(
                        "{\"source\": \"invoices\", \"columns\": {\"a\": \"id\"}}"));
        Assertions.assertTrue(e.getMessage().contains("invoices"),
                e.getMessage());
        e = Assertions.assertThrows(ToolException.class,
                () -> provider.executeQuery(
                        "{\"source\": \"sales\", \"columns\": {\"a\": \"total\"}}"));
        Assertions.assertTrue(e.getMessage().contains("total"), e.getMessage());
    }
}
