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
     * and so on. Since tools may execute on a background thread, this is the
     * moment to capture any state that depends on Vaadin thread locals such as
     * {@code UI.getCurrent()} or {@code VaadinSession.getCurrent()}.
     * <p>
     * The default does nothing. Throwing from this method aborts the turn
     * before the commit step: the conversation history is unchanged, the
     * request listener is not notified, the LLM stream is not opened, the
     * assistant placeholder is updated to a generic error message,
     * {@link #onResponse(ResponseListener.ResponseEvent)} fires with the thrown
     * exception so per-turn state captured before the throw can still be
     * released, and the exception propagates back to the caller of the prompt
     * entry point.
     * </p>
     */
    default void onRequest() {
    }

    /**
     * Called when the turn ends: normally when the LLM stream has completed,
     * successfully or with an error, but also when the turn fails before a
     * stream ever opens. The call runs through {@code ui.access()}, so the
     * session lock is held and Vaadin thread locals are bound — though not
     * necessarily on a request thread.
     * <p>
     * Fires at most once per prompt. A prompt rejected by the
     * {@link RequestInterceptor} ends without firing it, as does a postponed
     * prompt abandoned because its UI was detached. A turn whose UI is detached
     * when it ends also skips the hook, which requires {@code ui.access()}.
     * </p>
     * <p>
     * On success {@link ResponseListener.ResponseEvent#getError()} is empty;
     * use the call to commit staged state or run deferred UI updates. On
     * failure it carries the cause (stream error, timeout, or any throw on the
     * prompt path before the stream opens); release per-turn state captured in
     * {@code onRequest} (locks, pending writes, snapshots) and discard the
     * staged work. Note that a failure before {@link #onRequest()} — for
     * example a throwing {@link RequestInterceptor} — also fires this method,
     * so it can run without a preceding {@code onRequest} call.
     * </p>
     * <p>
     * An error is not the only abnormal ending. A turn cut off at the model's
     * output limit ends with no error at all, and
     * {@link ResponseListener.ResponseEvent#getMetadata()} carries the finish
     * reason that tells the two apart — committing staged state on such a turn
     * applies work the model never finished describing. The finish reason is
     * the underlying framework's own word, so a controller that acts on it
     * decides which values matter to it; see
     * {@link com.vaadin.flow.component.ai.provider.ResponseMetadata}.
     * </p>
     * <p>
     * The default does nothing. Exceptions thrown from the hook are caught and
     * logged; Errors propagate.
     * </p>
     *
     * @param event
     *            the outcome of the turn — response text, error, and provider
     *            metadata — never {@code null}
     */
    default void onResponse(ResponseListener.ResponseEvent event) {
    }
}
