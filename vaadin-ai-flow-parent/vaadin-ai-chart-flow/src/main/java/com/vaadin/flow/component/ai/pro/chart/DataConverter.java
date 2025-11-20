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
 *
 * @author Vaadin Ltd
 */
public interface DataConverter extends Serializable {

    /**
     * Converts database query results into a DataSeries for use in Vaadin
     * Charts.
     * <p>
     * The converter should analyze the structure of the results and create an
     * appropriate DataSeries. Common patterns include:
     * </p>
     * <ul>
     * <li>Two columns: first column as X-axis categories, second as Y-axis
     * values</li>
     * <li>Three or more columns: first column as categories, remaining columns
     * as multiple series</li>
     * <li>Numeric pairs: X-Y scatter plot data</li>
     * </ul>
     *
     * @param queryResults
     *            the database query results, where each map represents a row
     *            with column names as keys
     * @return a DataSeries containing the converted data
     * @throws IllegalArgumentException
     *             if the query results cannot be converted
     */
    DataSeries convertToDataSeries(List<Map<String, Object>> queryResults);
}
