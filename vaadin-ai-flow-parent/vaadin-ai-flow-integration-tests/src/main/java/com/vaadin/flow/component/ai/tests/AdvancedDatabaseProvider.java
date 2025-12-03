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
 * Advanced in-memory H2 database provider with comprehensive dataset supporting
 * all chart types including: bullet, boxplot, OHLC/candlestick, sankey, xrange/gantt,
 * range charts, bubble, and more.
 * <p>
 * This provider is designed specifically to demonstrate the full capabilities of
 * the ChartAiController with various complex chart types.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class AdvancedDatabaseProvider implements DatabaseProvider {

    private static final String DB_URL = "jdbc:h2:mem:advanceddb;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private volatile boolean initialized = false;

    public AdvancedDatabaseProvider() {
        initializeDatabase();
    }

    private synchronized void initializeDatabase() {
        if (initialized) {
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Basic sales data for simple charts
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS sales (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        "MONTH" VARCHAR(50) NOT NULL,
                        revenue INT NOT NULL,
                        target INT NOT NULL
                    )
                    """);

            // Weather data for range charts (arearange, columnrange)
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS weather (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        "MONTH" VARCHAR(50) NOT NULL,
                        temp_low DECIMAL(5,2) NOT NULL,
                        temp_high DECIMAL(5,2) NOT NULL
                    )
                    """);

            // Stock data for OHLC/Candlestick charts
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS stock_prices (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        trade_date DATE NOT NULL,
                        open DECIMAL(10,2) NOT NULL,
                        high DECIMAL(10,2) NOT NULL,
                        low DECIMAL(10,2) NOT NULL,
                        close DECIMAL(10,2) NOT NULL
                    )
                    """);

            // Statistics data for BoxPlot charts
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS test_scores (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        subject VARCHAR(50) NOT NULL,
                        min_score INT NOT NULL,
                        q1 INT NOT NULL,
                        median INT NOT NULL,
                        q3 INT NOT NULL,
                        max_score INT NOT NULL
                    )
                    """);

            // Bubble chart data
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS countries (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        country VARCHAR(50) NOT NULL,
                        gdp_per_capita INT NOT NULL,
                        life_expectancy DECIMAL(4,1) NOT NULL,
                        population INT NOT NULL
                    )
                    """);

            // Sankey diagram data (energy flow)
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS energy_flow (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        source VARCHAR(50) NOT NULL,
                        destination VARCHAR(50) NOT NULL,
                        flow_amount INT NOT NULL
                    )
                    """);

            // Xrange/Gantt chart data (project tasks)
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS project_tasks (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        task_name VARCHAR(100) NOT NULL,
                        start_day INT NOT NULL,
                        end_day INT NOT NULL,
                        task_row INT NOT NULL
                    )
                    """);

            // Scatter plot data
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS scatter_data (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        x_value DECIMAL(10,2) NOT NULL,
                        y_value DECIMAL(10,2) NOT NULL
                    )
                    """);

            // Insert sales data (with targets for bullet charts)
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO sales (\"MONTH\", revenue, target) VALUES (?, ?, ?)")) {
                pstmt.setString(1, "January");
                pstmt.setInt(2, 45000);
                pstmt.setInt(3, 50000);
                pstmt.executeUpdate();

                pstmt.setString(1, "February");
                pstmt.setInt(2, 52000);
                pstmt.setInt(3, 48000);
                pstmt.executeUpdate();

                pstmt.setString(1, "March");
                pstmt.setInt(2, 48000);
                pstmt.setInt(3, 52000);
                pstmt.executeUpdate();

                pstmt.setString(1, "April");
                pstmt.setInt(2, 61000);
                pstmt.setInt(3, 55000);
                pstmt.executeUpdate();

                pstmt.setString(1, "May");
                pstmt.setInt(2, 58000);
                pstmt.setInt(3, 60000);
                pstmt.executeUpdate();

                pstmt.setString(1, "June");
                pstmt.setInt(2, 67000);
                pstmt.setInt(3, 65000);
                pstmt.executeUpdate();
            }

            // Insert weather data (for range charts)
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO weather (\"MONTH\", temp_low, temp_high) VALUES (?, ?, ?)")) {
                String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                double[] lows = {-5, -3, 2, 8, 13, 18, 21, 20, 15, 9, 3, -2};
                double[] highs = {3, 5, 11, 17, 22, 27, 30, 29, 24, 18, 10, 5};

                for (int i = 0; i < months.length; i++) {
                    pstmt.setString(1, months[i]);
                    pstmt.setDouble(2, lows[i]);
                    pstmt.setDouble(3, highs[i]);
                    pstmt.executeUpdate();
                }
            }

            // Insert stock data (for OHLC/Candlestick)
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO stock_prices (trade_date, open, high, low, close) VALUES (?, ?, ?, ?, ?)")) {
                String[] dates = {"2025-01-01", "2025-01-02", "2025-01-03", "2025-01-06", "2025-01-07"};
                double[][] prices = {
                        {100.00, 105.00, 98.50, 103.00},
                        {103.00, 107.50, 102.00, 106.50},
                        {106.50, 108.00, 104.00, 105.00},
                        {105.00, 110.00, 104.50, 109.00},
                        {109.00, 112.00, 108.00, 111.50}
                };

                for (int i = 0; i < dates.length; i++) {
                    pstmt.setString(1, dates[i]);
                    pstmt.setDouble(2, prices[i][0]); // open
                    pstmt.setDouble(3, prices[i][1]); // high
                    pstmt.setDouble(4, prices[i][2]); // low
                    pstmt.setDouble(5, prices[i][3]); // close
                    pstmt.executeUpdate();
                }
            }

            // Insert test scores (for BoxPlot)
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO test_scores (subject, min_score, q1, median, q3, max_score) VALUES (?, ?, ?, ?, ?, ?)")) {
                pstmt.setString(1, "Math");
                pstmt.setInt(2, 45);
                pstmt.setInt(3, 68);
                pstmt.setInt(4, 78);
                pstmt.setInt(5, 88);
                pstmt.setInt(6, 98);
                pstmt.executeUpdate();

                pstmt.setString(1, "English");
                pstmt.setInt(2, 50);
                pstmt.setInt(3, 70);
                pstmt.setInt(4, 82);
                pstmt.setInt(5, 90);
                pstmt.setInt(6, 100);
                pstmt.executeUpdate();

                pstmt.setString(1, "Science");
                pstmt.setInt(2, 40);
                pstmt.setInt(3, 65);
                pstmt.setInt(4, 75);
                pstmt.setInt(5, 85);
                pstmt.setInt(6, 95);
                pstmt.executeUpdate();
            }

            // Insert country data (for Bubble charts)
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO countries (country, gdp_per_capita, life_expectancy, population) VALUES (?, ?, ?, ?)")) {
                pstmt.setString(1, "USA");
                pstmt.setInt(2, 65000);
                pstmt.setDouble(3, 78.5);
                pstmt.setInt(4, 331000);
                pstmt.executeUpdate();

                pstmt.setString(1, "China");
                pstmt.setInt(2, 10500);
                pstmt.setDouble(3, 76.9);
                pstmt.setInt(4, 1400000);
                pstmt.executeUpdate();

                pstmt.setString(1, "Japan");
                pstmt.setInt(2, 40000);
                pstmt.setDouble(3, 84.6);
                pstmt.setInt(4, 126000);
                pstmt.executeUpdate();

                pstmt.setString(1, "Germany");
                pstmt.setInt(2, 46000);
                pstmt.setDouble(3, 81.3);
                pstmt.setInt(4, 83000);
                pstmt.executeUpdate();

                pstmt.setString(1, "India");
                pstmt.setInt(2, 2100);
                pstmt.setDouble(3, 69.7);
                pstmt.setInt(4, 1380000);
                pstmt.executeUpdate();
            }

            // Insert energy flow data (for Sankey)
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO energy_flow (source, destination, flow_amount) VALUES (?, ?, ?)")) {
                pstmt.setString(1, "Coal");
                pstmt.setString(2, "Electricity");
                pstmt.setInt(3, 40);
                pstmt.executeUpdate();

                pstmt.setString(1, "Natural Gas");
                pstmt.setString(2, "Electricity");
                pstmt.setInt(3, 30);
                pstmt.executeUpdate();

                pstmt.setString(1, "Nuclear");
                pstmt.setString(2, "Electricity");
                pstmt.setInt(3, 20);
                pstmt.executeUpdate();

                pstmt.setString(1, "Renewables");
                pstmt.setString(2, "Electricity");
                pstmt.setInt(3, 10);
                pstmt.executeUpdate();

                pstmt.setString(1, "Electricity");
                pstmt.setString(2, "Residential");
                pstmt.setInt(3, 35);
                pstmt.executeUpdate();

                pstmt.setString(1, "Electricity");
                pstmt.setString(2, "Commercial");
                pstmt.setInt(3, 30);
                pstmt.executeUpdate();

                pstmt.setString(1, "Electricity");
                pstmt.setString(2, "Industrial");
                pstmt.setInt(3, 35);
                pstmt.executeUpdate();
            }

            // Insert project tasks (for Xrange/Gantt)
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO project_tasks (task_name, start_day, end_day, task_row) VALUES (?, ?, ?, ?)")) {
                pstmt.setString(1, "Planning");
                pstmt.setInt(2, 0);
                pstmt.setInt(3, 5);
                pstmt.setInt(4, 0);
                pstmt.executeUpdate();

                pstmt.setString(1, "Design");
                pstmt.setInt(2, 5);
                pstmt.setInt(3, 15);
                pstmt.setInt(4, 1);
                pstmt.executeUpdate();

                pstmt.setString(1, "Development");
                pstmt.setInt(2, 15);
                pstmt.setInt(3, 45);
                pstmt.setInt(4, 2);
                pstmt.executeUpdate();

                pstmt.setString(1, "Testing");
                pstmt.setInt(2, 40);
                pstmt.setInt(3, 55);
                pstmt.setInt(4, 3);
                pstmt.executeUpdate();

                pstmt.setString(1, "Deployment");
                pstmt.setInt(2, 55);
                pstmt.setInt(3, 60);
                pstmt.setInt(4, 4);
                pstmt.executeUpdate();
            }

            // Insert scatter data
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO scatter_data (x_value, y_value) VALUES (?, ?)")) {
                double[][] points = {
                        {10.5, 22.3}, {15.2, 28.5}, {20.1, 35.2}, {25.8, 42.1},
                        {30.3, 48.5}, {35.7, 55.8}, {40.2, 62.3}, {45.5, 68.9},
                        {50.1, 75.2}, {55.4, 81.5}
                };

                for (double[] point : points) {
                    pstmt.setDouble(1, point[0]);
                    pstmt.setDouble(2, point[1]);
                    pstmt.executeUpdate();
                }
            }

            initialized = true;
            System.out.println("Advanced in-memory database initialized successfully");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize advanced database", e);
        }
    }

    @Override
    public String getSchema() {
        StringBuilder schema = new StringBuilder("DATABASE SCHEMA - Advanced Charts Dataset:\n\n");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            var dbMetaData = conn.getMetaData();

            // Get all tables
            try (ResultSet tables = dbMetaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    String tableSchema = tables.getString("TABLE_SCHEM");

                    // Skip system tables and information schema - only show PUBLIC schema
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
                            int decimalDigits = columns.getInt("DECIMAL_DIGITS");
                            String isNullable = columns.getString("IS_NULLABLE");

                            schema.append("  - ").append(columnName)
                                    .append(" (").append(columnType);

                            // Add size for VARCHAR and DECIMAL types
                            if (columnType.contains("VARCHAR") || columnType.contains("CHAR")) {
                                schema.append("(").append(columnSize).append(")");
                            } else if (columnType.contains("DECIMAL") || columnType.contains("NUMERIC")) {
                                schema.append("(").append(columnSize).append(",").append(decimalDigits).append(")");
                            }

                            schema.append(")");

                            if ("NO".equals(isNullable)) {
                                schema.append(" NOT NULL");
                            }

                            schema.append("\n");
                        }
                    }

                    // Add chart-specific example queries based on table name
                    schema.append(getExampleQuery(tableName)).append("\n\n");
                }
            }

            // Add chart type examples section
            schema.append("CHART TYPE EXAMPLES:\n");
            schema.append("- Bullet Chart: SELECT \"MONTH\", revenue, target FROM sales\n");
            schema.append("- Range Charts (arearange/columnrange): SELECT \"MONTH\", temp_low, temp_high FROM weather\n");
            schema.append("- OHLC/Candlestick: SELECT trade_date, open, high, low, close FROM stock_prices\n");
            schema.append("- BoxPlot: SELECT min_score, q1, median, q3, max_score FROM test_scores\n");
            schema.append("- Bubble Chart: SELECT gdp_per_capita, life_expectancy, population FROM countries\n");
            schema.append("- Sankey Diagram: SELECT source, destination, flow_amount FROM energy_flow\n");
            schema.append("- Xrange/Gantt: SELECT start_day, end_day, task_row FROM project_tasks\n");
            schema.append("- Scatter Plot: SELECT x_value, y_value FROM scatter_data\n\n");

            schema.append("NOTES:\n");
            schema.append("- Use \"MONTH\" with quotes when querying sales or weather tables (reserved word)\n");
            schema.append("- Column names are designed to match automatic chart type detection\n");
            schema.append("- All tables support standard SQL SELECT queries\n");

        } catch (SQLException e) {
            System.err.println("Failed to retrieve schema: " + e.getMessage());
            return "Error retrieving database schema: " + e.getMessage();
        }

        return schema.toString();
    }

    /**
     * Returns a chart-appropriate example query for the given table.
     */
    private String getExampleQuery(String tableName) {
        return switch (tableName.toUpperCase()) {
            case "SALES" -> "Example (Bullet): SELECT \"MONTH\", revenue, target FROM sales";
            case "WEATHER" -> "Example (Range): SELECT \"MONTH\", temp_low, temp_high FROM weather";
            case "STOCK_PRICES" -> "Example (Candlestick): SELECT trade_date, open, high, low, close FROM stock_prices";
            case "TEST_SCORES" -> "Example (BoxPlot): SELECT min_score, q1, median, q3, max_score FROM test_scores";
            case "COUNTRIES" -> "Example (Bubble): SELECT gdp_per_capita, life_expectancy, population FROM countries";
            case "ENERGY_FLOW" -> "Example (Sankey): SELECT source, destination, flow_amount FROM energy_flow";
            case "PROJECT_TASKS" -> "Example (Gantt): SELECT start_day, end_day, task_row FROM project_tasks";
            case "SCATTER_DATA" -> "Example (Scatter): SELECT x_value, y_value FROM scatter_data";
            default -> "Example: SELECT * FROM " + tableName + " LIMIT 5";
        };
    }

    @Override
    public List<Map<String, Object>> executeQuery(String query) {
        // Validate it's a SELECT query
        String trimmedQuery = query.trim();
        if (!trimmedQuery.toUpperCase().startsWith("SELECT")) {
            throw new IllegalArgumentException(
                    "Only SELECT queries are allowed. Query must start with SELECT.");
        }

        // Additional security check
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

            System.out.println("Query executed: " + query + " (" + results.size() + " rows)");

        } catch (SQLException e) {
            String errorMsg = "Failed to execute query: " + e.getMessage();
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg, e);
        }

        return results;
    }
}
