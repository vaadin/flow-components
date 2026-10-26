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
import java.util.OptionalInt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.common.QueryPage;
import com.vaadin.flow.component.ai.provider.QueryableProvider;
import com.vaadin.flow.component.ai.provider.ToolException;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

class MongoAggregationQueryableProviderTest {

    private static final String EMPLOYEES_BY_SALARY = """
            {"collection": "employees", "pipeline": [{"$project": {"_id": 0, "name": 1, "salary": 1}}]}""";

    private final QueryableProvider provider = SampleProviders.get("mongo");

    @Test
    void language_rendersProjectionPipelines() {
        var language = provider.getQueryLanguage();
        Assertions.assertEquals("MongoDB aggregation pipeline",
                language.getName());
        Assertions.assertEquals(
                "{\"collection\": \"data\", \"pipeline\": [{\"$project\": {\"_id\": 0, \"_x\": \"$temperature\", \"_y\": \"$sales\"}}]}",
                language.exampleQuery("data", List.of("temperature", "sales"),
                        List.of("_x", "_y")));
        Assertions.assertTrue(language.getRules().contains("$out"));
    }

    @Test
    void schema_listsCollectionsWithSampledFields() {
        var schema = provider.getSchema();
        Assertions.assertTrue(schema.contains(
                "- sales: _id (int), month (string), monthOrder (int), revenue (int), region (string)"),
                schema);
        Assertions.assertTrue(schema.contains("prices.open (double)"), schema);
        Assertions.assertTrue(schema.contains("NOTES:"), schema);
    }

    @Test
    void query_runsThePipeline() {
        var rows = provider.executeQuery("""
                {"collection": "sales", "pipeline": [
                  {"$group": {"_id": "$region", "total": {"$sum": "$revenue"}}},
                  {"$project": {"_id": 0, "_name": "$_id", "_y": "$total"}},
                  {"$sort": {"_name": 1}}]}""");
        Assertions.assertEquals(2, rows.size());
        Assertions.assertEquals("North", rows.getFirst().get("_name"));
        Assertions.assertEquals(338000, rows.getFirst().get("_y"));
    }

    @Test
    void query_flattensSubdocuments() {
        var rows = provider.executeQuery(
                """
                        {"collection": "stockPrices", "pipeline": [{"$match": {"_id": 1}}]}""");
        Assertions.assertEquals(1, rows.size());
        Assertions.assertEquals(142.5, rows.getFirst().get("prices.open"));
        Assertions.assertEquals(java.time.LocalDate.parse("2025-01-06"),
                rows.getFirst().get("tradeDate"));
    }

    @Test
    void paging_appendsSortSkipAndLimitStages() {
        var page = provider.executeQuery(EMPLOYEES_BY_SALARY,
                new QueryPage(1, 2, List.of(new QuerySortOrder("salary",
                        SortDirection.DESCENDING))));
        Assertions.assertEquals(2, page.size());
        Assertions.assertEquals("John Doe", page.get(0).get("name"));
        Assertions.assertEquals("Charlie Brown", page.get(1).get("name"));
    }

    @Test
    void count_appendsACountStage() {
        Assertions.assertEquals(OptionalInt.of(5),
                provider.count(EMPLOYEES_BY_SALARY));
    }

    @Test
    void invalidQueries_areRelayedAsToolExceptions() {
        Assertions.assertThrows(ToolException.class,
                () -> provider.executeQuery("not json"));
        Assertions.assertThrows(ToolException.class, () -> provider
                .executeQuery("{\"collection\": \"nope\", \"pipeline\": []}"));
        var e = Assertions.assertThrows(ToolException.class,
                () -> provider.executeQuery(
                        "{\"collection\": \"sales\", \"pipeline\": [{\"$out\": \"evil\"}]}"));
        Assertions.assertTrue(e.getMessage().contains("$out"), e.getMessage());
    }
}
