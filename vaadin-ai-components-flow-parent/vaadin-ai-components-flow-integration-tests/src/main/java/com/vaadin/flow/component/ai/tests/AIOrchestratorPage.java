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

import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.router.Route;

import reactor.core.publisher.Flux;

/**
 * Test page for AIOrchestrator.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ai/orchestrator")
public class AIOrchestratorPage extends Div {

    private final AIOrchestrator orchestrator;

    public AIOrchestratorPage() {
        var messageList = new MessageList();
        messageList.setId("message-list");
        var messageInput = new MessageInput();
        messageInput.setId("message-input");
        orchestrator = AIOrchestrator.builder(new EchoLLMProvider())
                .withMessageList(messageList).withInput(messageInput).build();

        var promptButton = new NativeButton("Send Hello",
                e -> orchestrator.prompt("Hello from button"));
        promptButton.setId("prompt-button");

        add(messageList, messageInput, promptButton);
    }

    /**
     * A simple LLM provider that echoes the user message back.
     */
    private static class EchoLLMProvider implements LLMProvider {
        @Override
        public Flux<String> stream(LLMRequest request) {
            var response = "Echo: " + request.userMessage();
            return Flux.fromArray(response.split(" ")).map(word -> word + " ");
        }
    }
}
