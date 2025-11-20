/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.input.AiInput;
import com.vaadin.flow.component.ai.messagelist.AiMessageList;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.upload.AiFileReceiver;

import java.io.Serializable;
import java.util.Objects;

/**
 * Base class for AI orchestrators providing common functionality.
 * <p>
 * This abstract class handles:
 * </p>
 * <ul>
 * <li>LLM provider management</li>
 * <li>Input component integration</li>
 * <li>File upload integration</li>
 * <li>UI context validation</li>
 * </ul>
 *
 * @author Vaadin Ltd
 */
public abstract class BaseAiOrchestrator implements Serializable {

    protected final LLMProvider provider;
    protected AiMessageList messageList;
    protected AiInput input;
    protected AiFileReceiver fileReceiver;

    /**
     * Creates a new base orchestrator.
     *
     * @param provider
     *            the LLM provider to use for generating responses
     */
    protected BaseAiOrchestrator(LLMProvider provider) {
        Objects.requireNonNull(provider, "Provider cannot be null");
        this.provider = provider;
    }

    /**
     * Gets the LLM provider.
     *
     * @return the provider
     */
    public LLMProvider getProvider() {
        return provider;
    }

    /**
     * Gets the message list component.
     *
     * @return the message list, or null if not configured
     */
    public AiMessageList getMessageList() {
        return messageList;
    }

    /**
     * Sets the message list component.
     *
     * @param messageList
     *            the message list
     */
    protected void setMessageList(AiMessageList messageList) {
        this.messageList = messageList;
    }

    /**
     * Gets the input component.
     *
     * @return the input component, or null if not configured
     */
    public AiInput getInput() {
        return input;
    }

    /**
     * Sets the input component.
     *
     * @param input
     *            the input component
     */
    protected void setInput(AiInput input) {
        this.input = input;
    }

    /**
     * Gets the file receiver component.
     *
     * @return the file receiver, or null if not configured
     */
    public AiFileReceiver getFileReceiver() {
        return fileReceiver;
    }

    /**
     * Sets the file receiver component.
     *
     * @param fileReceiver
     *            the file receiver
     */
    protected void setFileReceiver(AiFileReceiver fileReceiver) {
        this.fileReceiver = fileReceiver;
    }

    /**
     * Validates that a UI context exists for the current thread.
     *
     * @return the current UI
     * @throws IllegalStateException
     *             if no UI context is available
     */
    protected UI validateUiContext() {
        UI ui = UI.getCurrent();
        if (ui == null) {
            throw new IllegalStateException(
                    "No UI found. Make sure the orchestrator is used within a UI context.");
        }
        return ui;
    }

    /**
     * Base builder for orchestrators.
     *
     * @param <T>
     *            the orchestrator type being built
     * @param <B>
     *            the builder type (for method chaining)
     */
    protected abstract static class BaseBuilder<T extends BaseAiOrchestrator, B extends BaseBuilder<T, B>> {
        protected final LLMProvider provider;
        protected AiMessageList messageList;
        protected AiInput input;
        protected AiFileReceiver fileReceiver;

        protected BaseBuilder(LLMProvider provider) {
            this.provider = provider;
        }

        /**
         * Sets the message list component.
         *
         * @param messageList
         *            the message list
         * @return this builder
         */
        public B withMessageList(AiMessageList messageList) {
            this.messageList = messageList;
            return self();
        }

        /**
         * Sets the input component.
         *
         * @param input
         *            the input component
         * @return this builder
         */
        public B withInput(AiInput input) {
            this.input = input;
            return self();
        }

        /**
         * Sets the file receiver component for file uploads.
         *
         * @param fileReceiver
         *            the file receiver
         * @return this builder
         */
        public B withFileReceiver(AiFileReceiver fileReceiver) {
            this.fileReceiver = fileReceiver;
            return self();
        }

        /**
         * Returns this builder instance with the correct type.
         *
         * @return this builder
         */
        @SuppressWarnings("unchecked")
        protected B self() {
            return (B) this;
        }

        /**
         * Applies common configuration to the orchestrator being built.
         *
         * @param orchestrator
         *            the orchestrator to configure
         */
        protected void applyCommonConfiguration(T orchestrator) {
            orchestrator.setMessageList(messageList);
            orchestrator.setInput(input);
            orchestrator.setFileReceiver(fileReceiver);
        }

        /**
         * Builds the orchestrator.
         *
         * @return the configured orchestrator
         */
        public abstract T build();
    }
}
