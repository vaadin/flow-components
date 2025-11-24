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
    protected final List<LLMProvider.Attachment> pendingAttachments = new ArrayList<>();
    protected final List<Object> toolObjects = new ArrayList<>();
    private UI ui;

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
     * Sets the tool objects that contain {@link Tool}-annotated methods.
     *
     * @param toolObjects
     *            the objects containing tool methods
     */
    protected void setToolObjects(List<Object> toolObjects) {
        this.toolObjects.clear();
        if (toolObjects != null) {
            this.toolObjects.addAll(toolObjects);
        }
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
     * Adds a user message to the message list.
     *
     * @param userMessage
     *            the user's message text
     */
    protected void addUserMessageToList(String userMessage) {
        if (messageList != null) {
            AiMessage userItem = messageList.createMessage(userMessage, "User");
            messageList.addMessage(userItem);
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
        
        // Create a placeholder for the assistant's message (may be null if no
        // messageList)
        AiMessage assistantMessage = createAssistantMessagePlaceholder();

        // Get tools from subclass
        LLMProvider.Tool[] tools = createTools();

        // Build LLM request with any pending attachments
        LLMProvider.LLMRequestBuilder requestBuilder = new LLMProvider.LLMRequestBuilder()
                .userMessage(userMessage)
                .attachments(new ArrayList<>(pendingAttachments));

        // Add system prompt if provided by subclass
        String systemPrompt = getSystemPrompt();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            requestBuilder.systemPrompt(systemPrompt);
        }

        // Add tools if provided by subclass
        if (tools != null && tools.length > 0) {
            requestBuilder.tools(tools);
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
        if (toolObjects.isEmpty()) {
            return new LLMProvider.Tool[0];
        }

        List<LLMProvider.Tool> tools = new ArrayList<>();
        for (Object toolObject : toolObjects) {
            tools.addAll(convertObjectToTools(toolObject));
        }
        return tools.toArray(new LLMProvider.Tool[0]);
    }

    /**
     * Converts an object with {@link Tool}-annotated methods into
     * LLMProvider.Tool instances.
     *
     * @param toolObject
     *            the object containing tool methods
     * @return list of converted tools
     */
    private List<LLMProvider.Tool> convertObjectToTools(Object toolObject) {
        List<LLMProvider.Tool> tools = new ArrayList<>();

        for (Method method : toolObject.getClass().getDeclaredMethods()) {
            Tool toolAnnotation = method.getAnnotation(Tool.class);
            if (toolAnnotation == null) {
                continue;
            }

            // Make method accessible if it's private
            method.setAccessible(true);

            tools.add(createToolFromMethod(toolObject, method, toolAnnotation));
        }

        return tools;
    }

    /**
     * Creates an LLMProvider.Tool from an annotated method.
     *
     * @param toolObject
     *            the object containing the method
     * @param method
     *            the tool method
     * @param toolAnnotation
     *            the tool annotation
     * @return the created tool
     */
    private LLMProvider.Tool createToolFromMethod(Object toolObject,
            Method method, Tool toolAnnotation) {
        return new LLMProvider.Tool() {
            @Override
            public String getName() {
                return method.getName();
            }

            @Override
            public String getDescription() {
                StringBuilder description = new StringBuilder(
                        toolAnnotation.value());

                // Add parameter descriptions
                Parameter[] parameters = method.getParameters();
                if (parameters.length > 0) {
                    description.append("\n\nParameters:");
                    for (int i = 0; i < parameters.length; i++) {
                        Parameter param = parameters[i];
                        ParameterDescription paramDesc = param
                                .getAnnotation(ParameterDescription.class);
                        String paramName = getParameterName(param, i);
                        String paramType = param.getType().getSimpleName();
                        description.append("\n- ").append(paramName)
                                .append(" (").append(paramType).append(")");
                        if (paramDesc != null) {
                            description.append(": ").append(paramDesc.value());
                        }
                    }
                }

                return description.toString();
            }

            @Override
            public String getParametersSchema() {
                Parameter[] parameters = method.getParameters();
                if (parameters.length == 0) {
                    return null;
                }

                // Build a simple JSON schema
                StringBuilder schema = new StringBuilder();
                schema.append("{\"type\":\"object\",\"properties\":{");

                for (int i = 0; i < parameters.length; i++) {
                    Parameter param = parameters[i];
                    if (i > 0) {
                        schema.append(",");
                    }
                    String paramName = getParameterName(param, i);
                    schema.append("\"").append(paramName).append("\":{");
                    schema.append("\"type\":\"")
                            .append(getJsonType(param.getType())).append("\"");

                    ParameterDescription paramDesc = param
                            .getAnnotation(ParameterDescription.class);
                    if (paramDesc != null) {
                        schema.append(",\"description\":\"")
                                .append(escapeJson(paramDesc.value()))
                                .append("\"");
                    }
                    schema.append("}");
                }

                schema.append("},\"required\":[");
                for (int i = 0; i < parameters.length; i++) {
                    if (i > 0) {
                        schema.append(",");
                    }
                    schema.append("\"").append(getParameterName(parameters[i], i))
                            .append("\"");
                }
                schema.append("]}");

                return schema.toString();
            }

            @Override
            public String execute(String arguments) {
                try {
                    // Parse arguments
                    Object[] args = parseArguments(method, arguments);

                    
                    final AtomicReference<Object> resultRef = new AtomicReference<>();
                    final AtomicReference<Exception> exceptionRef = new AtomicReference<>();

                    ui.access(() -> {
                        try {
                            Object result = method.invoke(toolObject, args);
                            resultRef.set(result);
                        } catch (Exception e) {
                            exceptionRef.set(e);
                        }
                    });

                    // Check if an exception occurred during execution
                    if (exceptionRef.get() != null) {
                        throw exceptionRef.get();
                    }

                    Object result = resultRef.get();
                    return result != null ? result.toString() : "";
                   
                } catch (Exception e) {
                    return "Error executing tool: " + e.getMessage();
                }
            }
        };
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
        return null;
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
        protected List<Object> toolObjects = new ArrayList<>();

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
         * Sets the objects containing {@link Tool}-annotated methods that will
         * be available to the LLM.
         * <p>
         * Methods annotated with {@link Tool} will be automatically discovered
         * and converted into LLM tools. Parameters can be documented using
         * {@link ParameterDescription} annotations.
         * </p>
         *
         * @param toolObjects
         *            the objects containing tool methods
         * @return this builder
         */
        public B setTools(Object... toolObjects) {
            this.toolObjects.clear();
            if (toolObjects != null) {
                this.toolObjects.addAll(List.of(toolObjects));
            }
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
            orchestrator.setToolObjects(toolObjects);

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
        }

        /**
         * Builds the orchestrator.
         *
         * @return the configured orchestrator
         */
        public abstract T build();
    }
}
