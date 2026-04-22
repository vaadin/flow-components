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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
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

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.common.ChatMessage;
import com.vaadin.flow.component.ai.provider.LLMProvider.LLMRequest;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.tests.MockUIExtension;

import reactor.core.publisher.Flux;
import tools.jackson.databind.JsonNode;

class SpringAILLMProviderTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private ChatModel mockChatModel;
    private SpringAILLMProvider provider;

    private TestLogger logger = TestLoggerFactory
            .getTestLogger(SpringAILLMProvider.class);

    @BeforeEach
    void setup() {
        mockChatModel = Mockito.mock(ChatModel.class);
        provider = new SpringAILLMProvider(mockChatModel);
        logger.clear();
    }

    @Test
    void stream_withNullRequest_throwsNullPointerException() {
        Assertions.assertThrows(NullPointerException.class,
                () -> provider.stream(null).blockFirst());
    }

    @Test
    void stream_withNullUserMessage_throwsNullPointerException() {
        var request = new TestLLMRequest(null, null, Collections.emptyList(),
                new Object[0]);
        Assertions.assertThrows(NullPointerException.class,
                () -> provider.stream(request).blockFirst());
    }

    @Test
    void constructor_withNullChatModel_throwsNullPointerException() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new SpringAILLMProvider((ChatModel) null));
    }

    @Test
    void constructor_withNullChatClient_throwsNullPointerException() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new SpringAILLMProvider((ChatClient) null));
    }

    @Test
    void constructor_withChatClient_nonStreaming_returnsResponse() {
        var chatClient = ChatClient.builder(mockChatModel).build();
        var chatClientProvider = new SpringAILLMProvider(chatClient);
        chatClientProvider.setStreaming(false);
        var request = createSimpleRequest("Hello");
        mockSimpleChat("Full response");

        var results = chatClientProvider.stream(request).collectList().block();

        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("Full response", results.getFirst());
    }

    @Test
    void constructor_withChatClient_defaultConfig_returnsStreamedTokens() {
        var chatClient = ChatClient.builder(mockChatModel).build();
        var chatClientStreamingProvider = new SpringAILLMProvider(chatClient);
        var request = createSimpleRequest("Hello");
        var tokens = List.of("Hello", " ", "World");

        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.fromIterable(tokens.stream()
                        .map(this::mockSimpleChatResponse).toList()));

        var results = chatClientStreamingProvider.stream(request).collectList()
                .block();
        Assertions.assertEquals(tokens, results);
    }

    @Test
    void constructor_withChatClient_setNonStreaming_setStreaming_returnsStreamedTokens() {
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
        Assertions.assertEquals(tokens, results);
    }

    @Test
    void stream_withNonStreamingModel_returnsResponse() {
        provider.setStreaming(false);
        var request = createSimpleRequest("Hello");
        mockSimpleChat("Full response");

        var results = provider.stream(request).collectList().block();

        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("Full response", results.getFirst());
    }

    @Test
    void stream_chatModelThrowsException_propagatesError() {
        var request = createSimpleRequest("Hello");
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenThrow(new RuntimeException("API error"));
        Assertions.assertThrows(RuntimeException.class,
                () -> provider.stream(request).blockFirst());
    }

    @Test
    void stream_emptyTextResponse_returnsEmpty() {
        provider.setStreaming(false);
        var request = createSimpleRequest("Hello");
        mockSimpleChat("");
        var results = provider.stream(request).collectList().block();
        Assertions.assertNotNull(results);
        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    void stream_nullTextResponse_returnsEmpty() {
        provider.setStreaming(false);
        var request = createSimpleRequest("Hello");
        mockSimpleChat(null);
        var results = provider.stream(request).collectList().block();
        Assertions.assertNotNull(results);
        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    void stream_withSystemPromptInRequest_includesSystemMessage() {
        provider.setStreaming(false);
        var request = new TestLLMRequest("Hello", "You are a helpful assistant",
                Collections.emptyList(), new Object[0]);
        mockSimpleChat("Response");

        provider.stream(request).blockFirst();

        var hasSystemMessage = capturePrompt().getInstructions().stream()
                .anyMatch(SystemMessage.class::isInstance);
        Assertions.assertTrue(hasSystemMessage);
    }

    @Test
    void stream_withNullSystemPrompt_noSystemMessage() {
        provider.setStreaming(false);
        var request = createSimpleRequest("Hello");
        mockSimpleChat("Response");

        provider.stream(request).blockFirst();

        var hasSystemMessage = capturePrompt().getInstructions().stream()
                .anyMatch(SystemMessage.class::isInstance);
        Assertions.assertFalse(hasSystemMessage);
    }

    @Test
    void stream_withEmptySystemPrompt_noSystemMessage() {
        provider.setStreaming(false);
        var request = new TestLLMRequest("Hello", "   ",
                Collections.emptyList(), new Object[0]);
        mockSimpleChat("Response");

        provider.stream(request).blockFirst();

        var hasSystemMessage = capturePrompt().getInstructions().stream()
                .anyMatch(SystemMessage.class::isInstance);
        Assertions.assertFalse(hasSystemMessage);
    }

    @Test
    void stream_withNullAttachments_returnsResponse() {
        provider.setStreaming(false);
        var request = new TestLLMRequest("Hello", null, null, new Object[0]);
        mockSimpleChat("Hi");
        var result = provider.stream(request).blockFirst();
        Assertions.assertEquals("Hi", result);
    }

    @Test
    void stream_withNullAttachmentInList_throwsNullPointerException() {
        var attachment = new AIAttachment("test.txt", "text/plain",
                "Test".getBytes(StandardCharsets.UTF_8));
        var attachments = new ArrayList<AIAttachment>();
        attachments.add(attachment);
        attachments.add(null);

        var request = new TestLLMRequest("Hello", null, attachments,
                new Object[0]);
        mockSimpleChat("hi");

        Assertions.assertThrows(NullPointerException.class,
                () -> provider.stream(request).blockFirst());
    }

    @Test
    void stream_withUnsupportedAttachmentType_ignoresAttachment() {
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
    void stream_withPdfAttachment_handlesPdf() {
        provider.setStreaming(false);
        var pdfData = "PDF binary content".getBytes(StandardCharsets.UTF_8);
        var attachment = new AIAttachment("document.pdf", "application/pdf",
                pdfData);
        var request = new TestLLMRequest("Summarize this document", null,
                List.of(attachment), new Object[0]);

        mockSimpleChat("Summary");

        var result = provider.stream(request).blockFirst();
        Assertions.assertEquals("Summary", result);

        Mockito.verify(mockChatModel).call(Mockito.any(Prompt.class));
    }

    @Test
    void stream_withBinaryPdfData_handlesBinaryPdf() {
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
        Assertions.assertEquals("Summary", result);

        Mockito.verify(mockChatModel).call(Mockito.any(Prompt.class));
    }

    @Test
    void stream_withImageAttachment_processesImage() {
        provider.setStreaming(false);
        var imageData = "fake-image-data".getBytes();
        var attachment = new AIAttachment("test.png", "image/png", imageData);
        var request = new TestLLMRequest("Describe this image", null,
                List.of(attachment), new Object[0]);

        mockSimpleChat("It's a test");

        var result = provider.stream(request).blockFirst();
        Assertions.assertEquals("It's a test", result);

        Mockito.verify(mockChatModel).call(Mockito.any(Prompt.class));
    }

    @Test
    void stream_withTextAttachment_processesText() {
        provider.setStreaming(false);
        var textContent = "Test UTF-8: é à ü";
        var attachment = new AIAttachment("test.txt", "text/plain",
                textContent.getBytes(StandardCharsets.UTF_8));
        var request = new TestLLMRequest("Summarize this", null,
                List.of(attachment), new Object[0]);

        mockSimpleChat("Summary");

        var result = provider.stream(request).blockFirst();
        Assertions.assertEquals("Summary", result);

        Mockito.verify(mockChatModel).call(Mockito.any(Prompt.class));
    }

    @Test
    void stream_withStreamingModel_returnsStreamedTokens() {
        var request = createSimpleRequest("Hello");
        var tokens = List.of("Hello", " ", "World");

        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.fromIterable(tokens.stream()
                        .map(this::mockSimpleChatResponse).toList()));

        var results = provider.stream(request).collectList().block();
        Assertions.assertEquals(tokens, results);
    }

    @Test
    void stream_withSingleTool_toolsAreConfigured() {
        provider.setStreaming(false);
        var toolObject = new SampleToolsClass();
        var request = new TestLLMRequest("Get temperature", null,
                Collections.emptyList(), new Object[] { toolObject });
        mockSimpleChat("The temperature is 22°C");

        provider.stream(request).blockFirst();

        var chatOptions = capturePrompt().getOptions();
        Assertions.assertNotNull(chatOptions);
        var toolCallbacks = ((ToolCallingChatOptions) chatOptions)
                .getToolCallbacks();
        Assertions.assertNotNull(toolCallbacks);
        Assertions.assertEquals(2, toolCallbacks.size());
    }

    @Test
    void stream_withMultipleToolObjects_allToolsAreConfigured() {
        provider.setStreaming(false);
        var tool1 = new SampleToolsClass();
        var tool2 = new AnotherSampleToolsClass();
        var request = new TestLLMRequest("Get weather info", null,
                Collections.emptyList(), new Object[] { tool1, tool2 });
        mockSimpleChat("Weather info");

        provider.stream(request).blockFirst();

        var chatOptions = capturePrompt().getOptions();
        Assertions.assertNotNull(chatOptions);
        var toolCallbacks = ((ToolCallingChatOptions) chatOptions)
                .getToolCallbacks();
        Assertions.assertNotNull(toolCallbacks);
        Assertions.assertEquals(3, toolCallbacks.size());
    }

    @Test
    void stream_withEmptyToolsArray_noToolCallbacksConfigured() {
        provider.setStreaming(false);
        var request = new TestLLMRequest("Hello", null, Collections.emptyList(),
                new Object[0]);
        mockSimpleChat("Hi");

        provider.stream(request).blockFirst();

        var chatOptions = capturePrompt().getOptions();
        var noToolCallbacks = chatOptions == null
                || ((ToolCallingChatOptions) chatOptions).getToolCallbacks()
                        .isEmpty();
        Assertions.assertTrue(noToolCallbacks);
    }

    @Test
    void stream_withNullToolsArray_noToolCallbacksConfigured() {
        provider.setStreaming(false);
        var request = new TestLLMRequest("Hello", null, Collections.emptyList(),
                null);
        mockSimpleChat("Hi");

        provider.stream(request).blockFirst();

        var chatOptions = capturePrompt().getOptions();
        var noToolCallbacks = chatOptions == null
                || ((ToolCallingChatOptions) chatOptions).getToolCallbacks()
                        .isEmpty();
        Assertions.assertTrue(noToolCallbacks);
    }

    @Test
    void chatMemory_retainsHistory() {
        provider.setStreaming(false);
        var response1 = mockSimpleChatResponse("Response 1");
        var response2 = mockSimpleChatResponse("Response 2");
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(response1, response2);

        provider.stream(createSimpleRequest("First message")).blockFirst();
        provider.stream(createSimpleRequest("Second message")).blockFirst();

        var secondRequestMessages = getPromptCaptor(2).getAllValues().get(1)
                .getInstructions();
        Assertions.assertEquals(3, secondRequestMessages.size());
    }

    @Test
    void stream_preservesChatHistoryAcrossRequests() {
        provider.setStreaming(false);
        var response1 = mockSimpleChatResponse("Hi there");
        var response2 = mockSimpleChatResponse("I'm good");
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(response1, response2);

        provider.stream(createSimpleRequest("Hello")).blockFirst();
        provider.stream(createSimpleRequest("How are you?")).blockFirst();

        var allPrompts = getPromptCaptor(2).getAllValues();
        Assertions.assertEquals(1, allPrompts.get(0).getInstructions().size(),
                "First call should have 1 user message");
        Assertions.assertEquals(3, allPrompts.get(1).getInstructions().size(),
                "Second call should have 3 messages (user1, ai1, user2)");
    }

    @Test
    void stream_withMaxMessagesLimit_dropsOldestMessages() {
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
        Assertions.assertTrue(messageCount <= 31,
                "Message count should not exceed memory limit, got: "
                        + messageCount);

        var userMessageTexts = lastRequest.getInstructions().stream()
                .filter(UserMessage.class::isInstance)
                .map(UserMessage.class::cast).map(UserMessage::getText)
                .toList();
        Assertions.assertFalse(
                userMessageTexts.stream()
                        .anyMatch(text -> text.contains("Message 0")),
                "Should not contain very old messages");
        Assertions
                .assertTrue(
                        userMessageTexts.stream()
                                .anyMatch(text -> text.contains(
                                        "Message " + (requestCount - 1))),
                        "Should contain recent messages");
    }

    @Test
    void stream_withMultipleAttachmentsOfDifferentTypes_processesAll() {
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
        Assertions.assertEquals(3, userMessage.getMedia().size());
    }

    @Test
    void stream_withAudioAttachment_processesAudio() {
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

        Assertions.assertEquals(1, media.size());
        Assertions.assertEquals("audio/mpeg",
                media.getFirst().getMimeType().toString());
    }

    @Test
    void stream_withVideoAttachment_processesVideo() {
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

        Assertions.assertEquals(1, media.size());
        Assertions.assertEquals("video/mp4",
                media.getFirst().getMimeType().toString());
    }

    @Test
    void stream_withStreamingAndPushDisabled_logsWarning() {
        ui.getUI().getPushConfiguration().setPushMode(PushMode.DISABLED);

        var request = createSimpleRequest("Hello");
        var tokens = List.of("Hello", " ", "World");
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.fromIterable(tokens.stream()
                        .map(this::mockSimpleChatResponse).toList()));

        provider.stream(request).collectList().block();

        var warning = logger.getLoggingEvents().stream().filter(
                event -> event.getMessage().contains("Push is not enabled"))
                .findFirst();
        Assertions.assertTrue(warning.isPresent(), "Expected push warning");
    }

    @Test
    void stream_withNonStreamingAndPushDisabled_doesNotLogWarning() {
        provider.setStreaming(false);
        ui.getUI().getPushConfiguration().setPushMode(PushMode.DISABLED);

        var request = createSimpleRequest("Hello");
        mockSimpleChat("Hi there");

        provider.stream(request).collectList().block();

        var warning = logger.getLoggingEvents().stream().filter(
                event -> event.getMessage().contains("Push is not enabled"))
                .findFirst();
        Assertions.assertFalse(warning.isPresent(), "Expected no push warning");
    }

    @Test
    void setHistory_restoresConversation() {
        provider.setStreaming(false);
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Previous question",
                        null, null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Previous answer",
                        null, null));

        provider.setHistory(history, Collections.emptyMap());

        // Verify the restored history is used in the next request by checking
        // that the prompt contains the restored messages
        var response = mockSimpleChatResponse("Follow-up answer");
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(response);
        provider.stream(createSimpleRequest("Follow-up")).blockFirst();

        var captor = ArgumentCaptor.forClass(Prompt.class);
        Mockito.verify(mockChatModel).call(captor.capture());
        var messages = captor.getValue().getInstructions();
        Assertions.assertTrue(
                messages.stream().anyMatch(msg -> msg instanceof UserMessage
                        && Objects.equals(msg.getText(), "Previous question")));
        Assertions.assertTrue(messages.stream()
                .anyMatch(msg -> msg instanceof AssistantMessage
                        && Objects.equals(msg.getText(), "Previous answer")));
    }

    @Test
    void setHistory_clearsExistingHistory() {
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

        provider.setHistory(newHistory, Collections.emptyMap());

        // Verify the old history is cleared by checking the next request
        var response2 = mockSimpleChatResponse("Response");
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(response2);
        provider.stream(createSimpleRequest("Check")).blockFirst();

        var captor = ArgumentCaptor.forClass(Prompt.class);
        Mockito.verify(mockChatModel, Mockito.atLeast(2))
                .call(captor.capture());
        var lastMessages = captor.getAllValues().getLast().getInstructions();
        Assertions.assertFalse(
                lastMessages.stream().anyMatch(msg -> msg instanceof UserMessage
                        && Objects.equals(msg.getText(), "Old message")));
        Assertions.assertTrue(
                lastMessages.stream().anyMatch(msg -> msg instanceof UserMessage
                        && Objects.equals(msg.getText(), "New question")));
    }

    @Test
    void setHistory_withNullHistory_throwsNullPointerException() {
        Assertions.assertThrows(NullPointerException.class,
                () -> provider.setHistory(null, Collections.emptyMap()));
    }

    @Test
    void setHistory_exceedingMaxMessages_evictsOldest() {
        provider.setStreaming(false);
        var history = new ArrayList<ChatMessage>();
        for (int i = 0; i < 20; i++) {
            history.add(new ChatMessage(ChatMessage.Role.USER, "Question " + i,
                    null, null));
            history.add(new ChatMessage(ChatMessage.Role.ASSISTANT,
                    "Answer " + i, null, null));
        }
        Assertions.assertEquals(40, history.size());

        provider.setHistory(history, Collections.emptyMap());

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
        Assertions.assertTrue(chatMessages.size() <= 31);
        Assertions.assertTrue(
                chatMessages.stream().anyMatch(msg -> msg instanceof UserMessage
                        && Objects.equals(msg.getText(), "Question 19")));
        Assertions.assertFalse(
                chatMessages.stream().anyMatch(msg -> msg instanceof UserMessage
                        && Objects.equals(msg.getText(), "Question 0")));
    }

    @Test
    void setHistory_withChatClientConstructor_throwsUnsupportedOperationException() {
        var chatClient = ChatClient.builder(mockChatModel).build();
        var chatClientProvider = new SpringAILLMProvider(chatClient);
        var history = new ArrayList<ChatMessage>();
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> chatClientProvider.setHistory(history,
                        Collections.emptyMap()));
    }

    @Test
    void setHistory_withAttachments_restoresUserMessageWithMedia() {
        provider.setStreaming(false);
        var imageData = "fake-image-data".getBytes();
        var attachment = new AIAttachment("photo.png", "image/png", imageData);
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Look at this", "msg-1",
                        null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Nice photo!", null,
                        null));
        var attachments = Map.of("msg-1", List.of(attachment));

        provider.setHistory(history, attachments);

        var response = mockSimpleChatResponse("Follow-up answer");
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(response);
        provider.stream(createSimpleRequest("Follow-up")).blockFirst();

        var captor = ArgumentCaptor.forClass(Prompt.class);
        Mockito.verify(mockChatModel).call(captor.capture());
        var messages = captor.getValue().getInstructions();

        // Find the restored user message with media
        var restoredUserMsg = messages.stream()
                .filter(UserMessage.class::isInstance)
                .map(UserMessage.class::cast)
                .filter(msg -> Objects.equals(msg.getText(), "Look at this"))
                .findFirst().orElseThrow();

        // Should have media attached
        Assertions.assertEquals(1, restoredUserMsg.getMedia().size());
        Assertions.assertEquals("image/png",
                restoredUserMsg.getMedia().getFirst().getMimeType().toString());
    }

    @Test
    void setHistory_withAttachments_assistantMessageIgnoresAttachments() {
        provider.setStreaming(false);
        var attachment = new AIAttachment("file.txt", "text/plain",
                "content".getBytes());
        var history = List.of(new ChatMessage(ChatMessage.Role.ASSISTANT,
                "Hello", "msg-1", null));
        var attachments = Map.of("msg-1", List.of(attachment));

        provider.setHistory(history, attachments);

        var response = mockSimpleChatResponse("Response");
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(response);
        provider.stream(createSimpleRequest("Check")).blockFirst();

        var captor = ArgumentCaptor.forClass(Prompt.class);
        Mockito.verify(mockChatModel).call(captor.capture());
        var messages = captor.getValue().getInstructions();

        Assertions.assertTrue(messages.stream()
                .anyMatch(msg -> msg instanceof AssistantMessage
                        && Objects.equals(msg.getText(), "Hello")));
    }

    @Test
    void setHistory_withAttachments_nullAttachmentMapThrows() {
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", null, null));
        Assertions.assertThrows(NullPointerException.class,
                () -> provider.setHistory(history, null));
    }

    @Test
    void setHistory_withEmptyAttachmentMap_behavesLikeTextOnly() {
        provider.setStreaming(false);
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1", null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Hi", null, null));

        provider.setHistory(history, Collections.emptyMap());

        var response = mockSimpleChatResponse("Response");
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(response);
        provider.stream(createSimpleRequest("Check")).blockFirst();

        var captor = ArgumentCaptor.forClass(Prompt.class);
        Mockito.verify(mockChatModel).call(captor.capture());
        var messages = captor.getValue().getInstructions();

        // User message should have no media
        var userMsg = messages.stream().filter(UserMessage.class::isInstance)
                .map(UserMessage.class::cast)
                .filter(msg -> Objects.equals(msg.getText(), "Hello"))
                .findFirst().orElseThrow();
        Assertions.assertTrue(userMsg.getMedia().isEmpty());
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

    // --- Explicit tools tests ---

    @Test
    void stream_withExplicitTools_toolCallbacksConfigured() {
        provider.setStreaming(false);
        var explicitTool = createExplicitTool("myTool", "A test tool",
                "{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\"}}}",
                args -> "result");

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));
        mockSimpleChat("Done");

        provider.stream(request).blockFirst();

        var chatOptions = capturePrompt().getOptions();
        Assertions.assertNotNull(chatOptions);
        var toolCallbacks = ((ToolCallingChatOptions) chatOptions)
                .getToolCallbacks();
        Assertions.assertNotNull(toolCallbacks);
        Assertions.assertEquals(1, toolCallbacks.size());
        Assertions.assertEquals("myTool",
                toolCallbacks.getFirst().getToolDefinition().name());
    }

    @Test
    void stream_withExplicitTool_passesArgumentsToCallback() {
        provider.setStreaming(false);
        var receivedArgs = new ArrayList<JsonNode>();
        var explicitTool = createExplicitTool("myTool", "A test tool",
                "{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"}}}",
                args -> {
                    receivedArgs.add(args);
                    return "result for " + args.get("city").asString();
                });

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));
        mockSimpleChat("Done");

        provider.stream(request).blockFirst();

        var chatOptions = capturePrompt().getOptions();
        var toolCallbacks = ((ToolCallingChatOptions) chatOptions)
                .getToolCallbacks();
        Assertions.assertEquals(1, toolCallbacks.size());

        // Call the callback directly to verify arguments are parsed and
        // forwarded as a JsonNode
        var result = toolCallbacks.getFirst().call("{\"city\":\"Helsinki\"}");
        Assertions.assertEquals(1, receivedArgs.size(),
                "Tool executor should have been called once");
        Assertions.assertEquals("Helsinki",
                receivedArgs.getFirst().get("city").asString(),
                "Tool executor should receive arguments as a JsonNode matching the callback input");
        Assertions.assertEquals("result for Helsinki", result);
    }

    @Test
    void stream_withBothVendorAndExplicitTools_allConfigured() {
        provider.setStreaming(false);
        var vendorTool = new SampleToolsClass();
        var explicitTool = createExplicitTool("explicitTool", "Explicit", null,
                args -> "result");

        var request = new TestLLMRequestWithExplicitTools("Call tools", null,
                Collections.emptyList(), new Object[] { vendorTool },
                List.of(explicitTool));
        mockSimpleChat("Done");

        provider.stream(request).blockFirst();

        var chatOptions = capturePrompt().getOptions();
        Assertions.assertNotNull(chatOptions);
        var toolCallbacks = ((ToolCallingChatOptions) chatOptions)
                .getToolCallbacks();
        Assertions.assertNotNull(toolCallbacks);
        // 2 from SampleToolsClass + 1 explicit
        Assertions.assertEquals(3, toolCallbacks.size());
    }

    @Test
    void stream_withExplicitTool_malformedJsonArguments_returnsError() {
        provider.setStreaming(false);
        var receivedArgs = new ArrayList<JsonNode>();
        var explicitTool = createExplicitTool("myTool", "A test tool",
                "{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"}}}",
                args -> {
                    receivedArgs.add(args);
                    return "ok";
                });

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));
        mockSimpleChat("Done");

        provider.stream(request).blockFirst();

        var toolCallbacks = ((ToolCallingChatOptions) capturePrompt()
                .getOptions()).getToolCallbacks();
        var result = toolCallbacks.getFirst().call("Not json");

        Assertions.assertTrue(result.startsWith("Error executing tool:"));
        Assertions.assertEquals(0, receivedArgs.size());
    }

    @Test
    void stream_withExplicitToolNullSchema_usesEmptySchema() {
        provider.setStreaming(false);
        var explicitTool = createExplicitTool("simpleTool", "A simple tool",
                null, args -> "done");

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));
        mockSimpleChat("OK");

        provider.stream(request).blockFirst();

        var chatOptions = capturePrompt().getOptions();
        Assertions.assertNotNull(chatOptions);
        var toolCallbacks = ((ToolCallingChatOptions) chatOptions)
                .getToolCallbacks();
        Assertions.assertEquals(1, toolCallbacks.size());
        var toolDef = toolCallbacks.getFirst().getToolDefinition();
        Assertions.assertEquals("simpleTool", toolDef.name());
        Assertions.assertEquals("A simple tool", toolDef.description());
        // Should have a default empty schema
        Assertions.assertNotNull(toolDef.inputSchema());
    }

    private static LLMProvider.ToolSpec createExplicitTool(String name,
            String description, String parametersSchema,
            java.util.function.Function<JsonNode, String> executor) {
        return new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public String getParametersSchema() {
                return parametersSchema;
            }

            @Override
            public String execute(JsonNode arguments) {
                return executor.apply(arguments);
            }
        };
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

    private record TestLLMRequestWithExplicitTools(String userMessage,
            String systemPrompt, List<AIAttachment> attachments, Object[] tools,
            List<LLMProvider.ToolSpec> explicitTools) implements LLMRequest {
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
