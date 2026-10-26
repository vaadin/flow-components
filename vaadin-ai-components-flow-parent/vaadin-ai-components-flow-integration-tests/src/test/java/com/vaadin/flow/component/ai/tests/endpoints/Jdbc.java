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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

/**
 * Shared JDBC helpers for the endpoint tests.
 */
final class Jdbc {

    private Jdbc() {
    }

    static List<Object[]> query(DataSource dataSource, String sql)
            throws SQLException {
        var rows = new ArrayList<Object[]>();
        try (var connection = dataSource.getConnection();
                var statement = connection.createStatement();
                var resultSet = statement.executeQuery(sql)) {
            int columns = resultSet.getMetaData().getColumnCount();
            while (resultSet.next()) {
                var row = new Object[columns];
                for (int i = 0; i < columns; i++) {
                    row[i] = resultSet.getObject(i + 1);
                }
                rows.add(row);
            }
        }
        return rows;
    }

    static Object scalar(DataSource dataSource, String sql)
            throws SQLException {
        return query(dataSource, sql).getFirst()[0];
    }

    static void execute(DataSource dataSource, String sql) throws SQLException {
        try (var connection = dataSource.getConnection();
                var statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    static long count(DataSource dataSource, String table) throws SQLException {
        return ((Number) scalar(dataSource, "SELECT COUNT(*) FROM " + table))
                .longValue();
    }
}
