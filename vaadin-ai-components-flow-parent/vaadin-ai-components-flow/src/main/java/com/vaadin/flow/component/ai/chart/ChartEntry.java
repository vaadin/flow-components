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
import java.util.Objects;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Holds the data source queries and pending LLM state for a chart. Chart
 * entries are stored directly on the {@link Chart} instance via
 * {@link ComponentUtil}, so their lifecycle is tied to the chart component.
 *
 * @author Vaadin Ltd
 */
class ChartEntry implements Serializable {

    private final String id;
    private List<String> queries = new ArrayList<>();
    private String pendingConfigurationJson;
    private boolean pendingDataUpdate;

    /**
     * Gets the {@link ChartEntry} for the given chart, or {@code null} if none
     * has been set.
     *
     * @param chart
     *            the chart component, not {@code null}
     * @return the chart entry, or {@code null}
     */
    public static ChartEntry get(Chart chart) {
        return ComponentUtil.getData(chart, ChartEntry.class);
    }

    /**
     * Gets the {@link ChartEntry} for the given chart, creating one if it does
     * not exist.
     *
     * @param chart
     *            the chart component, not {@code null}
     * @param chartId
     *            the chart ID to assign if a new entry is created
     * @return the chart entry, never {@code null}
     */
    public static ChartEntry getOrCreate(Chart chart, String chartId) {
        ChartEntry entry = ComponentUtil.getData(chart, ChartEntry.class);
        if (entry == null) {
            entry = new ChartEntry(chartId);
            ComponentUtil.setData(chart, ChartEntry.class, entry);
        } else if (!entry.id.equals(chartId)) {
            throw new IllegalStateException(
                    "Chart already has an entry with id '" + entry.id
                            + "', cannot reassign to '" + chartId + "'");
        }
        return entry;
    }

    /**
     * Creates a new chart entry with the given ID.
     *
     * @param id
     *            the chart ID, not {@code null}
     */
    ChartEntry(String id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    /**
     * Returns the chart ID.
     *
     * @return the chart ID, never {@code null}
     */
    public String getId() {
        return id;
    }

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
     * Returns whether a data update is pending.
     *
     * @return {@code true} if queries were changed and the chart needs
     *         re-rendering
     */
    public boolean isPendingDataUpdate() {
        return pendingDataUpdate;
    }

    /**
     * Marks or clears the pending data update flag.
     *
     * @param pendingDataUpdate
     *            {@code true} if the chart data needs re-rendering
     */
    public void setPendingDataUpdate(boolean pendingDataUpdate) {
        this.pendingDataUpdate = pendingDataUpdate;
    }

    /**
     * Returns whether this entry has pending state waiting to be applied.
     *
     * @return {@code true} if there is pending configuration or data update
     */
    public boolean hasPendingState() {
        return pendingConfigurationJson != null || pendingDataUpdate;
    }

    /**
     * Clears all pending state.
     */
    public void clearPendingState() {
        pendingConfigurationJson = null;
        pendingDataUpdate = false;
    }

    /**
     * Returns the current state of the chart as a JSON string suitable for LLM
     * tool responses. Includes the chart ID, the Highcharts configuration
     * (without series data), and any SQL queries.
     *
     * @param chart
     *            the chart component, not {@code null}
     * @param chartId
     *            the chart ID
     * @return the state as a JSON string, never {@code null}
     */
    static String getStateAsJson(Chart chart, String chartId) {
        ObjectNode result = JacksonUtils.createObjectNode();
        result.put("chartId", chartId);

        ChartEntry entry = get(chart);
        if (entry != null && !entry.queries.isEmpty()) {
            String configJson = ChartSerialization
                    .toJSON(chart.getConfiguration());
            ObjectNode configNode = JacksonUtils.readTree(configJson);

            // Strip data from series but keep configuration (name,
            // plotOptions, yAxis, type) so the LLM can see per-series
            // settings.
            if (configNode.has("series")
                    && configNode.get("series").isArray()) {
                for (var seriesNode : configNode.get("series")) {
                    if (seriesNode instanceof ObjectNode seriesObj) {
                        seriesObj.remove("data");
                    }
                }
            }
            result.set("configuration", configNode);

            ArrayNode arr = result.putArray("queries");
            entry.queries.forEach(arr::add);
        }

        return result.toString();
    }

}
