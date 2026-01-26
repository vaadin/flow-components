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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

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
import org.springframework.ai.tool.annotation.Tool;

import com.vaadin.flow.component.ai.provider.LLMProvider.Attachment;
import com.vaadin.flow.component.ai.provider.LLMProvider.LLMRequest;

import reactor.core.publisher.Flux;

public class SpringAiLLMProviderTest {

    private ChatModel mockChatModel;

    private SpringAiLLMProvider provider;
    private SpringAiLLMProvider streamingProvider;

    @Before
    public void setup() {
        mockChatModel = Mockito.mock(ChatModel.class);
        provider = new SpringAiLLMProvider(mockChatModel, false);
        streamingProvider = new SpringAiLLMProvider(mockChatModel, true);
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
                () -> new SpringAiLLMProvider((ChatModel) null, false));
    }

    @Test
    public void constructor_withNullChatClient_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class,
                () -> new SpringAiLLMProvider((ChatClient) null, false));
    }

    @Test
    public void constructor_withChatClient_nonStreaming_returnsResponse() {
        var chatClient = ChatClient.builder(mockChatModel).build();
        var chatClientProvider = new SpringAiLLMProvider(chatClient, false);
        var request = createSimpleRequest("Hello");
        mockSimpleChat("Full response");

        var results = chatClientProvider.stream(request).collectList().block();

        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("Full response", results.getFirst());
    }

    @Test
    public void constructor_withChatClient_streaming_returnsStreamedTokens() {
        var chatClient = ChatClient.builder(mockChatModel).build();
        var chatClientStreamingProvider = new SpringAiLLMProvider(chatClient,
                true);
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
        var request = createSimpleRequest("Hello");
        mockSimpleChat("");
        var results = provider.stream(request).collectList().block();
        Assert.assertNotNull(results);
        Assert.assertTrue(results.isEmpty());
    }

    @Test
    public void stream_nullTextResponse_returnsEmpty() {
        var request = createSimpleRequest("Hello");
        mockSimpleChat(null);
        var results = provider.stream(request).collectList().block();
        Assert.assertNotNull(results);
        Assert.assertTrue(results.isEmpty());
    }

    @Test
    public void stream_withSystemPromptInRequest_includesSystemMessage() {
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
        var request = createSimpleRequest("Hello");
        mockSimpleChat("Response");

        provider.stream(request).blockFirst();

        var hasSystemMessage = capturePrompt().getInstructions().stream()
                .anyMatch(SystemMessage.class::isInstance);
        Assert.assertFalse(hasSystemMessage);
    }

    @Test
    public void stream_withEmptySystemPrompt_noSystemMessage() {
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
        var request = new TestLLMRequest("Hello", null, null, new Object[0]);
        mockSimpleChat("Hi");
        var result = provider.stream(request).blockFirst();
        Assert.assertEquals("Hi", result);
    }

    @Test
    public void stream_withNullAttachmentInList_throwsNullPointerException() {
        var attachment = new TestAttachment(
                "Test".getBytes(StandardCharsets.UTF_8), "text/plain",
                "test.txt");
        var attachments = new ArrayList<Attachment>();
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
        var attachment = new TestAttachment("data".getBytes(),
                "application/octet-stream", "file.bin");
        var request = new TestLLMRequest("Process this", null,
                List.of(attachment), new Object[0]);

        mockSimpleChat("Done");

        provider.stream(request).blockFirst();

        Mockito.verify(mockChatModel).call(Mockito.any(Prompt.class));
    }

    @Test
    public void stream_withPdfAttachment_handlesPdf() {
        var pdfData = "PDF binary content".getBytes(StandardCharsets.UTF_8);
        var attachment = new TestAttachment(pdfData, "application/pdf",
                "document.pdf");
        var request = new TestLLMRequest("Summarize this document", null,
                List.of(attachment), new Object[0]);

        mockSimpleChat("Summary");

        var result = provider.stream(request).blockFirst();
        Assert.assertEquals("Summary", result);

        Mockito.verify(mockChatModel).call(Mockito.any(Prompt.class));
    }

    @Test
    public void stream_withBinaryPdfData_handlesBinaryPdf() {
        // Binary PDF data should be handled correctly
        var binaryPdfData = new byte[] { 0x25, 0x50, 0x44, 0x46, (byte) 0xFF,
                (byte) 0xFE, (byte) 0x00, (byte) 0x80 };
        var attachment = new TestAttachment(binaryPdfData, "application/pdf",
                "binary.pdf");
        var request = new TestLLMRequest("Summarize", null, List.of(attachment),
                new Object[0]);

        mockSimpleChat("Summary");

        var result = provider.stream(request).blockFirst();
        Assert.assertEquals("Summary", result);

        Mockito.verify(mockChatModel).call(Mockito.any(Prompt.class));
    }

    @Test
    public void stream_withImageAttachment_processesImage() {
        var imageData = "fake-image-data".getBytes();
        var attachment = new TestAttachment(imageData, "image/png", "test.png");
        var request = new TestLLMRequest("Describe this image", null,
                List.of(attachment), new Object[0]);

        mockSimpleChat("It's a test");

        var result = provider.stream(request).blockFirst();
        Assert.assertEquals("It's a test", result);

        Mockito.verify(mockChatModel).call(Mockito.any(Prompt.class));
    }

    @Test
    public void stream_withTextAttachment_processesText() {
        var textContent = "Test UTF-8: é à ü";
        var attachment = new TestAttachment(
                textContent.getBytes(StandardCharsets.UTF_8), "text/plain",
                "test.txt");
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

        var results = streamingProvider.stream(request).collectList().block();
        Assert.assertEquals(tokens, results);
    }

    @Test
    public void stream_withStreamingModelAndTool_executesTool() {
        var toolObject = new SampleToolsClass();
        var request = new TestLLMRequest("Get temperature", null,
                Collections.emptyList(), new Object[] { toolObject });

        var tokens = List.of("The", " temperature", " is", " 22°C");
        Mockito.when(mockChatModel.stream(Mockito.any(Prompt.class)))
                .thenReturn(Flux.fromIterable(tokens.stream()
                        .map(this::mockSimpleChatResponse).toList()));

        var results = streamingProvider.stream(request).collectList().block();

        Assert.assertNotNull(results);
        Assert.assertEquals(tokens, results);
        Mockito.verify(mockChatModel).stream(Mockito.any(Prompt.class));
    }

    @Test
    public void stream_withNullTools_handlesNullToolsArray() {
        var request = new TestLLMRequest("Hello", null, Collections.emptyList(),
                null);
        mockSimpleChat("Hi");

        var result = provider.stream(request).blockFirst();

        Assert.assertEquals("Hi", result);
        Mockito.verify(mockChatModel).call(Mockito.any(Prompt.class));
    }

    @Test
    public void stream_withEmptyToolsArray_handlesEmptyTools() {
        var request = new TestLLMRequest("Hello", null, Collections.emptyList(),
                new Object[0]);
        mockSimpleChat("Hi");

        var result = provider.stream(request).blockFirst();

        Assert.assertEquals("Hi", result);
        Mockito.verify(mockChatModel).call(Mockito.any(Prompt.class));
    }

    @Test
    public void chatMemory_retainsHistory() {
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
        var requestCount = 20;

        // Each request adds 2 messages: UserMessage and AiMessage
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
        var imageAttachment = new TestAttachment("fake-image".getBytes(),
                "image/jpeg", "photo.jpg");
        var textAttachment = new TestAttachment(
                "Hello world".getBytes(StandardCharsets.UTF_8), "text/plain",
                "doc.txt");
        var pdfAttachment = new TestAttachment(
                "PDF content".getBytes(StandardCharsets.UTF_8),
                "application/pdf", "file.pdf");
        var unsupportedBinaryAttachment = new TestAttachment(
                "binary".getBytes(), "application/octet-stream", "data.bin");
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
        var audioData = "fake-audio-data".getBytes();
        var attachment = new TestAttachment(audioData, "audio/mpeg",
                "audio.mp3");
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
        var videoData = "fake-video-data".getBytes();
        var attachment = new TestAttachment(videoData, "video/mp4",
                "video.mp4");
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
            List<Attachment> attachments,
            Object[] tools) implements LLMRequest {
    }

    private record TestAttachment(byte[] data, String contentType,
            String fileName) implements Attachment {
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

    private static class PrivateToolClass {
        public String getPrivateMethodResult() {
            return "Private method result";
        }

        @Tool(description = "Private method tool")
        private String privateMethod() {
            return getPrivateMethodResult();
        }
    }

    private static class ErrorThrowingToolClass {
        public String getErrorMessage() {
            return "Tool execution failed";
        }

        @Tool(description = "Throws an error")
        public String throwError() {
            throw new RuntimeException(getErrorMessage());
        }
    }
}
