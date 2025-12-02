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
import com.vaadin.flow.component.ai.messagelist.AiMessage;
import com.vaadin.flow.component.ai.messagelist.AiMessageList;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.upload.AiFileReceiver;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.server.streams.UploadHandler;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Orchestrator for AI-powered chat interfaces.
 * <p>
 * This class connects an {@link AiMessageList}, {@link AiInput}, and an
 * {@link LLMProvider} to create an interactive AI chat experience. It handles:
 * </p>
 * <ul>
 * <li>Streaming responses from the LLM to the message list</li>
 * <li>Automatic UI updates via server push</li>
 * <li>Optional file upload support</li>
 * <li>Plugin architecture for extending functionality</li>
 * <li>Input validation for security</li>
 * </ul>
 * <p>
 * Conversation history is managed internally by the {@link LLMProvider}
 * instance. Each orchestrator maintains its own conversation context through
 * its provider instance.
 * </p>
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * MessageList messageList = new MessageList();
 * MessageInput messageInput = new MessageInput();
 *
 * LLMProvider provider = new LangChain4jProvider(model);
 * provider.setSystemPrompt("You are a helpful assistant.");
 *
 * AiOrchestrator orchestrator = AiOrchestrator.create(provider)
 *         .withMessageList(messageList)
 *         .withInput(messageInput)
 *         .build();
 * </pre>
 *
 * @author Vaadin Ltd
 */
public class AiOrchestrator implements Serializable {

    protected final LLMProvider provider;
    protected final String systemPrompt;
    protected AiMessageList messageList;
    protected AiInput input;
    protected AiFileReceiver fileReceiver;
    protected InputValidator inputValidator;
    protected final List<LLMProvider.Attachment> pendingAttachments = new ArrayList<>();
    protected Object[] toolObjects = new Object[0];
    protected final List<AiPlugin> plugins = new ArrayList<>();
    private UI ui;

    /**
     * Creates a new AI orchestrator.
     *
     * @param provider
     *            the LLM provider to use for generating responses
     * @param systemPrompt
     *            the system prompt for the LLM (can be null)
     */
    private AiOrchestrator(LLMProvider provider, String systemPrompt) {
        Objects.requireNonNull(provider, "Provider cannot be null");
        this.provider = provider;
        this.systemPrompt = systemPrompt;
    }

    /**
     * Creates a new builder for AiOrchestrator.
     *
     * @param provider
     *            the LLM provider
     * @return a new builder
     */
    public static Builder builder(LLMProvider provider) {
        return new Builder(provider, null);
    }

