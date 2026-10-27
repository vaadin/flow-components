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
package com.vaadin.flow.component.ai.tests.endpoints;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

/**
 * Creates and seeds in-memory H2 databases holding the {@link SampleData}
 * tables. The {@link Variant}s are the same tables in databases that behave
 * like different SQL products: plain H2, and H2 in its PostgreSQL and MySQL
 * compatibility modes, where the LLM has to follow the dialect it is told. Each
 * database also offers a read-only user, the account a production provider
 * should connect with. Once seeded, H2 logs every statement on the database,
 * see {@link #logStatements(String)}.
 */
public final class SampleH2Database {

    /** URL of the shared in-memory database of the default variant. */
    public static final String TEST_DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";

    /** Admin user of the databases created here. */
    public static final String ADMIN_USER = "sa";

    /** Password of the admin user. */
    public static final String ADMIN_PASSWORD = "";

    /** Name and password of the read-only user of each variant. */
    public static final String READER_USER = "reader";

    private static final Set<String> SEEDED_URLS = new HashSet<>();

    /**
     * The database variants holding the sample tables.
     */
    public enum Variant {

        /**
         * Plain H2. The month column of the sales table is named
         * {@code "MONTH"}, a quoted H2 keyword that queries must quote.
         */
        DEFAULT(TEST_DB_URL, "H2", true),

        /**
         * H2 in PostgreSQL compatibility mode with lower-case identifiers. The
         * month column is a plain {@code month}, as in PostgreSQL, where MONTH
         * is not reserved.
         */
        POSTGRESQL_MODE(
                "jdbc:h2:mem:sampledb_pg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;NON_KEYWORDS=MONTH;DB_CLOSE_DELAY=-1",
                "PostgreSQL", false),

        /**
         * H2 in MySQL compatibility mode with lower-case identifiers and a
         * plain {@code month} column.
         */
        MYSQL_MODE(
                "jdbc:h2:mem:sampledb_mysql;MODE=MySQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=MONTH;DB_CLOSE_DELAY=-1",
                "MySQL", false);

        private final String url;
        private final String dialect;
        private final boolean quoteMonth;

        Variant(String url, String dialect, boolean quoteMonth) {
            this.url = url;
            this.dialect = dialect;
            this.quoteMonth = quoteMonth;
        }

        /**
         * Returns the JDBC URL of the admin connection, which creates the
         * database and keeps it open.
         *
         * @return the JDBC URL
         */
        public String url() {
            return url;
        }

        /**
         * Returns the name of the SQL dialect the database emulates.
         *
         * @return the dialect name
         */
        public String dialect() {
            return dialect;
        }

        /**
         * Returns a data source connecting as the admin user, seeding the
         * database first if needed.
         *
         * @return the admin data source
         */
        public DataSource dataSource() {
            seedOnce(url, quoteMonth);
            return SampleH2Database.dataSource(url, ADMIN_USER, ADMIN_PASSWORD);
        }

        /**
         * Returns a data source connecting as a user that can only read the
         * sample tables, seeding the database and creating the user first if
         * needed.
         *
         * @return the read-only data source
         */
        public DataSource readOnlyDataSource() {
            seedOnce(url, quoteMonth);
            return SampleH2Database.readOnlyDataSource(url, READER_USER,
                    READER_USER);
        }
    }

    private SampleH2Database() {
    }

    /**
     * Seeds the database at the given URL with the sample tables, once per URL,
     * and has H2 log every later statement on it. Connects as the admin user,
     * which creates an in-memory database if it does not exist yet.
     *
     * @param url
     *            the JDBC URL, which should keep the database open with
     *            {@code DB_CLOSE_DELAY=-1}
     * @param quoteMonth
     *            {@code true} to name the month column of the sales table
     *            {@code "MONTH"}, a quoted H2 keyword, {@code false} to name it
     *            {@code month}, which needs {@code NON_KEYWORDS=MONTH} in the
     *            URL
     */
    public static synchronized void seedOnce(String url, boolean quoteMonth) {
        if (SEEDED_URLS.contains(url)) {
            return;
        }
        try (var connection = DriverManager.getConnection(url, ADMIN_USER,
                ADMIN_PASSWORD)) {
            seed(connection, quoteMonth);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to seed sample database " + url, e);
        }
        logStatements(url);
        SEEDED_URLS.add(url);
    }

    /**
     * Has H2 log every later statement on the database at the given URL, from
     * any connection: at {@code INFO} with its parameters and the number of
     * rows, or at {@code ERROR} with the exception when it fails. H2 writes the
     * log through SLF4J under the {@code h2database} logger.
     *
     * @param url
     *            the JDBC URL of an open database
     */
    public static void logStatements(String url) {
        try (var connection = DriverManager.getConnection(url, ADMIN_USER,
                ADMIN_PASSWORD); var statement = connection.createStatement()) {
            // Trace level 4 hands H2's trace to SLF4J
            statement.execute("SET TRACE_LEVEL_FILE 4");
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to switch on the statement log of " + url, e);
        }
    }

