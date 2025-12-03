/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.pro.chart;

import com.vaadin.flow.component.charts.model.DataSeries;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Interface for converting database query results into Vaadin Charts DataSeries
 * objects.
 * <p>
 * Implementations of this interface handle the transformation of tabular
 * database results into chart-appropriate data structures.
 * </p>
 * <p>
 * Different chart types require different data structures. The converter should
 * analyze the query results and create appropriate DataSeriesItem objects based
 * on the number of columns and data patterns.
 * </p>
 *
 * @author Vaadin Ltd
 */
public interface DataConverter extends Serializable {

    /**
     * Converts database query results into a DataSeries for use in Vaadin
     * Charts.
     * <p>
     * The converter should analyze the structure of the results and create an
     * appropriate DataSeries with the correct DataSeriesItem subtype. Common patterns include:
     * </p>
     * <ul>
     * <li><b>1 column:</b> Histogram data (value counts)</li>
     * <li><b>2 columns:</b> Basic charts - category/name and value (line, bar, column, pie, etc.)</li>
     * <li><b>2 columns (both numeric):</b> Scatter plot - X and Y coordinates</li>
     * <li><b>3 columns:</b> Multiple interpretations:
     *   <ul>
     *     <li>Range charts (arearange, columnrange): X, low, high</li>
     *     <li>Bubble charts: X, Y, Z (size)</li>
     *     <li>Bullet charts: category, Y, target</li>
     *     <li>Sankey: from, to, weight</li>
     *     <li>Xrange/Gantt: x (start), x2 (end), y (category)</li>
     *   </ul>
     * </li>
     * <li><b>5 columns:</b> OHLC/Candlestick - X, open, high, low, close</li>
     * <li><b>5 columns (boxplot):</b> low, q1, median, q3, high</li>
     * <li><b>3 columns (heatmap):</b> X, Y, heatScore</li>
     * </ul>
     * <p>
     * The default implementation uses heuristics to detect the chart type based on
     * column count and data patterns. For advanced use cases, implement a custom
     * converter that creates the appropriate DataSeriesItem subclass:
     * </p>
     * <ul>
     * <li>{@link com.vaadin.flow.component.charts.model.DataSeriesItem} - Basic charts</li>
     * <li>{@link com.vaadin.flow.component.charts.model.DataSeriesItem3d} - Bubble charts</li>
     * <li>{@link com.vaadin.flow.component.charts.model.DataSeriesItemBullet} - Bullet charts</li>
     * <li>{@link com.vaadin.flow.component.charts.model.BoxPlotItem} - Box plot charts</li>
     * <li>{@link com.vaadin.flow.component.charts.model.OhlcItem} - OHLC/Candlestick charts</li>
     * <li>{@link com.vaadin.flow.component.charts.model.DataSeriesItemSankey} - Sankey diagrams</li>
     * <li>{@link com.vaadin.flow.component.charts.model.DataSeriesItemXrange} - Xrange/Gantt charts</li>
     * <li>{@link com.vaadin.flow.component.charts.model.FlagItem} - Flag markers</li>
     * </ul>
     *
     * @param queryResults
     *            the database query results, where each map represents a row
     *            with column names as keys
     * @return a DataSeries containing the converted data with appropriate DataSeriesItem objects
     * @throws IllegalArgumentException
     *             if the query results cannot be converted
     */
    DataSeries convertToDataSeries(List<Map<String, Object>> queryResults);
}
