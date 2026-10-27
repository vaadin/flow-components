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

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class SampleH2DatabaseTest {

    @ParameterizedTest
    @EnumSource(SampleH2Database.Variant.class)
    void everyVariantHoldsAllTables(SampleH2Database.Variant variant)
            throws SQLException {
        var dataSource = variant.dataSource();
        for (var table : SampleData.TABLES) {
            Assertions.assertEquals(table.rows().size(),
                    Jdbc.count(dataSource, table.sqlName()),
                    variant + " " + table.sqlName());
        }
    }

    @ParameterizedTest
    @EnumSource(SampleH2Database.Variant.class)
    void valuesKeepTheirTypes(SampleH2Database.Variant variant)
            throws SQLException {
        var row = Jdbc.query(variant.dataSource(),
                "SELECT trade_date, open_price, volume FROM stock_prices WHERE id = 1")
                .getFirst();
        Assertions.assertEquals(Date.valueOf("2025-01-06"), row[0]);
        Assertions.assertEquals(0,
                new BigDecimal("142.50").compareTo((BigDecimal) row[1]));
        Assertions.assertEquals(52000, row[2]);
    }

    @Test
    void defaultVariantQuotesMonthKeyword() throws SQLException {
        var dataSource = SampleH2Database.Variant.DEFAULT.dataSource();
        Assertions.assertEquals("January", Jdbc.scalar(dataSource,
                "SELECT \"MONTH\" FROM sales WHERE id = 1"));
        Assertions.assertThrows(SQLException.class, () -> Jdbc
                .scalar(dataSource, "SELECT month FROM sales WHERE id = 1"));
    }

    @ParameterizedTest
    @EnumSource(value = SampleH2Database.Variant.class, names = {
            "POSTGRESQL_MODE", "MYSQL_MODE" })
    void compatibilityModesUsePlainMonthColumn(SampleH2Database.Variant variant)
            throws SQLException {
        Assertions.assertEquals("January", Jdbc.scalar(variant.dataSource(),
                "SELECT month FROM sales WHERE id = 1"));
        // Also for the read-only user, whose connection URL keeps the
        // NON_KEYWORDS setting
        Assertions.assertEquals("January",
                Jdbc.scalar(variant.readOnlyDataSource(),
                        "SELECT month FROM sales WHERE id = 1"));
    }

    @ParameterizedTest
    @EnumSource(value = SampleH2Database.Variant.class, names = {
            "POSTGRESQL_MODE", "MYSQL_MODE" })
    void compatibilityModesAcceptValueAsAlias(SampleH2Database.Variant variant)
            throws SQLException {
        Assertions.assertEquals(45000, Jdbc.scalar(variant.readOnlyDataSource(),
                "SELECT revenue AS value FROM sales WHERE id = 1"));
        Assertions.assertThrows(SQLException.class,
                () -> Jdbc.scalar(
                        SampleH2Database.Variant.DEFAULT.readOnlyDataSource(),
                        "SELECT revenue AS value FROM sales WHERE id = 1"),
                "plain H2 keeps VALUE reserved");
    }

    @Test
    void mysqlModeAcceptsBackticks() throws SQLException {
        Assertions.assertEquals(45000,
                Jdbc.scalar(SampleH2Database.Variant.MYSQL_MODE.dataSource(),
                        "SELECT `revenue` FROM `sales` WHERE id = 1"));
    }

    @ParameterizedTest
    @EnumSource(SampleH2Database.Variant.class)
    void readOnlyUserCanOnlyRead(SampleH2Database.Variant variant)
            throws SQLException {
        var reader = variant.readOnlyDataSource();
        Assertions.assertEquals(12, Jdbc.count(reader, "sales"));
        // The wrapping the grid controller applies, quoting the column label
        // as the database reports it
        var label = variant == SampleH2Database.Variant.DEFAULT ? "REVENUE"
                : "revenue";
        Assertions.assertEquals(3, Jdbc.query(reader,
                "SELECT * FROM (SELECT * FROM (SELECT id, revenue FROM sales) AS _t ORDER BY \""
                        + label + "\" DESC) AS _limited LIMIT 3 OFFSET 2")
                .size());
        for (var statement : new String[] { "DELETE FROM sales",
                "UPDATE sales SET revenue = 0", "CREATE TABLE evil (x INT)",
                "DROP TABLE sales" }) {
            var e = Assertions.assertThrows(SQLException.class,
                    () -> Jdbc.execute(reader, statement), statement);
            Assertions.assertEquals("90096", e.getSQLState(), statement);
        }
        Assertions.assertEquals(12, Jdbc.count(variant.dataSource(), "sales"));
    }

    @Test
    void snakeCaseConversion() {
        Assertions.assertEquals("month_order",
                SampleData.toSnakeCase("monthOrder"));
        Assertions.assertEquals("id", SampleData.toSnakeCase("id"));
        Assertions.assertEquals("open_price",
                SampleData.toSnakeCase("openPrice"));
    }
}
