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
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.LangChain4JLLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;

import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

/**
 * Test page for trying out ChartAIController with a real AI chat.
 */
@Route("vaadin-ai/chart-ai-chat")
public class ChartAIChatPage extends HorizontalLayout {

    public ChartAIChatPage() {
        UI.getCurrent().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);
        setSizeFull();

        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            add(new Div("Set the OPENAI_API_KEY environment variable."));
            return;
        }

        var chatModel = OpenAiStreamingChatModel.builder().apiKey(apiKey)
                .modelName("gpt-5.4-mini").build();

        var chart = new Chart();
        chart.setSizeFull();

        var chartController = new ChartAIController(chart,
                new InMemoryDatabaseProvider());

        var messageList = new MessageList();
        messageList.setMarkdown(true);
        messageList.setWidthFull();

        var messageInput = new MessageInput();
        messageInput.setWidthFull();

        var llmProvider = new LangChain4JLLMProvider(chatModel);

        AIOrchestrator.builder(llmProvider, ChartAIController.getSystemPrompt())
                .withMessageList(messageList).withInput(messageInput)
                .withController(chartController).build();

        var chatLayout = new VerticalLayout(messageList, messageInput);
        chatLayout.expand(messageList);

        var chartWrapper = new Div(chart);
        chartWrapper.setSizeFull();

        expand(chartWrapper, chatLayout);
        add(chartWrapper, chatLayout);
    }
}
