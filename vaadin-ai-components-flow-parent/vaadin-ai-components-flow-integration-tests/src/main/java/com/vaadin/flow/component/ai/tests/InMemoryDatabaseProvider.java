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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.ToolException;
import com.vaadin.flow.component.ai.tests.endpoints.SampleData;
import com.vaadin.flow.component.ai.tests.endpoints.SampleH2Database;

/**
 * In-memory H2 database implementation of DatabaseProvider for testing and demo
 * purposes. The tables hold the rows of {@link SampleData}.
 *
 * @author Vaadin Ltd
 */
public class InMemoryDatabaseProvider implements DatabaseProvider {

    private static final String DB_URL = SampleH2Database.TEST_DB_URL;
    private static final String DB_USER = SampleH2Database.ADMIN_USER;
    private static final String DB_PASSWORD = SampleH2Database.ADMIN_PASSWORD;

    public InMemoryDatabaseProvider() {
        SampleH2Database.seedOnce(DB_URL, true);
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
            schema.append(SampleData.NOTES);

        } catch (SQLException e) {
            return "Error retrieving database schema: " + e.getMessage();
        }

        return schema.toString();
    }

    @Override
    public List<Map<String, Object>> executeQuery(String query) {
        String trimmedQuery = query.trim();
        if (!trimmedQuery.toUpperCase().startsWith("SELECT")) {
            throw new ToolException("Only SELECT queries are allowed.");
        }

        String upperQuery = trimmedQuery.toUpperCase();
        String[] dangerousKeywords = { "INSERT", "UPDATE", "DELETE", "DROP",
                "CREATE", "ALTER", "TRUNCATE", "EXEC", "EXECUTE" };
        for (String keyword : dangerousKeywords) {
            if (upperQuery.contains(keyword)) {
                throw new ToolException(
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
            // Syntax errors and unknown tables or columns (SQL state class 42)
            // only quote the query itself, so the LLM can see what to fix.
            // Other errors, such as data conversion errors, can quote row
            // values, so the LLM only gets a generic error for them.
            if (e.getSQLState() != null && e.getSQLState().startsWith("42")) {
                throw new ToolException(e.getMessage(), e);
            }
            throw new RuntimeException(
                    "Failed to execute query: " + e.getMessage(), e);
        }

        return results;
    }
}
