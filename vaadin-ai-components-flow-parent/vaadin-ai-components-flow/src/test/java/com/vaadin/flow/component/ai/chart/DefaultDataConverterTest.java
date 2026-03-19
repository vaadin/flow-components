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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.charts.model.BoxPlotItem;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem3d;
import com.vaadin.flow.component.charts.model.DataSeriesItemBullet;
import com.vaadin.flow.component.charts.model.DataSeriesItemSankey;
import com.vaadin.flow.component.charts.model.DataSeriesItemTimeline;
import com.vaadin.flow.component.charts.model.DataSeriesItemXrange;
import com.vaadin.flow.component.charts.model.FlagItem;
import com.vaadin.flow.component.charts.model.GanttSeries;
import com.vaadin.flow.component.charts.model.HeatSeries;
import com.vaadin.flow.component.charts.model.NodeSeries;
import com.vaadin.flow.component.charts.model.OhlcItem;
import com.vaadin.flow.component.charts.model.Series;
import com.vaadin.flow.component.charts.model.TreeSeries;
import com.vaadin.flow.component.charts.model.WaterFallSum;
import com.vaadin.flow.component.charts.model.style.Color;

class DefaultDataConverterTest {

    private DefaultDataConverter converter;

    @BeforeEach
    void setUp() {
        converter = new DefaultDataConverter();
    }

    /**
     * Convenience method that converts data and returns the single series,
     * asserting that exactly one series was produced.
     */
    private Series convertSingle(List<Map<String, Object>> data) {
        var result = converter.convertToSeries(data);
        Assertions.assertEquals(1, result.size(),
                "Expected exactly one series");
        return result.getFirst();
    }

    // --- Input handling ---

    @Test
    void nullInput_throwsNullPointerException() {
        Assertions.assertThrows(NullPointerException.class,
                () -> converter.convertToSeries(null));
    }

    @Test
    void emptyInput_returnsEmptyDataSeries() {
        var result = converter.convertToSeries(Collections.emptyList());
        Assertions.assertEquals(1, result.size());
        Assertions.assertInstanceOf(DataSeries.class, result.getFirst());
        Assertions.assertTrue(
                ((DataSeries) result.getFirst()).getData().isEmpty());
    }

    @Test
    void emptyColumnValues_returnsEmptyDataSeries() {
        var data = List.of(Map.<String, Object> of());
        var result = convertSingle(data);
        Assertions.assertInstanceOf(DataSeries.class, result);
        Assertions.assertTrue(((DataSeries) result).getData().isEmpty());
    }

    @Test
    void caseInsensitive_matchesColumns() {
        var data = List.of(row("OPEN", 10, "High", 15, "LOW", 5, "Close", 12));
        var result = (DataSeries) convertSingle(data);
        Assertions.assertInstanceOf(OhlcItem.class,
                result.getData().getFirst());
    }

    @Test
    void inconsistentRows_missingKeysYieldNull() {
        var data = List.<Map<String, Object>> of(
                row("category", "A", "revenue", 100), Map.of("category", "B"));
        var result = (DataSeries) convertSingle(data);
        Assertions.assertEquals(2, result.getData().size());
        Assertions.assertEquals("A", result.getData().getFirst().getName());
        Assertions.assertEquals(100, result.getData().getFirst().getY());
        Assertions.assertEquals("B", result.getData().get(1).getName());
        Assertions.assertNull(result.getData().get(1).getY());
    }

    @Test
    void duplicateLowercaseColumnNames_firstColumnWins() {
        var row = new LinkedHashMap<String, Object>();
        row.put("Name", "Alice");
        row.put("NAME", "Bob");
        row.put("label", "L");
        row.put("description", "D");
        var data = List.<Map<String, Object>> of(row);
        var result = (DataSeries) convertSingle(data);
        var item = (DataSeriesItemTimeline) result.getData().getFirst();
        Assertions.assertEquals("Alice", item.getName());
    }

    // --- OHLC ---

    @Nested
    class OhlcTests {

