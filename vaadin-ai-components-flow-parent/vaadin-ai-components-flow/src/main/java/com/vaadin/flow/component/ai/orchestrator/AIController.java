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
 * @since 25.2
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
     * conversation history and the {@link RequestListener} only after this
     * method returns successfully. Implementations can prepare for the turn —
     * locking UI surfaces, snapshotting state the tool definitions depend on,
     * and so on.
     * <p>
     * The default does nothing. Throwing from this method aborts the turn
     * before the commit step: the conversation history is unchanged, the
     * request listener is not notified, the LLM stream is not opened, the
     * assistant placeholder is updated to a generic error message,
     * {@link #onResponse(Throwable)} fires with the thrown exception so
     * per-turn state captured before the throw can still be released, and the
     * exception propagates back to the caller of the prompt entry point.
     * </p>
     */
    default void onRequest() {
    }

    /**
     * Called on the UI thread under the session lock when the LLM stream has
     * completed — either successfully or with an error. Every turn fires this
     * exactly once.
     * <p>
     * On success {@code error} is {@code null}; use the call to commit staged
     * state or run deferred UI updates. On failure {@code error} carries the
     * cause (stream error, timeout, or any throw between {@link #onRequest()}
     * and the start of the stream); release per-turn state captured in
     * {@code onRequest} (locks, pending writes, snapshots) and discard the
     * staged work.
     * </p>
     * <p>
     * The default does nothing. Exceptions thrown from the hook are caught and
     * logged; Errors propagate.
     * </p>
     *
     * @param error
     *            the cause of failure, or {@code null} on success
     */
    default void onResponse(Throwable error) {
    }
}
