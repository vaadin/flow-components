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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.common.QueryPage;
import com.vaadin.flow.component.ai.provider.QueryableProvider;
import com.vaadin.flow.component.ai.provider.ToolException;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

class JpqlQueryableProviderTest {

    private final QueryableProvider provider = SampleProviders.get("jpql");

    @Test
    void language_rendersEntityQueries() {
        var language = provider.getQueryLanguage();
        Assertions.assertEquals("JPQL", language.getName());
        Assertions.assertEquals(
                "SELECT e.temperature AS _x, e.sales AS _y FROM data e",
                language.exampleQuery("data", List.of("temperature", "sales"),
                        List.of("_x", "_y")));
        Assertions.assertEquals(
                "SELECT e.ColumnA AS Readable_A, e.ColumnB AS Readable_B FROM TableName e",
                language.exampleQuery("TableName",
                        List.of("ColumnA", "ColumnB"),
                        List.of("Readable A", "Readable B")));
    }

    @Test
    void schema_describesEntitiesAndAssociations() {
        var schema = provider.getSchema();
        Assertions.assertTrue(schema.contains("Sale\n"), schema);
        Assertions.assertTrue(schema.contains("  - monthOrder: int"), schema);
        Assertions.assertTrue(
                schema.contains(
                        "  - dependsOn: ProjectTask (many-to-one association"),
                schema);
        Assertions.assertTrue(
                schema.contains("  - reports: collection of OrgMember"),
                schema);
        Assertions.assertTrue(schema.contains("  - id: Integer, id"), schema);
        Assertions.assertFalse(schema.contains("month_order"), schema);
    }

    @Test
    void query_aggregatesAndNavigates() {
        var totals = provider.executeQuery(
                "SELECT s.region AS region, SUM(s.revenue) AS total FROM Sale s GROUP BY s.region ORDER BY s.region");
        Assertions.assertEquals(2, totals.size());
        Assertions.assertEquals("North", totals.getFirst().get("region"));
        Assertions.assertEquals(338000L, totals.getFirst().get("total"));

        var dependencies = provider.executeQuery(
                "SELECT t.taskName AS task, t.dependsOn.taskName AS dependency FROM ProjectTask t WHERE t.dependsOn IS NOT NULL ORDER BY t.id");
        Assertions.assertEquals(4, dependencies.size());
        Assertions.assertEquals("Design", dependencies.getFirst().get("task"));
        Assertions.assertEquals("Requirements",
                dependencies.getFirst().get("dependency"));
    }

    @Test
    void query_expandsASelectedEntityIntoItsAttributes() {
        var rows = provider
                .executeQuery("SELECT e FROM Employee e ORDER BY e.id");
        Assertions.assertEquals(5, rows.size());
        Assertions.assertEquals(
                List.of("id", "name", "department", "salary", "age"),
                List.copyOf(rows.getFirst().keySet()));
        Assertions.assertEquals("John Doe", rows.getFirst().get("name"));
    }

    @Test
    void invalidQueries_areRelayedAsToolExceptions() {
        var e = Assertions.assertThrows(ToolException.class, () -> provider
                .executeQuery("SELECT e.nonexistent AS x FROM Employee e"));
        Assertions.assertTrue(e.getMessage().contains("nonexistent"),
                e.getMessage());
        Assertions.assertThrows(ToolException.class,
                () -> provider.executeQuery("DELETE FROM Sale s"));
    }

    @Test
    void paging_isDoneInMemory() {
        var page = provider.executeQuery(
                "SELECT e.name AS name, e.salary AS salary FROM Employee e",
                new QueryPage(0, 2, List.of(new QuerySortOrder("salary",
                        SortDirection.DESCENDING))));
        Assertions.assertEquals(List.of("Bob Johnson", "John Doe"),
                page.stream().map(row -> row.get("name")).toList());
    }
}
