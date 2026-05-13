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
 * Contributes tools and lifecycle hooks to an {@link AIOrchestrator} —
 * domain-specific behaviour like populating a grid, building a chart, or
 * filling a form from natural-language requests.
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
     * Returns the tools this controller exposes to the LLM.
     *
     * @return list of tools, or empty list if controller provides no tools
     */
    List<LLMProvider.ToolSpec> getTools();

    /**
     * Called synchronously on the UI thread just before the LLM stream opens.
     * By the time this method fires, the user message and an empty assistant
     * placeholder are already in the message list; the turn is committed to the
     * conversation history and the attachment-submit listener only after this
     * method returns successfully. Implementations can prepare for the turn —
     * locking UI surfaces, snapshotting state the tool definitions depend on,
     * and so on.
     * <p>
     * The default does nothing. Throwing from this method aborts the turn
     * before the commit step: the conversation history is unchanged, the
     * attachment-submit listener is not notified, the LLM stream is not opened,
     * the assistant placeholder is updated to a generic error message,
     * {@link #onResponseFailed(Throwable)} fires with the thrown exception so
     * per-turn state captured before the throw can still be released, and the
     * exception propagates back to the caller of the prompt entry point.
     * </p>
     */
    default void onRequestStart() {
    }

    /**
     * Called on the UI thread under the session lock when the LLM stream
     * completes successfully — after all tool calls for the turn have run and
     * the LLM has produced its final response. Use it for deferred UI updates
     * or to commit staged state.
     * <p>
     * Mutually exclusive with {@link #onResponseFailed(Throwable)}: every turn
     * fires exactly one of the two. Exceptions thrown from the hook are caught
     * and the user sees a generic error message; Errors propagate.
     * </p>
     */
    void onResponseComplete();

    /**
     * Called on the UI thread under the session lock when an LLM turn fails —
     * stream error, timeout, or any throw between {@link #onRequestStart()} and
     * the start of the stream. Implementations should release per-turn state
     * captured in {@code onRequestStart} (locks, pending writes, snapshots).
     * <p>
     * Mutually exclusive with {@link #onResponseComplete()}: every turn fires
     * exactly one of the two. The default does nothing. Exceptions thrown from
     * the hook are caught and logged; Errors propagate.
     * </p>
     *
     * @param error
     *            the cause of the failure (Exception or Error), never
     *            {@code null}
     */
    default void onResponseFailed(Throwable error) {
    }
}
