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

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;

/**
 * An H2 database that scopes the sample data per tenant with database means
 * rather than with prompts: each tenant has a schema of views over the shared
 * tables, with the sales rows filtered to the tenant's region, and a user that
 * may read only that schema. A connection as the tenant's user sees the same
 * table names as every other tenant, but only the tenant's rows, and a query
 * naming the underlying {@code PUBLIC} tables fails with a rights error.
 * <p>
 * The tenants are the two sales regions, North and South. Tables without a
 * region are shared by both tenants as unfiltered views.
 * </p>
 */
public final class TenantScopedH2Database {

    /** URL of the admin connection that creates and keeps the database. */
    public static final String URL = "jdbc:h2:mem:sampledb_tenants;DB_CLOSE_DELAY=-1";

    /** The tenants, named after the sales regions. */
    public static final List<String> TENANTS = List.of("North", "South");

    /** Password of every tenant user. */
    public static final String PASSWORD = "tenant";

    private static boolean initialized;

    private TenantScopedH2Database() {
    }

    /**
     * Returns a data source connecting as the given tenant's user, with the
     * tenant's schema as the default schema. Creates the database on the first
     * call.
     *
     * @param tenant
     *            one of {@link #TENANTS}
     * @return the tenant's data source
     */
    public static DataSource dataSource(String tenant) {
        if (!TENANTS.contains(tenant)) {
            throw new IllegalArgumentException("Unknown tenant " + tenant
                    + ", expected one of " + TENANTS);
        }
        initialize();
        return SampleH2Database
                .dataSource(
                        SampleH2Database.withoutAdminSettings(URL) + ";SCHEMA="
                                + schemaName(tenant),
                        userName(tenant), PASSWORD);
    }

    /**
     * Returns a data source connecting as the admin user, who sees the shared
     * {@code PUBLIC} tables and every tenant schema.
     *
     * @return the admin data source
     */
    public static DataSource adminDataSource() {
        initialize();
        return SampleH2Database.dataSource(URL, SampleH2Database.ADMIN_USER,
                SampleH2Database.ADMIN_PASSWORD);
    }

    /**
     * Returns the name of a tenant's schema.
     *
     * @param tenant
     *            the tenant
     * @return the schema name, for example {@code TENANT_NORTH}
     */
    public static String schemaName(String tenant) {
        return "TENANT_" + tenant.toUpperCase(Locale.ROOT);
    }

    /**
     * Returns the name of a tenant's read-only user.
     *
     * @param tenant
     *            the tenant
     * @return the user name, for example {@code north_reader}
     */
    public static String userName(String tenant) {
        return tenant.toLowerCase(Locale.ROOT) + "_reader";
    }

    private static synchronized void initialize() {
        if (initialized) {
            return;
        }
        try (var connection = DriverManager.getConnection(URL,
                SampleH2Database.ADMIN_USER, SampleH2Database.ADMIN_PASSWORD);
                var statement = connection.createStatement()) {
            SampleH2Database.seed(connection, true);
            for (var tenant : TENANTS) {
                createTenant(statement, tenant);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to create the tenant-scoped database", e);
        }
        initialized = true;
    }

    private static void createTenant(Statement statement, String tenant)
            throws SQLException {
        var schema = schemaName(tenant);
        var user = userName(tenant);
        statement.execute("CREATE SCHEMA " + schema);
        for (var table : SampleH2Database.tableNames()) {
            var filter = table.equals("sales")
                    ? " WHERE region = '" + tenant + "'"
                    : "";
            statement.execute("CREATE VIEW " + schema + "." + table
                    + " AS SELECT * FROM PUBLIC." + table + filter);
        }
        statement.execute(
                "CREATE USER " + user + " PASSWORD '" + PASSWORD + "'");
        statement.execute("GRANT SELECT ON SCHEMA " + schema + " TO " + user);
    }
}
