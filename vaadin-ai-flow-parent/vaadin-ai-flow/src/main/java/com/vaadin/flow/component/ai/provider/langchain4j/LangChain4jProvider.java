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
package com.vaadin.flow.component.ai.provider.langchain4j;

import com.vaadin.flow.component.ai.provider.LLMProvider;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LangChain4j implementation of LLMProvider.
 * <p>
 * Handles conversation memory internally using LangChain4j's ChatMemory. Each
 * provider instance maintains its own conversation memory, so multiple
 * provider instances can be used for different conversations.
 * </p>
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
 *         .apiKey(System.getenv("OPENAI_API_KEY")).modelName("gpt-4")
 *         .build();
 * LLMProvider provider = new LangChain4jProvider(model);
 * provider.setSystemPrompt("You are a helpful assistant.");
 *
 * LLMRequest request = LLMRequest.of("Hello, how are you?");
 * Flux&lt;String&gt; response = provider.stream(request);
 * </pre>
 *
 * @author Vaadin Ltd
 */
public class LangChain4jProvider implements LLMProvider {

    private final StreamingChatLanguageModel model;
    private final ChatMemory chatMemory;
    private String defaultSystemPrompt;

    /**
     * Creates a new LangChain4j provider with the specified streaming chat
     * model.
     *
     * @param model
     *            the LangChain4j streaming chat model to use
     */
    public LangChain4jProvider(StreamingChatLanguageModel model) {
        this(model, 10);
    }

    /**
     * Creates a new LangChain4j provider with the specified streaming chat
     * model and maximum message history.
     *
     * @param model
     *            the LangChain4j streaming chat model to use
     * @param maxMessages
     *            the maximum number of messages to keep in conversation memory
     */
    public LangChain4jProvider(StreamingChatLanguageModel model,
            int maxMessages) {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        this.model = model;
        this.chatMemory = MessageWindowChatMemory.withMaxMessages(maxMessages);
    }

    @Override
    public void setSystemPrompt(String systemPrompt) {
        this.defaultSystemPrompt = systemPrompt;
    }

