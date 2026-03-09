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

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.common.ChatMessage;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.router.Route;

import reactor.core.publisher.Flux;

/**
 * Test page for AIOrchestrator chat history features.
 */
@Route("vaadin-ai/orchestrator-history")
public class AIOrchestratorHistoryPage extends Div {

    private AIOrchestrator orchestrator;
    private final Div chatContainer;
    private final Span historyInfo;

    public AIOrchestratorHistoryPage() {
        chatContainer = new Div();

        historyInfo = new Span();
        historyInfo.setId("history-info");

        buildOrchestrator(null);

        var getHistoryButton = new NativeButton("Get History", e -> {
            var history = orchestrator.getHistory();
            var sb = new StringBuilder();
            sb.append("size=").append(history.size());
            for (var msg : history) {
                sb.append("|").append(msg.role()).append(":")
                        .append(msg.content());
            }
            historyInfo.setText(sb.toString());
        });
        getHistoryButton.setId("get-history");

        var restoreHistoryButton = new NativeButton("Restore History", e -> {
            var history = List.of(
                    new ChatMessage(ChatMessage.Role.USER, "Previous question",
                            "msg-1", Instant.now()),
                    new ChatMessage(ChatMessage.Role.ASSISTANT,
                            "Previous answer", null, Instant.now()));
            buildOrchestrator(history);
        });
        restoreHistoryButton.setId("restore-history");

        add(getHistoryButton, restoreHistoryButton, chatContainer, historyInfo);
    }

    private void buildOrchestrator(List<ChatMessage> history) {
        chatContainer.removeAll();

        var messageList = new MessageList();
        messageList.setSizeFull();

        var messageInput = new MessageInput();

        var builder = AIOrchestrator.builder(new EchoLLMProvider(), null)
                .withMessageList(messageList).withInput(messageInput);
        if (history != null) {
            builder.withHistory(history, Collections.emptyMap());
        }
        orchestrator = builder.build();

        chatContainer.add(messageList, messageInput);
    }

    private static class EchoLLMProvider implements LLMProvider {
        @Override
        public Flux<String> stream(LLMRequest request) {
            var response = "Echo: " + request.userMessage();
            return Flux.fromArray(response.split(" ")).map(word -> word + " ");
        }

        @Override
        public void setHistory(List<ChatMessage> history,
                Map<String, List<AIAttachment>> attachmentsByMessageId) {
            // No-op
        }
    }
}
