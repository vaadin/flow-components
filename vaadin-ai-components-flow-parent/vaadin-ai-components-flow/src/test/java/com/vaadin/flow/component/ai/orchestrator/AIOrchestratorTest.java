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
import java.util.concurrent.atomic.AtomicReference;
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
    void prompt_attachmentSubmitListenerException_orchestratorRecoversForNextPrompt() {
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));
        var attachment = new AIAttachment("file.txt", "text/plain",
                "data".getBytes());
        Mockito.when(mockFileReceiver.takeAttachments())
                .thenReturn(List.of(attachment));

        var attempts = new AtomicInteger();
        RequestListener listener = event -> {
            if (attempts.incrementAndGet() == 1) {
                throw new RuntimeException("storage transiently unavailable");
            }
        };

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withFileReceiver(mockFileReceiver)
                .withRequestListener(listener).build();

        Assertions.assertThrows(RuntimeException.class,
                () -> orchestrator.prompt("First"));

        orchestrator.prompt("Second");

        Mockito.verify(mockProvider, Mockito.times(1))
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void prompt_toolOnlyResponse_firesControllerOnResponseComplete() {
        // Tool-only turns produce empty text, but the controller still
        // needs the lifecycle hook to flush its pending state (e.g. a
        // ChartAIController applies its pending query in onResponseComplete
        // and would otherwise never render).
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        var controller = Mockito.mock(AIController.class);
        var fakeTool = Mockito.mock(LLMProvider.ToolSpec.class);
        Mockito.when(fakeTool.getName()).thenReturn("fake_tool");
        Mockito.when(controller.getTools()).thenReturn(List.of(fakeTool));
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenAnswer(inv -> {
                    var req = (LLMProvider.LLMRequest) inv.getArgument(0);
                    req.explicitTools().get(0).execute(null);
                    return Flux.empty();
                });

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withController(controller)
                .withMetadata(null).build();
        orchestrator.prompt("do it");

        Mockito.verify(fakeTool).execute(Mockito.any());
        Mockito.verify(controller).onResponse(null);
    }

    @Test
    void prompt_emptyResponse_firesUserListenerWithEmptyText() {
        // Empty-text turns are still successful exchanges (tool-only,
        // content filter, deliberate silence). The listener fires with an
        // empty response so apps can observe every completion. The history
        // stays gated to avoid polluting it with empty assistant messages.
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        var listener = Mockito.mock(ResponseListener.class);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.empty());

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withResponseListener(listener)
                .build();
        orchestrator.prompt("hi");

        var captor = ArgumentCaptor
                .forClass(ResponseListener.ResponseEvent.class);
        Mockito.verify(listener).onResponse(captor.capture());
        Assertions.assertEquals("", captor.getValue().getResponse());
        Assertions.assertTrue(orchestrator.getHistory().stream()
                .noneMatch(msg -> msg.role() == ChatMessage.Role.ASSISTANT));
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
    void prompt_withExplicitAttachments_includesAttachmentsInRequest() {
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("Hello",
                List.of(createAttachment("a.txt"), createAttachment("b.png")));

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        var attachments = captor.getValue().attachments();
        Assertions.assertEquals(2, attachments.size());
        Assertions.assertEquals("a.txt", attachments.getFirst().name());
        Assertions.assertEquals("b.png", attachments.get(1).name());
    }

    @Test
    @SuppressWarnings("unchecked")
    void prompt_withExplicitAttachments_rendersInUserMessageList() {
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));
        var attachment = createAttachment("a.txt");

        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("Hello", List.of(attachment));

        var attachmentsCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(mockMessageList).addMessage(Mockito.eq("Hello"),
                Mockito.eq("You"), attachmentsCaptor.capture());
        var rendered = (List<AIAttachment>) attachmentsCaptor.getValue();
        Assertions.assertEquals(1, rendered.size());
        Assertions.assertEquals("a.txt", rendered.getFirst().name());
    }

    @Test
    void prompt_withExplicitAttachments_firesRequestListenerWithAttachments() {
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));
        var receivedEvent = new AtomicReference<RequestListener.RequestEvent>();

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withRequestListener(receivedEvent::set).build();
        orchestrator.prompt("Hello", List.of(createAttachment("a.txt")));

        var event = receivedEvent.get();
        Assertions.assertNotNull(event);
        Assertions.assertEquals("Hello", event.getUserMessage());
        Assertions.assertNotNull(event.getMessageId());
        Assertions.assertEquals(1, event.getAttachments().size());
        Assertions.assertEquals("a.txt",
                event.getAttachments().getFirst().name());
    }

    @Test
    void prompt_withExplicitAttachments_doesNotDrainFileReceiver() {
        // The two-arg overload must leave the configured receiver alone so
        // any pending UI uploads stay buffered for the next prompt(String).
        // Pins the "Replace (don't drain)" semantic from the JavaDoc.
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("Hello", List.of(createAttachment("a.txt")));

        Mockito.verify(mockFileReceiver, Mockito.never()).takeAttachments();
    }

    @Test
    void prompt_withExplicitAttachments_ignoresPendingReceiverAttachments() {
        // Even if the receiver has files buffered, the explicit list wins
        // and the receiver's content does not leak into this turn.
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));
        Mockito.when(mockFileReceiver.takeAttachments())
                .thenReturn(List.of(createAttachment("from-receiver.txt")));

        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("Hello", List.of(createAttachment("explicit.txt")));

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        var attachments = captor.getValue().attachments();
        Assertions.assertEquals(1, attachments.size());
        Assertions.assertEquals("explicit.txt", attachments.getFirst().name());
    }

    @Test
    void prompt_withExplicitAttachments_nullListThrowsNullPointerException() {
        var orchestrator = getSimpleOrchestrator();

        Assertions.assertThrows(NullPointerException.class,
                () -> orchestrator.prompt("Hello", null));
        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void prompt_withExplicitAttachments_nullElementThrowsNullPointerException() {
        var listWithNull = new ArrayList<AIAttachment>();
        listWithNull.add(null);
        var orchestrator = getSimpleOrchestrator();

        Assertions.assertThrows(NullPointerException.class,
                () -> orchestrator.prompt("Hello", listWithNull));
        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void prompt_withExplicitAttachments_emptyListSendsNoAttachments() {
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("Hello", List.of());

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        Assertions.assertTrue(captor.getValue().attachments().isEmpty());
        // Empty explicit list is still an explicit list, so we still don't
        // fall back to draining the receiver.
        Mockito.verify(mockFileReceiver, Mockito.never()).takeAttachments();
    }

    @Test
    void prompt_withExplicitAttachments_nullUserMessageIsIgnored() {
        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt(null, List.of(createAttachment("a.txt")));

        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void prompt_withExplicitAttachments_blankUserMessageIsIgnored() {
        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("   ", List.of(createAttachment("a.txt")));

        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void prompt_withExplicitAttachments_listIsCopiedDefensively() {
        // Without a defensive copy, the caller mutating their list during
        // the streamed response would silently desync the LLMRequest, the
        // user message list, and the history snapshot. Pin that mutations
        // after prompt() can't affect what the provider sees.
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));
        var mutable = new ArrayList<AIAttachment>();
        mutable.add(createAttachment("a.txt"));

        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("Hello", mutable);
        mutable.add(createAttachment("b.txt"));
        mutable.clear();

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        var attachments = captor.getValue().attachments();
        Assertions.assertEquals(1, attachments.size());
        Assertions.assertEquals("a.txt", attachments.getFirst().name());
    }

    @Test
    void prompt_withExplicitAttachments_addsUserMessageToHistory() {
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = getSimpleOrchestrator();
        orchestrator.prompt("Hello", List.of(createAttachment("a.txt")));

        var history = orchestrator.getHistory();
        Assertions.assertFalse(history.isEmpty());
        var user = history.getFirst();
        Assertions.assertEquals(ChatMessage.Role.USER, user.role());
        Assertions.assertEquals("Hello", user.content());
        Assertions.assertNotNull(user.messageId());
    }

    @Test
    void prompt_withExplicitAttachments_listenerAndHistoryShareMessageId() {
        // The request listener and the conversation history record the
        // turn under the same messageId — that's the correlation handle
        // external storage (e.g. attachment maps keyed by messageId) relies
        // on, so it can't drift between the two surfaces.
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));
        var receivedEvent = new AtomicReference<RequestListener.RequestEvent>();

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withRequestListener(receivedEvent::set).build();
        orchestrator.prompt("Hello", List.of(createAttachment("a.txt")));

        Assertions.assertEquals(receivedEvent.get().getMessageId(),
                orchestrator.getHistory().getFirst().messageId());
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
                .withResponseListener(
                        event -> captured.add(event.getResponse()))
                .build();
        orchestrator.prompt("Hello");

        Assertions.assertEquals(1, captured.size());
        Assertions.assertEquals("Response", captured.getFirst());
    }

    @Test
    void responseListener_afterStreamError_firesWithErrorAndEmptyResponse() {
        // ResponseListener fires once per turn — on success and on failure.
        // On failure event.getError() carries the cause and event.getResponse()
        // is the partial (possibly empty) stream collected before the error.
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        var streamError = new RuntimeException("API Error");
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.error(streamError));

        var capturedEvent = new AtomicReference<ResponseListener.ResponseEvent>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withFileReceiver(mockFileReceiver).withInput(mockInput)
                .withResponseListener(capturedEvent::set).build();
        orchestrator.prompt("Hello");

        Assertions.assertNotNull(capturedEvent.get(),
                "Listener must fire on error");
        Assertions.assertEquals("", capturedEvent.get().getResponse(),
                "No partial stream was emitted before the error");
        Assertions.assertSame(streamError,
                capturedEvent.get().getError().orElse(null),
                "Listener must receive the stream error verbatim");
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
                .withResponseListener(
                        event -> captured.add(event.getResponse()))
                .build();
        orchestrator.prompt("Hi");

        Assertions.assertEquals(1, captured.size());
        Assertions.assertEquals("Hello World", captured.getFirst());
    }

    @Test
    void responseListener_onSuccess_carriesEmptyErrorOptional() {
        // The error optional must be empty on the success path; the listener
        // distinguishes success from failure via getError().isPresent().
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("ok"));

        var capturedEvent = new AtomicReference<ResponseListener.ResponseEvent>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withResponseListener(capturedEvent::set).build();
        orchestrator.prompt("Hi");

        Assertions.assertNotNull(capturedEvent.get());
        Assertions.assertEquals("ok", capturedEvent.get().getResponse());
        Assertions.assertTrue(capturedEvent.get().getError().isEmpty(),
                "Success path must carry an empty error optional, got: "
                        + capturedEvent.get().getError());
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
                .withResponseListener(
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
                .withResponseListener(
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
                .withResponseListener(event -> {
                    throw new RuntimeException("Listener error");
                }).build();

        orchestrator.prompt("Hello");

        Assertions.assertEquals(2, orchestrator.getHistory().size());
    }

    @Test
    void onFailurePath_responseListenerThrow_stillFiresControllerOnResponse() {
        stubAddMessage();
        var streamError = new RuntimeException("API died");
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.error(streamError));

        var controller = mockController();
        AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withController(controller)
                .withResponseListener(event -> {
                    throw new RuntimeException("listener died");
                }).build().prompt("Hello");

        Mockito.verify(controller).onResponse(streamError);
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
                .withMetadata(null).build();
        orchestrator.prompt("Hello");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        var explicitTools = captor.getValue().explicitTools();
        Assertions.assertEquals(2, explicitTools.size());
        Assertions.assertEquals("tool1", explicitTools.get(0).getName());
        Assertions.assertEquals("tool2", explicitTools.get(1).getName());
    }

    @Test
    void builder_withController_callsOnResponseComplete() {
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
            public void onResponse(Throwable error) {
                if (error != null) {
                    return;
                }
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
    void builder_withControllerOnResponseCompleteThrows_showsErrorMessage()
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
            public void onResponse(Throwable error) {
                if (error != null) {
                    return;
                }
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
    void builder_withController_onResponseFiresWithErrorOnError() {
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
            public void onResponse(Throwable error) {
                if (error != null) {
                    return;
                }
                callCount.incrementAndGet();
            }
        };

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withController(controller)
                .build();
        orchestrator.prompt("Hello");

        Assertions.assertEquals(0, callCount.get(),
                "onResponseComplete should not be called on error");
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
            public void onResponse(Throwable error) {
                if (error != null) {
                    return;
                }
                controllerCallCount.incrementAndGet();
            }
        };

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withController(controller)
                .withResponseListener(
                        event -> listenerCapture.add(event.getResponse()))
                .build();
        orchestrator.prompt("Hello");

        Assertions.assertEquals(1, listenerCapture.size());
        Assertions.assertEquals("Response", listenerCapture.getFirst());
        Assertions.assertEquals(1, controllerCallCount.get());
    }

    @Test
    void builder_withController_callsOnRequestStartBeforeStream() {
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var controller = mockController();
        orchestratorWith(controller).prompt("Hello");

        var inOrder = Mockito.inOrder(controller, mockProvider);
        inOrder.verify(controller).onRequest();
        inOrder.verify(mockProvider)
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void inputSubmit_callsOnRequestStartBeforeStream() {
        // The input submit path must reach doPrompt the same way prompt(...)
        // does — both go through processUserInput.
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var controller = mockController();
        AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withInput(mockInput)
                .withController(controller).build();

        var captor = ArgumentCaptor.forClass(SerializableConsumer.class);
        Mockito.verify(mockInput).addSubmitListener(captor.capture());
        captor.getValue().accept("Hi from input");

        var inOrder = Mockito.inOrder(controller, mockProvider);
        inOrder.verify(controller).onRequest();
        inOrder.verify(mockProvider)
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void builder_withController_firesOnResponseWithErrorOnGenericStreamError() {
        stubAddMessage();
        var thrown = new RuntimeException("API died");
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.error(thrown));

        var controller = mockController();
        orchestratorWith(controller).prompt("Hello");

        Mockito.verify(controller).onResponse(thrown);
        Mockito.verify(controller, Mockito.never()).onResponse(null);
    }

    @Test
    void builder_withController_firesOnResponseWithErrorOnTimeoutException() {
        stubAddMessage();
        var timeout = new TimeoutException("simulated");
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.error(timeout));

        var controller = mockController();
        orchestratorWith(controller).prompt("Hello");

        Mockito.verify(controller).onResponse(timeout);
        Mockito.verify(controller, Mockito.never()).onResponse(null);
    }

    @Test
    void builder_withController_onRequestThrows_firesOnResponseAndSkipsStream() {
        stubAddMessage();
        var thrown = new RuntimeException("controller refused");
        var controller = mockController();
        Mockito.doThrow(thrown).when(controller).onRequest();

        var orchestrator = orchestratorWith(controller);
        var caught = Assertions.assertThrows(RuntimeException.class,
                () -> orchestrator.prompt("Hello"));
        Assertions.assertSame(thrown, caught);

        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
        Mockito.verify(controller).onResponse(thrown);
        Mockito.verify(controller, Mockito.never()).onResponse(null);
    }

    @Test
    void streamError_setsErrorMessageOnAssistantPlaceholderExactlyOnce() {
        // Async errors and the pre-stream catch target the same text; the
        // two paths must not stack.
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.error(new RuntimeException("API died")));

        orchestratorWith(mockController()).prompt("Hello");

        Mockito.verify(mockMessage, Mockito.times(1))
                .setText("An error occurred. Please try again.");
    }

    @Test
    void attachmentListenerThrows_doesNotCommitToHistory() {
        // Listener fires in the commit phase before history.add — a
        // listener throw must not leave the user message in history.
        stubAddMessage();
        Mockito.when(mockFileReceiver.takeAttachments())
                .thenReturn(List.of(createAttachment("a.txt")));

        RequestListener listener = event -> {
            throw new RuntimeException("listener failed");
        };

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withFileReceiver(mockFileReceiver)
                .withRequestListener(listener).build();

        Assertions.assertThrows(RuntimeException.class,
                () -> orchestrator.prompt("Hello"));

        Assertions.assertTrue(orchestrator.getHistory().isEmpty(),
                "Attachment listener throw must not commit user message to history");
    }

    @Test
    void preStreamThrow_withoutMessageList_doesNotCrash() {
        // No messageList means no assistant placeholder; the catch block
        // must skip the setText update without an NPE.
        var controller = mockController();
        Mockito.doThrow(new RuntimeException("controller refused"))
                .when(controller).onRequest();

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withController(controller).build();

        var caught = Assertions.assertThrows(RuntimeException.class,
                () -> orchestrator.prompt("Hello"));
        Assertions.assertEquals("controller refused", caught.getMessage());
    }

    @Test
    void onRequestThrows_doesNotCommitToHistoryOrNotifyRequestListener() {
        // No orphan state from a turn that never reached the LLM.
        stubAddMessage();
        Mockito.when(mockFileReceiver.takeAttachments())
                .thenReturn(List.of(createAttachment("a.txt")));

        var attachmentListener = Mockito.mock(RequestListener.class);
        var controller = mockController();
        Mockito.doThrow(new RuntimeException("controller refused"))
                .when(controller).onRequest();

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withFileReceiver(mockFileReceiver).withController(controller)
                .withRequestListener(attachmentListener).build();

        Assertions.assertThrows(RuntimeException.class,
                () -> orchestrator.prompt("Hello"));

        Assertions.assertTrue(orchestrator.getHistory().isEmpty(),
                "Pre-stream throw must not commit user message to history");
        Mockito.verify(attachmentListener, Mockito.never())
                .onRequest(Mockito.any());
    }

    @Test
    void preStreamThrow_setsErrorMessageOnAssistantPlaceholder() {
        // Synchronous pre-stream failures (onRequestStart, attachment
        // listener, sync provider throw) update the placeholder, matching
        // the async stream-error path.
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);

        var thrown = new RuntimeException("simulated onRequestStart failure");
        var controller = mockController();
        Mockito.doThrow(thrown).when(controller).onRequest();

        var orchestrator = orchestratorWith(controller);
        Assertions.assertThrows(RuntimeException.class,
                () -> orchestrator.prompt("Hello"));

        Mockito.verify(mockMessage)
                .setText("An error occurred. Please try again.");
    }

    @Test
    void builder_withController_onResponseThrows_isCaughtAndLogged() {
        // A misbehaving hook must not propagate; it gets logged so the
        // failure is operator-visible instead.
        stubAddMessage();
        var streamError = new RuntimeException("API died");
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.error(streamError));

        var controller = mockController();
        Mockito.doThrow(new RuntimeException("controller blew up"))
                .when(controller).onResponse(Mockito.any());

        orchestratorWith(controller).prompt("Hello");

        Mockito.verify(controller).onResponse(streamError);
        var logged = logger.getLoggingEvents().stream().filter(event -> event
                .getMessage().equals("Error in controller onResponse"))
                .findFirst();
        Assertions.assertTrue(logged.isPresent(),
                "Expected an error log entry for the onResponse throw");
    }

    @Test
    void onResponseThrows_onFailurePath_doesNotAddSeparateErrorMessage() {
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.error(new RuntimeException("API died")));

        var controller = mockController();
        Mockito.doThrow(new RuntimeException("controller blew up"))
                .when(controller).onResponse(Mockito.any());

        orchestratorWith(controller).prompt("Hello");

        Mockito.verify(mockMessageList, Mockito.never()).addMessage(
                Mockito.eq("An error occurred. Please try again."),
                Mockito.anyString(), Mockito.anyList());
    }

    @Test
    void builder_withController_preStreamThrow_firesOnResponseWithError() {
        stubAddMessage();
        var thrown = new IllegalStateException(
                "provider exploded before returning a stream");
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenThrow(thrown);

        var controller = mockController();
        var orchestrator = orchestratorWith(controller);
        var caught = Assertions.assertThrows(IllegalStateException.class,
                () -> orchestrator.prompt("Hello"));
        Assertions.assertSame(thrown, caught);

        Mockito.verify(controller).onResponse(thrown);
        Mockito.verify(controller, Mockito.never()).onResponse(null);
    }

    @Test
    void preStreamThrow_controllerCanRetryFromOnResponse() {
        // Order invariant: isProcessing must be released BEFORE
        // onResponse fires, otherwise a controller's recovery prompt is
        // silently dropped at the CAS-false branch.
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenThrow(new IllegalStateException("first turn dies"))
                .thenReturn(Flux.just("Response"));

        var orchestratorRef = new AtomicReference<AIOrchestrator>();
        var controller = mockController();
        // Retry only on the failure-side fire; the success fire on the
        // retry turn must not re-recurse.
        Mockito.doAnswer(invocation -> {
            if (invocation.getArgument(0) != null) {
                orchestratorRef.get().prompt("retry");
            }
            return null;
        }).when(controller).onResponse(Mockito.any());

        var orchestrator = orchestratorWith(controller);
        orchestratorRef.set(orchestrator);

        Assertions.assertThrows(IllegalStateException.class,
                () -> orchestrator.prompt("first"));

        Mockito.verify(mockProvider, Mockito.times(2))
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
        Mockito.verify(controller)
                .onResponse(Mockito.any(IllegalStateException.class));
        Mockito.verify(controller).onResponse(null);
    }

    @Test
    void preStreamThrow_firesOnResponseForErrorSubtypes() {
        // onResponseFailed must fire for any Throwable, not just
        // Exception — controllers need to release per-turn state
        // (snapshots, pending writes, locks) regardless of which
        // failure mode killed the turn.
        stubAddMessage();
        var thrown = new AssertionError("simulated assertion in provider");
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenThrow(thrown);

        var controller = mockController();
        var orchestrator = orchestratorWith(controller);
        var caught = Assertions.assertThrows(AssertionError.class,
                () -> orchestrator.prompt("Hello"));
        Assertions.assertSame(thrown, caught);

        Mockito.verify(controller).onResponse(thrown);
        Mockito.verify(controller, Mockito.never()).onResponse(null);
    }

    @Test
    void doPrompt_hangSafety_releasesProcessingFlagOnNonExceptionThrowable() {
        // Error subtypes (OOM, AssertionError) must still release
        // isProcessing, otherwise every later prompt is silently
        // dropped at the CAS-false branch.
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenThrow(new AssertionError("simulated fatal"))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).build();
        Assertions.assertThrows(AssertionError.class,
                () -> orchestrator.prompt("first"));

        orchestrator.prompt("second");
        Mockito.verify(mockProvider, Mockito.times(2))
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void builder_withController_successfulTurnFiresOnResponseWithNullError() {
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var controller = mockController();
        orchestratorWith(controller).prompt("Hello");

        Mockito.verify(controller).onResponse(null);
        // No failure-side fire — error arg never carries a Throwable on a
        // successful turn.
        Mockito.verify(controller, Mockito.never())
                .onResponse(Mockito.any(Throwable.class));
    }

    @Test
    void prompt_byDefault_includesSessionContextToolWithCurrentDateTime() {
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).build();
        orchestrator.prompt("Hello");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        var tools = captor.getValue().explicitTools();
        Assertions.assertEquals(1, tools.size());
        var contextTool = tools.get(0);
        Assertions.assertEquals("get_session_context", contextTool.getName());
        Assertions.assertTrue(
                contextTool.getDescription()
                        .contains("Current server date and time:"),
                "Default supplier should render a date/time line; got: "
                        + contextTool.getDescription());
    }

    @Test
    void prompt_withCustomContextSupplier_replacesDefaultAndExposesContent() {
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withMetadata(() -> "Tenant: acme").build();
        orchestrator.prompt("Hello");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        var tools = captor.getValue().explicitTools();
        Assertions.assertEquals(1, tools.size());
        var contextTool = tools.get(0);
        Assertions.assertTrue(
                contextTool.getDescription().contains("Tenant: acme"),
                "Custom supplier value should appear in the description; got: "
                        + contextTool.getDescription());
        Assertions.assertFalse(
                contextTool.getDescription()
                        .contains("Current server date and time:"),
                "Custom supplier should fully replace the default; got: "
                        + contextTool.getDescription());
        Assertions.assertEquals("Tenant: acme", contextTool.execute(null),
                "execute should return the same content the description shows");
    }

    @Test
    void prompt_withNullContext_omitsSessionContextTool() {
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withMetadata(null).build();
        orchestrator.prompt("Hello");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        Assertions.assertTrue(captor.getValue().explicitTools().isEmpty(),
                "withMetadata(null) should suppress the built-in tool");
    }

    @Test
    void prompt_withMetadataSupplierReturningBlank_omitsSessionContextTool() {
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withMetadata(() -> "   ")
                .build();
        orchestrator.prompt("Hello");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        Assertions.assertTrue(captor.getValue().explicitTools().isEmpty(),
                "Empty/blank supplier output should suppress the tool for that turn");
    }

    @Test
    void prompt_withMetadataSupplierReturningNull_omitsSessionContextTool() {
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withMetadata(() -> null)
                .build();
        orchestrator.prompt("Hello");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        Assertions.assertTrue(captor.getValue().explicitTools().isEmpty(),
                "Null supplier output should suppress the tool for that turn");
    }

    @Test
    void prompt_withMetadataAndController_mergesContextFirst() {
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var tool1 = createToolSpec("tool1", "First controller tool");
        var controller = createController(tool1);
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withController(controller)
                .withMetadata(() -> "Tenant: acme").build();
        orchestrator.prompt("Hello");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        var tools = captor.getValue().explicitTools();
        Assertions.assertEquals(2, tools.size());
        Assertions.assertEquals("get_session_context", tools.get(0).getName());
        Assertions.assertEquals("tool1", tools.get(1).getName());
    }

    @Test
    void prompt_withMetadataSupplier_invokedOncePerTurn() {
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var callCount = new AtomicInteger();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withMetadata(() -> {
                    callCount.incrementAndGet();
                    return "tick " + callCount.get();
                }).build();

        orchestrator.prompt("first");
        orchestrator.prompt("second");

        Assertions.assertEquals(2, callCount.get(),
                "Supplier should be invoked once per prompt");
    }

    @Test
    void prompt_withMetadataSupplierThrowing_abortsTurn() {
        stubAddMessage();
        var thrown = new RuntimeException("context supplier failed");
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withMetadata(() -> {
                    throw thrown;
                }).build();

        var caught = Assertions.assertThrows(RuntimeException.class,
                () -> orchestrator.prompt("Hello"));
        Assertions.assertSame(thrown, caught);

        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
        Assertions.assertTrue(orchestrator.getHistory().isEmpty(),
                "Aborted turn must not leave the user message in history");
    }

    @Test
    void builder_withMetadataCalledTwice_logsWarning() {
        AIOrchestrator.builder(mockProvider, null).withMetadata(() -> "first")
                .withMetadata(() -> "second");

        var warning = logger.getLoggingEvents().stream()
                .filter(e -> e.getMessage()
                        .contains("Context supplier was already set"))
                .findFirst();
        Assertions.assertTrue(warning.isPresent(),
                "Second withMetadata call should log a replacement warning");
    }

    @Test
    void builder_withNoControllersAndNoContext_explicitToolsIsEmpty() {
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withMetadata(null).build();
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
    void withController_reservedSessionContextToolName_logsWarning() {
        var reserved = createToolSpec("get_session_context", "Clashing tool");
        AIController controller = createController(reserved);

        AIOrchestrator.builder(mockProvider, null).withController(controller);

        var warning = logger.getLoggingEvents().stream()
                .filter(event -> event.getMessage().equals(
                        "Tool name '{}' is reserved for the built-in session context tool"))
                .findFirst();

        Assertions.assertTrue(warning.isPresent(),
                "Using the reserved tool name should log a warning");
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
    void builder_withResponseListenerCalledTwice_logsWarning() {
        var orchestratorBuilder = AIOrchestrator.builder(mockProvider, null)
                .withResponseListener(event -> {
                });
        assertNoBuilderWarning();
        orchestratorBuilder.withResponseListener(event -> {
        }).build();
        assertBuilderWarning("responseListener");
    }

    @Test
    void builder_claimsAllResources_toPreventSharing() throws Exception {
        // Builder with-methods that do NOT configure a shareable resource —
        // value types, listeners, tools, and restored conversation state. Any
        // other with-method is treated as configuring a resource that build()
        // must claim. If you add a new resource with-method (component,
        // controller, ...), do not add it here — ensure build() claims it.
        Set<String> nonResourceSetters = Set.of("withTools", "withUserName",
                "withAssistantName", "withRequestListener",
                "withAttachmentClickListener", "withResponseListener",
                "withHistory", "withMetadata");

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

    private void stubAddMessage() {
        var mockMessage = createMockMessage();
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
    }

    private AIController mockController() {
        var controller = Mockito.mock(AIController.class);
        Mockito.when(controller.getTools()).thenReturn(List.of());
        return controller;
    }

    private AIOrchestrator orchestratorWith(AIController controller) {
        return AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withController(controller)
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
