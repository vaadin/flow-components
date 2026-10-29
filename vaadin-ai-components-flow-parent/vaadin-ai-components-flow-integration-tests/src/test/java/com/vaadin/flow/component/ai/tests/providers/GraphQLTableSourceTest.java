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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.provider.TableSource.Capability;
import com.vaadin.flow.component.ai.provider.TableSource.Column;
import com.vaadin.flow.component.ai.provider.TableSource.Pushdown;
import com.vaadin.flow.component.ai.provider.TableSource.SortKey;
import com.vaadin.flow.component.ai.tests.endpoints.SampleApiServer;
import com.vaadin.flow.component.ai.tests.endpoints.SampleData;

class GraphQLTableSourceTest {

    private static final List<GraphQLTableSource> SOURCES = GraphQLTableSource
            .discover(SampleApiServer.get().graphQlUri());

    private static GraphQLTableSource source(String field) {
        return SOURCES.stream().filter(source -> source.getName().equals(field))
                .findFirst().orElseThrow();
    }

    private static Map<String, Class<?>> columnTypes(String field) {
        return source(field).getColumns().stream()
                .collect(Collectors.toMap(Column::name, Column::type));
    }

    @Test
    void discover_createsOneSourcePerListField() {
        Assertions.assertEquals(SampleData.TABLES.size(), SOURCES.size());
        Assertions.assertEquals("GraphQL field employees",
                source("employees").getDescription());
    }

    @Test
    void columns_comeFromTheObjectType_withObjectFieldsAsIds() {
        Assertions.assertEquals(
                List.of(new Column("id", Integer.class),
                        new Column("name", String.class),
                        new Column("department", String.class),
                        new Column("salary", Integer.class),
                        new Column("age", Integer.class)),
                source("employees").getColumns());

        var tasks = columnTypes("projectTasks");
        Assertions.assertEquals(Integer.class, tasks.get("dependsOnId"));
        Assertions.assertFalse(tasks.containsKey("dependsOn"));
        Assertions.assertEquals(LocalDate.class, tasks.get("startDate"));
        Assertions.assertEquals(LocalDate.class,
                columnTypes("stockPrices").get("tradeDate"));
        Assertions
                .assertFalse(columnTypes("orgMembers").containsKey("reports"));
    }

    @Test
    void arguments_matchingAColumn_becomeEqualityFilters() {
        Assertions.assertEquals(Set.of("department"),
                source("employees").getArguments());
        Assertions.assertEquals(
                Set.of(Capability.EQUALITY_FILTER, Capability.SORT,
                        Capability.LIMIT, Capability.COUNT),
                source("employees").getCapabilities());
        Assertions.assertTrue(
                source("employees").supportsEqualityFilter("department"));
        Assertions.assertFalse(
                source("employees").supportsEqualityFilter("name"));
        Assertions.assertEquals(Set.of(), source("kpiMetrics").getArguments());
        Assertions.assertEquals(
                Set.of(Capability.SORT, Capability.LIMIT, Capability.COUNT),
                source("kpiMetrics").getCapabilities());
        // expenseCategories(parent: String) filters by name, not by the
        // parentId column, so it is not an equality filter on a column
        Assertions.assertEquals(Set.of(),
                source("expenseCategories").getArguments());
    }

    @Test
    void fetch_returnsTypedRows() {
        var rows = source("projectTasks").fetch(Pushdown.NONE);

        Assertions.assertEquals(SampleData.PROJECT_TASKS.size(), rows.size());
        var first = SampleData.PROJECT_TASKS.get(0);
        Assertions.assertEquals(first.taskName(), rows.get(0).get("taskName"));
        Assertions.assertEquals(first.startDate(),
                rows.get(0).get("startDate"));
        Assertions.assertEquals(first.dependsOn(),
                rows.get(0).get("dependsOnId"));
        Assertions.assertTrue(
                rows.stream().anyMatch(row -> row.get("dependsOnId") != null));
    }

    @Test
    void fetch_pushesAnArgumentFilterAndIgnoresOthers() {
        var engineering = source("employees")
                .fetch(new Pushdown(Map.of("department", "Engineering")));
        var byName = source("employees")
                .fetch(new Pushdown(Map.of("name", "Jane Smith")));

        var expected = SampleData.EMPLOYEES.stream()
                .filter(employee -> employee.department().equals("Engineering"))
                .count();
        Assertions.assertEquals(expected, engineering.size());
        Assertions.assertTrue(expected < SampleData.EMPLOYEES.size());
        Assertions.assertEquals(SampleData.EMPLOYEES.size(), byName.size());
    }

    @Test
    void fetch_pushesTheSortAndThePageAsArguments() {
        var rows = source("employees")
                .fetch(new Pushdown(Map.of("department", "Engineering"),
                        List.of(new SortKey("salary", true)), 1, 1L));

        var expected = SampleData.EMPLOYEES.stream()
                .filter(employee -> employee.department().equals("Engineering"))
                .sorted((a, b) -> Integer.compare(b.salary(), a.salary()))
                .skip(1).map(SampleData.Employee::name).findFirst()
                .orElseThrow();
        Assertions.assertEquals(List.of(expected),
                rows.stream().map(row -> row.get("name")).toList());
    }

    @Test
    void sort_byAnIdColumnOfAnObjectField_isNotSupported() {
        Assertions.assertTrue(source("projectTasks")
                .supportsSort(List.of(new SortKey("taskName", false))));
        Assertions.assertFalse(source("projectTasks")
                .supportsSort(List.of(new SortKey("dependsOnId", false))));
    }

    @Test
    void count_readsTheCountField() {
        Assertions.assertEquals(SampleData.EMPLOYEES.size(),
                source("employees").count(Pushdown.NONE));
        Assertions.assertEquals(
                SampleData.EMPLOYEES.stream()
                        .filter(employee -> employee.department()
                                .equals("Sales"))
                        .count(),
                source("employees")
                        .count(new Pushdown(Map.of("department", "Sales"))));
    }

    @Test
    void query_rendersArgumentsAndSelections() {
        var selections = new LinkedHashMap<String, String>();
        selections.put("id", "id");
        selections.put("name", "name");
        selections.put("managerId", "manager { id }");

        var query = GraphQLTableSource.query("employees",
                Map.of("department", "R&D"), selections);

        Assertions.assertEquals(
                "{ employees(department: \"R&D\") { id name manager { id } } }",
                query);
    }
}
