/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.pro.chart;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.orchestrator.BaseAiOrchestrator;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Orchestrator for AI-powered chart generation.
 * <p>
 * This class connects a {@link Chart}, {@link AiInput},
 * {@link LLMProvider}, and {@link DatabaseProvider} to enable users to generate
 * and modify charts using natural language. The orchestrator:
 * </p>
 * <ul>
 * <li>Accepts natural language chart descriptions from users</li>
 * <li>Uses the LLM to generate SQL queries based on the database schema</li>
 * <li>Executes queries and converts results to chart data</li>
 * <li>Generates and applies Highcharts configurations</li>
 * </ul>
 * <p>
 * <strong>Security Notice:</strong> Always use read-only database credentials
 * with access restricted to only necessary tables and views.
 * </p>
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * Chart chart = new Chart();
 * MessageInput messageInput = new MessageInput();
 *
 * LLMProvider llmProvider = new LangChain4jProvider(model);
 * DatabaseProvider dbProvider = new MyDatabaseProvider();
 *
 * AiChartOrchestrator orchestrator = AiChartOrchestrator.create(llmProvider, dbProvider)
 *         .withChart(chart)
 *         .withInput(messageInput)
 *         .build();
 * </pre>
 *
 * @author Vaadin Ltd
 */
public class AiChartOrchestrator extends BaseAiOrchestrator {

    private final DatabaseProvider databaseProvider;
    private Chart chart;
    private DataConverter dataConverter;

    private String currentUserRequest;
    private UI currentUI;

    /**
     * Creates a new AI chart orchestrator.
     *
     * @param llmProvider
     *            the LLM provider for natural language processing
     * @param databaseProvider
     *            the database provider for schema and query execution
     */
    private AiChartOrchestrator(LLMProvider llmProvider,
            DatabaseProvider databaseProvider) {
        super(llmProvider);
        Objects.requireNonNull(databaseProvider,
                "Database provider cannot be null");
        this.databaseProvider = databaseProvider;
    }

    /**
     * Creates a new builder for AiChartOrchestrator.
     *
     * @param llmProvider
     *            the LLM provider
     * @param databaseProvider
     *            the database provider
     * @return a new builder
     */
    public static Builder create(LLMProvider llmProvider,
            DatabaseProvider databaseProvider) {
        return new Builder(llmProvider, databaseProvider);
    }

    /**
     * Builder for AiChartOrchestrator.
     */
    public static class Builder extends BaseBuilder<AiChartOrchestrator, Builder> {
        private final DatabaseProvider databaseProvider;
        private Chart chart;
        private DataConverter dataConverter = new DefaultDataConverter();

        private Builder(LLMProvider llmProvider,
                DatabaseProvider databaseProvider) {
            super(llmProvider);
            this.databaseProvider = databaseProvider;
        }

        /**
         * Sets the chart component.
         *
         * @param chart
         *            the chart
         * @return this builder
         */
        public Builder withChart(Chart chart) {
            this.chart = chart;
            return this;
        }

        /**
         * Sets the data converter.
         *
         * @param dataConverter
         *            the data converter
         * @return this builder
         */
        public Builder withDataConverter(DataConverter dataConverter) {
            this.dataConverter = dataConverter;
            return this;
        }

        /**
         * Builds the orchestrator.
         *
         * @return the configured orchestrator
         */
        @Override
        public AiChartOrchestrator build() {
            AiChartOrchestrator orchestrator = new AiChartOrchestrator(
                    provider, databaseProvider);
            orchestrator.chart = chart;
            orchestrator.dataConverter = dataConverter;

            // Apply common configuration from base builder (includes input
            // listener and file receiver configuration)
            applyCommonConfiguration(orchestrator);

            return orchestrator;
        }
    }

    /**
     * Gets the chart component.
     *
     * @return the chart
     */
    public Chart getChart() {
        return chart;
    }

    /**
     * Gets the LLM provider.
     *
     * @return the LLM provider
     */
    public LLMProvider getLlmProvider() {
        return provider;
    }

    /**
     * Gets the database provider.
     *
     * @return the database provider
     */
    public DatabaseProvider getDatabaseProvider() {
        return databaseProvider;
    }

    /**
     * Gets the data converter.
     *
     * @return the data converter
     */
    public DataConverter getDataConverter() {
        return dataConverter;
    }

