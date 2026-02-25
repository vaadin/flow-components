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
package com.vaadin.flow.component.ai;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.common.ChatMessage;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.testutil.ClassesSerializableTest;
import com.vaadin.tests.EnableFeatureFlagRule;
import com.vaadin.tests.MockUIRule;

import reactor.core.publisher.Flux;

public class AIComponentsSerializableTest extends ClassesSerializableTest {

    @Rule
    public EnableFeatureFlagRule featureFlagRule = new EnableFeatureFlagRule(
            AIComponentsFeatureFlagProvider.AI_COMPONENTS);
    @Rule
    public MockUIRule ui = new MockUIRule();

    private LLMProvider mockProvider;

    @Override
    protected Stream<String> getExcludedPatterns() {
        return Stream.concat(super.getExcludedPatterns(), Stream.of(
                "com\\.vaadin\\.flow\\.component\\.upload\\.receivers\\.TempDirectory\\$LazyHolder",
                "com\\.vaadin\\.flow\\.component\\.upload\\.receivers\\.TempDirectory",
                "com\\.vaadin\\.flow\\.component\\.ai\\.provider\\..*",
                "com\\.vaadin\\.flow\\.component\\.ai\\.AIComponentsFeatureFlagProvider",
                // AIOrchestrator — private constructor, requires LLMProvider
                "com\\.vaadin\\.flow\\.component\\.ai\\.orchestrator\\.AIOrchestrator",
                "com\\.vaadin\\.flow\\.component\\.ai\\.orchestrator\\.AIOrchestrator\\$Reconnector",
                "com\\.vaadin\\.flow\\.component\\.ai\\.orchestrator\\.AIOrchestrator\\$Builder"));
    }

    @Before
    public void setUp() {
        mockProvider = Mockito.mock(LLMProvider.class);
    }

    @Test
    public void serialization_roundTrip_reconnectRestoresProvider()
            throws Throwable {
        var orchestrator = AIOrchestrator.builder(mockProvider, null).build();
        var deserialized = serializeAndDeserialize(orchestrator);

        var newProvider = Mockito.mock(LLMProvider.class);
        Mockito.when(
                newProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));
        deserialized.reconnect(newProvider).apply();

