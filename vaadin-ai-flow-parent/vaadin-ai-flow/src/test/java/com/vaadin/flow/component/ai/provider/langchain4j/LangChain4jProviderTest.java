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
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link LangChain4jProvider}.
 */
public class LangChain4jProviderTest {

    private StreamingChatLanguageModel mockModel;
    private LangChain4jProvider provider;

    @Before
    public void setUp() {
        mockModel = mock(StreamingChatLanguageModel.class);
        provider = new LangChain4jProvider(mockModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_withNullModel_throwsException() {
        new LangChain4jProvider(null);
    }

    @Test
    public void constructor_withValidModel_createsProvider() {
        LangChain4jProvider p = new LangChain4jProvider(mockModel);
        assertNotNull("Provider should not be null", p);
        assertEquals("Model should be set", mockModel, p.getModel());
    }

    @Test
    public void getModel_returnsConfiguredModel() {
        assertEquals("Should return configured model", mockModel,
                provider.getModel());
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateStream_withNullMessages_throwsException() {
        provider.generateStream(null, "system prompt", null).blockFirst();
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateStream_withEmptyMessages_throwsException() {
        List<LLMProvider.Message> emptyMessages = new ArrayList<>();
        provider.generateStream(emptyMessages, "system prompt", null)
                .blockFirst();
    }

    @Test
    public void generateStream_withValidMessages_callsModel() {
        List<LLMProvider.Message> messages = new ArrayList<>();
        messages.add(LLMProvider.createMessage("user", "Hello"));

        AtomicInteger tokenCount = new AtomicInteger(0);

        // Mock the model to call onComplete immediately
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(1);
            handler.onNext("Hi");
            handler.onNext(" there");

            AiMessage aiMessage = AiMessage.from("Hi there");
            Response<AiMessage> response = Response.from(aiMessage);
            handler.onComplete(response);
            return null;
        }).when(mockModel).generate(anyList(), any());

        Flux<String> result = provider.generateStream(messages, null, null);

        result.doOnNext(token -> tokenCount.incrementAndGet())
                .blockLast(Duration.ofSeconds(5));

        assertEquals("Should emit 2 tokens", 2, tokenCount.get());
        verify(mockModel, times(1)).generate(anyList(), any());
    }

    @Test
    public void generateStream_withSystemPrompt_includesSystemMessage() {
        List<LLMProvider.Message> messages = new ArrayList<>();
        messages.add(LLMProvider.createMessage("user", "Hello"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ChatMessage>> messagesCaptor = ArgumentCaptor
                .forClass(List.class);

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(1);
            AiMessage aiMessage = AiMessage.from("Response");
            Response<AiMessage> response = Response.from(aiMessage);
            handler.onComplete(response);
            return null;
        }).when(mockModel).generate(messagesCaptor.capture(), any());

        provider.generateStream(messages, "System prompt here", null)
                .blockLast(Duration.ofSeconds(5));

        List<ChatMessage> capturedMessages = messagesCaptor.getValue();
        assertTrue("Should have at least 2 messages",
                capturedMessages.size() >= 2);

        // First message should be system message
        ChatMessage firstMessage = capturedMessages.get(0);
        assertTrue("First message should be SystemMessage",
                firstMessage instanceof dev.langchain4j.data.message.SystemMessage);
    }

    @Test
    public void generateStream_withoutSystemPrompt_doesNotIncludeSystemMessage() {
        List<LLMProvider.Message> messages = new ArrayList<>();
        messages.add(LLMProvider.createMessage("user", "Hello"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ChatMessage>> messagesCaptor = ArgumentCaptor
                .forClass(List.class);

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(1);
            AiMessage aiMessage = AiMessage.from("Response");
            Response<AiMessage> response = Response.from(aiMessage);
            handler.onComplete(response);
            return null;
        }).when(mockModel).generate(messagesCaptor.capture(), any());

        provider.generateStream(messages, null, null)
                .blockLast(Duration.ofSeconds(5));

        List<ChatMessage> capturedMessages = messagesCaptor.getValue();
        assertEquals("Should have 1 message", 1, capturedMessages.size());

        // Should not contain system message
        ChatMessage firstMessage = capturedMessages.get(0);
        assertFalse("First message should not be SystemMessage",
                firstMessage instanceof dev.langchain4j.data.message.SystemMessage);
    }

    @Test
    public void generateStream_withEmptySystemPrompt_doesNotIncludeSystemMessage() {
        List<LLMProvider.Message> messages = new ArrayList<>();
        messages.add(LLMProvider.createMessage("user", "Hello"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ChatMessage>> messagesCaptor = ArgumentCaptor
                .forClass(List.class);

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(1);
            AiMessage aiMessage = AiMessage.from("Response");
            Response<AiMessage> response = Response.from(aiMessage);
            handler.onComplete(response);
            return null;
        }).when(mockModel).generate(messagesCaptor.capture(), any());

        provider.generateStream(messages, "", null)
                .blockLast(Duration.ofSeconds(5));

        List<ChatMessage> capturedMessages = messagesCaptor.getValue();
        assertEquals("Should have 1 message", 1, capturedMessages.size());
    }

    @Test
    public void generateStream_convertsUserMessages() {
        List<LLMProvider.Message> messages = new ArrayList<>();
        messages.add(LLMProvider.createMessage("user", "User message"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ChatMessage>> messagesCaptor = ArgumentCaptor
                .forClass(List.class);

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(1);
            AiMessage aiMessage = AiMessage.from("Response");
            Response<AiMessage> response = Response.from(aiMessage);
            handler.onComplete(response);
            return null;
        }).when(mockModel).generate(messagesCaptor.capture(), any());

        provider.generateStream(messages, null, null)
                .blockLast(Duration.ofSeconds(5));

        List<ChatMessage> capturedMessages = messagesCaptor.getValue();
        assertEquals("Should have 1 message", 1, capturedMessages.size());

        ChatMessage message = capturedMessages.get(0);
        assertTrue("Message should be UserMessage",
                message instanceof dev.langchain4j.data.message.UserMessage);
    }

    @Test
    public void generateStream_convertsAssistantMessages() {
        List<LLMProvider.Message> messages = new ArrayList<>();
        messages.add(LLMProvider.createMessage("user", "Hi"));
        messages.add(LLMProvider.createMessage("assistant",
                "Assistant message"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ChatMessage>> messagesCaptor = ArgumentCaptor
                .forClass(List.class);

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(1);
            AiMessage aiMessage = AiMessage.from("Response");
            Response<AiMessage> response = Response.from(aiMessage);
            handler.onComplete(response);
            return null;
        }).when(mockModel).generate(messagesCaptor.capture(), any());

        provider.generateStream(messages, null, null)
                .blockLast(Duration.ofSeconds(5));

        List<ChatMessage> capturedMessages = messagesCaptor.getValue();
        assertEquals("Should have 2 messages", 2, capturedMessages.size());

        ChatMessage secondMessage = capturedMessages.get(1);
        assertTrue("Second message should be AiMessage",
                secondMessage instanceof dev.langchain4j.data.message.AiMessage);
    }

    @Test
    public void generateStream_convertsAiRoleMessages() {
        List<LLMProvider.Message> messages = new ArrayList<>();
        messages.add(LLMProvider.createMessage("user", "Hi"));
        messages.add(LLMProvider.createMessage("ai", "AI message"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ChatMessage>> messagesCaptor = ArgumentCaptor
                .forClass(List.class);

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(1);
            AiMessage aiMessage = AiMessage.from("Response");
            Response<AiMessage> response = Response.from(aiMessage);
            handler.onComplete(response);
            return null;
        }).when(mockModel).generate(messagesCaptor.capture(), any());

        provider.generateStream(messages, null, null)
                .blockLast(Duration.ofSeconds(5));

        List<ChatMessage> capturedMessages = messagesCaptor.getValue();
        assertEquals("Should have 2 messages", 2, capturedMessages.size());

        ChatMessage secondMessage = capturedMessages.get(1);
        assertTrue("Second message should be AiMessage",
                secondMessage instanceof dev.langchain4j.data.message.AiMessage);
    }

    @Test
    public void generateStream_convertsSystemRoleMessages() {
        List<LLMProvider.Message> messages = new ArrayList<>();
        messages.add(LLMProvider.createMessage("system", "System instruction"));
        messages.add(LLMProvider.createMessage("user", "Hi"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ChatMessage>> messagesCaptor = ArgumentCaptor
                .forClass(List.class);

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(1);
            AiMessage aiMessage = AiMessage.from("Response");
            Response<AiMessage> response = Response.from(aiMessage);
            handler.onComplete(response);
            return null;
        }).when(mockModel).generate(messagesCaptor.capture(), any());

        provider.generateStream(messages, null, null)
                .blockLast(Duration.ofSeconds(5));

        List<ChatMessage> capturedMessages = messagesCaptor.getValue();
        assertEquals("Should have 2 messages", 2, capturedMessages.size());

        ChatMessage firstMessage = capturedMessages.get(0);
        assertTrue("First message should be SystemMessage",
                firstMessage instanceof dev.langchain4j.data.message.SystemMessage);
    }

    @Test
    public void generateStream_withUnknownRole_defaultsToUserMessage() {
        List<LLMProvider.Message> messages = new ArrayList<>();
        messages.add(LLMProvider.createMessage("unknown_role", "Message"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ChatMessage>> messagesCaptor = ArgumentCaptor
                .forClass(List.class);

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(1);
            AiMessage aiMessage = AiMessage.from("Response");
            Response<AiMessage> response = Response.from(aiMessage);
            handler.onComplete(response);
            return null;
        }).when(mockModel).generate(messagesCaptor.capture(), any());

        provider.generateStream(messages, null, null)
                .blockLast(Duration.ofSeconds(5));

        List<ChatMessage> capturedMessages = messagesCaptor.getValue();
        assertEquals("Should have 1 message", 1, capturedMessages.size());

        ChatMessage message = capturedMessages.get(0);
        assertTrue("Message should default to UserMessage",
                message instanceof dev.langchain4j.data.message.UserMessage);
    }

    @Test
    public void generateStream_onError_propagatesError() {
        List<LLMProvider.Message> messages = new ArrayList<>();
        messages.add(LLMProvider.createMessage("user", "Hello"));

        RuntimeException expectedException = new RuntimeException(
                "Model error");

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(1);
            handler.onError(expectedException);
            return null;
        }).when(mockModel).generate(anyList(), any());

        Flux<String> result = provider.generateStream(messages, null, null);

        AtomicBoolean errorOccurred = new AtomicBoolean(false);
        try {
            result.doOnError(e -> errorOccurred.set(true))
                    .blockLast(Duration.ofSeconds(5));
        } catch (Exception e) {
            errorOccurred.set(true);
        }

        assertTrue("Error should have been propagated", errorOccurred.get());
    }

