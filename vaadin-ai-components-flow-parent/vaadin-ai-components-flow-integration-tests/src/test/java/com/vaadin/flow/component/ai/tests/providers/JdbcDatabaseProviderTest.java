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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.provider.ToolException;
import com.vaadin.flow.component.ai.tests.endpoints.SampleData;
import com.vaadin.flow.component.ai.tests.endpoints.SampleH2Database;
import com.vaadin.flow.component.ai.tests.endpoints.TenantScopedH2Database;
import com.vaadin.flow.component.ai.tests.endpoints.jpa.SampleJpaDatabase;

class JdbcDatabaseProviderTest {

    @Test
    void postgresqlMode_namesTheDialect_andTakesPlainMonth() {
        var provider = new JdbcDatabaseProvider(
                SampleH2Database.Variant.POSTGRESQL_MODE::readOnlyDataSource,
                "PostgreSQL SQL", null, null);

        var schema = provider.getSchema();
        var rows = provider
                .executeQuery("SELECT month FROM sales WHERE id = 1");

        Assertions.assertTrue(
                schema.startsWith(
                        "DATABASE SCHEMA (query it with PostgreSQL SQL):"),
                schema);
        Assertions.assertTrue(schema.contains("Table: sales"), schema);
        Assertions.assertEquals("January", rows.get(0).get("month"));
    }

    @Test
    void mysqlMode_acceptsBackticks() {
        var provider = new JdbcDatabaseProvider(
                SampleH2Database.Variant.MYSQL_MODE::readOnlyDataSource,
                "MySQL SQL", null, null);

        var rows = provider
                .executeQuery("SELECT `revenue` FROM `sales` WHERE id = 1");

        Assertions.assertEquals(1, rows.size());
    }

    @Test
    void tenant_seesOnlyItsRegion_andTheReservedMonthIsMarked() {
        var provider = new JdbcDatabaseProvider(
                () -> TenantScopedH2Database.dataSource("North"), "SQL",
                TenantScopedH2Database.schemaName("North"), "- notes\n");

        var schema = provider.getSchema();
        var regions = provider
                .executeQuery("SELECT DISTINCT region FROM sales");

        Assertions.assertTrue(schema.contains("View: SALES"), schema);
        Assertions.assertTrue(
                schema.contains("-- reserved word, write it as \"MONTH\""),
                schema);
        Assertions.assertTrue(schema.endsWith("NOTES:\n- notes\n"), schema);
        Assertions.assertEquals(1, regions.size());
        Assertions.assertEquals("North", regions.get(0).get("REGION"));
    }

    @Test
    void jpaNative_queriesTheTablesHibernateGenerated() {
        var provider = new JdbcDatabaseProvider(SampleJpaDatabase::dataSource,
                "SQL", "PUBLIC", null);

        var rows = provider.executeQuery("SELECT COUNT(*) AS n FROM employees");

        Assertions.assertEquals((long) SampleData.EMPLOYEES.size(),
                ((Number) rows.get(0).get("N")).longValue());
    }

    @Test
    void writes_areRejected_andSyntaxErrorsAreRelayed() {
        var provider = new JdbcDatabaseProvider(
                SampleH2Database.Variant.DEFAULT::readOnlyDataSource, "SQL",
                null, null);

        Assertions.assertThrows(ToolException.class,
                () -> provider.executeQuery("DELETE FROM sales"));
        Assertions.assertThrows(ToolException.class,
                () -> provider.executeQuery("SELECT FROM WHERE"));
    }
}