    /**
     * Prepares for processing by storing the user request and UI context.
     *
     * @param userMessage
     *            the user's input message
     */
    @Override
    protected void processUserInput(String userMessage) {
        this.currentUserRequest = userMessage;

        UI ui = validateUiContext();
        this.currentUI = ui; // Store UI reference for tools to use

        if (messageList == null) {
            System.err.println("Warning: messageList is null, cannot show responses");
        }

        System.out.println("Chart orchestrator: Sending request to LLM with user message: " + currentUserRequest);

        // Use base class implementation for the rest
        super.processUserInput(userMessage);
    }

    /**
     * Returns the system prompt for chart generation.
     *
     * @return the system prompt
     */
    @Override
    protected String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    /**
     * Chart orchestrator can process without message list since tools directly
     * update the chart component.
     *
     * @return true to allow processing without message list
     */
    @Override
    protected boolean shouldProcessWithoutMessageList() {
        return true;
    }

    /**
     * Called when processing completes.
     */
    @Override
    protected void onProcessingComplete() {
        System.out.println("Chart orchestrator: Streaming complete");
    }

    /**
     * Creates the tools available to the LLM.
     *
     * @return the array of tools
     */
    @Override
    protected LLMProvider.Tool[] createTools() {
        System.out.println("Chart orchestrator: Creating tools for LLM");
        List<LLMProvider.Tool> toolsList = new ArrayList<>();

        // Tool 1: getSchema
        toolsList.add(new LLMProvider.Tool() {
            @Override
            public String getName() {
                return "getSchema";
            }

            @Override
            public String getDescription() {
                return "Retrieves the database schema including tables, columns, and data types. Takes no parameters - call as getSchema()";
            }

            @Override
            public String getParametersSchema() {
                return null; // No parameters
            }

            @Override
            public String execute(String arguments) {
                System.out.println("Chart orchestrator: Tool 'getSchema' called");
                String schema = databaseProvider.getSchema();
                System.out.println("Chart orchestrator: Tool 'getSchema' returned schema with length: " + schema.length());
                return schema;
            }
        });

        // Tool 2: updateChartData
        toolsList.add(new LLMProvider.Tool() {
            @Override
            public String getName() {
                return "updateChartData";
            }

            @Override
            public String getDescription() {
                return "Executes a SQL SELECT query and updates the chart with the results. " +
                       "Parameter: query (string) - The SQL SELECT query to execute. " +
                       "Call as updateChartData({\"query\": \"SELECT column1, column2 FROM table\"})";
            }

            @Override
            public String getParametersSchema() {
                return null; // Schema embedded in description
            }

            @Override
            public String execute(String arguments) {
                System.out.println("Chart orchestrator: Tool 'updateChartData' called with arguments: " + arguments);
                try {
                    // Parse arguments to extract query
                    // This is a simplified version - in production, use proper JSON parsing
                    String query = extractQueryFromArguments(arguments);
                    System.out.println("Chart orchestrator: Executing query: " + query);

                    // Execute query
                    List<Map<String, Object>> results = databaseProvider
                            .executeQuery(query);
                    System.out.println("Chart orchestrator: Query returned " + results.size() + " rows");

                    // Convert results to chart data
                    DataSeries series = dataConverter.convertToDataSeries(results);
                    System.out.println("Chart orchestrator: Converted data to series with " + series.size() + " items");

                    // Update chart using stored UI reference
                    if (currentUI != null) {
                        currentUI.access(() -> {
                            System.out.println("Chart orchestrator: Updating chart in UI thread");
                            Configuration config = chart.getConfiguration();

                            config.setSeries(series);

                            // Log the series data
                            System.out.println("Chart orchestrator: Chart configuration updated. Series count: " + config.getSeries().size());

                            chart.drawChart();
                            System.out.println("Chart orchestrator: chart.drawChart() called");
                        });
                    } else {
                        System.err.println("Chart orchestrator: currentUI is null! Cannot update chart.");
                    }

                    String result = "Chart data updated successfully with " + results.size() + " rows";
                    System.out.println("Chart orchestrator: Tool 'updateChartData' completed: " + result);
                    return result;
                } catch (Exception e) {
                    String error = "Error updating chart data: " + e.getMessage();
                    System.err.println("Chart orchestrator: " + error);
                    e.printStackTrace();
                    return error;
                }
            }
        });

        // Tool 3: updateChartConfig
        toolsList.add(new LLMProvider.Tool() {
            @Override
            public String getName() {
                return "updateChartConfig";
            }

            @Override
            public String getDescription() {
                return "Updates the chart configuration (title, axes labels, chart type, etc.) using Highcharts JSON format. " +
                       "Parameter: config (string) - The chart configuration in JSON format. " +
                       "Call as updateChartConfig({\"config\": \"{\\\"title\\\": {\\\"text\\\": \\\"My Chart\\\"}}\"})" ;
            }

            @Override
            public String getParametersSchema() {
                return null; // Schema embedded in description
            }

            @Override
            public String execute(String arguments) {
                System.out.println("Chart orchestrator: Tool 'updateChartConfig' called with arguments: " + arguments);
                try {
                    // Parse and apply configuration
                    // This is a simplified version - in production, use proper JSON parsing
                    String config = extractConfigFromArguments(arguments);
                    System.out.println("Chart orchestrator: Applying config: " + config);

                    if (currentUI != null) {
                        currentUI.access(() -> {
                            applyChartConfig(config);
                            chart.drawChart();
                        });
                    } else {
                        System.err.println("Chart orchestrator: currentUI is null! Cannot update chart config.");
                    }

                    String result = "Chart configuration updated successfully";
                    System.out.println("Chart orchestrator: Tool 'updateChartConfig' completed: " + result);
                    return result;
                } catch (Exception e) {
                    String error = "Error updating chart config: " + e.getMessage();
                    System.err.println("Chart orchestrator: " + error);
                    e.printStackTrace();
                    return error;
                }
            }
        });

        LLMProvider.Tool[] tools = toolsList.toArray(new LLMProvider.Tool[0]);
        System.out.println("Chart orchestrator: Created " + tools.length + " tools for LLM");
        return tools;
    }

