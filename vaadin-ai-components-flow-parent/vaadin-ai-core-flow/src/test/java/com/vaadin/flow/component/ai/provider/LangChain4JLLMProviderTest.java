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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.event.Level;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.common.ChatMessage;
import com.vaadin.flow.component.ai.provider.LLMProvider.LLMRequest;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.tests.MockUIExtension;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import tools.jackson.databind.JsonNode;

class LangChain4JLLMProviderTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private ChatModel mockChatModel;
    private StreamingChatModel mockStreamingChatModel;

    private LangChain4JLLMProvider provider;
    private LangChain4JLLMProvider streamingProvider;

    private TestLogger logger = TestLoggerFactory
            .getTestLogger(LangChain4JLLMProvider.class);

    @BeforeEach
    void setup() {
        mockChatModel = Mockito.mock(ChatModel.class);
        mockStreamingChatModel = Mockito.mock(StreamingChatModel.class);
        provider = new LangChain4JLLMProvider(mockChatModel);
        streamingProvider = new LangChain4JLLMProvider(mockStreamingChatModel);
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
                () -> new LangChain4JLLMProvider((ChatModel) null));
    }

    @Test
    void constructor_withNullStreamingChatModel_throwsNullPointerException() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new LangChain4JLLMProvider((StreamingChatModel) null));
    }

    @Test
    void chatMemory_retainsHistory() {
        var response1 = mockSimpleResponse("Response 1");
        var response2 = mockSimpleResponse("Response 2");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2);

        provider.stream(createSimpleRequest("First message")).blockFirst();
        provider.stream(createSimpleRequest("Second message")).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(2)).chat(captor.capture());

        var secondRequestMessages = captor.getAllValues().get(1).messages();
        Assertions.assertEquals(3, secondRequestMessages.size());
    }

    @Test
    void stream_withStreamingModel_returnsStreamedTokens() {
        var request = createSimpleRequest("Hello");
        var tokens = List.of("Hello", " ", "World");

        Mockito.doAnswer(invocation -> {
            StreamingChatResponseHandler handler = invocation.getArgument(1);
            tokens.forEach(handler::onPartialResponse);
            var aiMessage = Mockito.mock(AiMessage.class);
            Mockito.when(aiMessage.hasToolExecutionRequests())
                    .thenReturn(false);
            var response = Mockito.mock(ChatResponse.class);
            Mockito.when(response.aiMessage()).thenReturn(aiMessage);
            handler.onCompleteResponse(response);
            return null;
        }).when(mockStreamingChatModel).chat(Mockito.any(ChatRequest.class),
                Mockito.any(StreamingChatResponseHandler.class));

        var results = streamingProvider.stream(request).collectList().block();
        Assertions.assertEquals(tokens, results);
    }

    @Test
    void stream_withNonStreamingModel_returnsResponse() {
        var request = createSimpleRequest("Hello");
        mockSimpleChat(request, "Full response");

        var results = provider.stream(request).collectList().block();

        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("Full response", results.getFirst());
    }

    @Test
    void stream_chatModelThrowsException_propagatesError() {
        var request = createSimpleRequest("Hello");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenThrow(new RuntimeException("API error"));
        Assertions.assertThrows(RuntimeException.class,
                () -> provider.stream(request).blockFirst());
    }

    @Test
    void stream_streamingModelReportsError_propagatesError() {
        var request = createSimpleRequest("Hello");
        var originalError = new RuntimeException("Stream error");
        Mockito.doAnswer(invocation -> {
            StreamingChatResponseHandler handler = invocation.getArgument(1);
            handler.onError(originalError);
            return null;
        }).when(mockStreamingChatModel).chat(Mockito.any(ChatRequest.class),
                Mockito.any(StreamingChatResponseHandler.class));

        // Bounded block: a swallowed error would leave the sink open
        // forever instead of failing the test
        var thrown = Assertions.assertThrows(RuntimeException.class,
                () -> streamingProvider.stream(request)
                        .blockFirst(Duration.ofSeconds(5)));
        Assertions.assertSame(originalError, thrown);
    }

    @Test
    void stream_emptyTextResponse_returnsEmpty() {
        var request = createSimpleRequest("Hello");
        var response = mockSimpleResponse("");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);
        var results = provider.stream(request).collectList().block();
        Assertions.assertNotNull(results);
        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    void stream_nullTextResponse_returnsEmpty() {
        var request = createSimpleRequest("Hello");
        var response = mockSimpleResponse(null);
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);
        var results = provider.stream(request).collectList().block();
        Assertions.assertNotNull(results);
        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    void stream_withSystemPromptInRequest_usesRequestPrompt() {
        var request = new TestLLMRequest("Hello", "You are a helpful assistant",
                Collections.emptyList(), new Object[0]);

        mockSimpleChat(request, "Response");

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());

        var messages = captor.getValue().messages();
        Assertions.assertTrue(
                messages.stream().anyMatch(SystemMessage.class::isInstance),
                "Should contain system message");
    }

    @Test
    void stream_withNullSystemPrompt_noSystemMessage() {
        var request = createSimpleRequest("Hello");

        mockSimpleChat(request, "Response");

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());

        var messages = captor.getValue().messages();
        Assertions.assertFalse(
                messages.stream().anyMatch(SystemMessage.class::isInstance),
                "Should not contain system message");
    }

    @Test
    void stream_withEmptySystemPrompt_noSystemMessage() {
        var request = new TestLLMRequest("Hello", "   ",
                Collections.emptyList(), new Object[0]);

        mockSimpleChat(request, "Response");

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());

        var messages = captor.getValue().messages();
        Assertions.assertFalse(
                messages.stream().anyMatch(SystemMessage.class::isInstance),
                "Should not contain system message");
    }

    @Test
    void stream_preservesChatHistoryAcrossRequests() {
        var request1 = createSimpleRequest("Hello");
        var response1 = mockSimpleResponse("Hi there");
        var request2 = createSimpleRequest("How are you?");
        var response2 = mockSimpleResponse("I'm good");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2);
        provider.stream(request1).blockFirst();
        provider.stream(request2).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(2)).chat(captor.capture());

        var allMessages = captor.getAllValues();
        Assertions.assertEquals(1, allMessages.get(0).messages().size(),
                "First call should have 1 user message");
        Assertions.assertEquals(3, allMessages.get(1).messages().size(),
                "Second call should have 3 messages (user1, ai1, user2)");
    }

    @Test
    void stream_withNullAiMessage_returnsEmptyMessage() {
        var request = createSimpleRequest("Hello");

        var response = Mockito.mock(ChatResponse.class);
        Mockito.when(response.aiMessage()).thenReturn(null);
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        var results = provider.stream(request).collectList().block();

        Assertions.assertNotNull(results);
        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    void stream_withNullAiMessage_keepsChatMemoryUsable() {
        var nullAiResponse = Mockito.mock(ChatResponse.class);
        Mockito.when(nullAiResponse.aiMessage()).thenReturn(null);
        var secondResponse = mockSimpleResponse("Second");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(nullAiResponse, secondResponse);

        // Bounded blocks: completing the turn is what is under test here, so
        // a missing terminal signal must fail instead of hanging
        provider.stream(createSimpleRequest("First")).collectList()
                .block(Duration.ofSeconds(5));
        var results = provider.stream(createSimpleRequest("Second question"))
                .collectList().block(Duration.ofSeconds(5));

        Assertions.assertEquals(List.of("Second"), results);

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(2)).chat(captor.capture());
        var messages = captor.getAllValues().get(1).messages();
        Assertions.assertEquals(2, messages.size(),
                "A response without an AI message must not add that message to "
                        + "chat memory, but got: " + messages);
        var memoryTexts = getUserMessageContents(captor.getAllValues().get(1),
                TextContent.class).stream().map(TextContent::text).toList();
        Assertions.assertEquals(List.of("First", "Second question"),
                memoryTexts, "Both user turns should still be in chat memory");
    }

    @Test
    void stream_withMaxMessagesLimit_dropsOldestMessages() {
        var requestCount = 20;

        // Each request adds 2 messages: UserMessage and AiMessage
        IntStream.range(0, requestCount).forEach(i -> {
            var request = createSimpleRequest("Message " + i);
            mockSimpleChat(request, "Response " + i);
        });

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(requestCount))
                .chat(captor.capture());

        var lastRequest = captor.getAllValues().get(requestCount - 1);
        var messageCount = lastRequest.messages().size();
        Assertions.assertTrue(messageCount <= 30,
                "Message count should not exceed memory limit, got: "
                        + messageCount);

        var userMessageTextContents = getUserMessageContents(lastRequest,
                TextContent.class).stream().map(TextContent::text).toList();
        Assertions.assertFalse(
                userMessageTextContents.stream()
                        .anyMatch(text -> text.contains("Message 0")),
                "Should not contain very old messages");
        Assertions
                .assertTrue(
                        userMessageTextContents.stream()
                                .anyMatch(text -> text.contains(
                                        "Message " + (requestCount - 1))),
                        "Should contain recent messages");
    }

    @Test
    void stream_withImageAttachment_convertsToBase64() {
        var imageData = "fake-image-data".getBytes();
        var attachment = new AIAttachment("test.png", "image/png", imageData);
        var request = new TestLLMRequest("Describe this image", null,
                List.of(attachment), new Object[0]);

        mockSimpleChat(request, "It's a test");

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());

        var userMessageContents = getUserMessageContents(captor.getValue(),
                ImageContent.class);
        Assertions.assertFalse(userMessageContents.isEmpty(),
                "Should contain image content");
    }

    @Test
    void stream_withTextAttachment_usesUTF8Encoding() {
        var textContent = "Test UTF-8: é à ü";
        var attachment = new AIAttachment("test.txt", "text/plain",
                textContent.getBytes(StandardCharsets.UTF_8));
        var request = new TestLLMRequest("Summarize this", null,
                List.of(attachment), new Object[0]);

        mockSimpleChat(request, "Summary");

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());

        var messages = captor.getValue().messages();
        var userMessage = (UserMessage) messages.getFirst();
        var textContentPreserved = userMessage.contents().stream()
                .filter(TextContent.class::isInstance)
                .map(TextContent.class::cast).map(TextContent::text)
                .anyMatch(text -> text.contains(textContent));

        Assertions.assertTrue(textContentPreserved);
    }

    @Test
    void stream_withInvalidUtf8TextAttachment_replacesInvalidSequences() {
        // Lone continuation byte: not decodable as UTF-8. Text attachments
        // are decoded leniently, so it is replaced instead of rejected.
        var attachment = new AIAttachment("broken.txt", "text/plain",
                new byte[] { 0x41, (byte) 0x80, 0x42 });
        var request = new TestLLMRequest("Summarize this", null,
                List.of(attachment), new Object[0]);

        mockSimpleChat(request, "Summary");

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());
        var texts = getUserMessageContents(captor.getValue(), TextContent.class)
                .stream().map(TextContent::text).toList();
        Assertions.assertTrue(
                texts.stream().anyMatch(text -> text.contains("A\uFFFDB")),
                "Invalid UTF-8 should be replaced, but got: " + texts);
    }

    @Test
    void stream_withNullAttachments_returnsResponse() {
        var request = new TestLLMRequest("Hello", null, null, new Object[0]);
        mockSimpleChat(request, "Hi");
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
        var response = mockSimpleResponse("hi");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        Assertions.assertThrows(NullPointerException.class,
                () -> provider.stream(request).blockFirst());
    }

    @Test
    void stream_withUnsupportedAttachmentType_ignoresAttachment() {
        var attachment = new AIAttachment("file.bin",
                "application/octet-stream", "data".getBytes());
        var request = new TestLLMRequest("Process this", null,
                List.of(attachment), new Object[0]);

        mockSimpleChat(request, "Done");

        Mockito.verify(mockChatModel).chat(Mockito.any(ChatRequest.class));
    }

    @Test
    void stream_withPdfAttachment_handlesPdf() {
        var pdfData = "PDF binary content".getBytes(StandardCharsets.UTF_8);
        var attachment = new AIAttachment("document.pdf", "application/pdf",
                pdfData);
        var request = new TestLLMRequest("Summarize this document", null,
                List.of(attachment), new Object[0]);

        mockSimpleChat(request, "Summary");

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());

        var userMessage = (UserMessage) captor.getValue().messages().getFirst();
        var pdfContent = userMessage.contents().stream()
                .filter(PdfFileContent.class::isInstance).findFirst()
                .orElse(null);

        Assertions.assertNotNull(pdfContent,
                "Should include PDF content as PdfFileContent");
    }

    @Test
    void stream_withBinaryPdfData_handlesBinaryPdf() {
        // Binary PDF data should be handled correctly with base64 encoding
        var binaryPdfData = new byte[] { 0x25, 0x50, 0x44, 0x46, (byte) 0xFF,
                (byte) 0xFE, (byte) 0x00, (byte) 0x80 };
        var attachment = new AIAttachment("binary.pdf", "application/pdf",
                binaryPdfData);
        var request = new TestLLMRequest("Summarize", null, List.of(attachment),
                new Object[0]);

        mockSimpleChat(request, "Summary");

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());

        var userMessage = (UserMessage) captor.getValue().messages().getFirst();
        var pdfContent = userMessage.contents().stream()
                .filter(PdfFileContent.class::isInstance).findFirst()
                .orElse(null);

        Assertions.assertNotNull(pdfContent, "Should handle binary PDF data");
    }

    @Test
    void stream_withMultipleAttachmentsOfDifferentTypes_processesAll() {
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
        mockSimpleChat(request, "Processed");

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());

        var messages = captor.getValue().messages();
        var userMessage = (UserMessage) messages.getFirst();

        Assertions.assertEquals(4, userMessage.contents().size());
    }

    @Test
    void stream_withNullToolExecutor_addsToolNotFoundMessageToRequest() {
        var request = new TestLLMRequest("Call unknown tool", null,
                Collections.emptyList(), new Object[0]);

        var response1 = mockSimpleResponseWithTool("unknownTool");
        var response2 = mockSimpleResponse("Tool not available");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2);

        provider.stream(request).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(2)).chat(captor.capture());

        var secondRequest = captor.getAllValues().get(1);
        var toolResults = getToolExecutionResults(secondRequest);
        Assertions.assertEquals(1, toolResults.size());
        Assertions.assertTrue(
                toolResults.getFirst().text().contains("Tool not found"));
    }

    @Test
    void stream_withStreamingModelAndTool_executesTool() {
        var toolObject = new SampleToolsClass();
        var request = new TestLLMRequest("Get temperature", null,
                Collections.emptyList(), new Object[] { toolObject });

        Mockito.doAnswer(invocation -> {
            StreamingChatResponseHandler handler = invocation.getArgument(1);
            var response = mockSimpleResponseWithTool("getTemperature");
            handler.onCompleteResponse(response);
            return null;
        }).doAnswer(invocation -> {
            StreamingChatResponseHandler handler = invocation.getArgument(1);
            handler.onPartialResponse("Weather");
            handler.onPartialResponse(" ");
            handler.onPartialResponse("updated");
            var aiMessage = Mockito.mock(AiMessage.class);
            Mockito.when(aiMessage.hasToolExecutionRequests())
                    .thenReturn(false);
            var response = Mockito.mock(ChatResponse.class);
            Mockito.when(response.aiMessage()).thenReturn(aiMessage);
            handler.onCompleteResponse(response);
            return null;
        }).when(mockStreamingChatModel).chat(Mockito.any(ChatRequest.class),
                Mockito.any(StreamingChatResponseHandler.class));

        var results = streamingProvider.stream(request).collectList().block();

        Assertions.assertNotNull(results);
        Assertions.assertEquals(3, results.size(),
                "Should have streamed tokens");

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockStreamingChatModel, Mockito.times(2)).chat(
                captor.capture(),
                Mockito.any(StreamingChatResponseHandler.class));

        var secondRequest = captor.getAllValues().get(1);
        var toolResults = getToolExecutionResults(secondRequest);
        Assertions.assertEquals(1, toolResults.size());
        Assertions.assertEquals(toolResults.getFirst().text(),
                toolObject.getTemperature());
    }

    @Test
    void stream_withMultipleToolCalls_executesTools() {
        var toolObject = new SampleToolsClass();
        var request = new TestLLMRequest("Get temperature and humidity", null,
                Collections.emptyList(), new Object[] { toolObject });

        var aiMessage1 = Mockito.mock(AiMessage.class);
        Mockito.when(aiMessage1.text()).thenReturn("");
        Mockito.when(aiMessage1.hasToolExecutionRequests()).thenReturn(true);
        var toolRequest1 = Mockito.mock(ToolExecutionRequest.class);
        Mockito.when(toolRequest1.name()).thenReturn("getTemperature");
        Mockito.when(toolRequest1.arguments()).thenReturn("{}");

        var toolRequest2 = Mockito.mock(ToolExecutionRequest.class);
        Mockito.when(toolRequest2.name()).thenReturn("getHumidity");
        Mockito.when(toolRequest2.arguments()).thenReturn("{}");
        Mockito.when(aiMessage1.toolExecutionRequests())
                .thenReturn(List.of(toolRequest1, toolRequest2));

        var response1 = Mockito.mock(ChatResponse.class);
        Mockito.when(response1.aiMessage()).thenReturn(aiMessage1);
        var response2 = mockSimpleResponse("Final response");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2);

        provider.stream(request).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(2)).chat(captor.capture());

        var secondRequest = captor.getAllValues().get(1);
        var toolResults = getToolExecutionResults(secondRequest);

        Assertions.assertEquals(2, toolResults.size());
        var resultTexts = toolResults.stream()
                .map(ToolExecutionResultMessage::text).toList();
        Assertions
                .assertTrue(resultTexts.contains(toolObject.getTemperature()));
        Assertions.assertTrue(resultTexts.contains(toolObject.getHumidity()));
    }

    @Test
    void stream_withToolError_addsErrorMessageToRequest() {
        var toolObject = new ErrorThrowingToolClass();
        var request = new TestLLMRequest("Call error tool", null,
                Collections.emptyList(), new Object[] { toolObject });

        var response1 = mockSimpleResponseWithTool("throwError");
        var response2 = mockSimpleResponse("Handled error");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2);

        provider.stream(request).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(2)).chat(captor.capture());

        var secondRequest = captor.getAllValues().get(1);
        var toolResultMessages = getToolExecutionResults(secondRequest);

        Assertions.assertEquals(1, toolResultMessages.size());
        Assertions.assertEquals(toolObject.getErrorMessage(),
                toolResultMessages.getFirst().text());
    }

    @Test
    void stream_withStreamingModelAndPushDisabled_logsWarning() {
        ui.getUI().getPushConfiguration().setPushMode(PushMode.DISABLED);

        var request = createSimpleRequest("Hello");
        Mockito.doAnswer(invocation -> {
            StreamingChatResponseHandler handler = invocation.getArgument(1);
            handler.onPartialResponse("Hi");
            var aiMessage = Mockito.mock(AiMessage.class);
            Mockito.when(aiMessage.hasToolExecutionRequests())
                    .thenReturn(false);
            var response = Mockito.mock(ChatResponse.class);
            Mockito.when(response.aiMessage()).thenReturn(aiMessage);
            handler.onCompleteResponse(response);
            return null;
        }).when(mockStreamingChatModel).chat(Mockito.any(ChatRequest.class),
                Mockito.any(StreamingChatResponseHandler.class));

        streamingProvider.stream(request).collectList().block();

        Assertions.assertTrue(hasDeliveryWarning(), "Expected push warning");
    }

    @Test
    void stream_withNonStreamingModelAndPushDisabled_doesNotLogWarning() {
        ui.getUI().getPushConfiguration().setPushMode(PushMode.DISABLED);

        var request = createSimpleRequest("Hello");
        var response = mockSimpleResponse("Hi there");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

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
        var callThread = captureChatModelCallThread();

        provider.stream(createSimpleRequest("Hello")).collectList().block();

        Assertions.assertSame(Thread.currentThread(), callThread.get(),
                "Without background execution the blocking call must stay on "
                        + "the subscribing thread");
    }

    @Test
    void stream_nonStreamingWithBackgroundExecution_callsModelOffSubscribingThread() {
        provider.setBackgroundExecution(true);
        var callThread = captureChatModelCallThread();

        provider.stream(createSimpleRequest("Hello")).collectList().block();

        Assertions.assertNotSame(Thread.currentThread(), callThread.get(),
                "With background execution the blocking call must move off the "
                        + "subscribing thread");
    }

    @Test
    void stream_nonStreamingWithBackgroundExecution_returnsResponse() {
        provider.setBackgroundExecution(true);
        var response = mockSimpleResponse("Hi there");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        var results = provider.stream(createSimpleRequest("Hello"))
                .collectList().block();

        Assertions.assertEquals(List.of("Hi there"), results);
    }

    @Test
    void stream_nonStreamingWithBackgroundExecution_subscribeReturnsWhileCallStillRunning()
            throws Exception {
        provider.setBackgroundExecution(true);
        var callStarted = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var completed = new CountDownLatch(1);
        var response = mockSimpleResponse("Response");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenAnswer(invocation -> {
                    callStarted.countDown();
                    // Timed so an implementation that blocks the subscriber
                    // fails the count assertion below instead of deadlocking:
                    // the release latch only opens after subscribe() has
                    // returned.
                    release.await(5, TimeUnit.SECONDS);
                    return response;
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
        provider.setBackgroundExecution(true);
        ui.getUI().getPushConfiguration().setPushMode(PushMode.DISABLED);
        var response = mockSimpleResponse("Hi there");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        provider.stream(createSimpleRequest("Hello")).collectList().block();

        Assertions.assertTrue(hasDeliveryWarning(),
                "A background turn needs push or polling to reach the browser");
    }

    @Test
    void stream_nonStreamingWithBackgroundExecution_manualPush_logsWarning() {
        Mockito.when(ui.getService().ensurePushAvailable()).thenReturn(true);
        ui.getUI().getPushConfiguration().setPushMode(PushMode.MANUAL);
        provider.setBackgroundExecution(true);
        var response = mockSimpleResponse("Hi there");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        provider.stream(createSimpleRequest("Hello")).collectList().block();

        Assertions.assertTrue(hasDeliveryWarning(),
                "Manual push does not deliver the response on its own, so the "
                        + "warning is expected");
    }

    @Test
    void stream_nonStreamingWithBackgroundExecution_pollingEnabled_doesNotLogWarning() {
        ui.getUI().getPushConfiguration().setPushMode(PushMode.DISABLED);
        ui.getUI().setPollInterval(500);
        provider.setBackgroundExecution(true);
        var response = mockSimpleResponse("Hi there");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        provider.stream(createSimpleRequest("Hello")).collectList().block();

        Assertions.assertFalse(hasDeliveryWarning(),
                "Polling delivers the response, so no warning is expected");
    }

    @Test
    void stream_nonStreamingWithBackgroundExecution_automaticPush_doesNotLogWarning() {
        Mockito.when(ui.getService().ensurePushAvailable()).thenReturn(true);
        ui.getUI().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);
        provider.setBackgroundExecution(true);
        var response = mockSimpleResponse("Hi there");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        provider.stream(createSimpleRequest("Hello")).collectList().block();

        Assertions.assertFalse(hasDeliveryWarning(),
                "Automatic push delivers the response, so no warning is "
                        + "expected");
    }

    @Test
    void stream_nonStreamingWithBackgroundExecution_noCurrentUi_completesWithoutWarning() {
        provider.setBackgroundExecution(true);
        var response = mockSimpleResponse("Hi there");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        // A programmatic call outside a request has no current UI; the turn
        // must run normally and the delivery check must skip quietly.
        ui.clearUI();
        var results = provider.stream(createSimpleRequest("Hello"))
                .collectList().block();
        Assertions.assertEquals(List.of("Hi there"), results);

        Assertions.assertFalse(hasDeliveryWarning(),
                "There is no UI whose delivery could be blocked, so no "
                        + "warning is expected");
    }

    @Test
    void stream_withBackgroundExecution_repeatedTurns_logsWarningOnce() {
        provider.setBackgroundExecution(true);
        ui.getUI().getPushConfiguration().setPushMode(PushMode.DISABLED);
        var response = mockSimpleResponse("Hi there");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        provider.stream(createSimpleRequest("Hello")).collectList().block();
        provider.stream(createSimpleRequest("Hello again")).collectList()
                .block();

        Assertions.assertEquals(1, deliveryWarningCount(),
                "Expected exactly one delivery warning across two turns");
    }

    @Test
    void stream_streamingWithBackgroundExecution_callsModelOnSubscribingThread() {
        streamingProvider.setBackgroundExecution(true);
        var callThread = new AtomicReference<Thread>();
        Mockito.doAnswer(invocation -> {
            callThread.set(Thread.currentThread());
            StreamingChatResponseHandler handler = invocation.getArgument(1);
            handler.onPartialResponse("Hi");
            var aiMessage = Mockito.mock(AiMessage.class);
            Mockito.when(aiMessage.hasToolExecutionRequests())
                    .thenReturn(false);
            var response = Mockito.mock(ChatResponse.class);
            Mockito.when(response.aiMessage()).thenReturn(aiMessage);
            handler.onCompleteResponse(response);
            return null;
        }).when(mockStreamingChatModel).chat(Mockito.any(ChatRequest.class),
                Mockito.any(StreamingChatResponseHandler.class));

        streamingProvider.stream(createSimpleRequest("Hello")).collectList()
                .block();

        Assertions.assertSame(Thread.currentThread(), callThread.get(),
                "A streaming response already arrives asynchronously and must "
                        + "not be rescheduled");
    }

    /**
     * Records the thread the blocking chat model call runs on and answers with
     * a simple response.
     */
    private AtomicReference<Thread> captureChatModelCallThread() {
        var callThread = new AtomicReference<Thread>();
        var response = mockSimpleResponse("Response");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenAnswer(invocation -> {
                    callThread.set(Thread.currentThread());
                    return response;
                });
        return callThread;
    }

    private boolean hasDeliveryWarning() {
        return logger.getLoggingEvents().stream().anyMatch(event -> event
                .getMessage().contains("neither automatic push nor polling"));
    }

    private long deliveryWarningCount() {
        return logger.getLoggingEvents().stream()
                .filter(event -> event.getMessage()
                        .contains("neither automatic push nor polling"))
                .count();
    }

    @Test
    void setHistory_restoresConversation() {
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Previous question",
                        null, null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Previous answer",
                        null, null));

        provider.setHistory(history, Collections.emptyMap());

        // Verify the restored history is used in the next request by checking
        // that the chat memory contains the restored messages
        var response = mockSimpleResponse("Follow-up answer");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);
        provider.stream(createSimpleRequest("Follow-up")).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());
        var messages = captor.getValue().messages();
        // Should contain: Previous question, Previous answer, Follow-up
        Assertions.assertTrue(messages.stream()
                .anyMatch(msg -> msg instanceof UserMessage userMsg
                        && userMsg.singleText().equals("Previous question")));
        Assertions.assertTrue(
                messages.stream().anyMatch(msg -> msg instanceof AiMessage ai
                        && ai.text().equals("Previous answer")));
    }

    @Test
    void setHistory_clearsExistingHistory() {
        var response = mockSimpleResponse("Old response");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);
        provider.stream(createSimpleRequest("Old message")).blockFirst();

        var newHistory = List.of(
                new ChatMessage(ChatMessage.Role.USER, "New question", null,
                        null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "New answer", null,
                        null));

        provider.setHistory(newHistory, Collections.emptyMap());

        // Verify the old history is cleared by checking the next request
        var response2 = mockSimpleResponse("Response");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response2);
        provider.stream(createSimpleRequest("Check")).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.atLeast(2))
                .chat(captor.capture());
        var lastMessages = captor.getAllValues().getLast().messages();
        Assertions.assertFalse(lastMessages.stream()
                .anyMatch(msg -> msg instanceof UserMessage userMsg
                        && userMsg.singleText().equals("Old message")));
        Assertions.assertTrue(lastMessages.stream()
                .anyMatch(msg -> msg instanceof UserMessage userMsg
                        && userMsg.singleText().equals("New question")));
    }

    @Test
    void setHistory_withNullHistory_throwsNullPointerException() {
        Assertions.assertThrows(NullPointerException.class,
                () -> provider.setHistory(null, Collections.emptyMap()));
    }

    @Test
    void setHistory_withNullHistory_keepsExistingHistory() {
        provider.setHistory(
                List.of(new ChatMessage(ChatMessage.Role.USER,
                        "Previous question", null, null)),
                Collections.emptyMap());

        Assertions.assertThrows(NullPointerException.class,
                () -> provider.setHistory(null, Collections.emptyMap()));

        mockSimpleChat(createSimpleRequest("Follow-up"), "Response");

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());
        var texts = getUserMessageContents(captor.getValue(), TextContent.class)
                .stream().map(TextContent::text).toList();
        Assertions.assertTrue(texts.contains("Previous question"),
                "A rejected history must not clear the existing one, but got: "
                        + texts);
    }

    @Test
    void setHistory_exceedingMaxMessages_evictsOldest() {
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
        var response = mockSimpleResponse("Response");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);
        provider.stream(createSimpleRequest("Check")).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());
        var messages = captor.getValue().messages();
        // Filter to only user/assistant messages (exclude system)
        var chatMessages = messages.stream().filter(
                msg -> msg instanceof UserMessage || msg instanceof AiMessage)
                .toList();
        Assertions.assertTrue(chatMessages.size() <= 30);
        Assertions.assertTrue(chatMessages.stream()
                .anyMatch(msg -> msg instanceof UserMessage userMsg
                        && userMsg.singleText().equals("Question 19")));
        Assertions.assertFalse(chatMessages.stream()
                .anyMatch(msg -> msg instanceof UserMessage userMsg
                        && userMsg.singleText().equals("Question 0")));
    }

    @Test
    void setHistory_withAttachments_restoresUserMessageWithImageContent() {
        var imageData = "fake-image-data".getBytes();
        var attachment = new AIAttachment("photo.png", "image/png", imageData);
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Look at this", "msg-1",
                        null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Nice photo!", null,
                        null));
        var attachments = Map.of("msg-1", List.of(attachment));

        provider.setHistory(history, attachments);

        var response = mockSimpleResponse("Follow-up answer");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);
        provider.stream(createSimpleRequest("Follow-up")).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());
        var messages = captor.getValue().messages();

        // Find the restored user message with image content
        var userMessages = messages.stream()
                .filter(UserMessage.class::isInstance)
                .map(UserMessage.class::cast).toList();
        var restoredUserMsg = userMessages.stream()
                .filter(msg -> msg.contents().stream()
                        .anyMatch(c -> c instanceof TextContent tc
                                && tc.text().equals("Look at this")))
                .findFirst().orElseThrow();

        // Should have TextContent + ImageContent
        Assertions.assertTrue(restoredUserMsg.contents().stream()
                .anyMatch(ImageContent.class::isInstance));
    }

    @Test
    void setHistory_withAttachments_assistantMessageIgnoresAttachments() {
        var attachment = new AIAttachment("file.txt", "text/plain",
                "content".getBytes());
        var history = List.of(new ChatMessage(ChatMessage.Role.ASSISTANT,
                "Hello", "msg-1", null));
        var attachments = Map.of("msg-1", List.of(attachment));

        provider.setHistory(history, attachments);

        var response = mockSimpleResponse("Response");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);
        provider.stream(createSimpleRequest("Check")).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());
        var messages = captor.getValue().messages();

        // Assistant message should be AiMessage (text-only), not have
        // attachments
        Assertions.assertTrue(
                messages.stream().anyMatch(msg -> msg instanceof AiMessage ai
                        && ai.text().equals("Hello")));
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
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1", null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Hi", null, null));

        provider.setHistory(history, Collections.emptyMap());

        var response = mockSimpleResponse("Response");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);
        provider.stream(createSimpleRequest("Check")).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());
        var messages = captor.getValue().messages();

        // User message should be text-only (no ImageContent etc.)
        var userMsg = messages.stream().filter(UserMessage.class::isInstance)
                .map(UserMessage.class::cast)
                .filter(msg -> msg.contents().stream()
                        .anyMatch(c -> c instanceof TextContent tc
                                && tc.text().equals("Hello")))
                .findFirst().orElseThrow();
        Assertions.assertEquals(1, userMsg.contents().size());
        Assertions.assertTrue(
                userMsg.contents().getFirst() instanceof TextContent);
    }

    private void mockSimpleChat(LLMRequest request, String responseText) {
        var response = mockSimpleResponse(responseText);
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);
        provider.stream(request).blockFirst();
    }

    // --- Explicit tools tests ---

    @Test
    void stream_withExplicitTool_executesTool() {
        var toolResult = "tool executed";
        var explicitTool = createExplicitTool("myTool", "A test tool",
                "{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\"}},\"required\":[\"query\"]}",
                args -> toolResult);

        var request = new TestLLMRequestWithExplicitTools("Call my tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));

        var response1 = mockSimpleResponseWithTool("myTool");
        var response2 = mockSimpleResponse("Done");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2);

        provider.stream(request).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(2)).chat(captor.capture());

        // Verify tool spec was included in the first request
        var firstRequest = captor.getAllValues().get(0);
        Assertions.assertFalse(firstRequest.toolSpecifications().isEmpty());
        Assertions.assertEquals("myTool",
                firstRequest.toolSpecifications().getFirst().name());

        // Verify tool was executed and result was in second request
        var toolResults = getToolExecutionResults(captor.getAllValues().get(1));
        Assertions.assertEquals(1, toolResults.size());
        Assertions.assertEquals(toolResult, toolResults.getFirst().text());
    }

    @Test
    void stream_withExplicitTool_passesArgumentsToExecutor() {
        var receivedArgs = new ArrayList<JsonNode>();
        var explicitTool = createExplicitTool("myTool", "A test tool",
                "{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"}}}",
                args -> {
                    receivedArgs.add(args);
                    return "result for " + args.get("city").asString();
                });

        var request = new TestLLMRequestWithExplicitTools("Call my tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));

        var response1 = mockSimpleResponseWithTool("myTool",
                "{\"city\":\"Helsinki\"}");
        var response2 = mockSimpleResponse("Done");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2);

        provider.stream(request).blockFirst();

        Assertions.assertEquals(1, receivedArgs.size(),
                "Tool executor should have been called once");
        Assertions.assertEquals("Helsinki",
                receivedArgs.getFirst().get("city").asString(),
                "Tool executor should receive arguments as a JsonNode parsed from the LLM response");

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(2)).chat(captor.capture());
        var toolResults = getToolExecutionResults(captor.getAllValues().get(1));
        Assertions.assertEquals("result for Helsinki",
                toolResults.getFirst().text());
    }

    @Test
    void stream_withExplicitTool_malformedJsonArguments_returnsError() {
        var receivedArgs = new ArrayList<JsonNode>();
        var explicitTool = createExplicitTool("myTool", "A test tool",
                "{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"}}}",
                args -> {
                    receivedArgs.add(args);
                    return "ok";
                });

        var request = new TestLLMRequestWithExplicitTools("Call my tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));

        var response1 = mockSimpleResponseWithTool("myTool", "not json");
        var response2 = mockSimpleResponse("Done");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2);

        provider.stream(request).blockFirst();

        Assertions.assertEquals(0, receivedArgs.size());

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(2)).chat(captor.capture());
        var toolResults = getToolExecutionResults(captor.getAllValues().get(1));
        Assertions.assertTrue(toolResults.getFirst().text()
                .startsWith("Error executing tool:"));
        assertNoJavaInternals(toolResults.getFirst().text());
    }

    @Test
    void stream_withExplicitToolSchema_createsToolWithParameters() {
        var explicitTool = createExplicitTool("myTool", "A test tool",
                "{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"}}}",
                args -> "done");

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));

        var response = mockSimpleResponse("OK");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        provider.stream(request).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());
        var parameters = captor.getValue().toolSpecifications().getFirst()
                .parameters();
        Assertions.assertNotNull(parameters,
                "The declared schema should be passed to the model");
        Assertions.assertEquals(List.of("city"),
                List.copyOf(parameters.properties().keySet()));
    }

    @Test
    void stream_withExplicitToolSchemaWithRequired_createsToolWithRequiredParameters() {
        var explicitTool = createExplicitTool("myTool", "A test tool",
                "{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"},"
                        + "\"unit\":{\"type\":\"string\"}},\"required\":[\"city\"]}",
                args -> "done");

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));

        var response = mockSimpleResponse("OK");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        provider.stream(request).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());
        var parameters = captor.getValue().toolSpecifications().getFirst()
                .parameters();
        Assertions.assertEquals(List.of("city"), parameters.required());
    }

    @Test
    void stream_withExplicitTool_nullOrBlankArguments_passesEmptyObject() {
        var receivedArgs = new ArrayList<JsonNode>();
        var explicitTool = createExplicitTool("myTool", "A test tool", null,
                args -> {
                    receivedArgs.add(args);
                    return "ok";
                });

        var request = new TestLLMRequestWithExplicitTools("Call my tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));

        var response1 = mockSimpleResponseWithTool("myTool", null);
        var response2 = mockSimpleResponseWithTool("myTool", "  ");
        var response3 = mockSimpleResponse("Done");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2, response3);

        provider.stream(request).blockFirst();

        Assertions.assertEquals(2, receivedArgs.size(),
                "Both missing and blank arguments should reach the executor");
        var allEmptyObjects = receivedArgs.stream().allMatch(
                args -> args.isObject() && args.propertyNames().isEmpty());
        Assertions.assertTrue(allEmptyObjects,
                "Missing arguments should be parsed as an empty object");

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(3)).chat(captor.capture());
        var toolResults = getToolExecutionResults(captor.getAllValues().get(2));
        Assertions.assertEquals(2, toolResults.size(),
                "Both tool calls should have produced a result, but got: "
                        + toolResults);
        var allSucceeded = toolResults.stream()
                .allMatch(result -> "ok".equals(result.text()));
        Assertions.assertTrue(allSucceeded,
                "Both tool calls should have succeeded, but got: "
                        + toolResults);
    }

    @Test
    void stream_withExplicitTool_malformedJsonArguments_relaysParserMessage() {
        var explicitTool = createExplicitTool("myTool", "A test tool",
                "{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"}}}",
                args -> "ok");

        var request = new TestLLMRequestWithExplicitTools("Call my tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));

        var response1 = mockSimpleResponseWithTool("myTool", "not json");
        var response2 = mockSimpleResponse("Done");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2);

        provider.stream(request).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(2)).chat(captor.capture());
        var toolResults = getToolExecutionResults(captor.getAllValues().get(1));
        var result = toolResults.getFirst().text();
        Assertions.assertTrue(result
                .startsWith("Error executing tool: invalid JSON arguments: "));
        Assertions.assertTrue(result.contains("Unrecognized token"),
                "The parser diagnostic should be relayed so the model can "
                        + "repair its next attempt, but got: " + result);
        assertNoJavaInternals(result);
    }

    @Test
    void stream_withExplicitTool_nonObjectJsonArguments_reportsExpectedShape() {
        var receivedArgs = new ArrayList<JsonNode>();
        var explicitTool = createExplicitTool("myTool", "A test tool",
                "{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"}}}",
                args -> {
                    receivedArgs.add(args);
                    return "ok";
                });

        var request = new TestLLMRequestWithExplicitTools("Call my tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));

        // An empty string is valid JSON, so it parses, but it is not the
        // object a tool with declared parameters expects.
        var response1 = mockSimpleResponseWithTool("myTool", "\"\"");
        var response2 = mockSimpleResponse("Done");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2);

        provider.stream(request).blockFirst();

        Assertions.assertEquals(0, receivedArgs.size());

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(2)).chat(captor.capture());
        var result = getToolExecutionResults(captor.getAllValues().get(1))
                .getFirst().text();

        Assertions.assertTrue(result.startsWith("Error executing tool:"));
        Assertions.assertTrue(result.contains("JSON object"),
                "The model should be told what shape to send, but got: "
                        + result);
        assertNoJavaInternals(result);
    }

    @Test
    void stream_withExplicitTool_jsonArrayArguments_reportsExpectedShape() {
        var explicitTool = createExplicitTool("myTool", "A test tool",
                "{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"}}}",
                args -> "ok");

        var request = new TestLLMRequestWithExplicitTools("Call my tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));

        var response1 = mockSimpleResponseWithTool("myTool", "[1, 2]");
        var response2 = mockSimpleResponse("Done");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2);

        provider.stream(request).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(2)).chat(captor.capture());
        var result = getToolExecutionResults(captor.getAllValues().get(1))
                .getFirst().text();

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

    @Test
    void stream_withExplicitToolThrowingToolException_relaysMessageToModel() {
        var explicitTool = createExplicitTool("myTool", "A test tool", null,
                args -> {
                    throw new ToolException("Unknown column 'foo'");
                });

        var request = new TestLLMRequestWithExplicitTools("Call my tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));

        var response1 = mockSimpleResponseWithTool("myTool");
        var response2 = mockSimpleResponse("Done");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2);

        provider.stream(request).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(2)).chat(captor.capture());
        var toolResults = getToolExecutionResults(captor.getAllValues().get(1));
        Assertions.assertEquals("Error executing tool: Unknown column 'foo'",
                toolResults.getFirst().text());
    }

    @Test
    void stream_withExplicitToolThrowingUnexpectedException_returnsGenericError() {
        var explicitTool = createExplicitTool("myTool", "A test tool", null,
                args -> {
                    throw new RuntimeException("internal detail");
                });

        var request = new TestLLMRequestWithExplicitTools("Call my tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));

        var response1 = mockSimpleResponseWithTool("myTool");
        var response2 = mockSimpleResponse("Done");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2);

        provider.stream(request).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(2)).chat(captor.capture());
        var toolResults = getToolExecutionResults(captor.getAllValues().get(1));
        Assertions.assertEquals("Error executing tool.",
                toolResults.getFirst().text());
    }

    @Test
    void stream_withExplicitToolThrowingUnexpectedException_logsError() {
        var toolFailure = new RuntimeException("internal detail");
        var explicitTool = createExplicitTool("myTool", "A test tool", null,
                args -> {
                    throw toolFailure;
                });

        var request = new TestLLMRequestWithExplicitTools("Call my tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));

        var response1 = mockSimpleResponseWithTool("myTool");
        var response2 = mockSimpleResponse("Done");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2);

        provider.stream(request).blockFirst();

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
        var explicitTool = createExplicitTool("simpleTool", "A simple tool",
                null, args -> "done");

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));

        var response = mockSimpleResponse("OK");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        provider.stream(request).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());
        var spec = captor.getValue().toolSpecifications().getFirst();
        Assertions.assertEquals("simpleTool", spec.name());
        Assertions.assertEquals("A simple tool", spec.description());
        assertNoParametersSchema(spec.parameters());
    }

    @Test
    void stream_withExplicitToolBlankSchema_substitutesNoParametersSchema() {
        var explicitTool = createExplicitTool("simpleTool", "A simple tool",
                "   ", args -> "done");

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));

        var response = mockSimpleResponse("OK");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        provider.stream(request).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());
        assertNoParametersSchema(
                captor.getValue().toolSpecifications().getFirst().parameters());
    }

    @Test
    void stream_withExplicitToolMalformedSchema_substitutesNoParametersSchema() {
        var explicitTool = createExplicitTool("myTool", "A test tool",
                "not json", args -> "done");

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));

        var response = mockSimpleResponse("OK");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        provider.stream(request).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());
        assertNoParametersSchema(
                captor.getValue().toolSpecifications().getFirst().parameters());
    }

    @Test
    void stream_withExplicitToolNullSchema_executeReceivesEmptyArguments() {
        var receivedArgs = new ArrayList<JsonNode>();
        var explicitTool = createExplicitTool("simpleTool", "A simple tool",
                null, args -> {
                    receivedArgs.add(args);
                    return "done";
                });

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));

        // The model may fill the placeholder schema that was substituted for
        // the missing one; a tool that declared no parameters must not see
        // that.
        var response1 = mockSimpleResponseWithTool("simpleTool",
                "{\"reason\":\"checking the form\"}");
        var response2 = mockSimpleResponse("Done");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2);

        provider.stream(request).blockFirst();

        Assertions.assertEquals(1, receivedArgs.size());
        Assertions.assertTrue(receivedArgs.getFirst().isEmpty(),
                "A tool that declared no parameters must receive an empty "
                        + "arguments object, got: " + receivedArgs.getFirst());
    }

    @Test
    void stream_withExplicitToolMalformedSchema_executeReceivesEmptyArguments() {
        // A declared schema that fails to parse is replaced with the
        // placeholder schema, so the model never saw the tool's real
        // parameters — whatever it sent under the placeholder is not what the
        // tool declared, and the tool receives no arguments, keeping the
        // placeholder's property name a provider-internal detail.
        var receivedArgs = new ArrayList<JsonNode>();
        var explicitTool = createExplicitTool("myTool", "A test tool",
                "not json", args -> {
                    receivedArgs.add(args);
                    return "done";
                });

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));

        var response1 = mockSimpleResponseWithTool("myTool",
                "{\"reason\":\"exploring the data\"}");
        var response2 = mockSimpleResponse("Done");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2);

        provider.stream(request).blockFirst();

        Assertions.assertEquals(1, receivedArgs.size());
        Assertions.assertTrue(receivedArgs.getFirst().isEmpty(),
                "A tool whose schema was replaced with the placeholder must "
                        + "receive an empty arguments object, got: "
                        + receivedArgs.getFirst());
    }

    @Test
    void stream_withExplicitToolNullSchema_malformedArgumentsStillExecute() {
        // A model that has nothing to fill sometimes sends an empty string —
        // the very case the placeholder schema works around. A tool that
        // declared no parameters ignores its arguments by contract, so it
        // must run rather than bounce an error back to the model.
        var receivedArgs = new ArrayList<JsonNode>();
        var explicitTool = createExplicitTool("simpleTool", "A simple tool",
                null, args -> {
                    receivedArgs.add(args);
                    return "done";
                });

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));

        var response1 = mockSimpleResponseWithTool("simpleTool", "\"\"");
        var response2 = mockSimpleResponse("Done");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2);

        provider.stream(request).blockFirst();

        Assertions.assertEquals(1, receivedArgs.size());
        Assertions.assertTrue(receivedArgs.getFirst().isEmpty(),
                "A tool that declared no parameters must receive an empty "
                        + "arguments object, got: " + receivedArgs.getFirst());

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(2)).chat(captor.capture());
        Assertions.assertEquals("done",
                getToolExecutionResults(captor.getAllValues().get(1)).getFirst()
                        .text(),
                "The tool's real result must go back to the model, not an "
                        + "argument-parsing error");
    }

    @Test
    void stream_withExplicitToolSchema_preservesDeclaredProperties() {
        var explicitTool = createExplicitTool("myTool", "A test tool",
                "{\"type\":\"object\",\"properties\":{\"city\":"
                        + "{\"type\":\"string\"}},\"required\":[\"city\"]}",
                args -> "done");

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));

        var response = mockSimpleResponse("OK");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        provider.stream(request).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());
        var parameters = captor.getValue().toolSpecifications().getFirst()
                .parameters();
        Assertions.assertEquals(Set.of("city"),
                parameters.properties().keySet(),
                "A declared schema's properties must reach the tool "
                        + "specification unmodified");
        Assertions.assertEquals(List.of("city"), parameters.required());
    }

    @Test
    void stream_withExplicitToolEmptyPropertiesSchema_passesSchemaThrough() {
        // Substitution is limited to null/blank schemas: a syntactically
        // valid schema authored by the user is passed through as-is, even
        // when its properties object is empty.
        var explicitTool = createExplicitTool("myTool", "A test tool",
                "{\"type\":\"object\",\"properties\":{}}", args -> "done");

        var request = new TestLLMRequestWithExplicitTools("Call tool", null,
                Collections.emptyList(), new Object[0], List.of(explicitTool));

        var response = mockSimpleResponse("OK");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        provider.stream(request).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());
        var parameters = captor.getValue().toolSpecifications().getFirst()
                .parameters();
        Assertions.assertNotNull(parameters);
        Assertions.assertTrue(parameters.properties().isEmpty(),
                "An explicit empty-properties schema must not be rewritten, "
                        + "got: " + parameters);
    }

    /**
     * Asserts the parameters are the non-empty no-parameters shape: at least
     * one property, all of them optional. A tool without a declared property
     * makes models disagree on what to send as arguments, and some LLM APIs
     * reject the request that replays such a tool call.
     */
    private static void assertNoParametersSchema(JsonObjectSchema parameters) {
        Assertions.assertNotNull(parameters);
        Assertions.assertFalse(parameters.properties().isEmpty(),
                "Schema must declare at least one property, got: "
                        + parameters);
        Assertions.assertTrue(
                parameters.required() == null
                        || parameters.required().isEmpty(),
                "All declared properties must be optional, got: " + parameters);
    }

    @Test
    void stream_withBothVendorAndExplicitTools_allConfigured() {
        var vendorTool = new SampleToolsClass();
        var explicitTool = createExplicitTool("explicitTool", "Explicit", null,
                args -> "result");

        var request = new TestLLMRequestWithExplicitTools("Call tools", null,
                Collections.emptyList(), new Object[] { vendorTool },
                List.of(explicitTool));

        var response = mockSimpleResponse("Done");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        provider.stream(request).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());
        var specs = captor.getValue().toolSpecifications();
        // 2 from SampleToolsClass + 1 explicit
        Assertions.assertEquals(3, specs.size());
        var names = specs.stream()
                .map(dev.langchain4j.agent.tool.ToolSpecification::name)
                .toList();
        Assertions.assertTrue(names.contains("explicitTool"));
        Assertions.assertTrue(names.contains("getTemperature"));
        Assertions.assertTrue(names.contains("getHumidity"));
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

    // --- Response metadata tests ---

    @Test
    void stream_nonStreamingWithFinishReasonAndUsage_publishesMetadata() {
        var collected = new ArrayList<ResponseMetadata>();
        var request = requestWithMetadataSink("Hello", List.of(), collected);
        var response = mockSimpleResponse("Truncated");
        Mockito.when(response.finishReason()).thenReturn(FinishReason.LENGTH);
        Mockito.when(response.tokenUsage())
                .thenReturn(new TokenUsage(1200, 8, 1208));
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        var results = provider.stream(request).collectList().block();

        Assertions.assertEquals(List.of("Truncated"), results);
        Assertions.assertEquals(1, collected.size(),
                "Provider should publish the response metadata once");
        var metadata = collected.getFirst();
        Assertions.assertEquals("LENGTH", metadata.finishReason());
        Assertions.assertEquals(1200, metadata.tokenUsage().inputTokens());
        Assertions.assertEquals(8, metadata.tokenUsage().outputTokens());
        Assertions.assertEquals(1208, metadata.tokenUsage().totalTokens());
    }

    @Test
    void stream_nonStreamingToolRoundTrips_accumulatesTokenUsage() {
        var collected = new ArrayList<ResponseMetadata>();
        var explicitTool = createExplicitTool("myTool", "A test tool", null,
                args -> "tool result");
        var request = requestWithMetadataSink("Call tool",
                List.of(explicitTool), collected);
        var toolResponse = mockSimpleResponseWithTool("myTool");
        Mockito.when(toolResponse.finishReason())
                .thenReturn(FinishReason.TOOL_EXECUTION);
        Mockito.when(toolResponse.tokenUsage())
                .thenReturn(new TokenUsage(100, 10, 110));
        var finalResponse = mockSimpleResponse("done");
        Mockito.when(finalResponse.finishReason())
                .thenReturn(FinishReason.STOP);
        Mockito.when(finalResponse.tokenUsage())
                .thenReturn(new TokenUsage(200, 20, 220));
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(toolResponse).thenReturn(finalResponse);

        var results = provider.stream(request).collectList().block();

        Assertions.assertEquals(List.of("done"), results);
        Assertions.assertEquals(2, collected.size(),
                "Each round trip publishes the state known so far");
        Assertions.assertEquals(110,
                collected.getFirst().tokenUsage().totalTokens(),
                "The first snapshot carries the first round trip alone");
        var metadata = collected.getLast();
        Assertions.assertEquals("STOP", metadata.finishReason(),
                "The reason that ended the turn wins");
        Assertions.assertEquals(300, metadata.tokenUsage().inputTokens());
        Assertions.assertEquals(30, metadata.tokenUsage().outputTokens());
        Assertions.assertEquals(330, metadata.tokenUsage().totalTokens());
    }

    @Test
    void stream_secondToolRoundTripFails_firstRoundMetadataStillPublished() {
        // The failed turn was still billed for the round trips that ran; the
        // sink must have received what was observed before the failure.
        var collected = new ArrayList<ResponseMetadata>();
        var explicitTool = createExplicitTool("myTool", "A test tool", null,
                args -> "tool result");
        var request = requestWithMetadataSink("Call tool",
                List.of(explicitTool), collected);
        var toolResponse = mockSimpleResponseWithTool("myTool");
        Mockito.when(toolResponse.finishReason())
                .thenReturn(FinishReason.TOOL_EXECUTION);
        Mockito.when(toolResponse.tokenUsage())
                .thenReturn(new TokenUsage(100, 10, 110));
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(toolResponse)
                .thenThrow(new RuntimeException("API down"));

        var response = provider.stream(request).collectList();
        Assertions.assertThrows(RuntimeException.class, response::block);

        var metadata = collected.getLast();
        Assertions.assertEquals("TOOL_EXECUTION", metadata.finishReason());
        Assertions.assertEquals(110, metadata.tokenUsage().totalTokens());
    }

    @Test
    void stream_streamingWithMetadata_publishesMetadata() {
        var collected = new ArrayList<ResponseMetadata>();
        var request = requestWithMetadataSink("Hello", List.of(), collected);
        var response = mockSimpleResponse("Hello World");
        Mockito.when(response.finishReason()).thenReturn(FinishReason.STOP);
        Mockito.when(response.tokenUsage())
                .thenReturn(new TokenUsage(50, 5, 55));
        Mockito.doAnswer(invocation -> {
            StreamingChatResponseHandler handler = invocation.getArgument(1);
            handler.onPartialResponse("Hello ");
            handler.onPartialResponse("World");
            handler.onCompleteResponse(response);
            return null;
        }).when(mockStreamingChatModel).chat(Mockito.any(ChatRequest.class),
                Mockito.any(StreamingChatResponseHandler.class));

        var results = streamingProvider.stream(request).collectList().block();

        Assertions.assertEquals(List.of("Hello ", "World"), results);
        Assertions.assertEquals(1, collected.size(),
                "Provider should publish the response metadata once");
        var metadata = collected.getFirst();
        Assertions.assertEquals("STOP", metadata.finishReason());
        Assertions.assertEquals(55, metadata.tokenUsage().totalTokens());
    }

    @Test
    void stream_nonStreamingWithFinishReasonButNoUsage_publishesReasonOnly() {
        var collected = new ArrayList<ResponseMetadata>();
        var request = requestWithMetadataSink("Hello", List.of(), collected);
        var response = mockSimpleResponse("Done");
        Mockito.when(response.finishReason()).thenReturn(FinishReason.STOP);
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        provider.stream(request).collectList().block();

        Assertions.assertEquals(1, collected.size(),
                "A reported finish reason alone is worth publishing");
        Assertions.assertEquals("STOP", collected.getFirst().finishReason());
        Assertions.assertNull(collected.getFirst().tokenUsage());
    }

    @Test
    void stream_nonStreamingWithoutAiMessage_publishesMetadata() {
        var collected = new ArrayList<ResponseMetadata>();
        var request = requestWithMetadataSink("Hello", List.of(), collected);
        var response = Mockito.mock(ChatResponse.class);
        Mockito.when(response.aiMessage()).thenReturn(null);
        Mockito.when(response.finishReason())
                .thenReturn(FinishReason.CONTENT_FILTER);
        Mockito.when(response.tokenUsage())
                .thenReturn(new TokenUsage(30, 0, 30));
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        provider.stream(request).collectList().block();

        Assertions.assertEquals(1, collected.size(),
                "A turn that ends without a message still reports why");
        Assertions.assertEquals("CONTENT_FILTER",
                collected.getFirst().finishReason());
        Assertions.assertEquals(30,
                collected.getFirst().tokenUsage().totalTokens());
    }

    @Test
    void stream_nonStreamingWithoutMetadata_sinkNotCalled() {
        var collected = new ArrayList<ResponseMetadata>();
        var request = requestWithMetadataSink("Hello", List.of(), collected);
        var response = mockSimpleResponse("plain");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        provider.stream(request).collectList().block();

        Assertions.assertTrue(collected.isEmpty(),
                "No finish reason and no usage means nothing to publish");
    }

    @Test
    void stream_nonStreamingWithUsageButNoFinishReason_publishesUsage() {
        var collected = new ArrayList<ResponseMetadata>();
        var request = requestWithMetadataSink("Hello", List.of(), collected);
        var response = mockSimpleResponse("Done");
        Mockito.when(response.tokenUsage())
                .thenReturn(new TokenUsage(50, 5, 55));
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        provider.stream(request).collectList().block();

        Assertions.assertEquals(1, collected.size(),
                "Reported usage is worth publishing on its own");
        Assertions.assertNull(collected.getFirst().finishReason());
        Assertions.assertEquals(55,
                collected.getFirst().tokenUsage().totalTokens());
    }

    @Test
    void stream_nonStreamingLastRoundTripReportsNothingNew_doesNotRepublish() {
        var collected = new ArrayList<ResponseMetadata>();
        var explicitTool = createExplicitTool("myTool", "A test tool", null,
                args -> "tool result");
        var request = requestWithMetadataSink("Call tool",
                List.of(explicitTool), collected);
        var toolResponse = mockSimpleResponseWithTool("myTool");
        Mockito.when(toolResponse.finishReason())
                .thenReturn(FinishReason.TOOL_EXECUTION);
        Mockito.when(toolResponse.tokenUsage())
                .thenReturn(new TokenUsage(100, 10, 110));
        var finalResponse = mockSimpleResponse("done");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(toolResponse).thenReturn(finalResponse);

        provider.stream(request).collectList().block();

        Assertions.assertEquals(1, collected.size(),
                "A round trip that reports neither a reason nor usage must "
                        + "not re-publish the earlier state");
    }

    @Test
    void stream_turnEndsWithoutAiMessageAndWithoutFinishReason_logsWarning() {
        var response = Mockito.mock(ChatResponse.class);
        Mockito.when(response.aiMessage()).thenReturn(null);
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        provider.stream(createSimpleRequest("Hello")).collectList().block();

        Assertions.assertTrue(hasMissingFinishReasonWarning(),
                "A turn that ends without a message and without a finish "
                        + "reason must warn");
    }

    @Test
    void stream_turnEndsWithoutFinishReason_logsWarning() {
        var response = mockSimpleResponse("Done");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        provider.stream(createSimpleRequest("Hello")).collectList().block();

        Assertions.assertTrue(hasMissingFinishReasonWarning(),
                "Expected a warning about the missing finish reason");
    }

    @Test
    void stream_turnEndsWithFinishReason_noMissingFinishReasonWarning() {
        var response = mockSimpleResponse("Done");
        Mockito.when(response.finishReason()).thenReturn(FinishReason.STOP);
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        provider.stream(createSimpleRequest("Hello")).collectList().block();

        Assertions.assertFalse(hasMissingFinishReasonWarning(),
                "A turn with a reported finish reason must not warn");
    }

    @Test
    void stream_finishReasonOnlyOnFinalToolRoundTrip_noMissingFinishReasonWarning() {
        // Some models report the reason only on the round trip that ends the
        // turn; earlier tool round trips without one are not abnormal.
        var explicitTool = createExplicitTool("myTool", "A test tool", null,
                args -> "tool result");
        var request = requestWithMetadataSink("Call tool",
                List.of(explicitTool), new ArrayList<>());
        var toolResponse = mockSimpleResponseWithTool("myTool");
        var finalResponse = mockSimpleResponse("done");
        Mockito.when(finalResponse.finishReason())
                .thenReturn(FinishReason.STOP);
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(toolResponse).thenReturn(finalResponse);

        provider.stream(request).collectList().block();

        Assertions.assertFalse(hasMissingFinishReasonWarning(),
                "The reason on the final round trip covers the turn");
    }

    @Test
    void stream_finalToolRoundTripWithoutFinishReason_logsWarning() {
        // The turn ended on a round trip the model said nothing about. The
        // TOOL_EXECUTION reason from the earlier round trip describes that
        // round trip, not how the turn ended, so it must not silence the
        // warning.
        var explicitTool = createExplicitTool("myTool", "A test tool", null,
                args -> "tool result");
        var request = requestWithMetadataSink("Call tool",
                List.of(explicitTool), new ArrayList<>());
        var toolResponse = mockSimpleResponseWithTool("myTool");
        Mockito.when(toolResponse.finishReason())
                .thenReturn(FinishReason.TOOL_EXECUTION);
        var finalResponse = mockSimpleResponse("done");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(toolResponse).thenReturn(finalResponse);

        provider.stream(request).collectList().block();

        Assertions.assertTrue(hasMissingFinishReasonWarning(),
                "A turn ending on a round trip with no finish reason must "
                        + "warn even when an earlier round trip reported one");
    }

    private boolean hasMissingFinishReasonWarning() {
        return logger.getLoggingEvents().stream().anyMatch(
                event -> event.getMessage().contains("no finish reason"));
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

    private static List<ToolExecutionResultMessage> getToolExecutionResults(
            ChatRequest request) {
        return request.messages().stream()
                .filter(ToolExecutionResultMessage.class::isInstance)
                .map(ToolExecutionResultMessage.class::cast).toList();
    }

    private static <T extends Content> List<T> getUserMessageContents(
            ChatRequest request, Class<T> contentClass) {
        return request.messages().stream().filter(UserMessage.class::isInstance)
                .map(UserMessage.class::cast).map(UserMessage::contents)
                .flatMap(List::stream).filter(contentClass::isInstance)
                .map(contentClass::cast).toList();
    }

    private static LLMRequest createSimpleRequest(String message) {
        return new TestLLMRequest(message, null, Collections.emptyList(),
                new Object[0]);
    }

    private static ChatResponse mockSimpleResponseWithTool(String toolName) {
        return mockSimpleResponseWithTool(toolName, "{}");
    }

    private static ChatResponse mockSimpleResponseWithTool(String toolName,
            String arguments) {
        var aiMessage1 = Mockito.mock(AiMessage.class);
        Mockito.when(aiMessage1.text()).thenReturn("");
        Mockito.when(aiMessage1.hasToolExecutionRequests()).thenReturn(true);
        var toolRequest = Mockito.mock(ToolExecutionRequest.class);
        Mockito.when(toolRequest.name()).thenReturn(toolName);
        Mockito.when(toolRequest.arguments()).thenReturn(arguments);
        Mockito.when(aiMessage1.toolExecutionRequests())
                .thenReturn(List.of(toolRequest));
        var response1 = Mockito.mock(ChatResponse.class);
        Mockito.when(response1.aiMessage()).thenReturn(aiMessage1);
        return response1;
    }

    private static ChatResponse mockSimpleResponse(String text) {
        var aiMessage = Mockito.mock(AiMessage.class);
        Mockito.when(aiMessage.text()).thenReturn(text);
        Mockito.when(aiMessage.hasToolExecutionRequests()).thenReturn(false);
        var response = Mockito.mock(ChatResponse.class);
        Mockito.when(response.aiMessage()).thenReturn(aiMessage);
        return response;
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
        @Tool
        public String getTemperature() {
            return "22°C";
        }

        @Tool
        public String getHumidity() {
            return "65%";
        }
    }

    private static class ErrorThrowingToolClass {
        public String getErrorMessage() {
            return "Tool execution failed";
        }

        @Tool
        public String throwError() {
            throw new RuntimeException(getErrorMessage());
        }
    }
}
