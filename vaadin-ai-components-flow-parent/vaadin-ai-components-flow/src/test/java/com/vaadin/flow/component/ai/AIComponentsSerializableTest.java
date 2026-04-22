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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.common.ChatMessage;
import com.vaadin.flow.component.ai.orchestrator.AIController;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.testutil.ClassesSerializableTest;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

import reactor.core.publisher.Flux;
import tools.jackson.databind.JsonNode;

class AIComponentsSerializableTest extends ClassesSerializableTest {

    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            AIComponentsFeatureFlagProvider.AI_COMPONENTS);
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private LLMProvider mockProvider;

    @Override
    protected Stream<String> getExcludedPatterns() {
        return Stream.concat(super.getExcludedPatterns(), Stream.of(
                "com\\.vaadin\\.flow\\.component\\.upload\\.receivers\\.TempDirectory\\$LazyHolder",
                "com\\.vaadin\\.flow\\.component\\.upload\\.receivers\\.TempDirectory",
                "com\\.vaadin\\.flow\\.component\\.charts\\.model\\.serializers\\..*",
                "com\\.vaadin\\.flow\\.component\\.grid\\.GridColumnOrderHelper.*",
                "com\\.vaadin\\.flow\\.component\\.grid\\.GridSelectionSignalHelper.*",
                "com\\.vaadin\\.flow\\.component\\.ai\\.provider\\..*",
                // GridAIController — intentionally not serializable; restored
                // via reconnect()
                "com\\.vaadin\\.flow\\.component\\.ai\\.grid\\.GridAIController(\\$\\d+)?",
                "com\\.vaadin\\.flow\\.component\\.ai\\.grid\\.GridAITools.*",
                // AIController — intentionally not serializable; restored
                // via reconnect()
                "com\\.vaadin\\.flow\\.component\\.ai\\.orchestrator\\.AIController",
                "com\\.vaadin\\.flow\\.component\\.ai\\.AIComponentsFeatureFlagProvider",
                "com\\.vaadin\\.flow\\.component\\.ai\\.orchestrator\\.AIOrchestrator\\$Reconnector",
                "com\\.vaadin\\.flow\\.component\\.ai\\.orchestrator\\.AIOrchestrator\\$Builder",
                // Static utility class with anonymous ToolSpec instances —
                // not instantiable or serializable
                "com\\.vaadin\\.flow\\.component\\.ai\\.chart\\.ChartAITools(\\$\\d+)?",
                // ChartAIController — intentionally not serializable;
                // restored via reconnect()
                "com\\.vaadin\\.flow\\.component\\.ai\\.chart\\.ChartAIController(\\$\\d+)?",
                // Build-time generator — not a runtime component
                "com\\.vaadin\\.flow\\.component\\.ai\\.chart\\.PlotOptionsSchemaGenerator"));
    }

    @BeforeEach
    void setUp() {
        mockProvider = Mockito.mock(LLMProvider.class);
    }

    @Test
    void serialization_roundTrip_reconnectRestoresProvider() throws Throwable {
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
    void serialization_roundTrip_preservesConversationHistory()
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
        Assertions.assertEquals(2, restored.size());
        Assertions.assertEquals("Hello", restored.getFirst().content());
        Assertions.assertEquals("msg-1", restored.getFirst().messageId());
        Assertions.assertEquals("Hi there", restored.get(1).content());
    }

    @SuppressWarnings("unchecked")
    @Test
    void serialization_roundTrip_reconnectRestoresHistoryOnProvider()
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
        Assertions.assertEquals(2, restoredHistory.size());
        Assertions.assertEquals("Hello", restoredHistory.get(0).content());
        Assertions.assertEquals("msg-1", restoredHistory.get(0).messageId());
        Assertions.assertEquals("Hi there", restoredHistory.get(1).content());
        Assertions.assertTrue(attachmentsCaptor.getValue().isEmpty());
    }

    @Test
    void reconnect_whenAlreadyConnected_throws() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null).build();
        var newProvider = Mockito.mock(LLMProvider.class);
        Assertions.assertThrows(IllegalStateException.class,
                () -> orchestrator.reconnect(newProvider));
    }

    @Test
    void reconnect_withNullProvider_throws() throws Throwable {
        var orchestrator = AIOrchestrator.builder(mockProvider, null).build();
        var deserialized = serializeAndDeserialize(orchestrator);
        Assertions.assertThrows(NullPointerException.class,
                () -> deserialized.reconnect(null));
    }

    @Test
    void reconnect_withoutTools_usesEmptyTools() throws Throwable {
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
        Assertions.assertEquals(0, captor.getValue().tools().length);
    }

    @Test
    void prompt_afterDeserialization_withoutReconnect_throws()
            throws Throwable {
        var orchestrator = AIOrchestrator.builder(mockProvider, null).build();
        var deserialized = serializeAndDeserialize(orchestrator);

        Assertions.assertThrows(IllegalStateException.class,
                () -> deserialized.prompt("Hello"));
    }

    @Test
    void reconnect_withTools_restoresTools() throws Throwable {
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
        Assertions.assertEquals(1, captor.getValue().tools().length);
        Assertions.assertSame(newTool, captor.getValue().tools()[0]);
    }

    @Test
    void reconnect_preservesHistoryAfterApply() throws Throwable {
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
        Assertions.assertEquals(2, restored.size());
        Assertions.assertEquals("Hello", restored.get(0).content());
        Assertions.assertEquals("msg-1", restored.get(0).messageId());
        Assertions.assertEquals("Hi there", restored.get(1).content());
    }

    @SuppressWarnings("unchecked")
    @Test
    void reconnect_withAttachments_passesAttachmentsToProvider()
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
        Assertions.assertEquals(1, restoredAttachments.size());
        Assertions.assertTrue(restoredAttachments.containsKey("msg-1"));
        Assertions.assertEquals("photo.png",
                restoredAttachments.get("msg-1").getFirst().name());
    }

    @SuppressWarnings("unchecked")
    @Test
    void reconnect_withoutAttachments_passesEmptyMapToProvider()
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
        Assertions.assertTrue(attachmentsCaptor.getValue().isEmpty());
    }

    @Test
    void reconnect_withEmptyHistory_doesNotCallSetHistory() throws Throwable {
        var orchestrator = AIOrchestrator.builder(mockProvider, null).build();

        var deserialized = serializeAndDeserialize(orchestrator);
        var newProvider = Mockito.mock(LLMProvider.class);
        deserialized.reconnect(newProvider).apply();

        Mockito.verify(newProvider, Mockito.never())
                .setHistory(Mockito.anyList(), Mockito.anyMap());
    }

    @Test
    void reconnect_withController_replacesController() throws Throwable {
        var newProvider = Mockito.mock(LLMProvider.class);
        Mockito.when(
                newProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("New Response"));

        var tool1 = createToolSpec("originalTool", "Original");
        AIController originalController = createController(tool1);

        // Build without mocks (no message list) so it can serialize
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withController(originalController).build();

        var deserialized = serializeAndDeserialize(orchestrator);

        var tool2 = createToolSpec("newTool", "New");
        AIController newController = createController(tool2);

        deserialized.reconnect(newProvider).withController(newController)
                .apply();
        deserialized.prompt("Hello");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(newProvider).stream(captor.capture());
        var explicitTools = captor.getValue().explicitTools();
        Assertions.assertEquals(1, explicitTools.size());
        Assertions.assertEquals("newTool", explicitTools.getFirst().getName());
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
                // Test controller doesn't need to handle request completion
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

    private static class SampleTool {
        public String getTemperature() {
            return "22°C";
        }
    }
}