        @Test
        void createsOhlcItems() {
            var data = List.of(
                    row("x", 1, "open", 10, "high", 15, "low", 5, "close", 12),
                    row("x", 2, "open", 12, "high", 18, "low", 8, "close", 16));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(2, result.getData().size());
            var item = (OhlcItem) result.getData().getFirst();
            Assertions.assertEquals(1, item.getX());
            Assertions.assertEquals(10, item.getOpen());
            Assertions.assertEquals(15, item.getHigh());
            Assertions.assertEquals(5, item.getLow());
            Assertions.assertEquals(12, item.getClose());
        }

        @Test
        void withoutXColumn_usesRowIndex() {
            var data = List
                    .of(row("open", 10, "high", 15, "low", 5, "close", 12));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(0, result.getData().getFirst().getX());
        }

        @Test
        void withDateColumn_usesDateAsX() {
            var data = List.of(row("date", 1000, "open", 10, "high", 15, "low",
                    5, "close", 12));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(1000, result.getData().getFirst().getX());
        }
    }

    // --- BoxPlot ---

    @Nested
    class BoxPlotTests {

        @Test
        void createsBoxPlotItems() {
            var data = List.of(
                    row("low", 1, "q1", 3, "median", 5, "q3", 7, "high", 9));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(1, result.getData().size());
            var item = (BoxPlotItem) result.getData().getFirst();
            Assertions.assertEquals(1, item.getLow());
            Assertions.assertEquals(3, item.getLowerQuartile());
            Assertions.assertEquals(5, item.getMedian());
            Assertions.assertEquals(7, item.getUpperQuartile());
            Assertions.assertEquals(9, item.getHigh());
        }

        @Test
        void lowHighWithBoxPlotColumns_createsBoxPlotNotRange() {
            // low+high also match Range, but q1/median/q3 presence means
            // BoxPlot. Verifies the guard works.
            var data = List.of(
                    row("low", 1, "q1", 3, "median", 5, "q3", 7, "high", 9));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertInstanceOf(BoxPlotItem.class,
                    result.getData().getFirst());
        }
    }

    // --- Organization ---

    @Nested
    class OrganizationTests {

        @Test
        void createsNodeSeries() {
            var data = List.of(
                    row("id", "ceo", "name", "Alice", "title", "CEO", "parent",
                            null),
                    row("id", "cto", "name", "Bob", "title", "CTO", "parent",
                            "ceo"),
                    row("id", "dev", "name", "Carol", "title", "Developer",
                            "parent", "cto"));
            var result = convertSingle(data);
            Assertions.assertInstanceOf(NodeSeries.class, result);
            var ns = (NodeSeries) result;
            Assertions.assertEquals(3, ns.getNodes().size());
            Assertions.assertEquals(2, ns.getData().size());
        }

        @Test
        void skipsRowsWithNullId() {
            var data = List.of(
                    row("id", null, "name", "X", "title", "Y", "parent", null),
                    row("id", "a", "name", "Alice", "title", "CEO", "parent",
                            null));
            var ns = (NodeSeries) convertSingle(data);
            Assertions.assertEquals(1, ns.getNodes().size());
        }

        @Test
        void parentReferencesNonExistentId_noLink() {
            var data = List.of(row("id", "a", "name", "Alice", "title", "CEO",
                    "parent", "nonexistent"));
            var ns = (NodeSeries) convertSingle(data);
            Assertions.assertEquals(1, ns.getNodes().size());
            Assertions.assertEquals(0, ns.getData().size());
        }

        @Test
        void withDescriptionAndImage_setsFields() {
            var data = List.of(row("id", "ceo", "name", "Alice", "title", "CEO",
                    "parent", null, "description", "The boss", "image",
                    "alice.png"));
            var ns = (NodeSeries) convertSingle(data);
            var node = ns.getNodes().iterator().next();
            Assertions.assertEquals("The boss", node.getDescription());
            Assertions.assertEquals("alice.png", node.getImage());
        }
    }

    // --- Gantt ---

    @Nested
    class GanttTests {

