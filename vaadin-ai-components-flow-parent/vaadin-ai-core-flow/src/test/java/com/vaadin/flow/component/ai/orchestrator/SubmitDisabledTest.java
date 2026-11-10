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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.AIComponentsFeatureFlagProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.ui.AIFileReceiver;
import com.vaadin.flow.component.ai.ui.AIInput;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.flow.internal.streams.UploadCompleteEvent;
import com.vaadin.flow.internal.streams.UploadStartEvent;
import com.vaadin.flow.server.Command;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

class SubmitDisabledTest {
    private static final String FILES_UPLOADING = "element.files.some(file => file.uploading)";
    private static final String MANAGER_FILES_UPLOADING = "element.manager.files.some(file => file.uploading)";

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();
    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            AIComponentsFeatureFlagProvider.AI_COMPONENTS);

    private LLMProvider mockProvider;
    private Sinks.Many<String> response;
    private MessageInput messageInput;
    private UploadManager uploadManager;
    private Element uploadManagerConnector;
    private Upload upload;

    @BeforeEach
    void setup() throws ReflectiveOperationException {
        response = Sinks.many().unicast().onBackpressureBuffer();
        mockProvider = Mockito.mock(LLMProvider.class);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenAnswer(invocation -> response.asFlux());

        messageInput = new MessageInput();
        ui.add(messageInput);

        var owner = new Div();
        ui.add(owner);
        uploadManager = new UploadManager(owner);
        var connectorField = UploadManager.class.getDeclaredField("connector");
        connectorField.setAccessible(true);
        uploadManagerConnector = ((Component) connectorField.get(uploadManager))
                .getElement();

        upload = new Upload();
        ui.add(upload);
    }

    @Test
    void inputSubmit_responseStreaming_submitDisabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .build();

        submitFromClient("Hello");

        Assertions.assertTrue(messageInput.isSubmitDisabled());
    }

    @Test
    void inputSubmit_responseCompleted_submitEnabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .build();
        submitFromClient("Hello");

        response.tryEmitNext("Hi");
        response.tryEmitComplete();

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void inputSubmit_responseFailed_submitEnabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .build();
        submitFromClient("Hello");

        response.tryEmitError(new RuntimeException("Failed"));

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void prompt_responseCompletedImmediately_submitEnabled() {
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Hi"));
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput).build();

        orchestrator.prompt("Hello");

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void prompt_providerThrows_submitEnabled() {
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenThrow(new IllegalStateException("Failed"));
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput).build();

        Assertions.assertThrows(IllegalStateException.class,
                () -> orchestrator.prompt("Hello"));

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void prompt_rejectedByInterceptor_submitEnabled() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput)
                .withRequestInterceptor(
                        RequestInterceptor.RequestInterceptEvent::reject)
                .build();

        orchestrator.prompt("Hello");

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void prompt_postponed_submitDisabledUntilResponseCompleted() {
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput)
                .withRequestInterceptor(event -> continuation
                        .set(event.postpone(Duration.ofMinutes(1))))
                .build();

        orchestrator.prompt("Hello");
        Assertions.assertTrue(messageInput.isSubmitDisabled());

        continuation.get().proceed();
        Assertions.assertTrue(messageInput.isSubmitDisabled());

        response.tryEmitComplete();
        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void prompt_postponedAndRejected_submitEnabled() {
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput).withRequestInterceptor(event -> {
                    continuation.set(event.postpone(Duration.ofMinutes(1)));
                    event.reject();
                }).build();
        orchestrator.prompt("Hello");

        continuation.get().proceed();

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void prompt_postponedAndFailed_submitEnabled() {
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput)
                .withRequestInterceptor(event -> continuation
                        .set(event.postpone(Duration.ofMinutes(1))))
                .build();
        orchestrator.prompt("Hello");

        continuation.get().fail(new RuntimeException("Failed"));

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void prompt_postponedAndFailedWhileUploading_submitStaysDisabledUntilUploadFinished() {
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput).withFileReceiver(uploadManager)
                .withRequestInterceptor(event -> continuation
                        .set(event.postpone(Duration.ofMinutes(1))))
                .build();
        orchestrator.prompt("Hello");
        fireDomEvent(uploadManagerConnector, "upload-start", true);

        continuation.get().fail(new RuntimeException("Failed"));
        Assertions.assertTrue(messageInput.isSubmitDisabled());

        fireDomEvent(uploadManagerConnector, "all-finished", false);
        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void responseListenerDisablesSubmit_submitStaysDisabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withResponseListener(
                        event -> messageInput.setSubmitDisabled(true))
                .build();
        submitFromClient("Hello");

        response.tryEmitComplete();

        Assertions.assertTrue(messageInput.isSubmitDisabled());
    }

    @Test
    void responseListenerDisablesSubmitOnError_submitStaysDisabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withResponseListener(
                        event -> messageInput.setSubmitDisabled(true))
                .build();
        submitFromClient("Hello");

        response.tryEmitError(new RuntimeException("Failed"));

        Assertions.assertTrue(messageInput.isSubmitDisabled());
    }

    @Test
    void responseListenerDisablesSubmitWhileUploading_submitStaysDisabledAfterUpload() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(uploadManager).withResponseListener(
                        event -> messageInput.setSubmitDisabled(true))
                .build();
        submitFromClient("Hello");
        fireDomEvent(uploadManagerConnector, "upload-start", true);

        response.tryEmitComplete();
        fireDomEvent(uploadManagerConnector, "all-finished", false);

        Assertions.assertTrue(messageInput.isSubmitDisabled());
    }

    @Test
    void responseCompletedWhileUploading_responseListener_submitStaysDisabledUntilUploadFinished() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(uploadManager).withResponseListener(event -> {
                    // Does not disable submitting
                }).build();
        submitFromClient("Hello");
        fireDomEvent(uploadManagerConnector, "upload-start", true);

        response.tryEmitComplete();
        Assertions.assertTrue(messageInput.isSubmitDisabled());

        fireDomEvent(uploadManagerConnector, "all-finished", false);
        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void responseListener_submitAlreadyEnabled() {
        var submitDisabledInListener = new AtomicReference<Boolean>();
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withResponseListener(event -> submitDisabledInListener
                        .set(messageInput.isSubmitDisabled()))
                .build();
        submitFromClient("Hello");

        response.tryEmitComplete();

        Assertions.assertFalse(submitDisabledInListener.get());
    }

    @Test
    void prompt_submitDisabledByApplication_submitStaysDisabled() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput).build();
        messageInput.setSubmitDisabled(true);

        orchestrator.prompt("Hello");
        response.tryEmitComplete();

        Assertions.assertTrue(messageInput.isSubmitDisabled());
    }

    @Test
    void uploadStarted_submitDisabledByApplication_submitStaysDisabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(uploadManager).build();
        messageInput.setSubmitDisabled(true);

        fireDomEvent(uploadManagerConnector, "upload-start", true);
        fireDomEvent(uploadManagerConnector, "all-finished", false);

        Assertions.assertTrue(messageInput.isSubmitDisabled());
    }

    @Test
    void responseReportedAfterNextPromptStarted_submitStaysDisabled() {
        var nextResponse = Sinks.many().unicast()
                .<String> onBackpressureBuffer();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(response.asFlux(), nextResponse.asFlux());
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput).build();
        orchestrator.prompt("Hello");
        var uiTasks = queueUiAccessTasks();

        // The first turn is reported only after the next prompt has started
        response.tryEmitComplete();
        orchestrator.prompt("Again");
        uiTasks.forEach(Command::execute);

        Assertions.assertTrue(messageInput.isSubmitDisabled());
        Mockito.verify(mockProvider, Mockito.times(2))
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void responseReported_submitNotDisabledAgainBeforeStreamEnds() {
        var input = new ComponentInput();
        AIOrchestrator.builder(mockProvider, null).withInput(input).build();
        input.submit("Hello");

        // The response is reported while the stream still holds the request,
        // and the browser may receive the changes of each UI task separately
        response.tryEmitComplete();

        Assertions.assertEquals(List.of(true, false),
                input.submitDisabledValues);
    }

    @Test
    void inputSubmit_laterSubmitListenerCalled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .build();
        var submitted = new AtomicReference<String>();
        messageInput
                .addSubmitListener(event -> submitted.set(event.getValue()));

        submitFromClient("Hello");

        Assertions.assertEquals("Hello", submitted.get());
    }

    @Test
    void uploadManagerUploadStarted_submitDisabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(uploadManager).build();

        fireDomEvent(uploadManagerConnector, "upload-start", true);

        Assertions.assertTrue(messageInput.isSubmitDisabled());
    }

    @Test
    void uploadManagerUploadsFinished_submitEnabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(uploadManager).build();
        fireDomEvent(uploadManagerConnector, "upload-start", true);

        fireDomEvent(uploadManagerConnector, "all-finished", false);

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void uploadManagerUploadStartedAgain_submitDisabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(uploadManager).build();
        fireDomEvent(uploadManagerConnector, "upload-start", true);
        fireDomEvent(uploadManagerConnector, "all-finished", false);

        fireDomEvent(uploadManagerConnector, "upload-start", true);

        Assertions.assertTrue(messageInput.isSubmitDisabled());
    }

    @Test
    void uploadManagerDisabledWhileUploading_uploadsFinished_submitEnabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(uploadManager).build();
        fireDomEvent(uploadManagerConnector, "upload-start", true);

        uploadManager.setEnabled(false);
        fireDomEvent(uploadManagerConnector, "all-finished", false);
        uploadManager.setEnabled(true);

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void uploadManagerUploadCanceled_submitEnabledAndPromptAccepted() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput).withFileReceiver(uploadManager)
                .build();
        fireDomEvent(uploadManagerConnector, "upload-start", true);

        cancelUpload(uploadManagerConnector);
        Assertions.assertFalse(messageInput.isSubmitDisabled());

        orchestrator.prompt("Hello");
        Mockito.verify(mockProvider)
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void uploadUploadStarted_submitDisabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(upload).build();

        fireDomEvent(upload.getElement(), "upload-start", true);

        Assertions.assertTrue(messageInput.isSubmitDisabled());
    }

    @Test
    void uploadUploadsFinished_submitEnabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(upload).build();
        fireDomEvent(upload.getElement(), "upload-start", true);

        fireDomEvent(upload.getElement(), "upload-success", false);

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void uploadDisabledWhileUploading_uploadsFinished_submitEnabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(upload).build();
        fireDomEvent(upload.getElement(), "upload-start", true);

        upload.setEnabled(false);
        fireDomEvent(upload.getElement(), "upload-success", false);
        upload.setEnabled(true);

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void uploadUploadCanceled_submitEnabledAndPromptAccepted() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput).withFileReceiver(upload).build();
        fireDomEvent(upload.getElement(), "upload-start", true);

        cancelUpload(upload.getElement());
        Assertions.assertFalse(messageInput.isSubmitDisabled());

        orchestrator.prompt("Hello");
        Mockito.verify(mockProvider)
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void uploadManagerUploadCanceled_serverStillReceiving_submitEnabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(uploadManager).build();
        var connector = uploadManagerConnector.getComponent().orElseThrow();
        fireDomEvent(uploadManagerConnector, "upload-start", true);
        ComponentUtil.fireEvent(connector, new UploadStartEvent(connector));

        cancelUpload(uploadManagerConnector);
        Assertions.assertFalse(messageInput.isSubmitDisabled());

        ComponentUtil.fireEvent(connector, new UploadCompleteEvent(connector));
        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void uploadUploadCanceled_serverStillReceiving_submitEnabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(upload).build();
        fireDomEvent(upload.getElement(), "upload-start", true);
        ComponentUtil.fireEvent(upload, new UploadStartEvent(upload));

        cancelUpload(upload.getElement());
        Assertions.assertFalse(messageInput.isSubmitDisabled());

        ComponentUtil.fireEvent(upload, new UploadCompleteEvent(upload));
        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void uploadDetached_submitEnabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(upload).build();
        fireDomEvent(upload.getElement(), "upload-start", true);

        ui.remove(upload);

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void uploadsFinishedWhileResponseStreaming_submitStaysDisabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(uploadManager).build();
        submitFromClient("Hello");
        fireDomEvent(uploadManagerConnector, "upload-start", true);

        fireDomEvent(uploadManagerConnector, "all-finished", false);
        Assertions.assertTrue(messageInput.isSubmitDisabled());

        response.tryEmitComplete();
        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void responseCompletedWhileUploading_submitStaysDisabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(uploadManager).build();
        submitFromClient("Hello");
        fireDomEvent(uploadManagerConnector, "upload-start", true);

        response.tryEmitComplete();
        Assertions.assertTrue(messageInput.isSubmitDisabled());

        fireDomEvent(uploadManagerConnector, "all-finished", false);
        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void uploadStartedInBrowser_beforeUploadRequest_clientSubmitIgnored() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(uploadManager).build();
        fireDomEvent(uploadManagerConnector, "upload-start", true);

        submitFromClient("Hello");

        Mockito.verifyNoInteractions(mockProvider);
    }

    @Test
    void uploadManagerAllFinishedListenerAddedBefore_promptAccepted() {
        var orchestrator = new AtomicReference<AIOrchestrator>();
        uploadManager.addAllFinishedListener(
                event -> orchestrator.get().prompt("Hello"));
        orchestrator.set(AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput).withFileReceiver(uploadManager)
                .build());
        fireDomEvent(uploadManagerConnector, "upload-start", true);

        fireDomEvent(uploadManagerConnector, "all-finished", false);

        Mockito.verify(mockProvider)
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void uploadAllFinishedListenerAddedBefore_promptAccepted() {
        var orchestrator = new AtomicReference<AIOrchestrator>();
        upload.addAllFinishedListener(
                event -> orchestrator.get().prompt("Hello"));
        orchestrator.set(AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput).withFileReceiver(upload).build());
        fireDomEvent(upload.getElement(), "upload-start", true);

        fireDomEvent(upload.getElement(), "upload-success", false);

        Mockito.verify(mockProvider)
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void uploadsFinishedInBrowser_serverStillUploading_promptIgnored() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withFileReceiver(upload).build();
        fireDomEvent(upload.getElement(), "upload-start", true);
        ComponentUtil.fireEvent(upload, new UploadStartEvent(upload));
        fireDomEvent(upload.getElement(), "upload-success", false);

        orchestrator.prompt("Hello");
        Mockito.verifyNoInteractions(mockProvider);

        ComponentUtil.fireEvent(upload, new UploadCompleteEvent(upload));
        orchestrator.prompt("Hello");
        Mockito.verify(mockProvider)
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void customReceiverUploading_submitNotDisabled() {
        var receiver = Mockito.mock(AIFileReceiver.class);
        Mockito.when(receiver.isUploading()).thenReturn(true);
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput).withFileReceiver(receiver).build();
        orchestrator.prompt("Hello", List.of());

        response.tryEmitComplete();

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    @SuppressWarnings("unchecked")
    void customInput_responseStreaming_submitDisabledUntilCompleted() {
        var input = Mockito.mock(AIInput.class);
        AIOrchestrator.builder(mockProvider, null).withInput(input).build();
        var listener = ArgumentCaptor.forClass(SerializableConsumer.class);
        Mockito.verify(input).addSubmitListener(listener.capture());

        listener.getValue().accept("Hello");
        response.tryEmitComplete();

        InOrder inOrder = Mockito.inOrder(input);
        inOrder.verify(input).setSubmitDisabled(true);
        inOrder.verify(input).setSubmitDisabled(false);
    }

    @Test
    void reconnect_afterSerializationWhileStreaming_submitEnabled()
            throws IOException, ClassNotFoundException {
        var detachedInput = new MessageInput();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withInput(detachedInput).build();
        orchestrator.prompt("Hello");
        Assertions.assertTrue(detachedInput.isSubmitDisabled());

        var deserialized = serializeAndDeserialize(
                List.of(orchestrator, detachedInput));
        var deserializedOrchestrator = (AIOrchestrator) deserialized.get(0);
        var deserializedInput = (MessageInput) deserialized.get(1);
        Assertions.assertTrue(deserializedInput.isSubmitDisabled());

        deserializedOrchestrator.reconnect(Mockito.mock(LLMProvider.class))
                .apply();

        Assertions.assertFalse(deserializedInput.isSubmitDisabled());
    }

    @Test
    void preserveOnRefresh_responseCompletedAfterRefresh_submitEnabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .build();
        submitFromClient("Hello");

        refreshPreservingInput();
        Assertions.assertTrue(messageInput.isSubmitDisabled());
        response.tryEmitComplete();

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void preserveOnRefresh_responseCompletedBeforeInputMoved_submitEnabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .build();
        submitFromClient("Hello");

        var oldUI = ui.getUI();
        oldUI.getInternals().setSession(null);
        response.tryEmitComplete();
        createUI().getInternals().moveElementsFrom(oldUI);

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void preserveOnRefresh_postponedPromptAbandoned_submitEnabled() {
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput)
                .withRequestInterceptor(event -> continuation
                        .set(event.postpone(Duration.ofMinutes(1))))
                .build();
        orchestrator.prompt("Hello");

        refreshPreservingInput();
        continuation.get().proceed();

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void preserveOnRefresh_postponedPromptFailedAfterRefresh_submitEnabled() {
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput)
                .withRequestInterceptor(event -> continuation
                        .set(event.postpone(Duration.ofMinutes(1))))
                .build();
        orchestrator.prompt("Hello");

        refreshPreservingInput();
        Assertions.assertTrue(messageInput.isSubmitDisabled());
        continuation.get().fail(new RuntimeException("Failed"));

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void preserveOnRefresh_customComponentInput_responseCompletedAfterRefresh_submitEnabled() {
        var input = new ComponentInput();
        ui.add(input);
        AIOrchestrator.builder(mockProvider, null).withInput(input).build();
        input.submit("Hello");

        refreshPreservingInput();
        Assertions.assertTrue(input.isSubmitDisabled());
        response.tryEmitComplete();

        Assertions.assertFalse(input.isSubmitDisabled());
    }

    @Test
    void preserveOnRefresh_wrappingCustomInput_responseCompletedAfterRefresh_submitEnabled() {
        var input = new WrappingInput(messageInput);
        AIOrchestrator.builder(mockProvider, null).withInput(input).build();
        input.submit("Hello");

        refreshPreservingInput();
        Assertions.assertTrue(messageInput.isSubmitDisabled());
        response.tryEmitComplete();

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void preserveOnRefresh_responseListenerDisablesSubmitInInputUI_submitStaysDisabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withResponseListener(event -> messageInput.getUI()
                        .ifPresent(inputUI -> inputUI.access(
                                () -> messageInput.setSubmitDisabled(true))))
                .build();
        submitFromClient("Hello");

        refreshPreservingInput();
        response.tryEmitComplete();

        Assertions.assertTrue(messageInput.isSubmitDisabled());
    }

    @Test
    void preserveOnRefresh_refreshedBeforeQueuedResponseTaskRuns_responseListenerDisablesSubmit_submitStaysDisabled() {
        // The queued task runs with the session locked, so the listener's
        // accessSynchronously() runs right away
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withResponseListener(event -> messageInput.getUI()
                        .ifPresent(inputUI -> inputUI.accessSynchronously(
                                () -> messageInput.setSubmitDisabled(true))))
                .build();
        submitFromClient("Hello");
        var uiTasks = queueUiAccessTasks();

        response.tryEmitComplete();
        refreshPreservingInput();
        // Tasks queued while running the tasks run after them
        for (var i = 0; i < uiTasks.size(); i++) {
            uiTasks.get(i).execute();
        }

        Assertions.assertTrue(messageInput.isSubmitDisabled());
    }

    @Test
    void preserveOnRefresh_responseCompletedOnBackgroundThread_responseListenerDisablesSubmit_submitStaysDisabled()
            throws InterruptedException {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withResponseListener(event -> messageInput.getUI()
                        .ifPresent(inputUI -> inputUI.access(
                                () -> messageInput.setSubmitDisabled(true))))
                .build();
        submitFromClient("Hello");
        refreshPreservingInput();
        lockSessionInAccessTasks();

        // The stream ends on a thread that does not hold the session lock
        var streamEnded = new CountDownLatch(1);
        new Thread(() -> {
            response.tryEmitComplete();
            streamEnded.countDown();
        }).start();

        Assertions.assertTrue(MockSession.awaitUnlocked(ui, streamEnded),
                "The stream never ended");
        Assertions.assertTrue(messageInput.isSubmitDisabled());
    }

    @Test
    void preserveOnRefresh_postponedPromptFailedWhileUploadingAfterRefresh_submitStaysDisabledUntilUploadFinished() {
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput).withFileReceiver(uploadManager)
                .withRequestInterceptor(event -> continuation
                        .set(event.postpone(Duration.ofMinutes(1))))
                .build();
        orchestrator.prompt("Hello");

        refreshPreservingInput();
        fireDomEvent(uploadManagerConnector, "upload-start", true);
        continuation.get().fail(new RuntimeException("Failed"));
        Assertions.assertTrue(messageInput.isSubmitDisabled());

        fireDomEvent(uploadManagerConnector, "all-finished", false);
        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void preserveOnRefresh_responseListenerDisablesSubmitAfterPostponedPromptFailed_submitStaysDisabled() {
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput)
                .withRequestInterceptor(event -> continuation
                        .set(event.postpone(Duration.ofMinutes(1))))
                .withResponseListener(event -> messageInput.getUI()
                        .ifPresent(inputUI -> inputUI.access(
                                () -> messageInput.setSubmitDisabled(true))))
                .build();
        orchestrator.prompt("Hello");

        refreshPreservingInput();
        continuation.get().fail(new RuntimeException("Failed"));

        Assertions.assertTrue(messageInput.isSubmitDisabled());
    }

    /**
     * Moves the components to a new UI and detaches the old one, as a page
     * refresh does for a view with {@code @PreserveOnRefresh}.
     */
    private void refreshPreservingInput() {
        var oldUI = ui.getUI();
        createUI().getInternals().moveElementsFrom(oldUI);
        oldUI.getInternals().setSession(null);
    }

    private UI createUI() {
        var newUI = new UI();
        newUI.getInternals().setSession(ui.getSession());
        return newUI;
    }

    private void submitFromClient(String value) {
        var eventData = JacksonUtils.createObjectNode();
        eventData.put("event.detail.value", value);
        messageInput.getElement().getNode().getFeature(ElementListenerMap.class)
                .fireEvent(new DomEvent(messageInput.getElement(), "submit",
                        eventData));
    }

    /**
     * The browser dispatches upload-abort while the file is still uploading,
     * and file-remove once it has been removed from the files.
     */
    private static void cancelUpload(Element element) {
        fireDomEvent(element, "upload-abort", true);
        fireDomEvent(element, "file-remove", false);
    }

    /**
     * Queues the tasks run with {@code UI.access} from now on, instead of
     * running them right away, so that the test can run them later.
     */
    private List<Command> queueUiAccessTasks() {
        var tasks = new ArrayList<Command>();
        Mockito.doAnswer(invocation -> {
            tasks.add(invocation.getArgument(0));
            return CompletableFuture.completedFuture(null);
        }).when(ui.getSession()).access(Mockito.any());
        return tasks;
    }

    /**
     * Runs the tasks run with {@code VaadinSession.access} with the session
     * locked, as a real session does, instead of running them on the calling
     * thread without the lock.
     */
    private void lockSessionInAccessTasks() {
        var session = ui.getSession();
        Mockito.doAnswer(invocation -> {
            session.lock();
            try {
                invocation.<Command> getArgument(0).execute();
            } finally {
                session.unlock();
            }
            return CompletableFuture.completedFuture(null);
        }).when(session).access(Mockito.any());
    }

    private static void fireDomEvent(Element element, String eventType,
            boolean uploading) {
        // Only the expression of the given element is sent, as for the
        // event data of a browser event
        var filesUploading = "vaadin-upload".equals(element.getTag())
                ? FILES_UPLOADING
                : MANAGER_FILES_UPLOADING;
        var eventData = JacksonUtils.createObjectNode()
                .put(filesUploading, uploading)
                .put("event.detail.file.name", "file.txt")
                .put("event.detail.fileName", "file.txt");
        element.getNode().getFeature(ElementListenerMap.class)
                .fireEvent(new DomEvent(element, eventType, eventData));
    }

    /**
     * A custom input that is a component.
     */
    private static class ComponentInput extends Div implements AIInput {
        private final List<SerializableConsumer<String>> submitListeners = new ArrayList<>();
        private final List<Boolean> submitDisabledValues = new ArrayList<>();
        private boolean submitDisabled;

        @Override
        public void addSubmitListener(SerializableConsumer<String> listener) {
            submitListeners.add(listener);
        }

        @Override
        public void setSubmitDisabled(boolean submitDisabled) {
            this.submitDisabled = submitDisabled;
            submitDisabledValues.add(submitDisabled);
        }

        @Override
        public boolean isSubmitDisabled() {
            return submitDisabled;
        }

        void submit(String value) {
            submitListeners.forEach(listener -> listener.accept(value));
        }
    }

    /**
     * A custom input that is not a component, but wraps one.
     */
    private static class WrappingInput implements AIInput {
        private final List<SerializableConsumer<String>> submitListeners = new ArrayList<>();
        private final MessageInput messageInput;

        WrappingInput(MessageInput messageInput) {
            this.messageInput = messageInput;
        }

        @Override
        public void addSubmitListener(SerializableConsumer<String> listener) {
            submitListeners.add(listener);
        }

        @Override
        public void setSubmitDisabled(boolean submitDisabled) {
            messageInput.setSubmitDisabled(submitDisabled);
        }

        @Override
        public boolean isSubmitDisabled() {
            return messageInput.isSubmitDisabled();
        }

        void submit(String value) {
            submitListeners.forEach(listener -> listener.accept(value));
        }
    }

    private static List<?> serializeAndDeserialize(List<?> objects)
            throws IOException, ClassNotFoundException {
        var bytes = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(bytes)) {
            out.writeObject(objects);
        }
        try (var in = new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray()))) {
            return (List<?>) in.readObject();
        }
    }
}
