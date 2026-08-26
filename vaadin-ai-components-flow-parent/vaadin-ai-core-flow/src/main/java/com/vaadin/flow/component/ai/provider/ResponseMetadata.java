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
import java.util.Locale;

import org.slf4j.LoggerFactory;

/**
 * Metadata about an LLM response, published by an {@link LLMProvider} through
 * {@link LLMProvider.LLMRequest#metadataSink()} when the model reports it.
 * <p>
 * The metadata describes the whole turn: when the turn contains tool-call round
 * trips, the finish reason is the one that ended the turn and the token usage
 * covers all round trips, as far as the underlying framework reports it.
 * </p>
 *
 * @param finishReason
 *            why the model stopped, normalized across vendors, or {@code null}
 *            when the model did not report a reason
 * @param rawFinishReason
 *            the vendor's own finish reason value (e.g. {@code "max_tokens"}),
 *            or {@code null} when the model did not report a reason
 * @param tokenUsage
 *            the token usage of the turn, or {@code null} when unknown
 *
 * @author Vaadin Ltd
 * @since 25.3
 */
public record ResponseMetadata(FinishReason finishReason,
        String rawFinishReason, TokenUsage tokenUsage) implements Serializable {

    /**
     * Why the model stopped producing output, normalized across vendors.
     * Normalization is best-effort: a vendor value with no known equivalent
     * maps to {@link #OTHER}, and {@link ResponseMetadata#rawFinishReason()}
     * always carries the vendor's own value as the authoritative source.
     */
    public enum FinishReason {

        /** The model finished its answer normally. */
        STOP,

        /**
         * The response was cut off by a token limit and is incomplete.
         */
        LENGTH,

        /** The response was stopped by a content filter or guardrail. */
        CONTENT_FILTER,

        /**
         * The response ended with tool calls awaiting execution. Not normally
         * observed, since providers run tool calls within the turn.
         */
        TOOL_CALLS,

        /** A vendor-specific reason with no normalized equivalent. */
        OTHER
    }

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
     */
    public record TokenUsage(Integer inputTokens, Integer outputTokens,
            Integer totalTokens) implements Serializable {
    }

    /**
     * Maps a vendor finish reason value to the normalized {@link FinishReason}.
     *
     * @param rawFinishReason
     *            the vendor's finish reason value, may be {@code null}
     * @return the normalized reason, {@link FinishReason#OTHER} for an
     *         unrecognized value, or {@code null} when the input is
     *         {@code null} or blank
     */
    public static FinishReason normalizeFinishReason(String rawFinishReason) {
        if (rawFinishReason == null || rawFinishReason.isBlank()) {
            return null;
        }
        return switch (rawFinishReason.toLowerCase(Locale.ROOT)) {
        case "stop", "end_turn", "stop_sequence", "completed" ->
            FinishReason.STOP;
        case "length", "max_tokens", "max_output_tokens" -> FinishReason.LENGTH;
        case "content_filter", "content_filtered", "guardrail_intervened" ->
            FinishReason.CONTENT_FILTER;
        case "tool_calls", "tool_use", "tool_execution", "function_call" ->
            FinishReason.TOOL_CALLS;
        default -> {
            LoggerFactory.getLogger(ResponseMetadata.class).debug(
                    "Finish reason '{}' has no normalized equivalent, mapping to OTHER",
                    rawFinishReason);
            yield FinishReason.OTHER;
        }
        };
    }
}
