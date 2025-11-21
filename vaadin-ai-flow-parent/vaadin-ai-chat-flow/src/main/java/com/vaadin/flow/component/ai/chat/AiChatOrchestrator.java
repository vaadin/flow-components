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
package com.vaadin.flow.component.ai.chat;

import com.vaadin.flow.component.ai.messagelist.AiMessageList;
import com.vaadin.flow.component.ai.orchestrator.BaseAiOrchestrator;
import com.vaadin.flow.component.ai.provider.LLMProvider;

/**
 * Orchestrator for AI-powered chat interfaces.
 * <p>
 * This class connects an {@link AiMessageList}, {@link AiInput}, and an
 * {@link LLMProvider} to create an interactive AI chat experience. It handles:
 * </p>
 * <ul>
 * <li>Streaming responses from the LLM to the message list</li>
 * <li>Automatic UI updates via server push</li>
 * <li>Optional file upload support</li>
 * </ul>
 * <p>
 * Conversation history is managed internally by the {@link LLMProvider}
 * instance. Each orchestrator maintains its own conversation context through
 * its provider instance.
 * </p>
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * MessageList messageList = new MessageList();
 * MessageInput messageInput = new MessageInput();
 *
 * LLMProvider provider = new LangChain4jProvider(model);
 * provider.setSystemPrompt("You are a helpful assistant.");
 *
 * AiChatOrchestrator orchestrator = AiChatOrchestrator.create(provider)
 *         .withMessageList(messageList)
 *         .withInput(messageInput)
 *         .build();
 * </pre>
 *
 * @author Vaadin Ltd
 */
public class AiChatOrchestrator extends BaseAiOrchestrator {

    /**
     * Creates a new AI chat orchestrator.
     *
     * @param provider
     *            the LLM provider to use for generating responses
     */
    private AiChatOrchestrator(LLMProvider provider) {
        super(provider);
    }

    /**
     * Creates a new builder for AiChatOrchestrator.
     *
     * @param provider
     *            the LLM provider
     * @return a new builder
     */
    public static Builder create(LLMProvider provider) {
        return new Builder(provider);
    }

    /**
     * Builder for AiChatOrchestrator.
     */
    public static class Builder extends BaseBuilder<AiChatOrchestrator, Builder> {

        private Builder(LLMProvider provider) {
            super(provider);
        }

        /**
         * Builds the orchestrator.
         *
         * @return the configured orchestrator
         */
        @Override
        public AiChatOrchestrator build() {
            AiChatOrchestrator orchestrator = new AiChatOrchestrator(provider);

            // Apply common configuration from base builder (includes input
            // listener and file receiver configuration)
            applyCommonConfiguration(orchestrator);

            return orchestrator;
        }
    }
}
