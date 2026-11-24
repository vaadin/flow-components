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

import java.io.Serializable;

/**
 * The texts the {@link AIOrchestrator} shows in the message list on its own,
 * such as the message that replaces the assistant's answer when a turn fails.
 * Use to localize them; any text left {@code null} falls back to the built-in
 * English default.
 * <p>
 * The names shown as the authors of the messages are not part of this class.
 * Set them with {@link AIOrchestrator.Builder#withUserName(String)} and
 * {@link AIOrchestrator.Builder#withAssistantName(String)}.
 *
 * @author Vaadin Ltd
 * @since 25.5
 * @see AIOrchestrator.Builder#withI18n(AIOrchestratorI18n)
 */
public class AIOrchestratorI18n implements Serializable {

    private String errorMessage;
    private String timeoutErrorMessage;

    /**
     * Gets the message shown as the assistant's message when a turn fails for a
     * reason other than a timeout.
     *
     * @return the error message, or {@code null} when the built-in default is
     *         used
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the message shown as the assistant's message when a turn fails for a
     * reason other than a timeout. The message replaces any part of the answer
     * that was already shown. When the answer was complete but the
     * {@link AIController} could not apply it, the message is shown after the
     * answer instead.
     * <p>
     * The default is {@code "An error occurred. Please try again."}.
     *
     * @param errorMessage
     *            the error message, or {@code null} to use the built-in default
     * @return this instance, for chaining
     */
    public AIOrchestratorI18n setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    /**
     * Gets the message shown as the assistant's message when the LLM does not
     * finish its answer in time.
     *
     * @return the timeout error message, or {@code null} when the built-in
     *         default is used
     */
    public String getTimeoutErrorMessage() {
        return timeoutErrorMessage;
    }

    /**
     * Sets the message shown as the assistant's message when the LLM does not
     * finish its answer in time. The message replaces any part of the answer
     * that was already shown.
     * <p>
     * The default is {@code "Request timed out. Please try again."}.
     *
     * @param timeoutErrorMessage
     *            the timeout error message, or {@code null} to use the built-in
     *            default
     * @return this instance, for chaining
     */
    public AIOrchestratorI18n setTimeoutErrorMessage(
            String timeoutErrorMessage) {
        this.timeoutErrorMessage = timeoutErrorMessage;
        return this;
    }
}