    /**
     * Creates a new builder for AiOrchestrator with a system prompt.
     *
     * @param provider
     *            the LLM provider
     * @param systemPrompt
     *            the system prompt for the LLM
     * @return a new builder
     */
    public static Builder builder(LLMProvider provider, String systemPrompt) {
        return new Builder(provider, systemPrompt);
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
     * Gets the input component.
     *
     * @return the input component, or null if not set
     */
    public AiInput getInput() {
        return input;
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
     * Sets the input component.
     *
     * @param input
     *            the input component
     */
    protected void setInput(AiInput input) {
        this.input = input;
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
     * Sets the input validator for security checks.
     *
     * @param inputValidator
     *            the input validator
     */
    protected void setInputValidator(InputValidator inputValidator) {
        this.inputValidator = inputValidator;
    }

    /**
     * Sets the tool objects that contain {@link Tool}-annotated methods.
     *
     * @param toolObjects
     *            the objects containing tool methods
     */
    protected void setToolObjects(Object[] toolObjects) {
        this.toolObjects = toolObjects != null ? toolObjects : new Object[0];
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
     * Handles a validation rejection by displaying an error message to the
     * user.
     *
     * @param rejectionMessage
     *            the reason for rejection
     */
    protected void handleValidationRejection(String rejectionMessage) {
        if (messageList != null) {
            AiMessage errorMessage = messageList.createMessage(
                    "⚠️ " + rejectionMessage, "System");
            messageList.addMessage(errorMessage);
        }
    }

    /**
     * Adds a user message to the message list.
     *
     * @param userMessage
     *            the user's message text
     */
    protected void addUserMessageToList(String userMessage) {
        if (messageList != null) {
            AiMessage userItem = messageList.createMessage(userMessage, "User");
            messageList.addMessage(userItem);

            if (pendingAttachments.isEmpty()) {
                return;
            }
            var attachmentsLayout = new Div();
            for (LLMProvider.Attachment attachment : pendingAttachments) {
                var fileComponent = new Span();
                fileComponent.getElement().getStyle().setMarginInlineEnd("20px");
                fileComponent.setText("📎 " + attachment.fileName());
                attachmentsLayout.add(fileComponent);
            }

            userItem.setPrefix(attachmentsLayout);
        }
    }

    /**
     * Creates and adds an assistant message placeholder to the message list.
     *
     * @return the created assistant message, or null if messageList is not
     *         configured
     */
    protected AiMessage createAssistantMessagePlaceholder() {
        if (messageList == null) {
            return null;
        }
        AiMessage assistantMessage = messageList.createMessage("", "Assistant");
        messageList.addMessage(assistantMessage);
        return assistantMessage;
    }

    /**
     * Streams a response from the LLM and updates the assistant message in
     * real-time.
     *
     * @param request
     *            the LLM request to process
     * @param assistantMessage
     *            the assistant message to update (can be null if messageList is
     *            not configured)
     * @param onComplete
     *            optional callback to execute when streaming completes
     *            successfully (can be null)
     */
    protected void streamResponseToMessage(LLMProvider.LLMRequest request,
            AiMessage assistantMessage, Runnable onComplete) {
    
        Flux<String> responseStream = provider.stream(request);

        responseStream.subscribe(token -> {
            // Update UI with the accumulated response
            if (assistantMessage != null && messageList != null) {
                ui.access(() -> {
                    assistantMessage.appendText(token);
                });
            }
        }, error -> {
            // Handle error
            if (assistantMessage != null && messageList != null) {
                ui.access(() -> {
                    assistantMessage.setText("Error: " + error.getMessage());
                });
            }
        }, () -> {
            // Streaming complete - provider has already added the response to
            // conversation history
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    /**
     * Streams a response with custom token processing.
     *
     * @param request
     *            the LLM request to process
     * @param onToken
     *            callback to process each token
     * @param onError
     *            callback to handle errors
     * @param onComplete
     *            callback when streaming completes
     */
    protected void streamResponse(LLMProvider.LLMRequest request,
            Consumer<String> onToken, Consumer<Throwable> onError,
            Runnable onComplete) {
        Flux<String> responseStream = provider.stream(request);
        responseStream.subscribe(onToken::accept, onError::accept,
                onComplete);
    }

    /**
     * Sends a message to the AI orchestrator programmatically. This method
     * allows sending messages without requiring an input component.
     * <p>
     * This is useful for scenarios where you want to trigger AI interaction
     * from button clicks or other UI events without using a message input
     * component.
     * </p>
     *
     * @param userMessage
     *            the message to send to the AI
     */
    public void sendMessage(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return;
        }

        // Add user message to UI if messageList is configured
        addUserMessageToList(userMessage);

        // Process the message through the LLM
        processUserInput(userMessage);
    }

    /**
     * Handles a user input submission event. This method implements the common
     * pattern of validating input, adding the user message to the UI, and
     * delegating to the subclass for processing.
     * <p>
     * Subclasses must implement {@link #processUserInput(String)} to define
     * their specific processing logic.
     * </p>
     *
     * @param event
     *            the input submit event containing the user's message
     */
    protected void handleUserInput(
            com.vaadin.flow.component.ai.input.InputSubmitEvent event) {
        String userMessage = event.getValue();
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return;
        }

        // Add user message to UI
        addUserMessageToList(userMessage);

        // Delegate to subclass for specific processing
        processUserInput(userMessage);
    }

    /**
     * Processes the user's input message by building an LLM request and
     * streaming the response. This method implements the common pattern shared
     * by all orchestrators.
     *
     * @param userMessage
     *            the user's input message
     */
    protected void processUserInput(String userMessage) {
        if (this.ui == null) {
            this.ui = validateUiContext();
        }

        // Validate user input if validator is configured
        if (inputValidator != null) {
            InputValidator.ValidationResult result = inputValidator
                    .validateInput(userMessage);
            if (!result.isAccepted()) {
                handleValidationRejection(result.getRejectionMessage());
                return;
            }
        }

        // Validate attachments if validator is configured
        if (inputValidator != null && !pendingAttachments.isEmpty()) {
            for (LLMProvider.Attachment attachment : pendingAttachments) {
                InputValidator.ValidationResult result = inputValidator
                        .validateAttachment(attachment);
                if (!result.isAccepted()) {
                    handleValidationRejection(result.getRejectionMessage());
                    return;
                }
            }
        }

        // Create a placeholder for the assistant's message (may be null if no
        // messageList)
        AiMessage assistantMessage = createAssistantMessagePlaceholder();

        // Get tools from subclass
        LLMProvider.Tool[] tools = createTools();

        // Build LLM request with any pending attachments
        LLMProvider.LLMRequestBuilder requestBuilder = new LLMProvider.LLMRequestBuilder()
                .userMessage(userMessage)
                .attachments(new ArrayList<>(pendingAttachments));

        // Add system prompt if provided
        if (this.systemPrompt != null && !this.systemPrompt.isEmpty()) {
            requestBuilder.systemPrompt(this.systemPrompt);
        }

        // Add tools if provided by subclass
        if (tools != null && tools.length > 0) {
            requestBuilder.tools(tools);
        }

        if (toolObjects.length > 0) {
            requestBuilder.toolObjects(toolObjects);
        }

        LLMProvider.LLMRequest request = requestBuilder.build();

        // Clear pending attachments after building the request
        pendingAttachments.clear();
        if (fileReceiver != null) {
            ui.access(() -> {
                fileReceiver.clearFileList();
            });
        }

        // Stream response using base class method
        streamResponseToMessage(request, assistantMessage,
                () -> onProcessingComplete());
    }

    /**
     * Creates tools for the LLM to use. This method converts any
     * {@link Tool}-annotated methods from tool objects into LLMProvider.Tool
     * instances. Subclasses can override this method to add additional custom
     * tools.
     *
     * @return array of tools, or empty array if no tools needed
     */
    protected LLMProvider.Tool[] createTools() {
        List<LLMProvider.Tool> tools = new ArrayList<>();

        // Add tools from plugins
        for (AiPlugin plugin : plugins) {
            List<LLMProvider.Tool> pluginTools = plugin.getTools();
            if (pluginTools != null) {
                tools.addAll(pluginTools);
            }
        }

        return tools.toArray(new LLMProvider.Tool[0]);
    }


    /**
     * Gets the parameter name, falling back to arg{index} if the name is not
     * available.
     *
     * @param param
     *            the parameter
     * @param index
     *            the parameter index
     * @return the parameter name
     */
    private String getParameterName(Parameter param, int index) {
        String name = param.getName();
        // If parameter name is not available (e.g., arg0, arg1), use numbered
        // names
        if (name.matches("arg\\d+")) {
            return "arg" + index;
        }
        return name;
    }

    /**
     * Gets the JSON type name for a Java type.
     *
     * @param type
     *            the Java type
     * @return the JSON type name
     */
    private String getJsonType(Class<?> type) {
        if (type == String.class) {
            return "string";
        } else if (type == int.class || type == Integer.class
                || type == long.class || type == Long.class
                || type == short.class || type == Short.class
                || type == byte.class || type == Byte.class) {
            return "integer";
        } else if (type == double.class || type == Double.class
                || type == float.class || type == Float.class) {
            return "number";
        } else if (type == boolean.class || type == Boolean.class) {
            return "boolean";
        }
        return "string"; // Default to string for unknown types
    }

    /**
     * Escapes a string for use in JSON.
     *
     * @param value
     *            the string to escape
     * @return the escaped string
     */
    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    /**
     * Parses JSON arguments and converts them to method parameter values.
     *
     * @param method
     *            the method to invoke
     * @param arguments
     *            the JSON arguments string
     * @return array of parsed argument values
     */
    private Object[] parseArguments(Method method, String arguments) {
        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) {
            return new Object[0];
        }

        // Simple JSON parsing - extract values by parameter names
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            String paramName = getParameterName(param, i);
            Class<?> paramType = param.getType();

            // Extract value from JSON
            String value = extractJsonValue(arguments, paramName);
            args[i] = convertValue(value, paramType);
        }

        return args;
    }

    /**
     * Extracts a value from a JSON string by key.
     *
     * @param json
     *            the JSON string
     * @param key
     *            the key to extract
     * @return the extracted value as a string
     */
    private String extractJsonValue(String json, String key) {
        if (json == null || key == null) {
            return null;
        }

        // Look for "key": in the JSON
        String searchPattern = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchPattern);
        if (keyIndex == -1) {
            return null;
        }

        // Find the colon after the key
        int colonIndex = json.indexOf(':', keyIndex + searchPattern.length());
        if (colonIndex == -1) {
            return null;
        }

        // Skip whitespace after colon
        int valueStart = colonIndex + 1;
        while (valueStart < json.length()
                && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        if (valueStart >= json.length()) {
            return null;
        }

        // Check if value is a string (starts with ")
        if (json.charAt(valueStart) == '"') {
            valueStart++; // Skip opening quote
            // Find closing quote, handling escaped quotes
            int valueEnd = valueStart;
            while (valueEnd < json.length()) {
                if (json.charAt(valueEnd) == '"'
                        && (valueEnd == 0 || json.charAt(valueEnd - 1) != '\\')) {
                    return json.substring(valueStart, valueEnd);
                }
                valueEnd++;
            }
            return null;
        } else {
            // Non-string value - find end (comma, brace, bracket, or whitespace)
            int valueEnd = valueStart;
            while (valueEnd < json.length()) {
                char c = json.charAt(valueEnd);
                if (c == ',' || c == '}' || c == ']'
                        || Character.isWhitespace(c)) {
                    break;
                }
                valueEnd++;
            }
            return json.substring(valueStart, valueEnd).trim();
        }
    }

    /**
     * Converts a string value to the target type.
     *
     * @param value
     *            the string value
     * @param targetType
     *            the target type
     * @return the converted value
     */
    private Object convertValue(String value, Class<?> targetType) {
        if (value == null || value.equals("null")) {
            return null;
        }

        if (targetType == String.class) {
            return value;
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(value);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (targetType == short.class || targetType == Short.class) {
            return Short.parseShort(value);
        } else if (targetType == byte.class || targetType == Byte.class) {
            return Byte.parseByte(value);
        }

        return value; // Default to string
    }

    /**
     * Returns the system prompt for the LLM. Subclasses that need a system
     * prompt should override this method.
     *
     * @return the system prompt, or null if no system prompt needed
     */
    protected String getSystemPrompt() {
        return systemPrompt;
    }

    /**
     * Called when processing is complete. Subclasses can override to perform
     * additional actions after streaming completes.
     */
    protected void onProcessingComplete() {
        // Default: do nothing
    }

    /**
     * Configures the file receiver with the appropriate upload handler for
     * managing file attachments.
     */
    protected void configureFileReceiver() {
        if (fileReceiver == null) {
            return;
        }

        fileReceiver.setUploadHandler(UploadHandler.inMemory((meta, data) -> {
            pendingAttachments.add(LLMProvider.Attachment.of(
                    meta.fileName(),
                    meta.contentType(),
                    data));
        }));

        fileReceiver.addFileRemovedListener(fileName -> {
            pendingAttachments.removeIf(
                    attachment -> attachment.fileName().equals(fileName));
        });
    }

    /**
     * Builder for AiOrchestrator.
     */
    public static class Builder {
        protected final LLMProvider provider;
        protected final String systemPrompt;
        protected AiMessageList messageList;
        protected AiInput input;
        protected AiFileReceiver fileReceiver;
        protected InputValidator inputValidator;
        protected Object[] toolObjects = new Object[0];
        protected List<AiPlugin> plugins = new ArrayList<>();

        private Builder(LLMProvider provider, String systemPrompt) {
            this.provider = provider;
            this.systemPrompt = systemPrompt;
        }

        /**
         * Sets the message list component.
         *
         * @param messageList
         *            the message list
         * @return this builder
         */
        public Builder withMessageList(AiMessageList messageList) {
            this.messageList = messageList;
            return this;
        }

        /**
         * Sets the input component.
         *
         * @param input
         *            the input component
         * @return this builder
         */
        public Builder withInput(AiInput input) {
            this.input = input;
            return this;
        }

        /**
         * Sets the file receiver component for file uploads.
         *
         * @param fileReceiver
         *            the file receiver
         * @return this builder
         */
        public Builder withFileReceiver(AiFileReceiver fileReceiver) {
            this.fileReceiver = fileReceiver;
            return this;
        }

        /**
         * Sets the input validator for security checks.
         * <p>
         * Validators can prevent prompt injection attacks and enforce content
         * policies by checking both text input and file attachments before
         * they are sent to the LLM.
         * </p>
         *
         * @param inputValidator
         *            the input validator
         * @return this builder
         */
        public Builder withInputValidator(InputValidator inputValidator) {
            this.inputValidator = inputValidator;
            return this;
        }

        /**
         * Sets the objects containing vendor-specific {@link Tool}-annotated methods that will
         * be available to the LLM.
         * </p>
         *
         * @param toolObjects
         *            the objects containing tool methods
         * @return this builder
         */
        public Builder withTools(Object... toolObjects) {
            this.toolObjects = toolObjects != null ? toolObjects : new Object[0];
            return this;
        }

        /**
         * Adds a plugin to extend the orchestrator with additional capabilities.
         * <p>
         * Plugins provide tools, system prompt contributions, and can manage
         * their own state. Multiple plugins can be added and they will all be
         * active simultaneously.
         * </p>
         *
         * @param plugin
         *            the plugin to add
         * @return this builder
         */
        public Builder withPlugin(AiPlugin plugin) {
            if (plugin != null) {
                this.plugins.add(plugin);
            }
            return this;
        }

        /**
         * Builds the orchestrator.
         *
         * @return the configured orchestrator
         */
        public AiOrchestrator build() {
            AiOrchestrator orchestrator = new AiOrchestrator(provider, systemPrompt);

            orchestrator.setMessageList(messageList);
            orchestrator.setInput(input);
            orchestrator.setFileReceiver(fileReceiver);
            orchestrator.setInputValidator(inputValidator);
            orchestrator.setToolObjects(toolObjects);

            // Attach plugins and call their lifecycle hooks
            for (AiPlugin plugin : plugins) {
                orchestrator.plugins.add(plugin);
                plugin.onAttached(orchestrator);
            }

            // Configure input listener if provided
            if (input != null) {
                input.addSubmitListener(orchestrator::handleUserInput);
            }

            if (messageList != null) {
                messageList.setMarkdown(true);
            }

            // Configure file receiver if provided
            if (fileReceiver != null) {
                orchestrator.configureFileReceiver();
            }

            return orchestrator;
        }
    }
}