        @Test
        void createsGanttSeries() {
            var start = Instant.parse("2024-01-01T00:00:00Z");
            var end = Instant.parse("2024-02-01T00:00:00Z");
            var data = List
                    .of(row("name", "Task 1", "start", start, "end", end));
            var result = convertSingle(data);
            Assertions.assertInstanceOf(GanttSeries.class, result);
            var gs = (GanttSeries) result;
            Assertions.assertEquals(1, gs.size());
            Assertions.assertEquals("Task 1", gs.get(0).getName());
        }

        @Test
        void withIdAndParent_setsFields() {
            var start = Instant.parse("2024-01-01T00:00:00Z");
            var end = Instant.parse("2024-02-01T00:00:00Z");
            var data = List.of(row("id", "t1", "name", "Task 1", "start", start,
                    "end", end, "parent", "root"));
            var gs = (GanttSeries) convertSingle(data);
            var item = gs.get(0);
            Assertions.assertEquals("t1", item.getId());
            Assertions.assertEquals("root", item.getParent());
        }

        @Test
        void withDependencyColumn_setsDependency() {
            var data = List.of(row("id", "t2", "name", "Task 2", "start",
                    Instant.parse("2024-01-01T00:00:00Z"), "end",
                    Instant.parse("2024-02-01T00:00:00Z"), "dependency", "t1"));
            var gs = (GanttSeries) convertSingle(data);
            var deps = gs.get(0).getDependencies();
            Assertions.assertEquals(1, deps.size());
            Assertions.assertEquals("t1", deps.getFirst().getTo());
        }

        @Test
        void withCommaSeparatedDependency_setsMultipleDependencies() {
            var data = List.of(row("id", "t3", "name", "Task 3", "start",
                    Instant.parse("2024-01-01T00:00:00Z"), "end",
                    Instant.parse("2024-02-01T00:00:00Z"), "dependency",
                    "t1, t2"));
            var gs = (GanttSeries) convertSingle(data);
            var deps = gs.get(0).getDependencies();
            Assertions.assertEquals(2, deps.size());
            Assertions.assertEquals("t1", deps.getFirst().getTo());
            Assertions.assertEquals("t2", deps.get(1).getTo());
        }

        @Test
        void withCompletedColumn_setsCompleted() {
            var data = List.of(row("name", "Task", "start",
                    Instant.parse("2024-01-01T00:00:00Z"), "end",
                    Instant.parse("2024-02-01T00:00:00Z"), "completed", 0.75));
            var gs = (GanttSeries) convertSingle(data);
            Assertions.assertNotNull(gs.get(0).getCompleted());
            Assertions.assertEquals(0.75, gs.get(0).getCompleted().getAmount());
        }

        @Test
        void stringStartEnd_doesNotMatchGantt() {
            // JDBC might return date columns as strings depending on driver.
            // Gantt requires temporal/numeric start+end values.
            var data = List.of(row("name", "Sale", "start", "A", "end", "Z"));
            var result = convertSingle(data);
            Assertions.assertInstanceOf(DataSeries.class, result);
        }
    }

    // --- Treemap ---

    @Nested
    class TreemapTests {

        @Test
        void createsTreeSeries() {
            var data = List.of(row("id", "root", "parent", null, "value", 100),
                    row("id", "child1", "parent", "root", "value", 60),
                    row("id", "child2", "parent", "root", "value", 40));
            var result = convertSingle(data);
            Assertions.assertInstanceOf(TreeSeries.class, result);
            var ts = (TreeSeries) result;
            Assertions.assertEquals(3, ts.getData().size());
        }

        @Test
        void withNameAndColorValue_setsOptionalFields() {
            var data = List.of(row("id", "a", "parent", null, "value", 100,
                    "name", "Root", "colorvalue", 0.5));
            var ts = (TreeSeries) convertSingle(data);
            var item = ts.getData().iterator().next();
            Assertions.assertEquals("Root", item.getName());
            Assertions.assertEquals(0.5, item.getColorValue());
        }
    }

    // --- Sankey ---

    @Nested
    class SankeyTests {

