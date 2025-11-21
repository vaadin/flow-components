/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.pro.chart;

import java.util.Objects;

/**
 * Default implementation of {@link ChartState}.
 * <p>
 * This class provides an immutable snapshot of a chart's state including the
 * SQL query and chart configuration. All instances are serializable and can be
 * safely stored in databases, session storage, or other persistence mechanisms.
 * </p>
 *
 * @author Vaadin Ltd
 */
class DefaultChartState implements ChartState {

    private static final long serialVersionUID = 1L;

    private final String sqlQuery;
    private final String chartConfig;

    /**
     * Creates a new chart state.
     *
     * @param sqlQuery
     *            the SQL query, may be null
     * @param chartConfig
     *            the chart configuration JSON, may be null
     */
    DefaultChartState(String sqlQuery, String chartConfig) {
        this.sqlQuery = sqlQuery;
        this.chartConfig = chartConfig;
    }

    @Override
    public String getSqlQuery() {
        return sqlQuery;
    }

    @Override
    public String getChartConfig() {
        return chartConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DefaultChartState that = (DefaultChartState) o;
        return Objects.equals(sqlQuery, that.sqlQuery)
                && Objects.equals(chartConfig, that.chartConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sqlQuery, chartConfig);
    }

    @Override
    public String toString() {
        return "DefaultChartState{" + "sqlQuery='"
                + (sqlQuery != null ? sqlQuery.substring(0,
                        Math.min(sqlQuery.length(), 50)) + "..." : "null")
                + '\'' + ", chartConfig='"
                + (chartConfig != null ? chartConfig.substring(0,
                        Math.min(chartConfig.length(), 50)) + "..." : "null")
                + '\'' + '}';
    }
}
