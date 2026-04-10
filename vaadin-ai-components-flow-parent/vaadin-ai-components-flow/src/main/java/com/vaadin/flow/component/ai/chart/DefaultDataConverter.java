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

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.charts.model.AbstractSeries;
import com.vaadin.flow.component.charts.model.BoxPlotItem;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.DataSeriesItem3d;
import com.vaadin.flow.component.charts.model.DataSeriesItemBullet;
import com.vaadin.flow.component.charts.model.DataSeriesItemSankey;
import com.vaadin.flow.component.charts.model.DataSeriesItemTimeline;
import com.vaadin.flow.component.charts.model.DataSeriesItemXrange;
import com.vaadin.flow.component.charts.model.FlagItem;
import com.vaadin.flow.component.charts.model.GanttSeries;
import com.vaadin.flow.component.charts.model.GanttSeriesItem;
import com.vaadin.flow.component.charts.model.HeatSeries;
import com.vaadin.flow.component.charts.model.Node;
import com.vaadin.flow.component.charts.model.NodeSeries;
import com.vaadin.flow.component.charts.model.OhlcItem;
import com.vaadin.flow.component.charts.model.Series;
import com.vaadin.flow.component.charts.model.TreeSeries;
import com.vaadin.flow.component.charts.model.TreeSeriesItem;
import com.vaadin.flow.component.charts.model.WaterFallSum;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.charts.util.Util;

/**
 * Default implementation of {@link DataConverter} that handles common data
 * conversion patterns for various chart types.
 * <p>
 * This converter uses column-name-based pattern matching to determine the
 * appropriate series type and data structure. All expected column names are
 * defined in {@link ColumnNames} and prefixed with {@value ColumnNames#PREFIX}
 * to avoid collisions with real data columns. Patterns are checked in priority
 * order from most-specific to least-specific:
 * </p>
 * <ol>
 * <li>OHLC/Candlestick ({@code _open}, {@code _high}, {@code _low},
 * {@code _close})</li>
 * <li>BoxPlot ({@code _low}, {@code _q1}, {@code _median}, {@code _q3},
 * {@code _high})</li>
 * <li>Organization ({@code _id}, {@code _name}, {@code _parent},
 * {@code _title}; optionally {@code _description}, {@code _image},
 * {@code _color})</li>
 * <li>Gantt ({@code _name}, {@code _start}, {@code _end}; optionally
 * {@code _id}, {@code _parent}, {@code _dependency}, {@code _completed},
 * {@code _color})</li>
 * <li>Treemap ({@code _id}, {@code _parent}, {@code _value})</li>
 * <li>Sankey ({@code _from}, {@code _to}, {@code _weight})</li>
 * <li>Heatmap ({@code _x}, {@code _y}, {@code _value})</li>
 * <li>XRange ({@code _x}, {@code _x2}, {@code _y})</li>
 * <li>Timeline ({@code _name}, {@code _label}, {@code _description})</li>
 * <li>Bubble ({@code _x}, {@code _y}, {@code _z})</li>
 * <li>Flags ({@code _title}, optionally {@code _text}; requires absence of
 * {@code _parent} to avoid matching Organization/Treemap data)</li>
 * <li>Range ({@code _low}, {@code _high})</li>
 * <li>Bullet ({@code _y}, {@code _target})</li>
 * <li>Waterfall ({@code _y}, {@code _waterfall_type}; type values are
 * {@code "sum"} and {@code "intermediate"}, case-insensitive; {@code null} or
 * absent for regular data points)</li>
 * <li>Fallback: first non-numeric column as category, first numeric column as
 * value</li>
 * </ol>
 * <p>
 * Additionally, any pattern that produces a {@code DataSeriesItem} (or
 * subclass) supports an optional {@code _color} column. If present, the value
 * is used to set the item's color via {@link SolidColor#SolidColor(String)}.
 * </p>
 * <p>
 * Column name matching is case-insensitive. Column names are determined from
 * the first row; subsequent rows with missing keys will yield {@code null} for
 * those columns.
 * </p>
 * <p>
 * If the data contains a {@code _series} column, rows are automatically grouped
 * by that column's value and each group is converted into a separate named
 * series. The {@code _series} column is removed before pattern matching so it
 * does not interfere with chart type detection.
 * </p>
 *
 * @author Vaadin Ltd
 * @see ColumnNames
 */