        @Test
        void createsSankeyItems() {
            var data = List.of(row("from", "A", "to", "B", "weight", 10),
                    row("from", "B", "to", "C", "weight", 5));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(2, result.getData().size());
            var item = (DataSeriesItemSankey) result.getData().getFirst();
            Assertions.assertEquals("A", item.getFrom());
            Assertions.assertEquals("B", item.getTo());
            Assertions.assertEquals(10, item.getWeight());
        }
    }

    // --- Heatmap ---

    @Nested
    class HeatmapTests {

        @Test
        void createsHeatSeries() {
            var data = List.of(row("x", 0, "y", 0, "value", 10),
                    row("x", 0, "y", 1, "value", 20),
                    row("x", 1, "y", 0, "value", 30));
            var result = convertSingle(data);
            Assertions.assertInstanceOf(HeatSeries.class, result);
            var hs = (HeatSeries) result;
            Assertions.assertEquals(3, hs.getData().length);
            Assertions.assertEquals(0, hs.getData()[0][0]);
            Assertions.assertEquals(0, hs.getData()[0][1]);
            Assertions.assertEquals(10, hs.getData()[0][2]);
        }

        @Test
        void skipsRowsWithNullCoordinates() {
            var data = List.of(row("x", null, "y", 0, "value", 10),
                    row("x", 1, "y", 1, "value", 20));
            var hs = (HeatSeries) convertSingle(data);
            Assertions.assertEquals(1, hs.getData().length);
        }

        @Test
        void nullValue_stillAddsPoint() {
            var data = List.of(row("x", 0, "y", 0, "value", null));
            var hs = (HeatSeries) convertSingle(data);
            Assertions.assertEquals(1, hs.getData().length);
            Assertions.assertNull(hs.getData()[0][2]);
        }
    }

    // --- XRange ---

    @Nested
    class XRangeTests {

        @Test
        void createsXrangeItems() {
            var data = List.of(row("x", 0, "x2", 10, "y", 1));
            var result = (DataSeries) convertSingle(data);
            var item = (DataSeriesItemXrange) result.getData().getFirst();
            Assertions.assertEquals(0, item.getX());
            Assertions.assertEquals(10, item.getX2());
            Assertions.assertEquals(1, item.getY());
        }
    }

    // --- Timeline ---

    @Nested
    class TimelineTests {

        @Test
        void createsTimelineItems() {
            var data = List.of(
                    row("name", "Event 1", "label", "E1", "description",
                            "First event"),
                    row("name", "Event 2", "label", "E2", "description",
                            "Second event"));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(2, result.getData().size());
            var item = (DataSeriesItemTimeline) result.getData().getFirst();
            Assertions.assertEquals("Event 1", item.getName());
            Assertions.assertEquals("E1", item.getLabel());
            Assertions.assertEquals("First event", item.getDescription());
        }

        @Test
        void withXColumn_setsX() {
            var data = List.of(row("x", 1000, "name", "Event", "label", "E",
                    "description", "Desc"));
            var result = (DataSeries) convertSingle(data);
            var item = (DataSeriesItemTimeline) result.getData().getFirst();
            Assertions.assertEquals(1000, item.getX());
        }
    }

    // --- Bubble ---

    @Nested
    class BubbleTests {

        @Test
        void createsBubbleItems() {
            var data = List.of(row("x", 1, "y", 2, "z", 3));
            var result = (DataSeries) convertSingle(data);
            var item = (DataSeriesItem3d) result.getData().getFirst();
            Assertions.assertEquals(1, item.getX());
            Assertions.assertEquals(2, item.getY());
            Assertions.assertEquals(3, item.getZ());
        }
    }

    // --- Flags ---

    @Nested
    class FlagsTests {

        @Test
        void createsFlagItems() {
            var data = List
                    .of(row("x", 100, "title", "Flag 1", "text", "Details"));
            var result = (DataSeries) convertSingle(data);
            var item = (FlagItem) result.getData().getFirst();
            Assertions.assertEquals(100, item.getX());
            Assertions.assertEquals("Flag 1", item.getTitle());
            Assertions.assertEquals("Details", item.getText());
        }

