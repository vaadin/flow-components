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

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.service.tool.ToolExecutor;

/**
 * A LangChain4j-flavored view of an {@link LLMProvider.LLMRequest}, used by the
 * low-level streaming function passed to
 * {@link LangChain4JLLMProvider#LangChain4JLLMProvider(Function)}.
 * <p>
 * All inputs are already converted to LangChain4j types, so a custom streaming
 * function can talk to a LangChain4j model directly without repeating the
 * conversions the provider performs. For example, file attachments are exposed
 * as LangChain4j {@link dev.langchain4j.data.message.Content} inside
 * {@link #userMessage()} instead of as
 * {@link com.vaadin.flow.component.ai.common.AIAttachment}, and tools are
 * exposed as {@link ToolSpecification} and {@link ToolExecutor} instances.
 *
 * @author Vaadin Ltd
 */
public interface LangChain4JLLMRequest {

    /**
     * Gets the system prompt for this request.
     *
     * @return the system prompt, or {@code null} if not specified
     */
    String systemPrompt();

    /**
     * Gets the user message for this request, including any attachments
     * converted to LangChain4j {@link dev.langchain4j.data.message.Content}.
     *
     * @return the user message, never {@code null}
     */
    UserMessage userMessage();

    /**
     * Gets the full message list to send to the model: the system message (if a
     * non-blank {@link #systemPrompt()} is set), the prior conversation
     * history, and the current {@link #userMessage()} as the last entry.
     *
     * @return the message list to send, never {@code null}
     */
    List<ChatMessage> messages();

    /**
     * Gets the tool specifications for this request, converted from both
     * LangChain4j {@code @Tool} annotated objects and framework-agnostic
     * {@link LLMProvider.ToolSpec} definitions.
     *
     * @return the tool specifications, never {@code null} but may be empty
     */
    List<ToolSpecification> toolSpecifications();

    /**
     * Gets the tool executors for this request, keyed by tool name. Use these
     * to run the tool calls requested by the model.
     *
     * @return the tool executors, never {@code null} but may be empty
     */
    Map<String, ToolExecutor> toolExecutors();
}
