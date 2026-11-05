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

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.ai.AIComponentsFeatureFlagProvider;
import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.ui.AIInput;
import com.vaadin.flow.component.ai.ui.AIMessage;
import com.vaadin.flow.component.ai.ui.AIMessageList;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.streams.UploadCompleteEvent;
import com.vaadin.flow.internal.streams.UploadStartEvent;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

import reactor.core.publisher.Flux;

class UploadInProgressTest {
    private static final String UPLOADING_WARNING = "a file is still being uploaded";

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();
    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            AIComponentsFeatureFlagProvider.AI_COMPONENTS);

    private final TestLogger logger = TestLoggerFactory
            .getTestLogger(AIOrchestrator.class);

    private LLMProvider mockProvider;
    private AIMessageList mockMessageList;
    private UploadManager uploadManager;
    private Component uploadManagerConnector;
    private Upload upload;

    @BeforeEach
    void setup() throws ReflectiveOperationException {
        mockProvider = Mockito.mock(LLMProvider.class);
        mockMessageList = Mockito.mock(AIMessageList.class);
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList()))
                .thenReturn(Mockito.mock(AIMessage.class));
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));

        var owner = new Div();
        ui.add(owner);
        uploadManager = new UploadManager(owner);
        var connectorField = UploadManager.class.getDeclaredField("connector");
        connectorField.setAccessible(true);
        uploadManagerConnector = (Component) connectorField.get(uploadManager);

        upload = new Upload();
        ui.add(upload);
        logger.clearAll();
    }

    @Test
    void prompt_whileUploadManagerUploading_promptIgnored() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withFileReceiver(uploadManager).build();
        ui.dumpPendingJavaScriptInvocations();
        fireUploadStart(uploadManagerConnector);

        orchestrator.prompt("Hello");

        assertPromptIgnored();
    }

    @Test
    void prompt_afterUploadManagerUploadCompleted_promptSent() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withFileReceiver(uploadManager).build();
        fireUploadStart(uploadManagerConnector);
        orchestrator.prompt("Hello");
        fireUploadComplete(uploadManagerConnector);

        orchestrator.prompt("Hello");

        assertPromptSent("Hello");
    }

    @Test
    void prompt_whileUploadUploading_promptIgnored() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withFileReceiver(upload)
                .build();
        ui.dumpPendingJavaScriptInvocations();
        fireUploadStart(upload);

        orchestrator.prompt("Hello");

        assertPromptIgnored();
    }

    @Test
    void prompt_afterUploadUploadCompleted_promptSent() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withFileReceiver(upload)
                .build();
        fireUploadStart(upload);
        orchestrator.prompt("Hello");
        fireUploadComplete(upload);

        orchestrator.prompt("Hello");

        assertPromptSent("Hello");
    }

    @Test
    @SuppressWarnings("unchecked")
    void inputSubmit_whileUploading_promptIgnored() {
        var mockInput = Mockito.mock(AIInput.class);
        AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withInput(mockInput)
                .withFileReceiver(uploadManager).build();
        var listenerCaptor = ArgumentCaptor
                .forClass(SerializableConsumer.class);
        Mockito.verify(mockInput).addSubmitListener(listenerCaptor.capture());
        ui.dumpPendingJavaScriptInvocations();
        fireUploadStart(uploadManagerConnector);

        listenerCaptor.getValue().accept("Hello");

        assertPromptIgnored();
    }

    @Test
    void promptWithAttachments_whileUploading_promptSent() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withFileReceiver(uploadManager).build();
        fireUploadStart(uploadManagerConnector);

        orchestrator.prompt("Hello", List.of(
                new AIAttachment("file.txt", "text/plain", new byte[] { 1 })));

        assertPromptSent("Hello");
    }

    private void assertPromptIgnored() {
        Mockito.verifyNoInteractions(mockProvider);
        Mockito.verify(mockMessageList, Mockito.never()).addMessage(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyList());
        Assertions.assertTrue(ui.dumpPendingJavaScriptInvocations().isEmpty(),
                "The file list must not be cleared");
        Assertions.assertTrue(
                logger.getLoggingEvents().stream()
                        .anyMatch(event -> event.getMessage()
                                .contains(UPLOADING_WARNING)),
                "An ignored prompt must be logged");
    }

    private void assertPromptSent(String userMessage) {
        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        Assertions.assertEquals(userMessage, captor.getValue().userMessage());
    }

    private static void fireUploadStart(Component component) {
        ComponentUtil.fireEvent(component, new UploadStartEvent(component));
    }

    private static void fireUploadComplete(Component component) {
        ComponentUtil.fireEvent(component, new UploadCompleteEvent(component));
    }
}
