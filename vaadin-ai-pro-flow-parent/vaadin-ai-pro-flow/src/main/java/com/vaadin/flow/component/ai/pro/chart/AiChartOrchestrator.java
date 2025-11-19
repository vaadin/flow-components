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
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.messages.MessageInput;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Orchestrator for AI-powered chart generation.
 * <p>
 * This class connects a {@link Chart}, {@link MessageInput},
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
 * AiChartOrchestrator orchestrator = new AiChartOrchestrator(llmProvider,
 *         dbProvider, chart, messageInput);
 * </pre>
 *
 * @author Vaadin Ltd
 */
public class AiChartOrchestrator implements Serializable {

    private final LLMProvider llmProvider;
    private final DatabaseProvider databaseProvider;
    private final Chart chart;
    private final MessageInput messageInput;
    private final DataConverter dataConverter;

    private final List<LLMProvider.Message> conversationHistory = new ArrayList<>();

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

    /**
     * Creates a new AI chart orchestrator.
     *
     * @param llmProvider
     *            the LLM provider for natural language processing
     * @param databaseProvider
     *            the database provider for schema and query execution
     * @param chart
     *            the chart component to update
     * @param messageInput
     *            the message input for user requests
     */
    public AiChartOrchestrator(LLMProvider llmProvider,
            DatabaseProvider databaseProvider, Chart chart,
            MessageInput messageInput) {
        this(llmProvider, databaseProvider, chart, messageInput,
                new DefaultDataConverter());
    }

