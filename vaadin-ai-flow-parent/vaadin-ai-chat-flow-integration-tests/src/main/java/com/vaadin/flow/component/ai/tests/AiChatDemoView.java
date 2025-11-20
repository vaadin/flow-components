/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import com.vaadin.flow.component.ai.chat.AiChatOrchestrator;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.provider.langchain4j.LangChain4JLLMProvider;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

/**
 * Demo view for AI Chat functionality.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ai/ai-chat-demo")
public class AiChatDemoView extends VerticalLayout {

    public AiChatDemoView() {
        // Enable push for streaming responses
        getUI().ifPresent(ui -> ui.getPushConfiguration()
                .setPushMode(PushMode.AUTOMATIC));

        setSpacing(true);
        setPadding(true);
        setHeightFull();

        H2 title = new H2("AI Chat Demo");
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

        // Create UI components
        MessageList messageList = new MessageList();
        messageList.setWidthFull();
        messageList.getStyle().set("flex-grow", "1");

        MessageInput messageInput = new MessageInput();
        messageInput.setWidthFull();

        // Create LLM provider
        StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
                .apiKey(apiKey).modelName("gpt-4").build();
        LLMProvider provider = new LangChain4JLLMProvider(model);

        // Create and configure orchestrator using builder pattern
        AiChatOrchestrator.create(provider)
                .withMessageList(messageList)
                .withInput(messageInput)
                .build();

        // Layout
        Div chatContainer = new Div(messageList);
        chatContainer.setWidthFull();
        chatContainer.getStyle().set("flex-grow", "1").set("overflow", "auto");

        Div inputContainer = new Div(messageInput);
        inputContainer.setWidthFull();

        add(chatContainer, inputContainer);
        setFlexGrow(1, chatContainer);
    }
}
