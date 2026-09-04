/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.benchmark;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;

/**
 * {@link DatabaseProvider} backed by an in-memory H2 database, so grid and
 * chart scenarios can be scored by running the query the LLM produced and
 * comparing the rows instead of the SQL text.
 */
final class BenchmarkDatabase implements DatabaseProvider, AutoCloseable {

    private final String schema;
    private final transient Connection connection;

    private BenchmarkDatabase(List<String> schemaStatements,
            List<String> dataStatements) {
        schema = String.join("\n", schemaStatements);
        try {
            // PostgreSQL mode, and VALUE as a plain identifier, so SQL the
            // model writes for a typical production database is not rejected
            // by H2-only keyword rules.
            connection = DriverManager.getConnection("jdbc:h2:mem:"
                    + UUID.randomUUID()
                    + ";DB_CLOSE_DELAY=-1;MODE=PostgreSQL;NON_KEYWORDS=VALUE");
            try (var statement = connection.createStatement()) {
                for (var sql : schemaStatements) {
                    statement.execute(sql);
                }
                for (var sql : dataStatements) {
                    statement.execute(sql);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not set up H2 database", e);
        }
    }

    /**
     * Customers on three continents. Revenue is unique per row so a sorted
     * result has exactly one correct order.
     *
     * @return the database
     */
    static BenchmarkDatabase customers() {
        return new BenchmarkDatabase(
                List.of("""
                        CREATE TABLE customers (
                          id INT PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          country VARCHAR(50) NOT NULL,
                          region VARCHAR(20) NOT NULL, -- 'Europe', 'North America' or 'Asia'
                          revenue DECIMAL(12, 2) NOT NULL -- annual revenue in EUR
                        )"""),
                List.of("""
                        INSERT INTO customers VALUES
                          (1, 'Nordic Traders', 'Finland', 'Europe', 850000.00),
                          (2, 'Alpine Foods', 'Switzerland', 'Europe', 420000.00),
                          (3, 'Iberia Textiles', 'Spain', 'Europe', 130000.00),
                          (4, 'Maple Logistics', 'Canada', 'North America', 610000.00),
                          (5, 'Pacific Devices', 'United States', 'North America', 990000.00),
                          (6, 'Sakura Robotics', 'Japan', 'Asia', 770000.00)"""));
    }

    /**
     * Sales rows with more than one row per region, so a per-region result
     * requires aggregation.
     *
     * @return the database
     */
    static BenchmarkDatabase sales() {
        return new BenchmarkDatabase(List.of("""
                CREATE TABLE sales (
                  id INT PRIMARY KEY,
                  region VARCHAR(20) NOT NULL,
                  sold_on DATE NOT NULL,
                  amount DECIMAL(12, 2) NOT NULL
                )"""), List.of("""
                INSERT INTO sales VALUES
                  (1, 'Europe', '2026-01-15', 1200.00),
                  (2, 'North America', '2026-01-20', 2100.00),
                  (3, 'Europe', '2026-02-03', 2000.00),
                  (4, 'Asia', '2026-02-11', 900.00)"""));
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public List<Map<String, Object>> executeQuery(String sql) {
        var rows = new ArrayList<Map<String, Object>>();
        try (var statement = connection.createStatement();
                var resultSet = statement.executeQuery(sql)) {
            var metaData = resultSet.getMetaData();
            while (resultSet.next()) {
                var row = new LinkedHashMap<String, Object>();
                for (var column = 1; column <= metaData
                        .getColumnCount(); column++) {
                    row.put(metaData.getColumnLabel(column).toLowerCase(),
                            resultSet.getObject(column));
                }
                rows.add(row);
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(
                    "Query failed: " + e.getMessage(), e);
        }
        return rows;
    }

    /**
     * Reads one column from the rows. The lookup matches any label that
     * contains the column name, ignoring case, so the assertion does not depend
     * on how the LLM aliased the column ({@code name}, {@code customers.name},
     * {@code "Customer name"}).
     *
     * @param rows
     *            the query result
     * @param column
     *            the column name
     * @return the column values in row order
     */
    static List<Object> column(List<Map<String, Object>> rows, String column) {
        var values = new ArrayList<Object>();
        for (var row : rows) {
            var key = row.keySet().stream()
                    .filter(k -> k.toLowerCase().contains(column.toLowerCase()))
                    .findFirst().orElseThrow(
                            () -> new AssertionError("Result has no column "
                                    + column + ", only " + row.keySet()));
            values.add(row.get(key));
        }
        return values;
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not close H2 database", e);
        }
    }
}
