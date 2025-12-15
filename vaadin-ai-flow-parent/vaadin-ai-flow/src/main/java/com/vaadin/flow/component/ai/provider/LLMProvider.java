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
package com.vaadin.flow.component.ai.provider;

import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.List;

/**
 * Framework-agnostic interface for Large Language Model providers.
 * <p>
 * This interface can be used by any AI-powered component to communicate with
 * LLMs, not just chat components. Implementations handle conversation memory
 * internally, managing conversation context per provider instance.
 * </p>
 *
 * @author Vaadin Ltd
 */
public interface LLMProvider extends Serializable {

    /**
     * Streams a response from the LLM based on the provided request.
     *
     * @param request
     *            The LLM request containing user message, context, and
     *            configuration
     * @return A Flux stream of response tokens
     */
    Flux<String> stream(LLMRequest request);

    /**
     * Sets the default system prompt for this provider. This will be used for
     * all requests unless overridden in the request itself.
     *
     * @param systemPrompt
     *            The system prompt
     */
    default void setSystemPrompt(String systemPrompt) {
        // Default implementation does nothing
    }

    /**
     * Represents a tool that can be called by the LLM.
     */
    interface Tool extends Serializable {
        /**
         * Gets the name of the tool.
         *
         * @return the tool name
         */
        String getName();

        /**
         * Gets the description of what the tool does. This should clearly
         * explain the tool's purpose and parameters.
         *
         * @return the tool description
         */
        String getDescription();

        /**
         * Gets the JSON schema for the tool's parameters. Can be null if the
         * tool has no parameters.
         *
         * @return the parameters schema as a JSON string, or null
         */
        String getParametersSchema();

        /**
         * Executes the tool with the given arguments.
         *
         * @param arguments
         *            the tool arguments as a JSON string
         * @return the tool execution result
         */
        String execute(String arguments);
    }

    /**
     * Represents a request to the LLM with all necessary context and
     * configuration.
     */
    interface LLMRequest extends Serializable {
        /**
         * Gets the user message.
         *
         * @return the user message
         */
        String userMessage();

        /**
         * Gets the list of attachments.
         *
         * @return the attachments
         */
        List<Attachment> attachments();

        /**
         * Gets the system prompt for this request. If null, the provider
         * should use its default system prompt.
         *
         * @return the system prompt
         */
        String systemPrompt();

        /**
         * Gets the tools available for this request.
         *
         * @return the tools
         */
        Tool[] tools();

        Object[] toolObjects();

        /**
         * Creates a simple LLM request with just a user message.
         *
         * @param userMessage
         *            the user message
         * @return a new LLMRequest instance
         */
        static LLMRequest of(String userMessage) {
            return new LLMRequest() {
                @Override
                public String userMessage() {
                    return userMessage;
                }

                @Override
                public List<Attachment> attachments() {
                    return List.of();
                }

                @Override
                public String systemPrompt() {
                    return null;
                }

                @Override
                public Tool[] tools() {
                    return new Tool[0];
                }

                @Override
                public Object[] toolObjects() {
                    return new Object[0];
                }
            };
        }
    }

    /**
     * Builder for creating LLMRequest instances with a fluent API.
     */
    class LLMRequestBuilder implements Serializable {
        private String userMessage;
        private List<Attachment> attachments = List.of();
        private String systemPrompt;
        private Tool[] tools = new Tool[0];
        private Object[] toolObjects = new Object[0];

        /**
         * Sets the user message.
         *
         * @param userMessage
         *            the user message
         * @return this builder
         */
        public LLMRequestBuilder userMessage(String userMessage) {
            this.userMessage = userMessage;
            return this;
        }

        /**
         * Sets the attachments.
         *
         * @param attachments
         *            the attachments
         * @return this builder
         */
        public LLMRequestBuilder attachments(List<Attachment> attachments) {
            this.attachments = attachments;
            return this;
        }

        /**
         * Sets the system prompt.
         *
         * @param systemPrompt
         *            the system prompt
         * @return this builder
         */
        public LLMRequestBuilder systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        /**
         * Sets the tools.
         *
         * @param tools
         *            the tools
         * @return this builder
         */
        public LLMRequestBuilder tools(Tool... tools) {
            this.tools = tools;
            return this;
        }

        

        public LLMRequestBuilder toolObjects(Object[] toolObjects) {
            this.toolObjects = toolObjects;
            return this;
        }

        /**
         * Builds the LLMRequest.
         *
         * @return the LLMRequest instance
         */
        public LLMRequest build() {
            String finalUserMessage = userMessage;
            List<Attachment> finalAttachments = attachments;
            String finalSystemPrompt = systemPrompt;
            Tool[] finalTools = tools;
            Object[] finalToolObjects = toolObjects;

            return new LLMRequest() {
                @Override
                public String userMessage() {
                    return finalUserMessage;
                }

                @Override
                public List<Attachment> attachments() {
                    return finalAttachments;
                }

                @Override
                public String systemPrompt() {
                    return finalSystemPrompt;
                }

                @Override
                public Tool[] tools() {
                    return finalTools;
                }

                @Override
                public Object[] toolObjects() {
                    return finalToolObjects;
                }
            };
        }
    }

    /**
     * Represents an attachment file that can be sent to the LLM.
     */
    interface Attachment extends Serializable {
        /**
         * Gets the file name.
         *
         * @return the file name
         */
        String fileName();

        /**
         * Gets the content type (MIME type).
         *
         * @return the content type
         */
        String contentType();

        /**
         * Gets the file data.
         *
         * @return the file data
         */
        byte[] data();

        /**
         * Creates a new Attachment instance.
         *
         * @param fileName
         *            the file name
         * @param contentType
         *            the content type
         * @param data
         *            the file data
         * @return a new Attachment instance
         */
        static Attachment of(String fileName, String contentType,
                byte[] data) {
            return new Attachment() {
                @Override
                public String fileName() {
                    return fileName;
                }

                @Override
                public String contentType() {
                    return contentType;
                }

                @Override
                public byte[] data() {
                    return data;
                }
            };
        }
    }

}
