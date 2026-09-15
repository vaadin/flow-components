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
 * turn on its own, and each round costs another model call.
 * {@link LangChain4JLLMProvider} therefore ends the turn with this exception
 * once the model has requested more calls to any one tool, or more tool calls
 * in total, than {@link LangChain4JLLMProvider#setMaxCallsPerTool(int)} and
 * {@link LangChain4JLLMProvider#setMaxTotalToolCalls(int)} allow. None of the
 * tool calls of the refused round are executed, and the model is not called
 * again. You receive the exception as the error of the turn, through
 * {@link com.vaadin.flow.component.ai.orchestrator.ResponseListener.ResponseEvent#getError()
 * ResponseEvent.getError()} in the
 * {@link com.vaadin.flow.component.ai.orchestrator.ResponseListener} and in
 * {@link com.vaadin.flow.component.ai.orchestrator.AIController#onResponse(com.vaadin.flow.component.ai.orchestrator.ResponseListener.ResponseEvent)
 * AIController.onResponse}.
 * <p>
 * {@link SpringAILLMProvider} does not throw this exception. Spring AI runs the
 * tool-calling loop itself and, from version 2.0.1, stops it at the same limits
 * without failing the turn; see {@link SpringAILLMProvider} for how such a turn
 * ends.
 *
 * @author Vaadin Ltd
 * @since 25.3
 */
public class ToolCallLimitExceededException extends RuntimeException {

    private final String toolName;
    private final int limit;

    /**
     * Creates a new exception for a tool call limit exceeded during a turn. The
     * built-in provider creates it; the constructor is public so that you can
     * construct one when unit testing a
     * {@link com.vaadin.flow.component.ai.orchestrator.ResponseListener} or an
     * {@link com.vaadin.flow.component.ai.orchestrator.AIController}, or throw
     * it from a custom {@link LLMProvider} that bounds its own tool-calling
     * loop.
     *
     * @param toolName
     *            the name of the tool whose per-tool limit was exceeded, or
     *            {@code null} when the limit on all tool calls of the turn was
     *            exceeded instead
     * @param limit
     *            the limit that was exceeded
     */
    public ToolCallLimitExceededException(String toolName, int limit) {
        super(buildMessage(toolName, limit));
        this.toolName = toolName;
        this.limit = limit;
    }

    private static String buildMessage(String toolName, int limit) {
        if (toolName != null) {
            return "Tool call limit (" + limit + ") exceeded for tool '"
                    + toolName + "'";
        }
        return "Total tool call limit (" + limit + ") exceeded for this turn";
    }

    /**
     * Gets the name of the tool whose per-tool limit was exceeded.
     *
     * @return the tool name, or {@code null} when the limit on all tool calls
     *         of the turn was exceeded instead
     */
    public String getToolName() {
        return toolName;
    }

    /**
     * Gets the limit that was exceeded: the maximum number of calls to the tool
     * named by {@link #getToolName()}, or the maximum number of tool calls in
     * the turn when that is {@code null}.
     *
     * @return the limit that was exceeded
     */
    public int getLimit() {
        return limit;
    }
}
