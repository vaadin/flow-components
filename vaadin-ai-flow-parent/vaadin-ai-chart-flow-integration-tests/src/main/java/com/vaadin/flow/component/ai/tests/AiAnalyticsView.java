package com.vaadin.flow.component.ai.tests;

import java.util.List;

import com.vaadin.flow.component.ai.orchestrator.AiOrchestrator;
import com.vaadin.flow.component.ai.pro.chart.ChartState;
import com.vaadin.flow.component.ai.pro.chart.ChartStateSupport;
import com.vaadin.flow.component.ai.pro.chart.ChartTools;
import com.vaadin.flow.component.ai.pro.chart.DefaultDataConverter;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.state.AiStateSupport;
import com.vaadin.flow.component.ai.tool.AiToolBuilder;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.Route;

@Route("ai/analytics")
public class AiAnalyticsView extends VerticalLayout {

    private final AiOrchestrator orchestrator;
    private final AiStateSupport<ChartState> chartStateSupport;

    // In a real app this would be stored in a DB or user session
    private ChartState lastSavedChartState;

    public AiAnalyticsView() {
        MessageList messageList = new MessageList();
        MessageInput messageInput = new MessageInput();
        Upload upload = new Upload();

        Chart salesChart = new Chart();

        Grid<String> ordersGrid = new Grid<>();
        ordersGrid.addColumn(row -> row).setHeader("Order Summary");



        Button saveViewButton = new Button("Save view");
        Button restoreViewButton = new Button("Restore view");
        Button summarizeButton = new Button("Summarize dashboard");

        // ... layout omitted

        var actions = new Div();

        add(messageList, upload, salesChart, ordersGrid, actions, messageInput);

        // --- Back-end services ---
        LLMProvider llmProvider = createLlmProvider(); // Spring AI / LangChain4j
        DatabaseProvider databaseProvider = new InMemoryDatabaseProvider();

        // --- Chart tool set (provided by feature module) ---
        List<LLMProvider.Tool> chartTools = ChartTools.createTools(
                salesChart,
                databaseProvider,
                new DefaultDataConverter()
        );
        String chartSystemPrompt = ChartTools.defaultPrompt();

        // --- Optional state support for charts ---
        chartStateSupport = new ChartStateSupport(salesChart);

        // --- Additional custom tool: update grid from SQL ---
        LLMProvider.Tool updateGridTool = AiToolBuilder
            .name("updateOrderGrid")
            .description("Executes a SQL query and fills the orders grid.")
            .schema("""
            {
              "type": "object",
              "properties": {
                "sqlQuery": {
                  "type": "string",
                  "description": "A SQL SELECT query."
                }
              },
              "required": ["sqlQuery"]
            }
            """)
            .handle(args -> {
                

                List<String> rows = List.of(
                    "Order #1001: $250.00 - Completed",
                    "Order #1002: $125.50 - Pending",
                    "Order #1003: $320.75 - Shipped"
                );

                ordersGrid.setItems(rows);
                return "Grid updated with " + rows.size() + " rows";
            })
            .build();

        // --- Compose orchestrator using tool sets + custom tools ---
        orchestrator = AiOrchestrator.builder(llmProvider)
            .withSystemPrompt(chartSystemPrompt) // From chart tool set
            .withTools(chartTools)               // From chart tool set
            .withTools(updateGridTool)           // Custom tool
            .withInput(messageInput)
            .withMessageList(messageList)
            .withFileReceiver(upload)
            .build();

        // Optional: initial programmatic prompt to set up the view
        orchestrator.prompt(
            "Create a monthly sales chart for the last 6 months and " +
            "populate the orders grid with the 50 most recent orders."
        );

        // --- State control buttons ---
        saveViewButton.addClickListener(e -> {
            lastSavedChartState = chartStateSupport.capture();
            // ... notify user, persist, etc.
        });

        restoreViewButton.addClickListener(e -> {
            if (lastSavedChartState != null) {
                chartStateSupport.restore(lastSavedChartState);
                // ... notify user
            } else {
                // ... handle empty state
            }
        });

        // --- AI-driven summarization via programmatic prompt ---
        summarizeButton.addClickListener(e -> {
            orchestrator.prompt(
                """
                Summarize the current dashboard for a business stakeholder:
                - Explain what the sales chart shows...
                - Highlight anything notable in the orders grid...
                """
            );
        });
    }

    private LLMProvider createLlmProvider() {
        return Helpers.createLlmProvider();
    }
}
