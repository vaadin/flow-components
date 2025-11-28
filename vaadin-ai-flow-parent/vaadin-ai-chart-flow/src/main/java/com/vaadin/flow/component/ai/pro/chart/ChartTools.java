/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.pro.chart;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.tool.AiToolBuilder;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.internal.JacksonUtils;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides predefined tool sets and system prompts for AI-powered chart
 * visualizations.
 * <p>
 * This utility class allows creating AI orchestrators capable of generating
 * chart visualizations from natural language queries. It supports line, bar,
 * pie, column, and area charts using Vaadin Charts.
 * </p>
 *
 * <h3>Example Usage:</h3>
 *
 * <pre>
 * Chart chart = new Chart();
 * DatabaseProvider db = new MyDatabaseProvider();
 * DataConverter converter = new DefaultDataConverter();
 *
 * List&lt;LLMProvider.Tool&gt; chartTools = ChartTools.createTools(chart, db,
 *         converter);
 * String systemPrompt = ChartTools.defaultPrompt();
 *
 * AiOrchestrator orchestrator = AiOrchestrator.builder(llmProvider)
 *         .withSystemPrompt(systemPrompt)
 *         .withTools(chartTools)
 *         .withInput(messageInput)
 *         .withMessageList(messageList)
 *         .build();
 * </pre>
 *
 * @author Vaadin Ltd
 */
public final class ChartTools {

    private ChartTools() {
        // Utility class
    }

    /**
     * Creates a set of tools for AI-powered chart visualizations.
     * <p>
     * The tools allow the AI to query databases and create/update charts based
     * on natural language instructions.
     * </p>
     *
     * @param chart
     *            the chart component to update
     * @param databaseProvider
     *            the database provider for schema and query execution
     * @param dataConverter
     *            the converter for transforming query results into chart data
     * @return a list of tools for chart visualization
     */
    public static List<LLMProvider.Tool> createTools(Chart chart,
            DatabaseProvider databaseProvider, DataConverter dataConverter) {

        List<LLMProvider.Tool> tools = new ArrayList<>();

        // Tool: getSchema
        tools.add(AiToolBuilder.name("getSchema")
                .description(
                        "Retrieves the database schema including tables, columns, and data types")
                .schema("""
                        {
                          "type": "object",
                          "properties": {}
                        }
                        """)
                .handle(args -> databaseProvider.getSchema()).build());

        // Tool: updateChart
        tools.add(AiToolBuilder.name("updateChart").description(
                "Creates or updates a chart visualization with SQL query results")
                .schema("""
                        {
                          "type": "object",
                          "properties": {
                            "query": {
                              "type": "string",
                              "description": "SQL SELECT query"
                            },
                            "config": {
                              "type": "object",
                              "description": "Chart configuration with 'type' (line/bar/column/pie/area) and Highcharts options",
                              "properties": {
                                "type": {
                                  "type": "string",
                                  "enum": ["line", "bar", "column", "pie", "area"]
                                }
                              }
                            }
                          },
                          "required": ["query"]
                        }
                        """).handle(args -> {
                            try {
                                String query = args.get("query").asString();
                                List<Map<String, Object>> results = databaseProvider
                                        .executeQuery(query);

                                Map<String, Object> config = new HashMap<>();
                                if (args.has("config")
                                        && args.get("config").isObject()) {
                                    config.put("chartConfig",
                                            args.get("config").toString());
                                }

                                renderChart(chart, results, config, dataConverter);

                                return "Chart updated successfully with "
                                        + results.size() + " data points";
                            } catch (Exception e) {
                                return "Error updating chart: " + e.getMessage();
                            }
                        }).build());

        return tools;
    }

    /**
     * Returns the default system prompt for chart visualization tools.
     * <p>
     * This prompt instructs the AI on how to use the chart tools effectively.
     * </p>
     *
     * @return the system prompt text
     */
    public static String defaultPrompt() {
        return """
                You have access to chart visualization capabilities:

                TOOLS:
                1. getSchema() - Retrieves database schema (tables, columns, types)
                2. updateChart(query, config) - Creates/updates a chart visualization
                   - Supports: line, bar, column, pie, area charts
                   - Config includes chart type and Highcharts options

                WORKFLOW:
                1. Use getSchema() to understand available data
                2. Create appropriate SQL queries (SELECT only)
                3. Use updateChart to create the visualization

                GUIDELINES:
                - For trends over time: use line or area charts
                - For comparisons: use bar or column charts
                - For proportions: use pie charts
                - Always specify a meaningful chart title in the config
                """;
    }

    // ===== Private Rendering Methods =====

    private static void renderChart(Chart chart,
            List<Map<String, Object>> queryResults,
            Map<String, Object> config, DataConverter dataConverter) {
        DataSeries series = dataConverter.convertToDataSeries(queryResults);

        Configuration chartConfig = chart.getConfiguration();
        chartConfig.setSeries(series);

        String chartConfigJson = (String) config.get("chartConfig");
        if (chartConfigJson != null && !chartConfigJson.isEmpty()) {
            applyChartConfig(chart, chartConfigJson);
        }

        chart.drawChart();
    }

    private static void applyChartConfig(Chart chart, String configJson) {
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
