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
import java.util.concurrent.FutureTask;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.common.ChatMessage;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.testutil.ClassesSerializableTest;

import reactor.core.publisher.Flux;

public class AIComponentsSerializableTest extends ClassesSerializableTest {

    private LLMProvider mockProvider;
    private MockedStatic<FeatureFlags> mockFeatureFlagsStatic;
    private UI mockUI;

    @Override
    protected Stream<String> getExcludedPatterns() {
        return Stream.concat(super.getExcludedPatterns(), Stream.of(
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
    public void serialization_roundTrip_reconnectRestoresProvider()
            throws Throwable {
        var orchestrator = AIOrchestrator.builder(mockProvider, null).build();
        var deserialized = serializeAndDeserialize(orchestrator);

        var newProvider = Mockito.mock(LLMProvider.class);
        Mockito.when(
                newProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));
        deserialized.reconnect(newProvider).apply();

        mockUi();
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

        Mockito.verify(newProvider).setHistory(Mockito.anyList(),
                Mockito.anyMap());
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

        mockUi();
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

        mockUi();
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

        mockUi();
        deserialized.prompt("Use tool");

        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(newProvider).stream(captor.capture());
        Assert.assertEquals(1, captor.getValue().tools().length);
        Assert.assertSame(newTool, captor.getValue().tools()[0]);
    }

    private void mockUi() {
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

        var mockFeatureFlags = Mockito.mock(FeatureFlags.class);
        mockFeatureFlagsStatic = Mockito.mockStatic(FeatureFlags.class);
        Mockito.when(mockFeatureFlags
                .isEnabled(AIComponentsFeatureFlagProvider.FEATURE_FLAG_ID))
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

    private static class SampleTool {
        public String getTemperature() {
            return "22°C";
        }
    }
}
