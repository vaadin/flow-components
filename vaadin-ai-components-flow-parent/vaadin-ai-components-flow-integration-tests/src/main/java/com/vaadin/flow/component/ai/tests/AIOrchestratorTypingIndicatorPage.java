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

import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.common.ChatMessage;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;

import reactor.core.publisher.Flux;

/**
 * Test page for the typing indicator that the AIOrchestrator shows while a
 * response is being produced. The provider delays its response so that the
 * indicator can be observed.
 */
@Route("vaadin-ai/orchestrator-typing-indicator")
public class AIOrchestratorTypingIndicatorPage extends VerticalLayout {

    public AIOrchestratorTypingIndicatorPage() {
        setHeightFull();
        // The delayed provider responds from a background thread, so the
        // response needs push to reach the browser
        UI.getCurrent().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

        var messageList = new MessageList();
        messageList.setId("message-list");
        messageList.setSizeFull();

        var messageInput = new MessageInput();
        messageInput.setWidthFull();
        messageInput.setId("message-input");

        AIOrchestrator.builder(new DelayedEchoLLMProvider(), null)
                .withMessageList(messageList).withInput(messageInput).build();

        add(messageList, messageInput);
    }

    /**
     * An LLM provider that echoes the user message back after a delay.
     */
    private static class DelayedEchoLLMProvider implements LLMProvider {
        @Override
        public Flux<String> stream(LLMRequest request) {
            return Flux.just("Echo: " + request.userMessage())
                    .delaySubscription(Duration.ofSeconds(2));
        }

        @Override
        public void setHistory(List<ChatMessage> history,
                Map<String, List<AIAttachment>> attachmentsByMessageId) {
            // No-op for testing
        }
    }
}
