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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.vaadin.flow.component.ai.provider.QueryableProvider;
import com.vaadin.flow.component.ai.tests.InMemoryDatabaseProvider;
import com.vaadin.flow.component.ai.tests.endpoints.SampleApiServer;
import com.vaadin.flow.component.ai.tests.endpoints.SampleCsvFiles;
import com.vaadin.flow.component.ai.tests.endpoints.SampleData;
import com.vaadin.flow.component.ai.tests.endpoints.SampleH2Database;
import com.vaadin.flow.component.ai.tests.endpoints.SampleMongoServer;
import com.vaadin.flow.component.ai.tests.endpoints.jpa.SampleJpaDatabase;

/**
 * The providers over the sample data endpoints, by name, for the chat pages to
 * choose from. Each provider is created on first use and shared by every page,
 * like the endpoints behind them.
 */
public final class SampleProviders {

    /** The name of the provider the pages start with. */
    public static final String DEFAULT = "h2";

    /**
     * Notes for the providers whose field names are camelCase, as in the REST,
     * GraphQL and MongoDB endpoints.
     */
    static final String CAMEL_CASE_NOTES = """
            - sales: order by monthOrder for a chronological series
            - websiteTraffic: dayOfWeek 0=Monday ... 4=Friday and hourOfDay 0=9am ... 7=4pm are 0-based indices; set the axis categories to show labels
            """;

    /** A named provider with a description for the selector. */
    public record Entry(String name, String description,
            Supplier<QueryableProvider> factory) {
    }

    private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();
    private static final Map<String, QueryableProvider> INSTANCES = new ConcurrentHashMap<>();

    static {
        register(DEFAULT, "H2 database, SQL", InMemoryDatabaseProvider::new);
        register("postgresql", "H2 in PostgreSQL mode, PostgreSQL SQL",
                () -> dialect(SampleH2Database.Variant.POSTGRESQL_MODE,
                        "- PostgreSQL dialect: unquoted identifiers are lower case; quote with double quotes only when needed"));
        register("mysql", "H2 in MySQL mode, MySQL SQL", () -> dialect(
                SampleH2Database.Variant.MYSQL_MODE,
                "- MySQL dialect: quote identifiers with backticks when needed"));
        register("tenant-north", "Tenant-scoped H2, North region only, SQL",
                () -> new TenantScopedDatabaseProvider("North"));
        register("tenant-south", "Tenant-scoped H2, South region only, SQL",
                () -> new TenantScopedDatabaseProvider("South"));
        register("rest", "REST API, its own query JSON",
                () -> new RestQueryableProvider(SampleApiServer.get().restUri(),
                        CAMEL_CASE_NOTES));
        register("rest-mirror", "REST API mirrored into SQL",
                () -> new RestMirrorDatabaseProvider(
                        SampleApiServer.get().restUri()));
        register("csv", "CSV exports, their own query JSON",
                () -> new CsvQueryableProvider(SampleCsvFiles.directory(),
                        SampleData.NOTES));
        register("csv-mirror", "CSV exports loaded into SQL",
                () -> new CsvDatabaseProvider(SampleCsvFiles.directory()));
        register("jpa-native", "JPA entities, native SQL",
                () -> new JpaNativeDatabaseProvider(
                        SampleJpaDatabase::entityManagerFactory));
        register("jpql", "JPA entities, JPQL", () -> new JpqlQueryableProvider(
                SampleJpaDatabase::entityManagerFactory,
                """
                        - Sale: order by monthOrder for a chronological series
                        - TrafficCell: dayOfWeek 0=Monday ... 4=Friday and hourOfDay 0=9am ... 7=4pm are 0-based indices; set the axis categories to show labels
                        """));
        register("graphql", "GraphQL API, GraphQL",
                () -> new GraphQLQueryableProvider(
                        SampleApiServer.get().graphQlUri(), CAMEL_CASE_NOTES));
        register("mongo", "MongoDB, aggregation pipelines",
                () -> new MongoAggregationQueryableProvider(
                        () -> SampleMongoServer.get().database(),
                        CAMEL_CASE_NOTES));
        register("mongo-mirror", "MongoDB mirrored into SQL",
                () -> new MongoMirrorDatabaseProvider(
                        () -> SampleMongoServer.get().database()));
    }

    private SampleProviders() {
    }

    private static void register(String name, String description,
            Supplier<QueryableProvider> factory) {
        ENTRIES.put(name, new Entry(name, description, factory));
    }

    private static QueryableProvider dialect(SampleH2Database.Variant variant,
            String dialectRules) {
        return new JdbcDatabaseProvider(variant::readOnlyDataSource,
                new SqlDialectQueryLanguage(variant.dialect(), dialectRules),
                null, SampleData.NOTES);
    }

    /**
     * Returns the provider names, in selector order.
     *
     * @return the names
     */
    public static List<String> names() {
        return new ArrayList<>(ENTRIES.keySet());
    }

    /**
     * Returns the entries, in selector order.
     *
     * @return the entries
     */
    public static List<Entry> entries() {
        return new ArrayList<>(ENTRIES.values());
    }

    /**
     * Returns the description of a provider.
     *
     * @param name
     *            the provider name
     * @return the description, or {@code null} for an unknown name
     */
    public static String describe(String name) {
        var entry = ENTRIES.get(name);
        return entry == null ? null : entry.description();
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
    public static QueryableProvider get(String name) {
        var entry = ENTRIES.get(name);
        if (entry == null) {
            throw new IllegalArgumentException("Unknown provider " + name
                    + ", expected one of " + ENTRIES.keySet());
        }
        return INSTANCES.computeIfAbsent(name, n -> entry.factory().get());
    }
}
