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
package com.vaadin.flow.component.ai.orchestrator;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.component.AiFileReceiver;
import com.vaadin.flow.component.ai.component.AiInput;
import com.vaadin.flow.component.ai.component.AiMessage;
import com.vaadin.flow.component.ai.component.AiMessageList;
import com.vaadin.flow.component.ai.component.InputSubmitEvent;
import com.vaadin.flow.component.ai.component.InputSubmitListener;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.streams.UploadHandler;

import reactor.core.publisher.Flux;

public class AiOrchestratorTest {

    private LLMProvider mockProvider;
    private AiMessageList mockMessageList;
    private AiInput mockInput;
    private AiFileReceiver mockFileReceiver;

    @Before
    public void setup() {
        mockProvider = Mockito.mock(LLMProvider.class);
        mockMessageList = Mockito.mock(AiMessageList.class);
        mockInput = Mockito.mock(AiInput.class);
        mockFileReceiver = Mockito.mock(AiFileReceiver.class);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void builder_withNullProvider_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class,
                () -> AiOrchestrator.builder(null));
    }

    @Test
    public void builder_withProvider_usesProviderForPrompts() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AiOrchestrator.builder(mockProvider)
                .withMessageList(mockMessageList).build();
        orchestrator.prompt("Hello");

        Mockito.verify(mockProvider)
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    public void builder_withSystemPrompt_includesSystemPromptInRequest() {
        mockUi();
        var systemPrompt = "You are a helpful assistant";
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AiOrchestrator.builder(mockProvider, systemPrompt)
                .withMessageList(mockMessageList).build();
        orchestrator.prompt("Hello");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        Assert.assertEquals(systemPrompt, captor.getValue().systemPrompt());
    }

    @Test
    public void builder_withMessageList_addsMessagesToList() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AiOrchestrator.builder(mockProvider)
                .withMessageList(mockMessageList).build();
        orchestrator.prompt("Hello");

        Mockito.verify(mockMessageList, Mockito.atLeast(1))
                .addMessage(Mockito.any(AiMessage.class));
    }

    @Test
    public void builder_withInput_addsSubmitListener() {
        AiOrchestrator.builder(mockProvider).withInput(mockInput).build();

        Mockito.verify(mockInput)
                .addSubmitListener(Mockito.any(InputSubmitListener.class));
    }

    @Test
    public void builder_withFileReceiver_configuresUploadHandler() {
        AiOrchestrator.builder(mockProvider).withFileReceiver(mockFileReceiver)
                .build();

        Mockito.verify(mockFileReceiver)
                .setUploadHandler(Mockito.any(UploadHandler.class));
        Mockito.verify(mockFileReceiver)
                .addFileRemovedListener(Mockito.any(Consumer.class));
    }

    @Test
    public void builder_withToolObjects_setsTools() {
        mockUi();
        var tool = new SampleTool();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AiOrchestrator.builder(mockProvider)
                .withMessageList(mockMessageList).withTools(tool).build();

        orchestrator.prompt("Use tool");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        Assert.assertEquals(1, captor.getValue().tools().length);
        Assert.assertSame(tool, captor.getValue().tools()[0]);
    }

    @Test
    public void builder_withNullToolObjects_handlesNullGracefully() {
        var orchestrator = AiOrchestrator.builder(mockProvider)
                .withTools((Object[]) null).build();
        Assert.assertNotNull(orchestrator);
    }

    @Test
    public void prompt_withValidMessage_sendsRequestToProvider() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        prompt("Hello");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        Assert.assertEquals("Hello", captor.getValue().userMessage());
    }

    @Test
    public void prompt_withNullMessage_doesNotSendRequest() {
        prompt(null);
        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    public void prompt_withEmptyMessage_doesNotSendRequest() {
        prompt("   ");
        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    public void prompt_withSystemPrompt_includesSystemPromptInRequest() {
        mockUi();
        var systemPrompt = "You are a helpful assistant";
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AiOrchestrator.builder(mockProvider, systemPrompt)
                .withMessageList(mockMessageList).build();
        orchestrator.prompt("Hello");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        Assert.assertEquals(systemPrompt, captor.getValue().systemPrompt());
    }

    @Test
    public void prompt_withStreamingResponse_updatesMessageWithTokens()
            throws Exception {
        mockUi();
        var mockMessage = createMockMessage();
        var latch = new CountDownLatch(3);
        Mockito.doAnswer(inv -> {
            latch.countDown();
            return null;
        }).when(mockMessage).appendText(Mockito.anyString());
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Hello", " ", "World"));

        prompt("Hi");

        Assert.assertTrue("Tokens should be appended within timeout",
                latch.await(2, TimeUnit.SECONDS));

        var inOrder = Mockito.inOrder(mockMessage);
        inOrder.verify(mockMessage).appendText("Hello");
        inOrder.verify(mockMessage).appendText(" ");
        inOrder.verify(mockMessage).appendText("World");
    }

    @Test
    public void prompt_withStreamingError_setsErrorMessage() throws Exception {
        mockUi();
        var mockMessage = createMockMessage();
        var latch = new CountDownLatch(1);
        Mockito.doAnswer(inv -> {
            latch.countDown();
            return null;
        }).when(mockMessage).setText(Mockito.anyString());
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.error(new RuntimeException("API Error")));

        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("Hi");

        Assert.assertTrue("Error message should be set within timeout",
                latch.await(2, TimeUnit.SECONDS));

        Mockito.verify(mockMessage)
                .setText("An error occurred. Please try again.");
    }

    @Test
    public void inputSubmit_triggersPromptProcessing() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        getSimpleOrchestrator();

        var listenerCaptor = ArgumentCaptor.forClass(InputSubmitListener.class);
        Mockito.verify(mockInput).addSubmitListener(listenerCaptor.capture());

        var event = Mockito.mock(InputSubmitEvent.class);
        Mockito.when(event.getValue()).thenReturn("User message");
        listenerCaptor.getValue().onSubmit(event);

        Mockito.verify(mockProvider)
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    public void userMessage_isAddedToMessageList() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("Hello");

        Mockito.verify(mockMessageList, Mockito.times(2))
                .addMessage(Mockito.any(AiMessage.class));
    }

    @Test
    public void assistantPlaceholder_isCreated() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("Hello");

        Mockito.verify(mockMessageList).createMessage("", "Assistant");
    }

    @Test
    public void prompt_withTools_includesToolsInRequest() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var tool = new SampleTool();
        var orchestrator = AiOrchestrator.builder(mockProvider)
                .withMessageList(mockMessageList).withTools(tool).build();

        orchestrator.prompt("Get temperature");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        Assert.assertEquals(1, captor.getValue().tools().length);
        Assert.assertEquals(tool, captor.getValue().tools()[0]);
    }

    @Test
    public void prompt_withoutUIContext_throwsIllegalStateException() {
        UI.setCurrent(null);
        var orchestrator = getSimpleOrchestrator();
        Assert.assertThrows(IllegalStateException.class,
                () -> orchestrator.prompt("Hello"));
    }

    @Test
    public void prompt_whileProcessing_isIgnored() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.never());

        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("First");

        orchestrator.prompt("Second");

        Mockito.verify(mockProvider, Mockito.times(1))
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    public void prompt_withoutSystemPrompt_sendsNullSystemPromptInRequest() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AiOrchestrator.builder(mockProvider)
                .withMessageList(mockMessageList).build();
        orchestrator.prompt("Hello");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        Assert.assertNull(captor.getValue().systemPrompt());
    }

    @Test
    public void prompt_withWhitespaceOnlySystemPrompt_sendsNullSystemPrompt() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AiOrchestrator.builder(mockProvider, "   ")
                .withMessageList(mockMessageList).build();

        orchestrator.prompt("Hello");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        Assert.assertNull(captor.getValue().systemPrompt());
    }

    @Test
    public void builder_withFlowMessageList_wrapsCorrectly() {
        mockUi();
        var flowMessageList = Mockito.mock(MessageList.class);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));
        var orchestrator = AiOrchestrator.builder(mockProvider)
                .withMessageList(flowMessageList).build();
        orchestrator.prompt("Hello");
        Mockito.verify(flowMessageList, Mockito.atLeast(1))
                .addItem(Mockito.any());
    }

    @Test
    public void builder_withFlowMessageInput_wrapsCorrectly() {
        var flowMessageInput = Mockito.mock(MessageInput.class);
        AiOrchestrator.builder(mockProvider).withInput(flowMessageInput)
                .build();

        Mockito.verify(flowMessageInput).addSubmitListener(Mockito.any());
    }

    @Test
    public void builder_withFlowUpload_wrapsCorrectly() {
        var flowUploadManager = Mockito.mock(UploadManager.class);
        var orchestrator = AiOrchestrator.builder(mockProvider)
                .withFileReceiver(flowUploadManager).build();
        Assert.assertNotNull(orchestrator);
        Mockito.verify(flowUploadManager).setUploadHandler(Mockito.any());
        Mockito.verify(flowUploadManager).addFileRemovedListener(Mockito.any());
    }

    @Test
    public void inputSubmit_withNullValue_doesNotProcess() {
        mockUi();
        getSimpleOrchestrator();
        var listenerCaptor = ArgumentCaptor.forClass(InputSubmitListener.class);
        Mockito.verify(mockInput).addSubmitListener(listenerCaptor.capture());
        var event = Mockito.mock(InputSubmitEvent.class);
        Mockito.when(event.getValue()).thenReturn(null);
        listenerCaptor.getValue().onSubmit(event);
        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    public void inputSubmit_withEmptyValue_doesNotProcess() {
        mockUi();
        getSimpleOrchestrator();
        var listenerCaptor = ArgumentCaptor.forClass(InputSubmitListener.class);

        Mockito.verify(mockInput).addSubmitListener(listenerCaptor.capture());

        var event = Mockito.mock(InputSubmitEvent.class);
        Mockito.when(event.getValue()).thenReturn("   ");
        listenerCaptor.getValue().onSubmit(event);

        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    public void inputSubmit_whileProcessing_isIgnored() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.never());

        getSimpleOrchestrator();

        var listenerCaptor = ArgumentCaptor.forClass(InputSubmitListener.class);

        Mockito.verify(mockInput).addSubmitListener(listenerCaptor.capture());

        var event1 = Mockito.mock(InputSubmitEvent.class);
        Mockito.when(event1.getValue()).thenReturn("First");
        listenerCaptor.getValue().onSubmit(event1);

        var event2 = Mockito.mock(InputSubmitEvent.class);
        Mockito.when(event2.getValue()).thenReturn("Second");
        listenerCaptor.getValue().onSubmit(event2);

        Mockito.verify(mockProvider, Mockito.times(1))
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    public void prompt_withAttachments_includesAttachmentsInRequest() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = getSimpleOrchestrator();

        var handlerCaptor = ArgumentCaptor.forClass(UploadHandler.class);
        Mockito.verify(mockFileReceiver)
                .setUploadHandler(handlerCaptor.capture());

        orchestrator.prompt("Hello with attachment");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        Assert.assertNotNull(captor.getValue().attachments());
    }

    @Test
    public void prompt_withTimeout_setsTimeoutErrorMessage() throws Exception {
        mockUi();
        var mockMessage = createMockMessage();
        var latch = new CountDownLatch(1);
        Mockito.doAnswer(inv -> {
            latch.countDown();
            return null;
        }).when(mockMessage).setText(Mockito.anyString());
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.error(new TimeoutException("Timed out")));

        prompt("Hello");

        Assert.assertTrue("Timeout error should be set within timeout",
                latch.await(2, TimeUnit.SECONDS));

        Mockito.verify(mockMessage)
                .setText("Request timed out. Please try again.");
    }

    @Test
    public void prompt_withoutMessageList_stillSendsToProvider() {
        mockUi();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        prompt("Hello");

        Mockito.verify(mockProvider)
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    public void prompt_withEmptyResponse_completesSuccessfully() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.empty());

        var orchestrator = AiOrchestrator.builder(mockProvider)
                .withMessageList(mockMessageList).build();

        orchestrator.prompt("Hello");

        Mockito.verify(mockMessage, Mockito.never())
                .appendText(Mockito.anyString());
    }

    @Test
    public void prompt_withSystemPromptWithLeadingTrailingWhitespace_trimmed() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AiOrchestrator
                .builder(mockProvider, "  You are helpful  ")
                .withMessageList(mockMessageList).build();

        orchestrator.prompt("Hello");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        Assert.assertEquals("You are helpful",
                captor.getValue().systemPrompt());
    }

    @Test
    public void prompt_withMultipleTokens_appendsAllTokens() throws Exception {
        mockUi();
        var mockMessage = createMockMessage();
        var latch = new CountDownLatch(4);
        Mockito.doAnswer(inv -> {
            latch.countDown();
            return null;
        }).when(mockMessage).appendText(Mockito.anyString());
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Token1", "Token2", "Token3", "Token4"));

        prompt("Hello");

        Assert.assertTrue("All tokens should be appended within timeout",
                latch.await(2, TimeUnit.SECONDS));

        Mockito.verify(mockMessage, Mockito.times(4))
                .appendText(Mockito.anyString());
    }

    @Test
    public void prompt_requestContainsCorrectTools() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var tool1 = new SampleTool();
        var tool2 = new Object();
        var orchestrator = AiOrchestrator.builder(mockProvider)
                .withMessageList(mockMessageList).withTools(tool1, tool2)
                .build();

        orchestrator.prompt("Use tools");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        Assert.assertEquals(2, captor.getValue().tools().length);
        Assert.assertSame(tool1, captor.getValue().tools()[0]);
        Assert.assertSame(tool2, captor.getValue().tools()[1]);
    }

    @Test
    public void prompt_requestContainsEmptyAttachmentsList() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        prompt("Hello");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        Assert.assertNotNull(captor.getValue().attachments());
        Assert.assertTrue(captor.getValue().attachments().isEmpty());
    }

    @Test
    public void fileRemoval_withNonExistentFile_doesNothing() throws Exception {
        var orchestrator = getSimpleOrchestrator();

        var listenerCaptor = ArgumentCaptor.forClass(Consumer.class);
        Mockito.verify(mockFileReceiver)
                .addFileRemovedListener(listenerCaptor.capture());

        var pendingAttachments = getPendingAttachments(orchestrator);
        pendingAttachments.add(createPendingAttachment("existing.txt"));

        Assert.assertEquals(1, pendingAttachments.size());

        @SuppressWarnings("unchecked")
        Consumer<String> fileRemovedListener = listenerCaptor.getValue();
        fileRemovedListener.accept("non-existent.txt");

        Assert.assertEquals(1, pendingAttachments.size());
        Assert.assertEquals("existing.txt",
                pendingAttachments.getFirst().fileName());
    }

    @Test
    public void prompt_clearsPendingAttachments() throws Exception {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = getSimpleOrchestrator();
        var pendingAttachments = getPendingAttachments(orchestrator);
        pendingAttachments.add(createPendingAttachment("test.txt"));
        orchestrator.prompt("Hello");
        Assert.assertTrue(pendingAttachments.isEmpty());
        Mockito.verify(mockFileReceiver).clearFileList();
    }

    @Test
    public void builder_namesNotConfigured_usesDefaultNames() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AiOrchestrator.builder(mockProvider)
                .withMessageList(mockMessageList).build();
        orchestrator.prompt("Hello");

        var inOrder = Mockito.inOrder(mockMessageList);
        inOrder.verify(mockMessageList).createMessage("Hello", "You");
        inOrder.verify(mockMessageList).createMessage("", "Assistant");
    }

    @Test
    public void builder_withCustomUserName_usesCustomUserName() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AiOrchestrator.builder(mockProvider)
                .withMessageList(mockMessageList).withUserName("John Doe")
                .build();
        orchestrator.prompt("Hello");

        Mockito.verify(mockMessageList).createMessage("Hello", "John Doe");
    }

    @Test
    public void builder_withCustomAiName_usesCustomAiName() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AiOrchestrator.builder(mockProvider)
                .withMessageList(mockMessageList).withAiName("Claude").build();
        orchestrator.prompt("Hello");

        Mockito.verify(mockMessageList).createMessage("", "Claude");
    }

    @Test
    public void builder_withCustomUserNameAndAiName_usesBothCustomNames() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AiOrchestrator.builder(mockProvider)
                .withMessageList(mockMessageList).withUserName("Alice")
                .withAiName("Bot").build();
        orchestrator.prompt("Hello");

        var inOrder = Mockito.inOrder(mockMessageList);
        inOrder.verify(mockMessageList).createMessage("Hello", "Alice");
        inOrder.verify(mockMessageList).createMessage("", "Bot");
    }

    @Test
    public void builder_withNullUserName_throws() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.any())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var builder = AiOrchestrator.builder(mockProvider)
                .withMessageList(mockMessageList);
        Assert.assertThrows(NullPointerException.class,
                () -> builder.withUserName(null));
    }

    @Test
    public void builder_withNullAiName_throws() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.createMessage(Mockito.anyString(),
                Mockito.any())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var builder = AiOrchestrator.builder(mockProvider)
                .withMessageList(mockMessageList);
        Assert.assertThrows(NullPointerException.class,
                () -> builder.withAiName(null));
    }

    private AiOrchestrator getSimpleOrchestrator() {
        return AiOrchestrator.builder(mockProvider)
                .withMessageList(mockMessageList)
                .withFileReceiver(mockFileReceiver).withInput(mockInput)
                .build();
    }

    private void prompt(String message) {
        getSimpleOrchestrator().prompt(message);
    }

    private static void mockUi() {
        var mockUI = Mockito.mock(UI.class);
        Mockito.doAnswer(invocation -> {
            Command command = invocation.getArgument(0);
            var futureTask = new FutureTask<Void>(() -> {
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

    private static AiMessage createMockMessage() {
        var message = Mockito.mock(AiMessage.class);
        Mockito.when(message.getText()).thenReturn("");
        Mockito.when(message.getTime()).thenReturn(Instant.now());
        Mockito.when(message.getUserName()).thenReturn("Test");
        return message;
    }

    private static class SampleTool {
        public String getTemperature() {
            return "22°C";
        }
    }

    @SuppressWarnings("unchecked")
    private static List<LLMProvider.Attachment> getPendingAttachments(
            AiOrchestrator orchestrator) throws Exception {
        var field = AiOrchestrator.class.getDeclaredField("pendingAttachments");
        field.setAccessible(true);
        return (List<LLMProvider.Attachment>) field.get(orchestrator);
    }

    private static LLMProvider.Attachment createPendingAttachment(
            String fileName) {
        return new LLMProvider.Attachment() {
            @Override
            public String fileName() {
                return fileName;
            }

            @Override
            public String contentType() {
                return "text/plain";
            }

            @Override
            public byte[] data() {
                return "test".getBytes();
            }
        };
    }
}
