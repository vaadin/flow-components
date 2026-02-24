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
package com.vaadin.flow.component.ai.provider;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.common.ChatMessage;

import reactor.core.publisher.Flux;

/**
 * Framework-agnostic interface for Large Language Model (LLM) providers. This
 * interface enables AI-powered components to communicate with LLMs without
 * being tied to a specific implementation. Implementations are responsible for
 * managing conversation memory, handling streaming responses, processing
 * vendor-specific tool annotations, and handling file attachments.
 * <p>
 * Use the {@code from(...)} factory methods to create a provider from a
 * vendor-specific model or client object:
 *
 * <pre>
 * // Spring AI
 * LLMProvider provider = LLMProvider.from(chatModel);
 *
 * // LangChain4j
 * LLMProvider provider = LLMProvider.from(streamingChatModel);
 * </pre>
 *
 * @author Vaadin Ltd.
 */
public interface LLMProvider {

    /**
     * Creates an {@link LLMProvider} from a Spring AI
     * {@link org.springframework.ai.chat.model.ChatModel ChatModel}.
     * <p>
     * The provider manages conversation memory internally. Streaming is enabled
     * by default and can be toggled via
     * {@link SpringAILLMProvider#setStreaming(boolean)}.
     *
     * @param chatModel
     *            the Spring AI chat model, not {@code null}
     * @return a new provider instance, never {@code null}
     * @throws NullPointerException
     *             if chatModel is {@code null}
     */
    static SpringAILLMProvider from(
            org.springframework.ai.chat.model.ChatModel chatModel) {
        return new SpringAILLMProvider(chatModel);
    }

    /**
     * Creates an {@link LLMProvider} from a Spring AI
     * {@link org.springframework.ai.chat.client.ChatClient ChatClient}.
     * <p>
     * Use this when the {@code ChatClient} is pre-configured with custom
     * advisors or externally managed memory. Note that providers created from a
     * {@code ChatClient} do <b>not</b> support history restoration via
     * {@link #setHistory(List, Map)} because the memory is managed externally.
     *
     * @param chatClient
     *            the Spring AI chat client, not {@code null}
     * @return a new provider instance, never {@code null}
     * @throws NullPointerException
     *             if chatClient is {@code null}
     */
    static SpringAILLMProvider from(
            org.springframework.ai.chat.client.ChatClient chatClient) {
        return new SpringAILLMProvider(chatClient);
    }

    /**
     * Creates an {@link LLMProvider} from a LangChain4j
     * {@link dev.langchain4j.model.chat.StreamingChatModel StreamingChatModel}.
     * <p>
     * The provider manages conversation memory internally. Responses are
     * streamed token-by-token.
     *
     * @param streamingChatModel
     *            the LangChain4j streaming chat model, not {@code null}
     * @return a new provider instance, never {@code null}
     * @throws NullPointerException
     *             if streamingChatModel is {@code null}
     */
    static LangChain4JLLMProvider from(
            dev.langchain4j.model.chat.StreamingChatModel streamingChatModel) {
        return new LangChain4JLLMProvider(streamingChatModel);
    }

    /**
     * Creates an {@link LLMProvider} from a LangChain4j
     * {@link dev.langchain4j.model.chat.ChatModel ChatModel}.
     * <p>
     * The provider manages conversation memory internally. Responses are
     * returned as a single block (non-streaming).
     *
     * @param chatModel
     *            the LangChain4j chat model, not {@code null}
     * @return a new provider instance, never {@code null}
     * @throws NullPointerException
     *             if chatModel is {@code null}
     */
    static LangChain4JLLMProvider from(
            dev.langchain4j.model.chat.ChatModel chatModel) {
        return new LangChain4JLLMProvider(chatModel);
    }

    /**
     * Streams a response from the LLM based on the provided request. This
     * method returns a reactive stream that emits response tokens as they
     * become available from the LLM. The provider manages conversation history
     * internally, so each call to this method adds to the ongoing conversation
     * context.
     *
     * @param request
     *            the LLM request containing user message, system prompt,
     *            attachments, and tools, not {@code null}
     * @return a Flux stream that emits response tokens as strings, never
     *         {@code null}
     * @throws NullPointerException
     *             if request is {@code null}
     */
    Flux<String> stream(LLMRequest request);

    /**
     * Restores the provider's conversation memory from a list of chat messages
     * with their associated attachments. Any existing memory is cleared before
     * the new history is applied.
     * <p>
     * Providers that support setting chat history should override this method.
     * <p>
     * This method must not be called while a streaming response is in progress.
     *
     * @param history
     *            the list of chat messages to restore, not {@code null}
     * @param attachmentsByMessageId
     *            a map from {@link ChatMessage#messageId()} to the list of
     *            attachments for that message, not {@code null}
     * @throws NullPointerException
     *             if any argument is {@code null}
     * @throws UnsupportedOperationException
     *             if this provider does not support chat history restoration
     */
    default void setHistory(List<ChatMessage> history,
            Map<String, List<AIAttachment>> attachmentsByMessageId) {
        throw new UnsupportedOperationException(
                "This LLM provider does not support chat history restoration.");
    }

    /**
     * Represents a request to the LLM containing all necessary context,
     * configuration, and tools. Requests are immutable.
     */
    interface LLMRequest extends Serializable {
        /**
         * Gets the user's message.
         *
         * @return the user message, never {@code null}
         */
        String userMessage();

        /**
         * Gets the list of file attachments to include with the request.
         * Attachments can be images, PDFs, text files, or other supported
         * formats that the LLM can analyze and reference in its response.
         *
         * @return the list of attachments, never {@code null} but may be empty
         */
        List<AIAttachment> attachments();

        /**
         * Gets the system prompt for this specific request. The system prompt
         * defines the LLM's behavior, role, and constraints. If {@code null},
         * the provider may use its own internal default system prompt.
         *
         * @return the system prompt, or {@code null} if not specified
         */
        String systemPrompt();

        /**
         * Gets the tool objects for this request. Tool objects are classes with
         * vendor-specific annotations (e.g., LangChain4j's {@code @Tool},
         * Spring AI's {@code @Tool}) that the provider can introspect and
         * convert to native tool definitions.
         *
         * @return array of tool objects, never {@code null} but may be empty
         */
        Object[] tools();
    }
}
