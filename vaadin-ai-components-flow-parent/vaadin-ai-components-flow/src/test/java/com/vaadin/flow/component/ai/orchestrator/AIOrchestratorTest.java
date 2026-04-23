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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.vaadin.flow.component.ai.AIComponentsFeatureFlagProvider;
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
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

import reactor.core.publisher.Flux;
import tools.jackson.databind.JsonNode;

class AIOrchestratorTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();
    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            AIComponentsFeatureFlagProvider.AI_COMPONENTS);

    private LLMProvider mockProvider;
    private AIMessageList mockMessageList;
    private AIInput mockInput;
    private AIFileReceiver mockFileReceiver;

    private final TestLogger logger = TestLoggerFactory
            .getTestLogger(AIOrchestrator.class);

    @BeforeEach
    void setup() {
        mockProvider = Mockito.mock(LLMProvider.class);
        mockMessageList = Mockito.mock(AIMessageList.class);
        mockInput = Mockito.mock(AIInput.class);
        mockFileReceiver = Mockito.mock(AIFileReceiver.class);
        Mockito.when(mockFileReceiver.takeAttachments())
                .thenReturn(Collections.emptyList());
        logger.clear();
    }

    @Test
    void builder_withNullProvider_throwsNullPointerException() {
        Assertions.assertThrows(NullPointerException.class,
                () -> AIOrchestrator.builder(null, null));
    }

    @Test
    void builder_withProvider_usesProviderForPrompts() {
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
    void builder_withSystemPrompt_includesSystemPromptInRequest() {
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
        Assertions.assertEquals(systemPrompt, captor.getValue().systemPrompt());
    }

    @Test
    void builder_withMessageList_addsMessagesToList() {
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
    void builder_withInput_addsSubmitListener() {
        AIOrchestrator.builder(mockProvider, null).withInput(mockInput).build();

        Mockito.verify(mockInput)
                .addSubmitListener(Mockito.any(SerializableConsumer.class));
    }

    @Test
    void builder_withToolObjects_setsTools() {
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
        Assertions.assertEquals(1, captor.getValue().tools().length);
        Assertions.assertSame(tool, captor.getValue().tools()[0]);
    }

    @Test
    void builder_withNullToolObjects_handlesNullGracefully() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withTools((Object[]) null).build();
        Assertions.assertNotNull(orchestrator);
    }

    @Test
    void prompt_withValidMessage_sendsRequestToProvider() {
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
        Assertions.assertEquals("Hello", captor.getValue().userMessage());
    }

    @Test
    void prompt_withNullMessage_doesNotSendRequest() {
        prompt(null);
        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void prompt_withEmptyMessage_doesNotSendRequest() {
        prompt("   ");
        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void prompt_withSystemPrompt_includesSystemPromptInRequest() {
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
        Assertions.assertEquals(systemPrompt, captor.getValue().systemPrompt());
    }

    @Test
    void prompt_withStreamingResponse_updatesMessageWithTokens()
            throws Exception {
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

        Assertions.assertTrue(latch.await(2, TimeUnit.SECONDS),
                "Tokens should be appended within timeout");

        var inOrder = Mockito.inOrder(mockMessage);
        inOrder.verify(mockMessage).appendText("Hello");
        inOrder.verify(mockMessage).appendText(" ");
        inOrder.verify(mockMessage).appendText("World");
    }

    @Test
    void prompt_withStreamingError_setsErrorMessage() throws Exception {
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

        Assertions.assertTrue(latch.await(2, TimeUnit.SECONDS),
                "Error message should be set within timeout");

        Mockito.verify(mockMessage)
                .setText("An error occurred. Please try again.");
    }

    @SuppressWarnings("unchecked")
    @Test
    void inputSubmit_triggersPromptProcessing() {
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
    void userMessage_isAddedToMessageList() {
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
    void assistantPlaceholder_isCreated() {
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
    void prompt_withTools_includesToolsInRequest() {
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
        Assertions.assertEquals(1, captor.getValue().tools().length);
        Assertions.assertEquals(tool, captor.getValue().tools()[0]);
    }

    @Test
    void prompt_withoutUIContext_throwsIllegalStateException() {
        ui.clearUI();
        var orchestrator = getSimpleOrchestrator();
        Assertions.assertThrows(IllegalStateException.class,
                () -> orchestrator.prompt("Hello"));
    }

    @Test
    void prompt_whileProcessing_isIgnored() {
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
    void prompt_withoutSystemPrompt_sendsNullSystemPromptInRequest() {
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
        Assertions.assertNull(captor.getValue().systemPrompt());
    }

    @Test
    void prompt_withWhitespaceOnlySystemPrompt_sendsNullSystemPrompt() {
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
        Assertions.assertNull(captor.getValue().systemPrompt());
    }

    @Test
    void builder_withFlowMessageList_wrapsCorrectly() {
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
    void builder_withFlowMessageInput_wrapsCorrectly() {
        var flowMessageInput = Mockito.mock(MessageInput.class);
        AIOrchestrator.builder(mockProvider, null).withInput(flowMessageInput)
                .build();

        Mockito.verify(flowMessageInput).addSubmitListener(Mockito.any());
    }

    @Test
    void builder_withFlowUpload_withExistingHandler_throws() {
        var flowUploadManager = new UploadManager(new Div(),
                UploadHandler.inMemory((x, y) -> {
                }));
        var builder = AIOrchestrator.builder(mockProvider, null);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> builder.withFileReceiver(flowUploadManager));
    }

    @Test
    void builder_withFlowUpload_withoutHandler_succeeds() {
        var flowUploadManager = new UploadManager(new Div());
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withFileReceiver(flowUploadManager).build();
        Assertions.assertNotNull(orchestrator);
    }

    @Test
    void builder_withUploadComponent_withExistingHandler_throws() {
        var upload = new Upload(UploadHandler.inMemory((x, y) -> {
        }));
        var builder = AIOrchestrator.builder(mockProvider, null);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> builder.withFileReceiver(upload));
    }

    @Test
    void builder_withUploadComponent_withoutHandler_succeeds() {
        var upload = new Upload();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withFileReceiver(upload).build();
        Assertions.assertNotNull(orchestrator);
    }

    @SuppressWarnings("unchecked")
    @Test
    void inputSubmit_withNullValue_doesNotProcess() {
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
    void inputSubmit_withEmptyValue_doesNotProcess() {
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
    void inputSubmit_whileProcessing_isIgnored() {
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
    void prompt_withAttachments_includesAttachmentsInRequest() {
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
        Assertions.assertNotNull(attachments);
        Assertions.assertEquals(2, attachments.size());
        Assertions.assertEquals("test.txt", attachments.getFirst().name());
        Assertions.assertEquals("image.png", attachments.get(1).name());
    }

    @Test
    @SuppressWarnings("unchecked")
    void prompt_withAttachments_createsMessageWithAttachments() {
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
        Assertions.assertEquals(1, aiAttachments.size());
        Assertions.assertEquals("test.txt", aiAttachments.getFirst().name());
        Assertions.assertEquals("text/plain",
                aiAttachments.getFirst().mimeType());
        Assertions.assertArrayEquals("test".getBytes(),
                aiAttachments.getFirst().data());
    }

    @Test
    void prompt_withTimeout_setsTimeoutErrorMessage() throws Exception {
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

        Assertions.assertTrue(latch.await(2, TimeUnit.SECONDS),
                "Timeout error should be set within timeout");

        Mockito.verify(mockMessage)
                .setText("Request timed out. Please try again.");
    }

    @Test
    void prompt_withoutMessageList_stillSendsToProvider() {
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        prompt("Hello");

        Mockito.verify(mockProvider)
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void prompt_withEmptyResponse_completesSuccessfully() {
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
    void prompt_withSystemPromptWithLeadingTrailingWhitespace_trimmed() {
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
        Assertions.assertEquals("You are helpful",
                captor.getValue().systemPrompt());
    }

    @Test
    void prompt_withMultipleTokens_appendsAllTokens() throws Exception {
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

        Assertions.assertTrue(latch.await(2, TimeUnit.SECONDS),
                "All tokens should be appended within timeout");

        Mockito.verify(mockMessage, Mockito.times(4))
                .appendText(Mockito.anyString());
    }

    @Test
    void prompt_requestContainsCorrectTools() {
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
        Assertions.assertEquals(2, captor.getValue().tools().length);
        Assertions.assertSame(tool1, captor.getValue().tools()[0]);
        Assertions.assertSame(tool2, captor.getValue().tools()[1]);
    }

    @Test
    void prompt_requestContainsEmptyAttachmentsList() {
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
        Assertions.assertNotNull(captor.getValue().attachments());
        Assertions.assertTrue(captor.getValue().attachments().isEmpty());
    }

    @Test
    void prompt_callsTakeAttachments() {
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
    void builder_namesNotConfigured_usesDefaultNames() {
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
    void builder_withCustomUserName_usesCustomUserName() {
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
    void builder_withCustomAssistantName_usesCustomAssistantName() {
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
    void builder_withCustomUserNameAndAssistantName_usesBothCustomNames() {
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
    void builder_withNullUserName_throws() {
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.any(), Mockito.anyList())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var builder = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList);
        Assertions.assertThrows(NullPointerException.class,
                () -> builder.withUserName(null));
    }

    @Test
    void builder_withNullAssistantName_throws() {
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.any(), Mockito.anyList())).thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var builder = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList);
        Assertions.assertThrows(NullPointerException.class,
                () -> builder.withAssistantName(null));
    }

    @Test
    void chatMessage_withMessageId_preservesMessageId() {
        var message = new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-123",
                null);
        Assertions.assertEquals(ChatMessage.Role.USER, message.role());
        Assertions.assertEquals("Hello", message.content());
        Assertions.assertEquals("msg-123", message.messageId());
    }

    @Test
    void chatMessage_withoutMessageId_setsMessageIdToNull() {
        var message = new ChatMessage(ChatMessage.Role.ASSISTANT, "Hi there",
                null, null);
        Assertions.assertEquals(ChatMessage.Role.ASSISTANT, message.role());
        Assertions.assertEquals("Hi there", message.content());
        Assertions.assertNull(message.messageId());
        Assertions.assertNull(message.time());
    }

    @Test
    void chatMessage_withTime_preservesTime() {
        var now = Instant.now();
        var message = new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1",
                now);
        Assertions.assertEquals(now, message.time());
        Assertions.assertEquals("msg-1", message.messageId());
    }

    @Test
    void getHistory_onFreshOrchestrator_returnsEmptyList() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null).build();
        Assertions.assertTrue(orchestrator.getHistory().isEmpty());
    }

    @Test
    void getHistory_afterPrompt_containsUserAndAssistantMessages() {
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
        Assertions.assertEquals(2, history.size());
        Assertions.assertEquals(ChatMessage.Role.USER,
                history.getFirst().role());
        Assertions.assertEquals("Hello", history.getFirst().content());
        Assertions.assertNotNull(history.getFirst().messageId());
        Assertions.assertEquals(ChatMessage.Role.ASSISTANT,
                history.get(1).role());
        Assertions.assertEquals("Response", history.get(1).content());
        Assertions.assertNull(history.get(1).messageId());
    }

    @Test
    void getHistory_afterPrompt_recordsTimestamps() {
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
        Assertions.assertNotNull(history.getFirst().time());
        Assertions.assertFalse(history.getFirst().time().isBefore(before));
        Assertions.assertFalse(history.getFirst().time().isAfter(after));
        Assertions.assertNotNull(history.get(1).time());
        Assertions.assertFalse(history.get(1).time().isBefore(before));
        Assertions.assertFalse(history.get(1).time().isAfter(after));
    }

    @Test
    void withHistory_withTimestamps_restoresTimesOnUIMessages() {
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
    void withHistory_withNullTimestamp_doesNotCallSetTime() {
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
    void getHistory_afterMultipleTokens_concatenatesAssistantResponse() {
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
        Assertions.assertEquals(2, history.size());
        Assertions.assertEquals("Hello World", history.get(1).content());
    }

    @Test
    void getHistory_afterStreamError_doesNotAddAssistantMessage() {
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
        Assertions.assertEquals(1, history.size());
        Assertions.assertEquals(ChatMessage.Role.USER,
                history.getFirst().role());
    }

    @Test
    void getHistory_returnsUnmodifiableCopy() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null).build();
        var history = orchestrator.getHistory();
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> history.add(new ChatMessage(ChatMessage.Role.USER, "test",
                        null, null)));
    }

    @Test
    void withHistory_withNullHistory_throwsNullPointerException() {
        var builder = AIOrchestrator.builder(mockProvider, null);
        Assertions.assertThrows(NullPointerException.class,
                () -> builder.withHistory(null, Collections.emptyMap()));
    }

    @Test
    void withHistory_restoresProviderContext() {
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
    void withHistory_restoresMessageListUI() {
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
    void withHistory_restoresConversationHistory() {
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1", null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Hi there", null,
                        null));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withHistory(history, Collections.emptyMap()).build();

        var restored = orchestrator.getHistory();
        Assertions.assertEquals(2, restored.size());
        Assertions.assertEquals("Hello", restored.getFirst().content());
        Assertions.assertEquals("msg-1", restored.getFirst().messageId());
        Assertions.assertEquals("Hi there", restored.get(1).content());
        Assertions.assertNull(restored.get(1).messageId());
    }

    @Test
    void withHistory_withCustomNames_usesCustomNamesInUI() {
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
    void withHistory_rebuildsMsgIdMapping_attachmentClickWorks() {
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

        Assertions.assertEquals(1, clickEvents.size());
        Assertions.assertEquals("msg-1", clickEvents.getFirst().getMessageId());
        Assertions.assertEquals(0, clickEvents.getFirst().getAttachmentIndex());
    }

    @Test
    void withHistory_exceedingProviderMaxMessages_preservesFullHistory() {
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
        Assertions.assertEquals(40, restored.size(),
                "Orchestrator should preserve the full history regardless "
                        + "of provider's max message window");
        Assertions.assertEquals("Question 0", restored.getFirst().content());
        Assertions.assertEquals("msg-0", restored.getFirst().messageId());
        Assertions.assertEquals("Question 19", restored.get(38).content());
    }

    @Test
    void withHistory_whenProviderThrows_buildThrows() {
        Mockito.doThrow(new UnsupportedOperationException("Not supported"))
                .when(mockProvider)
                .setHistory(Mockito.anyList(), Mockito.anyMap());

        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1", null));

        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> AIOrchestrator.builder(mockProvider, null)
                        .withHistory(history, Collections.emptyMap()).build());
    }

    @Test
    void withHistory_withoutMessageList_onlyRestoresProvider() {
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1", null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Hi there", null,
                        null));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withHistory(history, Collections.emptyMap()).build();

        Mockito.verify(mockProvider).setHistory(history,
                Collections.emptyMap());
        var restored = orchestrator.getHistory();
        Assertions.assertEquals(2, restored.size());
    }

    @Test
    void withHistory_withAttachments_restoresAttachmentsInUI() {
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
    void withHistory_withAttachments_passesAttachmentsToProvider() {
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
    void withHistory_withAttachmentMap_onlyUserMessagesGetAttachments() {
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
    void withHistory_withEmptyAttachmentMap_usesEmptyAttachments() {
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
    void withHistory_withAttachments_nullHistoryThrows() {
        var attachments = Map.<String, List<AIAttachment>> of();
        var builder = AIOrchestrator.builder(mockProvider, null);
        Assertions.assertThrows(NullPointerException.class,
                () -> builder.withHistory(null, attachments));
    }

    @Test
    void withHistory_withAttachments_nullAttachmentMapThrows() {
        var history = List.<ChatMessage> of();
        var builder = AIOrchestrator.builder(mockProvider, null);
        Assertions.assertThrows(NullPointerException.class,
                () -> builder.withHistory(history, null));
    }

    @Test
    void withHistory_withAttachments_messageWithNoMessageId_getsEmptyAttachments() {
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
    void prompt_withFlowMessageList_scalesImageAttachmentThumbnails()
            throws Exception {
        var initialWidth = 500;
        var initialHeight = 400;
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
        Assertions.assertTrue(
                attachment.url().startsWith("data:image/jpeg;base64,"));

        var scaledImage = decodeDataUrlToImage(attachment.url());
        // 200 is the hardcoded max size for thumbnails
        var scaleFactor = (double) 200 / Math.max(initialWidth, initialHeight);
        Assertions.assertEquals((int) (scaleFactor * initialWidth),
                scaledImage.getWidth());
        Assertions.assertEquals((int) (scaleFactor * initialHeight),
                scaledImage.getHeight());
    }

    @Test
    void prompt_withFlowMessageList_smallImageNotScaled() throws Exception {
        var initialWidth = 100;
        var initialHeight = 80;
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
        Assertions.assertTrue(
                attachment.url().startsWith("data:image/png;base64,"));

        var image = decodeDataUrlToImage(attachment.url());
        Assertions.assertEquals(initialWidth, image.getWidth());
        Assertions.assertEquals(initialHeight, image.getHeight());
    }

    @Test
    void prompt_withFlowMessageList_nonImageAttachmentHasNoDataUrl() {
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
        Assertions.assertEquals("document.pdf", attachment.name());
        Assertions.assertEquals("application/pdf", attachment.mimeType());
        Assertions.assertNull(attachment.url());
    }

    @Test
    void responseCompleteListener_afterSuccessfulExchange_firesWithResponse() {
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

        Assertions.assertEquals(1, captured.size());
        Assertions.assertEquals("Response", captured.getFirst());
    }

    @Test
    void responseCompleteListener_afterStreamError_doesNotFire() {
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

        Assertions.assertTrue(captured.isEmpty(),
                "Listener should not fire on error");
    }

    @Test
    void responseCompleteListener_afterEmptyResponse_doesNotFire() {
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

        Assertions.assertTrue(captured.isEmpty(),
                "Listener should not fire on empty response");
    }

    @Test
    void responseCompleteListener_receivesResponseText() {
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

        Assertions.assertEquals(1, captured.size());
        Assertions.assertEquals("Hello World", captured.getFirst());
    }

    @Test
    void responseCompleteListener_afterMultipleExchanges_firesEachTime() {
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

        Assertions.assertEquals(2, captured.size());
        Assertions.assertEquals("Response 1", captured.get(0));
        Assertions.assertEquals("Response 2", captured.get(1));
    }

    @Test
    void responseCompleteListener_withRestoredHistory_doesNotFire() {
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1", null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Hi there", null,
                        null));

        var captured = new ArrayList<String>();
        AIOrchestrator.builder(mockProvider, null)
                .withResponseCompleteListener(
                        event -> captured.add(event.getResponse()))
                .withHistory(history, Collections.emptyMap()).build();

        Assertions.assertTrue(captured.isEmpty(),
                "Listener should not fire when history is restored via withHistory()");
    }

    @Test
    void responseCompleteListener_listenerThrows_doesNotBreakStreaming() {
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
        Assertions.assertEquals(2, history.size());
    }

    // --- AIController tests ---

    @Test
    void builder_withNullController_throwsNullPointerException() {
        Assertions.assertThrows(NullPointerException.class, () -> AIOrchestrator
                .builder(mockProvider, null).withController(null));
    }

    @Test
    void builder_withController_collectsToolsForRequest() {
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var tool1 = createToolSpec("tool1", "First tool");
        var tool2 = createToolSpec("tool2", "Second tool");
        var controller = createController(tool1, tool2);

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withController(controller)
                .build();
        orchestrator.prompt("Hello");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        var explicitTools = captor.getValue().explicitTools();
        Assertions.assertEquals(2, explicitTools.size());
        Assertions.assertEquals("tool1", explicitTools.get(0).getName());
        Assertions.assertEquals("tool2", explicitTools.get(1).getName());
    }

    @Test
    void builder_withController_callsOnRequestCompleted() {
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response text"));

        var callCount = new AtomicInteger();
        AIController controller = new AIController() {
            @Override
            public List<LLMProvider.ToolSpec> getTools() {
                return List.of();
            }

            @Override
            public void onRequestCompleted() {
                callCount.incrementAndGet();
            }
        };

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withController(controller)
                .build();
        orchestrator.prompt("Hello");

        Assertions.assertEquals(1, callCount.get());
    }

    @Test
    void builder_withControllerOnRequestCompletedThrows_showsErrorMessage()
            throws InterruptedException {
        var mockMessage = createMockMessage();
        var errorText = "An error occurred. Please try again.";
        var latch = new CountDownLatch(1);
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList())).thenAnswer(inv -> {
                    if (errorText.equals(inv.getArgument(0))) {
                        latch.countDown();
                    }
                    return mockMessage;
                });
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        AIController throwingController = new AIController() {
            @Override
            public List<LLMProvider.ToolSpec> getTools() {
                return List.of();
            }

            @Override
            public void onRequestCompleted() {
                throw new RuntimeException("Controller error");
            }
        };

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withController(throwingController).build();

        // Should not throw
        orchestrator.prompt("Hello");

        Assertions.assertTrue(latch.await(2, TimeUnit.SECONDS),
                "Error message should be appended within timeout");
        Mockito.verify(mockMessageList).addMessage(Mockito.eq(errorText),
                Mockito.anyString(), Mockito.anyList());

        // History records what the LLM actually said. The render failure
        // is surfaced as a separate UI message, not by rewriting the
        // assistant entry: the LLM's response is already committed to
        // the provider's chat memory, and overwriting only our local
        // history would misrepresent what the LLM produced.
        var history = orchestrator.getHistory();
        Assertions.assertEquals(2, history.size());
        var lastAssistant = history.get(history.size() - 1);
        Assertions.assertEquals(ChatMessage.Role.ASSISTANT,
                lastAssistant.role());
        Assertions.assertEquals("Response", lastAssistant.content());
    }

    @Test
    void builder_withController_onRequestCompletedNotCalledOnError() {
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.error(new RuntimeException("API Error")));

        var callCount = new AtomicInteger();
        AIController controller = new AIController() {
            @Override
            public List<LLMProvider.ToolSpec> getTools() {
                return List.of();
            }

            @Override
            public void onRequestCompleted() {
                callCount.incrementAndGet();
            }
        };

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withController(controller)
                .build();
        orchestrator.prompt("Hello");

        Assertions.assertEquals(0, callCount.get(),
                "onRequestCompleted should not be called on error");
    }

    @Test
    void builder_withControllerAndResponseCompleteListener_bothCalled() {
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var listenerCapture = new ArrayList<String>();
        var controllerCallCount = new AtomicInteger();
        AIController controller = new AIController() {
            @Override
            public List<LLMProvider.ToolSpec> getTools() {
                return List.of();
            }

            @Override
            public void onRequestCompleted() {
                controllerCallCount.incrementAndGet();
            }
        };

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withController(controller)
                .withResponseCompleteListener(
                        event -> listenerCapture.add(event.getResponse()))
                .build();
        orchestrator.prompt("Hello");

        Assertions.assertEquals(1, listenerCapture.size());
        Assertions.assertEquals("Response", listenerCapture.getFirst());
        Assertions.assertEquals(1, controllerCallCount.get());
    }

    @Test
    void builder_withNoControllers_explicitToolsIsEmpty() {
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
        Assertions.assertTrue(captor.getValue().explicitTools().isEmpty());
    }

    @Test
    void prompt_withDuplicateExplicitToolNames_logsWarning() {
        var tool1 = createToolSpec("sameName", "First tool");
        var tool2 = createToolSpec("sameName", "Second tool");
        AIController controller = createController(tool1, tool2);

        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withController(controller)
                .build();

        orchestrator.prompt("Hello");

        var warning = logger.getLoggingEvents().stream()
                .filter(event -> event.getMessage().equals(
                        "Duplicate tool name '{}': previous tool will be replaced"))
                .findFirst();

        Assertions.assertTrue(warning.isPresent(),
                "Expected duplicate tool name warning");
    }

    @Test
    void builder_withNullToolName_throwsIllegalArgumentException() {
        var controller = createController(createToolSpec(null, "A tool"));

        var exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> AIOrchestrator.builder(mockProvider, null)
                        .withController(controller));
        Assertions.assertEquals("Tool name must not be null or empty.",
                exception.getMessage());
    }

    @Test
    void builder_withEmptyToolName_throwsIllegalArgumentException() {
        var controller = createController(createToolSpec("", "A tool"));

        var exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> AIOrchestrator.builder(mockProvider, null)
                        .withController(controller));
        Assertions.assertEquals("Tool name must not be null or empty.",
                exception.getMessage());
    }

    @Test
    void builder_withInvalidToolNameContainingSpaces_throwsIllegalArgumentException() {
        var controller = createController(createToolSpec("my tool", "A tool"));

        var exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> AIOrchestrator.builder(mockProvider, null)
                        .withController(controller));
        Assertions.assertTrue(exception.getMessage().contains("'my tool'"),
                "Exception should mention the invalid tool name");
    }

    @Test
    void builder_withInvalidToolNameContainingSpecialChars_throwsIllegalArgumentException() {
        var controller = createController(
                createToolSpec("tool@name!", "A tool"));

        var exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> AIOrchestrator.builder(mockProvider, null)
                        .withController(controller));
        Assertions.assertTrue(exception.getMessage().contains("'tool@name!'"),
                "Exception should mention the invalid tool name");
    }

    @Test
    void builder_withToolNameExceeding64Characters_throwsIllegalArgumentException() {
        var controller = createController(
                createToolSpec("a".repeat(65), "A tool"));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> AIOrchestrator.builder(mockProvider, null)
                        .withController(controller));
    }

    @Test
    void builder_withToolNameExactly64Characters_doesNotThrow() {
        var controller = createController(
                createToolSpec("a".repeat(64), "A tool"));

        Assertions.assertDoesNotThrow(() -> AIOrchestrator
                .builder(mockProvider, null).withController(controller));
    }

    @Test
    void builder_withValidToolNameContainingUnderscoresAndHyphens_doesNotThrow() {
        var controller = createController(
                createToolSpec("my_tool-Name123", "A tool"));

        Assertions.assertDoesNotThrow(() -> AIOrchestrator
                .builder(mockProvider, null).withController(controller));
    }

    @Test
    void builder_withInvalidToolNameContainingDots_throwsIllegalArgumentException() {
        var controller = createController(
                createToolSpec("my.tool.name", "A tool"));

        var exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> AIOrchestrator.builder(mockProvider, null)
                        .withController(controller));
        Assertions.assertTrue(exception.getMessage().contains("'my.tool.name'"),
                "Exception should mention the invalid tool name");
    }

    @Test
    void reconnect_withInvalidToolName_throwsIllegalArgumentException()
            throws Exception {
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).build();

        // Simulate deserialization: null out the transient provider field
        var providerField = AIOrchestrator.class.getDeclaredField("provider");
        providerField.setAccessible(true);
        providerField.set(orchestrator, null);

        var controller = createController(
                createToolSpec("invalid tool!", "A tool"));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> orchestrator.reconnect(mockProvider)
                        .withController(controller));
    }

    @Test
    void builder_withControllerCalledTwice_logsWarning() {
        var orchestratorBuilder = AIOrchestrator.builder(mockProvider, null)
                .withController(createController());
        assertNoBuilderWarning();
        orchestratorBuilder.withController(createController()).build();
        assertBuilderWarning("controller");
    }

    @Test
    void builder_withMessageListCalledTwice_logsWarning() {
        var orchestratorBuilder = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList);
        assertNoBuilderWarning();
        orchestratorBuilder.withMessageList(mockMessageList).build();
        assertBuilderWarning("messageList");
    }

    @Test
    void builder_withInputCalledTwice_logsWarning() {
        var orchestratorBuilder = AIOrchestrator.builder(mockProvider, null)
                .withInput(mockInput);
        assertNoBuilderWarning();
        orchestratorBuilder.withInput(mockInput).build();
        assertBuilderWarning("input");
    }

    @Test
    void builder_withToolsCalledTwice_logsWarning() {
        var orchestratorBuilder = AIOrchestrator.builder(mockProvider, null)
                .withTools(new SampleTool());
        assertNoBuilderWarning();
        orchestratorBuilder.withTools(new SampleTool()).build();
        assertBuilderWarning("tools");
    }

    @Test
    void builder_withUserNameCalledTwice_logsWarning() {
        var orchestratorBuilder = AIOrchestrator.builder(mockProvider, null)
                .withUserName("A");
        assertNoBuilderWarning();
        orchestratorBuilder.withUserName("B").build();
        assertBuilderWarning("userName");
    }

    @Test
    void builder_withAssistantNameCalledTwice_logsWarning() {
        var orchestratorBuilder = AIOrchestrator.builder(mockProvider, null)
                .withAssistantName("A");
        assertNoBuilderWarning();
        orchestratorBuilder.withAssistantName("B").build();
        assertBuilderWarning("assistantName");
    }

    @Test
    void builder_withResponseCompleteListenerCalledTwice_logsWarning() {
        var orchestratorBuilder = AIOrchestrator.builder(mockProvider, null)
                .withResponseCompleteListener(event -> {
                });
        assertNoBuilderWarning();
        orchestratorBuilder.withResponseCompleteListener(event -> {
        }).build();
        assertBuilderWarning("responseCompleteListener");
    }

    @Test
    void builder_claimsAllResources_toPreventSharing() throws Exception {
        // Builder with-methods that do NOT configure a shareable resource —
        // value types, listeners, tools, and restored conversation state. Any
        // other with-method is treated as configuring a resource that build()
        // must claim. If you add a new resource with-method (component,
        // controller, ...), do not add it here — ensure build() claims it.
        Set<String> nonResourceSetters = Set.of("withTools", "withUserName",
                "withAssistantName", "withAttachmentSubmitListener",
                "withAttachmentClickListener", "withResponseCompleteListener",
                "withHistory");

        // Provider is set via the factory method, not a with-method.
        assertClaimed(null, LLMProvider.class);

        for (Method method : AIOrchestrator.Builder.class
                .getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers())
                    || !method.getName().startsWith("with")
                    || nonResourceSetters.contains(method.getName())) {
                continue;
            }
            Assertions.assertEquals(1, method.getParameterCount(),
                    "Resource with-method must take a single argument: "
                            + method.getName());
            assertClaimed(method, method.getParameterTypes()[0]);
        }
    }

    private static void assertClaimed(Method setter, Class<?> resourceType)
            throws Exception {
        var shared = Mockito.mock(resourceType);
        buildWith(setter, shared);
        Assertions.assertThrows(IllegalStateException.class,
                () -> buildWith(setter, shared),
                "Builder must claim the " + resourceType.getSimpleName()
                        + " passed to "
                        + (setter == null ? "builder()" : setter.getName())
                        + " to prevent sharing between orchestrators");
    }

    private static AIOrchestrator buildWith(Method setter, Object shared)
            throws Exception {
        if (setter == null) {
            return AIOrchestrator.builder((LLMProvider) shared, null).build();
        }
        var builder = AIOrchestrator.builder(Mockito.mock(LLMProvider.class),
                null);
        setter.invoke(builder, shared);
        return builder.build();
    }

    @Test
    void builder_buildFailsAfterClaim_resourceCanBeReused() {
        var input = Mockito.mock(AIInput.class);
        Mockito.doThrow(new RuntimeException("simulated build failure"))
                .doNothing().when(input).addSubmitListener(Mockito.any());

        Assertions.assertThrows(RuntimeException.class,
                () -> AIOrchestrator
                        .builder(Mockito.mock(LLMProvider.class), null)
                        .withInput(input).build());

        Assertions.assertDoesNotThrow(() -> AIOrchestrator
                .builder(Mockito.mock(LLMProvider.class), null).withInput(input)
                .build());
    }

    @Test
    void builder_withAlreadyClaimedResource_doesNotApplySideEffects() {
        var sharedInput = Mockito.mock(AIInput.class);
        AIOrchestrator.builder(Mockito.mock(LLMProvider.class), null)
                .withInput(sharedInput).build();

        Assertions.assertThrows(IllegalStateException.class,
                () -> AIOrchestrator
                        .builder(Mockito.mock(LLMProvider.class), null)
                        .withInput(sharedInput).build());

        Mockito.verify(sharedInput, Mockito.times(1))
                .addSubmitListener(Mockito.any());
    }

    @Test
    void builder_withNullSystemPrompt_logsWarning() {
        AIOrchestrator.builder(mockProvider, null).build();
        assertSystemPromptWarning(true);
    }

    @Test
    void builder_withBlankSystemPrompt_logsWarning() {
        AIOrchestrator.builder(mockProvider, "   ").build();
        assertSystemPromptWarning(true);
    }

    @Test
    void builder_withEmptySystemPrompt_logsWarning() {
        AIOrchestrator.builder(mockProvider, "").build();
        assertSystemPromptWarning(true);
    }

    @Test
    void builder_withNonBlankSystemPrompt_doesNotLogWarning() {
        AIOrchestrator.builder(mockProvider, "You are a helpful assistant")
                .build();
        assertSystemPromptWarning(false);
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

    private void assertBuilderWarning(String fieldName) {
        var warning = logger.getLoggingEvents().stream().filter(
                e -> e.getMessage().contains("was already set on the builder"))
                .findFirst();
        Assertions.assertTrue(warning.isPresent(),
                "Expected warning for " + fieldName);
    }

    private void assertSystemPromptWarning(boolean warningExpected) {
        var warning = logger.getLoggingEvents().stream().filter(
                e -> e.getMessage().contains("No system prompt was provided"))
                .findAny();
        Assertions.assertEquals(warningExpected, warning.isPresent());
    }

    private void assertNoBuilderWarning() {
        Assertions
                .assertFalse(
                        logger.getLoggingEvents().stream()
                                .anyMatch(e -> e.getMessage()
                                        .contains("was already set")),
                        "No warning expected for single call");
    }

    private static AIController createController(
            LLMProvider.ToolSpec... tools) {
        return new AIController() {
            @Override
            public List<LLMProvider.ToolSpec> getTools() {
                return List.of(tools);
            }

            @Override
            public void onRequestCompleted() {
                // no-op
            }
        };
    }

    private static LLMProvider.ToolSpec createToolSpec(String name,
            String description) {
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
                return null;
            }

            @Override
            public String execute(JsonNode arguments) {
                return "result";
            }
        };
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
}
