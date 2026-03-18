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
package com.vaadin.flow.component.ai.chart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the data source queries and pending LLM state for a chart. Chart
 * entries do not hold a direct reference to the
 * {@link com.vaadin.flow.component.charts.Chart} instance — the chart is
 * resolved dynamically through the {@link ChartRegistry}'s resolver function,
 * so that chart lifecycle changes (e.g., removal from a dashboard) are
 * reflected automatically.
 *
 * @author Vaadin Ltd
 */
public class ChartEntry implements Serializable {

    private List<String> queries = new ArrayList<>();
    private String pendingConfigurationJson;
    private List<String> pendingQueries;

    /**
     * Gets the current SQL queries for this chart's data series.
     *
     * @return an unmodifiable list of SQL queries, never {@code null}
     */
    public List<String> getQueries() {
        return Collections.unmodifiableList(queries);
    }

    /**
     * Sets the SQL queries for this chart's data series.
     *
     * @param queries
     *            the SQL queries, not {@code null}
     */
    public void setQueries(List<String> queries) {
        this.queries = new ArrayList<>(queries);
    }

    /**
     * Gets the pending configuration JSON that will be applied when
     * {@link #hasPendingState()} is true.
     *
     * @return the pending configuration JSON, or {@code null} if none
     */
    public String getPendingConfigurationJson() {
        return pendingConfigurationJson;
    }

    /**
     * Sets the pending configuration JSON to be applied later.
     *
     * @param configurationJson
     *            the configuration JSON string
     */
    public void setPendingConfigurationJson(String configurationJson) {
        this.pendingConfigurationJson = configurationJson;
    }

    /**
     * Gets the pending SQL queries that will replace the current ones when
     * applied.
     *
     * @return the pending queries, or {@code null} if none
     */
    public List<String> getPendingQueries() {
        return pendingQueries;
    }

    /**
     * Sets the pending SQL queries to be applied later.
     *
     * @param queries
     *            the pending SQL queries
     */
    public void setPendingQueries(List<String> queries) {
        this.pendingQueries = queries != null ? new ArrayList<>(queries)
                : new ArrayList<>();
    }

    /**
     * Returns whether this entry has pending state waiting to be applied.
     *
     * @return {@code true} if there is pending configuration or queries
     */
    public boolean hasPendingState() {
        return pendingConfigurationJson != null || pendingQueries != null;
    }

    /**
     * Clears all pending state.
     */
    public void clearPendingState() {
        pendingConfigurationJson = null;
        pendingQueries = null;
    }

    /**
     * Applies the pending queries to the current state and clears them. This
     * does not apply the pending configuration — that requires deserialization
     * and is handled externally.
     */
    public void applyPendingQueries() {
        if (pendingQueries != null) {
            queries = new ArrayList<>(pendingQueries);
            pendingQueries = null;
        }
    }
}