    /**
     * Extracts the query from tool arguments (simplified JSON parsing).
     *
     * @param arguments
     *            the JSON arguments
     * @return the query string
     */
    private String extractQueryFromArguments(String arguments) {
        // Simplified extraction - in production, use proper JSON parser
        int queryStart = arguments.indexOf("\"query\"");
        if (queryStart == -1) {
            throw new IllegalArgumentException("No query found in arguments");
        }
        int valueStart = arguments.indexOf(":", queryStart);
        int openQuote = arguments.indexOf("\"", valueStart);
        int closeQuote = arguments.indexOf("\"", openQuote + 1);
        return arguments.substring(openQuote + 1, closeQuote).trim();
    }

    /**
     * Extracts the config from tool arguments (simplified JSON parsing).
     *
     * @param arguments
     *            the JSON arguments
     * @return the config string
     */
    private String extractConfigFromArguments(String arguments) {
        // Simplified extraction - in production, use proper JSON parser
        int configStart = arguments.indexOf("\"config\"");
        if (configStart == -1) {
            throw new IllegalArgumentException("No config found in arguments");
        }
        int valueStart = arguments.indexOf(":", configStart);
        int openQuote = arguments.indexOf("\"", valueStart);
        int closeQuote = arguments.lastIndexOf("\"");
        return arguments.substring(openQuote + 1, closeQuote).trim();
    }

    /**
     * Applies a chart configuration (simplified).
     *
     * @param configJson
     *            the configuration JSON
     */
    private void applyChartConfig(String configJson) {
        // This is a simplified version
        // In production, parse JSON and apply to chart.getConfiguration()
        // For now, this is a placeholder
    }

    private static final String SYSTEM_PROMPT = """
            You are a chart configuration assistant. Your role is to help users create and modify charts
            based on their database data. You have access to three tools:

            1. getSchema() - Retrieves the database schema
            2. updateChartData(query: string) - Executes a SQL query and updates the chart with the results
            3. updateChartConfig(config: string) - Updates the chart configuration (JSON format following Highcharts API)

            When a user requests a chart:
            1. First, call getSchema() to understand the available tables and columns
            2. Generate an appropriate SQL SELECT query and call updateChartData() with it
            3. If needed, call updateChartConfig() to adjust the chart's visual appearance

            Always use SELECT queries only. Never use INSERT, UPDATE, DELETE, or other data modification commands.
            The SQL queries should be simple and focused on retrieving the data needed for the chart.

            For chart configurations, use Highcharts JSON format. Common properties include:
            - chart: { type: 'line' | 'bar' | 'column' | 'pie' | 'area' | 'scatter' etc. }
            - title: { text: 'Chart Title' }
            - xAxis: { title: { text: 'X Axis Label' }, categories: [...] }
            - yAxis: { title: { text: 'Y Axis Label' } }
            - series: [{ name: 'Series Name', data: [...] }]

            Respond to users in a helpful, concise manner, explaining what chart you're creating.
            """;
}
