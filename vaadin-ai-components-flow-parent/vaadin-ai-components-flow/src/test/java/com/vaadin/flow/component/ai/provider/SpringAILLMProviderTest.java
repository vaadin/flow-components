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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.annotation.Tool;

import com.vaadin.flow.component.PushConfiguration;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.common.ChatMessage;
import com.vaadin.flow.component.ai.provider.LLMProvider.LLMRequest;
import com.vaadin.flow.shared.communication.PushMode;

import reactor.core.publisher.Flux;

public class SpringAILLMProviderTest {

    private ChatModel mockChatModel;
    private SpringAILLMProvider provider;

    private UI ui;

    @Before
    public void setup() {
        mockChatModel = Mockito.mock(ChatModel.class);
        provider = new SpringAILLMProvider(mockChatModel);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
        ui = null;
    }

    @Test
    public void stream_withNullRequest_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class,
                () -> provider.stream(null).blockFirst());
    }

    @Test
    public void stream_withNullUserMessage_throwsNullPointerException() {
        var request = new TestLLMRequest(null, null, Collections.emptyList(),
                new Object[0]);
        Assert.assertThrows(NullPointerException.class,
                () -> provider.stream(request).blockFirst());
    }

    @Test
    public void constructor_withNullChatModel_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class,
                () -> new SpringAILLMProvider((ChatModel) null));
    }

    @Test
    public void constructor_withNullChatClient_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class,
                () -> new SpringAILLMProvider((ChatClient) null));
    }

    @Test
    public void constructor_withChatClient_nonStreaming_returnsResponse() {
        var chatClient = ChatClient.builder(mockChatModel).build();
        var chatClientProvider = new SpringAILLMProvider(chatClient);
        chatClientProvider.setStreaming(false);
        var request = createSimpleRequest("Hello");
        mockSimpleChat("Full response");

        var results = chatClientProvider.stream(request).collectList().block();

        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("Full response", results.getFirst());
    }

    @Test
    public void constructor_withChatClient_defaultConfig_returnsStreamedTokens() {
        var chatClient = ChatClient.builder(mockChatModel).build();
        var chatClientStreamingProvider = new SpringAILLMProvider(chatClient);
        var request = createSimpleRequest("Hello");
        var tokens = List.of("Hello", " ", "World");

        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.fromIterable(tokens.stream()
                        .map(this::mockSimpleChatResponse).toList()));

        var results = chatClientStreamingProvider.stream(request).collectList()
                .block();
        Assert.assertEquals(tokens, results);
    }

    @Test
    public void constructor_withChatClient_setNonStreaming_setStreaming_returnsStreamedTokens() {
        var chatClient = ChatClient.builder(mockChatModel).build();
        var chatClientStreamingProvider = new SpringAILLMProvider(chatClient);
        chatClientStreamingProvider.setStreaming(false);
        chatClientStreamingProvider.setStreaming(true);
        var request = createSimpleRequest("Hello");
        var tokens = List.of("Hello", " ", "World");

        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.fromIterable(tokens.stream()
                        .map(this::mockSimpleChatResponse).toList()));

        var results = chatClientStreamingProvider.stream(request).collectList()
                .block();
        Assert.assertEquals(tokens, results);
    }

    @Test
    public void stream_withNonStreamingModel_returnsResponse() {
        provider.setStreaming(false);
        var request = createSimpleRequest("Hello");
        mockSimpleChat("Full response");

        var results = provider.stream(request).collectList().block();

        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("Full response", results.getFirst());
    }

    @Test
    public void stream_chatModelThrowsException_propagatesError() {
        var request = createSimpleRequest("Hello");
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenThrow(new RuntimeException("API error"));
        Assert.assertThrows(RuntimeException.class,
                () -> provider.stream(request).blockFirst());
    }

    @Test
    public void stream_emptyTextResponse_returnsEmpty() {
        provider.setStreaming(false);
        var request = createSimpleRequest("Hello");
        mockSimpleChat("");
        var results = provider.stream(request).collectList().block();
        Assert.assertNotNull(results);
        Assert.assertTrue(results.isEmpty());
    }

    @Test
    public void stream_nullTextResponse_returnsEmpty() {
        provider.setStreaming(false);
        var request = createSimpleRequest("Hello");
        mockSimpleChat(null);
        var results = provider.stream(request).collectList().block();
        Assert.assertNotNull(results);
        Assert.assertTrue(results.isEmpty());
    }

    @Test
    public void stream_withSystemPromptInRequest_includesSystemMessage() {
        provider.setStreaming(false);
        var request = new TestLLMRequest("Hello", "You are a helpful assistant",
                Collections.emptyList(), new Object[0]);
        mockSimpleChat("Response");

        provider.stream(request).blockFirst();

        var hasSystemMessage = capturePrompt().getInstructions().stream()
                .anyMatch(SystemMessage.class::isInstance);
        Assert.assertTrue(hasSystemMessage);
    }

    @Test
    public void stream_withNullSystemPrompt_noSystemMessage() {
        provider.setStreaming(false);
        var request = createSimpleRequest("Hello");
        mockSimpleChat("Response");

        provider.stream(request).blockFirst();

        var hasSystemMessage = capturePrompt().getInstructions().stream()
                .anyMatch(SystemMessage.class::isInstance);
        Assert.assertFalse(hasSystemMessage);
    }

    @Test
    public void stream_withEmptySystemPrompt_noSystemMessage() {
        provider.setStreaming(false);
        var request = new TestLLMRequest("Hello", "   ",
                Collections.emptyList(), new Object[0]);
        mockSimpleChat("Response");

        provider.stream(request).blockFirst();

        var hasSystemMessage = capturePrompt().getInstructions().stream()
                .anyMatch(SystemMessage.class::isInstance);
        Assert.assertFalse(hasSystemMessage);
    }

    @Test
    public void stream_withNullAttachments_returnsResponse() {
        provider.setStreaming(false);
        var request = new TestLLMRequest("Hello", null, null, new Object[0]);
        mockSimpleChat("Hi");
        var result = provider.stream(request).blockFirst();
        Assert.assertEquals("Hi", result);
    }

    @Test
    public void stream_withNullAttachmentInList_throwsNullPointerException() {
        var attachment = new AIAttachment("test.txt", "text/plain",
                "Test".getBytes(StandardCharsets.UTF_8));
        var attachments = new ArrayList<AIAttachment>();
        attachments.add(attachment);
        attachments.add(null);

        var request = new TestLLMRequest("Hello", null, attachments,
                new Object[0]);
        mockSimpleChat("hi");

        Assert.assertThrows(NullPointerException.class,
                () -> provider.stream(request).blockFirst());
    }

    @Test
    public void stream_withUnsupportedAttachmentType_ignoresAttachment() {
        provider.setStreaming(false);
        var attachment = new AIAttachment("file.bin",
                "application/octet-stream", "data".getBytes());
        var request = new TestLLMRequest("Process this", null,
                List.of(attachment), new Object[0]);

        mockSimpleChat("Done");

        provider.stream(request).blockFirst();

        Mockito.verify(mockChatModel).call(Mockito.any(Prompt.class));
    }

    @Test
    public void stream_withPdfAttachment_handlesPdf() {
        provider.setStreaming(false);
        var pdfData = "PDF binary content".getBytes(StandardCharsets.UTF_8);
        var attachment = new AIAttachment("document.pdf", "application/pdf",
                pdfData);
        var request = new TestLLMRequest("Summarize this document", null,
                List.of(attachment), new Object[0]);

        mockSimpleChat("Summary");

        var result = provider.stream(request).blockFirst();
        Assert.assertEquals("Summary", result);

        Mockito.verify(mockChatModel).call(Mockito.any(Prompt.class));
    }

    @Test
    public void stream_withBinaryPdfData_handlesBinaryPdf() {
        provider.setStreaming(false);
        // Binary PDF data should be handled correctly
        var binaryPdfData = new byte[] { 0x25, 0x50, 0x44, 0x46, (byte) 0xFF,
                (byte) 0xFE, (byte) 0x00, (byte) 0x80 };
        var attachment = new AIAttachment("binary.pdf", "application/pdf",
                binaryPdfData);
        var request = new TestLLMRequest("Summarize", null, List.of(attachment),
                new Object[0]);

        mockSimpleChat("Summary");

        var result = provider.stream(request).blockFirst();
        Assert.assertEquals("Summary", result);

        Mockito.verify(mockChatModel).call(Mockito.any(Prompt.class));
    }

    @Test
    public void stream_withImageAttachment_processesImage() {
        provider.setStreaming(false);
        var imageData = "fake-image-data".getBytes();
        var attachment = new AIAttachment("test.png", "image/png", imageData);
        var request = new TestLLMRequest("Describe this image", null,
                List.of(attachment), new Object[0]);

        mockSimpleChat("It's a test");

        var result = provider.stream(request).blockFirst();
        Assert.assertEquals("It's a test", result);

        Mockito.verify(mockChatModel).call(Mockito.any(Prompt.class));
    }

    @Test
    public void stream_withTextAttachment_processesText() {
        provider.setStreaming(false);
        var textContent = "Test UTF-8: é à ü";
        var attachment = new AIAttachment("test.txt", "text/plain",
                textContent.getBytes(StandardCharsets.UTF_8));
        var request = new TestLLMRequest("Summarize this", null,
                List.of(attachment), new Object[0]);

        mockSimpleChat("Summary");

        var result = provider.stream(request).blockFirst();
        Assert.assertEquals("Summary", result);

        Mockito.verify(mockChatModel).call(Mockito.any(Prompt.class));
    }

    @Test
    public void stream_withStreamingModel_returnsStreamedTokens() {
        var request = createSimpleRequest("Hello");
        var tokens = List.of("Hello", " ", "World");

        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.fromIterable(tokens.stream()
                        .map(this::mockSimpleChatResponse).toList()));

        var results = provider.stream(request).collectList().block();
        Assert.assertEquals(tokens, results);
    }

    @Test
    public void stream_withSingleTool_toolsAreConfigured() {
        provider.setStreaming(false);
        var toolObject = new SampleToolsClass();
        var request = new TestLLMRequest("Get temperature", null,
                Collections.emptyList(), new Object[] { toolObject });
        mockSimpleChat("The temperature is 22°C");

        provider.stream(request).blockFirst();

        var chatOptions = capturePrompt().getOptions();
        Assert.assertNotNull(chatOptions);
        var toolCallbacks = ((ToolCallingChatOptions) chatOptions)
                .getToolCallbacks();
        Assert.assertNotNull(toolCallbacks);
        Assert.assertEquals(2, toolCallbacks.size());
    }

    @Test
    public void stream_withMultipleToolObjects_allToolsAreConfigured() {
        provider.setStreaming(false);
        var tool1 = new SampleToolsClass();
        var tool2 = new AnotherSampleToolsClass();
        var request = new TestLLMRequest("Get weather info", null,
                Collections.emptyList(), new Object[] { tool1, tool2 });
        mockSimpleChat("Weather info");

        provider.stream(request).blockFirst();

        var chatOptions = capturePrompt().getOptions();
        Assert.assertNotNull(chatOptions);
        var toolCallbacks = ((ToolCallingChatOptions) chatOptions)
                .getToolCallbacks();
        Assert.assertNotNull(toolCallbacks);
        Assert.assertEquals(3, toolCallbacks.size());
    }

    @Test
    public void stream_withEmptyToolsArray_noToolCallbacksConfigured() {
        provider.setStreaming(false);
        var request = new TestLLMRequest("Hello", null, Collections.emptyList(),
                new Object[0]);
        mockSimpleChat("Hi");

        provider.stream(request).blockFirst();

        var chatOptions = capturePrompt().getOptions();
        var noToolCallbacks = chatOptions == null
                || ((ToolCallingChatOptions) chatOptions).getToolCallbacks()
                        .isEmpty();
        Assert.assertTrue(noToolCallbacks);
    }

    @Test
    public void stream_withNullToolsArray_noToolCallbacksConfigured() {
        provider.setStreaming(false);
        var request = new TestLLMRequest("Hello", null, Collections.emptyList(),
                null);
        mockSimpleChat("Hi");

        provider.stream(request).blockFirst();

        var chatOptions = capturePrompt().getOptions();
        var noToolCallbacks = chatOptions == null
                || ((ToolCallingChatOptions) chatOptions).getToolCallbacks()
                        .isEmpty();
        Assert.assertTrue(noToolCallbacks);
    }

    @Test
    public void chatMemory_retainsHistory() {
        provider.setStreaming(false);
        var response1 = mockSimpleChatResponse("Response 1");
        var response2 = mockSimpleChatResponse("Response 2");
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(response1, response2);

        provider.stream(createSimpleRequest("First message")).blockFirst();
        provider.stream(createSimpleRequest("Second message")).blockFirst();

        var secondRequestMessages = getPromptCaptor(2).getAllValues().get(1)
                .getInstructions();
        Assert.assertEquals(3, secondRequestMessages.size());
    }

    @Test
    public void stream_preservesChatHistoryAcrossRequests() {
        provider.setStreaming(false);
        var response1 = mockSimpleChatResponse("Hi there");
        var response2 = mockSimpleChatResponse("I'm good");
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(response1, response2);

        provider.stream(createSimpleRequest("Hello")).blockFirst();
        provider.stream(createSimpleRequest("How are you?")).blockFirst();

        var allPrompts = getPromptCaptor(2).getAllValues();
        Assert.assertEquals("First call should have 1 user message", 1,
                allPrompts.get(0).getInstructions().size());
        Assert.assertEquals(
                "Second call should have 3 messages (user1, ai1, user2)", 3,
                allPrompts.get(1).getInstructions().size());
    }

    @Test
    public void stream_withMaxMessagesLimit_dropsOldestMessages() {
        provider.setStreaming(false);
        var requestCount = 20;

        // Each request adds 2 messages: UserMessage and AssistantMessage
        IntStream.range(0, requestCount).forEach(i -> {
            var request = createSimpleRequest("Message " + i);
            var response = mockSimpleChatResponse("Response " + i);
            Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                    .thenReturn(response);
            provider.stream(request).blockFirst();
        });

        var lastRequest = getPromptCaptor(requestCount).getAllValues()
                .get(requestCount - 1);
        var messageCount = lastRequest.getInstructions().size();
        // Spring AI's MessageWindowChatMemory with maxMessages(30) may include
        // up to 31 messages when building the prompt (30 in memory + current)
        Assert.assertTrue("Message count should not exceed memory limit, got: "
                + messageCount, messageCount <= 31);

        var userMessageTexts = lastRequest.getInstructions().stream()
                .filter(UserMessage.class::isInstance)
                .map(UserMessage.class::cast).map(UserMessage::getText)
                .toList();
        Assert.assertFalse("Should not contain very old messages",
                userMessageTexts.stream()
                        .anyMatch(text -> text.contains("Message 0")));
        Assert.assertTrue("Should contain recent messages",
                userMessageTexts.stream().anyMatch(text -> text
                        .contains("Message " + (requestCount - 1))));
    }

    @Test
    public void stream_withMultipleAttachmentsOfDifferentTypes_processesAll() {
        provider.setStreaming(false);
        var imageAttachment = new AIAttachment("photo.jpg", "image/jpeg",
                "fake-image".getBytes());
        var textAttachment = new AIAttachment("doc.txt", "text/plain",
                "Hello world".getBytes(StandardCharsets.UTF_8));
        var pdfAttachment = new AIAttachment("file.pdf", "application/pdf",
                "PDF content".getBytes(StandardCharsets.UTF_8));
        var unsupportedBinaryAttachment = new AIAttachment("data.bin",
                "application/octet-stream", "binary".getBytes());
        var request = new TestLLMRequest("Process all", null,
                Arrays.asList(imageAttachment, textAttachment, pdfAttachment,
                        unsupportedBinaryAttachment),
                new Object[0]);

        mockSimpleChat("Processed");
        provider.stream(request).blockFirst();

        var messages = capturePrompt().getInstructions();
        var userMessage = (UserMessage) messages.getFirst();

        // 3 supported attachments (image, text, pdf) - unsupported is ignored
        Assert.assertEquals(3, userMessage.getMedia().size());
    }

    @Test
    public void stream_withAudioAttachment_processesAudio() {
        provider.setStreaming(false);
        var audioData = "fake-audio-data".getBytes();
        var attachment = new AIAttachment("audio.mp3", "audio/mpeg", audioData);
        var request = new TestLLMRequest("Transcribe this audio", null,
                List.of(attachment), new Object[0]);

        mockSimpleChat("Transcription");
        provider.stream(request).blockFirst();

        var messages = capturePrompt().getInstructions();
        var userMessage = (UserMessage) messages.getFirst();
        var media = userMessage.getMedia();

        Assert.assertEquals(1, media.size());
        Assert.assertEquals("audio/mpeg",
                media.getFirst().getMimeType().toString());
    }

    @Test
    public void stream_withVideoAttachment_processesVideo() {
        provider.setStreaming(false);
        var videoData = "fake-video-data".getBytes();
        var attachment = new AIAttachment("video.mp4", "video/mp4", videoData);
        var request = new TestLLMRequest("Describe this video", null,
                List.of(attachment), new Object[0]);

        mockSimpleChat("Description");
        provider.stream(request).blockFirst();

        var messages = capturePrompt().getInstructions();
        var userMessage = (UserMessage) messages.getFirst();
        var media = userMessage.getMedia();

        Assert.assertEquals(1, media.size());
        Assert.assertEquals("video/mp4",
                media.getFirst().getMimeType().toString());
    }

    @Test
    public void stream_withStreamingAndPushDisabled_logsWarning() {
        ui = Mockito.mock(UI.class);
        var pushConfig = Mockito.mock(PushConfiguration.class);
        Mockito.when(pushConfig.getPushMode()).thenReturn(PushMode.DISABLED);
        Mockito.when(ui.getPushConfiguration()).thenReturn(pushConfig);
        UI.setCurrent(ui);

        var originalErr = System.err;
        var errStream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errStream));
        try {
            var request = createSimpleRequest("Hello");
            var tokens = List.of("Hello", " ", "World");
            Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                    .thenReturn(Flux.fromIterable(tokens.stream()
                            .map(this::mockSimpleChatResponse).toList()));

            provider.stream(request).collectList().block();

            var errContent = errStream.toString(StandardCharsets.UTF_8);
            Assert.assertTrue(errContent.contains("Push is not enabled"));
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    public void stream_withNonStreamingAndPushDisabled_doesNotLogWarning() {
        provider.setStreaming(false);
        ui = Mockito.mock(UI.class);
        var pushConfig = Mockito.mock(PushConfiguration.class);
        Mockito.when(pushConfig.getPushMode()).thenReturn(PushMode.DISABLED);
        Mockito.when(ui.getPushConfiguration()).thenReturn(pushConfig);
        UI.setCurrent(ui);

        var originalErr = System.err;
        var errStream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errStream));
        try {
            var request = createSimpleRequest("Hello");
            mockSimpleChat("Hi there");

            provider.stream(request).collectList().block();

            var errContent = errStream.toString(StandardCharsets.UTF_8);
            Assert.assertFalse(errContent.contains("Push is not enabled"));
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    public void setHistory_restoresConversation() {
        provider.setStreaming(false);
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Previous question",
                        null, null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Previous answer",
                        null, null));

        provider.setHistory(history);

        // Verify the restored history is used in the next request by checking
        // that the prompt contains the restored messages
        var response = mockSimpleChatResponse("Follow-up answer");
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(response);
        provider.stream(createSimpleRequest("Follow-up")).blockFirst();

        var captor = ArgumentCaptor.forClass(Prompt.class);
        Mockito.verify(mockChatModel).call(captor.capture());
        var messages = captor.getValue().getInstructions();
        Assert.assertTrue(
                messages.stream().anyMatch(msg -> msg instanceof UserMessage
                        && Objects.equals(msg.getText(), "Previous question")));
        Assert.assertTrue(messages.stream()
                .anyMatch(msg -> msg instanceof AssistantMessage
                        && Objects.equals(msg.getText(), "Previous answer")));
    }

    @Test
    public void setHistory_clearsExistingHistory() {
        provider.setStreaming(false);
        var response = mockSimpleChatResponse("Old response");
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(response);
        provider.stream(createSimpleRequest("Old message")).blockFirst();

        var newHistory = List.of(
                new ChatMessage(ChatMessage.Role.USER, "New question", null,
                        null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "New answer", null,
                        null));

        provider.setHistory(newHistory);

        // Verify the old history is cleared by checking the next request
        var response2 = mockSimpleChatResponse("Response");
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(response2);
        provider.stream(createSimpleRequest("Check")).blockFirst();

        var captor = ArgumentCaptor.forClass(Prompt.class);
        Mockito.verify(mockChatModel, Mockito.atLeast(2))
                .call(captor.capture());
        var lastMessages = captor.getAllValues().getLast().getInstructions();
        Assert.assertFalse(
                lastMessages.stream().anyMatch(msg -> msg instanceof UserMessage
                        && Objects.equals(msg.getText(), "Old message")));
        Assert.assertTrue(
                lastMessages.stream().anyMatch(msg -> msg instanceof UserMessage
                        && Objects.equals(msg.getText(), "New question")));
    }

    @Test
    public void setHistory_withNullHistory_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class,
                () -> provider.setHistory(null));
    }

    @Test
    public void setHistory_exceedingMaxMessages_evictsOldest() {
        provider.setStreaming(false);
        var history = new ArrayList<ChatMessage>();
        for (int i = 0; i < 20; i++) {
            history.add(new ChatMessage(ChatMessage.Role.USER, "Question " + i,
                    null, null));
            history.add(new ChatMessage(ChatMessage.Role.ASSISTANT,
                    "Answer " + i, null, null));
        }
        Assert.assertEquals(40, history.size());

        provider.setHistory(history);

        // Verify eviction by checking the next request's messages
        var response = mockSimpleChatResponse("Response");
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(response);
        provider.stream(createSimpleRequest("Check")).blockFirst();

        var captor = ArgumentCaptor.forClass(Prompt.class);
        Mockito.verify(mockChatModel).call(captor.capture());
        var messages = captor.getValue().getInstructions();
        // Filter to only user/assistant messages (exclude system)
        var chatMessages = messages.stream()
                .filter(msg -> msg instanceof UserMessage
                        || msg instanceof AssistantMessage)
                .toList();
        // +1 for the "Check" message we sent to trigger the request
        Assert.assertTrue(chatMessages.size() <= 31);
        Assert.assertTrue(
                chatMessages.stream().anyMatch(msg -> msg instanceof UserMessage
                        && Objects.equals(msg.getText(), "Question 19")));
        Assert.assertFalse(
                chatMessages.stream().anyMatch(msg -> msg instanceof UserMessage
                        && Objects.equals(msg.getText(), "Question 0")));
    }

    @Test
    public void setHistory_withChatClientConstructor_throwsUnsupportedOperationException() {
        var chatClient = ChatClient.builder(mockChatModel).build();
        var chatClientProvider = new SpringAILLMProvider(chatClient);
        var history = new ArrayList<ChatMessage>();
        Assert.assertThrows(UnsupportedOperationException.class,
                () -> chatClientProvider.setHistory(history));
    }

    private void mockSimpleChat(String responseText) {
        var response = mockSimpleChatResponse(responseText);
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(response);
    }

    private ChatResponse mockSimpleChatResponse(String text) {
        var assistantMessage = new AssistantMessage(text);
        var generation = new Generation(assistantMessage);
        return new ChatResponse(List.of(generation));
    }

    private static LLMRequest createSimpleRequest(String message) {
        return new TestLLMRequest(message, null, Collections.emptyList(),
                new Object[0]);
    }

    private Prompt capturePrompt() {
        return getPromptCaptor(1).getValue();
    }

    private ArgumentCaptor<Prompt> getPromptCaptor(int requestCount) {
        var captor = ArgumentCaptor.forClass(Prompt.class);
        Mockito.verify(mockChatModel, Mockito.times(requestCount))
                .call(captor.capture());
        return captor;
    }

    private record TestLLMRequest(String userMessage, String systemPrompt,
            List<AIAttachment> attachments,
            Object[] tools) implements LLMRequest {
    }

    private static class SampleToolsClass {
        @Tool(description = "Gets the current temperature")
        public String getTemperature() {
            return "22°C";
        }

        @Tool(description = "Gets the current humidity")
        public String getHumidity() {
            return "65%";
        }
    }

    private static class AnotherSampleToolsClass {
        @Tool(description = "Gets the rainfall probability")
        public String getRainfallProbability() {
            return "20%";
        }
    }
}
