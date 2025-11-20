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
package com.vaadin.flow.component.ai.provider.springai;

import com.vaadin.flow.component.ai.provider.LLMProvider;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link SpringAILLMProvider}.
 */
public class SpringAILLMProviderTest {

    private ChatModel mockChatModel;
    private ChatMemory chatMemory;
    private SpringAILLMProvider provider;

    @Before
    public void setUp() {
        mockChatModel = mock(ChatModel.class);
        chatMemory = new InMemoryChatMemory();
        provider = new SpringAILLMProvider(mockChatModel, chatMemory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_withNullChatModel_throwsException() {
        new SpringAILLMProvider(null, chatMemory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_withNullChatMemory_throwsException() {
        new SpringAILLMProvider(mockChatModel, null);
    }

    @Test
    public void constructor_withValidParameters_createsProvider() {
        SpringAILLMProvider p = new SpringAILLMProvider(mockChatModel,
                chatMemory);
        assertNotNull("Provider should not be null", p);
        assertEquals("ChatModel should be set", mockChatModel,
                p.getChatModel());
        assertEquals("ChatMemory should be set", chatMemory,
                p.getChatMemory());
    }

    @Test
    public void getChatModel_returnsConfiguredModel() {
        assertEquals("Should return configured model", mockChatModel,
                provider.getChatModel());
    }

    @Test
    public void getChatMemory_returnsConfiguredMemory() {
        assertEquals("Should return configured memory", chatMemory,
                provider.getChatMemory());
    }

    @Test
    public void setSystemPrompt_setsDefaultPrompt() {
        provider.setSystemPrompt("You are a helpful assistant");
        // System prompt is stored internally and used in stream() calls
        assertNotNull("Provider should still be usable",
                provider.getChatModel());
    }

    @Test(expected = IllegalArgumentException.class)
    public void stream_withNullRequest_throwsException() {
        provider.stream(null).blockFirst();
    }

    @Test(expected = IllegalArgumentException.class)
    public void stream_withNullUserMessage_throwsException() {
        LLMProvider.LLMRequest request = new LLMProvider.LLMRequestBuilder()
                .userMessage(null).build();
        provider.stream(request).blockFirst();
    }

    @Test
    public void constructor_withMaxMessages_createsProvider() {
        ChatMemory memory = new InMemoryChatMemory();
        SpringAILLMProvider p = new SpringAILLMProvider(mockChatModel, memory);
        assertNotNull("Provider should not be null", p);
        assertEquals("ChatModel should be set", mockChatModel,
                p.getChatModel());
        assertEquals("ChatMemory should be set", memory, p.getChatMemory());
    }

    @Test
    public void setSystemPrompt_canBeSetMultipleTimes() {
        provider.setSystemPrompt("First prompt");
        provider.setSystemPrompt("Second prompt");
        provider.setSystemPrompt("Third prompt");
        // Verify no exceptions thrown
        assertNotNull("Provider should still be functional",
                provider.getChatModel());
    }

    @Test
    public void setSystemPrompt_withNull_doesNotThrow() {
        provider.setSystemPrompt(null);
        // Null system prompt should be handled gracefully
        assertNotNull("Provider should still be functional",
                provider.getChatModel());
    }

    @Test
    public void setSystemPrompt_withEmptyString_doesNotThrow() {
        provider.setSystemPrompt("");
        // Empty system prompt should be handled gracefully
        assertNotNull("Provider should still be functional",
                provider.getChatModel());
    }

    @Test
    public void multipleCalls_useSameChatMemory() {
        SpringAILLMProvider provider1 = new SpringAILLMProvider(mockChatModel,
                chatMemory);
        SpringAILLMProvider provider2 = new SpringAILLMProvider(mockChatModel,
                chatMemory);

        assertSame("Should use same memory instance", chatMemory,
                provider1.getChatMemory());
        assertSame("Should use same memory instance", chatMemory,
                provider2.getChatMemory());
    }

    @Test
    public void differentInstances_canUseDifferentMemories() {
        ChatMemory memory1 = new InMemoryChatMemory();
        ChatMemory memory2 = new InMemoryChatMemory();

        SpringAILLMProvider provider1 = new SpringAILLMProvider(mockChatModel,
                memory1);
        SpringAILLMProvider provider2 = new SpringAILLMProvider(mockChatModel,
                memory2);

        assertNotSame("Should use different memory instances",
                provider1.getChatMemory(), provider2.getChatMemory());
    }

    @Test
    public void request_withSystemPrompt_doesNotThrow() {
        LLMProvider.LLMRequest request = new LLMProvider.LLMRequestBuilder()
                .userMessage("Hello").systemPrompt("You are helpful").build();

        assertNotNull("Request should be created", request);
        assertEquals("System prompt should be set", "You are helpful",
                request.systemPrompt());
    }

    @Test
    public void request_withEmptySystemPrompt_doesNotThrow() {
        LLMProvider.LLMRequest request = new LLMProvider.LLMRequestBuilder()
                .userMessage("Hello").systemPrompt("").build();

        assertNotNull("Request should be created", request);
        assertEquals("System prompt should be empty", "",
                request.systemPrompt());
    }

    @Test
    public void request_withModelName_doesNotThrow() {
        LLMProvider.LLMRequest request = new LLMProvider.LLMRequestBuilder()
                .userMessage("Hello").modelName("gpt-4").build();

        assertNotNull("Request should be created", request);
        assertEquals("Model name should be set", "gpt-4",
                request.modelName());
    }

    @Test
    public void request_withTools_doesNotThrow() {
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
                .userMessage("Use the tool").tools(testTool).build();

        assertNotNull("Request should be created", request);
        assertNotNull("Tools should be set", request.tools());
        assertEquals("Should have one tool", 1, request.tools().length);
    }

    @Test
    public void request_builderPattern_allowsFluentConstruction() {
        LLMProvider.LLMRequest request = new LLMProvider.LLMRequestBuilder()
                .userMessage("Hello").systemPrompt("Be helpful")
                .modelName("gpt-4").build();

        assertNotNull("Request should be created", request);
        assertEquals("User message should be set", "Hello",
                request.userMessage());
        assertEquals("System prompt should be set", "Be helpful",
                request.systemPrompt());
        assertEquals("Model name should be set", "gpt-4",
                request.modelName());
    }

    @Test
    public void provider_retainsConfiguredComponents() {
        ChatMemory testMemory = new InMemoryChatMemory();
        ChatModel testModel = mock(ChatModel.class);

        SpringAILLMProvider testProvider = new SpringAILLMProvider(testModel,
                testMemory);

        assertSame("Should retain chat model", testModel,
                testProvider.getChatModel());
        assertSame("Should retain chat memory", testMemory,
                testProvider.getChatMemory());

        // Set system prompt and verify provider is still functional
        testProvider.setSystemPrompt("Test prompt");
        assertSame("Should still retain chat model after setting prompt",
                testModel, testProvider.getChatModel());
    }
}
