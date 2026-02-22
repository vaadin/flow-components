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

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.vaadin.flow.component.ai.AIComponentsFeatureFlagProvider;
import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.ui.AIFileReceiver;
import com.vaadin.flow.component.ai.ui.AIMessage;
import com.vaadin.flow.component.ai.ui.AIMessageList;
import com.vaadin.tests.EnableFeatureFlagRule;
import com.vaadin.tests.MockUIRule;

import reactor.core.publisher.Flux;

public class AttachmentListenerTest {
    @Rule
    public MockUIRule ui = new MockUIRule();
    @Rule
    public EnableFeatureFlagRule featureFlagRule = new EnableFeatureFlagRule(
            AIComponentsFeatureFlagProvider.AI_COMPONENTS);

    private LLMProvider mockProvider;
    private AIMessageList mockMessageList;
    private AIFileReceiver mockFileReceiver;
    private AIMessage mockMessage;

    @Before
    public void setup() {
        mockProvider = Mockito.mock(LLMProvider.class);
        mockMessageList = Mockito.mock(AIMessageList.class);
        mockFileReceiver = Mockito.mock(AIFileReceiver.class);
        mockMessage = createMockMessage();

        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(mockMessage);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));
        Mockito.when(mockFileReceiver.takeAttachments())
                .thenReturn(Collections.emptyList());
    }

    // --- AttachmentSubmitListener tests ---

    @Test
    public void submitListener_withAttachments_isCalled() {
        var receivedEvent = new AtomicReference<AttachmentSubmitListener.AttachmentSubmitEvent>();
        var attachment = createAttachment("file.txt");
        Mockito.when(mockFileReceiver.takeAttachments())
                .thenReturn(List.of(attachment));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withFileReceiver(mockFileReceiver)
                .withAttachmentSubmitListener(receivedEvent::set).build();
        orchestrator.prompt("Hello");

        Assert.assertNotNull(receivedEvent.get());
        Assert.assertNotNull(receivedEvent.get().getMessageId());
        Assert.assertEquals(1, receivedEvent.get().getAttachments().size());
        Assert.assertEquals("file.txt",
                receivedEvent.get().getAttachments().getFirst().name());
    }

    @Test
    public void submitListener_withMultipleAttachments_receivesAll() {
        var receivedEvent = new AtomicReference<AttachmentSubmitListener.AttachmentSubmitEvent>();
        Mockito.when(mockFileReceiver.takeAttachments())
                .thenReturn(List.of(createAttachment("a.txt"),
                        createAttachment("b.txt"), createAttachment("c.txt")));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withFileReceiver(mockFileReceiver)
                .withAttachmentSubmitListener(receivedEvent::set).build();
        orchestrator.prompt("Hello");

        Assert.assertEquals(3, receivedEvent.get().getAttachments().size());
    }

    @Test
    public void submitListener_withoutAttachments_isNotCalled() {
        var receivedEvent = new AtomicReference<AttachmentSubmitListener.AttachmentSubmitEvent>();

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withFileReceiver(mockFileReceiver)
                .withAttachmentSubmitListener(receivedEvent::set).build();
        orchestrator.prompt("Hello");

        Assert.assertNull(receivedEvent.get());
    }

    @Test
    public void submitListener_multiplePrompts_receiveDifferentMessageIds() {
        var firstId = new AtomicReference<String>();
        var secondId = new AtomicReference<String>();
        var callCount = new int[] { 0 };

        Mockito.when(mockFileReceiver.takeAttachments())
                .thenReturn(List.of(createAttachment("file.txt")));
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.empty());

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withFileReceiver(mockFileReceiver)
                .withAttachmentSubmitListener(event -> {
                    if (callCount[0]++ == 0) {
                        firstId.set(event.getMessageId());
                    } else {
                        secondId.set(event.getMessageId());
                    }
                }).build();
        orchestrator.prompt("First");
        orchestrator.prompt("Second");

        Assert.assertNotNull(firstId.get());
        Assert.assertNotNull(secondId.get());
        Assert.assertNotEquals(firstId.get(), secondId.get());
    }

    // --- AttachmentClickListener tests ---

    @Test
    public void clickListener_configured_registersOnMessageList() {
        AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withAttachmentClickListener(event -> {
                }).build();

        Mockito.verify(mockMessageList).addAttachmentClickListener(
                Mockito.any(AIMessageList.AttachmentClickCallback.class));
    }

    @Test
    public void clickListener_notConfigured_doesNotRegisterOnMessageList() {
        AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).build();

        Mockito.verify(mockMessageList, Mockito.never())
                .addAttachmentClickListener(Mockito
                        .any(AIMessageList.AttachmentClickCallback.class));
    }

    @Test
    public void clickCallback_translatesMessageToMessageId() {
        var receivedSubmitEvent = new AtomicReference<AttachmentSubmitListener.AttachmentSubmitEvent>();
        var receivedClickEvent = new AtomicReference<AttachmentClickListener.AttachmentClickEvent>();

        Mockito.when(mockFileReceiver.takeAttachments())
                .thenReturn(List.of(createAttachment("file.txt")));

        var callbackCaptor = ArgumentCaptor
                .forClass(AIMessageList.AttachmentClickCallback.class);

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withFileReceiver(mockFileReceiver)
                .withAttachmentSubmitListener(receivedSubmitEvent::set)
                .withAttachmentClickListener(receivedClickEvent::set).build();

        Mockito.verify(mockMessageList)
                .addAttachmentClickListener(callbackCaptor.capture());

        orchestrator.prompt("Hello");

        // Simulate a click on the message that was added
        callbackCaptor.getValue().onAttachmentClick(mockMessage, 0);

        Assert.assertNotNull(receivedClickEvent.get());
        Assert.assertEquals(receivedSubmitEvent.get().getMessageId(),
                receivedClickEvent.get().getMessageId());
        Assert.assertEquals(0, receivedClickEvent.get().getAttachmentIndex());
    }

    @Test
    public void clickCallback_withAttachmentIndex_passesCorrectIndex() {
        var receivedClickEvent = new AtomicReference<AttachmentClickListener.AttachmentClickEvent>();

        Mockito.when(mockFileReceiver.takeAttachments()).thenReturn(
                List.of(createAttachment("a.txt"), createAttachment("b.txt")));

        var callbackCaptor = ArgumentCaptor
                .forClass(AIMessageList.AttachmentClickCallback.class);

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withFileReceiver(mockFileReceiver)
                .withAttachmentClickListener(receivedClickEvent::set).build();

        Mockito.verify(mockMessageList)
                .addAttachmentClickListener(callbackCaptor.capture());

        orchestrator.prompt("Hello");

        callbackCaptor.getValue().onAttachmentClick(mockMessage, 1);

        Assert.assertEquals(1, receivedClickEvent.get().getAttachmentIndex());
    }

    @Test
    public void clickCallback_unknownMessage_doesNotCallListener() {
        var receivedClickEvent = new AtomicReference<AttachmentClickListener.AttachmentClickEvent>();

        var callbackCaptor = ArgumentCaptor
                .forClass(AIMessageList.AttachmentClickCallback.class);

        AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withAttachmentClickListener(receivedClickEvent::set).build();

        Mockito.verify(mockMessageList)
                .addAttachmentClickListener(callbackCaptor.capture());

        // Simulate a click with a message that was never added via prompt
        var unknownMessage = createMockMessage();
        callbackCaptor.getValue().onAttachmentClick(unknownMessage, 0);

        Assert.assertNull(receivedClickEvent.get());
    }

    // --- Helpers ---

    private static AIMessage createMockMessage() {
        var message = Mockito.mock(AIMessage.class);
        Mockito.when(message.getText()).thenReturn("");
        Mockito.when(message.getTime()).thenReturn(Instant.now());
        Mockito.when(message.getUserName()).thenReturn("Test");
        return message;
    }

    private static AIAttachment createAttachment(String fileName) {
        return new AIAttachment(fileName, "text/plain", "test".getBytes());
    }
}
