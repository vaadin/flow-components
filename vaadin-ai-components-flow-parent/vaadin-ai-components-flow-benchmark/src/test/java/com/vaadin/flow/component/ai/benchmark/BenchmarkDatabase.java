/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.benchmark;

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
final class BenchmarkDatabase implements DatabaseProvider {

    private final String url;
    private final String schema;

    private BenchmarkDatabase(List<String> schemaStatements,
            List<String> dataStatements) {
        schema = String.join("\n", schemaStatements);
        // PostgreSQL mode, and VALUE as a plain identifier, so SQL the model
        // writes for a typical production database is not rejected by H2-only
        // keyword rules. DB_CLOSE_DELAY keeps the data between the per-query
        // connections until the JVM exits, so there is nothing to close.
        url = "jdbc:h2:mem:" + UUID.randomUUID()
                + ";DB_CLOSE_DELAY=-1;MODE=PostgreSQL;NON_KEYWORDS=VALUE";
        try (var connection = DriverManager.getConnection(url);
                var statement = connection.createStatement()) {
            for (var sql : schemaStatements) {
                statement.execute(sql);
            }
            for (var sql : dataStatements) {
                statement.execute(sql);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not set up H2 database: " + e.getMessage(), e);
        }
    }

    /**
     * Customers on three continents with their orders. Revenue is unique per
     * row so a sorted result has exactly one correct order. Region totals:
     * North America 1,600,000, Europe 1,400,000, Asia 770,000. Iberia Textiles
     * has only 2025 orders and Sakura Robotics none.
     *
     * @return the database
     */
    static BenchmarkDatabase customers() {
        return new BenchmarkDatabase(
                List.of("""
                        CREATE TABLE customers (
                          id INT PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          email VARCHAR(100) NOT NULL,
                          phone VARCHAR(30) NOT NULL,
                          country VARCHAR(50) NOT NULL,
                          region VARCHAR(20) NOT NULL, -- 'Europe', 'North America' or 'Asia'
                          revenue DECIMAL(12, 2) NOT NULL -- annual revenue in EUR
                        )""",
                        """
                                CREATE TABLE orders (
                                  id INT PRIMARY KEY,
                                  customer_id INT NOT NULL REFERENCES customers(id),
                                  ordered_on DATE NOT NULL,
                                  total DECIMAL(12, 2) NOT NULL
                                )"""),
                List.of("""
                        INSERT INTO customers VALUES
                          (1, 'Nordic Traders', 'orders@nordic.example', '+358 9 1234567', 'Finland', 'Europe', 850000.00),
                          (2, 'Alpine Foods', 'hello@alpine.example', '+41 44 1234567', 'Switzerland', 'Europe', 420000.00),
                          (3, 'Iberia Textiles', 'info@iberia.example', '+34 91 1234567', 'Spain', 'Europe', 130000.00),
                          (4, 'Maple Logistics', 'ops@maple.example', '+1 416 1234567', 'Canada', 'North America', 610000.00),
                          (5, 'Pacific Devices', 'sales@pacific.example', '+1 415 1234567', 'United States', 'North America', 990000.00),
                          (6, 'Sakura Robotics', 'contact@sakura.example', '+81 3 12345678', 'Japan', 'Asia', 770000.00)""",
                        """
                                INSERT INTO orders VALUES
                                  (1, 1, '2025-11-02', 12000.00),
                                  (2, 1, '2026-02-14', 8000.00),
                                  (3, 2, '2026-05-30', 4500.00),
                                  (4, 3, '2025-06-01', 2300.00),
                                  (5, 3, '2025-12-24', 1900.00),
                                  (6, 4, '2026-01-09', 15000.00),
                                  (7, 5, '2025-09-15', 30000.00),
                                  (8, 5, '2026-08-01', 27000.00)"""));
    }

    /**
     * Monthly revenue for two regions over six months. Rows are inserted out of
     * calendar order, so a chart in month order needs {@code month_order}.
     *
     * @return the database
     */
    static BenchmarkDatabase regionalSales() {
        return new BenchmarkDatabase(List.of("""
                CREATE TABLE monthly_sales (
                  id INT PRIMARY KEY,
                  month_name VARCHAR(3) NOT NULL, -- 'Jan'..'Jun'
                  month_order INT NOT NULL, -- 1 = Jan
                  region VARCHAR(20) NOT NULL, -- 'North' or 'South'
                  revenue INT NOT NULL
                )"""),
                List.of("""
                        INSERT INTO monthly_sales VALUES
                          (1, 'Mar', 3, 'North', 14000), (2, 'Jan', 1, 'South', 9000),
                          (3, 'Jun', 6, 'North', 20000), (4, 'Feb', 2, 'North', 12000),
                          (5, 'May', 5, 'South', 13000), (6, 'Jan', 1, 'North', 10000),
                          (7, 'Apr', 4, 'South', 11000), (8, 'Jun', 6, 'South', 15000),
                          (9, 'May', 5, 'North', 18000), (10, 'Feb', 2, 'South', 9500),
                          (11, 'Apr', 4, 'North', 16000), (12, 'Mar', 3, 'South', 10500)"""));
    }

    /**
     * Six employees in three departments.
     *
     * @return the database
     */
    static BenchmarkDatabase employees() {
        return new BenchmarkDatabase(
                List.of("""
                        CREATE TABLE employees (
                          id INT PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          department VARCHAR(50) NOT NULL, -- 'Engineering', 'Sales' or 'Marketing'
                          salary INT NOT NULL, -- annual, EUR
                          age INT NOT NULL
                        )"""),
                List.of("""
                        INSERT INTO employees VALUES
                          (1, 'Aino', 'Engineering', 72000, 34), (2, 'Bram', 'Engineering', 88000, 42),
                          (3, 'Chen', 'Sales', 54000, 28), (4, 'Dana', 'Sales', 67000, 39),
                          (5, 'Emil', 'Marketing', 49000, 26), (6, 'Fatima', 'Marketing', 61000, 45)"""));
    }

    /**
     * Deal amounts per sales rep and quarter, inserted out of quarter order.
     * The schema does not list the rep names, so they are only in the data.
     *
     * @return the database
     */
    static BenchmarkDatabase salesReps() {
        return new BenchmarkDatabase(
                List.of("""
                        CREATE TABLE deals (
                          id INT PRIMARY KEY,
                          rep_name VARCHAR(100) NOT NULL, -- the sales rep who closed the deals
                          quarter_name VARCHAR(2) NOT NULL, -- 'Q1'..'Q4'
                          quarter_order INT NOT NULL, -- 1 = Q1
                          amount INT NOT NULL -- EUR
                        )"""),
                List.of("""
                        INSERT INTO deals VALUES
                          (1, 'Mateo Ruiz', 'Q3', 3, 47000), (2, 'Hanna Berg', 'Q1', 1, 38000),
                          (3, 'Olivia Park', 'Q2', 2, 52000), (4, 'Hanna Berg', 'Q4', 4, 61000),
                          (5, 'Mateo Ruiz', 'Q1', 1, 29000), (6, 'Olivia Park', 'Q4', 4, 58000),
                          (7, 'Hanna Berg', 'Q2', 2, 44000), (8, 'Mateo Ruiz', 'Q4', 4, 53000),
                          (9, 'Olivia Park', 'Q1', 1, 41000), (10, 'Hanna Berg', 'Q3', 3, 49000),
                          (11, 'Mateo Ruiz', 'Q2', 2, 35000), (12, 'Olivia Park', 'Q3', 3, 55000)"""));
    }

    /**
     * Website visitors per weekday and hour: 5 days times 4 hours.
     *
     * @return the database
     */
    static BenchmarkDatabase traffic() {
        var rows = new ArrayList<String>();
        var id = 1;
        for (var day = 1; day <= 5; day++) {
            for (var hour = 9; hour <= 12; hour++) {
                rows.add("(" + id++ + ", " + day + ", " + hour + ", "
                        + (100 * day + 7 * hour) + ")");
            }
        }
        return new BenchmarkDatabase(List.of("""
                CREATE TABLE website_traffic (
                  id INT PRIMARY KEY,
                  day_of_week INT NOT NULL, -- 1 = Monday .. 5 = Friday
                  hour_of_day INT NOT NULL, -- 9..12
                  visitors INT NOT NULL
                )"""), List.of("INSERT INTO website_traffic VALUES "
                + String.join(", ", rows)));
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public List<Map<String, Object>> executeQuery(String sql) {
        var rows = new ArrayList<Map<String, Object>>();
        try (var connection = DriverManager.getConnection(url);
                var statement = connection.createStatement();
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
}
