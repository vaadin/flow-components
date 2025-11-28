package com.vaadin.flow.component.ai.tests;

import java.util.List;

import com.vaadin.flow.component.ai.orchestrator.AiOrchestrator;
import com.vaadin.flow.component.ai.pro.chart.ChartTools;
import com.vaadin.flow.component.ai.pro.chart.DataConverter;
import com.vaadin.flow.component.ai.pro.chart.DefaultDataConverter;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-ai/single-chart")
public class SingleChartView extends Div {
    public SingleChartView() {

        Chart chart = new Chart();
        MessageList messageList = new MessageList();
        MessageInput messageInput = new MessageInput();

        // Back-end services
        LLMProvider llmProvider = Helpers.createLlmProvider(); // Spring AI / LangChain4j
        DatabaseProvider databaseProvider = new InMemoryDatabaseProvider();
        DataConverter dataConverter = new DefaultDataConverter();

        // Predefined chart tool set & prompt (from chart feature module)
        List<LLMProvider.Tool> chartTools = ChartTools.createTools(
                chart,
                databaseProvider,
                dataConverter
        );
        String systemPrompt = ChartTools.defaultPrompt();

        // Generic orchestrator configured for a single chart
        AiOrchestrator orchestrator = AiOrchestrator.builder(llmProvider)
                .withSystemPrompt(systemPrompt)
                .withTools(chartTools)
                .withMessageList(messageList)
                .withInput(messageInput)
                .build();

        // Optional: ask the AI to create an initial chart
        orchestrator.prompt(
                "Create a column chart showing total revenue per month " +
                "for the last 12 months."
        );

        add(new HorizontalLayout(new VerticalLayout(messageList, messageInput), chart));
        setSizeFull();

    }
}
