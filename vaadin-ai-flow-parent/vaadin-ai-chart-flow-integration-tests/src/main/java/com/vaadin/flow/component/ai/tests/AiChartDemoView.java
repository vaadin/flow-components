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
import com.vaadin.flow.component.ai.pro.chart.AiChartPlugin;
import com.vaadin.flow.component.ai.pro.chart.AiChartPlugin.ChartState;
import com.vaadin.flow.component.ai.provider.langchain4j.LangChain4JLLMProvider;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

/**
 * Demo showing how to use AiChartPlugin with AiOrchestrator.
 * <p>
 * This demonstrates the plugin architecture where a single chat orchestrator
 * can be extended with chart visualization capabilities. Users can chat
 * naturally and also request chart visualizations from database data.
 * </p>
 * <p>
 * Example queries:
 * </p>
 * <ul>
 * <li>"Show me the sales data by region as a bar chart"</li>
 * <li>"Change that to a line chart"</li>
 * <li>"Display monthly revenue as a pie chart"</li>
 * </ul>
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ai/ai-chart-demo")
@CssImport("@vaadin/vaadin-lumo-styles/lumo.css")
public class AiChartDemoView extends HorizontalLayout {

    private ChartState savedState;

    public AiChartDemoView() {
        setSizeFull();

        // Chat section
        var messageList = new MessageList();
        messageList.setSizeFull();
        var messageInput = new MessageInput();
        messageInput.setWidthFull();
        var chatSection = new VerticalLayout(messageList, messageInput);
        chatSection.setWidth("50%");
        chatSection.setPadding(false);
        chatSection.setFlexGrow(1, messageList);

        // Chart visualization section
        var chart = new Chart();
        chart.setSizeFull();

        // Create AI chart plugin
        var chartPlugin = new AiChartPlugin(chart, new InMemoryDatabaseProvider());

        // Add state change listener to log state changes
        chartPlugin.addStateChangeListener(event -> {
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
                .modelName("gpt-4o-mini").build();
        var provider = new LangChain4JLLMProvider(model);

        // Create chat orchestrator with plugin
        AiOrchestrator.builder(provider, AiChartPlugin.getSystemPrompt())
                .withMessageList(messageList)
                .withInput(messageInput)
                .withPlugin(chartPlugin)
                .build();

        // State management buttons
        var restoreStateButton = new Button("Restore Saved State");
        restoreStateButton.setEnabled(false);
        restoreStateButton.addClickListener(e -> {
            if (savedState != null) {
                chartPlugin.restoreState(savedState);
            }
        });

        var saveStateButton = new Button("Save Current State", e -> {
            savedState = chartPlugin.getState();
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
}
