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
import java.util.concurrent.FutureTask;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.provider.LLMProvider.Attachment;
import com.vaadin.flow.component.ai.provider.LLMProvider.LLMRequest;
import com.vaadin.flow.server.Command;

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

public class LangChain4JLLMProviderTest {

    private ChatModel mockChatModel;
    private StreamingChatModel mockStreamingChatModel;

    private LangChain4JLLMProvider provider;
    private LangChain4JLLMProvider streamingProvider;

    @Before
    public void setup() {
        mockChatModel = Mockito.mock(ChatModel.class);
        mockStreamingChatModel = Mockito.mock(StreamingChatModel.class);
        provider = new LangChain4JLLMProvider(mockChatModel);
        streamingProvider = new LangChain4JLLMProvider(mockStreamingChatModel);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
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
                () -> new LangChain4JLLMProvider((ChatModel) null));
    }

    @Test
    public void constructor_withNullStreamingChatModel_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class,
                () -> new LangChain4JLLMProvider((StreamingChatModel) null));
    }

    @Test
    public void chatMemory_retainsHistory() {
        var response1 = mockSimpleResponse("Response 1");
        var response2 = mockSimpleResponse("Response 2");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2);

        provider.stream(createSimpleRequest("First message")).blockFirst();
        provider.stream(createSimpleRequest("Second message")).blockFirst();

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(2)).chat(captor.capture());

        var secondRequestMessages = captor.getAllValues().get(1).messages();
        Assert.assertEquals(3, secondRequestMessages.size());
    }

    @Test
    public void stream_withStreamingModel_returnsStreamedTokens() {
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
        Assert.assertEquals(tokens, results);
    }

    @Test
    public void stream_withNonStreamingModel_returnsResponse() {
        var request = createSimpleRequest("Hello");
        mockSimpleChat(request, "Full response");

        var results = provider.stream(request).collectList().block();

        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("Full response", results.getFirst());
    }

    @Test
    public void stream_chatModelThrowsException_propagatesError() {
        var request = createSimpleRequest("Hello");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenThrow(new RuntimeException("API error"));
        Assert.assertThrows(RuntimeException.class,
                () -> provider.stream(request).blockFirst());
    }

    @Test
    public void stream_emptyTextResponse_returnsEmpty() {
        var request = createSimpleRequest("Hello");
        var response = mockSimpleResponse("");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);
        var results = provider.stream(request).collectList().block();
        Assert.assertNotNull(results);
        Assert.assertTrue(results.isEmpty());
    }

    @Test
    public void stream_nullTextResponse_returnsEmpty() {
        var request = createSimpleRequest("Hello");
        var response = mockSimpleResponse(null);
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);
        var results = provider.stream(request).collectList().block();
        Assert.assertNotNull(results);
        Assert.assertTrue(results.isEmpty());
    }

    @Test
    public void stream_withSystemPromptInRequest_usesRequestPrompt() {
        var request = new TestLLMRequest("Hello", "You are a helpful assistant",
                Collections.emptyList(), new Object[0]);

        mockSimpleChat(request, "Response");

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());

        var messages = captor.getValue().messages();
        Assert.assertTrue("Should contain system message",
                messages.stream().anyMatch(SystemMessage.class::isInstance));
    }

    @Test
    public void stream_withNullSystemPrompt_noSystemMessage() {
        var request = createSimpleRequest("Hello");

        mockSimpleChat(request, "Response");

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());

        var messages = captor.getValue().messages();
        Assert.assertFalse("Should not contain system message",
                messages.stream().anyMatch(SystemMessage.class::isInstance));
    }

    @Test
    public void stream_withEmptySystemPrompt_noSystemMessage() {
        var request = new TestLLMRequest("Hello", "   ",
                Collections.emptyList(), new Object[0]);

        mockSimpleChat(request, "Response");

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());

        var messages = captor.getValue().messages();
        Assert.assertFalse("Should not contain system message",
                messages.stream().anyMatch(SystemMessage.class::isInstance));
    }

    @Test
    public void stream_preservesChatHistoryAcrossRequests() {
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
        Assert.assertEquals("First call should have 1 user message", 1,
                allMessages.get(0).messages().size());
        Assert.assertEquals(
                "Second call should have 3 messages (user1, ai1, user2)", 3,
                allMessages.get(1).messages().size());
    }

    @Test
    public void stream_withNullAiMessage_returnsEmptyMessage() {
        var request = createSimpleRequest("Hello");

        var response = Mockito.mock(ChatResponse.class);
        Mockito.when(response.aiMessage()).thenReturn(null);
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        var results = provider.stream(request).collectList().block();

        Assert.assertNotNull(results);
        Assert.assertTrue(results.isEmpty());
    }

    @Test
    public void stream_withMaxMessagesLimit_dropsOldestMessages() {
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
        Assert.assertTrue("Message count should not exceed memory limit, got: "
                + messageCount, messageCount <= 30);

        var userMessageTextContents = getUserMessageContents(lastRequest,
                TextContent.class).stream().map(TextContent::text).toList();
        Assert.assertFalse("Should not contain very old messages",
                userMessageTextContents.stream()
                        .anyMatch(text -> text.contains("Message 0")));
        Assert.assertTrue("Should contain recent messages",
                userMessageTextContents.stream().anyMatch(text -> text
                        .contains("Message " + (requestCount - 1))));
    }

    @Test
    public void stream_withImageAttachment_convertsToBase64() {
        var imageData = "fake-image-data".getBytes();
        var attachment = new TestAttachment(imageData, "image/png", "test.png");
        var request = new TestLLMRequest("Describe this image", null,
                List.of(attachment), new Object[0]);

        mockSimpleChat(request, "It's a test");

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());

        var userMessageContents = getUserMessageContents(captor.getValue(),
                ImageContent.class);
        Assert.assertFalse("Should contain image content",
                userMessageContents.isEmpty());
    }

    @Test
    public void stream_withTextAttachment_usesUTF8Encoding() {
        var textContent = "Test UTF-8: é à ü";
        var attachment = new TestAttachment(
                textContent.getBytes(StandardCharsets.UTF_8), "text/plain",
                "test.txt");
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

        Assert.assertTrue(textContentPreserved);
    }

    @Test
    public void stream_withNullAttachments_returnsResponse() {
        var request = new TestLLMRequest("Hello", null, null, new Object[0]);
        mockSimpleChat(request, "Hi");
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
        var response = mockSimpleResponse("hi");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        Assert.assertThrows(NullPointerException.class,
                () -> provider.stream(request).blockFirst());
    }

    @Test
    public void stream_withUnsupportedAttachmentType_ignoresAttachment() {
        var attachment = new TestAttachment("data".getBytes(),
                "application/octet-stream", "file.bin");
        var request = new TestLLMRequest("Process this", null,
                List.of(attachment), new Object[0]);

        mockSimpleChat(request, "Done");

        Mockito.verify(mockChatModel).chat(Mockito.any(ChatRequest.class));
    }

    @Test
    public void stream_withPdfAttachment_handlesPdf() {
        var pdfData = "PDF binary content".getBytes(StandardCharsets.UTF_8);
        var attachment = new TestAttachment(pdfData, "application/pdf",
                "document.pdf");
        var request = new TestLLMRequest("Summarize this document", null,
                List.of(attachment), new Object[0]);

        mockSimpleChat(request, "Summary");

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());

        var userMessage = (UserMessage) captor.getValue().messages().getFirst();
        var pdfContent = userMessage.contents().stream()
                .filter(PdfFileContent.class::isInstance).findFirst()
                .orElse(null);

        Assert.assertNotNull("Should include PDF content as PdfFileContent",
                pdfContent);
    }

    @Test
    public void stream_withBinaryPdfData_handlesBinaryPdf() {
        // Binary PDF data should be handled correctly with base64 encoding
        var binaryPdfData = new byte[] { 0x25, 0x50, 0x44, 0x46, (byte) 0xFF,
                (byte) 0xFE, (byte) 0x00, (byte) 0x80 };
        var attachment = new TestAttachment(binaryPdfData, "application/pdf",
                "binary.pdf");
        var request = new TestLLMRequest("Summarize", null, List.of(attachment),
                new Object[0]);

        mockSimpleChat(request, "Summary");

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());

        var userMessage = (UserMessage) captor.getValue().messages().getFirst();
        var pdfContent = userMessage.contents().stream()
                .filter(PdfFileContent.class::isInstance).findFirst()
                .orElse(null);

        Assert.assertNotNull("Should handle binary PDF data", pdfContent);
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
        mockSimpleChat(request, "Processed");

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel).chat(captor.capture());

        var messages = captor.getValue().messages();
        var userMessage = (UserMessage) messages.getFirst();

        Assert.assertEquals(4, userMessage.contents().size());
    }

    @Test
    public void stream_withToolExecutionDepthLimit_stopsAtMaxDepth() {
        var request = new TestLLMRequest("Infinite loop", null,
                Collections.emptyList(),
                new Object[] { new SampleToolsClass() });

        // Always return tool execution request (infinite loop)
        var response = mockSimpleResponseWithTool("getTemperature");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);

        try {
            provider.stream(request).blockFirst();
            Assert.fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertTrue(
                    "Error message should mention depth exceeded: "
                            + e.getMessage(),
                    e.getMessage()
                            .contains("Maximum tool execution depth exceeded"));
        }

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(20)).chat(captor.capture());
    }

    @Test
    public void stream_withSlowTool_toolExecutionTimesOut() {
        mockUi();
        var mockUI = UI.getCurrent();
        // Tool sleeps for 2 seconds, but timeout is 1 second
        provider.setToolExecutionTimeoutSeconds(1);

        var slowTool = new SlowTool(2);
        var request = new TestLLMRequest("Hello", null, Collections.emptyList(),
                new Object[] { slowTool });

        var response1 = mockSimpleResponseWithTool("slowOperation");
        var response2 = mockSimpleResponse("Response 2");
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2);
        provider.stream(request).blockFirst();

        Mockito.verify(mockUI).access(Mockito.any(Command.class));

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(2)).chat(captor.capture());

        var secondRequest = captor.getAllValues().get(1);
        var toolResults = getToolExecutionResults(secondRequest);

        Assert.assertEquals(1, toolResults.size());
        Assert.assertTrue(
                toolResults.getFirst().text().contains("Error executing tool"));
        Assert.assertFalse(toolResults.getFirst().text()
                .contains(slowTool.getCompletedMessage()));
    }

    @Test
    public void stream_withNullToolExecutor_addsToolNotFoundMessageToRequest() {
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
        Assert.assertEquals(1, toolResults.size());
        Assert.assertTrue(
                toolResults.getFirst().text().contains("Tool not found"));
    }

    @Test
    public void stream_withoutUI_executesTool() {
        var uiTools = new UITools();
        var request = new TestLLMRequest("Do UI action", null,
                Collections.emptyList(), new Object[] { uiTools });

        var response1 = mockSimpleResponseWithTool("uiAction");
        var response2 = mockSimpleResponse("UI action done");

        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2);

        var result = provider.stream(request).blockFirst();

        Assert.assertEquals("UI action done", result);
        Assert.assertFalse("Tool should not have executed in UI context",
                uiTools.isExecutedInUIContext());

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(2)).chat(captor.capture());
        var secondRequest = captor.getAllValues().get(1);
        var toolResults = getToolExecutionResults(secondRequest);
        Assert.assertEquals(1, toolResults.size());
        Assert.assertEquals(toolResults.getFirst().text(),
                uiTools.getUiActionResult());
    }

    @Test
    public void stream_withUI_executesToolInUIContext() throws Exception {
        mockUi();
        var uiTools = new UITools();
        var request = new TestLLMRequest("Do UI action", null,
                Collections.emptyList(), new Object[] { uiTools });

        var response1 = mockSimpleResponseWithTool("uiAction");
        var response2 = mockSimpleResponse("UI action done");

        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response1, response2);

        var result = provider.stream(request).blockFirst();

        Assert.assertEquals("UI action done", result);
        Mockito.verify(UI.getCurrent()).access(Mockito.any(Command.class));
        Assert.assertTrue("Tool should have executed in UI context",
                uiTools.isExecutedInUIContext());

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockChatModel, Mockito.times(2)).chat(captor.capture());
        var secondRequest = captor.getAllValues().get(1);
        var toolResults = getToolExecutionResults(secondRequest);
        Assert.assertEquals(1, toolResults.size());
        Assert.assertEquals(toolResults.getFirst().text(),
                uiTools.getUiActionResult());
    }

    @Test
    public void stream_withStreamingModelAndTool_executesTool() {
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

        Assert.assertNotNull(results);
        Assert.assertEquals("Should have streamed tokens", 3, results.size());

        var captor = ArgumentCaptor.forClass(ChatRequest.class);
        Mockito.verify(mockStreamingChatModel, Mockito.times(2)).chat(
                captor.capture(),
                Mockito.any(StreamingChatResponseHandler.class));

        var secondRequest = captor.getAllValues().get(1);
        var toolResults = getToolExecutionResults(secondRequest);
        Assert.assertEquals(1, toolResults.size());
        Assert.assertEquals(toolResults.getFirst().text(),
                toolObject.getTemperature());
    }

    @Test
    public void stream_withMultipleToolCalls_executesTools() {
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

        Assert.assertEquals(2, toolResults.size());
        var resultTexts = toolResults.stream()
                .map(ToolExecutionResultMessage::text).toList();
        Assert.assertTrue(resultTexts.contains(toolObject.getTemperature()));
        Assert.assertTrue(resultTexts.contains(toolObject.getHumidity()));
    }

    @Test
    public void stream_withToolError_addsErrorMessageToRequest() {
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

        Assert.assertEquals(1, toolResultMessages.size());
        Assert.assertEquals(toolObject.getErrorMessage(),
                toolResultMessages.getFirst().text());
    }

    private void mockSimpleChat(LLMRequest request, String responseText) {
        var response = mockSimpleResponse(responseText);
        Mockito.when(mockChatModel.chat(Mockito.any(ChatRequest.class)))
                .thenReturn(response);
        provider.stream(request).blockFirst();
    }

    private static List<ToolExecutionResultMessage> getToolExecutionResults(
            ChatRequest request) {
        return request.messages().stream()
                .filter(ToolExecutionResultMessage.class::isInstance)
                .map(ToolExecutionResultMessage.class::cast).toList();
    }

    /**
     * Creates a mock UI that executes commands asynchronously, similar to real
     * UI.access() behavior. Commands run in a separate thread and Future.get()
     * waits for completion with real timeout support.
     */
    private static void mockUi() {
        var mockUI = Mockito.mock(UI.class);
        Mockito.doAnswer(invocation -> {
            Command command = invocation.getArgument(0);
            var futureTask = new FutureTask<Void>(() -> {
                // Set UI in the worker thread, mimicking real UI.access()
                // behavior
                UI.setCurrent(mockUI);
                try {
                    command.execute();
                } finally {
                    UI.setCurrent(null);
                }
                return null;
            });
            new Thread(futureTask).start();
            return futureTask;
        }).when(mockUI).access(Mockito.any(Command.class));
        UI.setCurrent(mockUI);
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
        var aiMessage1 = Mockito.mock(AiMessage.class);
        Mockito.when(aiMessage1.text()).thenReturn("");
        Mockito.when(aiMessage1.hasToolExecutionRequests()).thenReturn(true);
        var toolRequest = Mockito.mock(ToolExecutionRequest.class);
        Mockito.when(toolRequest.name()).thenReturn(toolName);
        Mockito.when(toolRequest.arguments()).thenReturn("{}");
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
            List<Attachment> attachments,
            Object[] tools) implements LLMRequest {
    }

    private record TestAttachment(byte[] data, String contentType,
            String fileName) implements Attachment {
    }

    private static class UITools {
        private boolean executedInUIContext = false;

        public boolean isExecutedInUIContext() {
            return executedInUIContext;
        }

        public String getUiActionResult() {
            return "UI action executed";
        }

        @Tool
        public String uiAction() {
            executedInUIContext = UI.getCurrent() != null;
            return getUiActionResult();
        }
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

    private record SlowTool(int sleepDurationSeconds) {

        public String getCompletedMessage() {
            return "Completed after " + sleepDurationSeconds + " seconds";
        }

        @Tool
        public String slowOperation() {
            try {
                Thread.sleep(sleepDurationSeconds * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Interrupted";
            }
            return getCompletedMessage();
        }
    }
}
