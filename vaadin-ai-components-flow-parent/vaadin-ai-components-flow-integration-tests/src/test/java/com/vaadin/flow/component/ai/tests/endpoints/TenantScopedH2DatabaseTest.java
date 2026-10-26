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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TenantScopedH2DatabaseTest {

    @Test
    void tenantSeesOnlyItsOwnSales() throws SQLException {
        for (var tenant : TenantScopedH2Database.TENANTS) {
            var dataSource = TenantScopedH2Database.dataSource(tenant);
            var regions = Jdbc.query(dataSource,
                    "SELECT DISTINCT region FROM sales");
            Assertions.assertEquals(1, regions.size());
            Assertions.assertEquals(tenant, regions.getFirst()[0]);
            Assertions.assertEquals(6, Jdbc.count(dataSource, "sales"));
        }
        Assertions.assertEquals(12,
                Jdbc.count(TenantScopedH2Database.adminDataSource(), "sales"));
    }

    @Test
    void tenantSeesSharedTablesUnfiltered() throws SQLException {
        var dataSource = TenantScopedH2Database.dataSource("South");
        for (var table : SampleData.TABLES) {
            if (!table.sqlName().equals("sales")) {
                Assertions.assertEquals(table.rows().size(),
                        Jdbc.count(dataSource, table.sqlName()),
                        table.sqlName());
            }
        }
        Assertions.assertEquals("January", Jdbc.scalar(dataSource,
                "SELECT \"MONTH\" FROM sales WHERE month_order = 1"));
    }

    @Test
    void tenantCannotReachSharedTablesOrWrite() throws SQLException {
        var dataSource = TenantScopedH2Database.dataSource("North");
        for (var statement : new String[] { "SELECT * FROM PUBLIC.sales",
                "SELECT * FROM TENANT_SOUTH.sales", "DELETE FROM PUBLIC.sales",
                "CREATE TABLE evil (x INT)" }) {
            var e = Assertions.assertThrows(SQLException.class,
                    () -> Jdbc.execute(dataSource, statement), statement);
            Assertions.assertEquals("90096", e.getSQLState(), statement);
        }
        Assertions.assertThrows(SQLException.class,
                () -> Jdbc.execute(dataSource, "DELETE FROM sales"));
        Assertions.assertEquals(12,
                Jdbc.count(TenantScopedH2Database.adminDataSource(), "sales"));
    }

    @Test
    void unknownTenantIsRejected() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> TenantScopedH2Database.dataSource("East"));
    }
}
