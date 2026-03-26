/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.ai.tests;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.chart.ChartAIController;
import com.vaadin.flow.component.ai.chart.ChartEntry.ChartState;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.LangChain4JLLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;

import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

/**
 * Demo page for AI-powered chart visualization using ChartAIController.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ai/ai-chart-demo")
public class AIChartDemoPage extends HorizontalLayout {

    private ChartState savedState;

    public AIChartDemoPage() {
        setSizeFull();

        UI.getCurrent().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

        // Chat section
        var messageList = new MessageList();
        messageList.setSizeFull();
        var messageInput = new MessageInput();
        messageInput.setWidthFull();
        var chatSection = new VerticalLayout(messageList, messageInput);
        chatSection.setWidth("50%");
        chatSection.setPadding(false);
        chatSection.setFlexGrow(1, messageList);

        // Chart section
        var chart = new Chart();
        chart.setSizeFull();

        var chartController = new ChartAIController(chart,
                new InMemoryDatabaseProvider());

        // chartController.addStateChangeListener(event -> {
        //     ChartState state = event.getState();
        //     System.out.println(
        //             "Chart state changed - Queries: " + state.queries());
        // });

        var apiKey = System.getenv("OPENAI_API_KEY");
        var model = OpenAiStreamingChatModel.builder().apiKey(apiKey)
                .modelName("gpt-5.4-mini").build();
        var provider = new LangChain4JLLMProvider(model);

        // Create orchestrator with controller
        AIOrchestrator.builder(provider, ChartAIController.getSystemPrompt())
                .withMessageList(messageList).withInput(messageInput)
                .withController(chartController).build();

        // State management buttons
        var restoreStateButton = new NativeButton("Restore Saved State");
        restoreStateButton.setEnabled(false);
        restoreStateButton.addClickListener(e -> {
            if (savedState != null) {
                // chartController.restoreState(savedState);
            }
        });

        var saveStateButton = new NativeButton("Save Current State");
        saveStateButton.addClickListener(e -> {
            // savedState = chartController.getState();
            // if (savedState != null) {
            //     restoreStateButton.setEnabled(true);
            // }
        });

        var buttonBar = new HorizontalLayout(saveStateButton,
                restoreStateButton);

        var chartSection = new VerticalLayout(chart, buttonBar);
        chartSection.setWidth("50%");
        chartSection.setPadding(false);
        chartSection.setFlexGrow(1, chart);
        chartSection.setFlexGrow(0, buttonBar);

        add(chatSection, chartSection);
    }
}
