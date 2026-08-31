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

/**
 * Metadata about an LLM response, published by an {@link LLMProvider} through
 * {@link LLMProvider.LLMRequest#metadataSink()} when the model reports it.
 * <p>
 * The metadata describes the turn as far as the provider has observed it: when
 * the turn contains tool-call round trips, the finish reason is the latest one
 * observed and the token usage covers the round trips so far, as far as the
 * underlying framework reports them. On a turn that completes normally the last
 * published instance therefore describes the whole turn.
 * </p>
 *
 * @param finishReason
 *            why the model stopped, as reported by the underlying framework, or
 *            {@code null} when no reason was reported. The vocabulary and the
 *            casing depend on the provider, not only on the model:
 *            {@link SpringAILLMProvider} relays the model's own word, so an
 *            OpenAI turn cut off at the output limit arrives as
 *            {@code "max_tokens"}, while {@link LangChain4JLLMProvider} reports
 *            the name of the LangChain4j {@code FinishReason} constant the
 *            value was mapped to before Vaadin saw it, so the same turn arrives
 *            as {@code "LENGTH"}. Code that compares against particular values
 *            has to account for the provider it runs on.
 * @param tokenUsage
 *            the token usage of the turn, or {@code null} when unknown
 *
 * @author Vaadin Ltd
 * @since 25.3
 */
public record ResponseMetadata(String finishReason,
        TokenUsage tokenUsage) implements Serializable {

    /**
     * Token usage of a turn. Counts cover every round trip of the turn, as far
     * as the underlying framework reports them.
     *
     * @param inputTokens
     *            tokens in the input, or {@code null} when unknown
     * @param outputTokens
     *            tokens in the generated output, or {@code null} when unknown
     * @param totalTokens
     *            total tokens, or {@code null} when unknown
     * @since 25.3
     */
    public record TokenUsage(Integer inputTokens, Integer outputTokens,
            Integer totalTokens) implements Serializable {
    }
}
