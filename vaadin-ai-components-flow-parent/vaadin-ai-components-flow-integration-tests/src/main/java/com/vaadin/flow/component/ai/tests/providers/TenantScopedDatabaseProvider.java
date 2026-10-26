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

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.common.QueryLanguage;
import com.vaadin.flow.component.ai.common.QueryPage;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.tests.endpoints.SampleData;
import com.vaadin.flow.component.ai.tests.endpoints.TenantScopedH2Database;

/**
 * A {@link DatabaseProvider} for one tenant of the tenant-scoped sample
 * database: it connects as the tenant's read-only user, whose default schema
 * holds views filtered to the tenant's rows. The LLM sees the same table names
 * for every tenant and gets only the tenant's data; a query naming the shared
 * tables fails with a rights error that is relayed to the LLM.
 */
public class TenantScopedDatabaseProvider implements DatabaseProvider {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(TenantScopedDatabaseProvider.class);

    private final String tenant;
    private final JdbcDatabaseProvider delegate;

    /**
     * Creates a provider for a tenant.
     *
     * @param tenant
     *            one of {@link TenantScopedH2Database#TENANTS}
     */
    public TenantScopedDatabaseProvider(String tenant) {
        this.tenant = tenant;
        delegate = new JdbcDatabaseProvider(
                () -> TenantScopedH2Database.dataSource(tenant),
                QueryLanguage.SQL, TenantScopedH2Database.schemaName(tenant),
                """
                        - The tables hold the data of the %s region only; other regions are not visible to this connection.
                        """
                        .formatted(tenant) + SampleData.NOTES);
    }

    /**
     * Returns the tenant this provider serves.
     *
     * @return the tenant
     */
    public String getTenant() {
        return tenant;
    }

    @Override
    public String getSchema() {
        return delegate.getSchema();
    }

    @Override
    public List<Map<String, Object>> executeQuery(String sql) {
        LOGGER.info("Running as the {} tenant", tenant);
        return delegate.executeQuery(sql);
    }

    @Override
    public List<Map<String, Object>> executeQuery(String sql, QueryPage page) {
        return delegate.executeQuery(sql, page);
    }

    @Override
    public OptionalInt count(String sql) {
        return delegate.count(sql);
    }
}