        deserialized.prompt("Hello");
        Mockito.verify(newProvider)
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    public void serialization_roundTrip_preservesConversationHistory()
            throws Throwable {
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1",
                        Instant.now()),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Hi there", null,
                        Instant.now()));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withHistory(history, Collections.emptyMap()).build();

        var deserialized = serializeAndDeserialize(orchestrator);
        var restored = deserialized.getHistory();
        Assert.assertEquals(2, restored.size());
        Assert.assertEquals("Hello", restored.getFirst().content());
        Assert.assertEquals("msg-1", restored.getFirst().messageId());
        Assert.assertEquals("Hi there", restored.get(1).content());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void serialization_roundTrip_reconnectRestoresHistoryOnProvider()
            throws Throwable {
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1", null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Hi there", null,
                        null));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withHistory(history, Collections.emptyMap()).build();

        var deserialized = serializeAndDeserialize(orchestrator);
        var newProvider = Mockito.mock(LLMProvider.class);
        deserialized.reconnect(newProvider).apply();

        var historyCaptor = ArgumentCaptor.forClass(List.class);
        var attachmentsCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(newProvider).setHistory(historyCaptor.capture(),
                attachmentsCaptor.capture());

        var restoredHistory = (List<ChatMessage>) historyCaptor.getValue();
        Assert.assertEquals(2, restoredHistory.size());
        Assert.assertEquals("Hello", restoredHistory.get(0).content());
        Assert.assertEquals("msg-1", restoredHistory.get(0).messageId());
        Assert.assertEquals("Hi there", restoredHistory.get(1).content());
        Assert.assertTrue(attachmentsCaptor.getValue().isEmpty());
    }

    @Test
    public void reconnect_whenAlreadyConnected_throws() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null).build();
        var newProvider = Mockito.mock(LLMProvider.class);
        Assert.assertThrows(IllegalStateException.class,
                () -> orchestrator.reconnect(newProvider));
    }

    @Test
    public void reconnect_withNullProvider_throws() throws Throwable {
        var orchestrator = AIOrchestrator.builder(mockProvider, null).build();
        var deserialized = serializeAndDeserialize(orchestrator);
        Assert.assertThrows(NullPointerException.class,
                () -> deserialized.reconnect(null));
    }

    @Test
    public void reconnect_withoutTools_usesEmptyTools() throws Throwable {
        var tool = new SampleTool();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withTools(tool).build();

        var deserialized = serializeAndDeserialize(orchestrator);
        var newProvider = Mockito.mock(LLMProvider.class);
        Mockito.when(
                newProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));
        deserialized.reconnect(newProvider).apply();

        deserialized.prompt("Hello");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(newProvider).stream(captor.capture());
        Assert.assertEquals(0, captor.getValue().tools().length);
    }

    @Test
    public void prompt_afterDeserialization_withoutReconnect_throws()
            throws Throwable {
        var orchestrator = AIOrchestrator.builder(mockProvider, null).build();
        var deserialized = serializeAndDeserialize(orchestrator);

        Assert.assertThrows(IllegalStateException.class,
                () -> deserialized.prompt("Hello"));
    }

    @Test
    public void reconnect_withTools_restoresTools() throws Throwable {
        var tool = new SampleTool();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withTools(tool).build();

        var deserialized = serializeAndDeserialize(orchestrator);
        var newTool = new SampleTool();

        var newProvider = Mockito.mock(LLMProvider.class);
        Mockito.when(
                newProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));
        deserialized.reconnect(newProvider).withTools(newTool).apply();

        deserialized.prompt("Use tool");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(newProvider).stream(captor.capture());
        Assert.assertEquals(1, captor.getValue().tools().length);
        Assert.assertSame(newTool, captor.getValue().tools()[0]);
    }

    @Test
    public void reconnect_preservesHistoryAfterApply() throws Throwable {
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1", null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Hi there", null,
                        null));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withHistory(history, Collections.emptyMap()).build();

        var deserialized = serializeAndDeserialize(orchestrator);
        var newProvider = Mockito.mock(LLMProvider.class);
        deserialized.reconnect(newProvider).apply();

        var restored = deserialized.getHistory();
        Assert.assertEquals(2, restored.size());
        Assert.assertEquals("Hello", restored.get(0).content());
        Assert.assertEquals("msg-1", restored.get(0).messageId());
        Assert.assertEquals("Hi there", restored.get(1).content());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void reconnect_withAttachments_passesAttachmentsToProvider()
            throws Throwable {
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "See image", "msg-1",
                        null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Nice photo", null,
                        null));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withHistory(history, Collections.emptyMap()).build();

        var deserialized = serializeAndDeserialize(orchestrator);
        var newProvider = Mockito.mock(LLMProvider.class);
        var attachment = new AIAttachment("photo.png", "image/png",
                new byte[] { 1, 2, 3 });
        var attachments = Map.of("msg-1", List.of(attachment));

        deserialized.reconnect(newProvider).withAttachments(attachments)
                .apply();

        var attachmentsCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(newProvider).setHistory(Mockito.anyList(),
                attachmentsCaptor.capture());

        var restoredAttachments = (Map<String, List<AIAttachment>>) attachmentsCaptor
                .getValue();
        Assert.assertEquals(1, restoredAttachments.size());
        Assert.assertTrue(restoredAttachments.containsKey("msg-1"));
        Assert.assertEquals("photo.png",
                restoredAttachments.get("msg-1").getFirst().name());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void reconnect_withoutAttachments_passesEmptyMapToProvider()
            throws Throwable {
        var history = List.of(
                new ChatMessage(ChatMessage.Role.USER, "Hello", "msg-1", null),
                new ChatMessage(ChatMessage.Role.ASSISTANT, "Hi", null, null));

        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withHistory(history, Collections.emptyMap()).build();

        var deserialized = serializeAndDeserialize(orchestrator);
        var newProvider = Mockito.mock(LLMProvider.class);
        deserialized.reconnect(newProvider).apply();

        var attachmentsCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(newProvider).setHistory(Mockito.anyList(),
                attachmentsCaptor.capture());
        Assert.assertTrue(attachmentsCaptor.getValue().isEmpty());
    }

    @Test
    public void reconnect_withEmptyHistory_doesNotCallSetHistory()
            throws Throwable {
        var orchestrator = AIOrchestrator.builder(mockProvider, null).build();

        var deserialized = serializeAndDeserialize(orchestrator);
        var newProvider = Mockito.mock(LLMProvider.class);
        deserialized.reconnect(newProvider).apply();

        Mockito.verify(newProvider, Mockito.never())
                .setHistory(Mockito.anyList(), Mockito.anyMap());
    }

    private static class SampleTool {
        public String getTemperature() {
            return "22°C";
        }
    }
}
