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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.AIComponentsFeatureFlagProvider;
import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.ui.AIFileReceiver;
import com.vaadin.flow.component.ai.ui.AIInput;
import com.vaadin.flow.component.ai.ui.AIMessage;
import com.vaadin.flow.component.ai.ui.AIMessageList;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

import reactor.core.publisher.Flux;

/**
 * Tests for {@link RequestInterceptor}: the content handed to it, the effect of
 * replacing the message text and attachments, the reject / blank / throw paths
 * that cancel a prompt before it has any effect, and the postponement
 * continuation (proceed / fail / timeout, completion races, the busy guard
 * across the postponed gap, and UI detach).
 */
class RequestInterceptorTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();
    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            AIComponentsFeatureFlagProvider.AI_COMPONENTS);

    private LLMProvider mockProvider;
    private AIMessageList mockMessageList;

    @BeforeEach
    void setup() {
        mockProvider = Mockito.mock(LLMProvider.class);
        mockMessageList = Mockito.mock(AIMessageList.class);
        stubAddMessage();
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.just("Response"));
    }

    @Test
    void interceptor_receivesOriginalMessageAndAttachments() {
        var seen = new AtomicReference<RequestInterceptor.RequestInterceptEvent>();
        var attachment = createAttachment("receipt.png");
        var orchestrator = orchestratorWith(seen::set);

        orchestrator.prompt("Hello", List.of(attachment));

        Assertions.assertEquals("Hello", seen.get().getUserMessage());
        Assertions.assertEquals(List.of(attachment),
                seen.get().getAttachments());
    }

    @Test
    void interceptor_receivesAttachmentsFromFileReceiver() {
        var seen = new AtomicReference<RequestInterceptor.RequestInterceptEvent>();
        var attachment = createAttachment("upload.png");
        var fileReceiver = Mockito.mock(AIFileReceiver.class);
        Mockito.when(fileReceiver.takeAttachments())
                .thenReturn(List.of(attachment));
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withFileReceiver(fileReceiver)
                .withRequestInterceptor(seen::set).build();

        orchestrator.prompt("Hello");

        Assertions.assertEquals(List.of(attachment),
                seen.get().getAttachments());
    }

    @Test
    void blankOriginalMessage_doesNotInvokeInterceptor() {
        var seen = new AtomicReference<RequestInterceptor.RequestInterceptEvent>();
        var orchestrator = orchestratorWith(seen::set);

        orchestrator.prompt("   ");

        Assertions.assertNull(seen.get());
    }

    @Test
    void nullOriginalMessage_doesNotInvokeInterceptor() {
        var seen = new AtomicReference<RequestInterceptor.RequestInterceptEvent>();
        var orchestrator = orchestratorWith(seen::set);

        orchestrator.prompt(null);

        Assertions.assertNull(seen.get());
    }

    @Test
    void replacedMessage_isSentToProviderInsteadOfOriginal() {
        var orchestrator = orchestratorWith(
                event -> event.setUserMessage("sanitized"));

        orchestrator.prompt("original with SSN 123-45-6789");

        var request = captureRequest();
        Assertions.assertEquals("sanitized", request.userMessage());
    }

    @Test
    void replacedMessage_isShownInMessageListAndHistory() {
        var orchestrator = orchestratorWith(
                event -> event.setUserMessage("sanitized"));

        orchestrator.prompt("original");

        Mockito.verify(mockMessageList).addMessage(Mockito.eq("sanitized"),
                Mockito.eq("You"), Mockito.anyList());
        Mockito.verify(mockMessageList, Mockito.never()).addMessage(
                Mockito.eq("original"), Mockito.anyString(), Mockito.anyList());
        Assertions.assertEquals("sanitized",
                orchestrator.getHistory().getFirst().content());
    }

    @Test
    void replacedMessage_isReportedToRequestListener() {
        var listener = Mockito.mock(RequestListener.class);
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withRequestInterceptor(
                        event -> event.setUserMessage("sanitized"))
                .withRequestListener(listener).build();

        orchestrator.prompt("original");

        var captor = ArgumentCaptor
                .forClass(RequestListener.RequestEvent.class);
        Mockito.verify(listener).onRequest(captor.capture());
        Assertions.assertEquals("sanitized",
                captor.getValue().getUserMessage());
    }

    @Test
    void replacedAttachments_areSentToProviderInsteadOfOriginals() {
        var converted = createAttachment("converted.png");
        var orchestrator = orchestratorWith(
                event -> event.setAttachments(List.of(converted)));

        orchestrator.prompt("Convert this",
                List.of(createAttachment("original.heic")));

        var request = captureRequest();
        Assertions.assertEquals(List.of(converted), request.attachments());
    }

    @Test
    void replacedAttachments_areShownInMessageList() {
        var converted = createAttachment("converted.png");
        var orchestrator = orchestratorWith(
                event -> event.setAttachments(List.of(converted)));

        orchestrator.prompt("Convert this",
                List.of(createAttachment("original.heic")));

        Mockito.verify(mockMessageList).addMessage(Mockito.eq("Convert this"),
                Mockito.eq("You"), Mockito.eq(List.of(converted)));
    }

    @Test
    void rejectedPrompt_stillDrainsFileReceiver() {
        var fileReceiver = Mockito.mock(AIFileReceiver.class);
        Mockito.when(fileReceiver.takeAttachments())
                .thenReturn(List.of(createAttachment("upload.png")));
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withFileReceiver(fileReceiver)
                .withRequestInterceptor(
                        RequestInterceptor.RequestInterceptEvent::reject)
                .build();

        orchestrator.prompt("Hello");

        Mockito.verify(fileReceiver).takeAttachments();
    }

    @Test
    void strippedAttachments_sendPromptWithoutAttachments() {
        var orchestrator = orchestratorWith(
                event -> event.setAttachments(List.of()));

        orchestrator.prompt("Hello", List.of(createAttachment("secret.pdf")));

        var request = captureRequest();
        Assertions.assertTrue(request.attachments().isEmpty());
    }

    @Test
    void rejectedPrompt_sendsNothingAndLeavesNoTrace() {
        var listener = Mockito.mock(RequestListener.class);
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withRequestInterceptor(
                        RequestInterceptor.RequestInterceptEvent::reject)
                .withRequestListener(listener).build();

        orchestrator.prompt("Hello");

        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
        Mockito.verify(mockMessageList, Mockito.never()).addMessage(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyList());
        Mockito.verify(listener, Mockito.never())
                .onRequest(Mockito.any(RequestListener.RequestEvent.class));
        Assertions.assertTrue(orchestrator.getHistory().isEmpty());
    }

    @Test
    void rejectedPrompt_allowsNextPromptToRun() {
        var rejectFirst = new AtomicReference<>(true);
        var orchestrator = orchestratorWith(event -> {
            if (rejectFirst.getAndSet(false)) {
                event.reject();
            }
        });

        orchestrator.prompt("first");
        orchestrator.prompt("second");

        var request = captureRequest();
        Assertions.assertEquals("second", request.userMessage());
    }

    @Test
    void rejectWithMessage_showsOriginalPromptAndReasonWithoutHistoryEntry() {
        var attachment = createAttachment("big.pdf");
        var orchestrator = orchestratorWith(
                event -> event.reject("Files over 10 MB are not accepted."));

        orchestrator.prompt("Hello", List.of(attachment));

        var inOrder = Mockito.inOrder(mockMessageList);
        inOrder.verify(mockMessageList).addMessage(Mockito.eq("Hello"),
                Mockito.eq("You"), Mockito.eq(List.of(attachment)));
        inOrder.verify(mockMessageList).addMessage(
                Mockito.eq("Files over 10 MB are not accepted."),
                Mockito.eq("Assistant"), Mockito.eq(Collections.emptyList()));
        Assertions.assertTrue(orchestrator.getHistory().isEmpty());
    }

    @Test
    void rejectWithMessage_showsOriginalContentNotReplacements() {
        var original = createAttachment("original.png");
        var orchestrator = orchestratorWith(event -> {
            event.setUserMessage("sanitized");
            event.setAttachments(List.of(createAttachment("converted.png")));
            event.reject("Not accepted.");
        });

        orchestrator.prompt("Hello", List.of(original));

        Mockito.verify(mockMessageList).addMessage(Mockito.eq("Hello"),
                Mockito.eq("You"), Mockito.eq(List.of(original)));
        Mockito.verify(mockMessageList, Mockito.never()).addMessage(
                Mockito.eq("sanitized"), Mockito.anyString(),
                Mockito.anyList());
        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void rejectWithMessage_usesConfiguredAssistantName() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withAssistantName("Support")
                .withRequestInterceptor(event -> event.reject("Not allowed."))
                .build();

        orchestrator.prompt("Hello");

        Mockito.verify(mockMessageList).addMessage(Mockito.eq("Not allowed."),
                Mockito.eq("Support"), Mockito.eq(Collections.emptyList()));
    }

    @Test
    void rejectWithMessage_withoutMessageList_sendsNothing() {
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withRequestInterceptor(event -> event.reject("Not allowed."))
                .build();

        orchestrator.prompt("Hello");

        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void rejectionIsFinal_laterContentChangesDoNotUndoIt() {
        var orchestrator = orchestratorWith(event -> {
            event.reject();
            event.setUserMessage("changed after reject");
        });

        orchestrator.prompt("Hello");

        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void blankReplacementMessage_dropsPromptAndAllowsNextPrompt() {
        var blankFirst = new AtomicReference<>(true);
        var orchestrator = orchestratorWith(event -> {
            if (blankFirst.getAndSet(false)) {
                event.setUserMessage("   ");
            }
        });

        orchestrator.prompt("first");
        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
        Assertions.assertTrue(orchestrator.getHistory().isEmpty());

        orchestrator.prompt("second");
        var request = captureRequest();
        Assertions.assertEquals("second", request.userMessage());
    }

    @Test
    void throwingInterceptor_abortsPromptAndReportsError() {
        var thrown = new RuntimeException("validation infrastructure down");
        var responseListener = Mockito.mock(ResponseListener.class);
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withRequestInterceptor(event -> {
                    throw thrown;
                }).withResponseListener(responseListener).build();

        var caught = Assertions.assertThrows(RuntimeException.class,
                () -> orchestrator.prompt("Hello"));

        Assertions.assertSame(thrown, caught);
        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
        Assertions.assertTrue(orchestrator.getHistory().isEmpty());
        var captor = ArgumentCaptor
                .forClass(ResponseListener.ResponseEvent.class);
        Mockito.verify(responseListener).onResponse(captor.capture());
        Assertions.assertSame(thrown,
                captor.getValue().getError().orElseThrow());
    }

    @Test
    void throwingInterceptor_firesControllerOnResponseWithError() {
        var thrown = new RuntimeException("validation infrastructure down");
        var controller = Mockito.mock(AIController.class);
        Mockito.when(controller.getTools()).thenReturn(List.of());
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withRequestInterceptor(event -> {
                    throw thrown;
                }).withController(controller).build();

        Assertions.assertThrows(RuntimeException.class,
                () -> orchestrator.prompt("Hello"));

        Mockito.verify(controller, Mockito.never()).onRequest();
        Mockito.verify(controller).onResponse(thrown);
    }

    @Test
    void throwingInterceptor_allowsNextPromptToRun() {
        var throwFirst = new AtomicReference<>(true);
        var orchestrator = orchestratorWith(event -> {
            if (throwFirst.getAndSet(false)) {
                throw new RuntimeException("boom");
            }
        });

        Assertions.assertThrows(RuntimeException.class,
                () -> orchestrator.prompt("first"));
        orchestrator.prompt("second");

        var request = captureRequest();
        Assertions.assertEquals("second", request.userMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void inputSubmit_goesThroughInterceptor() {
        var mockInput = Mockito.mock(AIInput.class);
        AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withInput(mockInput)
                .withRequestInterceptor(
                        event -> event.setUserMessage("sanitized"))
                .build();
        var captor = ArgumentCaptor.forClass(SerializableConsumer.class);
        Mockito.verify(mockInput).addSubmitListener(captor.capture());

        captor.getValue().accept("typed into the input");

        var request = captureRequest();
        Assertions.assertEquals("sanitized", request.userMessage());
    }

    @Test
    void rejectedPrompt_doesNotInvokeControllerOnRequest() {
        var controller = Mockito.mock(AIController.class);
        Mockito.when(controller.getTools()).thenReturn(List.of());
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withRequestInterceptor(
                        RequestInterceptor.RequestInterceptEvent::reject)
                .withController(controller).build();

        orchestrator.prompt("Hello");

        Mockito.verify(controller, Mockito.never()).onRequest();
    }

    @Test
    void interceptor_runsBeforeControllerOnRequest() {
        var interceptor = Mockito.mock(RequestInterceptor.class);
        var controller = Mockito.mock(AIController.class);
        Mockito.when(controller.getTools()).thenReturn(List.of());
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withRequestInterceptor(interceptor).withController(controller)
                .build();

        orchestrator.prompt("Hello");

        var inOrder = Mockito.inOrder(interceptor, controller);
        inOrder.verify(interceptor).intercept(
                Mockito.any(RequestInterceptor.RequestInterceptEvent.class));
        inOrder.verify(controller).onRequest();
    }

    @Test
    void postponedPrompt_sendsNothingUntilProceed() {
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var orchestrator = orchestratorWith(event -> continuation
                .set(event.postpone(Duration.ofMinutes(1))));

        orchestrator.prompt("Hello");

        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
        Mockito.verify(mockMessageList, Mockito.never()).addMessage(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyList());

        continuation.get().proceed();

        var request = captureRequest();
        Assertions.assertEquals("Hello", request.userMessage());
        Mockito.verify(mockMessageList).addMessage(Mockito.eq("Hello"),
                Mockito.eq("You"), Mockito.anyList());
    }

    @Test
    void postponedPrompt_backgroundExecution_resumedTurnRunsOffCallerThread()
            throws Exception {
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var subscribeThread = new AtomicReference<Thread>();
        var subscribed = new CountDownLatch(1);
        Mockito.when(
                mockProvider.stream(Mockito.any(LLMProvider.LLMRequest.class)))
                .thenReturn(Flux.defer(() -> {
                    subscribeThread.set(Thread.currentThread());
                    subscribed.countDown();
                    return Flux.just("Response");
                }));
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withRequestInterceptor(event -> continuation
                        .set(event.postpone(Duration.ofMinutes(1))))
                .withBackgroundExecution().build();

        orchestrator.prompt("Hello");
        continuation.get().proceed();

        Assertions.assertTrue(subscribed.await(5, TimeUnit.SECONDS),
                "Provider stream was never subscribed");
        Assertions.assertNotSame(Thread.currentThread(), subscribeThread.get(),
                "A resumed postponed prompt must still run in the background");
    }

    @Test
    void proceed_appliesChangesMadeAfterInterceptReturned() {
        var seen = new AtomicReference<RequestInterceptor.RequestInterceptEvent>();
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var converted = createAttachment("converted.png");
        var orchestrator = orchestratorWith(event -> {
            seen.set(event);
            continuation.set(event.postpone(Duration.ofMinutes(1)));
        });

        orchestrator.prompt("Convert this",
                List.of(createAttachment("original.heic")));
        seen.get().setUserMessage("Converted for you");
        seen.get().setAttachments(List.of(converted));
        continuation.get().proceed();

        var request = captureRequest();
        Assertions.assertEquals("Converted for you", request.userMessage());
        Assertions.assertEquals(List.of(converted), request.attachments());
    }

    @Test
    void postponedPrompt_failReportsErrorAndAllowsNextPrompt() {
        var thrown = new RuntimeException("conversion failed");
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var postponeFirst = new AtomicReference<>(true);
        var responseListener = Mockito.mock(ResponseListener.class);
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withRequestInterceptor(event -> {
                    if (postponeFirst.getAndSet(false)) {
                        continuation.set(event.postpone(Duration.ofMinutes(1)));
                    }
                }).withResponseListener(responseListener).build();

        orchestrator.prompt("first");
        continuation.get().fail(thrown);

        var captor = ArgumentCaptor
                .forClass(ResponseListener.ResponseEvent.class);
        Mockito.verify(responseListener).onResponse(captor.capture());
        Assertions.assertSame(thrown,
                captor.getValue().getError().orElseThrow());
        Assertions.assertTrue(orchestrator.getHistory().isEmpty());

        orchestrator.prompt("second");
        var request = captureRequest();
        Assertions.assertEquals("second", request.userMessage());
    }

    @Test
    void postponedPrompt_timeoutFailsTurn() throws Exception {
        var errorReported = new CountDownLatch(1);
        var reportedError = new AtomicReference<Throwable>();
        var postponeFirst = new AtomicReference<>(true);
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withRequestInterceptor(event -> {
                    if (postponeFirst.getAndSet(false)) {
                        event.postpone(Duration.ofMillis(50));
                    }
                }).withResponseListener(event -> {
                    event.getError().ifPresent(reportedError::set);
                    errorReported.countDown();
                }).build();

        orchestrator.prompt("first");

        Assertions.assertTrue(errorReported.await(5, TimeUnit.SECONDS),
                "Timeout must fail the turn");
        Assertions.assertInstanceOf(TimeoutException.class,
                reportedError.get());
        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));

        orchestrator.prompt("second");
        var request = captureRequest();
        Assertions.assertEquals("second", request.userMessage());
    }

    @Test
    void proceedAfterTimeout_isIgnored() throws Exception {
        var timedOut = new CountDownLatch(1);
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withRequestInterceptor(event -> continuation
                        .set(event.postpone(Duration.ofMillis(50))))
                .withResponseListener(event -> timedOut.countDown()).build();

        orchestrator.prompt("Hello");
        Assertions.assertTrue(timedOut.await(5, TimeUnit.SECONDS));

        continuation.get().proceed();

        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void failAfterProceed_isIgnored() {
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var responseEvents = new ArrayList<ResponseListener.ResponseEvent>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withRequestInterceptor(event -> continuation
                        .set(event.postpone(Duration.ofMinutes(1))))
                .withResponseListener(responseEvents::add).build();

        orchestrator.prompt("Hello");
        continuation.get().proceed();
        continuation.get().fail(new RuntimeException("too late"));

        Assertions.assertEquals(1, responseEvents.size());
        Assertions.assertTrue(responseEvents.getFirst().getError().isEmpty());
    }

    @Test
    void proceedAfterFail_isIgnored() {
        var boom = new RuntimeException("conversion failed");
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var responseEvents = new ArrayList<ResponseListener.ResponseEvent>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withRequestInterceptor(event -> continuation
                        .set(event.postpone(Duration.ofMinutes(1))))
                .withResponseListener(responseEvents::add).build();

        orchestrator.prompt("Hello");
        continuation.get().fail(boom);
        continuation.get().proceed();

        // The failed turn must not be resurrected by the late proceed().
        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
        Assertions.assertEquals(1, responseEvents.size());
        Assertions.assertSame(boom,
                responseEvents.getFirst().getError().orElseThrow());
    }

    @Test
    void secondProceed_isIgnored() {
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var responseEvents = new ArrayList<ResponseListener.ResponseEvent>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withRequestInterceptor(event -> continuation
                        .set(event.postpone(Duration.ofMinutes(1))))
                .withResponseListener(responseEvents::add).build();

        orchestrator.prompt("Hello");
        continuation.get().proceed();
        continuation.get().proceed();

        // captureRequest verifies exactly one provider call; a second turn
        // started by the repeated proceed() would fail it.
        var request = captureRequest();
        Assertions.assertEquals("Hello", request.userMessage());
        Assertions.assertEquals(1, responseEvents.size());
    }

    @Test
    void rejectBeforeProceed_appliesRejectionAtResume() {
        var seen = new AtomicReference<RequestInterceptor.RequestInterceptEvent>();
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var orchestrator = orchestratorWith(event -> {
            seen.set(event);
            continuation.set(event.postpone(Duration.ofMinutes(1)));
        });

        orchestrator.prompt("Hello");
        seen.get().reject("Conversion is not possible for this file.");
        continuation.get().proceed();

        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
        Mockito.verify(mockMessageList).addMessage(Mockito.eq("Hello"),
                Mockito.eq("You"), Mockito.anyList());
        Mockito.verify(mockMessageList).addMessage(
                Mockito.eq("Conversion is not possible for this file."),
                Mockito.eq("Assistant"), Mockito.eq(Collections.emptyList()));
        Assertions.assertTrue(orchestrator.getHistory().isEmpty());
    }

    @Test
    void rejectedAtResume_allowsNextPromptToRun() {
        var seen = new AtomicReference<RequestInterceptor.RequestInterceptEvent>();
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var postponeFirst = new AtomicReference<>(true);
        var orchestrator = orchestratorWith(event -> {
            if (postponeFirst.getAndSet(false)) {
                seen.set(event);
                continuation.set(event.postpone(Duration.ofMinutes(1)));
            }
        });

        orchestrator.prompt("first");
        seen.get().reject();
        continuation.get().proceed();

        orchestrator.prompt("second");
        var request = captureRequest();
        Assertions.assertEquals("second", request.userMessage());
    }

    @Test
    void failingResume_reportsErrorAndAllowsNextPrompt() {
        var boom = new RuntimeException("display failed");
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var postponeFirst = new AtomicReference<>(true);
        var responseEvents = new ArrayList<ResponseListener.ResponseEvent>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withRequestInterceptor(event -> {
                    if (postponeFirst.getAndSet(false)) {
                        continuation.set(event.postpone(Duration.ofMinutes(1)));
                    }
                }).withResponseListener(responseEvents::add).build();

        orchestrator.prompt("first");
        // Fail only the resume's display call; later calls succeed again.
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList())).thenThrow(boom)
                .thenReturn(null);
        try {
            continuation.get().proceed();
        } catch (RuntimeException expected) {
            // In production the rethrown failure goes to the session error
            // handler; whether it reaches this caller is a harness detail.
        }

        Assertions.assertEquals(1, responseEvents.size());
        Assertions.assertSame(boom,
                responseEvents.getFirst().getError().orElseThrow());

        orchestrator.prompt("second");
        var request = captureRequest();
        Assertions.assertEquals("second", request.userMessage());
    }

    @Test
    void blankReplacementAtResume_dropsPromptAndAllowsNextPrompt() {
        var seen = new AtomicReference<RequestInterceptor.RequestInterceptEvent>();
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var postponeFirst = new AtomicReference<>(true);
        var orchestrator = orchestratorWith(event -> {
            if (postponeFirst.getAndSet(false)) {
                seen.set(event);
                continuation.set(event.postpone(Duration.ofMinutes(1)));
            }
        });

        orchestrator.prompt("first");
        seen.get().setUserMessage("   ");
        continuation.get().proceed();

        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
        Mockito.verify(mockMessageList, Mockito.never()).addMessage(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyList());

        orchestrator.prompt("second");
        var request = captureRequest();
        Assertions.assertEquals("second", request.userMessage());
    }

    @Test
    void changesAfterCompletion_throwIllegalStateException() {
        var seen = new AtomicReference<RequestInterceptor.RequestInterceptEvent>();
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var orchestrator = orchestratorWith(event -> {
            seen.set(event);
            continuation.set(event.postpone(Duration.ofMinutes(1)));
        });

        orchestrator.prompt("Hello");
        continuation.get().proceed();

        var event = seen.get();
        Assertions.assertThrows(IllegalStateException.class,
                () -> event.setUserMessage("late"));
        Assertions.assertThrows(IllegalStateException.class,
                () -> event.setAttachments(List.of()));
        Assertions.assertThrows(IllegalStateException.class, event::reject);
        Assertions.assertThrows(IllegalStateException.class,
                () -> event.reject("late"));
    }

    @Test
    void throwAfterPostpone_abortsPromptAndMakesContinuationInert() {
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var throwFirst = new AtomicReference<>(true);
        var orchestrator = orchestratorWith(event -> {
            if (throwFirst.getAndSet(false)) {
                continuation.set(event.postpone(Duration.ofMinutes(1)));
                throw new RuntimeException("failed after postponing");
            }
        });

        Assertions.assertThrows(RuntimeException.class,
                () -> orchestrator.prompt("first"));

        // The throw aborted the prompt before the postponement was armed, so
        // completing the leftover continuation must not start a turn.
        continuation.get().proceed();
        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));

        orchestrator.prompt("second");
        var request = captureRequest();
        Assertions.assertEquals("second", request.userMessage());
    }

    @Test
    void secondPostpone_throwsIllegalStateException() {
        var orchestrator = orchestratorWith(event -> {
            event.postpone(Duration.ofMinutes(1));
            event.postpone(Duration.ofMinutes(1));
        });

        Assertions.assertThrows(IllegalStateException.class,
                () -> orchestrator.prompt("Hello"));

        Assertions.assertThrows(IllegalStateException.class,
                () -> orchestrator.prompt("second"),
                "The failed prompt must release the busy flag, so the next "
                        + "prompt reaches the interceptor again");
        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
    }

    @Test
    void busyGuard_ignoresPromptsWhilePostponed() {
        var invocations = new AtomicReference<>(0);
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var orchestrator = orchestratorWith(event -> {
            invocations.getAndUpdate(count -> count + 1);
            continuation.set(event.postpone(Duration.ofMinutes(1)));
        });

        orchestrator.prompt("first");
        orchestrator.prompt("second while postponed");

        Assertions.assertEquals(1, invocations.get());
        continuation.get().proceed();
        var request = captureRequest();
        Assertions.assertEquals("first", request.userMessage());
    }

    @Test
    void uiDetachBeforeProceed_abandonsPromptAndReleasesProcessing() {
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var postponeFirst = new AtomicReference<>(true);
        var orchestrator = orchestratorWith(event -> {
            if (postponeFirst.getAndSet(false)) {
                continuation.set(event.postpone(Duration.ofMinutes(1)));
            }
        });

        orchestrator.prompt("first");
        var currentUi = UI.getCurrent();
        var session = currentUi.getSession();
        // There is no public API for detaching a UI in a unit test;
        // clearing the session through the internals simulates it.
        currentUi.getInternals().setSession(null);
        try {
            continuation.get().proceed();
        } finally {
            currentUi.getInternals().setSession(session);
        }

        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
        orchestrator.prompt("second");
        var request = captureRequest();
        Assertions.assertEquals("second", request.userMessage());
    }

    @Test
    void uiDetachDuringResume_abandonsPromptAndReleasesProcessing()
            throws Exception {
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var postponeFirst = new AtomicReference<>(true);
        var orchestrator = orchestratorWith(event -> {
            if (postponeFirst.getAndSet(false)) {
                continuation.set(event.postpone(Duration.ofMinutes(1)));
            }
        });

        orchestrator.prompt("first");
        // The harness session runs ui.access on the calling thread, so the
        // worker's resume passes the pre-lock detach check and then parks
        // on the session lock the test thread holds.
        var worker = new Thread(() -> continuation.get().proceed());
        worker.start();
        var deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (worker.isAlive() && worker.getState() != Thread.State.WAITING
                && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }

        var currentUi = UI.getCurrent();
        var session = currentUi.getSession();
        // Detach the UI while the resume waits for the lock; on acquisition
        // the re-check must abandon the prompt and release the busy flag
        // instead of dropping it silently. There is no public API for
        // detaching a UI in a unit test; clearing the session through the
        // internals simulates it.
        currentUi.getInternals().setSession(null);
        try {
            session.unlock();
            worker.join(5000);
        } finally {
            session.lock();
            currentUi.getInternals().setSession(session);
        }

        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
        orchestrator.prompt("second");
        var request = captureRequest();
        Assertions.assertEquals("second", request.userMessage());
    }

    @Test
    void uiDetachBeforeFail_doesNotThrowAndReleasesProcessing() {
        var thrown = new RuntimeException("conversion failed");
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var postponeFirst = new AtomicReference<>(true);
        var controller = Mockito.mock(AIController.class);
        Mockito.when(controller.getTools()).thenReturn(List.of());
        var responseEvents = new ArrayList<ResponseListener.ResponseEvent>();
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withController(controller)
                .withRequestInterceptor(event -> {
                    if (postponeFirst.getAndSet(false)) {
                        continuation.set(event.postpone(Duration.ofMinutes(1)));
                    }
                }).withResponseListener(responseEvents::add).build();

        orchestrator.prompt("first");
        var currentUi = UI.getCurrent();
        var session = currentUi.getSession();
        // There is no public API for detaching a UI in a unit test;
        // clearing the session through the internals simulates it.
        currentUi.getInternals().setSession(null);
        try {
            // fail() is meant to be called from background threads, so it
            // must not throw even when the UI is already gone.
            Assertions
                    .assertDoesNotThrow(() -> continuation.get().fail(thrown));
        } finally {
            currentUi.getInternals().setSession(session);
        }

        Assertions.assertEquals(1, responseEvents.size());
        Assertions.assertSame(thrown,
                responseEvents.getFirst().getError().orElseThrow());
        // The controller hook needs a live UI, so it is skipped.
        Mockito.verify(controller, Mockito.never()).onResponse(Mockito.any());

        orchestrator.prompt("second");
        var request = captureRequest();
        Assertions.assertEquals("second", request.userMessage());
    }

    @Test
    void postpone_invalidTimeout_throws() {
        var event = new RequestInterceptor.RequestInterceptEvent("Hello",
                List.of());

        Assertions.assertThrows(NullPointerException.class,
                () -> event.postpone(null));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> event.postpone(Duration.ZERO));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> event.postpone(Duration.ofSeconds(-1)));
    }

    @Test
    void hugeTimeout_abortsPromptAndAllowsNextPrompt() {
        var postponeFirst = new AtomicReference<>(true);
        var orchestrator = orchestratorWith(event -> {
            if (postponeFirst.getAndSet(false)) {
                // Passes validation (positive) but overflows toMillis() when
                // the timeout timer is armed after the interceptor returns.
                event.postpone(Duration.ofSeconds(Long.MAX_VALUE));
            }
        });

        Assertions.assertThrows(ArithmeticException.class,
                () -> orchestrator.prompt("first"));

        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
        orchestrator.prompt("second");
        var request = captureRequest();
        Assertions.assertEquals("second", request.userMessage());
    }

    @Test
    void continuation_failWithNullCause_throwsNullPointerException() {
        var event = new RequestInterceptor.RequestInterceptEvent("Hello",
                List.of());
        var continuation = event.postpone(Duration.ofMinutes(1));

        Assertions.assertThrows(NullPointerException.class,
                () -> continuation.fail(null));
    }

    @Test
    void postponedPrompt_timeoutReportsErrorToController() throws Exception {
        var controllerNotified = new CountDownLatch(1);
        var controller = Mockito.mock(AIController.class);
        Mockito.when(controller.getTools()).thenReturn(List.of());
        Mockito.doAnswer(invocation -> {
            controllerNotified.countDown();
            return null;
        }).when(controller).onResponse(Mockito.any());
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withController(controller)
                .withRequestInterceptor(
                        event -> event.postpone(Duration.ofMillis(50)))
                .build();

        orchestrator.prompt("Hello");

        // The harness session runs ui.access on the calling thread, so the
        // timer thread blocks on the session lock the test thread holds;
        // releasing it and waiting for the controller's effect makes the
        // handoff deterministic.
        var session = UI.getCurrent().getSession();
        session.unlock();
        try {
            Assertions.assertTrue(controllerNotified.await(5, TimeUnit.SECONDS),
                    "The controller must hear about the timeout");
        } finally {
            session.lock();
        }

        Mockito.verify(controller)
                .onResponse(Mockito.isA(TimeoutException.class));
    }

    @Test
    void proceedInsideInterceptor_sendsPromptWithChanges() {
        var orchestrator = orchestratorWith(event -> {
            var continuation = event.postpone(Duration.ofMinutes(1));
            event.setUserMessage("processed inline");
            continuation.proceed();
        });

        orchestrator.prompt("Hello");

        var request = captureRequest();
        Assertions.assertEquals("processed inline", request.userMessage());
        Mockito.verify(mockMessageList).addMessage(
                Mockito.eq("processed inline"), Mockito.eq("You"),
                Mockito.anyList());
    }

    @Test
    void proceedBeforeTimeout_noLateFailure() throws Exception {
        var errorReported = new CountDownLatch(1);
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        // The timeout must be long enough that a CI stall between prompt()
        // and proceed() cannot beat it, and the observation window below
        // must still contain the timer's firing time — a timer firing
        // after proceed() is what this test watches for.
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withRequestInterceptor(event -> continuation
                        .set(event.postpone(Duration.ofMillis(500))))
                .withResponseListener(event -> event.getError()
                        .ifPresent(error -> errorReported.countDown()))
                .build();

        orchestrator.prompt("Hello");
        continuation.get().proceed();

        Assertions.assertFalse(errorReported.await(1500, TimeUnit.MILLISECONDS),
                "The timeout must not fail the turn after proceed()");
        var request = captureRequest();
        Assertions.assertEquals("Hello", request.userMessage());
    }

    @Test
    void proceed_fromBackgroundThread_resumesTurn() throws Exception {
        var seen = new AtomicReference<RequestInterceptor.RequestInterceptEvent>();
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var completed = new CountDownLatch(1);
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withRequestInterceptor(event -> {
                    seen.set(event);
                    continuation.set(event.postpone(Duration.ofMinutes(1)));
                }).withResponseListener(event -> completed.countDown()).build();

        orchestrator.prompt("Hello");
        var worker = new Thread(() -> {
            seen.get().setUserMessage("processed on a worker");
            continuation.get().proceed();
        });
        worker.start();

        // The harness session runs ui.access on the calling thread, so the
        // worker blocks on the session lock the test thread holds. Release
        // it and wait for the turn to complete on the worker — waiting
        // while unlocked is what makes the handoff deterministic.
        var session = UI.getCurrent().getSession();
        session.unlock();
        try {
            Assertions.assertTrue(completed.await(5, TimeUnit.SECONDS),
                    "The turn must complete after a background-thread "
                            + "proceed()");
        } finally {
            session.lock();
        }
        worker.join(5000);

        var request = captureRequest();
        Assertions.assertEquals("processed on a worker", request.userMessage());
    }

    @Test
    void failedPostponedPrompt_doesNotReturnDrainedAttachments() {
        var fileReceiver = Mockito.mock(AIFileReceiver.class);
        Mockito.when(fileReceiver.takeAttachments())
                .thenReturn(List.of(createAttachment("upload.png")))
                .thenReturn(List.of());
        var continuation = new AtomicReference<RequestInterceptor.RequestContinuation>();
        var postponeFirst = new AtomicReference<>(true);
        var orchestrator = AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList).withFileReceiver(fileReceiver)
                .withRequestInterceptor(event -> {
                    if (postponeFirst.getAndSet(false)) {
                        continuation.set(event.postpone(Duration.ofMinutes(1)));
                    }
                }).build();

        orchestrator.prompt("first");
        continuation.get().fail(new RuntimeException("conversion failed"));

        // Uploads are consumed at submit time; a failed turn does not put
        // them back, so the next prompt starts without them.
        Mockito.verify(mockProvider, Mockito.never())
                .stream(Mockito.any(LLMProvider.LLMRequest.class));
        orchestrator.prompt("second");
        var request = captureRequest();
        Assertions.assertEquals(List.of(), request.attachments());
        Mockito.verify(fileReceiver, Mockito.times(2)).takeAttachments();
    }

    @Test
    void event_getAttachments_returnsUnmodifiableList() {
        var event = new RequestInterceptor.RequestInterceptEvent("Hello",
                List.of(createAttachment("a.png")));

        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> event.getAttachments().clear());
    }

    @Test
    void event_nullArguments_throwNullPointerException() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new RequestInterceptor.RequestInterceptEvent(null,
                        List.of()));
        Assertions.assertThrows(NullPointerException.class,
                () -> new RequestInterceptor.RequestInterceptEvent("Hello",
                        null));
        Assertions.assertThrows(NullPointerException.class,
                () -> new RequestInterceptor.RequestInterceptEvent("Hello",
                        Collections.singletonList((AIAttachment) null)));

        var event = new RequestInterceptor.RequestInterceptEvent("Hello",
                List.of());
        Assertions.assertThrows(NullPointerException.class,
                () -> event.setUserMessage(null));
        Assertions.assertThrows(NullPointerException.class,
                () -> event.setAttachments(null));
        Assertions.assertThrows(NullPointerException.class,
                () -> event.setAttachments(
                        Collections.singletonList((AIAttachment) null)));
        Assertions.assertThrows(NullPointerException.class,
                () -> event.reject((String) null));
    }

    @Test
    void event_lastRejectionMessageWins() {
        var event = new RequestInterceptor.RequestInterceptEvent("Hello",
                List.of());

        event.reject("first reason");
        event.reject("second reason");

        Assertions.assertTrue(event.isRejected());
        Assertions.assertEquals("second reason", event.getRejectionMessage());
    }

    @Test
    void event_silentRejectFollowedByReason_keepsRejectionAndReason() {
        var event = new RequestInterceptor.RequestInterceptEvent("Hello",
                List.of());

        event.reject();
        event.reject("added reason");

        Assertions.assertTrue(event.isRejected());
        Assertions.assertEquals("added reason", event.getRejectionMessage());
    }

    @Test
    void event_reasonFollowedBySilentReject_keepsReason() {
        var event = new RequestInterceptor.RequestInterceptEvent("Hello",
                List.of());

        event.reject("the reason");
        event.reject();

        // A silent reject() confirms the rejection; it does not erase an
        // earlier reason.
        Assertions.assertTrue(event.isRejected());
        Assertions.assertEquals("the reason", event.getRejectionMessage());
    }

    private AIOrchestrator orchestratorWith(RequestInterceptor interceptor) {
        return AIOrchestrator.builder(mockProvider, null)
                .withMessageList(mockMessageList)
                .withRequestInterceptor(interceptor).build();
    }

    private LLMProvider.LLMRequest captureRequest() {
        var captor = ArgumentCaptor.forClass(LLMProvider.LLMRequest.class);
        Mockito.verify(mockProvider).stream(captor.capture());
        return captor.getValue();
    }

    private void stubAddMessage() {
        var message = Mockito.mock(AIMessage.class);
        Mockito.when(message.getText()).thenReturn("");
        Mockito.when(message.getTime()).thenReturn(Instant.now());
        Mockito.when(message.getUserName()).thenReturn("Test");
        Mockito.when(mockMessageList.addMessage(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList())).thenReturn(message);
    }

    private static AIAttachment createAttachment(String fileName) {
        return new AIAttachment(fileName, "text/plain", "test".getBytes());
    }
}