        @Test
        void withoutTextColumn_createsFlagsWithTitleOnly() {
            var data = List.of(row("x", 100, "title", "Flag 1"));
            var result = (DataSeries) convertSingle(data);
            var item = (FlagItem) result.getData().getFirst();
            Assertions.assertEquals("Flag 1", item.getTitle());
        }

        @Test
        void withParentColumn_doesNotMatchFlags() {
            // parent column signals org/treemap data, not flags
            var data = List.of(row("id", "a", "title", "CEO", "name", "Alice",
                    "parent", null));
            var result = convertSingle(data);
            Assertions.assertInstanceOf(NodeSeries.class, result);
        }

        @Test
        void nullTitle_skipsRow() {
            var data = List.of(row("x", 100, "title", null),
                    row("x", 200, "title", "Valid"));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(1, result.getData().size());
            Assertions.assertEquals(200,
                    ((FlagItem) result.getData().getFirst()).getX());
        }
    }

    // --- Range ---

    @Nested
    class RangeTests {

        @Test
        void createsRangeItems() {
            var data = List.of(row("x", 1, "low", 5, "high", 15));
            var result = (DataSeries) convertSingle(data);
            var item = result.getData().getFirst();
            Assertions.assertEquals(1, item.getX());
            Assertions.assertEquals(5, item.getLow());
            Assertions.assertEquals(15, item.getHigh());
        }

        @Test
        void withoutXColumn_usesRowIndex() {
            var data = List.of(row("low", 5, "high", 15));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(0, result.getData().getFirst().getX());
        }
    }

    // --- Bullet ---

    @Nested
    class BulletTests {

        @Test
        void createsBulletItems() {
            var data = List.of(row("y", 275, "target", 250));
            var result = (DataSeries) convertSingle(data);
            var item = (DataSeriesItemBullet) result.getData().getFirst();
            Assertions.assertEquals(275, item.getY());
            Assertions.assertEquals(250, item.getTarget());
        }

        @Test
        void withXColumn_setsX() {
            var data = List.of(row("x", 0, "y", 275, "target", 250));
            var result = (DataSeries) convertSingle(data);
            var item = (DataSeriesItemBullet) result.getData().getFirst();
            Assertions.assertEquals(0, item.getX());
        }
    }

    // --- Waterfall ---

    @Nested
    class WaterfallTests {

        @Test
        void createsWaterfallItems() {
            var data = List.of(
                    row("name", "Revenue", "y", 100, "waterfall_type", null),
                    row("name", "Cost", "y", -50, "waterfall_type", null),
                    row("name", "Subtotal", "y", null, "waterfall_type",
                            "intermediate"),
                    row("name", "Tax", "y", -10, "waterfall_type", null),
                    row("name", "Total", "y", null, "waterfall_type", "sum"));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(5, result.getData().size());

            // Regular items
            Assertions.assertFalse(
                    result.getData().getFirst() instanceof WaterFallSum);
            Assertions.assertEquals("Revenue",
                    result.getData().getFirst().getName());
            Assertions.assertEquals(100, result.getData().getFirst().getY());

            // Intermediate sum
            Assertions.assertInstanceOf(WaterFallSum.class,
                    result.getData().get(2));
            Assertions.assertTrue(
                    ((WaterFallSum) result.getData().get(2)).isIntermediate());

            // Final sum
            Assertions.assertInstanceOf(WaterFallSum.class,
                    result.getData().get(4));
            Assertions.assertFalse(
                    ((WaterFallSum) result.getData().get(4)).isIntermediate());
        }

        @Test
        void caseInsensitiveType() {
            var data = List.of(
                    row("name", "Sub", "y", null, "waterfall_type",
                            "INTERMEDIATE"),
                    row("name", "Total", "y", null, "waterfall_type", "Sum"));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertTrue(((WaterFallSum) result.getData().getFirst())
                    .isIntermediate());
            Assertions.assertFalse(
                    ((WaterFallSum) result.getData().get(1)).isIntermediate());
        }

