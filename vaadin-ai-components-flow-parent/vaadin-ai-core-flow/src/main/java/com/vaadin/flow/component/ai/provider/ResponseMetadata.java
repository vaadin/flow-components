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
 * <p>
 * <b>Finish reason vocabulary.</b> Every model vendor words the reason
 * differently and keeps adding values: a turn cut off at the output limit is
 * {@code "length"} on OpenAI and {@code "max_tokens"} on Anthropic. Nothing
 * here maps those onto a fixed set of Vaadin values, because such a set goes
 * stale the moment a vendor adds one. Compare only against the values
 * documented for the model you call, and treat an unrecognized value as unknown
 * rather than as an error.
 * </p>
 * <p>
 * {@link LangChain4JLLMProvider} is a step further removed, and shows why the
 * mapping is left alone: LangChain4j collapses the vendor's word onto its own
 * five-constant {@code FinishReason} enum before Vaadin sees the response, so
 * the value is that constant's name and both examples above arrive as
 * {@code "LENGTH"}. The collapsing is per integration and loses whatever it
 * does not recognize — an unrecognized value reaches this record as
 * {@code null} through its OpenAI integration and as {@code "OTHER"} through
 * its Anthropic one. Use {@link SpringAILLMProvider} where the application
 * needs the model's own word.
 * </p>
 *
 * @param finishReason
 *            why the model stopped, worded as the underlying framework reports
 *            it — the vocabulary depends on both the model and the provider,
 *            see above — or {@code null} when no reason was reported
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
