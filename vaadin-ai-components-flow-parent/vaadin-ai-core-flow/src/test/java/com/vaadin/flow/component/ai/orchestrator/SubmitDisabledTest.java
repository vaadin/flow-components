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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
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
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

class SubmitDisabledTest {
    private static final byte[] CONTENT = "content"
            .getBytes(StandardCharsets.UTF_8);

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
    private int fileCount;

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

        receiveFile(uploadManagerConnector, () -> {
            continuation.get().fail(new RuntimeException("Failed"));
            Assertions.assertTrue(messageInput.isSubmitDisabled());
        });

        Assertions.assertFalse(messageInput.isSubmitDisabled());
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
    void fileReceived_submitDisabledByApplication_submitStaysDisabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(uploadManager).build();
        messageInput.setSubmitDisabled(true);

        receiveFile(uploadManagerConnector);

        Assertions.assertTrue(messageInput.isSubmitDisabled());
    }

    @Test
    void requestInterceptorRejectsAndDisablesSubmit_submitStaysDisabled() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput).withRequestInterceptor(event -> {
                    event.reject();
                    messageInput.setSubmitDisabled(true);
                }).build();

        orchestrator.prompt("Hello");

        Assertions.assertTrue(messageInput.isSubmitDisabled());
    }

    @Test
    void requestListenerDisablesSubmit_submitStaysDisabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withRequestListener(
                        event -> messageInput.setSubmitDisabled(true))
                .build();
        submitFromClient("Hello");

        response.tryEmitComplete();

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
    void uploadManagerReceivingFile_submitDisabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(uploadManager).build();

        receiveFile(uploadManagerConnector,
                () -> Assertions.assertTrue(messageInput.isSubmitDisabled()));
    }

    @Test
    void uploadManagerFileReceived_submitEnabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(uploadManager).build();

        receiveFile(uploadManagerConnector);

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void uploadManagerUploadCanceled_submitEnabledAndPromptAccepted() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput).withFileReceiver(uploadManager)
                .build();

        receiveCanceledFile(uploadManagerConnector);
        Assertions.assertFalse(messageInput.isSubmitDisabled());

        orchestrator.prompt("Hello");
        Mockito.verify(mockProvider)
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void uploadReceivingFile_submitDisabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(upload).build();

        receiveFile(upload.getElement(),
                () -> Assertions.assertTrue(messageInput.isSubmitDisabled()));
    }

    @Test
    void uploadFileReceived_submitEnabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(upload).build();

        receiveFile(upload.getElement());

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void uploadUploadCanceled_submitEnabledAndPromptAccepted() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput).withFileReceiver(upload).build();

        receiveCanceledFile(upload.getElement());
        Assertions.assertFalse(messageInput.isSubmitDisabled());

        orchestrator.prompt("Hello");
        Mockito.verify(mockProvider)
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void fileReceivedWhileAnotherReceiving_submitStaysDisabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(uploadManager).build();

        receiveFile(uploadManagerConnector, () -> {
            receiveFile(uploadManagerConnector);
            Assertions.assertTrue(messageInput.isSubmitDisabled());
        });

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void fileFailedAfterReceivedWhileAnotherReceiving_submitStaysDisabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(uploadManager).build();

        receiveFile(uploadManagerConnector, () -> {
            // Closing the request fails after the file has been received,
            // which reports the same file as failed after it has completed
            receive(uploadManagerConnector, new ByteArrayInputStream(CONTENT) {
                @Override
                public void close() throws IOException {
                    throw new IOException("Connection reset");
                }
            });
            Assertions.assertTrue(messageInput.isSubmitDisabled());
        });

        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void fileReceivedWhileResponseStreaming_submitStaysDisabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(uploadManager).build();
        submitFromClient("Hello");

        receiveFile(uploadManagerConnector);
        Assertions.assertTrue(messageInput.isSubmitDisabled());

        response.tryEmitComplete();
        Assertions.assertFalse(messageInput.isSubmitDisabled());
    }

    @Test
    void responseCompletedWhileUploading_submitStaysDisabled() {
        AIOrchestrator.builder(mockProvider, null).withInput(messageInput)
                .withFileReceiver(uploadManager).build();
        submitFromClient("Hello");

        receiveFile(uploadManagerConnector, () -> {
            response.tryEmitComplete();
            Assertions.assertTrue(messageInput.isSubmitDisabled());
        });

        Assertions.assertFalse(messageInput.isSubmitDisabled());
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
    void preserveOnRefresh_postponedPromptFailedWhileUploadingAfterRefresh_submitStaysDisabledUntilUploadFinished() {
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withInput(messageInput).withFileReceiver(uploadManager)
                .withRequestInterceptor(event -> continuation
                        .set(event.postpone(Duration.ofMinutes(1))))
                .build();
        orchestrator.prompt("Hello");

        refreshPreservingInput();
        receiveFile(uploadManagerConnector, () -> {
            continuation.get().fail(new RuntimeException("Failed"));
            Assertions.assertTrue(messageInput.isSubmitDisabled());
        });

        Assertions.assertFalse(messageInput.isSubmitDisabled());
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

    private void receiveFile(Element owner) {
        receiveFile(owner, () -> {
            // Nothing to do while the file is being received
        });
    }

    /**
     * Uploads a file to the upload handler of the given element, as the browser
     * does, and runs the given action while the server is receiving the file.
     */
    private void receiveFile(Element owner, Runnable whileReceiving) {
        var received = new AtomicBoolean();
        receive(owner, new ByteArrayInputStream(CONTENT) {
            @Override
            public synchronized int read(byte[] buffer, int offset,
                    int length) {
                if (!received.getAndSet(true)) {
                    whileReceiving.run();
                }
                return super.read(buffer, offset, length);
            }
        });
        Assertions.assertTrue(received.get(), "The file was not received");
    }

    /**
     * Uploads a file to the upload handler of the given element, and cancels it
     * in the browser while the server is receiving it.
     */
    private void receiveCanceledFile(Element owner) {
        receive(owner, new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Connection reset");
            }
        });
    }

    private void receive(Element owner, InputStream content) {
        var request = Mockito.mock(VaadinRequest.class);
        Mockito.when(request.getHeader("X-Filename"))
                .thenReturn("file" + (++fileCount) + ".txt");
        try {
            Mockito.when(request.getInputStream()).thenReturn(content);
            getUploadHandler(owner).handleRequest(request,
                    Mockito.mock(VaadinResponse.class), ui.getSession(), owner);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private UploadHandler getUploadHandler(Element owner) {
        ui.fakeClientCommunication();
        var target = URI.create(owner.getAttribute("target"));
        var resource = ui.getSession().getResourceRegistry().getResource(target)
                .orElseThrow();
        return (UploadHandler) ((StreamResourceRegistry.ElementStreamResource) resource)
                .getElementRequestHandler();
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
