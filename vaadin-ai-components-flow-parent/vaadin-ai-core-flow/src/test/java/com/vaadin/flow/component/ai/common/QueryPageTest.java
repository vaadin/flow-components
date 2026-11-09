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
package com.vaadin.flow.component.ai.common;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

class QueryPageTest {

    @Test
    void negativeOffset_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new QueryPage(-1, 1, List.of()));
    }

    @Test
    void nonPositiveLimit_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new QueryPage(0, 0, List.of()));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new QueryPage(0, -1, List.of()));
    }

    @Test
    void nullSortOrders_becomeEmpty() {
        Assertions.assertEquals(List.of(),
                new QueryPage(0, 1, null).sortOrders());
    }

    @Test
    void sortOrders_areCopied() {
        var sortOrders = new ArrayList<QuerySortOrder>();
        sortOrders.add(new QuerySortOrder("a", SortDirection.ASCENDING));
        var page = new QueryPage(0, 1, sortOrders);

        sortOrders.clear();

        Assertions.assertEquals(1, page.sortOrders().size());
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> page.sortOrders().clear());
    }

    @Test
    void first_startsAtZeroInTheQuerysOwnOrder() {
        var page = QueryPage.first(25);

        Assertions.assertEquals(0, page.offset());
        Assertions.assertEquals(25, page.limit());
        Assertions.assertEquals(List.of(), page.sortOrders());
    }
}
