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
package com.vaadin.flow.component.ai.orchestrator;

/**
 * Mixin interface for {@link AIController} implementations that contribute a
 * system prompt to the {@link AIOrchestrator}.
 * <p>
 * When a controller implementing this interface is attached to an orchestrator
 * via {@link AIOrchestrator.Builder#withController(AIController)}, the prompt
 * returned by {@link #getSystemPrompt()} is automatically appended to the
 * orchestrator's own system prompt (set via
 * {@link AIOrchestrator#builder(com.vaadin.flow.component.ai.provider.LLMProvider, String)})
 * each time an LLM request is constructed.
 * </p>
 * <p>
 * This lets controllers ship with the tool-calling workflow instructions the
 * LLM needs to use their tools effectively, while still allowing the
 * application to supply an additional, higher-level system prompt:
 * </p>
 *
 * <pre>
 * var chartController = new ChartAIController(chart, databaseProvider);
 * AIOrchestrator orchestrator = AIOrchestrator
 *         .builder(llmProvider, "You are a data analyst assistant.")
 *         .withController(chartController) // chart prompt is auto-appended
 *         .withMessageList(messageList).build();
 * </pre>
 * <p>
 * Composition rules:
 * </p>
 * <ul>
 * <li>If both the orchestrator and the controller provide a non-blank prompt,
 * they are joined with a blank line separator, orchestrator prompt first.</li>
 * <li>If only one side provides a non-blank prompt, it is used as-is.</li>
 * <li>If the controller returns {@code null} or a blank string, it is
 * ignored.</li>
 * <li>{@link #getSystemPrompt()} is invoked on every LLM request, so
 * implementations may return a dynamic value.</li>
 * </ul>
 *
 * @author Vaadin Ltd
 */
public interface HasSystemPrompt {

    /**
     * Returns the system prompt contributed by this controller. The
     * orchestrator appends this to its own system prompt before sending each
     * LLM request.
     * <p>
     * Return {@code null} or a blank string to contribute nothing.
     * </p>
     *
     * @return the system prompt, or {@code null} if this controller does not
     *         contribute one
     */
    String getSystemPrompt();
}
