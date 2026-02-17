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
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
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
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.ui.AIFileReceiver;
import com.vaadin.flow.component.ai.ui.AIInput;
import com.vaadin.flow.component.ai.ui.AIMessage;
import com.vaadin.flow.component.ai.ui.AIMessageList;
import com.vaadin.flow.component.ai.ui.InputSubmitEvent;
import com.vaadin.flow.component.ai.ui.InputSubmitListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.upload.UploadManager;
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
                .addSubmitListener(Mockito.any(InputSubmitListener.class));
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
    public void prompt_withStreamingResponse_updatesMessageWithTokens() {
        mockUiSync();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Hello", " ", "World"));

        prompt("Hi");

        var inOrder = Mockito.inOrder(mockMessage);
        inOrder.verify(mockMessage).appendText("Hello");
        inOrder.verify(mockMessage).appendText(" ");
        inOrder.verify(mockMessage).appendText("World");
    }

    @Test
    public void prompt_withStreamingError_setsErrorMessage() {
        mockUiSync();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.error(new RuntimeException("API Error")));

        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("Hi");

        Mockito.verify(mockMessage)
                .setText("An error occurred. Please try again.");
    }

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
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
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
    public void prompt_withMultipleTokens_appendsAllTokens() {
        mockUiSync();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Token1", "Token2", "Token3", "Token4"));

        prompt("Hello");

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
    public void builder_withErrorHandler_usesCustomMessageOnError() {
        mockUiSync();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.error(new RuntimeException("API Error")));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withErrorHandler(
                        error -> "Builder error: " + error.getMessage())
                .build();
        orchestrator.prompt("Hi");

        Mockito.verify(mockMessage).setText("Builder error: API Error");
    }

    @Test
    public void builder_withNullErrorHandler_throwsNullPointerException() {
        var builder = AIOrchestrator.builder(mockProvider, null);
        Assert.assertThrows(NullPointerException.class,
                () -> builder.withErrorHandler(null));
    }

    @Test
    public void builder_withoutErrorHandler_usesDefaultHandler() {
        mockUiSync();
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.error(new RuntimeException("API Error")));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).build();
        orchestrator.prompt("Hi");

        Mockito.verify(mockMessage)
                .setText("An error occurred. Please try again.");
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
        doMockUi(command -> {
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
        });
    }

    private static void mockUiSync() {
        doMockUi(command -> {
            command.execute();
            return null;
        });
    }

    private static void doMockUi(
            Function<Command, FutureTask<Void>> executeCommand) {
        mockUI = Mockito.mock(UI.class);
        Mockito.doAnswer(
                invocation -> executeCommand.apply(invocation.getArgument(0)))
                .when(mockUI).access(Mockito.any(Command.class));

        var mockFeatureFlags = Mockito.mock(FeatureFlags.class);
        mockFeatureFlagsStatic = Mockito.mockStatic(FeatureFlags.class);
        Mockito.when(mockFeatureFlags.isEnabled(AIOrchestrator.FEATURE_FLAG_ID))
                .thenReturn(true);

        var mockSession = Mockito.mock(VaadinSession.class);
        var mockService = Mockito.mock(VaadinService.class);
        var mockContext = Mockito.mock(VaadinContext.class);
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
