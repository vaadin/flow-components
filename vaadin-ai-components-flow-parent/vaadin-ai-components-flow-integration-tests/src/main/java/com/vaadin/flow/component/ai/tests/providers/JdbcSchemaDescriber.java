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
package com.vaadin.flow.component.ai.tests.providers;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Builds a text description of a database schema from {@link DatabaseMetaData}:
 * tables and views of one schema with their columns, primary keys and foreign
 * keys. A column whose bare name the database rejects in a query, because it is
 * a reserved word, is marked so the LLM quotes it. That check runs the name in
 * a query returning no rows, since the keyword list of
 * {@link DatabaseMetaData#getSQLKeywords()} leaves out words the database still
 * reserves, such as H2's {@code MONTH}.
 */
public final class JdbcSchemaDescriber {

    private static final Set<String> SYSTEM_SCHEMAS = Set.of(
            "INFORMATION_SCHEMA", "PG_CATALOG", "SYS", "MYSQL",
            "PERFORMANCE_SCHEMA");

    private JdbcSchemaDescriber() {
    }

    /**
     * Describes the tables and views of the given schema.
     *
     * @param connection
     *            an open connection
     * @param schema
     *            the schema name as the database reports it, or {@code null}
     *            for all non-system schemas
     * @return the schema description
     * @throws SQLException
     *             if reading the metadata fails
     */
    public static String describe(Connection connection, String schema)
            throws SQLException {
        var metaData = connection.getMetaData();
        var description = new StringBuilder();
        try (ResultSet tables = metaData.getTables(connection.getCatalog(),
                schema, "%", new String[] { "TABLE", "BASE TABLE", "VIEW" })) {
            while (tables.next()) {
                var tableSchema = tables.getString("TABLE_SCHEM");
                if (tableSchema != null && SYSTEM_SCHEMAS
                        .contains(tableSchema.toUpperCase(Locale.ROOT))) {
                    continue;
                }
                var tableName = tables.getString("TABLE_NAME");
                var tableType = tables.getString("TABLE_TYPE");
                description.append("VIEW".equalsIgnoreCase(tableType) ? "View: "
                        : "Table: ").append(tableName).append('\n');
                appendColumns(description, connection, tableSchema, tableName);
                appendPrimaryKey(description, metaData, tableSchema, tableName);
                appendForeignKeys(description, metaData, tableSchema,
                        tableName);
                description.append('\n');
            }
        }
        return description.toString();
    }

    private static void appendColumns(StringBuilder description,
            Connection connection, String schema, String table)
            throws SQLException {
        description.append("Columns:\n");
        try (ResultSet columns = connection.getMetaData().getColumns(null,
                schema, table, "%")) {
            while (columns.next()) {
                var name = columns.getString("COLUMN_NAME");
                var type = columns.getString("TYPE_NAME");
                description.append("  - ").append(name).append(" (")
                        .append(type);
                var upperType = type.toUpperCase(Locale.ROOT);
                if (upperType.contains("CHAR")) {
                    description.append('(')
                            .append(columns.getInt("COLUMN_SIZE")).append(')');
                } else if (upperType.contains("DECIMAL")
                        || upperType.contains("NUMERIC")) {
                    description.append('(')
                            .append(columns.getInt("COLUMN_SIZE")).append(", ")
                            .append(columns.getInt("DECIMAL_DIGITS"))
                            .append(')');
                }
                description.append(')');
                if ("NO".equals(columns.getString("IS_NULLABLE"))) {
                    description.append(" NOT NULL");
                }
                if (isReservedWord(connection, schema, table, name)) {
                    description.append(" -- reserved word, write it as \"")
                            .append(name).append('"');
                }
                description.append('\n');
            }
        }
    }

    /**
     * Tells whether a column needs quoting, by running a query that names it
     * bare and returns no rows. A failure means the name is a reserved word.
     */
    private static boolean isReservedWord(Connection connection, String schema,
            String table, String column) {
        var qualifiedTable = (schema == null ? "" : "\"" + schema + "\".")
                + "\"" + table + "\"";
        try (var statement = connection.createStatement();
                var resultSet = statement.executeQuery("SELECT " + column
                        + " FROM " + qualifiedTable + " WHERE 1 = 0")) {
            return false;
        } catch (SQLException e) {
            return true;
        }
    }

    private static void appendPrimaryKey(StringBuilder description,
            DatabaseMetaData metaData, String schema, String table)
            throws SQLException {
        var columns = new ArrayList<String>();
        try (ResultSet keys = metaData.getPrimaryKeys(null, schema, table)) {
            while (keys.next()) {
                columns.add(keys.getString("COLUMN_NAME"));
            }
        }
        if (!columns.isEmpty()) {
            description.append("Primary key: ")
                    .append(String.join(", ", columns)).append('\n');
        }
    }

    private static void appendForeignKeys(StringBuilder description,
            DatabaseMetaData metaData, String schema, String table)
            throws SQLException {
        List<String> foreignKeys = new ArrayList<>();
        try (ResultSet keys = metaData.getImportedKeys(null, schema, table)) {
            while (keys.next()) {
                foreignKeys.add(keys.getString("FKCOLUMN_NAME") + " -> "
                        + keys.getString("PKTABLE_NAME") + "("
                        + keys.getString("PKCOLUMN_NAME") + ")");
            }
        }
        if (!foreignKeys.isEmpty()) {
            description.append("Foreign keys: ")
                    .append(String.join(", ", foreignKeys)).append('\n');
        }
    }
}
