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
import java.util.function.Function;

import org.springframework.ai.content.Media;
import org.springframework.ai.tool.ToolCallback;

/**
 * A Spring AI-flavored view of an {@link LLMProvider.LLMRequest}, used by the
 * low-level streaming function passed to
 * {@link SpringAILLMProvider#SpringAILLMProvider(Function)}.
 * <p>
 * All inputs are already converted to Spring AI types, so a custom streaming
 * function can talk to a Spring AI {@code ChatClient} or {@code ChatModel}
 * directly without repeating the conversions the provider performs. For
 * example, file attachments are exposed as Spring AI {@link Media} instead of
 * as {@link com.vaadin.flow.component.ai.common.AIAttachment}, and
 * framework-agnostic tools are exposed as {@link ToolCallback} instances.
 *
 * @author Vaadin Ltd
 */
public interface SpringAILLMRequest {

    /**
     * Gets the user's message text.
     *
     * @return the user message, never {@code null}
     */
    String userMessage();

    /**
     * Gets the system prompt for this request.
     *
     * @return the system prompt, or {@code null} if not specified
     */
    String systemPrompt();

    /**
     * Gets the attachments for this request, converted to Spring AI
     * {@link Media}.
     *
     * @return the media list, never {@code null} but may be empty
     */
    List<Media> media();

    /**
     * Gets the vendor tool objects for this request. These are classes carrying
     * Spring AI {@code @Tool} annotations, passed through unchanged for the
     * {@code ChatClient} to introspect.
     *
     * @return the tool objects, never {@code null} but may be empty
     */
    Object[] tools();

    /**
     * Gets the tool callbacks for this request, converted from
     * framework-agnostic {@link LLMProvider.ToolSpec} definitions.
     *
     * @return the tool callbacks, never {@code null} but may be empty
     */
    List<ToolCallback> toolCallbacks();
}
