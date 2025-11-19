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
 * Interface for Large Language Model (LLM) providers.
 * <p>
 * Implementations of this interface provide access to LLM services that can
 * generate responses based on user messages and system instructions.
 * </p>
 *
 * @author Vaadin Ltd
 */
public interface LLMProvider extends Serializable {

    /**
     * Represents a message in a conversation with an LLM.
     */
    interface Message extends Serializable {
        /**
         * Gets the role of the message sender (e.g., "user", "assistant",
         * "system").
         *
         * @return the role
         */
        String getRole();

        /**
         * Gets the content of the message.
         *
         * @return the message content
         */
        String getContent();
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
         * Gets the description of what the tool does.
         *
         * @return the tool description
         */
        String getDescription();

        /**
         * Gets the JSON schema for the tool's parameters.
         *
         * @return the parameters schema
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
     * Generates a streaming response from the LLM.
     *
     * @param messages
     *            the conversation history
     * @param systemPrompt
     *            the system prompt to guide the LLM's behavior
     * @param tools
     *            optional tools that the LLM can call
     * @return a Flux that emits response tokens as they are generated
     */
    Flux<String> generateStream(List<Message> messages, String systemPrompt,
            List<Tool> tools);

    /**
     * Creates a simple text message.
     *
     * @param role
     *            the message role (e.g., "user", "assistant", "system")
     * @param content
     *            the message content
     * @return a new Message instance
     */
    static Message createMessage(String role, String content) {
        return new Message() {
            @Override
            public String getRole() {
                return role;
            }

            @Override
            public String getContent() {
                return content;
            }
        };
    }
}
