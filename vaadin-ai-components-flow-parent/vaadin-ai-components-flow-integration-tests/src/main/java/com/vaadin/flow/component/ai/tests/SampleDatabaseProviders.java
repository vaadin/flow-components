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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.tests.endpoints.SampleApiServer;
import com.vaadin.flow.component.ai.tests.providers.FederatedSampleProvider;
import com.vaadin.flow.component.ai.tests.providers.RestCalciteDatabaseProvider;

/**
 * The database providers the AI chat pages can run against, chosen in their
 * provider select or with the {@code provider} query parameter. Each provider
 * is created on first use and shared by every page, so concurrent page loads
 * cannot fill a database twice.
 */
final class SampleDatabaseProviders {

    /** The provider the pages start with. */
    static final String DEFAULT = "h2";

    private record Entry(String description,
            Supplier<DatabaseProvider> factory) {
    }

    private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();
    private static final Map<String, DatabaseProvider> INSTANCES = new ConcurrentHashMap<>();

    static {
        ENTRIES.put(DEFAULT, new Entry("H2 in-memory database, SQL",
                InMemoryDatabaseProvider::new));
        ENTRIES.put("rest", new Entry("REST API through Calcite",
                FederatedSampleProvider::rest));
        ENTRIES.put("rest-direct",
                new Entry(
                        "REST API through Calcite, written without the helper",
                        () -> new RestCalciteDatabaseProvider(
                                SampleApiServer.get().restUri(),
                                FederatedSampleProvider.CAMEL_CASE_NOTES)));
        ENTRIES.put("graphql", new Entry("GraphQL API through Calcite",
                FederatedSampleProvider::graphql));
        ENTRIES.put("mongo", new Entry("MongoDB through Calcite",
                FederatedSampleProvider::mongo));
        ENTRIES.put("csv", new Entry("CSV files through Calcite",
                FederatedSampleProvider::csv));
        ENTRIES.put("jpa",
                new Entry(
                        "JPA entities through Flow data providers and Calcite",
                        FederatedSampleProvider::jpa));
        ENTRIES.put("federated",
                new Entry("REST, GraphQL, MongoDB and CSV in one schema",
                        FederatedSampleProvider::federated));
    }

    private SampleDatabaseProviders() {
    }

    /**
     * Returns the provider names, in selector order.
     *
     * @return the names
     */
    static List<String> names() {
        return List.copyOf(ENTRIES.keySet());
    }

    /**
     * Returns the description of a provider.
     *
     * @param name
     *            the provider name
     * @return the description
     * @throws IllegalArgumentException
     *             if there is no provider by that name
     */
    static String describe(String name) {
        return entry(name).description();
    }

    /**
     * Returns the shared provider of the given name, creating it on the first
     * call.
     *
     * @param name
     *            the provider name
     * @return the provider
     * @throws IllegalArgumentException
     *             if there is no provider by that name
     */
    static DatabaseProvider get(String name) {
        var entry = entry(name);
        return INSTANCES.computeIfAbsent(name, n -> entry.factory().get());
    }

    private static Entry entry(String name) {
        var entry = ENTRIES.get(name);
        if (entry == null) {
            throw new IllegalArgumentException("Unknown provider " + name
                    + ", expected one of " + ENTRIES.keySet());
        }
        return entry;
    }
}
