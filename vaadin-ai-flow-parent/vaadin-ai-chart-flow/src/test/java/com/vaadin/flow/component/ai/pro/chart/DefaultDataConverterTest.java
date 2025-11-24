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
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for {@link DefaultDataConverter}.
 */
public class DefaultDataConverterTest {

    private DefaultDataConverter converter;

    @Before
    public void setUp() {
        converter = new DefaultDataConverter();
    }

    @Test
    public void convertToDataSeries_withNullInput_returnsEmptySeries() {
        DataSeries series = converter.convertToDataSeries(null);

        assertNotNull("Series should not be null", series);
        assertEquals("Series should be empty", 0, series.size());
    }

    @Test
    public void convertToDataSeries_withEmptyList_returnsEmptySeries() {
        List<Map<String, Object>> emptyResults = new ArrayList<>();

        DataSeries series = converter.convertToDataSeries(emptyResults);

        assertNotNull("Series should not be null", series);
        assertEquals("Series should be empty", 0, series.size());
    }

    @Test
    public void convertToDataSeries_withSingleColumn_countsOccurrences() {
        List<Map<String, Object>> results = new ArrayList<>();

        // Add multiple rows with some repeated values
        Map<String, Object> row1 = new LinkedHashMap<>();
        row1.put("age", 25);
        results.add(row1);

        Map<String, Object> row2 = new LinkedHashMap<>();
        row2.put("age", 30);
        results.add(row2);

        Map<String, Object> row3 = new LinkedHashMap<>();
        row3.put("age", 25);
        results.add(row3);

        Map<String, Object> row4 = new LinkedHashMap<>();
        row4.put("age", 35);
        results.add(row4);

        Map<String, Object> row5 = new LinkedHashMap<>();
        row5.put("age", 25);
        results.add(row5);

        DataSeries series = converter.convertToDataSeries(results);

        assertNotNull("Series should not be null", series);
        assertEquals("Series should have 3 unique values", 3, series.size());

        // Should count: 25 appears 3 times, 30 appears 1 time, 35 appears 1 time
        DataSeriesItem item1 = series.get(0);
        assertEquals("First item should be '25'", "25", item1.getName());
        assertEquals("First item count should be 3", 3.0,
                item1.getY().doubleValue(), 0.001);

        DataSeriesItem item2 = series.get(1);
        assertEquals("Second item should be '30'", "30", item2.getName());
        assertEquals("Second item count should be 1", 1.0,
                item2.getY().doubleValue(), 0.001);

        DataSeriesItem item3 = series.get(2);
        assertEquals("Third item should be '35'", "35", item3.getName());
        assertEquals("Third item count should be 1", 1.0,
                item3.getY().doubleValue(), 0.001);
    }

    @Test
    public void convertToDataSeries_withTwoColumns_createsSeriesWithCategoryAndValue() {
        List<Map<String, Object>> results = new ArrayList<>();

        Map<String, Object> row1 = new LinkedHashMap<>();
        row1.put("region", "North");
        row1.put("sales", 1000);
        results.add(row1);

        Map<String, Object> row2 = new LinkedHashMap<>();
        row2.put("region", "South");
        row2.put("sales", 1500);
        results.add(row2);

        DataSeries series = converter.convertToDataSeries(results);

        assertNotNull("Series should not be null", series);
        assertEquals("Series should have 2 items", 2, series.size());

        DataSeriesItem item1 = series.get(0);
        assertEquals("First item name should be 'North'", "North",
                item1.getName());
        assertEquals("First item value should be 1000", 1000.0,
                item1.getY().doubleValue(), 0.001);

        DataSeriesItem item2 = series.get(1);
        assertEquals("Second item name should be 'South'", "South",
                item2.getName());
        assertEquals("Second item value should be 1500", 1500.0,
                item2.getY().doubleValue(), 0.001);
    }

    @Test
    public void convertToDataSeries_withMoreThanTwoColumns_usesFirstTwoColumns() {
        List<Map<String, Object>> results = new ArrayList<>();

        Map<String, Object> row1 = new LinkedHashMap<>();
        row1.put("region", "East");
        row1.put("sales", 2000);
        row1.put("profit", 500); // This column should be ignored
        results.add(row1);

        DataSeries series = converter.convertToDataSeries(results);

        assertNotNull("Series should not be null", series);
        assertEquals("Series should have 1 item", 1, series.size());

        DataSeriesItem item = series.get(0);
        assertEquals("Item name should be 'East'", "East", item.getName());
        assertEquals("Item value should be 2000", 2000.0,
                item.getY().doubleValue(), 0.001);
    }

    @Test
    public void convertToDataSeries_withNullCategoryValue_usesUnknown() {
        List<Map<String, Object>> results = new ArrayList<>();

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("region", null);
        row.put("sales", 1000);
        results.add(row);

        DataSeries series = converter.convertToDataSeries(results);

        assertNotNull("Series should not be null", series);
        assertEquals("Series should have 1 item", 1, series.size());

        DataSeriesItem item = series.get(0);
        assertEquals("Item name should be 'Unknown'", "Unknown",
                item.getName());
    }

    @Test
    public void convertToDataSeries_withNullValue_usesZero() {
        List<Map<String, Object>> results = new ArrayList<>();

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("region", "West");
        row.put("sales", null);
        results.add(row);

        DataSeries series = converter.convertToDataSeries(results);

        assertNotNull("Series should not be null", series);
        assertEquals("Series should have 1 item", 1, series.size());

        DataSeriesItem item = series.get(0);
        assertEquals("Item value should be 0", 0.0,
                item.getY().doubleValue(), 0.001);
    }

