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
package com.vaadin.flow.component.ai.provider;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.common.ChatMessage;

import reactor.core.publisher.Flux;
import tools.jackson.databind.JsonNode;

/**
 * Framework-agnostic interface for Large Language Model (LLM) providers. This
 * interface enables AI-powered components to communicate with LLMs without
 * being tied to a specific implementation. Implementations are responsible for
 * managing conversation memory, handling streaming responses, processing
 * vendor-specific tool annotations, and handling file attachments.
 * <p>
 * Create an instance by constructing the appropriate implementation directly:
 *
 * <pre>
 * // Spring AI
 * LLMProvider provider = new SpringAILLMProvider(chatModel);
 *
 * // LangChain4j
 * LLMProvider provider = new LangChain4JLLMProvider(streamingChatModel);
 * </pre>
 *
 * @author Vaadin Ltd.
 */
public interface LLMProvider {

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
     * Restores the provider's conversation memory from a list of chat messages
     * with their associated attachments. Any existing memory is cleared before
     * the new history is applied.
     * <p>
     * Providers that support setting chat history should override this method.
     * <p>
     * This method must not be called while a streaming response is in progress.
     *
     * @param history
     *            the list of chat messages to restore, not {@code null}
     * @param attachmentsByMessageId
     *            a map from {@link ChatMessage#messageId()} to the list of
     *            attachments for that message, not {@code null}
     * @throws NullPointerException
     *             if any argument is {@code null}
     * @throws UnsupportedOperationException
     *             if this provider does not support chat history restoration
     */
    default void setHistory(List<ChatMessage> history,
            Map<String, List<AIAttachment>> attachmentsByMessageId) {
        throw new UnsupportedOperationException(
                "This LLM provider does not support chat history restoration.");
    }

    /**
     * Represents a request to the LLM containing all necessary context,
     * configuration, and tools. Requests are immutable.
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
        List<AIAttachment> attachments();

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
         * Gets the explicit tool definitions for this request. Unlike
         * vendor-specific annotated tools returned by {@link #tools()}, these
         * are framework-agnostic tool definitions provided programmatically
         * (typically by
         * {@link com.vaadin.flow.component.ai.orchestrator.AIController}
         * instances).
         *
         * @return list of explicit tool definitions, never {@code null} but may
         *         be empty
         */
        default List<ToolSpec> explicitTools() {
            return List.of();
        }
    }

    /**
     * A framework-agnostic tool definition that the LLM can invoke.
     * <p>
     * Unlike vendor-specific tool annotations (e.g., LangChain4j's
     * {@code @Tool}, Spring AI's {@code @Tool}), this interface allows tools to
     * be defined programmatically without depending on any specific AI
     * framework.
     * </p>
     * <p>
     * Tool definitions are typically provided by
     * {@link com.vaadin.flow.component.ai.orchestrator.AIController}
     * implementations through their
     * {@link com.vaadin.flow.component.ai.orchestrator.AIController#getTools()}
     * method.
     * </p>
     */
    interface ToolSpec {

        /**
         * Gets the unique name of this tool. The name must contain only
         * alphanumeric characters, underscores, and hyphens, with a maximum
         * length of 64 characters (matching the pattern
         * {@code ^[a-zA-Z0-9_-]{1,64}$}), as required by popular LLM APIs.
         * Names that do not match this pattern will be rejected by the
         * orchestrator at request time. To avoid name collisions, use a
         * namespaced name such as {@code "MyController_updateConfig"}.
         *
         * @return the tool name, never {@code null} or empty
         */
        String getName();

        /**
         * Gets a human-readable description of what this tool does. This
         * description is sent to the LLM to help it decide when to invoke the
         * tool.
         *
         * @return the tool description, never {@code null}
         */
        String getDescription();

        /**
         * Gets the JSON Schema describing the parameters this tool accepts. The
         * schema should follow the JSON Schema specification.
         * <p>
         * Example:
         * </p>
         *
         * <pre>
         * {
         *   "type": "object",
         *   "properties": {
         *     "query": { "type": "string", "description": "The SQL query" }
         *   },
         *   "required": ["query"]
         * }
         * </pre>
         * <p>
         * Hand-writing the JSON is fine for small schemas but becomes
         * error-prone as they grow. For larger schemas, a schema generator
         * catches keyword typos and structural mistakes at compile time. When
         * using {@link SpringAILLMProvider}, Spring AI's
         * {@code JsonSchemaGenerator} is available without an extra dependency:
         * </p>
         *
         * <pre>
         * import com.fasterxml.jackson.annotation.JsonPropertyDescription;
         * import org.springframework.ai.util.json.schema.JsonSchemaGenerator;
         *
         * record UpdateGridArgs(
         *         &#064;JsonPropertyDescription("The SQL query") String query) {}
         *
         * private static final String SCHEMA = JsonSchemaGenerator
         *         .generateForType(UpdateGridArgs.class);
         *
         * // in execute(JsonNode arguments):
         * UpdateGridArgs args = new ObjectMapper().treeToValue(arguments,
         *         UpdateGridArgs.class);
         * </pre>
         * <p>
         * For other setups, any general-purpose JSON Schema generator that
         * produces a schema string from a typed Java class works equivalently.
         * </p>
         * <p>
         * Whichever approach you pick, verify the output stays inside the
         * portable JSON Schema subset (string, integer, number, boolean, array,
         * object, plus {@code anyOf} and {@code enum}). Generators may emit
         * keywords such as {@code format}, {@code pattern}, or {@code $ref}
         * that not every LLM provider accepts.
         * </p>
         *
         * @return the JSON Schema string, or {@code null} if the tool takes no
         *         parameters
         */
        String getParametersSchema();

        /**
         * Executes the tool with the given arguments.
         * <p>
         * Implementations should return a human-readable result string on
         * success. On failure, they may throw any runtime exception.
         * </p>
         *
         * @param arguments
         *            the tool arguments as a {@link JsonNode} matching the
         *            parameters schema
         * @return the result of the tool execution as a string, never
         *         {@code null}
         */
        String execute(JsonNode arguments);
    }
}