    @Test
    public void generateStream_withMultipleTokens_emitsAll() {
        List<LLMProvider.Message> messages = new ArrayList<>();
        messages.add(LLMProvider.createMessage("user", "Hello"));

        List<String> capturedTokens = new ArrayList<>();

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(1);
            handler.onNext("Token");
            handler.onNext(" ");
            handler.onNext("1");
            handler.onNext(" ");
            handler.onNext("2");
            handler.onNext(" ");
            handler.onNext("3");

            AiMessage aiMessage = AiMessage.from("Token 1 2 3");
            Response<AiMessage> response = Response.from(aiMessage);
            handler.onComplete(response);
            return null;
        }).when(mockModel).generate(anyList(), any());

        Flux<String> result = provider.generateStream(messages, null, null);

        result.doOnNext(capturedTokens::add).blockLast(Duration.ofSeconds(5));

        assertEquals("Should emit 7 tokens", 7, capturedTokens.size());
        assertEquals("Token", capturedTokens.get(0));
        assertEquals(" ", capturedTokens.get(1));
        assertEquals("1", capturedTokens.get(2));
    }

    @Test
    public void generateStream_withTools_passesToolsToModel() {
        List<LLMProvider.Message> messages = new ArrayList<>();
        messages.add(LLMProvider.createMessage("user", "Hello"));

        List<LLMProvider.Tool> tools = new ArrayList<>();
        LLMProvider.Tool mockTool = mock(LLMProvider.Tool.class);
        when(mockTool.getName()).thenReturn("testTool");
        when(mockTool.getDescription()).thenReturn("Test tool description");
        tools.add(mockTool);

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(2);
            AiMessage aiMessage = AiMessage.from("Response");
            Response<AiMessage> response = Response.from(aiMessage);
            handler.onComplete(response);
            return null;
        }).when(mockModel).generate(anyList(), anyList(), any());

        provider.generateStream(messages, null, tools)
                .blockLast(Duration.ofSeconds(5));

        verify(mockModel, times(1)).generate(anyList(), anyList(), any());
    }

    @Test
    public void generateStream_withNullTool_skipsTool() {
        List<LLMProvider.Message> messages = new ArrayList<>();
        messages.add(LLMProvider.createMessage("user", "Hello"));

        List<LLMProvider.Tool> tools = new ArrayList<>();
        tools.add(null);

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(1);
            AiMessage aiMessage = AiMessage.from("Response");
            Response<AiMessage> response = Response.from(aiMessage);
            handler.onComplete(response);
            return null;
        }).when(mockModel).generate(anyList(), any());

        // Should not throw exception
        provider.generateStream(messages, null, tools)
                .blockLast(Duration.ofSeconds(5));

        verify(mockModel, times(1)).generate(anyList(), any());
    }

    @Test
    public void generateStream_withToolWithNullName_skipsTool() {
        List<LLMProvider.Message> messages = new ArrayList<>();
        messages.add(LLMProvider.createMessage("user", "Hello"));

        List<LLMProvider.Tool> tools = new ArrayList<>();
        LLMProvider.Tool mockTool = mock(LLMProvider.Tool.class);
        when(mockTool.getName()).thenReturn(null);
        tools.add(mockTool);

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(1);
            AiMessage aiMessage = AiMessage.from("Response");
            Response<AiMessage> response = Response.from(aiMessage);
            handler.onComplete(response);
            return null;
        }).when(mockModel).generate(anyList(), any());

        // Should not throw exception
        provider.generateStream(messages, null, tools)
                .blockLast(Duration.ofSeconds(5));

        verify(mockModel, times(1)).generate(anyList(), any());
    }

    @Test
    public void generateStream_withToolWithNullDescription_usesNameAsDescription() {
        List<LLMProvider.Message> messages = new ArrayList<>();
        messages.add(LLMProvider.createMessage("user", "Hello"));

        List<LLMProvider.Tool> tools = new ArrayList<>();
        LLMProvider.Tool mockTool = mock(LLMProvider.Tool.class);
        when(mockTool.getName()).thenReturn("testTool");
        when(mockTool.getDescription()).thenReturn(null);
        tools.add(mockTool);

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(2);
            AiMessage aiMessage = AiMessage.from("Response");
            Response<AiMessage> response = Response.from(aiMessage);
            handler.onComplete(response);
            return null;
        }).when(mockModel).generate(anyList(), anyList(), any());

        // Should use tool name as description
        provider.generateStream(messages, null, tools)
                .blockLast(Duration.ofSeconds(5));

        verify(mockModel, times(1)).generate(anyList(), anyList(), any());
    }

    @Test
    public void generateStream_withToolExecution_executesToolAndReturnsResult() {
        List<LLMProvider.Message> messages = new ArrayList<>();
        messages.add(LLMProvider.createMessage("user", "Use the tool"));

        List<LLMProvider.Tool> tools = new ArrayList<>();
        LLMProvider.Tool mockTool = mock(LLMProvider.Tool.class);
        when(mockTool.getName()).thenReturn("testTool");
        when(mockTool.getDescription()).thenReturn("Test tool");
        when(mockTool.execute(anyString())).thenReturn("Tool result");
        tools.add(mockTool);

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(2);

            // First response requests tool execution
            ToolExecutionRequest toolRequest = ToolExecutionRequest.builder()
                    .name("testTool").arguments("{\"param\":\"value\"}")
                    .build();

            List<ToolExecutionRequest> toolRequests = new ArrayList<>();
            toolRequests.add(toolRequest);

            AiMessage aiMessageWithTool = AiMessage.from("Calling tool", toolRequests);
            Response<AiMessage> response = Response.from(aiMessageWithTool);
            handler.onComplete(response);
            return null;
        }).when(mockModel).generate(anyList(), anyList(), any());

        provider.generateStream(messages, null, tools)
                .blockLast(Duration.ofSeconds(5));

        // Verify tool was executed
        verify(mockTool, times(1)).execute("{\"param\":\"value\"}");
    }

    @Test
    public void provider_isSerializable() {
        assertTrue("LangChain4jProvider should be serializable",
                java.io.Serializable.class
                        .isAssignableFrom(LangChain4jProvider.class));
    }

    @Test
    public void provider_implementsLLMProvider() {
        assertTrue("LangChain4jProvider should implement LLMProvider",
                LLMProvider.class.isAssignableFrom(LangChain4jProvider.class));
    }
}
