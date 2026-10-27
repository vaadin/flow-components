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
import java.util.List;
import java.util.Set;

import com.vaadin.flow.component.ai.provider.FederatedDatabaseProvider;
import com.vaadin.flow.component.ai.provider.TableSource;
import com.vaadin.flow.component.ai.tests.endpoints.SampleApiServer;
import com.vaadin.flow.component.ai.tests.endpoints.SampleCsvFiles;
import com.vaadin.flow.component.ai.tests.endpoints.SampleMongoServer;

/**
 * {@link FederatedDatabaseProvider}s over the sample data endpoints: one per
 * endpoint type with the whole data set, and {@link #federated()}, which splits
 * the data set between the four endpoints the way an application might find it,
 * so that the LLM writes one SQL across them. The REST, GraphQL and MongoDB
 * endpoints name their tables in camelCase, the CSV files in snake_case.
 */
public final class FederatedSampleProvider {

    static final Set<String> REST_TABLES = Set.of("sales", "products",
            "websiteTraffic");

    static final Set<String> GRAPHQL_TABLES = Set.of("employees",
            "projectTasks", "stockPrices");

    static final Set<String> MONGO_TABLES = Set.of("energyFlow", "kpiMetrics",
            "budgetItems");

    static final Set<String> CSV_TABLES = Set.of("org_chart", "sales_pipeline",
            "expense_categories");

    private static final String CAMEL_CASE_NOTES = """
            - sales: order by monthOrder for a chronological series
            - websiteTraffic: dayOfWeek 0=Monday ... 4=Friday and hourOfDay 0=9am ... 7=4pm are 0-based indices; set the axis categories to show labels
            """;

    private static final String SNAKE_CASE_NOTES = """
            - sales: order by month_order for a chronological series
            - website_traffic: day_of_week 0=Monday ... 4=Friday and hour_of_day 0=9am ... 7=4pm are 0-based indices; set the axis categories to show labels
            """;

    private static final String MONGO_NOTES = """
            - stockPrices keeps its prices and orgChart its manager in nested documents, which are not exposed
            """;

    private FederatedSampleProvider() {
    }

    /**
     * Creates a provider over every collection of the sample REST API.
     *
     * @return the provider
     */
    public static FederatedDatabaseProvider rest() {
        return new FederatedDatabaseProvider(
                List.copyOf(RestTableSource
                        .discover(SampleApiServer.get().restUri())),
                CAMEL_CASE_NOTES);
    }

    /**
     * Creates a provider over every sample CSV file.
     *
     * @return the provider
     */
    public static FederatedDatabaseProvider csv() {
        return new FederatedDatabaseProvider(
                List.copyOf(
                        CsvTableSource.inDirectory(SampleCsvFiles.directory())),
                SNAKE_CASE_NOTES);
    }

    /**
     * Creates a provider over every list field of the sample GraphQL API.
     *
     * @return the provider
     */
    public static FederatedDatabaseProvider graphql() {
        return new FederatedDatabaseProvider(
                List.copyOf(GraphQLTableSource
                        .discover(SampleApiServer.get().graphQlUri())),
                CAMEL_CASE_NOTES);
    }

    /**
     * Creates a provider over every collection of the sample MongoDB database,
     * through Calcite's MongoDB adapter.
     *
     * @return the provider
     */
    public static FederatedDatabaseProvider mongo() {
        return new FederatedDatabaseProvider(List.copyOf(mongoSources()),
                CAMEL_CASE_NOTES + MONGO_NOTES);
    }

    /**
     * Creates a provider with the data set split between the four endpoints:
     * the web-facing collections from the REST API, the people and project data
     * from the GraphQL API, the metrics from MongoDB and the back-office
     * exports from CSV files.
     *
     * @return the provider
     */
    public static FederatedDatabaseProvider federated() {
        var sources = new ArrayList<TableSource>();
        for (var source : RestTableSource
                .discover(SampleApiServer.get().restUri())) {
            if (REST_TABLES.contains(source.getName())) {
                sources.add(source);
            }
        }
        for (var source : GraphQLTableSource
                .discover(SampleApiServer.get().graphQlUri())) {
            if (GRAPHQL_TABLES.contains(source.getName())) {
                sources.add(source);
            }
        }
        for (var source : CsvTableSource
                .inDirectory(SampleCsvFiles.directory())) {
            if (CSV_TABLES.contains(source.getName())) {
                sources.add(source);
            }
        }
        for (var source : mongoSources()) {
            if (MONGO_TABLES.contains(source.getName())) {
                sources.add(source);
            }
        }
        return new FederatedDatabaseProvider(sources, CAMEL_CASE_NOTES);
    }

    private static List<MongoTableSource> mongoSources() {
        var server = SampleMongoServer.get();
        return MongoTableSource.discover(server.connectionString(),
                SampleMongoServer.DATABASE_NAME);
    }
}