        @Test
        void withoutWaterfallTypeColumn_doesNotMatch() {
            var data = List.of(row("name", "A", "y", 100));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertFalse(
                    result.getData().getFirst() instanceof WaterFallSum);
        }
    }

    // --- Fallback ---

    @Nested
    class FallbackTests {

        @Test
        void categoryAndValue_createsCategoryItems() {
            var data = List.of(row("department", "Sales", "revenue", 1000),
                    row("department", "Engineering", "revenue", 2000));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(2, result.getData().size());
            var item = result.getData().getFirst();
            Assertions.assertEquals("Sales", item.getName());
            Assertions.assertEquals(1000, item.getY());
        }

        @Test
        void allNumericTwoColumns_createsXYItems() {
            var data = List.of(row("a", 1, "b", 2), row("a", 3, "b", 4));
            var result = (DataSeries) convertSingle(data);
            var item = result.getData().getFirst();
            Assertions.assertEquals(1, item.getX());
            Assertions.assertEquals(2, item.getY());
        }

        @Test
        void singleNumericColumn_usesRowIndexAsX() {
            var data = List.of(row("val", 10), row("val", 20), row("val", 30));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(3, result.getData().size());
            Assertions.assertEquals(0, result.getData().getFirst().getX());
            Assertions.assertEquals(10, result.getData().getFirst().getY());
        }

        @Test
        void singleNonNumericColumn_returnsEmptySeries() {
            var data = List.of(row("status", "active"));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertTrue(result.getData().isEmpty());
        }

        @Test
        void numericStringValues_detectedAsNumeric() {
            var data = List.of(row("a", "1.5", "b", "2.5"));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(1.5, result.getData().getFirst().getX());
            Assertions.assertEquals(2.5, result.getData().getFirst().getY());
        }
    }

    // --- Null handling ---

    @Nested
    class NullHandlingTests {

        @Test
        void nullValuesInRow_preservedAsNull() {
            var data = List.of(row("department", "Sales", "revenue", null));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals("Sales",
                    result.getData().getFirst().getName());
            Assertions.assertNull(result.getData().getFirst().getY());
        }

        @Test
        void allNullRow_skippedForNamedPattern() {
            var data = List.of(row("x", null, "y", null, "z", null),
                    row("x", 1, "y", 2, "z", 3));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(1, result.getData().size());
            Assertions.assertInstanceOf(DataSeriesItem3d.class,
                    result.getData().getFirst());
        }

        @Test
        void allNullRow_skippedForFallback() {
            var data = List.of(row("dept", null, "revenue", null),
                    row("dept", "Sales", "revenue", 1000));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(1, result.getData().size());
        }
    }

    // --- Temporal conversion ---

    @Nested
    class TemporalConversionTests {

        @Test
        void instantXValue_convertedToHighchartsTimestamp() {
            var instant = Instant.parse("2024-01-15T12:00:00Z");
            var data = List.of(row("x", instant, "open", 10, "high", 15, "low",
                    5, "close", 12));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(instant.getEpochSecond() * 1000,
                    result.getData().getFirst().getX());
        }

        @Test
        void localDateXValue_convertedToHighchartsTimestamp() {
            var date = LocalDate.of(2024, 1, 15);
            var expectedMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant()
                    .getEpochSecond() * 1000;
            var data = List.of(row("x", date, "open", 10, "high", 15, "low", 5,
                    "close", 12));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(expectedMillis,
                    result.getData().getFirst().getX());
        }

        @Test
        void localDateTimeXValue_convertedToHighchartsTimestamp() {
            var dateTime = LocalDateTime.of(2024, 1, 15, 12, 30, 0);
            var expectedMillis = dateTime.toInstant(ZoneOffset.UTC)
                    .getEpochSecond() * 1000;
            var data = List.of(row("x", dateTime, "open", 10, "high", 15, "low",
                    5, "close", 12));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(expectedMillis,
                    result.getData().getFirst().getX());
        }

