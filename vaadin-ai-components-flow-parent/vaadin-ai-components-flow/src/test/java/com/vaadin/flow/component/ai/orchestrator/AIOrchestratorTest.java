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
package com.vaadin.flow.component.ai.orchestrator;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.imageio.ImageIO;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.common.ChatMessage;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.ui.AIFileReceiver;
import com.vaadin.flow.component.ai.ui.AIInput;
import com.vaadin.flow.component.ai.ui.AIMessage;
import com.vaadin.flow.component.ai.ui.AIMessageList;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.UploadHandler;

import reactor.core.publisher.Flux;

public class AIOrchestratorTest {

    private LLMProvider mockProvider;
    private AIMessageList mockMessageList;
    private AIInput mockInput;
    private AIFileReceiver mockFileReceiver;
    private static MockedStatic<FeatureFlags> mockFeatureFlagsStatic;

    private static UI mockUI;

    @Before
    public void setup() {
        mockProvider = Mockito.mock(LLMProvider.class);
        mockMessageList = Mockito.mock(AIMessageList.class);
        mockInput = Mockito.mock(AIInput.class);
        mockFileReceiver = Mockito.mock(AIFileReceiver.class);
        Mockito.when(mockFileReceiver.takeAttachments())
                .thenReturn(Collections.emptyList());
    }

    @After
    public void tearDown() {
        if (mockFeatureFlagsStatic != null) {
            mockFeatureFlagsStatic.close();
            mockFeatureFlagsStatic = null;
        }
        UI.setCurrent(null);
        mockUI = null;
    }

