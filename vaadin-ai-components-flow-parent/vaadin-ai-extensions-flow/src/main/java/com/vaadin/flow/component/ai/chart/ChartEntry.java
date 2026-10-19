/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.chart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Holds the data source queries, the configuration the LLM has set and the
 * pending LLM state for a chart. Chart entries are stored directly on the
 * {@link Chart} instance via {@link ComponentUtil}, so their lifecycle is tied
 * to the chart component.
 *
 * @author Vaadin Ltd
 */
class ChartEntry implements Serializable {

    private final String id;
    private List<String> queries = new ArrayList<>();
    private List<String> pendingQueries;
    private final List<String> pendingConfigurationJsons = new ArrayList<>();
    /**
     * Built only from the configuration JSON the LLM sent, so unlike the
     * chart's own configuration it holds no series, categories or other values
     * the renderer derives from the query results.
     */
    private Configuration llmConfiguration = new Configuration();

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
     * Gets the configuration JSON updates staged for the next successful
     * {@code onResponseComplete}, in the order they were staged.
     *
     * @return an unmodifiable list of configuration JSON strings, empty if none
     */
    public List<String> getPendingConfigurationJsons() {
        return Collections.unmodifiableList(pendingConfigurationJsons);
    }

    /**
     * Stages a configuration JSON update to be applied on the next successful
     * {@code onResponseComplete}, after the updates staged before it.
     *
     * @param configurationJson
     *            the configuration JSON string, not {@code null}
     */
    public void addPendingConfigurationJson(String configurationJson) {
        pendingConfigurationJsons
                .add(Objects.requireNonNull(configurationJson));
    }

    /**
     * Returns the staged SQL queries waiting to be committed by the next
     * successful {@code onResponseComplete}, or {@code null} if none.
     *
     * @return the pending SQL queries, or {@code null}
     */
    public List<String> getPendingQueries() {
        return pendingQueries == null ? null
                : Collections.unmodifiableList(pendingQueries);
    }

    /**
     * Stages SQL queries to be applied on the next successful
     * {@code onResponseComplete}. Pass {@code null} to clear.
     *
     * @param queries
     *            the SQL queries to stage, or {@code null}
     */
    public void setPendingQueries(List<String> queries) {
        this.pendingQueries = queries == null ? null : new ArrayList<>(queries);
    }

    /**
     * Gets the configuration the LLM has set.
     *
     * @return the configuration, never {@code null}
     */
    public Configuration getLlmConfiguration() {
        return llmConfiguration;
    }

    /**
     * Replaces the configuration the LLM has set.
     *
     * @param configuration
     *            the configuration, not {@code null}
     */
    public void setLlmConfiguration(Configuration configuration) {
        llmConfiguration = Objects.requireNonNull(configuration);
    }

    /**
     * Applies configuration JSON the LLM sent to the configuration it has set
     * so far, by the same rules the renderer applies it to the chart.
     *
     * @param configurationJson
     *            the configuration JSON, not {@code null}
     */
    public void applyLlmConfiguration(String configurationJson) {
        llmConfiguration = ChartRenderer.applyConfiguration(llmConfiguration,
                configurationJson);
        // Each update adds its series entries. Keep the latest per name, the
        // one the renderer uses as that series' settings.
        llmConfiguration.setSeries(new ArrayList<>(
                ChartRenderer.extractSeriesConfig(llmConfiguration).values()));
    }

    /**
     * Returns whether this entry has pending state waiting to be applied.
     *
     * @return {@code true} if there is pending configuration or pending queries
     */
    public boolean hasPendingState() {
        return !pendingConfigurationJsons.isEmpty() || pendingQueries != null;
    }

    /**
     * Clears all pending state.
     */
    public void clearPendingState() {
        pendingConfigurationJsons.clear();
        pendingQueries = null;
    }

    /**
     * Returns the current state of the chart as a JSON string suitable for LLM
     * tool responses. Includes the chart ID, the configuration the LLM has set,
     * and any SQL queries. Nothing in it comes from the query results.
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
            ObjectNode configNode = JacksonUtils.readTree(
                    ChartSerialization.toJSON(entry.llmConfiguration));
            result.set("configuration", configNode);

            ArrayNode arr = result.putArray("queries");
            entry.queries.forEach(arr::add);
        }

        return result.toString();
    }

}
