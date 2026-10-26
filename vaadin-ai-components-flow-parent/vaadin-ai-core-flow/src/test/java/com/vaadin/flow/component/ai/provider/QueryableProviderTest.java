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
package com.vaadin.flow.component.ai.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.common.QueryLanguage;
import com.vaadin.flow.component.ai.common.QueryPage;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

class QueryableProviderTest {

    private final List<String> executed = new ArrayList<>();

    private final QueryableProvider provider = new QueryableProvider() {
        @Override
        public QueryLanguage getQueryLanguage() {
            return QueryLanguage.SQL;
        }

        @Override
        public String getSchema() {
            return "schema";
        }

        @Override
        public List<Map<String, Object>> executeQuery(String query) {
            executed.add(query);
            return List.of(Map.of("v", 2), Map.of("v", 1), Map.of("v", 3));
        }
    };

    @Test
    void pagedExecute_runsTheQueryUnchangedAndPagesInMemory() {
        var page = new QueryPage(1, 1,
                List.of(new QuerySortOrder("v", SortDirection.ASCENDING)));
        var rows = provider.executeQuery("{ items { v } }", page);

        Assertions.assertEquals(List.of("{ items { v } }"), executed);
        Assertions.assertEquals(List.of(Map.of("v", 2)), rows);
    }

    @Test
    void count_isUnknownByDefault() {
        Assertions.assertTrue(provider.count("{ items { v } }").isEmpty());
        Assertions.assertTrue(executed.isEmpty());
    }
}
