/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.tests;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory H2 database implementation of DatabaseProvider for testing and demo purposes.
 * <p>
 * This provider creates an H2 in-memory database on initialization and populates it
 * with sample sales, employee, and product data. It executes real SQL queries against
 * the database.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class InMemoryDatabaseProvider implements DatabaseProvider {

    private static final String DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private volatile boolean initialized = false;

    public InMemoryDatabaseProvider() {
        initializeDatabase();
    }

    /**
     * Initializes the H2 in-memory database with sample data.
     */
    private synchronized void initializeDatabase() {
        if (initialized) {
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Create sales table
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS sales (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        "MONTH" VARCHAR(50) NOT NULL,
                        revenue INT NOT NULL,
                        region VARCHAR(50) NOT NULL
                    )
                    """);

            // Create employees table
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS employees (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        department VARCHAR(50) NOT NULL,
                        salary INT NOT NULL,
                        age INT NOT NULL
                    )
                    """);

            // Create products table
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS products (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        product_name VARCHAR(100) NOT NULL,
                        category VARCHAR(50) NOT NULL,
                        units_sold INT NOT NULL,
                        price DECIMAL(10, 2) NOT NULL
                    )
                    """);

            // Insert sample sales data
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO sales (\"MONTH\", revenue, region) VALUES (?, ?, ?)")) {
                insertSalesRow(pstmt, "January", 45000, "North");
                insertSalesRow(pstmt, "February", 52000, "North");
                insertSalesRow(pstmt, "March", 48000, "South");
                insertSalesRow(pstmt, "April", 61000, "East");
                insertSalesRow(pstmt, "May", 58000, "West");
                insertSalesRow(pstmt, "June", 67000, "North");
            }

            // Insert sample employee data
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO employees (name, department, salary, age) VALUES (?, ?, ?, ?)")) {
                insertEmployeeRow(pstmt, "John Doe", "Engineering", 85000, 32);
                insertEmployeeRow(pstmt, "Jane Smith", "Sales", 72000, 28);
                insertEmployeeRow(pstmt, "Bob Johnson", "Engineering", 95000, 41);
                insertEmployeeRow(pstmt, "Alice Williams", "Marketing", 68000, 35);
                insertEmployeeRow(pstmt, "Charlie Brown", "Sales", 75000, 29);
            }

            // Insert sample product data
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO products (product_name, category, units_sold, price) VALUES (?, ?, ?, ?)")) {
                insertProductRow(pstmt, "Laptop", "Electronics", 150, 899.99);
                insertProductRow(pstmt, "Mouse", "Accessories", 450, 29.99);
                insertProductRow(pstmt, "Keyboard", "Accessories", 320, 79.99);
                insertProductRow(pstmt, "Monitor", "Electronics", 89, 299.99);
                insertProductRow(pstmt, "Headphones", "Accessories", 210, 149.99);
            }

            initialized = true;
            System.out.println("In-memory database initialized successfully");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize in-memory database", e);
        }
    }

    private void insertSalesRow(PreparedStatement pstmt, String month, int revenue, String region)
            throws SQLException {
        pstmt.setString(1, month);
        pstmt.setInt(2, revenue);
        pstmt.setString(3, region);
        pstmt.executeUpdate();
    }

    private void insertEmployeeRow(PreparedStatement pstmt, String name, String department,
            int salary, int age) throws SQLException {
        pstmt.setString(1, name);
        pstmt.setString(2, department);
        pstmt.setInt(3, salary);
        pstmt.setInt(4, age);
        pstmt.executeUpdate();
    }

    private void insertProductRow(PreparedStatement pstmt, String productName, String category,
            int unitsSold, double price) throws SQLException {
        pstmt.setString(1, productName);
        pstmt.setString(2, category);
        pstmt.setInt(3, unitsSold);
        pstmt.setDouble(4, price);
        pstmt.executeUpdate();
    }

    @Override
    public String getSchema() {
        StringBuilder schema = new StringBuilder("DATABASE SCHEMA:\n\n");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            var dbMetaData = conn.getMetaData();

            // Get all tables
            try (ResultSet tables = dbMetaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    String tableSchema = tables.getString("TABLE_SCHEM");

                    // Skip system tables and information schema
                    if (tableSchema != null && !tableSchema.equalsIgnoreCase("PUBLIC")) {
                        continue;
                    }
                    if (tableName.startsWith("INFORMATION_SCHEMA")) {
                        continue;
                    }

                    schema.append("Table: ").append(tableName).append("\n");
                    schema.append("Columns:\n");

                    // Get columns for this table
                    try (ResultSet columns = dbMetaData.getColumns(null, null, tableName, null)) {
                        while (columns.next()) {
                            String columnName = columns.getString("COLUMN_NAME");
                            String columnType = columns.getString("TYPE_NAME");
                            int columnSize = columns.getInt("COLUMN_SIZE");
                            String isNullable = columns.getString("IS_NULLABLE");

                            schema.append("  - ").append(columnName)
                                    .append(" (").append(columnType);

                            // Add size for VARCHAR and DECIMAL types
                            if (columnType.contains("VARCHAR") || columnType.contains("CHAR")) {
                                schema.append("(").append(columnSize).append(")");
                            }

                            schema.append(")");

                            if ("NO".equals(isNullable)) {
                                schema.append(" NOT NULL");
                            }

                            schema.append("\n");
                        }
                    }

                    // Add a sample query for each table
                    schema.append("Example: SELECT * FROM ").append(tableName).append(" LIMIT 5\n\n");
                }
            }

            // Add special notes about certain columns
            schema.append("NOTES:\n");
            schema.append("- Use \"MONTH\" with quotes when querying the sales table (reserved word)\n");
            schema.append("- All tables support standard SQL SELECT queries\n");

        } catch (SQLException e) {
            System.err.println("Failed to retrieve schema: " + e.getMessage());
            return "Error retrieving database schema: " + e.getMessage();
        }

        return schema.toString();
    }

    @Override
    public List<Map<String, Object>> executeQuery(String query) {
        // Validate it's a SELECT query
        String trimmedQuery = query.trim();
        if (!trimmedQuery.toUpperCase().startsWith("SELECT")) {
            throw new IllegalArgumentException(
                    "Only SELECT queries are allowed. Query must start with SELECT.");
        }

        // Additional security check: reject queries with suspicious keywords
        String upperQuery = trimmedQuery.toUpperCase();
        String[] dangerousKeywords = { "INSERT", "UPDATE", "DELETE", "DROP", "CREATE",
                "ALTER", "TRUNCATE", "EXEC", "EXECUTE" };
        for (String keyword : dangerousKeywords) {
            if (upperQuery.contains(keyword)) {
                throw new IllegalArgumentException(
                        "Query contains forbidden keyword: " + keyword);
            }
        }

        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }

            System.out.println("Query executed successfully: " + query);
            System.out.println("Returned " + results.size() + " rows");

        } catch (SQLException e) {
            String errorMsg = "Failed to execute query: " + e.getMessage();
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg, e);
        }

        return results;
    }
}