    @Test
    public void builder_withNullProvider_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class,
                () -> AIOrchestrator.builder(null, null));
    }

    @Test
    public void builder_withProvider_usesProviderForPrompts() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
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
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AIOrchestrator.builder(mockProvider, systemPrompt)
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
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).build();
        orchestrator.prompt("Hello");

        Mockito.verify(mockMessageList, Mockito.atLeast(1)).addMessage(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyList());
    }

    @Test
    public void builder_withInput_addsSubmitListener() {
        AIOrchestrator.builder(mockProvider, null).withInput(mockInput).build();

        Mockito.verify(mockInput)
                .addSubmitListener(Mockito.any(SerializableConsumer.class));
    }

    @Test
    public void builder_withToolObjects_setsTools() {
        mockUi();
        var tool = new SampleTool();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withTools(tool).build();

        orchestrator.prompt("Use tool");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        Assert.assertEquals(1, captor.getValue().tools().length);
        Assert.assertSame(tool, captor.getValue().tools()[0]);
    }

    @Test
    public void builder_withNullToolObjects_handlesNullGracefully() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withTools((Object[]) null).build();
        Assert.assertNotNull(orchestrator);
    }

    @Test
    public void prompt_withValidMessage_sendsRequestToProvider() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
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
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AIOrchestrator.builder(mockProvider, systemPrompt)
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
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
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
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
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

    @SuppressWarnings("unchecked")
    @Test
    public void inputSubmit_triggersPromptProcessing() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        getSimpleOrchestrator();

        var listenerCaptor = ArgumentCaptor
                .forClass(SerializableConsumer.class);
        Mockito.verify(mockInput).addSubmitListener(listenerCaptor.capture());

        listenerCaptor.getValue().accept("User message");

        Mockito.verify(mockProvider)
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    public void userMessage_isAddedToMessageList() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("Hello");

        Mockito.verify(mockMessageList, Mockito.times(2)).addMessage(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyList());
    }

    @Test
    public void assistantPlaceholder_isCreated() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("Hello");

        Mockito.verify(mockMessageList).addMessage("", "Assistant",
                Collections.emptyList());
    }

    @Test
    public void prompt_withTools_includesToolsInRequest() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var tool = new SampleTool();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
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
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
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
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
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
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AIOrchestrator.builder(mockProvider, "   ")
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
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(flowMessageList).build();
        orchestrator.prompt("Hello");
        Mockito.verify(flowMessageList, Mockito.atLeast(1))
                .addItem(Mockito.any());
    }

    @Test
    public void builder_withFlowMessageInput_wrapsCorrectly() {
        var flowMessageInput = Mockito.mock(MessageInput.class);
        AIOrchestrator.builder(mockProvider, null).withInput(flowMessageInput)
                .build();

        Mockito.verify(flowMessageInput).addSubmitListener(Mockito.any());
    }

    @Test
    public void builder_withFlowUpload_withExistingHandler_throws() {
        mockUi();
        var flowUploadManager = new UploadManager(new Div(),
                UploadHandler.inMemory((x, y) -> {
                }));
        var builder = AIOrchestrator.builder(mockProvider, null);
        Assert.assertThrows(IllegalArgumentException.class,
                () -> builder.withFileReceiver(flowUploadManager));
    }

    @Test
    public void builder_withFlowUpload_withoutHandler_succeeds() {
        mockUi();
        var flowUploadManager = new UploadManager(new Div());
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withFileReceiver(flowUploadManager).build();
        Assert.assertNotNull(orchestrator);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void inputSubmit_withNullValue_doesNotProcess() {
        mockUi();
        getSimpleOrchestrator();
        var listenerCaptor = ArgumentCaptor
                .forClass(SerializableConsumer.class);
        Mockito.verify(mockInput).addSubmitListener(listenerCaptor.capture());
        listenerCaptor.getValue().accept(null);
        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void inputSubmit_withEmptyValue_doesNotProcess() {
        mockUi();
        getSimpleOrchestrator();
        var listenerCaptor = ArgumentCaptor
                .forClass(SerializableConsumer.class);

        Mockito.verify(mockInput).addSubmitListener(listenerCaptor.capture());

        listenerCaptor.getValue().accept("   ");

        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void inputSubmit_whileProcessing_isIgnored() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.never());

        getSimpleOrchestrator();

        var listenerCaptor = ArgumentCaptor
                .forClass(SerializableConsumer.class);

        Mockito.verify(mockInput).addSubmitListener(listenerCaptor.capture());

        listenerCaptor.getValue().accept("First");

        listenerCaptor.getValue().accept("Second");

        Mockito.verify(mockProvider, Mockito.times(1))
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    public void prompt_withAttachments_includesAttachmentsInRequest() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));
        Mockito.when(mockFileReceiver.takeAttachments()).thenReturn(List.of(
                createAttachment("test.txt"), createAttachment("image.png")));

        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("Hello with attachment");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        var attachments = captor.getValue().attachments();
        Assert.assertNotNull(attachments);
        Assert.assertEquals(2, attachments.size());
        Assert.assertEquals("test.txt", attachments.getFirst().name());
        Assert.assertEquals("image.png", attachments.get(1).name());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void prompt_withAttachments_createsMessageWithAttachments() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));
        var attachment = createAttachment("test.txt");
        Mockito.when(mockFileReceiver.takeAttachments())
                .thenReturn(List.of(attachment));

        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("Hello with attachment");

        var attachmentsCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(mockMessageList).addMessage(
                Mockito.eq("Hello with attachment"), Mockito.eq("You"),
                attachmentsCaptor.capture());

        var aiAttachments = (List<AIAttachment>) attachmentsCaptor.getValue();
        Assert.assertEquals(1, aiAttachments.size());
        Assert.assertEquals("test.txt", aiAttachments.getFirst().name());
        Assert.assertEquals("text/plain", aiAttachments.getFirst().mimeType());
        Assert.assertArrayEquals("test".getBytes(),
                aiAttachments.getFirst().data());
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
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
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
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.empty());

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).build();

        orchestrator.prompt("Hello");

        Mockito.verify(mockMessage, Mockito.never())
                .appendText(Mockito.anyString());
    }

    @Test
    public void prompt_withSystemPromptWithLeadingTrailingWhitespace_trimmed() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AIOrchestrator
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
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
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
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var tool1 = new SampleTool();
        var tool2 = new Object();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
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
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
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
    public void prompt_callsTakeAttachments() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("Hello");
        Mockito.verify(mockFileReceiver).takeAttachments();
    }

    @Test
    public void builder_namesNotConfigured_usesDefaultNames() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).build();
        orchestrator.prompt("Hello");

        var inOrder = Mockito.inOrder(mockMessageList);
        inOrder.verify(mockMessageList).addMessage(Mockito.eq("Hello"),
                Mockito.eq("You"), Mockito.anyList());
        inOrder.verify(mockMessageList).addMessage("", "Assistant",
                Collections.emptyList());
    }

    @Test
    public void builder_withCustomUserName_usesCustomUserName() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withUserName("John Doe")
                .build();
        orchestrator.prompt("Hello");

        Mockito.verify(mockMessageList).addMessage(Mockito.eq("Hello"),
                Mockito.eq("John Doe"), Mockito.anyList());
    }

    @Test
    public void builder_withCustomAssistantName_usesCustomAssistantName() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withAssistantName("Claude")
                .build();
        orchestrator.prompt("Hello");

        Mockito.verify(mockMessageList).addMessage("", "Claude",
                Collections.emptyList());
    }

    @Test
    public void builder_withCustomUserNameAndAssistantName_usesBothCustomNames() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withUserName("Alice")
                .withAssistantName("Bot").build();
        orchestrator.prompt("Hello");

        var inOrder = Mockito.inOrder(mockMessageList);
        inOrder.verify(mockMessageList).addMessage(Mockito.eq("Hello"),
                Mockito.eq("Alice"), Mockito.anyList());
        inOrder.verify(mockMessageList).addMessage("", "Bot",
                Collections.emptyList());
    }

    @Test
    public void builder_withNullUserName_throws() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.any(), Mockito.anyList())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var builder = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList);
        Assert.assertThrows(NullPointerException.class,
                () -> builder.withUserName(null));
    }

    @Test
    public void builder_withNullAssistantName_throws() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.any(), Mockito.anyList())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var builder = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList);
        Assert.assertThrows(NullPointerException.class,
                () -> builder.withAssistantName(null));
    }

    @Test
    public void chatMessage_withMessageId_preservesMessageId() {
        var message = new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-123",
                null);
        Assert.assertEquals(ChatMessage.Role.USER, message.role());
        Assert.assertEquals("Hello", message.content());
        Assert.assertEquals("msg-123", message.messageId());
    }

    @Test
    public void chatMessage_withoutMessageId_setsMessageIdToNull() {
        var message = new ChatMessage(ChatMessage.Role.ASSISTANT, "Hi there",
                null, null);
        Assert.assertEquals(ChatMessage.Role.ASSISTANT, message.role());
        Assert.assertEquals("Hi there", message.content());
        Assert.assertNull(message.messageId());
        Assert.assertNull(message.time());
    }

    @Test
    public void chatMessage_withTime_preservesTime() {
        var now = Instant.now();
        var message = new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1",
                now);
        Assert.assertEquals(now, message.time());
        Assert.assertEquals("msg-1", message.messageId());
    }

    @Test
    public void getHistory_onFreshOrchestrator_returnsEmptyList() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null).build();
        Assert.assertTrue(orchestrator.getHistory().isEmpty());
    }

    @Test
    public void getHistory_afterPrompt_containsUserAndAssistantMessages() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("Hello");

        var history = orchestrator.getHistory();
        Assert.assertEquals(2, history.size());
        Assert.assertEquals(ChatMessage.Role.USER, history.getFirst().role());
        Assert.assertEquals("Hello", history.getFirst().content());
        Assert.assertNotNull(history.getFirst().messageId());
        Assert.assertEquals(ChatMessage.Role.ASSISTANT, history.get(1).role());
        Assert.assertEquals("Response", history.get(1).content());
        Assert.assertNull(history.get(1).messageId());
    }

    @Test
    public void getHistory_afterPrompt_recordsTimestamps() {
        mockUi();
        var before = Instant.now();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("Hello");
        var after = Instant.now();

        var history = orchestrator.getHistory();
        Assert.assertNotNull(history.getFirst().time());
        Assert.assertFalse(history.getFirst().time().isBefore(before));
        Assert.assertFalse(history.getFirst().time().isAfter(after));
        Assert.assertNotNull(history.get(1).time());
        Assert.assertFalse(history.get(1).time().isBefore(before));
        Assert.assertFalse(history.get(1).time().isAfter(after));
    }

    @Test
    public void withHistory_withTimestamps_restoresTimesOnUIMessages() {
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);

        var pastTime = Instant.parse("2025-06-15T10:30:00Z");
        var history = List.of(new ChatMessage(ChatMessage.Role.USER, "Hello",
                "msg-1", pastTime));

        AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withHistory(history, Collections.emptyMap()).build();

        Mockito.verify(mockMessage).setTime(pastTime);
    }

    @Test
    public void withHistory_withNullTimestamp_doesNotCallSetTime() {
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);

        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1", null));

        AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withHistory(history, Collections.emptyMap()).build();

        Mockito.verify(mockMessage, Mockito.never())
                .setTime(Mockito.any(Instant.class));
    }

    @Test
    public void getHistory_afterMultipleTokens_concatenatesAssistantResponse() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Hello", " ", "World"));

        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("Hi");

        var history = orchestrator.getHistory();
        Assert.assertEquals(2, history.size());
        Assert.assertEquals("Hello World", history.get(1).content());
    }

    @Test
    public void getHistory_afterStreamError_doesNotAddAssistantMessage() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.error(new RuntimeException("API Error")));

        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("Hi");

        var history = orchestrator.getHistory();
        Assert.assertEquals(1, history.size());
        Assert.assertEquals(ChatMessage.Role.USER, history.getFirst().role());
    }

    @Test
    public void getHistory_returnsUnmodifiableCopy() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null).build();
        var history = orchestrator.getHistory();
        Assert.assertThrows(UnsupportedOperationException.class,
                () -> history.add(new ChatMessage(ChatMessage.Role.USER, "test",
                        null, null)));
    }

    @Test
    public void withHistory_withNullHistory_throwsNullPointerException() {
        var builder = AIOrchestrator.builder(mockProvider, null);
        Assert.assertThrows(NullPointerException.class,
                () -> builder.withHistory(null, Collections.emptyMap()));
    }

    @Test
    public void withHistory_restoresProviderContext() {
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1", null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Hi there", null,
                        null));

        AIOrchestrator.builder(mockProvider, null)
                .withHistory(history, Collections.emptyMap()).build();

        Mockito.verify(mockProvider).setHistory(history,
                Collections.emptyMap());
    }

    @Test
    public void withHistory_restoresMessageListUI() {
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);

        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1", null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Hi there", null,
                        null));

        AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withHistory(history, Collections.emptyMap()).build();

        var inOrder = Mockito.inOrder(mockMessageList);
        inOrder.verify(mockMessageList).addMessage("Hello", "You",
                Collections.emptyList());
        inOrder.verify(mockMessageList).addMessage("Hi there", "Assistant",
                Collections.emptyList());
    }

    @Test
    public void withHistory_restoresConversationHistory() {
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1", null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Hi there", null,
                        null));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withHistory(history, Collections.emptyMap()).build();

        var restored = orchestrator.getHistory();
        Assert.assertEquals(2, restored.size());
        Assert.assertEquals("Hello", restored.getFirst().content());
        Assert.assertEquals("msg-1", restored.getFirst().messageId());
        Assert.assertEquals("Hi there", restored.get(1).content());
        Assert.assertNull(restored.get(1).messageId());
    }

    @Test
    public void withHistory_withCustomNames_usesCustomNamesInUI() {
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);

        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1", null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Hi there", null,
                        null));

        AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withUserName("Alice")
                .withAssistantName("Bot")
                .withHistory(history, Collections.emptyMap()).build();

        var inOrder = Mockito.inOrder(mockMessageList);
        inOrder.verify(mockMessageList).addMessage("Hello", "Alice",
                Collections.emptyList());
        inOrder.verify(mockMessageList).addMessage("Hi there", "Bot",
                Collections.emptyList());
    }

    @Test
    public void withHistory_rebuildsMsgIdMapping_attachmentClickWorks() {
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);

        var clickCaptor = ArgumentCaptor
                .forClass(AIMessageList.AttachmentClickCallback.class);

        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1", null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Hi there", null,
                        null));

        var clickEvents = new ArrayList<AttachmentClickListener.AttachmentClickEvent>();
        AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withAttachmentClickListener(clickEvents::add)
                .withHistory(history, Collections.emptyMap()).build();

        Mockito.verify(mockMessageList)
                .addAttachmentClickListener(clickCaptor.capture());

        // Simulate clicking an attachment on the restored user message
        clickCaptor.getValue().onAttachmentClick(mockMessage, 0);

        Assert.assertEquals(1, clickEvents.size());
        Assert.assertEquals("msg-1", clickEvents.getFirst().getMessageId());
        Assert.assertEquals(0, clickEvents.getFirst().getAttachmentIndex());
    }

    @Test
    public void withHistory_exceedingProviderMaxMessages_preservesFullHistory() {
        var history = new ArrayList<ChatMessage>();
        for (int i = 0; i < 20; i++) {
            history.add(new ChatMessage(ChatMessage.Role.USER, "Question " + i,
                    "msg-" + i, null));
            history.add(new ChatMessage(ChatMessage.Role.ASSISTANT,
                    "Answer " + i, null, null));
        }

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withHistory(history, Collections.emptyMap()).build();

        var restored = orchestrator.getHistory();
        Assert.assertEquals(
                "Orchestrator should preserve the full history regardless "
                        + "of provider's max message window",
                40, restored.size());
        Assert.assertEquals("Question 0", restored.getFirst().content());
        Assert.assertEquals("msg-0", restored.getFirst().messageId());
        Assert.assertEquals("Question 19", restored.get(38).content());
    }

    @Test
    public void withHistory_whenProviderThrows_buildThrows() {
        Mockito.doThrow(new UnsupportedOperationException("Not supported"))
                .when(mockProvider)
                .setHistory(Mockito.anyList(), Mockito.anyMap());

        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1", null));

        Assert.assertThrows(UnsupportedOperationException.class,
                () -> AIOrchestrator.builder(mockProvider, null)
                        .withHistory(history, Collections.emptyMap()).build());
    }

    @Test
    public void withHistory_withoutMessageList_onlyRestoresProvider() {
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1", null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Hi there", null,
                        null));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withHistory(history, Collections.emptyMap()).build();

        Mockito.verify(mockProvider).setHistory(history,
                Collections.emptyMap());
        var restored = orchestrator.getHistory();
        Assert.assertEquals(2, restored.size());
    }

    @Test
    public void withHistory_withAttachments_restoresAttachmentsInUI() {
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);

        var imageData = "fake-image".getBytes();
        var attachment = new AIAttachment("photo.png", "image/png", imageData);
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Look at this", "msg-1",
                        null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Nice photo!", null,
                        null));
        var attachments = Map.of("msg-1", List.of(attachment));

        AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withHistory(history, attachments).build();

        var inOrder = Mockito.inOrder(mockMessageList);
        inOrder.verify(mockMessageList).addMessage("Look at this", "You",
                List.of(attachment));
        inOrder.verify(mockMessageList).addMessage("Nice photo!", "Assistant",
                Collections.emptyList());
    }

    @Test
    public void withHistory_withAttachments_passesAttachmentsToProvider() {
        var imageData = "fake-image".getBytes();
        var attachment = new AIAttachment("photo.png", "image/png", imageData);
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Look at this", "msg-1",
                        null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Nice photo!", null,
                        null));
        var attachments = Map.of("msg-1", List.of(attachment));

        AIOrchestrator.builder(mockProvider, null)
                .withHistory(history, attachments).build();

        Mockito.verify(mockProvider).setHistory(history, attachments);
    }

    @Test
    public void withHistory_withAttachmentMap_onlyUserMessagesGetAttachments() {
        var mockUserMessage = createMockMessage();
        var mockAssistantMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockUserMessage, mockAssistantMessage);

        var attachment = new AIAttachment("file.txt", "text/plain",
                "content".getBytes());
        // Deliberately map an assistant message ID to attachments
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1", null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Hi there", "msg-2",
                        null));
        var attachments = Map.of("msg-1", List.of(attachment), "msg-2",
                List.of(attachment));

        AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withHistory(history, attachments).build();

        var inOrder = Mockito.inOrder(mockMessageList);
        inOrder.verify(mockMessageList).addMessage("Hello", "You",
                List.of(attachment));
        // Assistant message should always get empty list, even if map has entry
        inOrder.verify(mockMessageList).addMessage("Hi there", "Assistant",
                Collections.emptyList());
    }

    @Test
    public void withHistory_withEmptyAttachmentMap_usesEmptyAttachments() {
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);

        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1", null));

        AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withHistory(history, Collections.emptyMap()).build();

        Mockito.verify(mockMessageList).addMessage("Hello", "You",
                Collections.emptyList());
        Mockito.verify(mockProvider).setHistory(history,
                Collections.emptyMap());
    }

    @Test
    public void withHistory_withAttachments_nullHistoryThrows() {
        var attachments = Map.<String, List<AIAttachment>> of();
        var builder = AIOrchestrator.builder(mockProvider, null);
        Assert.assertThrows(NullPointerException.class,
                () -> builder.withHistory(null, attachments));
    }

    @Test
    public void withHistory_withAttachments_nullAttachmentMapThrows() {
        var history = List.<ChatMessage> of();
        var builder = AIOrchestrator.builder(mockProvider, null);
        Assert.assertThrows(NullPointerException.class,
                () -> builder.withHistory(history, null));
    }

    @Test
    public void withHistory_withAttachments_messageWithNoMessageId_getsEmptyAttachments() {
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);

        var attachment = new AIAttachment("file.txt", "text/plain",
                "content".getBytes());
        // User message without messageId
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", null, null));
        var attachments = Map.of("some-id", List.of(attachment));

        AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withHistory(history, attachments).build();

        Mockito.verify(mockMessageList).addMessage("Hello", "You",
                Collections.emptyList());
    }

    @Test
    public void responseCompleteListener_afterSuccessfulExchange_firesWithResponse() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var captured = new ArrayList<String>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withFileReceiver(mockFileReceiver).withInput(mockInput)
                .withResponseCompleteListener(
                        event -> captured.add(event.getResponse()))
                .build();
        orchestrator.prompt("Hello");

        Assert.assertEquals(1, captured.size());
        Assert.assertEquals("Response", captured.getFirst());
    }

    @Test
    public void responseCompleteListener_afterStreamError_doesNotFire() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.error(new RuntimeException("API Error")));

        var captured = new ArrayList<String>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withFileReceiver(mockFileReceiver).withInput(mockInput)
                .withResponseCompleteListener(
                        event -> captured.add(event.getResponse()))
                .build();
        orchestrator.prompt("Hello");

        Assert.assertTrue("Listener should not fire on error",
                captured.isEmpty());
    }

    @Test
    public void responseCompleteListener_afterEmptyResponse_doesNotFire() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.empty());

        var captured = new ArrayList<String>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withFileReceiver(mockFileReceiver).withInput(mockInput)
                .withResponseCompleteListener(
                        event -> captured.add(event.getResponse()))
                .build();
        orchestrator.prompt("Hello");

        Assert.assertTrue("Listener should not fire on empty response",
                captured.isEmpty());
    }

    @Test
    public void responseCompleteListener_receivesResponseText() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Hello", " ", "World"));

        var captured = new ArrayList<String>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withFileReceiver(mockFileReceiver).withInput(mockInput)
                .withResponseCompleteListener(
                        event -> captured.add(event.getResponse()))
                .build();
        orchestrator.prompt("Hi");

        Assert.assertEquals(1, captured.size());
        Assert.assertEquals("Hello World", captured.getFirst());
    }

    @Test
    public void responseCompleteListener_afterMultipleExchanges_firesEachTime() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response 1"))
                .thenReturn(Flux.just("Response 2"));

        var captured = new ArrayList<String>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withFileReceiver(mockFileReceiver).withInput(mockInput)
                .withResponseCompleteListener(
                        event -> captured.add(event.getResponse()))
                .build();
        orchestrator.prompt("First");
        orchestrator.prompt("Second");

        Assert.assertEquals(2, captured.size());
        Assert.assertEquals("Response 1", captured.get(0));
        Assert.assertEquals("Response 2", captured.get(1));
    }

    @Test
    public void responseCompleteListener_withRestoredHistory_doesNotFire() {
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1", null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Hi there", null,
                        null));

        var captured = new ArrayList<String>();
        AIOrchestrator.builder(mockProvider, null)
                .withResponseCompleteListener(
                        event -> captured.add(event.getResponse()))
                .withHistory(history, Collections.emptyMap()).build();

        Assert.assertTrue(
                "Listener should not fire when history is restored via withHistory()",
                captured.isEmpty());
    }

    @Test
    public void responseCompleteListener_listenerThrows_doesNotBreakStreaming() {
        mockUi();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withFileReceiver(mockFileReceiver).withInput(mockInput)
                .withResponseCompleteListener(event -> {
                    throw new RuntimeException("Listener error");
                }).build();

        // Should not throw
        orchestrator.prompt("Hello");

        // History should still be recorded
        var history = orchestrator.getHistory();
        Assert.assertEquals(2, history.size());
    }

    private AIOrchestrator getSimpleOrchestrator() {
        return AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withFileReceiver(mockFileReceiver).withInput(mockInput)
                .build();
    }

    private void prompt(String message) {
        getSimpleOrchestrator().prompt(message);
    }

    private static void mockUi() {
        mockUI = Mockito.mock(UI.class);
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

        // Mock feature flags to enable the AI components feature
        FeatureFlags mockFeatureFlags = Mockito.mock(FeatureFlags.class);
        mockFeatureFlagsStatic = Mockito.mockStatic(FeatureFlags.class);
        Mockito.when(mockFeatureFlags.isEnabled(AIOrchestrator.FEATURE_FLAG_ID))
                .thenReturn(true);

        VaadinSession mockSession = Mockito.mock(VaadinSession.class);
        VaadinService mockService = Mockito.mock(VaadinService.class);
        VaadinContext mockContext = Mockito.mock(VaadinContext.class);
        Mockito.when(mockUI.getSession()).thenReturn(mockSession);
        Mockito.when(mockSession.getService()).thenReturn(mockService);
        Mockito.when(mockService.getContext()).thenReturn(mockContext);
        mockFeatureFlagsStatic.when(() -> FeatureFlags.get(mockContext))
                .thenReturn(mockFeatureFlags);

        UI.setCurrent(mockUI);
    }

    private static AIMessage createMockMessage() {
        var message = Mockito.mock(AIMessage.class);
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

    private static AIAttachment createAttachment(String fileName) {
        return new AIAttachment(fileName, "text/plain", "test".getBytes());
    }

    @Test
    public void prompt_withFlowMessageList_scalesImageAttachmentThumbnails()
            throws Exception {
        var initialWidth = 500;
        var initialHeight = 400;
        mockUi();
        var flowMessageList = new MessageList();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var largeImageData = createTestImage(initialWidth, initialHeight);
        Mockito.when(mockFileReceiver.takeAttachments()).thenReturn(List.of(
                new AIAttachment("large.png", "image/png", largeImageData)));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(flowMessageList)
                .withFileReceiver(mockFileReceiver).build();
        orchestrator.prompt("Check this image");

        var attachment = flowMessageList.getItems().getFirst().getAttachments()
                .getFirst();
        Assert.assertTrue(
                attachment.url().startsWith("data:image/jpeg;base64,"));

        var scaledImage = decodeDataUrlToImage(attachment.url());
        // 200 is the hardcoded max size for thumbnails
        var scaleFactor = (double) 200 / Math.max(initialWidth, initialHeight);
        Assert.assertEquals((int) (scaleFactor * initialWidth),
                scaledImage.getWidth());
        Assert.assertEquals((int) (scaleFactor * initialHeight),
                scaledImage.getHeight());
    }

    @Test
    public void prompt_withFlowMessageList_smallImageNotScaled()
            throws Exception {
        var initialWidth = 100;
        var initialHeight = 80;
        mockUi();
        var flowMessageList = new MessageList();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var smallImageData = createTestImage(initialWidth, initialHeight);
        Mockito.when(mockFileReceiver.takeAttachments()).thenReturn(List.of(
                new AIAttachment("small.png", "image/png", smallImageData)));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(flowMessageList)
                .withFileReceiver(mockFileReceiver).build();
        orchestrator.prompt("Check this small image");

        var attachment = flowMessageList.getItems().getFirst().getAttachments()
                .getFirst();
        Assert.assertTrue(
                attachment.url().startsWith("data:image/png;base64,"));

        var image = decodeDataUrlToImage(attachment.url());
        Assert.assertEquals(initialWidth, image.getWidth());
        Assert.assertEquals(initialHeight, image.getHeight());
    }

    @Test
    public void prompt_withFlowMessageList_nonImageAttachmentHasNoDataUrl() {
        mockUi();
        var flowMessageList = new MessageList();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));
        Mockito.when(mockFileReceiver.takeAttachments())
                .thenReturn(List.of(new AIAttachment("document.pdf",
                        "application/pdf", "fake pdf content".getBytes())));
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(flowMessageList)
                .withFileReceiver(mockFileReceiver).build();
        orchestrator.prompt("Check this document");

        var attachment = flowMessageList.getItems().getFirst().getAttachments()
                .getFirst();
        Assert.assertEquals("document.pdf", attachment.name());
        Assert.assertEquals("application/pdf", attachment.mimeType());
        Assert.assertNull(attachment.url());
    }

    private static byte[] createTestImage(int width, int height)
            throws IOException {
        var image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        var g2d = image.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        var baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    private static BufferedImage decodeDataUrlToImage(String dataUrl)
            throws IOException {
        var base64Data = dataUrl.substring(dataUrl.indexOf(",") + 1);
        var imageBytes = Base64.getDecoder().decode(base64Data);
        return ImageIO.read(new ByteArrayInputStream(imageBytes));
    }
}