        @Test
        void sqlDateXValue_convertedToHighchartsTimestamp() {
            var sqlDate = java.sql.Date.valueOf("2024-01-15");
            var expectedMillis = sqlDate.toLocalDate()
                    .atStartOfDay(ZoneOffset.UTC).toInstant().getEpochSecond()
                    * 1000;
            var data = List.of(row("x", sqlDate, "open", 10, "high", 15, "low",
                    5, "close", 12));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(expectedMillis,
                    result.getData().getFirst().getX());
        }

        @Test
        void sqlTimestampXValue_convertedToHighchartsTimestamp() {
            var ts = java.sql.Timestamp.valueOf("2024-01-15 12:00:00");
            var expectedMillis = ts.toInstant().getEpochSecond() * 1000;
            var data = List.of(row("x", ts, "open", 10, "high", 15, "low", 5,
                    "close", 12));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(expectedMillis,
                    result.getData().getFirst().getX());
        }

        @Test
        void ganttWithLocalDateStartEnd() {
            var data = List.of(row("name", "Task", "start",
                    LocalDate.of(2024, 1, 1), "end", LocalDate.of(2024, 2, 1)));
            var gs = (GanttSeries) convertSingle(data);
            Assertions.assertEquals(1, gs.size());
        }

        @Test
        void ganttWithNumericMillisStartEnd() {
            var data = List.of(row("name", "Task", "start", 1704067200000L,
                    "end", 1706745600000L));
            var gs = (GanttSeries) convertSingle(data);
            Assertions.assertEquals(1, gs.size());
        }
    }

    // --- X column alternatives ---

    @Nested
    class XColumnAlternativesTests {

        @Test
        void timestampColumnUsedAsX() {
            var data = List.of(row("timestamp", 5000, "open", 10, "high", 15,
                    "low", 5, "close", 12));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(5000, result.getData().getFirst().getX());
        }

        @Test
        void timeColumnUsedAsX() {
            var data = List.of(row("time", 5000, "open", 10, "high", 15, "low",
                    5, "close", 12));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(5000, result.getData().getFirst().getX());
        }

        @Test
        void datetimeColumnUsedAsX() {
            var data = List.of(row("datetime", 5000, "open", 10, "high", 15,
                    "low", 5, "close", 12));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(5000, result.getData().getFirst().getX());
        }
    }

    // --- Color column ---

    @Nested
    class ColorColumnTests {

        @Test
        void setsItemColor() {
            var data = List.of(row("department", "Sales", "revenue", 1000,
                    "color", "#FF0000"));
            var result = (DataSeries) convertSingle(data);
            assertColor("#FF0000", result.getData().getFirst().getColor());
        }

        @Test
        void ganttWithColor_setsItemColor() {
            var data = List.of(row("name", "Task", "start",
                    Instant.parse("2024-01-01T00:00:00Z"), "end",
                    Instant.parse("2024-02-01T00:00:00Z"), "color", "#0000FF"));
            var gs = (GanttSeries) convertSingle(data);
            assertColor("#0000FF", gs.get(0).getColor());
        }

        @Test
        void organizationWithColor_setsNodeColor() {
            var data = List.of(row("id", "a", "name", "Alice", "title", "CEO",
                    "parent", null, "color", "#FF0000"));
            var ns = (NodeSeries) convertSingle(data);
            assertColor("#FF0000", ns.getNodes().iterator().next().getColor());
        }

        @Test
        void nullColor_doesNotSetColor() {
            var data = List.of(
                    row("department", "Sales", "revenue", 1000, "color", null));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertNull(result.getData().getFirst().getColor());
        }

        @Test
        void colorColumnAbsent_noColorSet() {
            var data = List.of(row("department", "Sales", "revenue", 1000));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertNull(result.getData().getFirst().getColor());
        }
    }

    // --- Multi-series ---

    @Nested
    class MultiSeriesTests {

