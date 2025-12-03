/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.tests;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for dynamic schema generation in database providers.
 */
public class DatabaseProviderSchemaTest {

    @Test
    public void testInMemoryDatabaseProvider_schemaContainsTables() {
        var provider = new InMemoryDatabaseProvider();
        String schema = provider.getSchema();

        Assert.assertNotNull("Schema should not be null", schema);
        Assert.assertTrue("Schema should contain 'DATABASE SCHEMA'",
                schema.contains("DATABASE SCHEMA"));
        Assert.assertTrue("Schema should contain sales table",
                schema.contains("Table: SALES") || schema.contains("Table: sales"));
        Assert.assertTrue("Schema should contain employees table",
                schema.contains("Table: EMPLOYEES") || schema.contains("Table: employees"));
        Assert.assertTrue("Schema should contain products table",
                schema.contains("Table: PRODUCTS") || schema.contains("Table: products"));
    }

    @Test
    public void testInMemoryDatabaseProvider_schemaContainsColumns() {
        var provider = new InMemoryDatabaseProvider();
        String schema = provider.getSchema();

        Assert.assertTrue("Schema should contain MONTH column",
                schema.contains("MONTH"));
        Assert.assertTrue("Schema should contain revenue column",
                schema.contains("revenue") || schema.contains("REVENUE"));
        Assert.assertTrue("Schema should contain character type (VARCHAR or CHAR)",
                schema.contains("VARCHAR") || schema.contains("CHAR") ||
                schema.contains("CHARACTER"));
    }

    @Test
    public void testAdvancedDatabaseProvider_schemaContainsTables() {
        var provider = new AdvancedDatabaseProvider();
        String schema = provider.getSchema();

        Assert.assertNotNull("Schema should not be null", schema);
        Assert.assertTrue("Schema should contain 'Advanced Charts Dataset'",
                schema.contains("Advanced Charts Dataset"));

        // Check for all expected tables
        String[] expectedTables = {
                "sales", "weather", "stock_prices", "test_scores",
                "countries", "energy_flow", "project_tasks", "scatter_data"
        };

        for (String table : expectedTables) {
            Assert.assertTrue("Schema should contain " + table + " table",
                    schema.toLowerCase().contains("table: " + table));
        }
    }

    @Test
    public void testAdvancedDatabaseProvider_schemaContainsChartExamples() {
        var provider = new AdvancedDatabaseProvider();
        String schema = provider.getSchema();

        Assert.assertTrue("Schema should contain Bullet example",
                schema.contains("Bullet"));
        Assert.assertTrue("Schema should contain Candlestick example",
                schema.contains("Candlestick"));
        Assert.assertTrue("Schema should contain BoxPlot example",
                schema.contains("BoxPlot"));
        Assert.assertTrue("Schema should contain Sankey example",
                schema.contains("Sankey"));
        Assert.assertTrue("Schema should contain Gantt example",
                schema.contains("Gantt"));
    }

    @Test
    public void testAdvancedDatabaseProvider_schemaContainsSpecialColumnNames() {
        var provider = new AdvancedDatabaseProvider();
        String schema = provider.getSchema();

        // Check for special column names that trigger chart type detection
        Assert.assertTrue("Schema should contain 'target' column",
                schema.toLowerCase().contains("target"));
        Assert.assertTrue("Schema should contain 'temp_low' column",
                schema.toLowerCase().contains("temp_low"));
        Assert.assertTrue("Schema should contain 'temp_high' column",
                schema.toLowerCase().contains("temp_high"));
        Assert.assertTrue("Schema should contain 'open' column",
                schema.toLowerCase().contains("open"));
        Assert.assertTrue("Schema should contain 'high' column",
                schema.toLowerCase().contains("high"));
        Assert.assertTrue("Schema should contain 'low' column",
                schema.toLowerCase().contains("low"));
        Assert.assertTrue("Schema should contain 'close' column",
                schema.toLowerCase().contains("close"));
        Assert.assertTrue("Schema should contain 'q1' column",
                schema.toLowerCase().contains("q1"));
        Assert.assertTrue("Schema should contain 'median' column",
                schema.toLowerCase().contains("median"));
        Assert.assertTrue("Schema should contain 'q3' column",
                schema.toLowerCase().contains("q3"));
    }

    @Test
    public void testInMemoryDatabaseProvider_canExecuteQuery() {
        var provider = new InMemoryDatabaseProvider();

        // Test that we can execute a basic query
        var results = provider.executeQuery("SELECT * FROM sales LIMIT 1");
        Assert.assertNotNull("Results should not be null", results);
        Assert.assertFalse("Results should not be empty", results.isEmpty());
    }

    @Test
    public void testAdvancedDatabaseProvider_canExecuteQuery() {
        var provider = new AdvancedDatabaseProvider();

        // Test that we can execute a basic query on each table
        String[] tables = {
                "sales", "weather", "stock_prices", "test_scores",
                "countries", "energy_flow", "project_tasks", "scatter_data"
        };

        for (String table : tables) {
            var results = provider.executeQuery("SELECT * FROM " + table + " LIMIT 1");
            Assert.assertNotNull("Results from " + table + " should not be null", results);
            Assert.assertFalse("Results from " + table + " should not be empty",
                    results.isEmpty());
        }
    }

    @Test
    public void printBasicDatabaseSchema() {
        var provider = new InMemoryDatabaseProvider();
        String schema = provider.getSchema();
        System.out.println("\n" + "=".repeat(80));
        System.out.println("BASIC DATABASE PROVIDER SCHEMA (InMemoryDatabaseProvider)");
        System.out.println("=".repeat(80));
        System.out.println(schema);
        System.out.println("=".repeat(80) + "\n");
    }

    @Test
    public void printAdvancedDatabaseSchema() {
        var provider = new AdvancedDatabaseProvider();
        String schema = provider.getSchema();
        System.out.println("\n" + "=".repeat(80));
        System.out.println("ADVANCED DATABASE PROVIDER SCHEMA (AdvancedDatabaseProvider)");
        System.out.println("=".repeat(80));
        System.out.println(schema);
        System.out.println("=".repeat(80) + "\n");
    }
}
