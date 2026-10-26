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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.common.QueryPage;
import com.vaadin.flow.component.ai.provider.QueryableProvider;
import com.vaadin.flow.component.ai.provider.ToolException;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

class GraphQLQueryableProviderTest {

    private final QueryableProvider provider = SampleProviders.get("graphql");

    @Test
    void language_rendersSelectionSetsWithAliases() {
        var language = provider.getQueryLanguage();
        Assertions.assertEquals("GraphQL", language.getName());
        Assertions.assertEquals("{ data { _x: temperature _y: sales } }",
                language.exampleQuery("data", List.of("temperature", "sales"),
                        List.of("_x", "_y")));
        Assertions.assertEquals(
                "{ TableName { Readable_A: ColumnA Readable_B: ColumnB } }",
                language.exampleQuery("TableName",
                        List.of("ColumnA", "ColumnB"),
                        List.of("Readable A", "Readable B")));
        Assertions.assertFalse(language.getRules().contains("SQL"));
    }

    @Test
    void schema_isTheIntrospectedSdlWithNotes() {
        var schema = provider.getSchema();
        Assertions.assertTrue(schema.contains("type Sale {"), schema);
        Assertions.assertTrue(schema.contains("sales(region: String)"), schema);
        Assertions.assertTrue(schema.contains("monthOrder"), schema);
        Assertions.assertTrue(schema.contains("NOTES:"), schema);
    }

    @Test
    void query_returnsAliasedRows() {
        var rows = provider.executeQuery(
                "{ sales(region: \"North\") { _name: month _y: revenue } }");
        Assertions.assertEquals(6, rows.size());
        Assertions.assertEquals(Map.of("_name", "January", "_y", 45000),
                rows.getFirst());
    }

    @Test
    void query_flattensNestedObjects() {
        var rows = provider.executeQuery(
                "{ projectTasks { taskName dependsOn { taskName } } }");
        Assertions.assertEquals(6, rows.size());
        Assertions.assertNull(rows.getFirst().get("dependsOn.taskName"));
        Assertions.assertEquals("Requirements",
                rows.get(1).get("dependsOn.taskName"));
    }

    @Test
    void query_withUnknownField_relaysTheValidationError() {
        var e = Assertions.assertThrows(ToolException.class, () -> provider
                .executeQuery("{ sales { revenue nonexistent } }"));
        Assertions.assertTrue(e.getMessage().contains("nonexistent"),
                e.getMessage());
    }

    @Test
    void paging_isDoneInMemoryAndCountIsUnknown() {
        var query = "{ sales(region: \"North\") { _name: month _y: revenue } }";
        var page = provider.executeQuery(query, new QueryPage(0, 2,
                List.of(new QuerySortOrder("_y", SortDirection.DESCENDING))));
        Assertions.assertEquals(2, page.size());
        Assertions.assertEquals("June", page.getFirst().get("_name"));
        Assertions.assertTrue(provider.count(query).isEmpty());
    }
}
