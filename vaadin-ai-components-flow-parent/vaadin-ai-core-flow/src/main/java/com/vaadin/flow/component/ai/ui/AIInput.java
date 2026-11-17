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
package com.vaadin.flow.component.ai.ui;

import java.io.Serializable;

import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.function.SerializableConsumer;

/**
 * Interface for input components that are used in an AI conversation.
 *
 * @author Vaadin Ltd
 * @since 25.3
 */
public interface AIInput extends Serializable {

    /**
     * Adds a listener for submit events.
     * <p>
     * The listener is notified with the submitted value when the user submits
     * input.
     *
     * @param listener
     *            the listener to add, not {@code null}
     */
    void addSubmitListener(SerializableConsumer<String> listener);

    /**
     * Sets whether submitting input is disabled. While disabled, the user can
     * still type, but cannot submit, and the typed text is kept.
     * <p>
     * The {@link AIOrchestrator} disables submitting while it processes a
     * request, and while a file is being uploaded to a file receiver that is an
     * {@code Upload} or an {@code UploadManager}. During an upload, the browser
     * shows this only when server push or polling is enabled. The orchestrator
     * only enables submitting again if it disabled it, see
     * {@link #isSubmitDisabled()}. The default implementation does nothing.
     * <p>
     * Disabling submitting while the orchestrator has it disabled, for example
     * in a {@link AIOrchestrator.Builder#withResponseListener response
     * listener}, does not last: the orchestrator enables submitting when the
     * request and the uploads have ended. To keep the user from sending more
     * messages, disable the input component itself, for example with
     * {@code setEnabled(false)} on a {@code MessageInput}, or reject the
     * prompts in a {@link AIOrchestrator.Builder#withRequestInterceptor request
     * interceptor}.
     *
     * @param submitDisabled
     *            {@code true} to disable submitting, {@code false} to enable it
     * @since 25.4
     */
    default void setSubmitDisabled(boolean submitDisabled) {
        // Submitting is not disabled unless the input implements this
    }

    /**
     * Gets whether submitting input is disabled.
     * <p>
     * The {@link AIOrchestrator} leaves submitting disabled when it is already
     * disabled, for example by the application, at the time the orchestrator
     * would disable it. The default implementation returns {@code false}.
     *
     * @return {@code true} if submitting is disabled, {@code false} otherwise
     * @since 25.4
     */
    default boolean isSubmitDisabled() {
        return false;
    }
}
