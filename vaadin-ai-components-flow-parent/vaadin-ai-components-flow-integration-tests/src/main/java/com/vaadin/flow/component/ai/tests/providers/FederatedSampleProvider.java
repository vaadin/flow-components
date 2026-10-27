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
import com.vaadin.flow.component.ai.tests.endpoints.SampleData;

/**
 * The sample data set behind one {@link FederatedDatabaseProvider}, split
 * between two endpoints the way an application might find it: the web-facing
 * collections come from the REST API, under their camelCase names, and the
 * back-office exports come from CSV files, under their snake_case names. The
 * LLM writes one SQL over both.
 */
public final class FederatedSampleProvider {

    /** The collections served by the REST API; the rest come from CSV. */
    static final Set<String> REST_COLLECTIONS = Set.of("sales", "products",
            "stockPrices", "pipelineStages", "energyFlows", "websiteTraffic");

    static final String NOTES = """
            - sales: order by monthOrder for a chronological series
            - websiteTraffic: dayOfWeek 0=Monday ... 4=Friday and hourOfDay 0=9am ... 7=4pm are 0-based indices; set the axis categories to show labels
            """;

    private FederatedSampleProvider() {
    }

    /**
     * Creates the provider, starting the sample API server and writing the CSV
     * files if they do not exist yet.
     *
     * @return the provider
     */
    public static FederatedDatabaseProvider create() {
        var sources = new ArrayList<TableSource>();
        for (var source : RestTableSource
                .discover(SampleApiServer.get().restUri())) {
            if (REST_COLLECTIONS.contains(source.getName())) {
                sources.add(source);
            }
        }
        for (var source : CsvTableSource
                .inDirectory(SampleCsvFiles.directory())) {
            var table = SampleData.table(source.getName());
            if (table != null
                    && !REST_COLLECTIONS.contains(table.resourceName())) {
                sources.add(source);
            }
        }
        return new FederatedDatabaseProvider(List.copyOf(sources), NOTES);
    }
}
