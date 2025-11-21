/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.pro.chart;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.*;

/**
 * Tests for {@link ChartState} and {@link DefaultChartState}.
 *
 * @author Vaadin Ltd
 */
public class ChartStateTest {

    @Test
    public void testCreateWithFactoryMethod() {
        String sqlQuery = "SELECT * FROM sales";
        String chartConfig = "{\"title\": {\"text\": \"Sales Chart\"}}";

        ChartState state = ChartState.of(sqlQuery, chartConfig);

        assertNotNull(state);
        assertEquals(sqlQuery, state.getSqlQuery());
        assertEquals(chartConfig, state.getChartConfig());
    }

    @Test
    public void testCreateWithNullValues() {
        ChartState state = ChartState.of(null, null);

        assertNotNull(state);
        assertNull(state.getSqlQuery());
        assertNull(state.getChartConfig());
    }

    @Test
    public void testCreateWithOnlySqlQuery() {
        String sqlQuery = "SELECT * FROM sales";

        ChartState state = ChartState.of(sqlQuery, null);

        assertNotNull(state);
        assertEquals(sqlQuery, state.getSqlQuery());
        assertNull(state.getChartConfig());
    }

    @Test
    public void testCreateWithOnlyChartConfig() {
        String chartConfig = "{\"title\": {\"text\": \"Sales Chart\"}}";

        ChartState state = ChartState.of(null, chartConfig);

        assertNotNull(state);
        assertNull(state.getSqlQuery());
        assertEquals(chartConfig, state.getChartConfig());
    }

    @Test
    public void testEquals() {
        String sqlQuery = "SELECT * FROM sales";
        String chartConfig = "{\"title\": {\"text\": \"Sales Chart\"}}";

        ChartState state1 = ChartState.of(sqlQuery, chartConfig);
        ChartState state2 = ChartState.of(sqlQuery, chartConfig);

        assertEquals(state1, state2);
        assertEquals(state1.hashCode(), state2.hashCode());
    }

    @Test
    public void testNotEquals_DifferentSqlQuery() {
        String chartConfig = "{\"title\": {\"text\": \"Sales Chart\"}}";

        ChartState state1 = ChartState.of("SELECT * FROM sales", chartConfig);
        ChartState state2 = ChartState.of("SELECT * FROM revenue",
                chartConfig);

        assertNotEquals(state1, state2);
    }

    @Test
    public void testNotEquals_DifferentChartConfig() {
        String sqlQuery = "SELECT * FROM sales";

        ChartState state1 = ChartState.of(sqlQuery,
                "{\"title\": {\"text\": \"Sales\"}}");
        ChartState state2 = ChartState.of(sqlQuery,
                "{\"title\": {\"text\": \"Revenue\"}}");

        assertNotEquals(state1, state2);
    }

    @Test
    public void testEqualsWithNull() {
        ChartState state = ChartState.of(null, null);

        assertNotEquals(null, state);
    }

    @Test
    public void testEqualsSameInstance() {
        ChartState state = ChartState.of("SELECT 1", "{}");

        assertEquals(state, state);
    }

    @Test
    public void testToString() {
        String sqlQuery = "SELECT * FROM sales";
        String chartConfig = "{\"title\": {\"text\": \"Sales Chart\"}}";

        ChartState state = ChartState.of(sqlQuery, chartConfig);
        String toString = state.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("DefaultChartState"));
        assertTrue(toString.contains("sqlQuery"));
        assertTrue(toString.contains("chartConfig"));
    }

    @Test
    public void testToStringWithLongValues() {
        String longSqlQuery = "SELECT * FROM sales WHERE date > '2020-01-01' AND amount > 1000 ORDER BY date DESC LIMIT 100";
        String longChartConfig = "{\"title\": {\"text\": \"Sales Chart\"}, \"xAxis\": {\"categories\": [\"Jan\", \"Feb\", \"Mar\"]}}";

        ChartState state = ChartState.of(longSqlQuery, longChartConfig);
        String toString = state.toString();

        assertNotNull(toString);
        // toString should truncate values to 50 chars
        assertFalse(toString.contains(longSqlQuery));
        assertFalse(toString.contains(longChartConfig));
        assertTrue(toString.contains("..."));
    }

    @Test
    public void testToStringWithNullValues() {
        ChartState state = ChartState.of(null, null);
        String toString = state.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("null"));
    }

    @Test
    public void testSerialization() throws Exception {
        String sqlQuery = "SELECT * FROM sales";
        String chartConfig = "{\"title\": {\"text\": \"Sales Chart\"}}";

        ChartState original = ChartState.of(sqlQuery, chartConfig);

        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(
                baos.toByteArray());
        ChartState deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            deserialized = (ChartState) ois.readObject();
        }

        // Verify
        assertNotNull(deserialized);
        assertEquals(original.getSqlQuery(), deserialized.getSqlQuery());
        assertEquals(original.getChartConfig(),
                deserialized.getChartConfig());
        assertEquals(original, deserialized);
    }

    @Test
    public void testSerializationWithNullValues() throws Exception {
        ChartState original = ChartState.of(null, null);

        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(
                baos.toByteArray());
        ChartState deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            deserialized = (ChartState) ois.readObject();
        }

        // Verify
        assertNotNull(deserialized);
        assertNull(deserialized.getSqlQuery());
        assertNull(deserialized.getChartConfig());
        assertEquals(original, deserialized);
    }

    @Test
    public void testSerializationWithComplexJson() throws Exception {
        String sqlQuery = "SELECT region, SUM(amount) as total FROM sales GROUP BY region";
        String chartConfig = """
                {
                  "chart": {"type": "column"},
                  "title": {"text": "Sales by Region"},
                  "xAxis": {"title": {"text": "Region"}},
                  "yAxis": {"title": {"text": "Total Sales"}},
                  "series": [{"name": "Sales", "data": []}]
                }
                """;

        ChartState original = ChartState.of(sqlQuery, chartConfig);

        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(
                baos.toByteArray());
        ChartState deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            deserialized = (ChartState) ois.readObject();
        }

        // Verify
        assertNotNull(deserialized);
        assertEquals(sqlQuery, deserialized.getSqlQuery());
        assertEquals(chartConfig, deserialized.getChartConfig());
    }

    @Test
    public void testImmutability() {
        String sqlQuery = "SELECT * FROM sales";
        String chartConfig = "{\"title\": {\"text\": \"Sales Chart\"}}";

        ChartState state = ChartState.of(sqlQuery, chartConfig);

        // Verify we get the same values
        assertEquals(sqlQuery, state.getSqlQuery());
        assertEquals(chartConfig, state.getChartConfig());

        // Access multiple times - values should remain consistent
        for (int i = 0; i < 5; i++) {
            assertEquals(sqlQuery, state.getSqlQuery());
            assertEquals(chartConfig, state.getChartConfig());
        }
    }
}