    @Test
    public void convertToDataSeries_withIntegerValues_convertsCorrectly() {
        List<Map<String, Object>> results = new ArrayList<>();

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("product", "Widget");
        row.put("quantity", 42);
        results.add(row);

        DataSeries series = converter.convertToDataSeries(results);

        assertNotNull("Series should not be null", series);
        assertEquals("Series should have 1 item", 1, series.size());

        DataSeriesItem item = series.get(0);
        assertEquals("Item value should be 42", 42.0,
                item.getY().doubleValue(), 0.001);
    }

    @Test
    public void convertToDataSeries_withDoubleValues_convertsCorrectly() {
        List<Map<String, Object>> results = new ArrayList<>();

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("product", "Gadget");
        row.put("price", 99.95);
        results.add(row);

        DataSeries series = converter.convertToDataSeries(results);

        assertNotNull("Series should not be null", series);
        assertEquals("Series should have 1 item", 1, series.size());

        DataSeriesItem item = series.get(0);
        assertEquals("Item value should be 99.95", 99.95,
                item.getY().doubleValue(), 0.001);
    }

    @Test
    public void convertToDataSeries_withStringNumberValue_parsesCorrectly() {
        List<Map<String, Object>> results = new ArrayList<>();

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("category", "A");
        row.put("value", "123.45");
        results.add(row);

        DataSeries series = converter.convertToDataSeries(results);

        assertNotNull("Series should not be null", series);
        assertEquals("Series should have 1 item", 1, series.size());

        DataSeriesItem item = series.get(0);
        assertEquals("Item value should be 123.45", 123.45,
                item.getY().doubleValue(), 0.001);
    }

    @Test
    public void convertToDataSeries_withNonNumericStringValue_usesZero() {
        List<Map<String, Object>> results = new ArrayList<>();

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("category", "B");
        row.put("value", "not a number");
        results.add(row);

        DataSeries series = converter.convertToDataSeries(results);

        assertNotNull("Series should not be null", series);
        assertEquals("Series should have 1 item", 1, series.size());

        DataSeriesItem item = series.get(0);
        assertEquals("Item value should be 0", 0.0,
                item.getY().doubleValue(), 0.001);
    }

    @Test
    public void convertToDataSeries_withMultipleRows_convertsAll() {
        List<Map<String, Object>> results = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("month", "Month" + i);
            row.put("revenue", i * 1000);
            results.add(row);
        }

        DataSeries series = converter.convertToDataSeries(results);

        assertNotNull("Series should not be null", series);
        assertEquals("Series should have 5 items", 5, series.size());

        for (int i = 0; i < 5; i++) {
            DataSeriesItem item = series.get(i);
            assertEquals("Item " + i + " name should be correct",
                    "Month" + (i + 1), item.getName());
            assertEquals("Item " + i + " value should be correct",
                    (i + 1) * 1000.0, item.getY().doubleValue(), 0.001);
        }
    }

    @Test
    public void convertToDataSeries_withMixedTypes_handlesCorrectly() {
        List<Map<String, Object>> results = new ArrayList<>();

        Map<String, Object> row1 = new LinkedHashMap<>();
        row1.put("name", "Item1");
        row1.put("value", 100);
        results.add(row1);

        Map<String, Object> row2 = new LinkedHashMap<>();
        row2.put("name", "Item2");
        row2.put("value", 200.5);
        results.add(row2);

        Map<String, Object> row3 = new LinkedHashMap<>();
        row3.put("name", "Item3");
        row3.put("value", "300");
        results.add(row3);

        DataSeries series = converter.convertToDataSeries(results);

        assertNotNull("Series should not be null", series);
        assertEquals("Series should have 3 items", 3, series.size());

        assertEquals("First value should be 100", 100.0,
                series.get(0).getY().doubleValue(), 0.001);
        assertEquals("Second value should be 200.5", 200.5,
                series.get(1).getY().doubleValue(), 0.001);
        assertEquals("Third value should be 300", 300.0,
                series.get(2).getY().doubleValue(), 0.001);
    }

    @Test
    public void convertToDataSeries_withLongValues_convertsCorrectly() {
        List<Map<String, Object>> results = new ArrayList<>();

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", "Record1");
        row.put("count", 9999999999L);
        results.add(row);

        DataSeries series = converter.convertToDataSeries(results);

        assertNotNull("Series should not be null", series);
        assertEquals("Series should have 1 item", 1, series.size());

        DataSeriesItem item = series.get(0);
        assertEquals("Item value should match long value", 9999999999.0,
                item.getY().doubleValue(), 0.001);
    }

    @Test
    public void convertToDataSeries_withZeroValues_handlesCorrectly() {
        List<Map<String, Object>> results = new ArrayList<>();

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("category", "Zero");
        row.put("value", 0);
        results.add(row);

        DataSeries series = converter.convertToDataSeries(results);

        assertNotNull("Series should not be null", series);
        assertEquals("Series should have 1 item", 1, series.size());

        DataSeriesItem item = series.get(0);
        assertEquals("Item value should be 0", 0.0,
                item.getY().doubleValue(), 0.001);
    }

    @Test
    public void convertToDataSeries_withNegativeValues_handlesCorrectly() {
        List<Map<String, Object>> results = new ArrayList<>();

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("category", "Negative");
        row.put("value", -100);
        results.add(row);

        DataSeries series = converter.convertToDataSeries(results);

        assertNotNull("Series should not be null", series);
        assertEquals("Series should have 1 item", 1, series.size());

        DataSeriesItem item = series.get(0);
        assertEquals("Item value should be -100", -100.0,
                item.getY().doubleValue(), 0.001);
    }

    @Test
    public void convertToDataSeries_implementsDataConverterInterface() {
        assertTrue("DefaultDataConverter should implement DataConverter",
                DataConverter.class.isAssignableFrom(DefaultDataConverter.class));
    }
}