public class DefaultDataConverter implements DataConverter {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DefaultDataConverter.class);

    @Override
    public List<Series> convertToSeries(List<Map<String, Object>> data) {
        Objects.requireNonNull(data, "Data must not be null");

        if (data.isEmpty()) {
            return List.of(new DataSeries());
        }

        var columnMapping = buildColumnMapping(data.getFirst());
        var columns = columnMapping.keySet();

        if (columns.contains(SERIES)) {
            return convertGrouped(data, columnMapping);
        }

        return List.of(convertSingle(data, columns, columnMapping));
    }

    /**
     * Converts a single dataset (no {@code _series} column) into one series.
     */
    private AbstractSeries convertSingle(List<Map<String, Object>> data,
            Set<String> columns, Map<String, String> columnMapping) {
        return tryOhlc(data, columns, columnMapping)
                .or(() -> tryBoxPlot(data, columns, columnMapping))
                .or(() -> tryOrganization(data, columns, columnMapping))
                .or(() -> tryGantt(data, columns, columnMapping))
                .or(() -> tryTreemap(data, columns, columnMapping))
                .or(() -> trySankey(data, columns, columnMapping))
                .or(() -> tryHeatmap(data, columns, columnMapping))
                .or(() -> tryXRange(data, columns, columnMapping))
                .or(() -> tryTimeline(data, columns, columnMapping))
                .or(() -> tryBubble(data, columns, columnMapping))
                .or(() -> tryXY(data, columns, columnMapping))
                .or(() -> tryFlags(data, columns, columnMapping))
                .or(() -> tryRange(data, columns, columnMapping))
                .or(() -> tryBullet(data, columns, columnMapping))
                .or(() -> tryWaterfall(data, columns, columnMapping))
                .orElseGet(() -> convertFallback(data, columns, columnMapping));
    }

    /**
     * Groups rows by the {@code _series} column, converts each group
     * independently, and returns one named series per group.
     */
    private List<Series> convertGrouped(List<Map<String, Object>> data,
            Map<String, String> columnMapping) {
        var originalSeriesKey = columnMapping.get(SERIES);
        var groups = new LinkedHashMap<String, List<Map<String, Object>>>();
        for (var row : data) {
            var groupName = toText(row.get(originalSeriesKey));
            var key = groupName != null ? groupName : "";
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }

        var result = new ArrayList<Series>();
        for (var entry : groups.entrySet()) {
            var groupRows = entry.getValue().stream()
                    .map(row -> withoutKey(row, originalSeriesKey)).toList();
            var groupMapping = buildColumnMapping(groupRows.getFirst());
            var series = convertSingle(groupRows, groupMapping.keySet(),
                    groupMapping);
            series.setName(entry.getKey().isEmpty() ? null : entry.getKey());
            result.add(series);
        }
        return result;
    }

    private static Map<String, Object> withoutKey(Map<String, Object> row,
            String key) {
        var copy = new LinkedHashMap<>(row);
        copy.remove(key);
        return copy;
    }

    // --- Pattern matchers returning DataSeries ---

    private Optional<AbstractSeries> tryOhlc(List<Map<String, Object>> data,
            Set<String> columns, Map<String, String> columnMapping) {
        if (!hasColumns(columns, OPEN, HIGH, LOW, CLOSE)) {
            return Optional.empty();
        }
        var series = new DataSeries();
        for (var i = 0; i < data.size(); i++) {
            var row = data.get(i);
            var open = getNumber(row, columnMapping, OPEN);
            var high = getNumber(row, columnMapping, HIGH);
            var low = getNumber(row, columnMapping, LOW);
            var close = getNumber(row, columnMapping, CLOSE);
            if (open == null && high == null && low == null && close == null) {
                continue;
            }
            var x = resolveX(row, columnMapping, X, i);
            var item = new OhlcItem(x, open, high, low, close);
            applyColor(item, row, columnMapping, columns);
            series.add(item);
        }
        return Optional.of(series);
    }

    private Optional<AbstractSeries> tryBoxPlot(List<Map<String, Object>> data,
            Set<String> columns, Map<String, String> columnMapping) {
        if (!hasColumns(columns, LOW, Q1, MEDIAN, Q3, HIGH)) {
            return Optional.empty();
        }
        var series = new DataSeries();
        for (var row : data) {
            var low = getNumber(row, columnMapping, LOW);
            var q1 = getNumber(row, columnMapping, Q1);
            var median = getNumber(row, columnMapping, MEDIAN);
            var q3 = getNumber(row, columnMapping, Q3);
            var high = getNumber(row, columnMapping, HIGH);
            if (low == null && q1 == null && median == null && q3 == null
                    && high == null) {
                continue;
            }
            var item = new BoxPlotItem(low, q1, median, q3, high);
            applyColor(item, row, columnMapping, columns);
            series.add(item);
        }
        return Optional.of(series);
    }

    private Optional<AbstractSeries> trySankey(List<Map<String, Object>> data,
            Set<String> columns, Map<String, String> columnMapping) {
        if (!hasColumns(columns, FROM, TO, WEIGHT)) {
            return Optional.empty();
        }
        var series = new DataSeries();
        for (var row : data) {
            var from = getText(row, columnMapping, FROM);
            var to = getText(row, columnMapping, TO);
            var weight = getNumber(row, columnMapping, WEIGHT);
            if (from == null && to == null && weight == null) {
                continue;
            }
            var item = new DataSeriesItemSankey(from, to, weight);
            applyColor(item, row, columnMapping, columns);
            series.add(item);
        }
        return Optional.of(series);
    }

    private Optional<AbstractSeries> tryXRange(List<Map<String, Object>> data,
            Set<String> columns, Map<String, String> columnMapping) {
        if (!hasColumns(columns, X, X2, Y)) {
            return Optional.empty();
        }
        var series = new DataSeries();
        for (var row : data) {
            var x = getNumber(row, columnMapping, X);
            var x2 = getNumber(row, columnMapping, X2);
            var y = getNumber(row, columnMapping, Y);
            if (x == null && x2 == null && y == null) {
                continue;
            }
            var item = new DataSeriesItemXrange(x, x2, y);
            applyColor(item, row, columnMapping, columns);
            series.add(item);
        }
        return Optional.of(series);
    }

    private Optional<AbstractSeries> tryBubble(List<Map<String, Object>> data,
            Set<String> columns, Map<String, String> columnMapping) {
        if (!hasColumns(columns, X, Y, Z)) {
            return Optional.empty();
        }
        var series = new DataSeries();
        for (var row : data) {
            var x = getNumber(row, columnMapping, X);
            var y = getNumber(row, columnMapping, Y);
            var z = getNumber(row, columnMapping, Z);
            if (x == null && y == null && z == null) {
                continue;
            }
            var item = new DataSeriesItem3d(x, y, z);
            applyColor(item, row, columnMapping, columns);
            series.add(item);
        }
        return Optional.of(series);
    }

    private Optional<AbstractSeries> tryXY(List<Map<String, Object>> data,
            Set<String> columns, Map<String, String> columnMapping) {
        if (!hasColumns(columns, X, Y) || columns.contains(Z)
                || columns.contains(TARGET)
                || columns.contains(WATERFALL_TYPE)) {
            return Optional.empty();
        }
        var series = new DataSeries();
        for (var i = 0; i < data.size(); i++) {
            var row = data.get(i);
            var x = getNumber(row, columnMapping, X);
            var y = getNumber(row, columnMapping, Y);
            if (x == null && y == null) {
                continue;
            }
            var item = new DataSeriesItem(x, y);
            applyColor(item, row, columnMapping, columns);
            series.add(item);
        }
        return Optional.of(series);
    }

    private Optional<AbstractSeries> tryRange(List<Map<String, Object>> data,
            Set<String> columns, Map<String, String> columnMapping) {
        if (!hasColumns(columns, LOW, HIGH) || hasBoxPlotColumns(columns)) {
            return Optional.empty();
        }
        var series = new DataSeries();
        for (var i = 0; i < data.size(); i++) {
            var row = data.get(i);
            var x = resolveX(row, columnMapping, X, i);
            var low = getNumber(row, columnMapping, LOW);
            var high = getNumber(row, columnMapping, HIGH);
            if (low == null && high == null) {
                continue;
            }
            var item = new DataSeriesItem(x, low, high);
            applyColor(item, row, columnMapping, columns);
            series.add(item);
        }
        return Optional.of(series);
    }

    private Optional<AbstractSeries> tryBullet(List<Map<String, Object>> data,
            Set<String> columns, Map<String, String> columnMapping) {
        if (!hasColumns(columns, Y, TARGET)) {
            return Optional.empty();
        }
        var series = new DataSeries();
        for (var row : data) {
            var y = getNumber(row, columnMapping, Y);
            var target = getNumber(row, columnMapping, TARGET);
            if (y == null && target == null) {
                continue;
            }

            DataSeriesItemBullet item;
            if (columns.contains(X)) {
                var x = getNumber(row, columnMapping, X);
                item = new DataSeriesItemBullet(x, y, target);
            } else {
                item = new DataSeriesItemBullet(y, target);
            }
            applyColor(item, row, columnMapping, columns);
            series.add(item);
        }
        return Optional.of(series);
    }

    private Optional<AbstractSeries> tryWaterfall(
            List<Map<String, Object>> data, Set<String> columns,
            Map<String, String> columnMapping) {
        if (!hasColumns(columns, Y, WATERFALL_TYPE)) {
            return Optional.empty();
        }
        var series = new DataSeries();
        for (var row : data) {
            var name = columns.contains(NAME)
                    ? getText(row, columnMapping, NAME)
                    : null;
            var type = getText(row, columnMapping, WATERFALL_TYPE);
            if (type != null) {
                var sumItem = new WaterFallSum(name);
                if ("intermediate".equalsIgnoreCase(type)) {
                    sumItem.setIntermediate(true);
                }
                applyColor(sumItem, row, columnMapping, columns);
                series.add(sumItem);
            } else {
                var y = getNumber(row, columnMapping, Y);
                if (name == null && y == null) {
                    continue;
                }
                var item = new DataSeriesItem(name, y);
                applyColor(item, row, columnMapping, columns);
                series.add(item);
            }
        }
        return Optional.of(series);
    }

    private Optional<AbstractSeries> tryTimeline(List<Map<String, Object>> data,
            Set<String> columns, Map<String, String> columnMapping) {
        if (!hasColumns(columns, NAME, LABEL, DESCRIPTION)) {
            return Optional.empty();
        }
        var series = new DataSeries();
        for (var row : data) {
            var name = getText(row, columnMapping, NAME);
            var label = getText(row, columnMapping, LABEL);
            var description = getText(row, columnMapping, DESCRIPTION);
            if (name == null && label == null && description == null) {
                continue;
            }
            DataSeriesItemTimeline item;
            if (columns.contains(X)) {
                var x = getNumber(row, columnMapping, X);
                item = new DataSeriesItemTimeline(x, name, label, description);
            } else {
                item = new DataSeriesItemTimeline(name, label, description);
            }
            applyColor(item, row, columnMapping, columns);
            series.add(item);
        }
        return Optional.of(series);
    }

    private Optional<AbstractSeries> tryFlags(List<Map<String, Object>> data,
            Set<String> columns, Map<String, String> columnMapping) {
        if (!hasColumns(columns, TITLE) || columns.contains(PARENT)) {
            return Optional.empty();
        }
        var series = new DataSeries();
        for (var i = 0; i < data.size(); i++) {
            var row = data.get(i);
            var x = resolveX(row, columnMapping, X, i);
            var title = getText(row, columnMapping, TITLE);
            if (title == null) {
                continue;
            }
            var item = new FlagItem(x, title);
            if (columns.contains(TEXT)) {
                item.setText(getText(row, columnMapping, TEXT));
            }
            applyColor(item, row, columnMapping, columns);
            series.add(item);
        }
        return Optional.of(series);
    }

    // --- Pattern matchers returning specialized series types ---

    private Optional<AbstractSeries> tryOrganization(
            List<Map<String, Object>> data, Set<String> columns,
            Map<String, String> columnMapping) {
        if (!hasColumns(columns, ID, NAME, PARENT, TITLE)) {
            return Optional.empty();
        }
        var series = new NodeSeries();
        var nodesById = new HashMap<String, Node>();

        // First pass: create all nodes.
        for (var row : data) {
            var id = getText(row, columnMapping, ID);
            if (id == null) {
                continue;
            }
            var name = getText(row, columnMapping, NAME);
            var title = getText(row, columnMapping, TITLE);
            var node = new Node(id, name, title);
            if (columns.contains(DESCRIPTION)) {
                node.setDescription(getText(row, columnMapping, DESCRIPTION));
            }
            if (columns.contains(IMAGE)) {
                node.setImage(getText(row, columnMapping, IMAGE));
            }
            var color = getText(row, columnMapping, COLOR);
            if (color != null) {
                node.setColor(new SolidColor(color));
            }
            nodesById.put(id, node);
            series.addNode(node);
        }

        // Second pass: link children to parents.
        for (var row : data) {
            var id = getText(row, columnMapping, ID);
            var parentId = getText(row, columnMapping, PARENT);
            if (id == null || parentId == null) {
                continue;
            }
            var child = nodesById.get(id);
            var parent = nodesById.get(parentId);
            if (child != null && parent != null) {
                series.add(parent, child);
            }
        }

        return Optional.of(series);
    }

    private Optional<AbstractSeries> tryGantt(List<Map<String, Object>> data,
            Set<String> columns, Map<String, String> columnMapping) {
        if (!hasColumns(columns, NAME, START, END)) {
            return Optional.empty();
        }
        if (!hasTemporalStartEnd(data, columnMapping)) {
            return Optional.empty();
        }
        var series = new GanttSeries();
        for (var row : data) {
            var name = getText(row, columnMapping, NAME);
            var start = getInstant(row, columnMapping, START);
            var end = getInstant(row, columnMapping, END);
            if (name == null && start == null && end == null) {
                continue;
            }
            var item = new GanttSeriesItem();
            item.setName(name);
            if (start != null) {
                item.setStart(start);
            }
            if (end != null) {
                item.setEnd(end);
            }
            setOptionalText(item::setId, row, columnMapping, columns, ID);
            setOptionalText(item::setParent, row, columnMapping, columns,
                    PARENT);
            if (columns.contains(DEPENDENCY)) {
                var dep = getText(row, columnMapping, DEPENDENCY);
                if (dep != null) {
                    Arrays.stream(dep.split(",")).filter(id -> !id.isBlank())
                            .map(String::trim).forEach(item::addDependency);
                }
            }
            if (columns.contains(COMPLETED)) {
                item.setCompleted(getNumber(row, columnMapping, COMPLETED));
            }
            var color = getText(row, columnMapping, COLOR);
            if (color != null) {
                item.setColor(new SolidColor(color));
            }
            series.add(item);
        }
        return Optional.of(series);
    }

    private Optional<AbstractSeries> tryTreemap(List<Map<String, Object>> data,
            Set<String> columns, Map<String, String> columnMapping) {
        if (!hasColumns(columns, ID, PARENT, VALUE)) {
            return Optional.empty();
        }
        var series = new TreeSeries();
        for (var row : data) {
            var id = getText(row, columnMapping, ID);
            var parent = getText(row, columnMapping, PARENT);
            var value = getNumber(row, columnMapping, VALUE);
            if (id == null && parent == null && value == null) {
                continue;
            }
            var item = new TreeSeriesItem();
            item.setId(id);
            item.setParent(parent);
            item.setValue(value);
            setOptionalText(item::setName, row, columnMapping, columns, NAME);
            if (columns.contains(COLOR_VALUE)) {
                item.setColorValue(getNumber(row, columnMapping, COLOR_VALUE));
            }
            series.add(item);
        }
        return Optional.of(series);
    }

    private Optional<AbstractSeries> tryHeatmap(List<Map<String, Object>> data,
            Set<String> columns, Map<String, String> columnMapping) {
        if (!hasColumns(columns, X, Y, VALUE)) {
            return Optional.empty();
        }
        var series = new HeatSeries();
        for (var row : data) {
            var x = getNumber(row, columnMapping, X);
            var y = getNumber(row, columnMapping, Y);
            var value = getNumber(row, columnMapping, VALUE);
            if (x == null || y == null) {
                continue;
            }
            series.addHeatPoint(x.intValue(), y.intValue(), value);
        }
        return Optional.of(series);
    }

    // --- Fallback ---

    private DataSeries convertFallback(List<Map<String, Object>> data,
            Set<String> columns, Map<String, String> columnMapping) {
        var lowerNames = new ArrayList<>(columns);
        if (lowerNames.isEmpty()) {
            return new DataSeries();
        }

        var classification = classifyColumns(data, columnMapping, lowerNames);
        var nameCol = classification.nameCol;
        var valueCol = classification.valueCol;

        if (nameCol == null && valueCol != null && lowerNames.size() == 1) {
            return convertSingleNumericColumn(data, columns, columnMapping,
                    valueCol);
        }
        if (nameCol == null && valueCol != null) {
            return convertAllNumericColumns(data, columns, columnMapping,
                    lowerNames);
        }
        if (nameCol != null && valueCol != null) {
            return convertCategoryAndValue(data, columns, columnMapping,
                    nameCol, valueCol);
        }
        return new DataSeries();
    }

    private static DataSeries convertSingleNumericColumn(
            List<Map<String, Object>> data, Set<String> columns,
            Map<String, String> columnMapping, String valueCol) {
        var series = new DataSeries();
        for (var i = 0; i < data.size(); i++) {
            var row = data.get(i);
            var y = getNumber(row, columnMapping, valueCol);
            if (y != null) {
                var item = new DataSeriesItem(i, y);
                applyColor(item, row, columnMapping, columns);
                series.add(item);
            }
        }
        return series;
    }

    private static DataSeries convertAllNumericColumns(
            List<Map<String, Object>> data, Set<String> columns,
            Map<String, String> columnMapping, List<String> lowerNames) {
        var series = new DataSeries();
        var xCol = lowerNames.get(0);
        var yCol = lowerNames.get(1);
        for (var row : data) {
            var x = getNumber(row, columnMapping, xCol);
            var y = getNumber(row, columnMapping, yCol);
            if (x == null && y == null) {
                continue;
            }
            var item = new DataSeriesItem(x, y);
            applyColor(item, row, columnMapping, columns);
            series.add(item);
        }
        return series;
    }

    private static DataSeries convertCategoryAndValue(
            List<Map<String, Object>> data, Set<String> columns,
            Map<String, String> columnMapping, String nameCol,
            String valueCol) {
        var series = new DataSeries();
        for (var row : data) {
            var name = getText(row, columnMapping, nameCol);
            var y = getNumber(row, columnMapping, valueCol);
            if (name == null && y == null) {
                continue;
            }
            var item = new DataSeriesItem(name, y);
            applyColor(item, row, columnMapping, columns);
            series.add(item);
        }
        return series;
    }

    /**
     * Classifies columns into a category column and a value column by scanning
     * row values to determine types.
     */
    private static ColumnClassification classifyColumns(
            List<Map<String, Object>> data, Map<String, String> columnMapping,
            List<String> lowerNames) {
        String nameCol = null;
        String valueCol = null;
        for (var lowerName : lowerNames) {
            if (nameCol != null && valueCol != null) {
                break;
            }
            var numeric = findColumnType(data, columnMapping, lowerName);
            if (valueCol == null && Boolean.TRUE.equals(numeric)) {
                valueCol = lowerName;
            } else if (nameCol == null && !Boolean.TRUE.equals(numeric)) {
                nameCol = lowerName;
            }
        }

        // If we found a category column but no value column, use the first
        // remaining column as the value column (it may have all-null values).
        if (nameCol != null && valueCol == null && lowerNames.size() >= 2) {
            var finalNameCol = nameCol;
            valueCol = lowerNames.stream()
                    .filter(name -> !name.equals(finalNameCol)).findFirst()
                    .orElse(null);
        }

        return new ColumnClassification(nameCol, valueCol);
    }

    /**
     * Determines whether a column is numeric by finding the first non-null
     * value across all rows.
     *
     * @return {@code true} if numeric, {@code false} if non-numeric,
     *         {@code null} if all values are null
     */
    private static Boolean findColumnType(List<Map<String, Object>> data,
            Map<String, String> columnMapping, String lowerName) {
        return data.stream().map(row -> getRaw(row, columnMapping, lowerName))
                .filter(Objects::nonNull).findFirst()
                .map(DefaultDataConverter::isNumericValue).orElse(null);
    }

    private record ColumnClassification(String nameCol,
            String valueCol) implements Serializable {
    }

    // --- Value accessors ---

    private static Object getRaw(Map<String, Object> row,
            Map<String, String> columnMapping, String lowerName) {
        var originalKey = columnMapping.get(lowerName);
        return originalKey == null ? null : row.get(originalKey);
    }

    private static Number getNumber(Map<String, Object> row,
            Map<String, String> columnMapping, String column) {
        return toNumber(getRaw(row, columnMapping, column));
    }

    private static String getText(Map<String, Object> row,
            Map<String, String> columnMapping, String column) {
        return toText(getRaw(row, columnMapping, column));
    }

    private static Instant getInstant(Map<String, Object> row,
            Map<String, String> columnMapping, String column) {
        return toInstant(getRaw(row, columnMapping, column));
    }

    // --- Utility methods ---

    private static Number resolveX(Map<String, Object> row,
            Map<String, String> columnMapping, String xCol, int rowIndex) {
        return xCol != null ? getNumber(row, columnMapping, xCol) : rowIndex;
    }

    private static void applyColor(DataSeriesItem item, Map<String, Object> row,
            Map<String, String> columnMapping, Set<String> columns) {
        if (columns.contains(COLOR)) {
            var color = getText(row, columnMapping, COLOR);
            if (color != null) {
                item.setColor(new SolidColor(color));
            }
        }
    }

    private static void setOptionalText(Consumer<String> setter,
            Map<String, Object> row, Map<String, String> columnMapping,
            Set<String> columns, String columnName) {
        if (columns.contains(columnName)) {
            setter.accept(getText(row, columnMapping, columnName));
        }
    }

    private static boolean hasColumns(Set<String> columns, String... required) {
        return Arrays.stream(required).allMatch(columns::contains);
    }

    private static boolean hasBoxPlotColumns(Set<String> columns) {
        return columns.contains(Q1) || columns.contains(MEDIAN)
                || columns.contains(Q3);
    }

    /**
     * Checks whether at least one row has a temporal or numeric value in the
     * {@code _start} or {@code _end} column.
     */
    private static boolean hasTemporalStartEnd(List<Map<String, Object>> data,
            Map<String, String> columnMapping) {
        for (var row : data) {
            if (isTemporalOrNumeric(getRaw(row, columnMapping, START))) {
                return true;
            }
            if (isTemporalOrNumeric(getRaw(row, columnMapping, END))) {
                return true;
            }
        }
        return false;
    }

    // --- Value conversion methods ---

    /**
     * Builds a mapping from lowercase column name to the original-case key as
     * it appears in the row map. If multiple columns map to the same lowercase
     * name (e.g. "Name" and "NAME"), the first one wins and a warning is
     * logged.
     */
    private static Map<String, String> buildColumnMapping(
            Map<String, Object> row) {
        var mapping = new LinkedHashMap<String, String>();
        for (var key : row.keySet()) {
            var lowerKey = key.toLowerCase(Locale.ENGLISH);
            var existing = mapping.putIfAbsent(lowerKey, key);
            if (existing != null) {
                LOGGER.warn(
                        "Duplicate column name after case folding: '{}'"
                                + " and '{}' both map to '{}'. Using '{}'.",
                        existing, key, lowerKey, existing);
            }
        }
        return mapping;
    }

    /**
     * Converts a value to a {@link Number}. Handles {@link Number},
     * {@link Instant}, {@link Timestamp}, {@link java.sql.Date},
     * {@link LocalDate}, {@link LocalDateTime}, {@link Date}, and numeric
     * strings.
     */
    private static Number toNumber(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number;
        }
        var instant = toInstant(value);
        if (instant != null) {
            return Util.toHighchartsTS(instant);
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converts a value to an {@link Instant}. Handles {@link Instant},
     * {@link Timestamp}, {@link java.sql.Date}, {@link LocalDate},
     * {@link LocalDateTime}, {@link Date}, and numeric values (interpreted as
     * milliseconds since epoch).
     */
    private static Instant toInstant(Object value) {
        return switch (value) {
        case Instant instant -> instant;
        case Timestamp timestamp -> timestamp.toInstant();
        case java.sql.Date sqlDate ->
            sqlDate.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        case LocalDate localDate ->
            localDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        case LocalDateTime localDateTime ->
            localDateTime.toInstant(ZoneOffset.UTC);
        case Date date -> date.toInstant();
        case Number number -> Instant.ofEpochMilli(number.longValue());
        case null, default -> null;
        };
    }

    private static String toText(Object value) {
        return value == null ? null : value.toString();
    }

    private static boolean isTemporalOrNumeric(Object value) {
        return value instanceof Number || value instanceof Instant
                || value instanceof LocalDate || value instanceof LocalDateTime
                || value instanceof Date;
    }

    /**
     * Checks whether a value is numeric (a {@link Number} or a parseable
     * numeric string). Temporal types are intentionally <b>not</b> considered
     * numeric here: in the fallback path, date columns are more useful as
     * category labels than as millisecond Y-values. Named pattern matchers use
     * {@link #toNumber(Object)} directly, which does handle temporals.
     */
    private static boolean isNumericValue(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Number) {
            return true;
        }
        try {
            Double.parseDouble(value.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
