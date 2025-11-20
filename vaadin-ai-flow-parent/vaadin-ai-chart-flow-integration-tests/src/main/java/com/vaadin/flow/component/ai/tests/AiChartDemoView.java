/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.tests;

import com.vaadin.flow.component.ai.pro.chart.AiChartOrchestrator;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.provider.langchain4j.LangChain4JLLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

/**
 * Demo view for AI Chart functionality.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ai/ai-chart-demo")
public class AiChartDemoView extends VerticalLayout {

    private AiChartOrchestrator orchestrator;

    public AiChartDemoView() {
        // Enable push for streaming responses
        getUI().ifPresent(ui -> ui.getPushConfiguration()
                .setPushMode(PushMode.AUTOMATIC));

        setSpacing(true);
        setPadding(true);
        setHeightFull();

        H2 title = new H2("AI Chart Demo");
        add(title);

        // Check for API key
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            Div error = new Div();
            error.setText(
                    "Error: OPENAI_API_KEY environment variable is not set. "
                            + "Please set it to use this demo.");
            error.getStyle().set("color", "red").set("padding", "20px");
            add(error);
            return;
        }

        // Instructions
        Paragraph instructions = new Paragraph(
                "Ask the AI to create charts from the sample database. "
                        + "Try requests like: 'Show me monthly revenue', "
                        + "'Create a bar chart of employee salaries by department', "
                        + "or 'Display product sales'.");
        instructions.getStyle().set("color", "gray");
        add(instructions);

        // Create chart
        Chart chart = new Chart();
        chart.setWidthFull();
        chart.setHeight("400px");
        add(chart);

        // Create message input
        MessageInput messageInput = new MessageInput();
        messageInput.setWidthFull();

        MessageList messageList = new MessageList();
        messageList.setWidthFull();

        // Create providers
        StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
                .apiKey(apiKey).modelName("gpt-4o-mini").build();
        LLMProvider llmProvider = new LangChain4JLLMProvider(model);
        DatabaseProvider databaseProvider = new DummyDatabaseProvider();

        // Create orchestrator using builder pattern
        orchestrator = AiChartOrchestrator.create(llmProvider, databaseProvider)
                .withChart(chart)
                .withInput(messageInput)
                .withMessageList(messageList)
                .build();

        add(messageList, messageInput);
    }
}
