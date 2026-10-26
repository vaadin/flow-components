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

import java.time.LocalDate;
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

class CsvQueryableProviderTest {

    private static final String SALES_BY_SALARY = """
            {"source": "employees", "columns": {"Name": "name", "Salary": "salary"},
             "filter": {"department": "Sales"}, "sort": [{"field": "salary", "direction": "desc"}]}""";

    private final QueryableProvider provider = SampleProviders.get("csv");

    @Test
    void language_speaksOfFiles() {
        var language = provider.getQueryLanguage();
        Assertions.assertEquals("CSV query", language.getName());
        Assertions.assertTrue(language.getRules().contains("file"));
        Assertions.assertTrue(language.getRules().contains("the CSV files"));
    }

    @Test
    void schema_listsFilesWithTypedColumns() {
        var schema = provider.getSchema();
        Assertions.assertTrue(schema.contains(
                "- employees: id (integer), name (text), department (text), salary (integer), age (integer)"),
                schema);
        Assertions.assertTrue(schema.contains("trade_date (date)"), schema);
        Assertions.assertTrue(schema.contains("price (number)"), schema);
    }

    @Test
    void query_filtersSortsAndTypesTheText() {
        var rows = provider.executeQuery(SALES_BY_SALARY);
        Assertions.assertEquals(
                List.of(Map.of("Name", "Charlie Brown", "Salary", 75000L),
                        Map.of("Name", "Jane Smith", "Salary", 72000L)),
                rows);
        var prices = provider.executeQuery(
                "{\"source\": \"stock_prices\", \"columns\": {\"day\": \"trade_date\"}, \"filter\": {\"id\": 1}}");
        Assertions.assertEquals(LocalDate.parse("2025-01-06"),
                prices.getFirst().get("day"));
    }

    @Test
    void paging_isInMemoryAndCountIsKnown() {
        var page = provider.executeQuery(SALES_BY_SALARY,
                new QueryPage(0, 1, List.of(new QuerySortOrder("Salary",
                        SortDirection.ASCENDING))));
        Assertions.assertEquals("Jane Smith", page.getFirst().get("Name"));
        Assertions.assertEquals(OptionalInt.of(2),
                provider.count(SALES_BY_SALARY));
    }

    @Test
    void unknownNames_areRelayedAsToolExceptions() {
        var e = Assertions.assertThrows(ToolException.class,
                () -> provider.executeQuery(
                        "{\"source\": \"../etc/passwd\", \"columns\": {\"a\": \"id\"}}"));
        Assertions.assertTrue(e.getMessage().contains("Unknown file"),
                e.getMessage());
        e = Assertions.assertThrows(ToolException.class,
                () -> provider.executeQuery(
                        "{\"source\": \"employees\", \"columns\": {\"a\": \"bonus\"}}"));
        Assertions.assertTrue(e.getMessage().contains("bonus"), e.getMessage());
    }
}
