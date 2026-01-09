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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import reactor.core.publisher.Flux;

/**
 * Framework-agnostic interface for Large Language Model (LLM) providers. This
 * interface enables AI-powered components to communicate with LLMs without
 * being tied to a specific implementation. Implementations are responsible for
 * managing conversation memory, handling streaming responses, processing
 * vendor-specific tool annotations, and handling file attachments.
 *
 * @author Vaadin Ltd.
 */
public interface LLMProvider extends Serializable {

    /**
     * Streams a response from the LLM based on the provided request. This
     * method returns a reactive stream that emits response tokens as they
     * become available from the LLM. The provider manages conversation history
     * internally, so each call to this method adds to the ongoing conversation
     * context.
     *
     * @param request
     *            the LLM request containing user message, system prompt,
     *            attachments, and tools, not {@code null}
     * @return a Flux stream that emits response tokens as strings, never
     *         {@code null}
     * @throws NullPointerException
     *             if request is {@code null}
     */
    Flux<String> stream(LLMRequest request);

    /**
     * Represents a request to the LLM containing all necessary context,
     * configuration, and tools. Requests are immutable and should be created
     * using either {@link #of(String)} for simple cases or
     * {@link LLMRequestBuilder} for complex requests with attachments, tools,
     * and custom system prompts.
     */
    interface LLMRequest extends Serializable {
        /**
         * Gets the user's message.
         *
         * @return the user message, never {@code null}
         */
        String userMessage();

        /**
         * Gets the list of file attachments to include with the request.
         * Attachments can be images, PDFs, text files, or other supported
         * formats that the LLM can analyze and reference in its response.
         *
         * @return the list of attachments, never {@code null} but may be empty
         */
        List<Attachment> attachments();

        /**
         * Gets the system prompt for this specific request. The system prompt
         * defines the LLM's behavior, role, and constraints. If {@code null},
         * the provider may use its own internal default system prompt.
         *
         * @return the system prompt, or {@code null} if not specified
         */
        String systemPrompt();

        /**
         * Gets the tool objects for this request. Tool objects are classes with
         * vendor-specific annotations (e.g., LangChain4j's {@code @Tool},
         * Spring AI's {@code @Tool}) that the provider can introspect and
         * convert to native tool definitions.
         *
         * @return array of tool objects, never {@code null} but may be empty
         */
        Object[] tools();

        /**
         * Creates a simple LLM request with just a user message. For requests
         * with attachments, tools, or custom system prompts, use
         * {@link LLMRequestBuilder} instead.
         *
         * @param userMessage
         *            the user message, not {@code null} or empty
         * @return a new LLMRequest instance
         * @throws IllegalArgumentException
         *             if userMessage is {@code null} or empty
         */
        static LLMRequest of(String userMessage) {
            if (userMessage == null || userMessage.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "User message must not be null or empty");
            }

            var finalUserMessage = userMessage.trim();

            return new LLMRequest() {
                @Override
                public String userMessage() {
                    return finalUserMessage;
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
                public Object[] tools() {
                    return new Object[0];
                }
            };
        }
    }

    /**
     * Builder for creating {@link LLMRequest} instances with a fluent API. This
     * builder provides a convenient way to construct complex requests with
     * multiple attachments, tools, and configuration options.
     */
    class LLMRequestBuilder implements Serializable {
        private String userMessage;
        private List<Attachment> attachments = List.of();
        private String systemPrompt;
        private Object[] tools = new Object[0];

        /**
         * Sets the user message.
         *
         * @param userMessage
         *            the user message, not {@code null}
         * @return this builder for method chaining
         */
        public LLMRequestBuilder userMessage(String userMessage) {
            this.userMessage = userMessage;
            return this;
        }

        /**
         * Sets the file attachments.
         *
         * @param attachments
         *            the list of attachments, or {@code null} for no
         *            attachments
         * @return this builder for method chaining
         */
        public LLMRequestBuilder attachments(List<Attachment> attachments) {
            this.attachments = attachments != null ? attachments : List.of();
            return this;
        }

        /**
         * Sets the system prompt.
         *
         * @param systemPrompt
         *            the system prompt, or {@code null} if not needed
         * @return this builder for method chaining
         */
        public LLMRequestBuilder systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        /**
         * Sets the tool objects.
         *
         * @param tools
         *            the objects with vendor-specific tool annotations
         * @return this builder for method chaining
         * @throws IllegalArgumentException
         *             if any element in the tools array is {@code null}
         */
        public LLMRequestBuilder tools(Object... tools) {
            if (tools == null) {
                this.tools = new Object[0];
            } else {
                // Validate no null elements
                for (var i = 0; i < tools.length; i++) {
                    if (tools[i] == null) {
                        throw new IllegalArgumentException(
                                "Tool at index " + i + " must not be null");
                    }
                }
                this.tools = tools;
            }
            return this;
        }

        /**
         * Builds the immutable LLMRequest.
         *
         * @return the LLMRequest instance
         * @throws IllegalStateException
         *             if userMessage was not set or is empty
         */
        public LLMRequest build() {
            if (userMessage == null || userMessage.trim().isEmpty()) {
                throw new IllegalStateException(
                        "User message must be set before building");
            }

            var finalUserMessage = userMessage.trim();
            var finalAttachments = List.copyOf(attachments);
            var finalSystemPrompt = systemPrompt != null
                    && !systemPrompt.trim().isEmpty() ? systemPrompt.trim()
                            : null;
            var finalTools = Arrays.copyOf(tools, tools.length);

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
                public Object[] tools() {
                    return finalTools;
                }
            };
        }
    }

    /**
     * Represents a file attachment that can be sent to the LLM for analysis.
     * Attachments support various file types including images, documents, and
     * text files. The LLM can analyze, reference, and answer questions about
     * the attachment content.
     */
    interface Attachment extends Serializable {
        /**
         * Gets the file name.
         *
         * @return the file name including extension
         */
        String fileName();

        /**
         * Gets the MIME content type.
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
         *            the file name, not {@code null}
         * @param contentType
         *            the MIME type, not {@code null}
         * @param data
         *            the file data, not {@code null}
         * @return a new Attachment instance
         * @throws IllegalArgumentException
         *             if any parameter is {@code null}
         */
        static Attachment of(String fileName, String contentType, byte[] data) {
            if (fileName == null || contentType == null || data == null) {
                throw new IllegalArgumentException(
                        "fileName, contentType, and data must not be null");
            }

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
