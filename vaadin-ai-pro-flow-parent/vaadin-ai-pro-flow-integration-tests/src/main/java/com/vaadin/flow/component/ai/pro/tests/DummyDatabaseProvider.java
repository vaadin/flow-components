/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.pro.tests;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dummy implementation of DatabaseProvider for testing and demo purposes.
 * <p>
 * This provider contains sample sales and employee data.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class DummyDatabaseProvider implements DatabaseProvider {

    @Override
    public String getSchema() {
        return """
                DATABASE SCHEMA:

                Table: sales
                Columns:
                  - month (VARCHAR): Month name
                  - revenue (INTEGER): Monthly revenue in dollars
                  - region (VARCHAR): Sales region (North, South, East, West)

                Table: employees
                Columns:
                  - name (VARCHAR): Employee name
                  - department (VARCHAR): Department name
                  - salary (INTEGER): Annual salary in dollars
                  - age (INTEGER): Employee age

                Table: products
                Columns:
                  - product_name (VARCHAR): Product name
                  - category (VARCHAR): Product category
                  - units_sold (INTEGER): Number of units sold
                  - price (DECIMAL): Unit price in dollars
                """;
    }

    @Override
    public List<Map<String, Object>> executeQuery(String query) {
        // Simple query parsing for demo purposes
        String lowerQuery = query.toLowerCase().trim();

        // Validate it's a SELECT query
        if (!lowerQuery.startsWith("select")) {
            throw new IllegalArgumentException(
                    "Only SELECT queries are allowed");
        }

        // Return dummy data based on which table is queried
        if (lowerQuery.contains("from sales")) {
            return getSalesData();
        } else if (lowerQuery.contains("from employees")) {
            return getEmployeesData();
        } else if (lowerQuery.contains("from products")) {
            return getProductsData();
        } else {
            throw new IllegalArgumentException(
                    "Unknown table in query: " + query);
        }
    }

    private List<Map<String, Object>> getSalesData() {
        List<Map<String, Object>> data = new ArrayList<>();

        String[] months = { "January", "February", "March", "April", "May",
                "June" };
        int[] revenues = { 45000, 52000, 48000, 61000, 58000, 67000 };
        String[] regions = { "North", "North", "South", "East", "West",
                "North" };

        for (int i = 0; i < months.length; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("month", months[i]);
            row.put("revenue", revenues[i]);
            row.put("region", regions[i]);
            data.add(row);
        }

        return data;
    }

    private List<Map<String, Object>> getEmployeesData() {
        List<Map<String, Object>> data = new ArrayList<>();

        String[] names = { "John Doe", "Jane Smith", "Bob Johnson",
                "Alice Williams", "Charlie Brown" };
        String[] departments = { "Engineering", "Sales", "Engineering",
                "Marketing", "Sales" };
        int[] salaries = { 85000, 72000, 95000, 68000, 75000 };
        int[] ages = { 32, 28, 41, 35, 29 };

        for (int i = 0; i < names.length; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("name", names[i]);
            row.put("department", departments[i]);
            row.put("salary", salaries[i]);
            row.put("age", ages[i]);
            data.add(row);
        }

        return data;
    }

    private List<Map<String, Object>> getProductsData() {
        List<Map<String, Object>> data = new ArrayList<>();

        String[] products = { "Laptop", "Mouse", "Keyboard", "Monitor",
                "Headphones" };
        String[] categories = { "Electronics", "Accessories", "Accessories",
                "Electronics", "Accessories" };
        int[] unitsSold = { 150, 450, 320, 89, 210 };
        double[] prices = { 899.99, 29.99, 79.99, 299.99, 149.99 };

        for (int i = 0; i < products.length; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("product_name", products[i]);
            row.put("category", categories[i]);
            row.put("units_sold", unitsSold[i]);
            row.put("price", prices[i]);
            data.add(row);
        }

        return data;
    }
}