    @Override
    public Flux<String> stream(LLMRequest request) {
        if (request == null || request.userMessage() == null) {
            throw new IllegalArgumentException(
                    "Request and user message cannot be null");
        }

        return Flux.create(sink -> {
            try {
                // Add user message with attachments to memory
                chatMemory.add(buildUserMessage(request));

                // Start the chat with tool execution loop
                executeChatWithTools(request, chatMemory, sink);
            } catch (Exception e) {
                sink.error(e);
            }
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    /**
     * Executes the chat with tool calling support. If the AI requests tool
     * execution, it will execute the tools and make a follow-up request with
     * the results.
     */
    private void executeChatWithTools(LLMRequest request, ChatMemory chatMemory,
            FluxSink<String> sink) {
        List<ChatMessage> messages = buildMessages(request, chatMemory);

        // Parse tools from request
        Tool[] tools = request.tools() != null ? request.tools()
                : new Tool[0];
        Map<String, Tool> toolMap = new HashMap<>();
        List<ToolSpecification> toolSpecifications = new ArrayList<>();

        if (tools.length > 0) {
            for (Tool tool : tools) {
                // Convert generic Tool to LangChain4j ToolSpecification
                ToolSpecification.Builder specBuilder = ToolSpecification
                        .builder().name(tool.getName())
                        .description(tool.getDescription());

                // Add parameters schema if available
                String parametersSchema = tool.getParametersSchema();
                if (parametersSchema != null && !parametersSchema.isEmpty()) {
                    // For LangChain4j, we need to parse the schema and add
                    // parameters
                    // For now, we'll rely on the description containing
                    // parameter info
                }

                toolSpecifications.add(specBuilder.build());
                toolMap.put(tool.getName(), tool);
            }
        }

        // Generate the streaming response
        try {
            if (toolSpecifications.isEmpty()) {
                model.generate(messages,
                        new StreamingResponseHandler<AiMessage>() {
                            @Override
                            public void onNext(String token) {
                                sink.next(token);
                            }

                            @Override
                            public void onComplete(Response<AiMessage> response) {
                                AiMessage aiMessage = response.content();
                                if (aiMessage != null) {
                                    chatMemory.add(aiMessage);
                                }
                                sink.complete();
                            }

                            @Override
                            public void onError(Throwable error) {
                                sink.error(error);
                            }
                        });
            } else {
                model.generate(messages, toolSpecifications,
                        new StreamingResponseHandler<AiMessage>() {
                            @Override
                            public void onNext(String token) {
                                sink.next(token);
                            }

                            @Override
                            public void onComplete(Response<AiMessage> response) {
                                AiMessage aiMessage = response.content();
                                if (aiMessage != null) {
                                    chatMemory.add(aiMessage);

                                    // Check if the AI wants to execute tools
                                    if (aiMessage.hasToolExecutionRequests()) {
                                        for (ToolExecutionRequest toolExecRequest : aiMessage
                                                .toolExecutionRequests()) {
                                            Tool tool = toolMap
                                                    .get(toolExecRequest.name());
                                            String result = tool != null
                                                    ? tool.execute(
                                                            toolExecRequest
                                                                    .arguments())
                                                    : "Tool not found: "
                                                            + toolExecRequest
                                                                    .name();
                                            chatMemory.add(
                                                    ToolExecutionResultMessage.from(
                                                            toolExecRequest,
                                                            result));
                                        }
                                        executeChatWithTools(request, chatMemory,
                                                sink);
                                    } else {
                                        sink.complete();
                                    }
                                } else {
                                    sink.complete();
                                }
                            }

                            @Override
                            public void onError(Throwable error) {
                                sink.error(error);
                            }
                        });
            }
        } catch (Exception e) {
            sink.error(e);
        }
    }

    /**
     * Builds the message list for the chat request, including system prompt
     * and conversation history.
     */
    private List<ChatMessage> buildMessages(LLMRequest request,
            ChatMemory chatMemory) {
        List<ChatMessage> messages = new ArrayList<>();

        // Use request system prompt if provided, otherwise fall back to
        // default
        String systemPrompt = request.systemPrompt() != null
                ? request.systemPrompt()
                : defaultSystemPrompt;

        // Add system message
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(SystemMessage.from(systemPrompt));
        }

        // Add chat history from memory (which already includes the current
        // user message with attachments)
        messages.addAll(chatMemory.messages());

        return messages;
    }

    /**
     * Builds a UserMessage from the request, including text content and
     * attachments.
     */
    private UserMessage buildUserMessage(LLMRequest request) {
        List<dev.langchain4j.data.message.Content> contents = new ArrayList<>();

        // Add text content
        contents.add(TextContent.from(request.userMessage()));

        // Add attachments
        for (Attachment attachment : request.attachments()) {
            if (attachment.contentType().startsWith("image/")) {
                // Convert byte[] to base64 data URL
                String base64 = java.util.Base64.getEncoder()
                        .encodeToString(attachment.data());
                String dataUrl = "data:" + attachment.contentType()
                        + ";base64," + base64;
                contents.add(ImageContent.from(dataUrl,
                        ImageContent.DetailLevel.AUTO));
            } else if (attachment.contentType().contains("text")
                    || attachment.contentType().contains("pdf")) {
                // For text/pdf, add as text content (simplified approach)
                String textContent = new String(attachment.data());
                contents.add(TextContent
                        .from("\n<attachment filename=\""
                                + attachment.fileName() + "\">\n" + textContent
                                + "\n</attachment>\n"));
            }
        }

        return UserMessage.from(contents);
    }

    /**
     * Gets the underlying LangChain4j streaming chat model.
     *
     * @return the streaming chat model
     */
    public StreamingChatLanguageModel getModel() {
        return model;
    }

    /**
     * Clears the conversation memory for this provider instance.
     */
    public void clearConversation() {
        chatMemory.clear();
    }
}
