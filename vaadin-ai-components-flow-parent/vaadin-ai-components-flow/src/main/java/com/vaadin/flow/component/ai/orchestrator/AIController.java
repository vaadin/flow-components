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
 * providing tools that the LLM can use.
 * <p>
 * Controllers provide domain-specific tools and functionality to the AI
 * orchestrator. Tools are functions that the AI can call to perform actions
 * like querying databases, creating visualizations, filling forms, etc.
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
     * Returns the tools this controller provides to the LLM.
     * <p>
     * Tools are functions that the AI can call to perform actions. Each tool
     * should have a clear name, description, and parameter schema.
     * </p>
     *
     * @return list of tools, or empty list if controller provides no tools
     */
    List<LLMProvider.ToolSpec> getTools();

    /**
     * Called by the orchestrator when a user prompt has been received but
     * before the request is dispatched to the LLM.
     * <p>
     * The orchestrator invokes this method on the UI thread under the session
     * lock, regardless of the trigger (input component submit, programmatic
     * {@link AIOrchestrator#prompt(String) prompt(...)}, file submit, or a
     * paste handler). Controllers can use it to prepare for the upcoming turn,
     * for example by locking UI surfaces the LLM is about to write to.
     * </p>
     * <p>
     * The default implementation does nothing. Any exception thrown is caught
     * by the orchestrator and reported to the user as a generic error message.
     * </p>
     */
    default void onRequestStart() {
    }

    /**
     * Called by the orchestrator when an LLM request cycle has completed.
     * <p>
     * This method is invoked after all tool executions for a given user request
     * have finished and the LLM has generated its final response. Controllers
     * can use this callback to perform deferred operations, such as rendering
     * UI updates or committing state changes.
     * </p>
     * <p>
     * The orchestrator invokes this method on the UI thread under the session
     * lock, so implementations may update UI components directly. Any exception
     * thrown is caught by the orchestrator and reported to the user as a
     * generic error message.
     * </p>
     */
    void onResponseComplete();

    /**
     * Called by the orchestrator when an LLM request cycle has failed, for
     * example after an error or a timeout.
     * <p>
     * The orchestrator invokes this method on the UI thread under the session
     * lock. It does not fire for successful turns; for those, use
     * {@link #onResponseComplete()}.
     * </p>
     * <p>
     * The default implementation does nothing. Any exception thrown is caught
     * by the orchestrator and reported to the user as a generic error message.
     * </p>
     *
     * @param error
     *            the error that ended the turn, never {@code null}
     */
    default void onResponseFailed(Throwable error) {
    }
}
