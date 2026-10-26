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

import java.sql.SQLException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.tests.endpoints.SampleH2Database;
import com.vaadin.flow.component.ai.tests.endpoints.TenantScopedH2Database;

class JdbcSchemaDescriberTest {

    @Test
    void marksReservedColumnNamesForQuoting() throws SQLException {
        try (var connection = SampleH2Database.Variant.DEFAULT.dataSource()
                .getConnection()) {
            var schema = JdbcSchemaDescriber.describe(connection,
                    connection.getSchema());
            Assertions.assertTrue(schema.contains(
                    "  - MONTH (CHARACTER VARYING(50)) NOT NULL -- reserved word, write it as \"MONTH\""),
                    schema);
            Assertions.assertTrue(
                    schema.contains("  - MONTH_ORDER (INTEGER) NOT NULL\n"),
                    schema);
            Assertions.assertTrue(schema.contains("Primary key: ID"), schema);
        }
    }

    @Test
    void leavesUnreservedNamesAlone() throws SQLException {
        try (var connection = SampleH2Database.Variant.POSTGRESQL_MODE
                .dataSource().getConnection()) {
            var schema = JdbcSchemaDescriber.describe(connection,
                    connection.getSchema());
            Assertions.assertTrue(schema.contains("  - month ("), schema);
            Assertions.assertFalse(schema.contains("reserved word"), schema);
        }
    }

    @Test
    void describesTheViewsOfATenantSchema() throws SQLException {
        try (var connection = TenantScopedH2Database.dataSource("North")
                .getConnection()) {
            var schema = JdbcSchemaDescriber.describe(connection,
                    connection.getSchema());
            Assertions.assertTrue(schema.contains("View: SALES"), schema);
            Assertions.assertTrue(
                    schema.contains("reserved word, write it as \"MONTH\""),
                    schema);
            Assertions.assertFalse(schema.contains("Table: "), schema);
        }
    }
}
