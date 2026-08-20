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
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

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
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
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

        var warning = logger.getLoggingEvents().stream().filter(
                event -> event.getMessage().contains("Push is not enabled"))
                .findFirst();
        Assertions.assertTrue(warning.isPresent(), "Expected push warning");
    }

    @Test
    void stream_withNonStreamingModelAndPushDisabled_doesNotLogWarning() {
        ui.getUI().getPushConfiguration().setPushMode(PushMode.DISABLED);

        var request = createSimpleRequest("Hello");
        var response = mockSimpleResponse("Hi there");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        provider.stream(request).collectList().block();

        var warning = logger.getLoggingEvents().stream().filter(
                event -> event.getMessage().contains("Push is not enabled"))
                .findFirst();
        Assertions.assertFalse(warning.isPresent(), "Expected no push warning");
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
    }

    @Test
    void stream_withExplicitToolNullSchema_createsToolWithoutParameters() {
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
        Assertions.assertNull(spec.parameters());
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
