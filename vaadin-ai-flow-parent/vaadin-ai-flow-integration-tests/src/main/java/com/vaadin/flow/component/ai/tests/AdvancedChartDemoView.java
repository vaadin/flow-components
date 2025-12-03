/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.ai.orchestrator.AiOrchestrator;
import com.vaadin.flow.component.ai.pro.chart.ChartAiController;
import com.vaadin.flow.component.ai.pro.chart.ChartAiController.ChartState;
import com.vaadin.flow.component.ai.provider.langchain4j.LangChain4JLLMProvider;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

/**
 * Advanced demo showcasing all chart types supported by ChartAiController.
 * <p>
 * This demo uses the {@link AdvancedDatabaseProvider} which includes comprehensive
 * datasets for testing all chart types including:
 * </p>
 * <ul>
 * <li>Basic charts (line, bar, column, pie)</li>
 * <li>Bullet charts (with targets)</li>
 * <li>Range charts (arearange, columnrange, areasplinerange)</li>
 * <li>BoxPlot charts</li>
 * <li>OHLC/Candlestick charts</li>
 * <li>Bubble charts</li>
 * <li>Sankey diagrams</li>
 * <li>Xrange/Gantt charts</li>
 * <li>Scatter plots</li>
 * </ul>
 *
 * <h3>Example Queries:</h3>
 * <ul>
 * <li>"Show me a bullet chart of sales with targets"</li>
 * <li>"Display temperature ranges as an arearange chart"</li>
 * <li>"Create a candlestick chart from stock prices"</li>
 * <li>"Show test scores as a boxplot"</li>
 * <li>"Create a bubble chart of countries by GDP, life expectancy and population"</li>
 * <li>"Display energy flow as a sankey diagram"</li>
 * <li>"Show project tasks as a gantt chart"</li>
 * </ul>
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ai/advanced-chart-demo")
@CssImport("@vaadin/vaadin-lumo-styles/lumo.css")
public class AdvancedChartDemoView extends HorizontalLayout {

    private ChartState savedState;
    private AiOrchestrator orchestrator;

    public AdvancedChartDemoView() {
        setSizeFull();

        // Chat section with instructions
        var messageList = new MessageList();
        messageList.setSizeFull();
        var messageInput = new MessageInput();
        messageInput.setWidthFull();

        // Chart visualization section
        var chart = new Chart();
        chart.setSizeFull();

        // Create AI chart controller with advanced database
        var chartController = new ChartAiController(chart, new AdvancedDatabaseProvider());

        // Add state change listener for debugging
        chartController.addStateChangeListener(event -> {
            ChartState state = event.getState();
            System.out.println("=== Chart State Changed ===");
            System.out.println("SQL Query: " + state.sqlQuery());
            System.out.println("Configuration:");
            try {
                ObjectMapper mapper = new ObjectMapper();
                Object json = mapper.readValue(state.configuration(), Object.class);
                String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
                System.out.println(prettyJson);
            } catch (Exception e) {
                System.out.println(state.configuration());
            }
            System.out.println("===========================");
        });

        // Create LLM provider
        var model = OpenAiStreamingChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-4o")
                .build();
        var provider = new LangChain4JLLMProvider(model);

        // Create chat orchestrator with controller
        orchestrator = AiOrchestrator.builder(provider,
                "You are a data visualization expert. " + ChartAiController.getSystemPrompt())
                .withMessageList(messageList)
                .withInput(messageInput)
                .withController(chartController)
                .build();

        var instructions = createInstructions();

        var chatSection = new VerticalLayout(instructions, messageList, messageInput);
        chatSection.setWidth("50%");
        chatSection.setPadding(false);
        chatSection.setFlexGrow(0, instructions);
        chatSection.setFlexGrow(1, messageList);
        chatSection.setFlexGrow(0, messageInput);

        // State management buttons
        var restoreStateButton = new Button("Restore Saved State");
        restoreStateButton.setEnabled(false);
        restoreStateButton.addClickListener(e -> {
            if (savedState != null) {
                chartController.restoreState(savedState);
            }
        });

        var saveStateButton = new Button("Save Current State", e -> {
            savedState = chartController.getState();
            if (savedState != null) {
                restoreStateButton.setEnabled(true);
            }
        });
        saveStateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var buttonBar = new HorizontalLayout(saveStateButton, restoreStateButton);

        var chartSection = new VerticalLayout(chart, buttonBar);
        chartSection.setWidth("50%");
        chartSection.setPadding(false);
        chartSection.setFlexGrow(1, chart);
        chartSection.setFlexGrow(0, buttonBar);

        add(chatSection, chartSection);
    }

    private Div createInstructions() {
        var container = new Div();
        container.getStyle()
                .set("padding", "var(--lumo-space-m)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("margin-bottom", "var(--lumo-space-s)");

        var title = new H2("Advanced Chart Demo");
        title.getStyle().set("margin-top", "0");

        var description = new Paragraph(
                "This demo showcases all chart types with comprehensive datasets. " +
                "Click any example query below to try it!");

        var examplesTitle = new H3("Example Queries:");
        examplesTitle.getStyle().set("margin-bottom", "var(--lumo-space-xs)");

        var examples = new Div();
        examples.getStyle()
                .set("font-family", "var(--lumo-font-family)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("line-height", "1.6");

        // Create clickable examples
        String[] exampleQueries = {
            "Show sales vs targets as a bullet chart",
            "Display temperature ranges as arearange",
            "Show stock prices as candlestick",
            "Display test scores as a boxplot",
            "Create a bubble chart of countries",
            "Show energy flow as sankey diagram",
            "Display project tasks as gantt chart",
            "Show scatter plot of x and y values"
        };

        String[] exampleLabels = {
            "Bullet", "Range", "Candlestick", "BoxPlot",
            "Bubble", "Sankey", "Gantt", "Scatter"
        };

        StringBuilder html = new StringBuilder("<ul style=\"margin: 0; padding-left: var(--lumo-space-l);\">");
        for (int i = 0; i < exampleQueries.length; i++) {
            String query = exampleQueries[i];
            String label = exampleLabels[i];
            String elementId = "example-" + i;

            html.append("<li><b>").append(label).append(":</b> ")
                .append("<span id=\"").append(elementId).append("\" ")
                .append("style=\"cursor: pointer; color: var(--lumo-primary-color); text-decoration: underline;\" ")
                .append("title=\"Click to execute this query\">")
                .append("\"").append(query).append("\"")
                .append("</span></li>");

            // Add server-side click listener
            final String queryToExecute = query;
            examples.getElement().addEventListener("click", event -> {
                orchestrator.prompt(queryToExecute);
            }).addEventData("element.id").setFilter("event.target.id === '" + elementId + "'");
        }
        html.append("</ul>");

        examples.getElement().setProperty("innerHTML", html.toString());

        container.add(title, description, examplesTitle, examples);
        return container;
    }
}
