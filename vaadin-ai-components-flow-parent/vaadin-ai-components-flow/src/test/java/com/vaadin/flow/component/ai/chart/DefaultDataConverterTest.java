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

import static com.vaadin.flow.component.ai.chart.ColumnNames.*;

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
        var data = List.of(row(OPEN.toUpperCase(), 10,
                capitalizeFirstLetter(HIGH), 15, LOW.toUpperCase(), 5,
                capitalizeFirstLetter(CLOSE), 12));
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
        row.put(capitalizeFirstLetter(NAME), "Alice");
        row.put(NAME.toUpperCase(), "Bob");
        row.put(LABEL, "L");
        row.put(DESCRIPTION, "D");
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
            var data = List.of(row(X, 1, OPEN, 10, HIGH, 15, LOW, 5, CLOSE, 12),
                    row(X, 2, OPEN, 12, HIGH, 18, LOW, 8, CLOSE, 16));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(2, result.getData().size());
            var item = (OhlcItem) result.getData().getFirst();
            Assertions.assertEquals(1, item.getX());
            Assertions.assertEquals(10, item.getOpen());
            Assertions.assertEquals(15, item.getHigh());
            Assertions.assertEquals(5, item.getLow());
            Assertions.assertEquals(12, item.getClose());
        }
    }

    // --- BoxPlot ---

    @Nested
    class BoxPlotTests {

        @Test
        void createsBoxPlotItems() {
            var data = List.of(row(LOW, 1, Q1, 3, MEDIAN, 5, Q3, 7, HIGH, 9));
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
            var data = List.of(row(LOW, 1, Q1, 3, MEDIAN, 5, Q3, 7, HIGH, 9));
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
                    row(ID, "ceo", NAME, "Alice", TITLE, "CEO", PARENT, null),
                    row(ID, "cto", NAME, "Bob", TITLE, "CTO", PARENT, "ceo"),
                    row(ID, "dev", NAME, "Carol", TITLE, "Developer", PARENT,
                            "cto"));
            var result = convertSingle(data);
            Assertions.assertInstanceOf(NodeSeries.class, result);
            var ns = (NodeSeries) result;
            Assertions.assertEquals(3, ns.getNodes().size());
            Assertions.assertEquals(2, ns.getData().size());
        }

        @Test
        void skipsRowsWithNullId() {
            var data = List.of(
                    row(ID, null, NAME, "X", TITLE, "Y", PARENT, null),
                    row(ID, "a", NAME, "Alice", TITLE, "CEO", PARENT, null));
            var ns = (NodeSeries) convertSingle(data);
            Assertions.assertEquals(1, ns.getNodes().size());
        }

        @Test
        void parentReferencesNonExistentId_noLink() {
            var data = List.of(row(ID, "a", NAME, "Alice", TITLE, "CEO", PARENT,
                    "nonexistent"));
            var ns = (NodeSeries) convertSingle(data);
            Assertions.assertEquals(1, ns.getNodes().size());
            Assertions.assertEquals(0, ns.getData().size());
        }

        @Test
        void withDescriptionAndImage_setsFields() {
            var data = List.of(row(ID, "ceo", NAME, "Alice", TITLE, "CEO",
                    PARENT, null, DESCRIPTION, "The boss", IMAGE, "alice.png"));
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
            var data = List.of(row(NAME, "Task 1", START, start, END, end));
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
            var data = List.of(row(ID, "t1", NAME, "Task 1", START, start, END,
                    end, PARENT, "root"));
            var gs = (GanttSeries) convertSingle(data);
            var item = gs.get(0);
            Assertions.assertEquals("t1", item.getId());
            Assertions.assertEquals("root", item.getParent());
        }

        @Test
        void withDependencyColumn_setsDependency() {
            var data = List.of(row(ID, "t2", NAME, "Task 2", START,
                    Instant.parse("2024-01-01T00:00:00Z"), END,
                    Instant.parse("2024-02-01T00:00:00Z"), DEPENDENCY, "t1"));
            var gs = (GanttSeries) convertSingle(data);
            var deps = gs.get(0).getDependencies();
            Assertions.assertEquals(1, deps.size());
            Assertions.assertEquals("t1", deps.getFirst().getTo());
        }

        @Test
        void withCommaSeparatedDependency_setsMultipleDependencies() {
            var data = List.of(row(ID, "t3", NAME, "Task 3", START,
                    Instant.parse("2024-01-01T00:00:00Z"), END,
                    Instant.parse("2024-02-01T00:00:00Z"), DEPENDENCY,
                    "t1, t2"));
            var gs = (GanttSeries) convertSingle(data);
            var deps = gs.get(0).getDependencies();
            Assertions.assertEquals(2, deps.size());
            Assertions.assertEquals("t1", deps.getFirst().getTo());
            Assertions.assertEquals("t2", deps.get(1).getTo());
        }

        @Test
        void withCompletedColumn_setsCompleted() {
            var data = List.of(row(NAME, "Task", START,
                    Instant.parse("2024-01-01T00:00:00Z"), END,
                    Instant.parse("2024-02-01T00:00:00Z"), COMPLETED, 0.75));
            var gs = (GanttSeries) convertSingle(data);
            Assertions.assertNotNull(gs.get(0).getCompleted());
            Assertions.assertEquals(0.75, gs.get(0).getCompleted().getAmount());
        }

        @Test
        void stringStartEnd_doesNotMatchGantt() {
            // JDBC might return date columns as strings depending on driver.
            // Gantt requires temporal/numeric start+end values.
            var data = List.of(row(NAME, "Sale", START, "A", END, "Z"));
            var result = convertSingle(data);
            Assertions.assertInstanceOf(DataSeries.class, result);
        }
    }

    // --- Treemap ---

    @Nested
    class TreemapTests {

        @Test
        void createsTreeSeries() {
            var data = List.of(row(ID, "root", PARENT, null, VALUE, 100),
                    row(ID, "child1", PARENT, "root", VALUE, 60),
                    row(ID, "child2", PARENT, "root", VALUE, 40));
            var result = convertSingle(data);
            Assertions.assertInstanceOf(TreeSeries.class, result);
            var ts = (TreeSeries) result;
            Assertions.assertEquals(3, ts.getData().size());
        }

        @Test
        void withNameAndColorValue_setsOptionalFields() {
            var data = List.of(row(ID, "a", PARENT, null, VALUE, 100, NAME,
                    "Root", COLOR_VALUE, 0.5));
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
            var data = List.of(row(FROM, "A", TO, "B", WEIGHT, 10),
                    row(FROM, "B", TO, "C", WEIGHT, 5));
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
            var data = List.of(row(X, 0, Y, 0, VALUE, 10),
                    row(X, 0, Y, 1, VALUE, 20), row(X, 1, Y, 0, VALUE, 30));
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
            var data = List.of(row(X, null, Y, 0, VALUE, 10),
                    row(X, 1, Y, 1, VALUE, 20));
            var hs = (HeatSeries) convertSingle(data);
            Assertions.assertEquals(1, hs.getData().length);
        }

        @Test
        void nullValue_stillAddsPoint() {
            var data = List.of(row(X, 0, Y, 0, VALUE, null));
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
            var data = List.of(row(X, 0, X2, 10, Y, 1));
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
                    row(NAME, "Event 1", LABEL, "E1", DESCRIPTION,
                            "First event"),
                    row(NAME, "Event 2", LABEL, "E2", DESCRIPTION,
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
            var data = List.of(row(X, 1000, NAME, "Event", LABEL, "E",
                    DESCRIPTION, "Desc"));
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
            var data = List.of(row(X, 1, Y, 2, Z, 3));
            var result = (DataSeries) convertSingle(data);
            var item = (DataSeriesItem3d) result.getData().getFirst();
            Assertions.assertEquals(1, item.getX());
            Assertions.assertEquals(2, item.getY());
            Assertions.assertEquals(3, item.getZ());
        }
    }

    // --- XY (scatter) ---

    @Nested
    class XYTests {

        @Test
        void createsNumericXYItems() {
            var data = List.of(row(X, 32, Y, 85000), row(X, 28, Y, 72000));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(2, result.getData().size());
            var item = result.getData().getFirst();
            Assertions.assertEquals(32, item.getX());
            Assertions.assertEquals(85000, item.getY());
        }

        @Test
        void withExtraColumns_stillMatchesXY() {
            // Extra non-alias columns (e.g. name for tooltip) should not
            // prevent XY pattern from matching
            var data = List.of(row(X, 32, Y, 85000, "name", "John"));
            var result = (DataSeries) convertSingle(data);
            var item = result.getData().getFirst();
            Assertions.assertEquals(32, item.getX());
            Assertions.assertEquals(85000, item.getY());
        }

        @Test
        void withSeriesColumn_groupsBySeriesAndCreatesXYItems() {
            var data = List.of(row(X, 32, Y, 85000, SERIES, "Engineering"),
                    row(X, 28, Y, 72000, SERIES, "Sales"));
            var result = converter.convertToSeries(data);
            Assertions.assertEquals(2, result.size());
            var eng = (DataSeries) result.getFirst();
            Assertions.assertEquals("Engineering", eng.getName());
            var item = eng.getData().getFirst();
            Assertions.assertEquals(32, item.getX());
            Assertions.assertEquals(85000, item.getY());
        }

        @Test
        void doesNotMatchWhenZPresent() {
            // _z present → should match bubble, not XY
            var data = List.of(row(X, 1, Y, 2, Z, 3));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertInstanceOf(DataSeriesItem3d.class,
                    result.getData().getFirst());
        }

        @Test
        void doesNotMatchWhenTargetPresent() {
            // _target present → should match bullet, not XY
            var data = List.of(row(X, 0, Y, 275, TARGET, 250));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertInstanceOf(DataSeriesItemBullet.class,
                    result.getData().getFirst());
        }

        @Test
        void nullXAndY_skipsRow() {
            var data = List.of(row(X, null, Y, null), row(X, 5, Y, 10));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(1, result.getData().size());
            Assertions.assertEquals(5, result.getData().getFirst().getX());
        }

        @Test
        void colorColumn_setsItemColor() {
            var data = List.of(row(X, 32, Y, 85000, COLOR, "#FF0000"));
            var result = (DataSeries) convertSingle(data);
            assertColor("#FF0000", result.getData().getFirst().getColor());
        }

        @Test
        void dateXColumn_convertsToEpochMilliseconds() {
            // Covers volume series in candlestick combo: _x is a date, _y
            // is numeric. Without tryXY, the fallback treats date as a
            // category name instead of a numeric timestamp.
            var date = java.sql.Date.valueOf(LocalDate.of(2025, 1, 6));
            var data = List.of(row(X, date, Y, 52000));
            var result = (DataSeries) convertSingle(data);
            var item = result.getData().getFirst();
            var expectedMs = LocalDate.of(2025, 1, 6)
                    .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
            Assertions.assertEquals(expectedMs, item.getX().longValue());
            Assertions.assertEquals(52000, item.getY());
        }
    }

    // --- Flags ---

    @Nested
    class FlagsTests {

        @Test
        void createsFlagItems() {
            var data = List.of(row(X, 100, TITLE, "Flag 1", TEXT, "Details"));
            var result = (DataSeries) convertSingle(data);
            var item = (FlagItem) result.getData().getFirst();
            Assertions.assertEquals(100, item.getX());
            Assertions.assertEquals("Flag 1", item.getTitle());
            Assertions.assertEquals("Details", item.getText());
        }

        @Test
        void withoutTextColumn_createsFlagsWithTitleOnly() {
            var data = List.of(row(X, 100, TITLE, "Flag 1"));
            var result = (DataSeries) convertSingle(data);
            var item = (FlagItem) result.getData().getFirst();
            Assertions.assertEquals("Flag 1", item.getTitle());
        }

        @Test
        void withParentColumn_doesNotMatchFlags() {
            // parent column signals org/treemap data, not flags
            var data = List.of(
                    row(ID, "a", TITLE, "CEO", NAME, "Alice", PARENT, null));
            var result = convertSingle(data);
            Assertions.assertInstanceOf(NodeSeries.class, result);
        }

        @Test
        void nullTitle_skipsRow() {
            var data = List.of(row(X, 100, TITLE, null),
                    row(X, 200, TITLE, "Valid"));
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
            var data = List.of(row(X, 1, LOW, 5, HIGH, 15));
            var result = (DataSeries) convertSingle(data);
            var item = result.getData().getFirst();
            Assertions.assertEquals(1, item.getX());
            Assertions.assertEquals(5, item.getLow());
            Assertions.assertEquals(15, item.getHigh());
        }
    }

    // --- Bullet ---

    @Nested
    class BulletTests {

        @Test
        void createsBulletItems() {
            var data = List.of(row(Y, 275, TARGET, 250));
            var result = (DataSeries) convertSingle(data);
            var item = (DataSeriesItemBullet) result.getData().getFirst();
            Assertions.assertEquals(275, item.getY());
            Assertions.assertEquals(250, item.getTarget());
        }

        @Test
        void withXColumn_setsX() {
            var data = List.of(row(X, 0, Y, 275, TARGET, 250));
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
                    row(NAME, "Revenue", Y, 100, WATERFALL_TYPE, null),
                    row(NAME, "Cost", Y, -50, WATERFALL_TYPE, null),
                    row(NAME, "Subtotal", Y, null, WATERFALL_TYPE,
                            "intermediate"),
                    row(NAME, "Tax", Y, -10, WATERFALL_TYPE, null),
                    row(NAME, "Total", Y, null, WATERFALL_TYPE, "sum"));
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
                    row(NAME, "Sub", Y, null, WATERFALL_TYPE, "INTERMEDIATE"),
                    row(NAME, "Total", Y, null, WATERFALL_TYPE, "Sum"));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertTrue(((WaterFallSum) result.getData().getFirst())
                    .isIntermediate());
            Assertions.assertFalse(
                    ((WaterFallSum) result.getData().get(1)).isIntermediate());
        }

        @Test
        void withoutWaterfallTypeColumn_doesNotMatch() {
            var data = List.of(row(NAME, "A", Y, 100));
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
            var data = List.of(row(X, null, Y, null, Z, null),
                    row(X, 1, Y, 2, Z, 3));
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
            var data = List
                    .of(row(X, instant, OPEN, 10, HIGH, 15, LOW, 5, CLOSE, 12));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(instant.getEpochSecond() * 1000,
                    result.getData().getFirst().getX());
        }

        @Test
        void localDateXValue_convertedToHighchartsTimestamp() {
            var date = LocalDate.of(2024, 1, 15);
            var expectedMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant()
                    .getEpochSecond() * 1000;
            var data = List
                    .of(row(X, date, OPEN, 10, HIGH, 15, LOW, 5, CLOSE, 12));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(expectedMillis,
                    result.getData().getFirst().getX());
        }

        @Test
        void localDateTimeXValue_convertedToHighchartsTimestamp() {
            var dateTime = LocalDateTime.of(2024, 1, 15, 12, 30, 0);
            var expectedMillis = dateTime.toInstant(ZoneOffset.UTC)
                    .getEpochSecond() * 1000;
            var data = List.of(
                    row(X, dateTime, OPEN, 10, HIGH, 15, LOW, 5, CLOSE, 12));
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
            var data = List
                    .of(row(X, sqlDate, OPEN, 10, HIGH, 15, LOW, 5, CLOSE, 12));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(expectedMillis,
                    result.getData().getFirst().getX());
        }

        @Test
        void sqlTimestampXValue_convertedToHighchartsTimestamp() {
            var ts = java.sql.Timestamp.valueOf("2024-01-15 12:00:00");
            var expectedMillis = ts.toInstant().getEpochSecond() * 1000;
            var data = List
                    .of(row(X, ts, OPEN, 10, HIGH, 15, LOW, 5, CLOSE, 12));
            var result = (DataSeries) convertSingle(data);
            Assertions.assertEquals(expectedMillis,
                    result.getData().getFirst().getX());
        }

        @Test
        void ganttWithLocalDateStartEnd() {
            var data = List.of(row(NAME, "Task", START,
                    LocalDate.of(2024, 1, 1), END, LocalDate.of(2024, 2, 1)));
            var gs = (GanttSeries) convertSingle(data);
            Assertions.assertEquals(1, gs.size());
        }

        @Test
        void ganttWithNumericMillisStartEnd() {
            var data = List.of(row(NAME, "Task", START, 1704067200000L, END,
                    1706745600000L));
            var gs = (GanttSeries) convertSingle(data);
            Assertions.assertEquals(1, gs.size());
        }
    }

    // --- Color column ---

    @Nested
    class ColorColumnTests {

        @Test
        void setsItemColor() {
            var data = List.of(row("department", "Sales", "revenue", 1000,
                    COLOR, "#FF0000"));
            var result = (DataSeries) convertSingle(data);
            assertColor("#FF0000", result.getData().getFirst().getColor());
        }

        @Test
        void ganttWithColor_setsItemColor() {
            var data = List.of(row(NAME, "Task", START,
                    Instant.parse("2024-01-01T00:00:00Z"), END,
                    Instant.parse("2024-02-01T00:00:00Z"), COLOR, "#0000FF"));
            var gs = (GanttSeries) convertSingle(data);
            assertColor("#0000FF", gs.get(0).getColor());
        }

        @Test
        void organizationWithColor_setsNodeColor() {
            var data = List.of(row(ID, "a", NAME, "Alice", TITLE, "CEO", PARENT,
                    null, COLOR, "#FF0000"));
            var ns = (NodeSeries) convertSingle(data);
            assertColor("#FF0000", ns.getNodes().iterator().next().getColor());
        }

        @Test
        void nullColor_doesNotSetColor() {
            var data = List.of(
                    row("department", "Sales", "revenue", 1000, COLOR, null));
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
            var data = List.of(row(SERIES, "Revenue", X, 2020, Y, 100),
                    row(SERIES, "Revenue", X, 2021, Y, 120),
                    row(SERIES, "Cost", X, 2020, Y, 80),
                    row(SERIES, "Cost", X, 2021, Y, 90));
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
            var data = List.of(row(SERIES, "A", X, 1, Y, 10),
                    row(SERIES, "B", X, 2, Y, 20));
            var result = converter.convertToSeries(data);
            Assertions.assertEquals(2, result.size());
            var seriesA = (DataSeries) result.getFirst();
            Assertions.assertEquals(1, seriesA.getData().getFirst().getX());
            Assertions.assertEquals(10, seriesA.getData().getFirst().getY());
        }

        @Test
        void seriesColumnRemovedFromNamedPatternMatching() {
            var data = List.of(
                    row(SERIES, "Flow A", FROM, "X", TO, "Y", WEIGHT, 10),
                    row(SERIES, "Flow B", FROM, "A", TO, "B", WEIGHT, 20));
            var result = converter.convertToSeries(data);
            Assertions.assertEquals(2, result.size());
            var dsA = (DataSeries) result.getFirst();
            Assertions.assertInstanceOf(DataSeriesItemSankey.class,
                    dsA.getData().getFirst());
        }

        @Test
        void preservesGroupInsertionOrder() {
            var data = List.of(row(SERIES, "C", X, 1, Y, 1),
                    row(SERIES, "A", X, 2, Y, 2), row(SERIES, "B", X, 3, Y, 3));
            var result = converter.convertToSeries(data);
            Assertions.assertEquals("C", result.getFirst().getName());
            Assertions.assertEquals("A", result.get(1).getName());
            Assertions.assertEquals("B", result.get(2).getName());
        }

        @Test
        void caseInsensitiveSeriesColumn() {
            var data = List.of(row("_SERIES", "Group A", X, 1, Y, 10));
            var result = converter.convertToSeries(data);
            Assertions.assertEquals(1, result.size());
            Assertions.assertEquals("Group A", result.getFirst().getName());
        }

        @Test
        void categoryAndValueWithSeriesColumn() {
            var data = List.of(
                    row(SERIES, "2023", "department", "Sales", "revenue", 1000),
                    row(SERIES, "2023", "department", "Eng", "revenue", 2000),
                    row(SERIES, "2024", "department", "Sales", "revenue", 1200),
                    row(SERIES, "2024", "department", "Eng", "revenue", 2500));
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

    private static String capitalizeFirstLetter(String columnName) {
        var columnNameWithoutPrefix = columnName.replaceFirst(PREFIX, "");
        return PREFIX + columnNameWithoutPrefix.substring(0, 1).toUpperCase()
                + columnNameWithoutPrefix.substring(1);
    }
}