    /**
     * Creates the sample tables through the given connection and inserts the
     * rows of {@link SampleData}.
     *
     * @param connection
     *            an open connection with rights to create tables
     * @param quoteMonth
     *            see {@link #seedOnce(String, boolean)}
     * @throws SQLException
     *             if a statement fails
     */
    public static void seed(Connection connection, boolean quoteMonth)
            throws SQLException {
        var month = quoteMonth ? "\"MONTH\"" : "month";
        try (var statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE sales (
                        id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        %s VARCHAR(50) NOT NULL,
                        month_order INT NOT NULL,
                        revenue INT NOT NULL,
                        region VARCHAR(50) NOT NULL
                    )
                    """.formatted(month));
            statement.execute("""
                    CREATE TABLE employees (
                        id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        department VARCHAR(50) NOT NULL,
                        salary INT NOT NULL,
                        age INT NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE products (
                        id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        product_name VARCHAR(100) NOT NULL,
                        category VARCHAR(50) NOT NULL,
                        units_sold INT NOT NULL,
                        price DECIMAL(10, 2) NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE stock_prices (
                        id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        trade_date DATE NOT NULL,
                        ticker VARCHAR(10) NOT NULL,
                        open_price DECIMAL(10,2) NOT NULL,
                        high_price DECIMAL(10,2) NOT NULL,
                        low_price DECIMAL(10,2) NOT NULL,
                        close_price DECIMAL(10,2) NOT NULL,
                        volume INT NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE project_tasks (
                        id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        task_name VARCHAR(100) NOT NULL,
                        phase VARCHAR(50) NOT NULL,
                        start_date DATE NOT NULL,
                        end_date DATE NOT NULL,
                        progress DECIMAL(3,2) NOT NULL,
                        depends_on INT
                    )
                    """);
            statement.execute("""
                    CREATE TABLE org_chart (
                        id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        emp_name VARCHAR(100) NOT NULL,
                        job_title VARCHAR(100) NOT NULL,
                        manager_id INT,
                        team_color VARCHAR(20)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE energy_flow (
                        id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        source_name VARCHAR(50) NOT NULL,
                        target_name VARCHAR(50) NOT NULL,
                        amount DECIMAL(10,2) NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE website_traffic (
                        id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        day_of_week INT NOT NULL,
                        hour_of_day INT NOT NULL,
                        visitors INT NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE budget_items (
                        id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        item_name VARCHAR(100) NOT NULL,
                        amount INT NOT NULL,
                        item_type VARCHAR(20)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE sales_pipeline (
                        id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        stage VARCHAR(50) NOT NULL,
                        prospects INT NOT NULL,
                        sort_order INT NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE kpi_metrics (
                        id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        metric_name VARCHAR(50) NOT NULL,
                        current_val DECIMAL(10,2) NOT NULL,
                        target_val DECIMAL(10,2) NOT NULL,
                        min_val DECIMAL(10,2) NOT NULL,
                        max_val DECIMAL(10,2) NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE expense_categories (
                        id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        category_name VARCHAR(100) NOT NULL,
                        parent_category VARCHAR(100),
                        amount DECIMAL(10,2) NOT NULL
                    )
                    """);
        }
        for (var table : SampleData.TABLES) {
            insertRows(connection, table, month);
        }
    }

    private static void insertRows(Connection connection,
            SampleData.Table<?> table, String monthColumn) throws SQLException {
        var columns = new ArrayList<String>();
        for (var component : table.componentNames()) {
            columns.add(component.equals("month") ? monthColumn
                    : SampleData.toSnakeCase(component));
        }
        var sql = "INSERT INTO " + table.sqlName() + " ("
                + String.join(", ", columns) + ") VALUES (" + columns.stream()
                        .map(c -> "?").collect(Collectors.joining(", "))
                + ")";
        try (var statement = connection.prepareStatement(sql)) {
            for (var row : table.rowMaps()) {
                int index = 1;
                for (var value : row.values()) {
                    statement.setObject(index++, value);
                }
                statement.executeUpdate();
            }
        }
    }

    /**
     * Creates a data source for an H2 database.
     *
     * @param url
     *            the JDBC URL
     * @param user
     *            the user to connect as
     * @param password
     *            the user's password
     * @return the data source
     */
    public static DataSource dataSource(String url, String user,
            String password) {
        var dataSource = new JdbcDataSource();
        dataSource.setURL(url);
        dataSource.setUser(user);
        dataSource.setPassword(password);
        return dataSource;
    }

    /**
     * Creates a user with read-only access to the whole {@code PUBLIC} schema
     * of the database at the given URL, if it does not exist yet, and returns a
     * data source connecting as that user. The user cannot modify data, create
     * objects or change database settings, so a statement such as
     * {@code DELETE FROM sales} fails with a rights error.
     *
     * @param url
     *            the JDBC URL of a seeded database
     * @param user
     *            the name of the read-only user
     * @param password
     *            the password to give the user
     * @return a data source connecting as the read-only user
     */
    public static synchronized DataSource readOnlyDataSource(String url,
            String user, String password) {
        try (var connection = DriverManager.getConnection(url, ADMIN_USER,
                ADMIN_PASSWORD); var statement = connection.createStatement()) {
            statement.execute("CREATE USER IF NOT EXISTS " + user
                    + " PASSWORD '" + password + "'");
            statement.execute("GRANT SELECT ON SCHEMA PUBLIC TO " + user);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to create read-only user " + user, e);
        }
        return dataSource(withoutAdminSettings(url), user, password);
    }

    /**
     * Strips the settings that need admin rights from a URL, keeping the
     * per-session ones ({@code SCHEMA}, {@code NON_KEYWORDS}) so that a
     * non-admin user can connect with it.
     *
     * @param url
     *            an H2 JDBC URL with settings
     * @return the URL a non-admin user can connect with
     */
    public static String withoutAdminSettings(String url) {
        var parts = url.split(";");
        var kept = Arrays.stream(parts).skip(1)
                .filter(setting -> setting.toUpperCase().startsWith("SCHEMA=")
                        || setting.toUpperCase().startsWith("NON_KEYWORDS="))
                .collect(Collectors.joining(";"));
        return kept.isEmpty() ? parts[0] : parts[0] + ";" + kept;
    }

    /**
     * Returns the SQL names of the sample tables, in table order.
     *
     * @return the table names
     */
    public static List<String> tableNames() {
        return SampleData.TABLES.stream().map(SampleData.Table::sqlName)
                .toList();
    }
}