        @Test
        void groupsBySeriesColumn() {
            var data = List.of(row("series", "Revenue", "x", 2020, "y", 100),
                    row("series", "Revenue", "x", 2021, "y", 120),
                    row("series", "Cost", "x", 2020, "y", 80),
                    row("series", "Cost", "x", 2021, "y", 90));
            var result = converter.convertToSeries(data);
            Assertions.assertEquals(2, result.size());
            Assertions.assertEquals("Revenue", result.getFirst().getName());
            Assertions.assertEquals("Cost", result.get(1).getName());

            var revenueSeries = (DataSeries) result.getFirst();
            Assertions.assertEquals(2, revenueSeries.getData().size());
            Assertions.assertEquals(100,
                    revenueSeries.getData().getFirst().getY());
            Assertions.assertEquals(120, revenueSeries.getData().get(1).getY());
        }

        @Test
        void withoutSeriesColumn_returnsSingleSeries() {
            var data = List.of(row("department", "Sales", "revenue", 1000));
            var result = converter.convertToSeries(data);
            Assertions.assertEquals(1, result.size());
        }

        @Test
        void seriesColumnRemovedFromFallbackPatternMatching() {
            var data = List.of(row("series", "A", "x", 1, "y", 10),
                    row("series", "B", "x", 2, "y", 20));
            var result = converter.convertToSeries(data);
            Assertions.assertEquals(2, result.size());
            var seriesA = (DataSeries) result.getFirst();
            Assertions.assertEquals(1, seriesA.getData().getFirst().getX());
            Assertions.assertEquals(10, seriesA.getData().getFirst().getY());
        }

        @Test
        void seriesColumnRemovedFromNamedPatternMatching() {
            var data = List.of(
                    row("series", "Flow A", "from", "X", "to", "Y", "weight",
                            10),
                    row("series", "Flow B", "from", "A", "to", "B", "weight",
                            20));
            var result = converter.convertToSeries(data);
            Assertions.assertEquals(2, result.size());
            var dsA = (DataSeries) result.getFirst();
            Assertions.assertInstanceOf(DataSeriesItemSankey.class,
                    dsA.getData().getFirst());
        }

        @Test
        void preservesGroupInsertionOrder() {
            var data = List.of(row("series", "C", "x", 1, "y", 1),
                    row("series", "A", "x", 2, "y", 2),
                    row("series", "B", "x", 3, "y", 3));
            var result = converter.convertToSeries(data);
            Assertions.assertEquals("C", result.getFirst().getName());
            Assertions.assertEquals("A", result.get(1).getName());
            Assertions.assertEquals("B", result.get(2).getName());
        }

        @Test
        void caseInsensitiveSeriesColumn() {
            var data = List.of(row("SERIES", "Group A", "x", 1, "y", 10));
            var result = converter.convertToSeries(data);
            Assertions.assertEquals(1, result.size());
            Assertions.assertEquals("Group A", result.getFirst().getName());
        }

        @Test
        void categoryAndValueWithSeriesColumn() {
            var data = List.of(
                    row("series", "2023", "department", "Sales", "revenue",
                            1000),
                    row("series", "2023", "department", "Eng", "revenue", 2000),
                    row("series", "2024", "department", "Sales", "revenue",
                            1200),
                    row("series", "2024", "department", "Eng", "revenue",
                            2500));
            var result = converter.convertToSeries(data);
            Assertions.assertEquals(2, result.size());
            Assertions.assertEquals("2023", result.getFirst().getName());
            Assertions.assertEquals("2024", result.get(1).getName());

            var series2023 = (DataSeries) result.getFirst();
            Assertions.assertEquals(2, series2023.getData().size());
            Assertions.assertEquals("Sales",
                    series2023.getData().getFirst().getName());
        }
    }

    // --- Helpers ---

    /**
     * Helper to build a row map preserving insertion order.
     */
    private static Map<String, Object> row(Object... keysAndValues) {
        var map = new LinkedHashMap<String, Object>();
        for (var i = 0; i < keysAndValues.length; i += 2) {
            map.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }

    /**
     * Asserts that a color matches the expected value by comparing string
     * representations, since {@code SolidColor} does not implement
     * {@code equals}.
     */
    private static void assertColor(String expected, Color actual) {
        Assertions.assertNotNull(actual, "Expected color " + expected);
        Assertions.assertEquals(expected, actual.toString());
    }
}
