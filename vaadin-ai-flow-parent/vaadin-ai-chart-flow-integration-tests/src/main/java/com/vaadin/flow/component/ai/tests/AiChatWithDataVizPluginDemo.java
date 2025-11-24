/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.tests;

import com.vaadin.flow.component.ai.chat.AiChatOrchestrator;
import com.vaadin.flow.component.ai.pro.chart.DataVisualizationPlugin;
import com.vaadin.flow.component.ai.pro.chart.VisualizationType;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.provider.langchain4j.LangChain4JLLMProvider;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

/**
 * Demo showing how to use DataVisualizationPlugin with AiChatOrchestrator.
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
public class AiChatWithDataVizPluginDemo extends VerticalLayout {

    public AiChatWithDataVizPluginDemo() {
        // Enable push for streaming
        getUI().ifPresent(
                ui -> ui.getPushConfiguration().setPushMode(PushMode.AUTOMATIC));

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        H2 title = new H2("AI Chat with Data Visualization Plugin");
        add(title);

        // Check API key
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            Div error = new Div();
            error.setText(
                    "Error: OPENAI_API_KEY environment variable not set.");
            error.getStyle().set("color", "red").set("padding", "20px");
            add(error);
            return;
        }

        // Instructions
        Paragraph instructions = new Paragraph(
                "This demo shows a chat orchestrator extended with data visualization plugin. "
                        + "Try: 'Show sales by region', 'Convert to table', 'What's total revenue?', or just chat!");
        instructions.getStyle().set("color", "gray");
        add(instructions);

        // Create main layout with chat on left, visualization on right
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);

        // === LEFT SIDE: Chat ===
        VerticalLayout chatSection = new VerticalLayout();
        chatSection.setWidth("50%");
        chatSection.setHeightFull();
        chatSection.setSpacing(false);
        chatSection.setPadding(false);

        MessageList messageList = new MessageList();
        messageList.setWidthFull();
        messageList.getStyle().set("flex-grow", "1");

        MessageInput messageInput = new MessageInput();
        messageInput.setWidthFull();

        chatSection.add(messageList, messageInput);

        // === RIGHT SIDE: Visualization ===
        VerticalLayout vizSection = new VerticalLayout();
        vizSection.setWidth("50%");
        vizSection.setHeightFull();
        vizSection.setSpacing(false);
        vizSection.setPadding(false);

        Div visualizationContainer = new Div();
        visualizationContainer.setSizeFull();
        visualizationContainer.getStyle().set("border",
                "1px solid var(--lumo-contrast-10pct)").set("border-radius",
                        "var(--lumo-border-radius-m)");

        Paragraph vizPlaceholder = new Paragraph(
                "Visualizations will appear here...");
        vizPlaceholder.getStyle().set("color",
                "var(--lumo-secondary-text-color)").set("padding",
                        "var(--lumo-space-l)");
        visualizationContainer.add(vizPlaceholder);

        vizSection.add(visualizationContainer);

        mainLayout.add(chatSection, vizSection);
        add(mainLayout);
        setFlexGrow(1, mainLayout);

        // === Setup AI Orchestrator with Plugin ===

        // Create LLM provider
        StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
                .apiKey(apiKey).modelName("gpt-4o-mini").build();
        LLMProvider llmProvider = new LangChain4JLLMProvider(model);

        // Create data visualization plugin
        DataVisualizationPlugin dataVizPlugin = DataVisualizationPlugin
                .create(new InMemoryDatabaseProvider())
                .withVisualizationContainer(visualizationContainer)
                .withInitialType(VisualizationType.CHART).build();

        // Create chat orchestrator with plugin
        AiChatOrchestrator orchestrator = AiChatOrchestrator.create(llmProvider)
                .withMessageList(messageList)
                .withInput(messageInput)
                .withPlugin(dataVizPlugin) // <-- Plugin added here!
                .build();

        // Add welcome message
        messageList.setItems(new com.vaadin.flow.component.messages.MessageListItem(
                "Welcome! I'm your AI assistant with data visualization capabilities. "
                        + "Ask me anything, or request to see data as charts, tables, or KPIs.",
                java.time.Instant.now(),
                "Assistant"));
    }
}
