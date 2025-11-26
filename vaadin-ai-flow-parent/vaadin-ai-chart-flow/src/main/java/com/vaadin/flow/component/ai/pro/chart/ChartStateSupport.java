/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.pro.chart;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.state.AiStateSupport;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.internal.JacksonUtils;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;

/**
 * State support implementation for AI-powered chart visualizations.
 * <p>
 * This class captures and restores the complete state of a visualization
 * including the data query, visualization type, and configuration.
 * </p>
 *
 * <h3>Example Usage:</h3>
 *
 * <pre>
 * Component container = new Div();
 * DatabaseProvider db = new MyDatabaseProvider();
 * DataConverter converter = new DefaultDataConverter();
 *
 * ChartStateSupport stateSupport = new ChartStateSupport(container, db,
 *         converter);
 *
 * // Later, after AI has created a visualization:
 * VisualizationState state = stateSupport.capture();
 * saveToDatabase(state);
 *
 * // Restore:
 * VisualizationState loaded = loadFromDatabase();
 * stateSupport.restore(loaded);
 * </pre>
 *
 * @author Vaadin Ltd
 */
public class ChartStateSupport
        implements AiStateSupport<VisualizationState> {

    private final Component visualizationContainer;
    private final DatabaseProvider databaseProvider;
    private final DataConverter dataConverter;

    // Track current state
    private VisualizationType currentType;
    private String currentSqlQuery;
    private Map<String, Object> currentConfiguration;

    /**
     * Creates a new chart state support instance.
     *
     * @param visualizationContainer
     *            the container where visualizations are rendered
     * @param databaseProvider
     *            the database provider for executing queries
     * @param dataConverter
     *            the converter for chart data
     */
    public ChartStateSupport(Component visualizationContainer,
            DatabaseProvider databaseProvider, DataConverter dataConverter) {
        this.visualizationContainer = visualizationContainer;
        this.databaseProvider = databaseProvider;
        this.dataConverter = dataConverter;
    }

    @Override
    public VisualizationState capture() {
        if (currentSqlQuery == null) {
            return null;
        }
        return VisualizationState.of(currentType, currentSqlQuery,
                currentConfiguration);
    }

    @Override
    public void restore(VisualizationState state) {
        if (state == null || state.getSqlQuery() == null) {
            return;
        }

        try {
            // Re-execute the query
            List<Map<String, Object>> results = databaseProvider
                    .executeQuery(state.getSqlQuery());

            // Update tracked state
            this.currentType = state.getType();
            this.currentSqlQuery = state.getSqlQuery();
            this.currentConfiguration = state.getConfiguration();

            // Render the visualization
            renderVisualization(state.getType(), results,
                    state.getConfiguration());

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to restore visualization state: " + e.getMessage(),
                    e);
        }
    }

    /**
     * Updates the internal state tracking. This should be called by the tools
     * after they modify the visualization.
     *
     * @param type
     *            the visualization type
     * @param sqlQuery
     *            the SQL query
     * @param configuration
     *            the configuration
     */
    public void updateState(VisualizationType type, String sqlQuery,
            Map<String, Object> configuration) {
        this.currentType = type;
        this.currentSqlQuery = sqlQuery;
        this.currentConfiguration = configuration;
    }

    private void renderVisualization(VisualizationType type,
            List<Map<String, Object>> queryResults,
            Map<String, Object> config) {
        if (visualizationContainer == null) {
            return;
        }

        switch (type) {
        case CHART -> renderChart(queryResults, config);
        case GRID -> renderGrid(queryResults);
        case KPI -> renderKpi(queryResults, config);
        }
    }

    private void renderChart(List<Map<String, Object>> queryResults,
            Map<String, Object> config) {
        DataSeries series = dataConverter.convertToDataSeries(queryResults);

        Chart chart;
        if (visualizationContainer instanceof Chart) {
            chart = (Chart) visualizationContainer;
        } else {
            chart = new Chart();
            chart.setSizeFull();
            if (visualizationContainer instanceof Div) {
                ((Div) visualizationContainer).removeAll();
                ((Div) visualizationContainer).add(chart);
            }
        }

        Configuration chartConfig = chart.getConfiguration();
        chartConfig.setSeries(series);

        String chartConfigJson = (String) config.get("chartConfig");
        if (chartConfigJson != null && !chartConfigJson.isEmpty()) {
            applyChartConfig(chart, chartConfigJson);
        }

        chart.drawChart();
    }

    private void renderGrid(List<Map<String, Object>> queryResults) {
        if (queryResults.isEmpty()) {
            return;
        }

        Grid<Map<String, Object>> grid = new Grid<>();
        grid.setWidthFull();
        grid.setHeightFull();

        Map<String, Object> firstRow = queryResults.get(0);
        for (String columnName : firstRow.keySet()) {
            grid.addColumn(row -> {
                Object value = row.get(columnName);
                return value != null ? value.toString() : "";
            }).setHeader(columnName).setSortable(true).setResizable(true);
        }

        grid.setItems(queryResults);

        if (visualizationContainer instanceof Div) {
            ((Div) visualizationContainer).removeAll();
            ((Div) visualizationContainer).add(grid);
        }
    }

    private void renderKpi(List<Map<String, Object>> queryResults,
            Map<String, Object> config) {
        if (queryResults.isEmpty()) {
            return;
        }

        Map<String, Object> firstRow = queryResults.get(0);
        Object value = firstRow.values().iterator().next();

        String format = (String) config.get("format");
        String formattedValue;
        if (format != null && value instanceof Number) {
            formattedValue = String.format(format, value);
        } else {
            formattedValue = value != null ? value.toString() : "N/A";
        }

        VerticalLayout kpiCard = new VerticalLayout();
        kpiCard.setSpacing(false);
        kpiCard.setPadding(true);
        kpiCard.setAlignItems(VerticalLayout.Alignment.CENTER);
        kpiCard.setJustifyContentMode(
                VerticalLayout.JustifyContentMode.CENTER);
        kpiCard.setWidthFull();
        kpiCard.setHeightFull();

        String label = (String) config.get("label");
        if (label != null) {
            H3 labelComponent = new H3(label);
            labelComponent.getStyle().set("margin", "0").set("color",
                    "var(--lumo-secondary-text-color)");
            kpiCard.add(labelComponent);
        }

        Span valueComponent = new Span(formattedValue);
        valueComponent.getStyle().set("font-size", "3em")
                .set("font-weight", "bold").set("margin", "0.5em 0");
        kpiCard.add(valueComponent);

        if (visualizationContainer instanceof Div) {
            ((Div) visualizationContainer).removeAll();
            ((Div) visualizationContainer).add(kpiCard);
        }
    }

    private void applyChartConfig(Chart chart, String configJson) {
        try {
            var configurationNode = (ObjectNode) JacksonUtils
                    .readTree(configJson);

            Configuration config = chart.getConfiguration();

            // Apply chart type if specified
            if (configurationNode.has("type")) {
                String chartTypeStr = configurationNode.get("type").asString()
                        .toLowerCase();
                switch (chartTypeStr) {
                case "line":
                    config.getChart().setType(ChartType.LINE);
                    break;
                case "bar":
                    config.getChart().setType(ChartType.BAR);
                    break;
                case "column":
                    config.getChart().setType(ChartType.COLUMN);
                    break;
                case "pie":
                    config.getChart().setType(ChartType.PIE);
                    break;
                case "area":
                    config.getChart().setType(ChartType.AREA);
                    break;
                }
            }

            // Apply title if specified
            if (configurationNode.has("title")
                    && configurationNode.get("title").isObject()) {
                var titleNode = (ObjectNode) configurationNode.get("title");
                if (titleNode.has("text")) {
                    config.setTitle(titleNode.get("text").asString());
                }
            }

            // Apply tooltip configuration
            if (configurationNode.has("tooltip")
                    && configurationNode.get("tooltip").isObject()) {
                var tooltipNode = (ObjectNode) configurationNode.get("tooltip");
                if (tooltipNode.has("pointFormat")) {
                    config.getTooltip().setPointFormat(
                            tooltipNode.get("pointFormat").asString());
                }
            }

            // Remove properties that were already applied
            configurationNode.remove("series");
            configurationNode.remove("type");
            configurationNode.remove("title");
            configurationNode.remove("tooltip");

            // Apply any remaining options as additional options
            if (!configurationNode.isEmpty()) {
                if (chart.getElement() != null) {
                    chart.getElement().setPropertyJson("additionalOptions",
                            configurationNode);
                }
            }
        } catch (Exception e) {
            System.err.println("Error applying chart config: " + e.getMessage());
        }
    }
}
