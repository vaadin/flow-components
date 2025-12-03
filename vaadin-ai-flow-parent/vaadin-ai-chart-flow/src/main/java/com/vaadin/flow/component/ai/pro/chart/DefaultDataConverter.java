/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.pro.chart;

import com.vaadin.flow.component.charts.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of DataConverter that handles common data conversion
 * patterns for various chart types.
 * <p>
 * This converter uses intelligent detection based on column count and naming patterns
 * to determine the appropriate data structure:
 * </p>
 * <ul>
 * <li><b>1 column:</b> Counts occurrences of each unique value (histograms)</li>
 * <li><b>2 columns:</b> Category/name and value (line, bar, column, pie) or X-Y scatter</li>
 * <li><b>3 columns:</b> Detects based on naming:
 *   <ul>
 *     <li>Range charts: columns named like 'low'/'high' → arearange, columnrange</li>
 *     <li>Bubble charts: numeric X, Y, Z → bubble chart with size</li>
 *     <li>Sankey: 'from'/'to'/'weight' → sankey diagram</li>
 *     <li>Xrange/Gantt: 'x'/'x2'/'y' or 'start'/'end' → xrange chart</li>
 *     <li>Bullet: 'target' column → bullet chart</li>
 *   </ul>
 * </li>
 * <li><b>5 columns:</b> Detects based on naming:
 *   <ul>
 *     <li>OHLC/Candlestick: 'open'/'high'/'low'/'close' → financial charts</li>
 *     <li>BoxPlot: 'q1'/'median'/'q3' or sequential numeric → box plot</li>
 *   </ul>
 * </li>
 * </ul>
 * <p>
 * For specialized chart types not well-handled by the default heuristics, consider
 * implementing a custom {@link DataConverter}.
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

        // Get column names from first row
        Map<String, Object> firstRow = queryResults.get(0);
        List<String> columnNames = new ArrayList<>(firstRow.keySet());

        if (columnNames.size() < 1) {
            throw new IllegalArgumentException(
                    "Query results must have at least 1 column");
        }

        // Route to appropriate converter based on column count and names
        switch (columnNames.size()) {
            case 1:
                return convertSingleColumn(queryResults, columnNames);
            case 2:
                return convertTwoColumns(queryResults, columnNames);
            case 3:
                return convertThreeColumns(queryResults, columnNames);
            case 4:
                return convertFourColumns(queryResults, columnNames);
            case 5:
                return convertFiveColumns(queryResults, columnNames);
            default:
                // For 6+ columns, fall back to simple two-column logic using first two columns
                return convertTwoColumns(queryResults, columnNames.subList(0, 2));
        }
    }

    /**
     * Converts single column data by counting occurrences.
     * Useful for histogram-type visualizations.
     */
    private DataSeries convertSingleColumn(
            List<Map<String, Object>> queryResults,
            List<String> columnNames) {
        DataSeries series = new DataSeries();
        String columnName = columnNames.get(0);
        Map<String, Integer> valueCounts = new java.util.LinkedHashMap<>();

        // Count occurrences of each value
        for (Map<String, Object> row : queryResults) {
            Object valueObj = row.get(columnName);
            String category = valueObj != null ? valueObj.toString() : "Unknown";
            valueCounts.put(category, valueCounts.getOrDefault(category, 0) + 1);
        }

        // Convert counts to data series
        for (Map.Entry<String, Integer> entry : valueCounts.entrySet()) {
            DataSeriesItem item = new DataSeriesItem(entry.getKey(), entry.getValue());
            series.add(item);
        }

        return series;
    }

    /**
     * Converts two column data.
     * Handles: basic charts (category + value), scatter plots (X + Y)
     */
    private DataSeries convertTwoColumns(
            List<Map<String, Object>> queryResults,
            List<String> columnNames) {
        DataSeries series = new DataSeries();
        String col1 = columnNames.get(0);
        String col2 = columnNames.get(1);

        for (Map<String, Object> row : queryResults) {
            Object val1 = row.get(col1);
            Object val2 = row.get(col2);

            // Try to detect if both values are numeric (scatter plot)
            if (isNumeric(val1) && isNumeric(val2)) {
                // Scatter plot: X, Y
                DataSeriesItem item = new DataSeriesItem(
                        convertToNumber(val1),
                        convertToNumber(val2)
                );
                series.add(item);
            } else {
                // Category + Value
                String category = val1 != null ? val1.toString() : "Unknown";
                Number value = convertToNumber(val2);
                DataSeriesItem item = new DataSeriesItem(category, value);
                series.add(item);
            }
        }

        return series;
    }

    /**
     * Converts three column data.
     * Detects: range charts, bubble, bullet, sankey, xrange/gantt
     */
    private DataSeries convertThreeColumns(
            List<Map<String, Object>> queryResults,
            List<String> columnNames) {
        DataSeries series = new DataSeries();

        // Detect chart type based on column names
        String col1Lower = columnNames.get(0).toLowerCase();
        String col2Lower = columnNames.get(1).toLowerCase();
        String col3Lower = columnNames.get(2).toLowerCase();

        // Sankey: from, to, weight
        if ((col1Lower.contains("from") || col1Lower.contains("source")) &&
            (col2Lower.contains("to") || col2Lower.contains("target") || col2Lower.contains("dest")) &&
            (col3Lower.contains("weight") || col3Lower.contains("value") || col3Lower.contains("flow"))) {
            for (Map<String, Object> row : queryResults) {
                String from = String.valueOf(row.get(columnNames.get(0)));
                String to = String.valueOf(row.get(columnNames.get(1)));
                Number weight = convertToNumber(row.get(columnNames.get(2)));
                series.add(new DataSeriesItemSankey(from, to, weight));
            }
            return series;
        }

        // Xrange/Gantt: x/start, x2/end, y
        if ((col1Lower.contains("start") || col1Lower.equals("x")) &&
            (col2Lower.contains("end") || col2Lower.equals("x2")) &&
            (col3Lower.equals("y") || col3Lower.contains("category") || col3Lower.contains("row"))) {
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

                // If first column is numeric, use X,Y,Target pattern
                if (isNumeric(cat)) {
                    series.add(new DataSeriesItemBullet(convertToNumber(cat), y, target));
                } else {
                    // Otherwise just Y and Target
                    DataSeriesItemBullet item = new DataSeriesItemBullet(y, target);
                    item.setName(cat != null ? cat.toString() : "Unknown");
                    series.add(item);
                }
            }
            return series;
        }

        // Range charts: x, low, high
        if ((col2Lower.contains("low") || col2Lower.contains("min")) &&
            (col3Lower.contains("high") || col3Lower.contains("max"))) {
            for (Map<String, Object> row : queryResults) {
                Object xVal = row.get(columnNames.get(0));
                Number low = convertToNumber(row.get(columnNames.get(1)));
                Number high = convertToNumber(row.get(columnNames.get(2)));

                if (isNumeric(xVal)) {
                    series.add(new DataSeriesItem(convertToNumber(xVal), low, high));
                } else {
                    // Category-based range
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

        // Default fallback: treat as category + value (ignore third column)
        return convertTwoColumns(queryResults, columnNames.subList(0, 2));
    }

    /**
     * Converts four column data.
     * Could be used for custom chart types or falls back to two columns.
     */
    private DataSeries convertFourColumns(
            List<Map<String, Object>> queryResults,
            List<String> columnNames) {
        // Most common: category, value pattern (ignore extra columns)
        return convertTwoColumns(queryResults, columnNames.subList(0, 2));
    }

    /**
     * Converts five column data.
     * Detects: OHLC/Candlestick, BoxPlot
     */
    private DataSeries convertFiveColumns(
            List<Map<String, Object>> queryResults,
            List<String> columnNames) {
        DataSeries series = new DataSeries();

        String col1Lower = columnNames.get(0).toLowerCase();
        String col2Lower = columnNames.get(1).toLowerCase();
        String col3Lower = columnNames.get(2).toLowerCase();
        String col4Lower = columnNames.get(3).toLowerCase();
        String col5Lower = columnNames.get(4).toLowerCase();

        // OHLC/Candlestick: x/time, open, high, low, close
        if ((col2Lower.contains("open") || col2Lower.equals("o")) &&
            (col3Lower.contains("high") || col3Lower.equals("h")) &&
            (col4Lower.contains("low") || col4Lower.equals("l")) &&
            (col5Lower.contains("close") || col5Lower.equals("c"))) {
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

        // BoxPlot: low, q1, median, q3, high
        if ((col1Lower.contains("low") || col1Lower.contains("min")) &&
            (col2Lower.contains("q1") || col2Lower.contains("lower")) &&
            (col3Lower.contains("median") || col3Lower.contains("q2") || col3Lower.contains("mid")) &&
            (col4Lower.contains("q3") || col4Lower.contains("upper")) &&
            (col5Lower.contains("high") || col5Lower.contains("max"))) {
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

        // Alternative BoxPlot detection: all numeric values in order
        Object val1 = queryResults.get(0).get(columnNames.get(0));
        Object val2 = queryResults.get(0).get(columnNames.get(1));
        Object val3 = queryResults.get(0).get(columnNames.get(2));
        Object val4 = queryResults.get(0).get(columnNames.get(3));
        Object val5 = queryResults.get(0).get(columnNames.get(4));

        if (isNumeric(val1) && isNumeric(val2) && isNumeric(val3) &&
            isNumeric(val4) && isNumeric(val5)) {
            // Assume boxplot order
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

        // Default fallback: treat as category + value
        return convertTwoColumns(queryResults, columnNames.subList(0, 2));
    }

    /**
     * Checks if an object represents a numeric value.
     */
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

    /**
     * Converts an object to a Number.
     * Handles Date objects by converting to milliseconds timestamp.
     *
     * @param obj the object to convert
     * @return the number value, or 0 if conversion fails
     */
    private Number convertToNumber(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Number) {
            return (Number) obj;
        }
        // Handle java.util.Date and java.sql.Date by converting to milliseconds
        if (obj instanceof java.util.Date) {
            return ((java.util.Date) obj).getTime();
        }
        // Handle java.time types
        if (obj instanceof java.time.Instant) {
            return ((java.time.Instant) obj).toEpochMilli();
        }
        if (obj instanceof java.time.LocalDate) {
            return ((java.time.LocalDate) obj).atStartOfDay(java.time.ZoneId.systemDefault())
                    .toInstant().toEpochMilli();
        }
        if (obj instanceof java.time.LocalDateTime) {
            return ((java.time.LocalDateTime) obj).atZone(java.time.ZoneId.systemDefault())
                    .toInstant().toEpochMilli();
        }
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
