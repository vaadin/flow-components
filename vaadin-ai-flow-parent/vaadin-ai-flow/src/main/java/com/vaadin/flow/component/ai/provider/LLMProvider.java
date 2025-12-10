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
import java.util.Objects;

/**
 * Framework-agnostic interface for Large Language Model providers.
 * <p>
 * This interface abstracts the communication with LLM services, allowing Vaadin
 * AI components to work with different AI frameworks (Spring AI, LangChain4j,
 * etc.) through a unified API. Implementations handle conversation memory
 * internally, managing conversation context per provider instance.
 * </p>
 *
 * <h3>Example Usage:</h3>
 *
 * <pre>
 * LLMProvider provider = new MyLLMProvider(model);
 *
 * LLMProvider.LLMRequest request = LLMProvider.LLMRequest.of("Hello, how are you?");
 *
 * provider.stream(request)
 *         .subscribe(token -&gt; System.out.print(token));
 * </pre>
 *
 * @author Vaadin Ltd
 */
public interface LLMProvider extends Serializable {

    /**
     * Streams a response from the LLM based on the provided request.
     * <p>
     * The response is streamed as individual tokens, allowing for real-time UI
     * updates as the LLM generates its response. Implementations should handle
     * tool execution internally when tools are provided in the request.
     * </p>
     *
     * @param request
     *            the LLM request containing user message, context, and
     *            configuration
     * @return a Flux stream of response tokens
     */
    Flux<String> stream(LLMRequest request);

    /**
     * Represents a tool that can be called by the LLM.
     * <p>
     * Tools allow the LLM to perform actions beyond generating text, such as
     * querying databases, updating UI components, or calling external APIs.
     * Each tool has a name, description, parameter schema, and an execute
     * method.
     * </p>
     *
     * <h3>Example Implementation:</h3>
     *
     * <pre>
     * LLMProvider.Tool weatherTool = new LLMProvider.Tool() {
     *     &#64;Override
     *     public String getName() {
     *         return "getWeather";
     *     }
     *
     *     &#64;Override
     *     public String getDescription() {
     *         return "Gets the current weather for a city";
     *     }
     *
     *     &#64;Override
     *     public String getParametersSchema() {
     *         return "{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"}},\"required\":[\"city\"]}";
     *     }
     *
     *     &#64;Override
     *     public String execute(String arguments) {
     *         // Parse arguments and return weather data
     *         return "Sunny, 72°F";
     *     }
     * };
     * </pre>
     */
    interface Tool extends Serializable {
        /**
         * Gets the name of the tool.
         * <p>
         * The name should be a valid identifier that the LLM can use to
         * reference this tool. It should be descriptive and follow naming
         * conventions (e.g., camelCase or snake_case).
         * </p>
         *
         * @return the tool name
         */
        String getName();

        /**
         * Gets the description of what the tool does.
         * <p>
         * This description is provided to the LLM to help it understand when
         * and how to use the tool. It should clearly explain the tool's purpose
         * and expected parameters.
         * </p>
         *
         * @return the tool description
         */
        String getDescription();

        /**
         * Gets the JSON schema for the tool's parameters.
         * <p>
         * The schema should follow the JSON Schema specification and describe
         * the expected structure of the arguments passed to
         * {@link #execute(String)}.
         * </p>
         *
         * @return the parameters schema as a JSON string, or null if the tool
         *         has no parameters
         */
        String getParametersSchema();

        /**
         * Executes the tool with the given arguments.
         * <p>
         * The arguments are provided as a JSON string matching the schema
         * returned by {@link #getParametersSchema()}. The result should be a
         * string that the LLM can use in its response.
         * </p>
         *
         * @param arguments
         *            the tool arguments as a JSON string
         * @return the tool execution result
         */
        String execute(String arguments);
    }

    /**
     * Represents a file attachment that can be included in an LLM request.
     * <p>
     * Attachments can include images, PDFs, text files, or other content that
     * multimodal LLMs can process alongside the user's text message.
     * </p>
     *
     * @param fileName
     *            the name of the file
     * @param mimeType
     *            the MIME type of the file (e.g., "image/png", "application/pdf")
     * @param data
     *            the file content as a byte array
     */
    record Attachment(String fileName, String mimeType, byte[] data)
            implements Serializable {

        /**
         * Creates a new attachment.
         *
         * @param fileName
         *            the name of the file, cannot be null
         * @param mimeType
         *            the MIME type of the file, cannot be null
         * @param data
         *            the file content, cannot be null
         */
        public Attachment {
            Objects.requireNonNull(fileName, "File name cannot be null");
            Objects.requireNonNull(mimeType, "MIME type cannot be null");
            Objects.requireNonNull(data, "Data cannot be null");
        }

        /**
         * Checks if this attachment is an image.
         *
         * @return true if the MIME type starts with "image/"
         */
        public boolean isImage() {
            return mimeType.startsWith("image/");
        }

        /**
         * Checks if this attachment is a PDF.
         *
         * @return true if the MIME type is "application/pdf"
         */
        public boolean isPdf() {
            return "application/pdf".equals(mimeType);
        }

        /**
         * Checks if this attachment is a text file.
         *
         * @return true if the MIME type starts with "text/"
         */
        public boolean isText() {
            return mimeType.startsWith("text/");
        }
    }

    /**
     * Represents a request to the LLM with all necessary context and
     * configuration.
     *
     * @param userMessage
     *            the user message, cannot be null
     * @param attachments
     *            the list of file attachments, cannot be null
     * @param systemPrompt
     *            the system prompt for this request, or null to use provider's
     *            default
     * @param tools
     *            the tools available for this request, cannot be null
     * @param toolObjects
     *            objects containing vendor-specific tool annotations, cannot be
     *            null
     */
    record LLMRequest(
            String userMessage,
            List<Attachment> attachments,
            String systemPrompt,
            Tool[] tools,
            Object[] toolObjects) implements Serializable {

        /**
         * Creates a new LLM request with validation.
         *
         * @param userMessage
         *            the user message, cannot be null
         * @param attachments
         *            the list of file attachments, cannot be null
         * @param systemPrompt
         *            the system prompt for this request, or null to use
         *            provider's default
         * @param tools
         *            the tools available for this request, cannot be null
         * @param toolObjects
         *            objects containing vendor-specific tool annotations,
         *            cannot be null
         */
        public LLMRequest {
            Objects.requireNonNull(userMessage, "User message cannot be null");
            Objects.requireNonNull(attachments, "Attachments cannot be null");
            Objects.requireNonNull(tools, "Tools cannot be null");
            Objects.requireNonNull(toolObjects, "Tool objects cannot be null");
            // Make defensive copies
            attachments = List.copyOf(attachments);
            tools = tools.clone();
            toolObjects = toolObjects.clone();
        }

        /**
         * Returns a defensive copy of the tools array.
         *
         * @return a copy of the tools array
         */
        @Override
        public Tool[] tools() {
            return tools.clone();
        }

        /**
         * Returns a defensive copy of the tool objects array.
         *
         * @return a copy of the tool objects array
         */
        @Override
        public Object[] toolObjects() {
            return toolObjects.clone();
        }

        /**
         * Creates a simple LLM request with just a user message.
         *
         * @param userMessage
         *            the user message
         * @return a new LLMRequest instance
         */
        public static LLMRequest of(String userMessage) {
            return new LLMRequest(
                    userMessage,
                    List.of(),
                    null,
                    new Tool[0],
                    new Object[0],
                    null);
        }
    }
}
