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

/**
 * Signals that a turn was ended because the model requested more tool calls
 * than allowed.
 * <p>
 * A model that keeps requesting tool calls instead of answering never ends the
 * turn on its own, and each round costs another model call. Both built-in
 * providers therefore end such a turn with this exception.
 * {@link LangChain4jLlmProvider} counts the tool calls of a turn and throws
 * once the model has requested more calls to any one tool, or more tool calls
 * in total, than {@link LangChain4jLlmProvider#setMaxCallsPerTool(int)} and
 * {@link LangChain4jLlmProvider#setMaxTotalToolCalls(int)} allow. None of the
 * tool calls of the refused round are executed, with the one exception that
 * {@link LangChain4jLlmProvider#setMaxCallsPerTool(int)} describes, and the
 * model is not called again. {@link SpringAiLlmProvider} throws when Spring AI,
 * which runs the loop itself, stops it at its own limits. You receive the
 * exception as the error of the turn, through
 * {@link com.vaadin.flow.component.ai.orchestrator.ResponseListener.ResponseEvent#getError()
 * ResponseEvent.getError()} in the
 * {@link com.vaadin.flow.component.ai.orchestrator.ResponseListener} and in
 * {@link com.vaadin.flow.component.ai.orchestrator.AiController#onResponse(com.vaadin.flow.component.ai.orchestrator.ResponseListener.ResponseEvent)
 * AiController.onResponse}.
 * <p>
 * The {@link #getMessage() message} names the limit that was exceeded, and the
 * tool when the limit was a per-tool one, in the words of the framework that
 * enforced it.
 *
 * @author Vaadin Ltd
 * @since 25.4
 */
public class ToolCallLimitExceededException extends RuntimeException {

    /**
     * Creates a new exception for a tool call limit exceeded during a turn. The
     * built-in providers create it; the constructor is public so that you can
     * construct one when unit testing a
     * {@link com.vaadin.flow.component.ai.orchestrator.ResponseListener} or an
     * {@link com.vaadin.flow.component.ai.orchestrator.AiController}, or throw
     * it from a custom {@link LlmProvider} that bounds its own tool-calling
     * loop.
     *
     * @param message
     *            a description of the limit that was exceeded
     */
    public ToolCallLimitExceededException(String message) {
        super(message);
    }
}
