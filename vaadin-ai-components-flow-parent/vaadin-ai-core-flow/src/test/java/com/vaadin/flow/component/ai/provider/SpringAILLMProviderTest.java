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

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.metadata.Usage;
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
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.tests.MockUIExtension;

import io.micrometer.observation.ObservationRegistry;
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
        Mockito.when(mockChatModel.getOptions())
                .thenReturn(ToolCallingChatOptions.builder().build());
        provider = new SpringAILLMProvider(mockChatModel);
        logger.clearAll();
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
    void stream_nonStreamingChatModelThrowsException_propagatesError() {
        provider.setStreaming(false);
        var request = createSimpleRequest("Hello");
        var originalError = new RuntimeException("API error");
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenThrow(originalError);
        // Bounded block: a swallowed error would leave the sink open
        // forever instead of failing the test
        var thrown = Assertions.assertThrows(RuntimeException.class,
                () -> provider.stream(request)
                        .blockFirst(Duration.ofSeconds(5)));
        Assertions.assertSame(originalError, thrown);
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
    void stream_withInvalidUtf8TextAttachment_replacesInvalidSequences() {
        provider.setStreaming(false);
        // Lone continuation byte: not decodable as UTF-8. Text attachments
        // are decoded leniently, so it is replaced instead of rejected.
        var attachment = new AIAttachment("broken.txt", "text/plain",
                new byte[] { 0x41, (byte) 0x80, 0x42 });
        var request = new TestLLMRequest("Summarize this", null,
                List.of(attachment), new Object[0]);

        mockSimpleChat("Summary");

        var result = provider.stream(request).blockFirst();
        Assertions.assertEquals("Summary", result);

        var userMessage = capturePrompt().getInstructions().stream()
                .filter(UserMessage.class::isInstance)
                .map(UserMessage.class::cast).findFirst().orElseThrow();
        Assertions.assertEquals(1, userMessage.getMedia().size());
        var text = String.valueOf(userMessage.getMedia().getFirst().getData());
        Assertions.assertTrue(text.contains("A\uFFFDB"),
                "Invalid UTF-8 should be replaced, but got: " + text);
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
        var toolCallbacks = chatOptions == null ? null
                : ((ToolCallingChatOptions) chatOptions).getToolCallbacks();
        var noToolCallbacks = toolCallbacks == null || toolCallbacks.isEmpty();
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
        var toolCallbacks = chatOptions == null ? null
                : ((ToolCallingChatOptions) chatOptions).getToolCallbacks();
        var noToolCallbacks = toolCallbacks == null || toolCallbacks.isEmpty();
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

        Assertions.assertTrue(hasDeliveryWarning(), "Expected push warning");
    }

    @Test
    void stream_withStreamingAndManualPush_logsWarning() {
        Mockito.when(ui.getService().ensurePushAvailable()).thenReturn(true);
        ui.getUI().getPushConfiguration().setPushMode(PushMode.MANUAL);

        var request = createSimpleRequest("Hello");
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.just(mockSimpleChatResponse("Hello")));

        provider.stream(request).collectList().block();

        Assertions.assertTrue(hasDeliveryWarning(),
                "Manual push does not deliver the response on its own, so the "
                        + "warning is expected");
    }

    @Test
    void stream_withStreamingAndPollingEnabled_doesNotLogWarning() {
        ui.getUI().getPushConfiguration().setPushMode(PushMode.DISABLED);
        ui.getUI().setPollInterval(500);

        var request = createSimpleRequest("Hello");
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.just(mockSimpleChatResponse("Hello")));

        provider.stream(request).collectList().block();

        Assertions.assertFalse(hasDeliveryWarning(),
                "Polling delivers the response, so no warning is expected");
    }

    @Test
    void stream_withStreamingAndAutomaticPush_doesNotLogWarning() {
        Mockito.when(ui.getService().ensurePushAvailable()).thenReturn(true);
        ui.getUI().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

        var request = createSimpleRequest("Hello");
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.just(mockSimpleChatResponse("Hello")));

        provider.stream(request).collectList().block();

        Assertions.assertFalse(hasDeliveryWarning(),
                "Automatic push delivers the response, so no warning is "
                        + "expected");
    }

    @Test
    void stream_withNonStreamingAndPushDisabled_doesNotLogWarning() {
        provider.setStreaming(false);
        ui.getUI().getPushConfiguration().setPushMode(PushMode.DISABLED);

        var request = createSimpleRequest("Hello");
        mockSimpleChat("Hi there");

        provider.stream(request).collectList().block();

        Assertions.assertFalse(hasDeliveryWarning(),
                "A synchronous turn completes within the request, so no "
                        + "warning is expected");
    }

    @Test
    void backgroundExecution_isDisabledByDefault() {
        Assertions.assertFalse(provider.isBackgroundExecution());
    }

    @Test
    void setBackgroundExecution_isReflectedByGetter() {
        provider.setBackgroundExecution(true);
        Assertions.assertTrue(provider.isBackgroundExecution());

        provider.setBackgroundExecution(false);
        Assertions.assertFalse(provider.isBackgroundExecution());
    }

    @Test
    void stream_nonStreamingByDefault_callsModelOnSubscribingThread() {
        provider.setStreaming(false);
        var callThread = captureChatModelCallThread();

        provider.stream(createSimpleRequest("Hello")).collectList().block();

        Assertions.assertSame(Thread.currentThread(), callThread.get(),
                "Without background execution the blocking call must stay on "
                        + "the subscribing thread");
    }

    @Test
    void stream_nonStreamingWithBackgroundExecution_callsModelOffSubscribingThread() {
        provider.setStreaming(false);
        provider.setBackgroundExecution(true);
        var callThread = captureChatModelCallThread();

        provider.stream(createSimpleRequest("Hello")).collectList().block();

        Assertions.assertNotSame(Thread.currentThread(), callThread.get(),
                "With background execution the blocking call must move off the "
                        + "subscribing thread");
    }

    @Test
    void stream_nonStreamingWithBackgroundExecution_returnsResponse() {
        provider.setStreaming(false);
        provider.setBackgroundExecution(true);
        mockSimpleChat("Full response");

        var results = provider.stream(createSimpleRequest("Hello"))
                .collectList().block();

        Assertions.assertEquals(List.of("Full response"), results);
    }

    @Test
    void stream_nonStreamingWithBackgroundExecution_subscribeReturnsWhileCallStillRunning()
            throws Exception {
        provider.setStreaming(false);
        provider.setBackgroundExecution(true);
        var callStarted = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var completed = new CountDownLatch(1);
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenAnswer(invocation -> {
                    callStarted.countDown();
                    // Timed so an implementation that blocks the subscriber
                    // fails the count assertion below instead of deadlocking:
                    // the release latch only opens after subscribe() has
                    // returned.
                    release.await(5, TimeUnit.SECONDS);
                    return mockSimpleChatResponse("Response");
                });

        provider.stream(createSimpleRequest("Hello")).subscribe(token -> {
        }, error -> {
        }, completed::countDown);

        Assertions.assertTrue(callStarted.await(5, TimeUnit.SECONDS),
                "The model was never called");
        Assertions.assertEquals(1, completed.getCount(),
                "subscribe() must return while the blocking call is still "
                        + "running");

        release.countDown();
        Assertions.assertTrue(completed.await(5, TimeUnit.SECONDS),
                "The response never completed after release");
    }

    @Test
    void stream_nonStreamingWithBackgroundExecution_pushDisabled_logsWarning() {
        provider.setStreaming(false);
        provider.setBackgroundExecution(true);
        ui.getUI().getPushConfiguration().setPushMode(PushMode.DISABLED);
        mockSimpleChat("Hi there");

        provider.stream(createSimpleRequest("Hello")).collectList().block();

        Assertions.assertTrue(hasDeliveryWarning(),
                "A background turn needs push or polling to reach the browser");
    }

    @Test
    void stream_withBackgroundExecution_repeatedTurns_logsWarningOnce() {
        provider.setStreaming(false);
        provider.setBackgroundExecution(true);
        ui.getUI().getPushConfiguration().setPushMode(PushMode.DISABLED);
        mockSimpleChat("Hi there");

        provider.stream(createSimpleRequest("Hello")).collectList().block();
        provider.stream(createSimpleRequest("Hello again")).collectList()
                .block();

        Assertions.assertEquals(1, deliveryWarningCount(),
                "Expected exactly one delivery warning across two turns");
    }

    @Test
    void stream_streamingWithBackgroundExecution_returnsStreamedTokens() {
        provider.setBackgroundExecution(true);
        var tokens = List.of("Hello", " ", "World");
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.fromIterable(tokens.stream()
                        .map(this::mockSimpleChatResponse).toList()));

        var results = provider.stream(createSimpleRequest("Hello"))
                .collectList().block();

        Assertions.assertEquals(tokens, results);
    }

    // No dispatch-thread variant of the streaming inertness test here: the
    // Spring AI ChatClient schedules its streaming call on its own
    // boundedElastic thread regardless of the background execution setting,
    // so "the setting does not reschedule a streaming response" has no
    // observable thread difference on this stack. The LangChain4j test
    // covers that contract; stream_streamingWithBackgroundExecution_
    // returnsStreamedTokens above covers this provider's inertness
    // behaviorally.

    /**
     * Records the thread the blocking chat model call runs on and answers with
     * a simple response.
     */
    private AtomicReference<Thread> captureChatModelCallThread() {
        var callThread = new AtomicReference<Thread>();
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenAnswer(invocation -> {
                    callThread.set(Thread.currentThread());
                    return mockSimpleChatResponse("Response");
                });
        return callThread;
    }

    private boolean hasDeliveryWarning() {
        return deliveryWarningCount() > 0;
    }

    private long deliveryWarningCount() {
        return logger.getLoggingEvents().stream()
                .filter(event -> event.getMessage()
                        .contains("neither automatic push nor polling"))
                .count();
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
    void setHistory_withNullHistory_keepsExistingHistory() {
        provider.setStreaming(false);
        provider.setHistory(
                List.of(new ChatMessage(ChatMessage.Role.USER,
                        "Previous question", null, null)),
                Collections.emptyMap());

        Assertions.assertThrows(NullPointerException.class,
                () -> provider.setHistory(null, Collections.emptyMap()));

        mockSimpleChat("Response");
        provider.stream(createSimpleRequest("Follow-up")).blockFirst();

        var messages = capturePrompt().getInstructions();
        Assertions.assertTrue(
                messages.stream()
                        .anyMatch(msg -> msg instanceof UserMessage && Objects
                                .equals(msg.getText(), "Previous question")),
                "A rejected history must not clear the existing one, but got: "
                        + messages);
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
    void setHistory_withChatClientConstructor_isNoOp() {
        var chatClient = ChatClient.builder(mockChatModel).build();
        var chatClientProvider = new SpringAILLMProvider(chatClient);
        chatClientProvider.setStreaming(false);
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Old message", null,
                        null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Old answer", null,
                        null));

        Assertions.assertDoesNotThrow(() -> chatClientProvider
                .setHistory(history, Collections.emptyMap()));

        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(mockSimpleChatResponse("Response"));
        chatClientProvider.stream(createSimpleRequest("New question"))
                .blockFirst();

        var captor = ArgumentCaptor.forClass(Prompt.class);
        Mockito.verify(mockChatModel).call(captor.capture());
        var messages = captor.getValue().getInstructions();
        Assertions.assertFalse(messages.stream()
                .anyMatch(msg -> Objects.equals(msg.getText(), "Old message")));
    }

    @Test
    void chatClientConstructor_withPreloadedChatMemory_sendsHistoryToModel() {
        var chatMemory = MessageWindowChatMemory.builder().build();
        chatMemory.add("conv-1", List.of(new UserMessage("Old message"),
                new AssistantMessage("Old answer")));
        var chatClient = ChatClient.builder(mockChatModel)
                .defaultAdvisors(a -> a
                        .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                                .build())
                        .param(ChatMemory.CONVERSATION_ID, "conv-1"))
                .build();
        var chatClientProvider = new SpringAILLMProvider(chatClient);
        chatClientProvider.setStreaming(false);

        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(mockSimpleChatResponse("Response"));
        chatClientProvider.stream(createSimpleRequest("New question"))
                .blockFirst();

        var captor = ArgumentCaptor.forClass(Prompt.class);
        Mockito.verify(mockChatModel).call(captor.capture());
        var messages = captor.getValue().getInstructions();
        Assertions.assertTrue(messages.stream()
                .anyMatch(msg -> Objects.equals(msg.getText(), "Old message")));
        Assertions.assertTrue(messages.stream()
                .anyMatch(msg -> Objects.equals(msg.getText(), "Old answer")));
        Assertions.assertTrue(messages.stream().anyMatch(
                msg -> Objects.equals(msg.getText(), "New question")));
    }

    @Test
    void setHistory_chatClientWithoutMemoryAdvisor_warns() {
        var chatClientProvider = new SpringAILLMProvider(
                ChatClient.builder(mockChatModel).build());

        chatClientProvider.setHistory(List
                .of(new ChatMessage(ChatMessage.Role.USER, "Hi", null, null)),
                Collections.emptyMap());

        Assertions.assertEquals(1, warningCount("no chat memory advisor"));
        Assertions.assertEquals(1, warningCount(""),
                "Expected the missing-advisor warning to be the only one");
    }

    @Test
    void setHistory_chatClientWithoutConversationId_warns() {
        var chatMemory = MessageWindowChatMemory.builder().build();
        var chatClientProvider = new SpringAILLMProvider(ChatClient
                .builder(mockChatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build());

        chatClientProvider.setHistory(List
                .of(new ChatMessage(ChatMessage.Role.USER, "Hi", null, null)),
                Collections.emptyMap());

        Assertions.assertEquals(1, warningCount("conversation to read"));
        Assertions.assertEquals(0, debugCount("Skipping history restoration"),
                "Expected the warning to replace the debug line, not precede it");
    }

    @Test
    void setHistory_chatClientWithConfiguredMemory_doesNotWarn() {
        var chatMemory = MessageWindowChatMemory.builder().build();
        var chatClientProvider = new SpringAILLMProvider(ChatClient
                .builder(mockChatModel)
                .defaultAdvisors(a -> a
                        .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                                .build())
                        .param(ChatMemory.CONVERSATION_ID, "conv-1"))
                .build());

        chatClientProvider.setHistory(List
                .of(new ChatMessage(ChatMessage.Role.USER, "Hi", null, null)),
                Collections.emptyMap());

        Assertions.assertEquals(0, warningCount(""));
    }

    @Test
    void setHistory_chatClientNotInspectable_logsDebugOnly() {
        var chatClientProvider = new SpringAILLMProvider(
                uninspectableClient(ChatClient.builder(mockChatModel).build()));

        chatClientProvider.setHistory(List
                .of(new ChatMessage(ChatMessage.Role.USER, "Hi", null, null)),
                Collections.emptyMap());

        Assertions.assertEquals(0, warningCount(""));
        Assertions.assertEquals(1,
                debugCount("provider was created with a ChatClient"));
        Assertions.assertEquals(1, debugCount("cannot be inspected"));
    }

    @Test
    void setHistory_chatClientRejectingPrompt_doesNotThrowAndDoesNotWarn() {
        var chatClientProvider = new SpringAILLMProvider(
                clientRejectingPrompt());

        Assertions
                .assertDoesNotThrow(
                        () -> chatClientProvider.setHistory(
                                List.of(new ChatMessage(ChatMessage.Role.USER,
                                        "Hi", null, null)),
                                Collections.emptyMap()));

        Assertions.assertEquals(0, warningCount(""));
        Assertions.assertEquals(1,
                debugCount("provider was created with a ChatClient"));
        Assertions.assertEquals(1,
                debugCount("did not accept a bare prompt()"));
        Assertions.assertEquals(1, debugCount(""),
                "Expected the rejected inspection to be reported once");
    }

    /**
     * A client that refuses the bare {@code prompt()} the inspection uses. Only
     * {@code prompt} throws, so logging and equality on the proxy still work.
     */
    private static ChatClient clientRejectingPrompt() {
        return (ChatClient) Proxy.newProxyInstance(
                ChatClient.class.getClassLoader(),
                new Class<?>[] { ChatClient.class }, (proxy, method, args) -> {
                    if (method.getName().equals("prompt")) {
                        throw new UnsupportedOperationException(
                                "prompt() requires an explicit prompt");
                    }
                    return null;
                });
    }

    /**
     * Wraps a client so that its request spec is no longer Spring AI's own
     * implementation, which is how an application-provided ChatClient looks to
     * the provider. The spec has too many methods to delegate by hand, so a
     * proxy stands in for a hand-written implementation.
     */
    private static ChatClient uninspectableClient(ChatClient delegate) {
        return (ChatClient) Proxy.newProxyInstance(
                ChatClient.class.getClassLoader(),
                new Class<?>[] { ChatClient.class }, (proxy, method, args) -> {
                    var result = method.invoke(delegate, args);
                    if (result instanceof ChatClient.ChatClientRequestSpec spec) {
                        return uninspectableSpec(spec);
                    }
                    return result;
                });
    }

    private static ChatClient.ChatClientRequestSpec uninspectableSpec(
            ChatClient.ChatClientRequestSpec delegate) {
        return (ChatClient.ChatClientRequestSpec) Proxy.newProxyInstance(
                ChatClient.class.getClassLoader(),
                new Class<?>[] { ChatClient.ChatClientRequestSpec.class },
                (proxy, method, args) -> method.invoke(delegate, args));
    }

    private long debugCount(String phrase) {
        return countEvents(Level.DEBUG, phrase);
    }

    private long warningCount(String phrase) {
        return countEvents(Level.WARN, phrase);
    }

    private long countEvents(Level level, String phrase) {
        return logger.getLoggingEvents().stream()
                .filter(event -> event.getLevel() == level)
                .filter(event -> event.getMessage().contains(phrase)).count();
    }

    @Test
    void setHistory_withChatClientConstructor_nullHistoryStillThrows() {
        var chatClient = ChatClient.builder(mockChatModel).build();
        var chatClientProvider = new SpringAILLMProvider(chatClient);
        Assertions.assertThrows(NullPointerException.class,
                () -> chatClientProvider.setHistory(null,
                        Collections.emptyMap()));
        Assertions.assertThrows(NullPointerException.class,
                () -> chatClientProvider.setHistory(List.of(), null));
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

    // --- Streaming finish_reason / abnormal termination tests ---

    @ParameterizedTest
    @NullAndEmptySource
    void stream_streamingWithMissingFinishReason_logsWarning(String reason) {
        // OpenAI-compatible backends emit "" for an unset finish_reason;
        // both "" and null must be treated as missing and surfaced via
        // the abnormal-termination warning.
        var request = createSimpleRequest("Hello");
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.just(mockChatResponse("", reason)));

        provider.stream(request).collectList().block();

        assertAbnormalTerminationWarningLogged();
    }

    @Test
    void stream_streamingCompletesEmptyWithNoChunks_logsWarning() {
        // Zero-chunk stream: nothing ever flips the terminal gate.
        // Uses the ChatClient constructor so no MessageChatMemoryAdvisor
        // sits on the chain. The advisor's stream aggregation requires
        // at least one emitted chunk to propagate the conversation id,
        // which would fail an empty-flux test before the warn-on-no-
        // terminal logic could run.
        var chatClient = ChatClient.builder(mockChatModel).build();
        var chatClientProvider = new SpringAILLMProvider(chatClient);
        var request = createSimpleRequest("Hello");
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.empty());

        chatClientProvider.stream(request).collectList().block();

        assertAbnormalTerminationWarningLogged();
    }

    @Test
    void stream_streamingWithValidFinishReasonButEmptyContent_completesWithoutError() {
        // Tool-only turns and content-filter stops produce empty text but
        // always carry a finish_reason; not errors.
        var request = createSimpleRequest("Hello");
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.just(mockChatResponse("", "STOP")));

        var results = provider.stream(request).collectList().block();

        Assertions.assertNotNull(results);
        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    void stream_streamingWithLengthFinishReason_emitsPartialContent() {
        var request = createSimpleRequest("Hello");
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.just(mockChatResponse("partial", "LENGTH")));

        var results = provider.stream(request).collectList().block();

        Assertions.assertEquals(List.of("partial"), results);
    }

    @Test
    void stream_streamingEndsWithPendingToolCalls_logsWarning() {
        // A tool-call chunk is intermediate, not terminal, so a stream
        // that ends right after one has never seen a real terminal chunk.
        // The default ChatClient executes tool calls itself and never
        // passes tool-call chunks downstream, so this scenario only
        // occurs with a custom ChatClient whose internal tool execution
        // is disabled.
        var toolAdvisorBuilder = ToolCallingAdvisor.builder()
                .toolExecutionEligibilityChecker(response -> false);
        var chatClient = ChatClient.builder(mockChatModel,
                ObservationRegistry.NOOP, null, null, toolAdvisorBuilder)
                .build();
        var chatClientProvider = new SpringAILLMProvider(chatClient);
        var request = createSimpleRequest("invoke tool");
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.just(mockChatResponseWithPendingToolCall()));

        chatClientProvider.stream(request).collectList().block();

        assertAbnormalTerminationWarningLogged();
    }

    @Test
    void stream_streamingMultiRoundTripCompletesSuccessfully_emitsTerminalText() {
        // The ChatClient executes tool calls itself: it consumes the
        // tool-call chunk, calls the tool, and requests a follow-up round
        // from the model. Only the follow-up round's chunks reach the
        // consumer.
        var request = createToolRequest();
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.just(mockChatResponseWithPendingToolCall()))
                .thenReturn(Flux.just(mockChatResponse("done", "STOP")));

        var results = provider.stream(request).collectList().block();

        Assertions.assertEquals(List.of("done"), results);
        assertAbnormalTerminationWarningNotLogged();
    }

    @Test
    void stream_streamingToolCallFollowedByNonTerminalChunks_logsWarning() {
        // Multi-roundtrip silent abort: the tool-call round-trip succeeds,
        // then the follow-up round produces text but never reaches a real
        // finish_reason - e.g. the provider truncates the stream after an
        // upstream error. The sticky check must still report this because
        // no terminal chunk was ever seen.
        var request = createToolRequest();
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.just(mockChatResponseWithPendingToolCall()))
                .thenReturn(Flux.just(mockChatResponse("partial", null)));

        provider.stream(request).collectList().block();

        assertAbnormalTerminationWarningLogged();
    }

    @Test
    void stream_streamingWithFinishReasonOnlyOnLastChunk_completesNormally() {
        // Real OpenAI streams set finish_reason only on the terminal chunk.
        var request = createSimpleRequest("Hello");
        var chunk1 = mockChatResponse("Hel", null);
        var chunk2 = mockChatResponse("lo", null);
        var terminal = mockChatResponse(" World", "STOP");
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.just(chunk1, chunk2, terminal));

        var results = provider.stream(request).collectList().block();

        Assertions.assertEquals(List.of("Hel", "lo", " World"), results);
    }

    @Test
    void stream_streamingWithNullGeneration_logsWarning() {
        // ChatResponse(emptyList()) yields getResult() == null and no
        // finish_reason: indistinguishable from an abort.
        var request = createSimpleRequest("Hello");
        var responseWithNoResult = new ChatResponse(Collections.emptyList());
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.just(responseWithNoResult));

        provider.stream(request).collectList().block();

        assertAbnormalTerminationWarningLogged();
    }

    @Test
    void stream_streamingWithNullGenerationButFollowedByFinish_completesNormally() {
        // A null-result chunk is tolerated as long as another chunk signs
        // the stream off with a finish_reason.
        var request = createSimpleRequest("Hello");
        var empty = new ChatResponse(Collections.emptyList());
        var terminal = mockChatResponse("ok", "STOP");
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.just(empty, terminal));

        var results = provider.stream(request).collectList().block();

        Assertions.assertEquals(List.of("ok"), results);
    }

    @Test
    void stream_streamingTerminalChunkFollowedByMetadataOnlyChunk_completesNormally() {
        // OpenAI's stream_options.include_usage=true appends a final
        // empty-choices chunk carrying usage metadata after the terminal
        // chunk. Once a real terminal chunk has been observed, a trailing
        // metadata-only chunk must not flip the gate back and trigger the
        // abnormal-termination warning.
        var request = createSimpleRequest("Hello");
        var terminal = mockChatResponse("Hi", "STOP");
        var trailingUsage = new ChatResponse(Collections.emptyList());
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.just(terminal, trailingUsage));

        var results = provider.stream(request).collectList().block();

        Assertions.assertEquals(List.of("Hi"), results);
        assertAbnormalTerminationWarningNotLogged();
    }

    @Test
    void stream_streamingWithNullTextInMessage_filtersOut() {
        // AssistantMessage.getText() is @Nullable; null text is filtered
        // rather than propagated as the empty string.
        var request = createSimpleRequest("Hello");
        var nullTextMessage = new AssistantMessage((String) null);
        var response = new ChatResponse(
                List.of(new Generation(nullTextMessage, ChatGenerationMetadata
                        .builder().finishReason("STOP").build())));
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.just(response));

        var results = provider.stream(request).collectList().block();

        Assertions.assertNotNull(results);
        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    void stream_streamingWithMultipleChunksAndMixedEmptyContent_emitsOnlyNonEmpty() {
        var request = createSimpleRequest("Hello");
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.just(mockChatResponse("", null),
                        mockChatResponse("Hello", null),
                        mockChatResponse("", null),
                        mockChatResponse(" World", "STOP")));

        var results = provider.stream(request).collectList().block();

        Assertions.assertEquals(List.of("Hello", " World"), results);
    }

    @Test
    void stream_streamingUpstreamErrorsDuringStream_propagatesOriginalError() {
        var request = createSimpleRequest("Hello");
        var originalError = new RuntimeException("network broken");
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.error(originalError));

        var thrown = Assertions.assertThrows(RuntimeException.class,
                () -> provider.stream(request).collectList().block());
        Assertions.assertEquals(originalError, thrown);
    }

    @Test
    void stream_streamingUpstreamErrorsAfterFinishReason_propagatesOriginalError() {
        // finish_reason was already seen, yet an upstream error must still
        // win over our abort detector.
        var request = createSimpleRequest("Hello");
        var chunk = mockChatResponse("data", "STOP");
        var originalError = new RuntimeException("broken after chunk");
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(
                        Flux.just(chunk).concatWith(Flux.error(originalError)));

        var thrown = Assertions.assertThrows(RuntimeException.class,
                () -> provider.stream(request).collectList().block());
        Assertions.assertEquals(originalError, thrown);
    }

    @Test
    void stream_streamingChatModelThrowsSynchronously_propagatesError() {
        var request = createSimpleRequest("Hello");
        var originalError = new RuntimeException("stream API down");
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenThrow(originalError);

        var thrown = Assertions.assertThrows(RuntimeException.class,
                () -> provider.stream(request).collectList().block());
        Assertions.assertEquals(originalError, thrown);
    }

    @Test
    void stream_nonStreamingEndsWithPendingToolCalls_logsWarning() {
        // The turn stopped between asking for a tool and answering with its
        // result — the silent truncation that is otherwise invisible. Needs a
        // client whose own tool execution is disabled, since the default one
        // would run the tool and ask for another round.
        var toolAdvisorBuilder = ToolCallingAdvisor.builder()
                .toolExecutionEligibilityChecker(response -> false);
        var chatClient = ChatClient.builder(mockChatModel,
                ObservationRegistry.NOOP, null, null, toolAdvisorBuilder)
                .build();
        var chatClientProvider = new SpringAILLMProvider(chatClient);
        chatClientProvider.setStreaming(false);
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(mockChatResponseWithPendingToolCall());

        chatClientProvider.stream(createSimpleRequest("invoke tool"))
                .collectList().block();

        assertWarningLogged("tool calls still pending");
    }

    @Test
    void stream_nonStreamingWithoutFinishReason_logsWarning() {
        provider.setStreaming(false);
        mockSimpleChatWithoutFinishReason("Done");

        provider.stream(createSimpleRequest("Hello")).collectList().block();

        assertWarningLogged("without a finish reason");
    }

    @Test
    void stream_nonStreamingWithFinishReason_noAbnormalWarning() {
        provider.setStreaming(false);
        mockSimpleChat("Done");

        provider.stream(createSimpleRequest("Hello")).collectList().block();

        assertNoWarningLogged("tool calls still pending");
        assertNoWarningLogged("without a finish reason");
    }

    private void mockSimpleChatWithoutFinishReason(String responseText) {
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(mockChatResponse(responseText, null));
    }

    private void assertWarningLogged(String phrase) {
        var warning = logger.getAllLoggingEvents().stream()
                .filter(event -> event.getLevel() == Level.WARN)
                .filter(event -> event.getMessage().contains(phrase))
                .findFirst();
        Assertions.assertTrue(warning.isPresent(),
                "Expected a warning containing: " + phrase);
    }

    private void assertNoWarningLogged(String phrase) {
        var warning = logger.getAllLoggingEvents().stream()
                .filter(event -> event.getMessage().contains(phrase))
                .findFirst();
        Assertions.assertFalse(warning.isPresent(),
                "Expected no warning containing: " + phrase);
    }

    private void assertAbnormalTerminationWarningLogged() {
        // The warning is emitted from a Reactor scheduler thread (Spring
        // AI's chatResponse pipeline), so we have to query across all
        // threads rather than just the current one.
        var warning = logger.getAllLoggingEvents().stream()
                .filter(event -> event.getMessage()
                        .contains("LLM stream ended without observing a "
                                + "terminal chunk"))
                .findFirst();
        Assertions.assertTrue(warning.isPresent(),
                "Expected abnormal-termination warning to be logged");
    }

    private void assertAbnormalTerminationWarningNotLogged() {
        var warning = logger.getAllLoggingEvents().stream()
                .filter(event -> event.getMessage()
                        .contains("LLM stream ended without observing a "
                                + "terminal chunk"))
                .findFirst();
        Assertions.assertFalse(warning.isPresent(),
                "Expected no abnormal-termination warning");
    }

    private void mockSimpleChat(String responseText) {
        var response = mockSimpleChatResponse(responseText);
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(response);
    }

    private ChatResponse mockSimpleChatResponse(String text) {
        // Single-chunk responses are always terminal; tag them with STOP so
        // the finish_reason gate is satisfied.
        return mockChatResponse(text, "STOP");
    }

    private static ChatResponse mockChatResponse(String text,
            String finishReason) {
        var assistantMessage = new AssistantMessage(text);
        var metadata = finishReason == null ? ChatGenerationMetadata.NULL
                : ChatGenerationMetadata.builder().finishReason(finishReason)
                        .build();
        var generation = new Generation(assistantMessage, metadata);
        return new ChatResponse(List.of(generation));
    }

    private static ChatResponse mockChatResponseWithPendingToolCall() {
        // Mirrors what a real backend emits at the end of a tool-using
        // round-trip: empty text, a tool call attached to the assistant
        // message, and a finish_reason set.
        var toolCall = new AssistantMessage.ToolCall("call_1", "function",
                "doSomething", "{}");
        var assistantMessage = AssistantMessage.builder().content("")
                .toolCalls(List.of(toolCall)).build();
        var metadata = ChatGenerationMetadata.builder().finishReason("STOP")
                .build();
        return new ChatResponse(
                List.of(new Generation(assistantMessage, metadata)));
    }

    private static LLMRequest createSimpleRequest(String message) {
        return new TestLLMRequest(message, null, Collections.emptyList(),
                new Object[0]);
    }

    private static LLMRequest createToolRequest() {
        // Registers a tool matching the call emitted by
        // mockChatResponseWithPendingToolCall so the ChatClient can
        // execute it.
        var tool = createExplicitTool("doSomething", "A test tool", null,
                args -> "tool result");
        return new TestLLMRequestWithExplicitTools("invoke tool", null,
                Collections.emptyList(), new Object[0], List.of(tool));
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
        assertNoJavaInternals(result);
        Assertions.assertEquals(0, receivedArgs.size());
    }

    @Test
    void stream_withExplicitTool_nonObjectJsonArguments_reportsExpectedShape() {
        provider.setStreaming(false);
        var receivedArgs = new ArrayList<JsonNode>();
        var explicitTool = createExplicitTool("myTool", "A test tool", null,
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
        // A model that has nothing to fill may send an empty string. That is
        // valid JSON, so it parses, but it is not the object a tool expects.
        var result = toolCallbacks.getFirst().call("\"\"");

        Assertions.assertTrue(result.startsWith("Error executing tool:"));
        Assertions.assertTrue(result.contains("JSON object"),
                "The model should be told what shape to send, but got: "
                        + result);
        assertNoJavaInternals(result);
        Assertions.assertEquals(0, receivedArgs.size());
    }

    @Test
    void stream_withExplicitTool_jsonArrayArguments_reportsExpectedShape() {
        provider.setStreaming(false);
        var explicitTool = createExplicitTool("myTool", "A test tool", null,
                args -> "ok");

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));
        mockSimpleChat("Done");

        provider.stream(request).blockFirst();

        var toolCallbacks = ((ToolCallingChatOptions) capturePrompt()
                .getOptions()).getToolCallbacks();
        var result = toolCallbacks.getFirst().call("[1, 2]");

        Assertions.assertTrue(result.startsWith("Error executing tool:"));
        Assertions.assertTrue(result.contains("JSON object"),
                "The model should be told what shape to send, but got: "
                        + result);
        assertNoJavaInternals(result);
    }

    /**
     * Asserts that a tool result carries nothing from the Java runtime. Such a
     * result goes straight back to the model, so a raw exception message would
     * both waste tokens and leak internals.
     */
    private static void assertNoJavaInternals(String result) {
        for (var leak : List.of("tools.jackson", "cannot be cast",
                "ClassLoader", "java.lang.", "[Source:")) {
            Assertions.assertFalse(result.contains(leak),
                    "Tool result relayed to the model must not contain '" + leak
                            + "', but got: " + result);
        }
    }

    // --- Response metadata tests ---

    @Test
    void stream_nonStreamingWithFinishReasonAndUsage_publishesMetadata() {
        provider.setStreaming(false);
        var collected = new ArrayList<ResponseMetadata>();
        var request = requestWithMetadataSink("Hello", collected);
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(chatResponseWithMetadata("Let me load the",
                        "max_tokens", 1200, 8));

        var results = provider.stream(request).collectList().block();

        Assertions.assertEquals(List.of("Let me load the"), results);
        Assertions.assertEquals(1, collected.size(),
                "Provider should publish the response metadata once");
        var metadata = collected.getFirst();
        Assertions.assertEquals("max_tokens", metadata.finishReason());
        Assertions.assertEquals(1200, metadata.tokenUsage().inputTokens());
        Assertions.assertEquals(8, metadata.tokenUsage().outputTokens());
        Assertions.assertEquals(1208, metadata.tokenUsage().totalTokens());
    }

    @Test
    void stream_streamingWithTerminalChunkMetadata_publishesMetadata() {
        var collected = new ArrayList<ResponseMetadata>();
        var request = requestWithMetadataSink("Hello", collected);
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.just(mockChatResponse("Hel", null),
                        chatResponseWithMetadata("lo", "stop", 100, 20)));

        var results = provider.stream(request).collectList().block();

        Assertions.assertEquals(List.of("Hel", "lo"), results);
        Assertions.assertEquals(1, collected.size(),
                "Provider should publish the response metadata once");
        var metadata = collected.getFirst();
        Assertions.assertEquals("stop", metadata.finishReason());
        Assertions.assertEquals(120, metadata.tokenUsage().totalTokens());
    }

    @Test
    void stream_nonStreamingWithFinishReasonButNoUsage_publishesReasonOnly() {
        provider.setStreaming(false);
        var collected = new ArrayList<ResponseMetadata>();
        var request = requestWithMetadataSink("Hello", collected);
        var generation = new Generation(new AssistantMessage("Done"),
                ChatGenerationMetadata.builder().finishReason("stop").build());
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class))).thenReturn(
                ChatResponse.builder().generations(List.of(generation))
                        .metadata(ChatResponseMetadata.builder().usage(null)
                                .build())
                        .build());

        provider.stream(request).collectList().block();

        Assertions.assertEquals(1, collected.size(),
                "A reported finish reason alone is worth publishing");
        Assertions.assertEquals("stop", collected.getFirst().finishReason());
        Assertions.assertNull(collected.getFirst().tokenUsage());
    }

    @Test
    void stream_nonStreamingWithoutMetadata_sinkNotCalled() {
        provider.setStreaming(false);
        var collected = new ArrayList<ResponseMetadata>();
        var request = requestWithMetadataSink("Hello", collected);
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(mockChatResponse("plain", null));

        provider.stream(request).collectList().block();

        Assertions.assertTrue(collected.isEmpty(),
                "No finish reason and no usage means nothing to publish");
    }

    @Test
    void stream_streamingErrorsAfterMetadataChunk_metadataStillPublished() {
        // The failed turn was still billed for what ran before the error; the
        // sink must have received everything observed up to that point.
        var collected = new ArrayList<ResponseMetadata>();
        var request = requestWithMetadataSink("Hello", collected);
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux
                        .just(chatResponseWithMetadata("Hi", "tool_use", 40,
                                10))
                        .concatWith(Flux.error(
                                new RuntimeException("network broken"))));

        var response = provider.stream(request).collectList();
        Assertions.assertThrows(RuntimeException.class, response::block);

        var metadata = collected.getLast();
        Assertions.assertEquals("tool_use", metadata.finishReason());
        Assertions.assertEquals(50, metadata.tokenUsage().totalTokens());
    }

    @Test
    void stream_nonStreamingUsageWithoutTotal_totalDerivedFromComponents() {
        // A backend that reports prompt and completion counts but no total
        // still reported the usage; it must not be discarded.
        provider.setStreaming(false);
        var collected = new ArrayList<ResponseMetadata>();
        var request = requestWithMetadataSink("Hello", collected);
        var generation = new Generation(new AssistantMessage("Done"),
                ChatGenerationMetadata.builder().finishReason("stop").build());
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class)))
                .thenReturn(ChatResponse.builder()
                        .generations(List.of(generation))
                        .metadata(ChatResponseMetadata.builder()
                                .usage(new DefaultUsage(100, 20, null)).build())
                        .build());

        provider.stream(request).collectList().block();

        var usage = collected.getLast().tokenUsage();
        Assertions.assertEquals(100, usage.inputTokens());
        Assertions.assertEquals(20, usage.outputTokens());
        Assertions.assertEquals(120, usage.totalTokens());
    }

    @Test
    void stream_nonStreamingUsageWithZeroTotal_totalDerivedFromComponents() {
        // A backend that reports the components but leaves the total at zero
        // still reported the usage; the total must be derived, not dropped.
        provider.setStreaming(false);
        var collected = new ArrayList<ResponseMetadata>();
        var request = requestWithMetadataSink("Hello", collected);
        mockCallWithUsage(new DefaultUsage(100, 20, 0));

        provider.stream(request).collectList().block();

        Assertions.assertEquals(120,
                collected.getLast().tokenUsage().totalTokens(),
                "An unreported total must be derived from the components");
    }

    @Test
    void stream_nonStreamingUsageWithoutInputTokens_publishesOutputTokensOnly() {
        provider.setStreaming(false);
        var collected = new ArrayList<ResponseMetadata>();
        var request = requestWithMetadataSink("Hello", collected);
        mockCallWithUsage(new DefaultUsage(0, 20, 0));

        provider.stream(request).collectList().block();

        var usage = collected.getLast().tokenUsage();
        Assertions.assertNull(usage.inputTokens());
        Assertions.assertEquals(20, usage.outputTokens());
        Assertions.assertNull(usage.totalTokens(),
                "A total cannot be derived without the input count");
    }

    @Test
    void stream_nonStreamingUsageWithoutOutputTokens_publishesInputTokensOnly() {
        provider.setStreaming(false);
        var collected = new ArrayList<ResponseMetadata>();
        var request = requestWithMetadataSink("Hello", collected);
        mockCallWithUsage(new DefaultUsage(100, 0, 0));

        provider.stream(request).collectList().block();

        var usage = collected.getLast().tokenUsage();
        Assertions.assertEquals(100, usage.inputTokens());
        Assertions.assertNull(usage.outputTokens());
        Assertions.assertNull(usage.totalTokens(),
                "A total cannot be derived without the output count");
    }

    @Test
    void stream_streamingAcrossToolRoundTrips_publishesCumulativeUsage() {
        // The default ChatClient consumes the tool-call round trip internally
        // and re-emits the follow-up round with usage already accumulated
        // across the round trips, so the last observed usage covers the whole
        // turn. This test pins that behavior — the collector's last-wins
        // accounting depends on it.
        var collected = new ArrayList<ResponseMetadata>();
        var tool = createExplicitTool("doSomething", "A test tool", null,
                args -> "tool result");
        var request = requestWithMetadataSink("invoke tool", List.of(tool),
                collected);
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.just(toolCallResponseWithUsage(30, 10)))
                .thenReturn(Flux.just(
                        chatResponseWithMetadata("done", "stop", 50, 20)));

        var results = provider.stream(request).collectList().block();

        Assertions.assertEquals(List.of("done"), results);
        var metadata = collected.getLast();
        Assertions.assertEquals("stop", metadata.finishReason());
        Assertions.assertEquals(80, metadata.tokenUsage().inputTokens());
        Assertions.assertEquals(30, metadata.tokenUsage().outputTokens());
        Assertions.assertEquals(110, metadata.tokenUsage().totalTokens());
    }

    @Test
    void stream_streamingResendsGrowingUsageTally_lastValueWins() {
        // Some backends resend a growing cumulative tally on every chunk
        // instead of reporting the counts once at the end; the published
        // usage must be the final tally, not a sum of the resends.
        var collected = new ArrayList<ResponseMetadata>();
        var request = requestWithMetadataSink("Hello", collected);
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(
                        Flux.just(chatResponseWithMetadata("Hel", null, 30, 4),
                                chatResponseWithMetadata("lo", null, 30, 9),
                                chatResponseWithMetadata("!", "stop", 30, 12)));

        provider.stream(request).collectList().block();

        var usage = collected.getLast().tokenUsage();
        Assertions.assertEquals(30, usage.inputTokens());
        Assertions.assertEquals(12, usage.outputTokens());
        Assertions.assertEquals(42, usage.totalTokens());
    }

    @Test
    void metadataSink_notOverridden_returnsNoOpConsumer() {
        var request = createSimpleRequest("Hello");

        Assertions.assertNotNull(request.metadataSink());
        Assertions
                .assertDoesNotThrow(() -> request.metadataSink().accept(null));
    }

    private static LLMRequest requestWithMetadataSink(String message,
            List<ResponseMetadata> collected) {
        return requestWithMetadataSink(message, List.of(), collected);
    }

    private static LLMRequest requestWithMetadataSink(String message,
            List<LLMProvider.ToolSpec> explicitTools,
            List<ResponseMetadata> collected) {
        return new LLMRequest() {
            @Override
            public String userMessage() {
                return message;
            }

            @Override
            public List<AIAttachment> attachments() {
                return Collections.emptyList();
            }

            @Override
            public String systemPrompt() {
                return null;
            }

            @Override
            public Object[] tools() {
                return new Object[0];
            }

            @Override
            public List<LLMProvider.ToolSpec> explicitTools() {
                return explicitTools;
            }

            @Override
            public Consumer<ResponseMetadata> metadataSink() {
                return collected::add;
            }
        };
    }

    private void mockCallWithUsage(Usage usage) {
        var generation = new Generation(new AssistantMessage("Done"),
                ChatGenerationMetadata.builder().finishReason("stop").build());
        Mockito.when(mockChatModel.call(Mockito.any(Prompt.class))).thenReturn(
                ChatResponse.builder().generations(List.of(generation))
                        .metadata(ChatResponseMetadata.builder().usage(usage)
                                .build())
                        .build());
    }

    private static ChatResponse chatResponseWithMetadata(String text,
            String finishReason, int inputTokens, int outputTokens) {
        var generationMetadata = finishReason == null
                ? ChatGenerationMetadata.NULL
                : ChatGenerationMetadata.builder().finishReason(finishReason)
                        .build();
        var generation = new Generation(new AssistantMessage(text),
                generationMetadata);
        return ChatResponse.builder().generations(List.of(generation))
                .metadata(ChatResponseMetadata.builder()
                        .usage(new DefaultUsage(inputTokens, outputTokens))
                        .build())
                .build();
    }

    private static ChatResponse toolCallResponseWithUsage(int inputTokens,
            int outputTokens) {
        var response = mockChatResponseWithPendingToolCall();
        return ChatResponse.builder().generations(response.getResults())
                .metadata(ChatResponseMetadata.builder()
                        .usage(new DefaultUsage(inputTokens, outputTokens))
                        .build())
                .build();
    }

    @Test
    void stream_withExplicitTool_nullOrBlankArguments_passesEmptyObject() {
        provider.setStreaming(false);
        var receivedArgs = new ArrayList<JsonNode>();
        var explicitTool = createExplicitTool("myTool", "A test tool", null,
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
        var callback = toolCallbacks.getFirst();

        Assertions.assertEquals("ok", callback.call(null));
        Assertions.assertEquals("ok", callback.call("  "));
        Assertions.assertEquals(2, receivedArgs.size());
        var allEmptyObjects = receivedArgs.stream().allMatch(
                args -> args.isObject() && args.propertyNames().isEmpty());
        Assertions.assertTrue(allEmptyObjects,
                "Missing arguments should be parsed as an empty object");
    }

    @Test
    void stream_withExplicitToolThrowingToolException_relaysMessage() {
        provider.setStreaming(false);
        var explicitTool = createExplicitTool("myTool", "A test tool", null,
                args -> {
                    throw new ToolException("Unknown column 'foo'");
                });

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));
        mockSimpleChat("Done");

        provider.stream(request).blockFirst();

        var toolCallbacks = ((ToolCallingChatOptions) capturePrompt()
                .getOptions()).getToolCallbacks();
        var result = toolCallbacks.getFirst().call("{}");

        Assertions.assertEquals("Error executing tool: Unknown column 'foo'",
                result);
    }

    @Test
    void stream_withExplicitToolThrowingUnexpectedException_returnsGenericError() {
        provider.setStreaming(false);
        var explicitTool = createExplicitTool("myTool", "A test tool", null,
                args -> {
                    throw new RuntimeException("internal detail");
                });

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));
        mockSimpleChat("Done");

        provider.stream(request).blockFirst();

        var toolCallbacks = ((ToolCallingChatOptions) capturePrompt()
                .getOptions()).getToolCallbacks();
        var result = toolCallbacks.getFirst().call("{}");

        Assertions.assertEquals("Error executing tool.", result);
    }

    @Test
    void stream_withExplicitToolThrowingUnexpectedException_logsError() {
        provider.setStreaming(false);
        var toolFailure = new RuntimeException("internal detail");
        var explicitTool = createExplicitTool("myTool", "A test tool", null,
                args -> {
                    throw toolFailure;
                });

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));
        mockSimpleChat("Done");

        provider.stream(request).blockFirst();

        var toolCallbacks = ((ToolCallingChatOptions) capturePrompt()
                .getOptions()).getToolCallbacks();
        toolCallbacks.getFirst().call("{}");

        // The generic result hides the failure from the LLM, so the log is
        // the only place where the actual error is visible.
        var error = logger.getAllLoggingEvents().stream()
                .filter(event -> event.getLevel() == Level.ERROR
                        && event.getThrowable().orElse(null) == toolFailure)
                .findFirst();
        Assertions.assertTrue(error.isPresent(),
                "Expected the tool failure to be logged");
    }

    @Test
    void stream_withExplicitToolNullSchema_substitutesNoParametersSchema() {
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
        assertNoParametersSchema(toolDef.inputSchema());
    }

    @Test
    void stream_withExplicitToolBlankSchema_substitutesNoParametersSchema() {
        provider.setStreaming(false);
        var explicitTool = createExplicitTool("simpleTool", "A simple tool",
                "   ", args -> "done");

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));
        mockSimpleChat("OK");

        provider.stream(request).blockFirst();

        var toolCallbacks = ((ToolCallingChatOptions) capturePrompt()
                .getOptions()).getToolCallbacks();
        assertNoParametersSchema(
                toolCallbacks.getFirst().getToolDefinition().inputSchema());
    }

    @Test
    void stream_withExplicitToolNullSchema_executeReceivesEmptyArguments() {
        provider.setStreaming(false);
        var receivedArgs = new ArrayList<JsonNode>();
        var explicitTool = createExplicitTool("simpleTool", "A simple tool",
                null, args -> {
                    receivedArgs.add(args);
                    return "done";
                });

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));
        mockSimpleChat("OK");

        provider.stream(request).blockFirst();

        var toolCallbacks = ((ToolCallingChatOptions) capturePrompt()
                .getOptions()).getToolCallbacks();
        // The model may fill the placeholder schema that was substituted for
        // the missing one; a tool that declared no parameters must not see
        // that.
        var result = toolCallbacks.getFirst()
                .call("{\"reason\":\"checking the form\"}");

        Assertions.assertEquals("done", result);
        Assertions.assertEquals(1, receivedArgs.size());
        Assertions.assertTrue(receivedArgs.getFirst().isEmpty(),
                "A tool that declared no parameters must receive an empty "
                        + "arguments object, got: " + receivedArgs.getFirst());
    }

    @Test
    void stream_withExplicitToolSchema_passesSchemaThroughVerbatim() {
        provider.setStreaming(false);
        var userSchema = "{\"type\":\"object\",\"properties\":{\"city\":"
                + "{\"type\":\"string\"}},\"required\":[\"city\"]}";
        var explicitTool = createExplicitTool("myTool", "A test tool",
                userSchema, args -> "done");

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));
        mockSimpleChat("OK");

        provider.stream(request).blockFirst();

        var toolCallbacks = ((ToolCallingChatOptions) capturePrompt()
                .getOptions()).getToolCallbacks();
        Assertions.assertEquals(userSchema,
                toolCallbacks.getFirst().getToolDefinition().inputSchema(),
                "A declared schema must reach the tool definition unmodified");
    }

    @Test
    void stream_withExplicitToolEmptyPropertiesSchema_passesSchemaThroughVerbatim() {
        // Substitution is limited to null/blank schemas: a syntactically
        // valid schema authored by the user is passed through as-is, even
        // when its properties object is empty.
        provider.setStreaming(false);
        var userSchema = "{\"type\":\"object\",\"properties\":{}}";
        var explicitTool = createExplicitTool("myTool", "A test tool",
                userSchema, args -> "done");

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));
        mockSimpleChat("OK");

        provider.stream(request).blockFirst();

        var toolCallbacks = ((ToolCallingChatOptions) capturePrompt()
                .getOptions()).getToolCallbacks();
        Assertions.assertEquals(userSchema,
                toolCallbacks.getFirst().getToolDefinition().inputSchema());
    }

    /**
     * Asserts the schema is the non-empty no-parameters shape: an object with
     * at least one property, all of them optional. A tool without a declared
     * property makes models disagree on what to send as arguments, and some LLM
     * APIs reject the request that replays such a tool call.
     */
    private static void assertNoParametersSchema(String inputSchema) {
        var schema = JacksonUtils.readTree(inputSchema);
        Assertions.assertEquals("object", schema.path("type").asString());
        Assertions.assertTrue(schema.path("properties").size() > 0,
                "Schema must declare at least one property, got: " + schema);
        Assertions.assertEquals(0, schema.path("required").size(),
                "All declared properties must be optional, got: " + schema);
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
