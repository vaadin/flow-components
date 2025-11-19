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
import com.vaadin.flow.component.charts.model.DataSeriesItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of DataConverter that handles common data conversion
 * patterns.
 * <p>
 * This converter uses the following logic:
 * </p>
 * <ul>
 * <li>For two columns: treats first column as categories and second as
 * values</li>
 * <li>For more than two columns: treats first column as categories and
 * remaining as multiple series</li>
 * <li>Handles numeric and string data types</li>
 * </ul>
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

        DataSeries series = new DataSeries();

        // Get column names from first row
        Map<String, Object> firstRow = queryResults.get(0);
        List<String> columnNames = new ArrayList<>(firstRow.keySet());

        if (columnNames.size() < 2) {
            throw new IllegalArgumentException(
                    "Query results must have at least 2 columns");
        }

        // Simple conversion: assume first column is category/name,
        // second column is value
        String categoryColumn = columnNames.get(0);
        String valueColumn = columnNames.get(1);

        for (Map<String, Object> row : queryResults) {
            Object categoryObj = row.get(categoryColumn);
            Object valueObj = row.get(valueColumn);

            String category = categoryObj != null ? categoryObj.toString()
                    : "Unknown";
            Number value = convertToNumber(valueObj);

            DataSeriesItem item = new DataSeriesItem(category, value);
            series.add(item);
        }

        return series;
    }

    /**
     * Converts an object to a Number.
     *
     * @param obj
     *            the object to convert
     * @return the number value, or 0 if conversion fails
     */
    private Number convertToNumber(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Number) {
            return (Number) obj;
        }
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