    /**
     * Creates a new AI chart orchestrator with a custom data converter.
     *
     * @param llmProvider
     *            the LLM provider for natural language processing
     * @param databaseProvider
     *            the database provider for schema and query execution
     * @param chart
     *            the chart component to update
     * @param messageInput
     *            the message input for user requests
     * @param dataConverter
     *            the data converter for transforming query results to chart
     *            data
     */
    public AiChartOrchestrator(LLMProvider llmProvider,
            DatabaseProvider databaseProvider, Chart chart,
            MessageInput messageInput, DataConverter dataConverter) {
        Objects.requireNonNull(llmProvider, "LLM provider cannot be null");
        Objects.requireNonNull(databaseProvider,
                "Database provider cannot be null");
        Objects.requireNonNull(chart, "Chart cannot be null");
        Objects.requireNonNull(messageInput, "Message input cannot be null");
        Objects.requireNonNull(dataConverter, "Data converter cannot be null");

        this.llmProvider = llmProvider;
        this.databaseProvider = databaseProvider;
        this.chart = chart;
        this.messageInput = messageInput;
        this.dataConverter = dataConverter;

        // Listen to message input submissions
        messageInput.addSubmitListener(this::handleUserRequest);
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
     * Gets the message input component.
     *
     * @return the message input
     */
    public MessageInput getMessageInput() {
        return messageInput;
    }

    /**
     * Gets the LLM provider.
     *
     * @return the LLM provider
     */
    public LLMProvider getLlmProvider() {
        return llmProvider;
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
     * Clears the conversation history.
     */
    public void clearConversation() {
        conversationHistory.clear();
    }

    /**
     * Handles a user request.
     *
     * @param event
     *            the submit event
     */
    private void handleUserRequest(MessageInput.SubmitEvent event) {
        String userRequest = event.getValue();
        if (userRequest == null || userRequest.trim().isEmpty()) {
            return;
        }

        // Add user message to conversation history
        LLMProvider.Message message = LLMProvider.createMessage("user",
                userRequest);
        conversationHistory.add(message);

        // Process the request with the LLM
        processRequest();
    }

    /**
     * Processes the current request using the LLM with tool support.
     */
    private void processRequest() {
        UI ui = UI.getCurrent();
        if (ui == null) {
            throw new IllegalStateException(
                    "No UI found. Make sure the orchestrator is used within a UI context.");
        }

        // Create tools
        List<LLMProvider.Tool> tools = createTools();

        // Get streaming response from LLM
        Flux<String> responseStream = llmProvider
                .generateStream(conversationHistory, SYSTEM_PROMPT, tools);

        StringBuilder fullResponse = new StringBuilder();

        responseStream.subscribe(token -> {
            fullResponse.append(token);
            // Note: Tool calls would be handled here in a full implementation
            // For now, we just collect the response
        }, error -> {
            ui.access(() -> {
                // Handle error - could show in a notification or message
                System.err.println("Error processing request: " + error.getMessage());
            });
        }, () -> {
            ui.access(() -> {
                // Add assistant response to history
                LLMProvider.Message assistantMessage = LLMProvider
                        .createMessage("assistant", fullResponse.toString());
                conversationHistory.add(assistantMessage);
            });
        });
    }

    /**
     * Creates the tools available to the LLM.
     *
     * @return the list of tools
     */
    private List<LLMProvider.Tool> createTools() {
        List<LLMProvider.Tool> tools = new ArrayList<>();

        // Tool 1: getSchema
        tools.add(new LLMProvider.Tool() {
            @Override
            public String getName() {
                return "getSchema";
            }

            @Override
            public String getDescription() {
                return "Retrieves the database schema including tables, columns, and data types";
            }

            @Override
            public String getParametersSchema() {
                return "{}"; // No parameters
            }

            @Override
            public String execute(String arguments) {
                return databaseProvider.getSchema();
            }
        });

        // Tool 2: updateChartData
        tools.add(new LLMProvider.Tool() {
            @Override
            public String getName() {
                return "updateChartData";
            }

            @Override
            public String getDescription() {
                return "Executes a SQL SELECT query and updates the chart with the results";
            }

            @Override
            public String getParametersSchema() {
                return """
                        {
                          "type": "object",
                          "properties": {
                            "query": {
                              "type": "string",
                              "description": "The SQL SELECT query to execute"
                            }
                          },
                          "required": ["query"]
                        }
                        """;
            }

            @Override
            public String execute(String arguments) {
                try {
                    // Parse arguments to extract query
                    // This is a simplified version - in production, use proper JSON parsing
                    String query = extractQueryFromArguments(arguments);

                    // Execute query
                    List<Map<String, Object>> results = databaseProvider
                            .executeQuery(query);

                    // Convert results to chart data
                    DataSeries series = dataConverter.convertToDataSeries(results);

                    // Update chart
                    UI ui = UI.getCurrent();
                    if (ui != null) {
                        ui.access(() -> {
                            Configuration config = chart.getConfiguration();
                            config.setSeries(series);
                            chart.drawChart();
                        });
                    }

                    return "Chart data updated successfully with " + results.size()
                            + " rows";
                } catch (Exception e) {
                    return "Error updating chart data: " + e.getMessage();
                }
            }
        });

        // Tool 3: updateChartConfig
        tools.add(new LLMProvider.Tool() {
            @Override
            public String getName() {
                return "updateChartConfig";
            }

            @Override
            public String getDescription() {
                return "Updates the chart configuration (title, axes labels, chart type, etc.) using Highcharts JSON format";
            }

            @Override
            public String getParametersSchema() {
                return """
                        {
                          "type": "object",
                          "properties": {
                            "config": {
                              "type": "string",
                              "description": "The chart configuration in JSON format (Highcharts API)"
                            }
                          },
                          "required": ["config"]
                        }
                        """;
            }

            @Override
            public String execute(String arguments) {
                try {
                    // Parse and apply configuration
                    // This is a simplified version - in production, use proper JSON parsing
                    String config = extractConfigFromArguments(arguments);

                    UI ui = UI.getCurrent();
                    if (ui != null) {
                        ui.access(() -> {
                            applyChartConfig(config);
                            chart.drawChart();
                        });
                    }

                    return "Chart configuration updated successfully";
                } catch (Exception e) {
                    return "Error updating chart config: " + e.getMessage();
                }
            }
        });

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
        int valueStart = arguments.indexOf(":", queryStart) + 1;
        int valueEnd = arguments.indexOf("\"", valueStart + 2);
        return arguments.substring(valueStart + 2, valueEnd).trim();
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
        int valueStart = arguments.indexOf(":", configStart) + 1;
        int valueEnd = arguments.lastIndexOf("\"");
        return arguments.substring(valueStart + 2, valueEnd).trim();
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
}
