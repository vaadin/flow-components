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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.charts.model.BoxPlotItem;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.DataSeriesItem3d;
import com.vaadin.flow.component.charts.model.DataSeriesItemBullet;
import com.vaadin.flow.component.charts.model.DataSeriesItemSankey;
import com.vaadin.flow.component.charts.model.DataSeriesItemXrange;
import com.vaadin.flow.component.charts.model.OhlcItem;

/**
 * Default implementation of DataConverter that handles common data conversion
 * patterns for various chart types.
 * <p>
 * This converter uses intelligent detection based on column count and naming
 * patterns to determine the appropriate data structure.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class DefaultDataConverter implements DataConverter {

    @Override
    public DataSeries convertToDataSeries(
            List<Map<String, Object>> queryResults) {
        if (queryResults == null || queryResults.isEmpty()) {
            return new DataSeries();
        }

        Map<String, Object> firstRow = queryResults.get(0);
        List<String> columnNames = new ArrayList<>(firstRow.keySet());

        if (columnNames.isEmpty()) {
            throw new IllegalArgumentException(
                    "Query results must have at least 1 column");
        }

        return switch (columnNames.size()) {
        case 1 -> convertSingleColumn(queryResults, columnNames);
        case 2 -> convertTwoColumns(queryResults, columnNames);
        case 3 -> convertThreeColumns(queryResults, columnNames);
        case 4 -> convertTwoColumns(queryResults, columnNames.subList(0, 2));
        case 5 -> convertFiveColumns(queryResults, columnNames);
        default -> convertTwoColumns(queryResults, columnNames.subList(0, 2));
        };
    }

    private DataSeries convertSingleColumn(
            List<Map<String, Object>> queryResults,
            List<String> columnNames) {
        DataSeries series = new DataSeries();
        String columnName = columnNames.get(0);
        Map<String, Integer> valueCounts = new java.util.LinkedHashMap<>();

        for (Map<String, Object> row : queryResults) {
            Object valueObj = row.get(columnName);
            String category = valueObj != null ? valueObj.toString()
                    : "Unknown";
            valueCounts.merge(category, 1, Integer::sum);
        }

        for (Map.Entry<String, Integer> entry : valueCounts.entrySet()) {
            series.add(new DataSeriesItem(entry.getKey(), entry.getValue()));
        }
        return series;
    }

    private DataSeries convertTwoColumns(
            List<Map<String, Object>> queryResults,
            List<String> columnNames) {
        DataSeries series = new DataSeries();
        String col1 = columnNames.get(0);
        String col2 = columnNames.get(1);

        for (Map<String, Object> row : queryResults) {
            Object val1 = row.get(col1);
            Object val2 = row.get(col2);

            if (isNumeric(val1) && isNumeric(val2)) {
                series.add(new DataSeriesItem(convertToNumber(val1),
                        convertToNumber(val2)));
            } else {
                String category = val1 != null ? val1.toString() : "Unknown";
                series.add(new DataSeriesItem(category, convertToNumber(val2)));
            }
        }
        return series;
    }

    private DataSeries convertThreeColumns(
            List<Map<String, Object>> queryResults,
            List<String> columnNames) {
        DataSeries series = new DataSeries();
        String col1Lower = columnNames.get(0).toLowerCase();
        String col2Lower = columnNames.get(1).toLowerCase();
        String col3Lower = columnNames.get(2).toLowerCase();

        // Sankey: from, to, weight
        if ((col1Lower.contains("from") || col1Lower.contains("source"))
                && (col2Lower.contains("to") || col2Lower.contains("target")
                        || col2Lower.contains("dest"))
                && (col3Lower.contains("weight")
                        || col3Lower.contains("value")
                        || col3Lower.contains("flow"))) {
            for (Map<String, Object> row : queryResults) {
                String from = String.valueOf(row.get(columnNames.get(0)));
                String to = String.valueOf(row.get(columnNames.get(1)));
                Number weight = convertToNumber(row.get(columnNames.get(2)));
                series.add(new DataSeriesItemSankey(from, to, weight));
            }
            return series;
        }

        // Xrange/Gantt: x/start, x2/end, y
        if ((col1Lower.contains("start") || col1Lower.equals("x"))
                && (col2Lower.contains("end") || col2Lower.equals("x2"))
                && (col3Lower.equals("y") || col3Lower.contains("category")
                        || col3Lower.contains("row"))) {
            for (Map<String, Object> row : queryResults) {
                Number x = convertToNumber(row.get(columnNames.get(0)));
                Number x2 = convertToNumber(row.get(columnNames.get(1)));
                Number y = convertToNumber(row.get(columnNames.get(2)));
                series.add(new DataSeriesItemXrange(x, x2, y));
            }
            return series;
        }

        // Bullet: category, y, target
        if (col3Lower.contains("target")) {
            for (Map<String, Object> row : queryResults) {
                Object cat = row.get(columnNames.get(0));
                Number y = convertToNumber(row.get(columnNames.get(1)));
                Number target = convertToNumber(row.get(columnNames.get(2)));
                if (isNumeric(cat)) {
                    series.add(new DataSeriesItemBullet(convertToNumber(cat), y,
                            target));
                } else {
                    DataSeriesItemBullet item = new DataSeriesItemBullet(y,
                            target);
                    item.setName(cat != null ? cat.toString() : "Unknown");
                    series.add(item);
                }
            }
            return series;
        }

        // Range charts: x, low, high
        if ((col2Lower.contains("low") || col2Lower.contains("min"))
                && (col3Lower.contains("high")
                        || col3Lower.contains("max"))) {
            for (Map<String, Object> row : queryResults) {
                Object xVal = row.get(columnNames.get(0));
                Number low = convertToNumber(row.get(columnNames.get(1)));
                Number high = convertToNumber(row.get(columnNames.get(2)));
                if (isNumeric(xVal)) {
                    series.add(new DataSeriesItem(convertToNumber(xVal), low,
                            high));
                } else {
                    DataSeriesItem item = new DataSeriesItem();
                    item.setName(xVal != null ? xVal.toString() : "Unknown");
                    item.setLow(low);
                    item.setHigh(high);
                    series.add(item);
                }
            }
            return series;
        }

        // Bubble chart: all numeric X, Y, Z
        Object val1 = queryResults.get(0).get(columnNames.get(0));
        Object val2 = queryResults.get(0).get(columnNames.get(1));
        Object val3 = queryResults.get(0).get(columnNames.get(2));
        if (isNumeric(val1) && isNumeric(val2) && isNumeric(val3)) {
            for (Map<String, Object> row : queryResults) {
                Number x = convertToNumber(row.get(columnNames.get(0)));
                Number y = convertToNumber(row.get(columnNames.get(1)));
                Number z = convertToNumber(row.get(columnNames.get(2)));
                series.add(new DataSeriesItem3d(x, y, z));
            }
            return series;
        }

        return convertTwoColumns(queryResults, columnNames.subList(0, 2));
    }

    private DataSeries convertFiveColumns(
            List<Map<String, Object>> queryResults,
            List<String> columnNames) {
        DataSeries series = new DataSeries();
        String col2Lower = columnNames.get(1).toLowerCase();
        String col3Lower = columnNames.get(2).toLowerCase();
        String col4Lower = columnNames.get(3).toLowerCase();
        String col5Lower = columnNames.get(4).toLowerCase();

        // OHLC/Candlestick
        if ((col2Lower.contains("open") || col2Lower.equals("o"))
                && (col3Lower.contains("high") || col3Lower.equals("h"))
                && (col4Lower.contains("low") || col4Lower.equals("l"))
                && (col5Lower.contains("close") || col5Lower.equals("c"))) {
            for (Map<String, Object> row : queryResults) {
                Number x = convertToNumber(row.get(columnNames.get(0)));
                Number open = convertToNumber(row.get(columnNames.get(1)));
                Number high = convertToNumber(row.get(columnNames.get(2)));
                Number low = convertToNumber(row.get(columnNames.get(3)));
                Number close = convertToNumber(row.get(columnNames.get(4)));
                series.add(new OhlcItem(x, open, high, low, close));
            }
            return series;
        }

        // BoxPlot
        String col1Lower = columnNames.get(0).toLowerCase();
        if ((col1Lower.contains("low") || col1Lower.contains("min"))
                && (col2Lower.contains("q1") || col2Lower.contains("lower"))
                && (col3Lower.contains("median") || col3Lower.contains("q2")
                        || col3Lower.contains("mid"))
                && (col4Lower.contains("q3") || col4Lower.contains("upper"))
                && (col5Lower.contains("high")
                        || col5Lower.contains("max"))) {
            for (Map<String, Object> row : queryResults) {
                Number low = convertToNumber(row.get(columnNames.get(0)));
                Number q1 = convertToNumber(row.get(columnNames.get(1)));
                Number median = convertToNumber(row.get(columnNames.get(2)));
                Number q3 = convertToNumber(row.get(columnNames.get(3)));
                Number high = convertToNumber(row.get(columnNames.get(4)));
                series.add(new BoxPlotItem(low, q1, median, q3, high));
            }
            return series;
        }

        // All numeric fallback: assume boxplot
        Object val1 = queryResults.get(0).get(columnNames.get(0));
        Object val2 = queryResults.get(0).get(columnNames.get(1));
        Object val3 = queryResults.get(0).get(columnNames.get(2));
        Object val4 = queryResults.get(0).get(columnNames.get(3));
        Object val5 = queryResults.get(0).get(columnNames.get(4));
        if (isNumeric(val1) && isNumeric(val2) && isNumeric(val3)
                && isNumeric(val4) && isNumeric(val5)) {
            for (Map<String, Object> row : queryResults) {
                Number low = convertToNumber(row.get(columnNames.get(0)));
                Number q1 = convertToNumber(row.get(columnNames.get(1)));
                Number median = convertToNumber(row.get(columnNames.get(2)));
                Number q3 = convertToNumber(row.get(columnNames.get(3)));
                Number high = convertToNumber(row.get(columnNames.get(4)));
                series.add(new BoxPlotItem(low, q1, median, q3, high));
            }
            return series;
        }

        return convertTwoColumns(queryResults, columnNames.subList(0, 2));
    }

    private boolean isNumeric(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Number) {
            return true;
        }
        try {
            Double.parseDouble(obj.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Number convertToNumber(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Number) {
            return (Number) obj;
        }
        if (obj instanceof java.util.Date) {
            return ((java.util.Date) obj).getTime();
        }
        if (obj instanceof java.time.Instant) {
            return ((java.time.Instant) obj).toEpochMilli();
        }
        if (obj instanceof java.time.LocalDate) {
            return ((java.time.LocalDate) obj)
                    .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
                    .toEpochMilli();
        }
        if (obj instanceof java.time.LocalDateTime) {
            return ((java.time.LocalDateTime) obj)
                    .atZone(java.time.ZoneId.systemDefault()).toInstant()
                    .toEpochMilli();
        }
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
