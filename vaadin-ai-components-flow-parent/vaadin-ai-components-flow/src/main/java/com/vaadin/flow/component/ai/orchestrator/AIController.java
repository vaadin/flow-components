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

import java.util.List;

import com.vaadin.flow.component.ai.provider.LLMProvider;

/**
 * Interface for AI controllers that extend orchestrator capabilities by
 * providing tools and an optional system prompt that the LLM can use.
 * <p>
 * Controllers provide domain-specific tools and functionality to the AI
 * orchestrator. Tools are functions that the AI can call to perform actions
 * like querying databases, creating visualizations, filling forms, etc.
 * </p>
 * <p>
 * Controllers that override {@link #getSystemPrompt()} contribute tool-calling
 * workflow instructions that are automatically appended to the orchestrator's
 * system prompt on every LLM request, so applications do not need to pass them
 * to the builder explicitly.
 * </p>
 * <p>
 * Controllers are <b>not serialized</b> with the orchestrator. After
 * deserialization, restore controllers via
 * {@link AIOrchestrator#reconnect(com.vaadin.flow.component.ai.provider.LLMProvider)
 * reconnect(provider)}{@code .withController(controller).apply()}.
 * </p>
 *
 * @author Vaadin Ltd
 */
public interface AIController {

    /**
     * Returns the system prompt contributed by this controller. The
     * orchestrator appends this to its own system prompt before sending each
     * LLM request.
     * <p>
     * If both the orchestrator and the controller provide a non-blank prompt,
     * they are joined with a blank line separator, orchestrator prompt first.
     * If only one side provides a non-blank prompt, it is used as-is.
     * </p>
     * <p>
     * This method is invoked on every LLM request, so implementations may
     * return a dynamic value. Return {@code null} or a blank string to
     * contribute nothing (the default).
     * </p>
     *
     * @return the system prompt, or {@code null} if this controller does not
     *         contribute one
     */
    default String getSystemPrompt() {
        return null;
    }

    /**
     * Returns the tools this controller provides to the LLM.
     * <p>
     * Tools are functions that the AI can call to perform actions. Each tool
     * should have a clear name, description, and parameter schema.
     * </p>
     *
     * @return list of tools, or empty list if controller provides no tools
     */
    default List<LLMProvider.ToolSpec> getTools() {
        return List.of();
    }

    /**
     * Called by the orchestrator when an LLM request cycle has completed.
     * <p>
     * This method is invoked after all tool executions for a given user request
     * have finished and the LLM has generated its final response. Controllers
     * can use this callback to perform deferred operations, such as rendering
     * UI updates or committing state changes.
     * </p>
     *
     */
    default void onRequestCompleted() {
    }
}
