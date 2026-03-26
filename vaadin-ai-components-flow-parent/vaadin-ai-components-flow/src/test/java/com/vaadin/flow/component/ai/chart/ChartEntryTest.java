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
package com.vaadin.flow.component.ai.chart;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.tests.MockUIExtension;

class ChartEntryTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Chart chart;

    @BeforeEach
    void setUp() {
        chart = new Chart();
        ui.add(chart);
    }

    @Nested
    class GetAndGetOrCreate {

        @Test
        void get_returnsNullWhenNoEntry() {
            Assertions.assertNull(ChartEntry.get(chart));
        }

        @Test
        void getOrCreate_createsNewEntry() {
            ChartEntry entry = ChartEntry.getOrCreate(chart, "test-id");
            Assertions.assertNotNull(entry);
            Assertions.assertEquals("test-id", entry.getId());
        }

        @Test
        void getOrCreate_returnsSameEntry() {
            ChartEntry first = ChartEntry.getOrCreate(chart, "id-1");
            ChartEntry second = ChartEntry.getOrCreate(chart, "id-2");
            Assertions.assertSame(first, second);
            Assertions.assertEquals("id-1", second.getId());
        }

        @Test
        void get_returnsEntryAfterGetOrCreate() {
            ChartEntry created = ChartEntry.getOrCreate(chart, "my-id");
            ChartEntry retrieved = ChartEntry.get(chart);
            Assertions.assertSame(created, retrieved);
        }

        @Test
        void constructor_nullId_throws() {
            Assertions.assertThrows(NullPointerException.class,
                    () -> new ChartEntry(null));
        }
    }

    @Nested
    class PendingState {

        private ChartEntry entry;

        @BeforeEach
        void setUp() {
            entry = ChartEntry.getOrCreate(chart, "test");
        }

        @Test
        void noPendingStateByDefault() {
            Assertions.assertFalse(entry.hasPendingState());
            Assertions.assertNull(entry.getPendingConfigurationJson());
            Assertions.assertFalse(entry.isPendingDataUpdate());
        }

        @Test
        void pendingConfigurationJson_makesPending() {
            entry.setPendingConfigurationJson("{\"type\":\"bar\"}");
            Assertions.assertTrue(entry.hasPendingState());
            Assertions.assertEquals("{\"type\":\"bar\"}",
                    entry.getPendingConfigurationJson());
        }

        @Test
        void pendingDataUpdate_makesPending() {
            entry.setPendingDataUpdate(true);
            Assertions.assertTrue(entry.hasPendingState());
        }

        @Test
        void clearPendingState_resetsBoth() {
            entry.setPendingConfigurationJson("{\"type\":\"bar\"}");
            entry.setPendingDataUpdate(true);
            entry.clearPendingState();

            Assertions.assertFalse(entry.hasPendingState());
            Assertions.assertNull(entry.getPendingConfigurationJson());
            Assertions.assertFalse(entry.isPendingDataUpdate());
        }
    }

    @Nested
    class Queries {

        private ChartEntry entry;

        @BeforeEach
        void setUp() {
            entry = ChartEntry.getOrCreate(chart, "test");
        }

        @Test
        void emptyByDefault() {
            Assertions.assertTrue(entry.getQueries().isEmpty());
        }

        @Test
        void setQueries_returnsUnmodifiableCopy() {
            entry.setQueries(List.of("SELECT 1", "SELECT 2"));
            Assertions.assertEquals(List.of("SELECT 1", "SELECT 2"),
                    entry.getQueries());
            Assertions.assertThrows(UnsupportedOperationException.class,
                    () -> entry.getQueries().add("SELECT 3"));
        }
    }

    @Nested
    class GetStateAsJson {

        @Test
        void noEntry_returnsChartIdOnly() {
            String json = ChartEntry.getStateAsJson(chart, "my-chart");
            Assertions.assertTrue(json.contains("\"chartId\":\"my-chart\""));
            // Should not create an entry as side effect
            Assertions.assertNull(ChartEntry.get(chart));
        }

        @Test
        void entryWithNoQueries_returnsChartIdOnly() {
            ChartEntry.getOrCreate(chart, "c1");
            String json = ChartEntry.getStateAsJson(chart, "c1");
            Assertions.assertTrue(json.contains("\"chartId\":\"c1\""));
            Assertions.assertFalse(json.contains("\"queries\""));
        }

        @Test
        void entryWithQueries_returnsConfigurationAndQueries() {
            ChartEntry entry = ChartEntry.getOrCreate(chart, "c1");
            entry.setQueries(List.of("SELECT x FROM t"));
            String json = ChartEntry.getStateAsJson(chart, "c1");
            Assertions.assertTrue(json.contains("\"chartId\":\"c1\""));
            Assertions.assertTrue(json.contains("\"configuration\""));
            Assertions.assertTrue(json.contains("\"queries\""));
            Assertions.assertTrue(json.contains("SELECT x FROM t"));
        }
    }

    @Nested
    class GetState {

        @Test
        void noEntry_returnsNull() {
            Assertions.assertNull(ChartEntry.getState(chart));
        }

        @Test
        void entryWithNoQueries_returnsNull() {
            ChartEntry.getOrCreate(chart, "test");
            Assertions.assertNull(ChartEntry.getState(chart));
        }

        @Test
        void entryWithQueries_returnsState() {
            ChartEntry entry = ChartEntry.getOrCreate(chart, "test");
            entry.setQueries(List.of("SELECT 1"));
            ChartEntry.ChartState state = ChartEntry.getState(chart);
            Assertions.assertNotNull(state);
            Assertions.assertEquals(List.of("SELECT 1"), state.queries());
            Assertions.assertNotNull(state.configuration());
            // Configuration should not contain series
            Assertions
                    .assertFalse(state.configuration().contains("\"series\""));
        }
    }
}
