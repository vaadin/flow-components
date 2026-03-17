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

import java.util.Collections;
import java.util.List;

import com.vaadin.flow.component.ai.provider.LLMProvider;

/**
 * Controls AI orchestrator behavior by providing framework-agnostic tools and
 * receiving lifecycle callbacks.
 * <p>
 * Controllers encapsulate feature-specific logic (e.g., chart generation, grid
 * manipulation) and are registered on an {@link AIOrchestrator} via
 * {@link AIOrchestrator.Builder#withController(AIController)}. Multiple
 * controllers can be registered on a single orchestrator.
 * </p>
 * <p>
 * Each controller provides:
 * </p>
 * <ul>
 * <li><b>Tools</b> — via {@link #getTools()}, framework-agnostic tool
 * definitions that the LLM can invoke during a request</li>
 * <li><b>Lifecycle hooks</b> — via {@link #onRequestCompleted(String)}, called
 * after each successful LLM response</li>
 * </ul>
 * <p>
 * Controllers are <b>not serialized</b> with the orchestrator. After
 * deserialization, restore controllers via
 * {@link AIOrchestrator#reconnect(com.vaadin.flow.component.ai.provider.LLMProvider)
 * reconnect(provider)}{@code .withControllers(controller).apply()}.
 * </p>
 *
 * @author Vaadin Ltd
 */
public interface AIController {

    /**
     * Returns the framework-agnostic tools provided by this controller.
     * <p>
     * These tools are collected by the orchestrator before each LLM request and
     * included alongside any vendor-specific annotated tools. The tools are
     * called each time a request is made, allowing dynamic tool sets.
     * </p>
     *
     * @return a list of tool definitions, never {@code null} but may be empty
     */
    default List<LLMProvider.ToolDefinition> getTools() {
        return Collections.emptyList();
    }

    /**
     * Called after each successful LLM response has been fully streamed and
     * added to the conversation history.
     * <p>
     * This hook can be used for post-processing, state updates, or triggering
     * follow-up actions. It is called from a background thread (Reactor
     * scheduler). To update Vaadin UI components from this method, use
     * {@code ui.access()}.
     * </p>
     * <p>
     * This method is not called when the LLM response fails, times out, or
     * produces an empty response.
     * </p>
     * <p>
     * Exceptions thrown from this method are logged but do not affect other
     * controllers or the orchestrator's operation.
     * </p>
     *
     * @param responseText
     *            the full text of the assistant's response, never {@code null}
     */
    default void onRequestCompleted(String responseText) {
    }
}
