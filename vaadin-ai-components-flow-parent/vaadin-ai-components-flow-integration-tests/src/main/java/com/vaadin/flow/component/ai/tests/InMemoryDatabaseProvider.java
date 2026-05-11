/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.ai.tests;

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

import com.vaadin.flow.component.ai.provider.DatabaseProvider;

/**
 * In-memory H2 database implementation of DatabaseProvider for testing and demo
 * purposes.
 *
 * @author Vaadin Ltd
 */
public class InMemoryDatabaseProvider implements DatabaseProvider {

    private static final String DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private static volatile boolean initialized = false;

    public InMemoryDatabaseProvider() {
        initializeDatabase();
    }

    private synchronized void initializeDatabase() {
        if (initialized) {
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER,
                DB_PASSWORD); Statement stmt = conn.createStatement()) {

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS sales (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        "MONTH" VARCHAR(50) NOT NULL,
                        month_order INT NOT NULL,
                        revenue INT NOT NULL,
                        region VARCHAR(50) NOT NULL
                    )
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS employees (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        department VARCHAR(50) NOT NULL,
                        salary INT NOT NULL,
                        age INT NOT NULL
                    )
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS products (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        product_name VARCHAR(100) NOT NULL,
                        category VARCHAR(50) NOT NULL,
                        units_sold INT NOT NULL,
                        price DECIMAL(10, 2) NOT NULL
                    )
                    """);

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO sales (\"MONTH\", month_order, revenue, region) VALUES (?, ?, ?, ?)")) {
                insertSalesRow(pstmt, "January", 1, 45000, "North");
                insertSalesRow(pstmt, "February", 2, 52000, "North");
                insertSalesRow(pstmt, "March", 3, 58000, "North");
                insertSalesRow(pstmt, "April", 4, 61000, "North");
                insertSalesRow(pstmt, "May", 5, 55000, "North");
                insertSalesRow(pstmt, "June", 6, 67000, "North");
                insertSalesRow(pstmt, "January", 1, 38000, "South");
                insertSalesRow(pstmt, "February", 2, 42000, "South");
                insertSalesRow(pstmt, "March", 3, 48000, "South");
                insertSalesRow(pstmt, "April", 4, 53000, "South");
                insertSalesRow(pstmt, "May", 5, 49000, "South");
                insertSalesRow(pstmt, "June", 6, 57000, "South");
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO employees (name, department, salary, age) VALUES (?, ?, ?, ?)")) {
                insertEmployeeRow(pstmt, "John Doe", "Engineering", 85000, 32);
                insertEmployeeRow(pstmt, "Jane Smith", "Sales", 72000, 28);
                insertEmployeeRow(pstmt, "Bob Johnson", "Engineering", 95000,
                        41);
                insertEmployeeRow(pstmt, "Alice Williams", "Marketing", 68000,
                        35);
                insertEmployeeRow(pstmt, "Charlie Brown", "Sales", 75000, 29);
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO products (product_name, category, units_sold, price) VALUES (?, ?, ?, ?)")) {
                insertProductRow(pstmt, "Laptop", "Electronics", 150, 899.99);
                insertProductRow(pstmt, "Mouse", "Accessories", 450, 29.99);
                insertProductRow(pstmt, "Keyboard", "Accessories", 320, 79.99);
                insertProductRow(pstmt, "Monitor", "Electronics", 89, 299.99);
                insertProductRow(pstmt, "Headphones", "Accessories", 210,
                        149.99);
            }

            // Stock prices for OHLC/Candlestick charts
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS stock_prices (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        trade_date DATE NOT NULL,
                        ticker VARCHAR(10) NOT NULL,
                        open_price DECIMAL(10,2) NOT NULL,
                        high_price DECIMAL(10,2) NOT NULL,
                        low_price DECIMAL(10,2) NOT NULL,
                        close_price DECIMAL(10,2) NOT NULL,
                        volume INT NOT NULL
                    )
                    """);

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO stock_prices (trade_date, ticker, open_price, high_price, low_price, close_price, volume) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                insertStockRow(pstmt, "2025-01-06", "ACME", 142.50, 148.20,
                        141.00, 147.80, 52000);
                insertStockRow(pstmt, "2025-01-07", "ACME", 147.80, 151.30,
                        146.50, 149.10, 48000);
                insertStockRow(pstmt, "2025-01-08", "ACME", 149.10, 152.00,
                        147.20, 148.50, 55000);
                insertStockRow(pstmt, "2025-01-09", "ACME", 148.50, 153.80,
                        148.00, 153.20, 63000);
                insertStockRow(pstmt, "2025-01-10", "ACME", 153.20, 155.50,
                        150.00, 151.40, 47000);
                insertStockRow(pstmt, "2025-01-13", "ACME", 151.40, 154.60,
                        149.80, 154.00, 51000);
                insertStockRow(pstmt, "2025-01-14", "ACME", 154.00, 156.20,
                        152.50, 155.80, 58000);
                insertStockRow(pstmt, "2025-01-15", "ACME", 155.80, 158.00,
                        154.30, 157.20, 62000);
                insertStockRow(pstmt, "2025-01-16", "ACME", 157.20, 159.40,
                        155.80, 156.50, 45000);
                insertStockRow(pstmt, "2025-01-17", "ACME", 156.50, 157.80,
                        153.00, 153.90, 53000);
            }

            // Project tasks for Gantt/XRange/Timeline charts
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS project_tasks (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        task_name VARCHAR(100) NOT NULL,
                        phase VARCHAR(50) NOT NULL,
                        start_date DATE NOT NULL,
                        end_date DATE NOT NULL,
                        progress DECIMAL(3,2) NOT NULL,
                        depends_on INT
                    )
                    """);

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO project_tasks (task_name, phase, start_date, end_date, progress, depends_on) VALUES (?, ?, ?, ?, ?, ?)")) {
                insertTaskRow(pstmt, "Requirements", "Planning", "2025-01-01",
                        "2025-01-15", 1.0, null);
                insertTaskRow(pstmt, "Design", "Planning", "2025-01-10",
                        "2025-01-25", 0.8, 1);
                insertTaskRow(pstmt, "Backend Dev", "Development", "2025-01-20",
                        "2025-02-20", 0.5, 2);
                insertTaskRow(pstmt, "Frontend Dev", "Development",
                        "2025-01-25", "2025-02-25", 0.3, 2);
                insertTaskRow(pstmt, "Testing", "QA", "2025-02-15",
                        "2025-03-10", 0.1, null);
                insertTaskRow(pstmt, "Deployment", "Release", "2025-03-05",
                        "2025-03-15", 0.0, 5);
            }

            // Organization hierarchy for Organization chart
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS org_chart (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        emp_name VARCHAR(100) NOT NULL,
                        job_title VARCHAR(100) NOT NULL,
                        manager_id INT,
                        team_color VARCHAR(20)
                    )
                    """);

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO org_chart (emp_name, job_title, manager_id, team_color) VALUES (?, ?, ?, ?)")) {
                insertOrgRow(pstmt, "Sarah CEO", "CEO", null, "#e74c3c");
                insertOrgRow(pstmt, "Tom VP Eng", "VP Engineering", 1,
                        "#3498db");
                insertOrgRow(pstmt, "Lisa VP Sales", "VP Sales", 1, "#2ecc71");
                insertOrgRow(pstmt, "Mike Lead", "Tech Lead", 2, "#3498db");
                insertOrgRow(pstmt, "Anna Dev", "Developer", 4, "#3498db");
                insertOrgRow(pstmt, "Dan Sales", "Sales Manager", 3, "#2ecc71");
            }

            // Energy flow for Sankey diagram
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS energy_flow (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        source_name VARCHAR(50) NOT NULL,
                        target_name VARCHAR(50) NOT NULL,
                        amount DECIMAL(10,2) NOT NULL
                    )
                    """);

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO energy_flow (source_name, target_name, amount) VALUES (?, ?, ?)")) {
                insertFlowRow(pstmt, "Solar", "Electricity", 120);
                insertFlowRow(pstmt, "Wind", "Electricity", 80);
                insertFlowRow(pstmt, "Coal", "Electricity", 200);
                insertFlowRow(pstmt, "Electricity", "Residential", 180);
                insertFlowRow(pstmt, "Electricity", "Industrial", 150);
                insertFlowRow(pstmt, "Electricity", "Commercial", 70);
            }

            // Temperature data for heatmap (day of week x hour)
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS website_traffic (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        day_of_week INT NOT NULL,
                        hour_of_day INT NOT NULL,
                        visitors INT NOT NULL
                    )
                    """);

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO website_traffic (day_of_week, hour_of_day, visitors) VALUES (?, ?, ?)")) {
                // day_of_week: 0=Monday .. 4=Friday
                // hour_of_day: 0=9am .. 7=4pm (0-based index for heatmap)
                int[][] traffic = { { 0, 0, 120 }, { 0, 1, 180 }, { 0, 2, 220 },
                        { 0, 3, 150 }, { 0, 4, 200 }, { 0, 5, 250 },
                        { 0, 6, 190 }, { 0, 7, 160 }, { 1, 0, 130 },
                        { 1, 1, 200 }, { 1, 2, 240 }, { 1, 3, 170 },
                        { 1, 4, 210 }, { 1, 5, 260 }, { 1, 6, 200 },
                        { 1, 7, 170 }, { 2, 0, 110 }, { 2, 1, 170 },
                        { 2, 2, 210 }, { 2, 3, 160 }, { 2, 4, 190 },
                        { 2, 5, 230 }, { 2, 6, 180 }, { 2, 7, 150 },
                        { 3, 0, 140 }, { 3, 1, 210 }, { 3, 2, 250 },
                        { 3, 3, 180 }, { 3, 4, 220 }, { 3, 5, 270 },
                        { 3, 6, 210 }, { 3, 7, 175 }, { 4, 0, 100 },
                        { 4, 1, 150 }, { 4, 2, 180 }, { 4, 3, 130 },
                        { 4, 4, 160 }, { 4, 5, 190 }, { 4, 6, 140 },
                        { 4, 7, 110 } };
                for (int[] row : traffic) {
                    pstmt.setInt(1, row[0]);
                    pstmt.setInt(2, row[1]);
                    pstmt.setInt(3, row[2]);
                    pstmt.executeUpdate();
                }
            }

            // Budget data for waterfall chart
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS budget_items (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        item_name VARCHAR(100) NOT NULL,
                        amount INT NOT NULL,
                        item_type VARCHAR(20)
                    )
                    """);

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO budget_items (item_name, amount, item_type) VALUES (?, ?, ?)")) {
                insertBudgetRow(pstmt, "Revenue", 420000, null);
                insertBudgetRow(pstmt, "Cost of Goods", -180000, null);
                insertBudgetRow(pstmt, "Gross Profit", 0, "intermediate");
                insertBudgetRow(pstmt, "Salaries", -120000, null);
                insertBudgetRow(pstmt, "Marketing", -35000, null);
                insertBudgetRow(pstmt, "Rent", -25000, null);
                insertBudgetRow(pstmt, "Net Profit", 0, "sum");
            }

            // Funnel/Pyramid data (sales pipeline)
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS sales_pipeline (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        stage VARCHAR(50) NOT NULL,
                        prospects INT NOT NULL,
                        sort_order INT NOT NULL
                    )
                    """);

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO sales_pipeline (stage, prospects, sort_order) VALUES (?, ?, ?)")) {
                insertPipelineRow(pstmt, "Leads", 1200, 1);
                insertPipelineRow(pstmt, "Qualified", 800, 2);
                insertPipelineRow(pstmt, "Proposal", 450, 3);
                insertPipelineRow(pstmt, "Negotiation", 200, 4);
                insertPipelineRow(pstmt, "Closed Won", 120, 5);
            }

            // Monthly metrics for gauge charts
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS kpi_metrics (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        metric_name VARCHAR(50) NOT NULL,
                        current_val DECIMAL(10,2) NOT NULL,
                        target_val DECIMAL(10,2) NOT NULL,
                        min_val DECIMAL(10,2) NOT NULL,
                        max_val DECIMAL(10,2) NOT NULL
                    )
                    """);

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO kpi_metrics (metric_name, current_val, target_val, min_val, max_val) VALUES (?, ?, ?, ?, ?)")) {
                insertKpiRow(pstmt, "Customer Satisfaction", 78, 90, 0, 100);
                insertKpiRow(pstmt, "Revenue Target %", 72, 100, 0, 100);
                insertKpiRow(pstmt, "Server Uptime %", 99.7, 99.9, 95, 100);
            }

            // Category hierarchy for treemap
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS expense_categories (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        category_name VARCHAR(100) NOT NULL,
                        parent_category VARCHAR(100),
                        amount DECIMAL(10,2) NOT NULL
                    )
                    """);

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO expense_categories (category_name, parent_category, amount) VALUES (?, ?, ?)")) {
                insertExpenseRow(pstmt, "Operating", null, 0);
                insertExpenseRow(pstmt, "Salaries", "Operating", 120000);
                insertExpenseRow(pstmt, "Rent", "Operating", 25000);
                insertExpenseRow(pstmt, "Utilities", "Operating", 8000);
                insertExpenseRow(pstmt, "Marketing", null, 0);
                insertExpenseRow(pstmt, "Digital Ads", "Marketing", 18000);
                insertExpenseRow(pstmt, "Events", "Marketing", 12000);
                insertExpenseRow(pstmt, "Content", "Marketing", 5000);
            }

            initialized = true;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to initialize in-memory database", e);
        }
    }

    private void insertSalesRow(PreparedStatement pstmt, String month,
            int monthOrder, int revenue, String region) throws SQLException {
        pstmt.setString(1, month);
        pstmt.setInt(2, monthOrder);
        pstmt.setInt(3, revenue);
        pstmt.setString(4, region);
        pstmt.executeUpdate();
    }

    private void insertEmployeeRow(PreparedStatement pstmt, String name,
            String department, int salary, int age) throws SQLException {
        pstmt.setString(1, name);
        pstmt.setString(2, department);
        pstmt.setInt(3, salary);
        pstmt.setInt(4, age);
        pstmt.executeUpdate();
    }

    private void insertProductRow(PreparedStatement pstmt, String productName,
            String category, int unitsSold, double price) throws SQLException {
        pstmt.setString(1, productName);
        pstmt.setString(2, category);
        pstmt.setInt(3, unitsSold);
        pstmt.setDouble(4, price);
        pstmt.executeUpdate();
    }

    private void insertStockRow(PreparedStatement pstmt, String date,
            String ticker, double open, double high, double low, double close,
            int volume) throws SQLException {
        pstmt.setDate(1, java.sql.Date.valueOf(date));
        pstmt.setString(2, ticker);
        pstmt.setDouble(3, open);
        pstmt.setDouble(4, high);
        pstmt.setDouble(5, low);
        pstmt.setDouble(6, close);
        pstmt.setInt(7, volume);
        pstmt.executeUpdate();
    }

    private void insertTaskRow(PreparedStatement pstmt, String name,
            String phase, String startDate, String endDate, double progress,
            Integer dependsOn) throws SQLException {
        pstmt.setString(1, name);
        pstmt.setString(2, phase);
        pstmt.setDate(3, java.sql.Date.valueOf(startDate));
        pstmt.setDate(4, java.sql.Date.valueOf(endDate));
        pstmt.setDouble(5, progress);
        if (dependsOn != null) {
            pstmt.setInt(6, dependsOn);
        } else {
            pstmt.setNull(6, java.sql.Types.INTEGER);
        }
        pstmt.executeUpdate();
    }

    private void insertOrgRow(PreparedStatement pstmt, String name,
            String title, Integer managerId, String color) throws SQLException {
        pstmt.setString(1, name);
        pstmt.setString(2, title);
        if (managerId != null) {
            pstmt.setInt(3, managerId);
        } else {
            pstmt.setNull(3, java.sql.Types.INTEGER);
        }
        pstmt.setString(4, color);
        pstmt.executeUpdate();
    }

    private void insertFlowRow(PreparedStatement pstmt, String source,
            String target, double amount) throws SQLException {
        pstmt.setString(1, source);
        pstmt.setString(2, target);
        pstmt.setDouble(3, amount);
        pstmt.executeUpdate();
    }

    private void insertBudgetRow(PreparedStatement pstmt, String name,
            int amount, String type) throws SQLException {
        pstmt.setString(1, name);
        pstmt.setInt(2, amount);
        if (type != null) {
            pstmt.setString(3, type);
        } else {
            pstmt.setNull(3, java.sql.Types.VARCHAR);
        }
        pstmt.executeUpdate();
    }

    private void insertPipelineRow(PreparedStatement pstmt, String stage,
            int prospects, int sortOrder) throws SQLException {
        pstmt.setString(1, stage);
        pstmt.setInt(2, prospects);
        pstmt.setInt(3, sortOrder);
        pstmt.executeUpdate();
    }

    private void insertKpiRow(PreparedStatement pstmt, String name,
            double current, double target, double min, double max)
            throws SQLException {
        pstmt.setString(1, name);
        pstmt.setDouble(2, current);
        pstmt.setDouble(3, target);
        pstmt.setDouble(4, min);
        pstmt.setDouble(5, max);
        pstmt.executeUpdate();
    }

    private void insertExpenseRow(PreparedStatement pstmt, String name,
            String parent, double amount) throws SQLException {
        pstmt.setString(1, name);
        if (parent != null) {
            pstmt.setString(2, parent);
        } else {
            pstmt.setNull(2, java.sql.Types.VARCHAR);
        }
        pstmt.setDouble(3, amount);
        pstmt.executeUpdate();
    }

    @Override
    public String getSchema() {
        StringBuilder schema = new StringBuilder("DATABASE SCHEMA:\n\n");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER,
                DB_PASSWORD)) {
            var dbMetaData = conn.getMetaData();

            try (ResultSet tables = dbMetaData.getTables(null, null, "%",
                    new String[] { "TABLE" })) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    String tableSchema = tables.getString("TABLE_SCHEM");

                    if (tableSchema != null
                            && !tableSchema.equalsIgnoreCase("PUBLIC")) {
                        continue;
                    }
                    if (tableName.startsWith("INFORMATION_SCHEMA")) {
                        continue;
                    }

                    schema.append("Table: ").append(tableName).append("\n");
                    schema.append("Columns:\n");

                    try (ResultSet columns = dbMetaData.getColumns(null, null,
                            tableName, null)) {
                        while (columns.next()) {
                            String columnName = columns
                                    .getString("COLUMN_NAME");
                            String columnType = columns.getString("TYPE_NAME");
                            int columnSize = columns.getInt("COLUMN_SIZE");
                            String isNullable = columns
                                    .getString("IS_NULLABLE");

                            schema.append("  - ").append(columnName)
                                    .append(" (").append(columnType);

                            if (columnType.contains("VARCHAR")
                                    || columnType.contains("CHAR")) {
                                schema.append("(").append(columnSize)
                                        .append(")");
                            }

                            schema.append(")");

                            if ("NO".equals(isNullable)) {
                                schema.append(" NOT NULL");
                            }

                            schema.append("\n");
                        }
                    }

                    schema.append("Example: SELECT * FROM ").append(tableName)
                            .append(" LIMIT 5\n\n");
                }
            }

            schema.append("NOTES:\n");
            schema.append(
                    "- This is an H2 database. Reserved words like MONTH, VALUE, etc. must be quoted with double quotes when used as identifiers\n");
            schema.append(
                    "- Use \"MONTH\" with quotes when querying the sales table (reserved word)\n");
            schema.append(
                    "- Do NOT use reserved words like VALUE, KEY, ORDER, etc. as column aliases. Use descriptive names instead (e.g. total_revenue, sale_count)\n");
            schema.append("- All tables support standard SQL SELECT queries\n");
            schema.append(
                    "- SALES table: use month_order column for chronological sorting (ORDER BY month_order)\n");
            schema.append(
                    "- WEBSITE_TRAFFIC table: both columns are 0-based indices. day_of_week: 0=Monday, 1=Tuesday, 2=Wednesday, 3=Thursday, 4=Friday. hour_of_day: 0=9am, 1=10am, ..., 7=4pm. Use xAxis/yAxis categories in configuration to set the display labels.\n");

        } catch (SQLException e) {
            return "Error retrieving database schema: " + e.getMessage();
        }

        return schema.toString();
    }

    @Override
    public List<Map<String, Object>> executeQuery(String query) {
        String trimmedQuery = query.trim();
        if (!trimmedQuery.toUpperCase().startsWith("SELECT")) {
            throw new IllegalArgumentException(
                    "Only SELECT queries are allowed.");
        }

        String upperQuery = trimmedQuery.toUpperCase();
        String[] dangerousKeywords = { "INSERT", "UPDATE", "DELETE", "DROP",
                "CREATE", "ALTER", "TRUNCATE", "EXEC", "EXECUTE" };
        for (String keyword : dangerousKeywords) {
            if (upperQuery.contains(keyword)) {
                throw new IllegalArgumentException(
                        "Query contains forbidden keyword: " + keyword);
            }
        }

        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER,
                DB_PASSWORD);
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

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to execute query: " + e.getMessage(), e);
        }

        return results;
    }
}
