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

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.vaadin.flow.component.ai.common.QueryLanguage;
import com.vaadin.flow.component.ai.common.QueryPage;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.ToolException;
import com.vaadin.flow.component.ai.tests.endpoints.SampleData;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

/**
 * The SQL-speaking providers answer the same queries with the same rows,
 * whatever the endpoint behind them.
 */
class SqlProvidersParityTest {

    private static final Set<String> LOWER_CASE_SCHEMAS = Set.of("postgresql",
            "mysql");

    /** Providers whose nested source has no flat stock price columns. */
    private static final Set<String> NO_STOCK_PRICES = Set.of("mongo-mirror");

    @ParameterizedTest
    @ValueSource(strings = { "h2", "postgresql", "mysql", "rest-mirror",
            "csv-mirror", "jpa-native", "mongo-mirror" })
    void returnsTheSampleRows(String name) {
        var provider = (DatabaseProvider) SampleProviders.get(name);
        Assertions.assertTrue(
                provider.getQueryLanguage().getName().contains("SQL")
                        || provider.getQueryLanguage()
                                .exampleQuery("t", List.of("a"), List.of("b"))
                                .startsWith("SELECT"),
                name);

        Assertions.assertEquals(normalize(SampleData.EMPLOYEES),
                normalize(provider.executeQuery(
                        "SELECT id, name, department, salary, age FROM employees ORDER BY id")),
                name);
        var month = LOWER_CASE_SCHEMAS.contains(name) ? "month" : "\"MONTH\"";
        Assertions.assertEquals(normalize(SampleData.SALES),
                normalize(provider.executeQuery("SELECT id, " + month
                        + " AS \"month\", month_order, revenue, region FROM sales ORDER BY id")),
                name);
        Assertions.assertEquals(normalize(SampleData.PRODUCTS),
                normalize(provider.executeQuery(
                        "SELECT id, product_name, category, units_sold, price FROM products ORDER BY id")),
                name);
        Assertions.assertEquals(normalize(SampleData.PROJECT_TASKS),
                normalize(provider.executeQuery(
                        "SELECT id, task_name, phase, start_date, end_date, progress, depends_on FROM project_tasks ORDER BY id")),
                name);
        if (!NO_STOCK_PRICES.contains(name)) {
            Assertions.assertEquals(normalize(SampleData.STOCK_PRICES),
                    normalize(provider.executeQuery(
                            "SELECT id, trade_date, ticker, open_price, high_price, low_price, close_price, volume FROM stock_prices ORDER BY id")),
                    name);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "h2", "postgresql", "mysql", "rest-mirror",
            "csv-mirror", "jpa-native", "mongo-mirror" })
    void aggregatesPagesAndCounts(String name) {
        var provider = (DatabaseProvider) SampleProviders.get(name);
        var totals = normalize(provider.executeQuery(
                "SELECT region, SUM(revenue) AS total_revenue FROM sales GROUP BY region ORDER BY region"));
        Assertions.assertEquals(
                List.of(Map.of("region", "North", "total_revenue", "338000"),
                        Map.of("region", "South", "total_revenue", "287000")),
                totals, name);

        var query = "SELECT id, salary FROM employees";
        var all = provider.executeQuery(query);
        var salaryLabel = all.getFirst().keySet().stream()
                .filter(key -> key.equalsIgnoreCase("salary")).findFirst()
                .orElseThrow();
        var page = provider.executeQuery(query, new QueryPage(1, 2, List.of(
                new QuerySortOrder(salaryLabel, SortDirection.DESCENDING))));
        Assertions.assertEquals(List.of("85000", "75000"),
                normalize(page).stream().map(row -> row.get("salary")).toList(),
                name);
        Assertions.assertEquals(OptionalInt.of(5), provider.count(query), name);
    }

    @ParameterizedTest
    @ValueSource(strings = { "h2", "postgresql", "mysql", "rest-mirror",
            "csv-mirror", "jpa-native", "mongo-mirror" })
    void rejectsWrites(String name) {
        var provider = (DatabaseProvider) SampleProviders.get(name);
        Assertions.assertThrows(ToolException.class,
                () -> provider.executeQuery("DELETE FROM employees"), name);
        Assertions.assertEquals(5,
                provider.executeQuery("SELECT id FROM employees").size());
    }

    @ParameterizedTest
    @ValueSource(strings = { "h2", "postgresql", "mysql", "rest-mirror",
            "csv-mirror", "jpa-native", "mongo-mirror" })
    void describesEveryTable(String name) {
        var schema = SampleProviders.get(name).getSchema();
        for (var table : SampleData.TABLES) {
            Assertions.assertTrue(
                    schema.toLowerCase(Locale.ROOT)
                            .contains(table.sqlName().toLowerCase(Locale.ROOT)),
                    name + " schema lacks " + table.sqlName());
        }
        if (SampleProviders.get(name).getQueryLanguage() == QueryLanguage.SQL) {
            Assertions.assertTrue(schema.contains("month_order")
                    || schema.contains("MONTH_ORDER"), name);
        }
    }

    private static List<Map<String, String>> normalize(
            List<? extends Object> rows) {
        var normalized = new ArrayList<Map<String, String>>();
        for (var row : rows) {
            // Result rows carry snake_case column labels in the case of the
            // database; sample records carry camelCase component names
            var fromRecord = !(row instanceof Map<?, ?>);
            var map = row instanceof Map<?, ?> m ? m
                    : SampleData.toMap((Record) row);
            var out = new LinkedHashMap<String, String>();
            map.forEach((key, value) -> out.put(
                    (fromRecord ? SampleData.toSnakeCase(String.valueOf(key))
                            : String.valueOf(key)).toLowerCase(Locale.ROOT),
                    normalize(value)));
            normalized.add(out);
        }
        return normalized;
    }

    private static String normalize(Object value) {
        return switch (value) {
        case null -> null;
        case Number number -> new BigDecimal(number.toString())
                .stripTrailingZeros().toPlainString();
        case Date date -> date.toLocalDate().toString();
        case LocalDate date -> date.toString();
        default -> String.valueOf(value);
        };
    }
}
