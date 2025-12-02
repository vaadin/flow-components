/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.tests;

import com.vaadin.flow.component.ai.orchestrator.AiOrchestrator;
import com.vaadin.flow.component.ai.pro.chart.DataVisualizationPlugin;
import com.vaadin.flow.component.ai.provider.langchain4j.LangChain4JLLMProvider;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

/**
 * Demo showing how to use DataVisualizationPlugin with AiOrchestrator.
 * <p>
 * This demonstrates the plugin architecture where a single chat orchestrator
 * can be extended with data visualization capabilities. Users can chat
 * naturally and also request data visualizations.
 * </p>
 * <p>
 * Example queries:
 * </p>
 * <ul>
 * <li>"Show me the sales data by region as a chart"</li>
 * <li>"Convert that to a table"</li>
 * <li>"What's the total revenue?" (shows as KPI)</li>
 * <li>"Tell me a joke" (regular chat - no visualization)</li>
 * </ul>
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ai/chat-with-dataviz-plugin")
@CssImport("@vaadin/vaadin-lumo-styles/lumo.css")
public class AiChatWithDataVizPluginDemo extends HorizontalLayout {

    private Object savedState;

    public AiChatWithDataVizPluginDemo() {
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

        // Visualization section
        var visualizationContainer = new Div();
        visualizationContainer.setSizeFull();

        // Create data visualization plugin
        var dataVizPlugin = DataVisualizationPlugin
                .create(new InMemoryDatabaseProvider())
                .withVisualizationContainer(visualizationContainer)
                .build();

        // Create LLM provider
        var model = OpenAiStreamingChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-4o-mini").build();
        var provider = new LangChain4JLLMProvider(model);

        // Create chat orchestrator with plugin
        AiOrchestrator.create(provider)
                .withMessageList(messageList)
                .withInput(messageInput)
                .withPlugin(dataVizPlugin)
                .build();

        // State management buttons
        var restoreStateButton = new Button("Restore Saved State");
        restoreStateButton.setEnabled(false);
        restoreStateButton.addClickListener(e -> {
            if (savedState != null) {
                dataVizPlugin.restoreState(savedState);
            }
        });

        var saveStateButton = new Button("Save Current State", e -> {
            savedState = dataVizPlugin.captureState();
            if (savedState != null) {
                restoreStateButton.setEnabled(true);
            }
        });
        saveStateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var buttonBar = new HorizontalLayout(saveStateButton, restoreStateButton);

        var vizSection = new VerticalLayout(visualizationContainer, buttonBar);
        vizSection.setWidth("50%");
        vizSection.setPadding(false);
        vizSection.setFlexGrow(1, visualizationContainer);
        vizSection.setFlexGrow(0, buttonBar);

        add(chatSection, vizSection);       
    }
}
