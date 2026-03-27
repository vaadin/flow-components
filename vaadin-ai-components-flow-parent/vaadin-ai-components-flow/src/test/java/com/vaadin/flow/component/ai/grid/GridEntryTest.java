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
package com.vaadin.flow.component.ai.grid;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;

class GridEntryTest {

    private Grid<Map<String, Object>> grid;

    @BeforeEach
    void setUp() {
        grid = new Grid<>();
    }

    // --- Constructor ---

    @Test
    void constructor_nullId_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new GridEntry(null));
    }

    @Test
    void constructor_setsId() {
        var entry = new GridEntry("myGrid");
        Assertions.assertEquals("myGrid", entry.getId());
    }

    // --- get / getOrCreate ---

    @Test
    void get_noEntry_returnsNull() {
        Assertions.assertNull(GridEntry.get(grid));
    }

    @Test
    void getOrCreate_createsNewEntry() {
        var entry = GridEntry.getOrCreate(grid, "g1");
        Assertions.assertNotNull(entry);
        Assertions.assertEquals("g1", entry.getId());
    }

    @Test
    void getOrCreate_returnsSameEntry() {
        var first = GridEntry.getOrCreate(grid, "g1");
        var second = GridEntry.getOrCreate(grid, "g1");
        Assertions.assertSame(first, second);
    }

    @Test
    void get_afterGetOrCreate_returnsSameEntry() {
        var created = GridEntry.getOrCreate(grid, "g1");
        var retrieved = GridEntry.get(grid);
        Assertions.assertSame(created, retrieved);
    }

    // --- Pending state ---

    @Test
    void hasPendingState_initiallyFalse() {
        var entry = new GridEntry("g1");
        Assertions.assertFalse(entry.hasPendingState());
    }

    @Test
    void hasPendingState_trueAfterSetPendingQuery() {
        var entry = new GridEntry("g1");
        entry.setPendingQuery("SELECT 1");
        Assertions.assertTrue(entry.hasPendingState());
    }

    @Test
    void clearPendingState_resetsPendingQuery() {
        var entry = new GridEntry("g1");
        entry.setPendingQuery("SELECT 1");
        entry.clearPendingState();
        Assertions.assertFalse(entry.hasPendingState());
        Assertions.assertNull(entry.getPendingQuery());
    }

    @Test
    void clearPendingState_doesNotAffectCurrentQuery() {
        var entry = new GridEntry("g1");
        entry.setCurrentQuery("SELECT a FROM t");
        entry.setPendingQuery("SELECT b FROM t");
        entry.clearPendingState();
        Assertions.assertEquals("SELECT a FROM t", entry.getCurrentQuery());
    }

    // --- Current query ---

    @Test
    void currentQuery_initiallyNull() {
        var entry = new GridEntry("g1");
        Assertions.assertNull(entry.getCurrentQuery());
    }

    @Test
    void setCurrentQuery_storesQuery() {
        var entry = new GridEntry("g1");
        entry.setCurrentQuery("SELECT 1");
        Assertions.assertEquals("SELECT 1", entry.getCurrentQuery());
    }

    // --- getStateAsJson ---

    @Nested
    class GetStateAsJsonTests {

        @Test
        void noEntry_returnsEmptyStatus() {
            var node = parseState("g1");
            Assertions.assertEquals("g1", node.get("gridId").stringValue());
            Assertions.assertEquals("empty", node.get("status").stringValue());
            Assertions.assertTrue(node.has("message"));
        }

        @Test
        void entryWithNoCurrentQuery_returnsEmptyStatus() {
            GridEntry.getOrCreate(grid, "g1");
            var node = parseState("g1");
            Assertions.assertEquals("empty", node.get("status").stringValue());
        }

        @Test
        void entryWithCurrentQuery_returnsQuery() {
            var entry = GridEntry.getOrCreate(grid, "g1");
            entry.setCurrentQuery("SELECT a FROM t");
            var node = parseState("g1");
            Assertions.assertEquals("SELECT a FROM t",
                    node.get("query").stringValue());
            Assertions.assertFalse(node.has("status"));
        }

        @Test
        void includesGridId() {
            var node = parseState("myGrid");
            Assertions.assertEquals("myGrid", node.get("gridId").stringValue());
        }

        private JsonNode parseState(String gridId) {
            return JacksonUtils
                    .readTree(GridEntry.getStateAsJson(grid, gridId));
        }
    }
}
