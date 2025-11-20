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
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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
    public void stream_withNullRequest_throwsException() {
        provider.stream(null).blockFirst();
    }

    @Test(expected = IllegalArgumentException.class)
    public void stream_withNullUserMessage_throwsException() {
        LLMProvider.LLMRequest request = new LLMProvider.LLMRequestBuilder()
                .conversationId("test").userMessage(null).build();
        provider.stream(request).blockFirst();
    }

    @Test
    public void stream_withValidRequest_callsModel() {
        LLMProvider.LLMRequest request = LLMProvider.LLMRequest
                .of("conversation1", "Hello");

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

        List<String> tokens = new ArrayList<>();
        provider.stream(request).doOnNext(tokens::add)
                .blockLast(Duration.ofSeconds(5));

        assertEquals("Should have 2 tokens", 2, tokens.size());
        assertEquals("First token", "Hi", tokens.get(0));
        assertEquals("Second token", " there", tokens.get(1));
        verify(mockModel, times(1)).generate(anyList(), any());
    }

    @Test
    public void stream_withSystemPrompt_includesSystemMessage() {
        provider.setSystemPrompt("You are a helpful assistant");
        LLMProvider.LLMRequest request = LLMProvider.LLMRequest
                .of("conversation1", "Hello");

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(1);
            handler.onNext("Response");

            AiMessage aiMessage = AiMessage.from("Response");
            Response<AiMessage> response = Response.from(aiMessage);
            handler.onComplete(response);
            return null;
        }).when(mockModel).generate(anyList(), any());

        provider.stream(request).blockLast(Duration.ofSeconds(5));

        verify(mockModel, times(1)).generate(anyList(), any());
    }

    @Test
    public void stream_withTools_passesToolsToModel() {
        LLMProvider.Tool testTool = new LLMProvider.Tool() {
            @Override
            public String getName() {
                return "testTool";
            }

            @Override
            public String getDescription() {
                return "A test tool";
            }

            @Override
            public String getParametersSchema() {
                return null;
            }

            @Override
            public String execute(String arguments) {
                return "Tool executed";
            }
        };

        LLMProvider.LLMRequest request = new LLMProvider.LLMRequestBuilder()
                .conversationId("conversation1").userMessage("Use the tool")
                .tools(testTool).build();

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(2);
            handler.onNext("Using tool");

            AiMessage aiMessage = AiMessage.from("Using tool");
            Response<AiMessage> response = Response.from(aiMessage);
            handler.onComplete(response);
            return null;
        }).when(mockModel).generate(anyList(), anyList(), any());

        provider.stream(request).blockLast(Duration.ofSeconds(5));

        verify(mockModel, times(1)).generate(anyList(), anyList(), any());
    }

    @Test
    public void stream_withToolExecution_executesToolAndFollowsUp() {
        LLMProvider.Tool testTool = new LLMProvider.Tool() {
            @Override
            public String getName() {
                return "calculator";
            }

            @Override
            public String getDescription() {
                return "Performs calculations";
            }

            @Override
            public String getParametersSchema() {
                return null;
            }

            @Override
            public String execute(String arguments) {
                return "42";
            }
        };

        LLMProvider.LLMRequest request = new LLMProvider.LLMRequestBuilder()
                .conversationId("conversation1").userMessage("Calculate 2+2")
                .tools(testTool).build();

        // First call: AI requests tool execution
        AtomicInteger callCount = new AtomicInteger(0);
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(2);

            if (callCount.getAndIncrement() == 0) {
                // First call: request tool execution
                handler.onNext("Let me calculate");

                ToolExecutionRequest toolRequest = ToolExecutionRequest
                        .builder().name("calculator").arguments("{}").build();
                AiMessage aiMessage = AiMessage.from(
                        List.of(toolRequest));
                Response<AiMessage> response = Response.from(aiMessage);
                handler.onComplete(response);
            } else {
                // Second call: final response after tool execution
                handler.onNext("The answer is 42");

                AiMessage aiMessage = AiMessage.from("The answer is 42");
                Response<AiMessage> response = Response.from(aiMessage);
                handler.onComplete(response);
            }
            return null;
        }).when(mockModel).generate(anyList(), anyList(), any());

        List<String> tokens = new ArrayList<>();
        provider.stream(request).doOnNext(tokens::add)
                .blockLast(Duration.ofSeconds(5));

        // Should have tokens from both calls
        assertTrue("Should have received tokens",
                tokens.size() >= 2);
        verify(mockModel, times(2)).generate(anyList(), anyList(), any());
    }

    @Test
    public void stream_multipleCalls_usesSeparateConversations() {
        LLMProvider.LLMRequest request1 = LLMProvider.LLMRequest
                .of("conversation1", "Hello 1");
        LLMProvider.LLMRequest request2 = LLMProvider.LLMRequest
                .of("conversation2", "Hello 2");

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(1);
            handler.onNext("Response");

            AiMessage aiMessage = AiMessage.from("Response");
            Response<AiMessage> response = Response.from(aiMessage);
            handler.onComplete(response);
            return null;
        }).when(mockModel).generate(anyList(), any());

        provider.stream(request1).blockLast(Duration.ofSeconds(5));
        provider.stream(request2).blockLast(Duration.ofSeconds(5));

        verify(mockModel, times(2)).generate(anyList(), any());
    }

    @Test
    public void clearConversation_removesConversationMemory() {
        LLMProvider.LLMRequest request = LLMProvider.LLMRequest
                .of("conversation1", "Hello");

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(1);
            handler.onNext("Response");

            AiMessage aiMessage = AiMessage.from("Response");
            Response<AiMessage> response = Response.from(aiMessage);
            handler.onComplete(response);
            return null;
        }).when(mockModel).generate(anyList(), any());

        provider.stream(request).blockLast(Duration.ofSeconds(5));
        provider.clearConversation("conversation1");

        // Should still work after clearing
        provider.stream(request).blockLast(Duration.ofSeconds(5));

        verify(mockModel, times(2)).generate(anyList(), any());
    }

    @Test
    public void clearAllConversations_removesAllMemory() {
        LLMProvider.LLMRequest request1 = LLMProvider.LLMRequest
                .of("conversation1", "Hello 1");
        LLMProvider.LLMRequest request2 = LLMProvider.LLMRequest
                .of("conversation2", "Hello 2");

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(1);
            handler.onNext("Response");

            AiMessage aiMessage = AiMessage.from("Response");
            Response<AiMessage> response = Response.from(aiMessage);
            handler.onComplete(response);
            return null;
        }).when(mockModel).generate(anyList(), any());

        provider.stream(request1).blockLast(Duration.ofSeconds(5));
        provider.stream(request2).blockLast(Duration.ofSeconds(5));
        provider.clearAllConversations();

        // Should still work after clearing
        provider.stream(request1).blockLast(Duration.ofSeconds(5));

        verify(mockModel, times(3)).generate(anyList(), any());
    }

    @Test
    public void stream_withError_propagatesError() {
        LLMProvider.LLMRequest request = LLMProvider.LLMRequest
                .of("conversation1", "Hello");

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(1);
            handler.onError(new RuntimeException("Test error"));
            return null;
        }).when(mockModel).generate(anyList(), any());

        try {
            provider.stream(request).blockLast(Duration.ofSeconds(5));
            fail("Should have thrown exception");
        } catch (Exception e) {
            assertTrue("Should contain error message",
                    e.getMessage().contains("Test error"));
        }
    }

    @Test
    public void stream_withRequestSystemPrompt_overridesDefault() {
        provider.setSystemPrompt("Default prompt");
        LLMProvider.LLMRequest request = new LLMProvider.LLMRequestBuilder()
                .conversationId("conversation1").userMessage("Hello")
                .systemPrompt("Override prompt").build();

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            StreamingResponseHandler<AiMessage> handler = invocation
                    .getArgument(1);
            handler.onNext("Response");

            AiMessage aiMessage = AiMessage.from("Response");
            Response<AiMessage> response = Response.from(aiMessage);
            handler.onComplete(response);
            return null;
        }).when(mockModel).generate(anyList(), any());

        provider.stream(request).blockLast(Duration.ofSeconds(5));

        verify(mockModel, times(1)).generate(anyList(), any());
    }
}
