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
package com.vaadin.flow.component.ai.provider;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.tests.MockUIExtension;

import reactor.core.publisher.Flux;

class BackgroundExecutionTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private BackgroundExecution backgroundExecution;

    private final TestLogger logger = TestLoggerFactory
            .getTestLogger(BackgroundExecutionTest.class);

    @BeforeEach
    void setup() {
        backgroundExecution = new BackgroundExecution(
                BackgroundExecutionTest.class);
        logger.clearAll();
    }

    @Test
    void isEnabled_defaultsToFalse() {
        Assertions.assertFalse(backgroundExecution.isEnabled());
    }

    @Test
    void applyToStreamingResponse_returnsSourceUnchanged() {
        var source = Flux.just("Hello");
        backgroundExecution.setEnabled(true);

        Assertions.assertSame(source,
                backgroundExecution.applyToStreamingResponse(source),
                "A streaming response already arrives asynchronously and must "
                        + "not be rescheduled");
    }

    @Test
    void applyToBlockingResponse_whenDisabled_returnsSourceUnchanged() {
        var source = Flux.just("Hello");

        Assertions.assertSame(source,
                backgroundExecution.applyToBlockingResponse(source),
                "The default must leave the response on the subscribing "
                        + "thread");
    }

    @Test
    void applyToBlockingResponse_whenDisabled_subscribesOnSubscribingThread() {
        var subscribeThread = captureSubscribeThread();

        backgroundExecution.applyToBlockingResponse(subscribeThread.source())
                .blockLast();

        Assertions.assertSame(Thread.currentThread(),
                subscribeThread.thread().get());
    }

    @Test
    void applyToBlockingResponse_whenEnabled_subscribesOffSubscribingThread() {
        backgroundExecution.setEnabled(true);
        var subscribeThread = captureSubscribeThread();

        backgroundExecution.applyToBlockingResponse(subscribeThread.source())
                .blockLast();

        Assertions.assertNotSame(Thread.currentThread(),
                subscribeThread.thread().get());
    }

    @Test
    void applyToBlockingResponse_whenDisabledAndPushDisabled_doesNotWarn() {
        ui.getUI().getPushConfiguration().setPushMode(PushMode.DISABLED);

        backgroundExecution.applyToBlockingResponse(Flux.just("Hello"))
                .blockLast();

        Assertions.assertEquals(0, deliveryWarningCount(),
                "A synchronous turn completes within the request");
    }

    @Test
    void applyToBlockingResponse_whenEnabledAndPushDisabled_warns() {
        ui.getUI().getPushConfiguration().setPushMode(PushMode.DISABLED);
        backgroundExecution.setEnabled(true);

        backgroundExecution.applyToBlockingResponse(Flux.just("Hello"))
                .blockLast();

        Assertions.assertEquals(1, deliveryWarningCount());
    }

    @Test
    void applyToBlockingResponse_whenEnabledAndManualPush_warns() {
        Mockito.when(ui.getService().ensurePushAvailable()).thenReturn(true);
        ui.getUI().getPushConfiguration().setPushMode(PushMode.MANUAL);
        backgroundExecution.setEnabled(true);

        backgroundExecution.applyToBlockingResponse(Flux.just("Hello"))
                .blockLast();

        Assertions.assertEquals(1, deliveryWarningCount(),
                "Nothing calls ui.push() for the application, so manual push "
                        + "does not deliver the response");
    }

    @Test
    void applyToBlockingResponse_whenEnabledAndPollingEnabled_doesNotWarn() {
        ui.getUI().getPushConfiguration().setPushMode(PushMode.DISABLED);
        ui.getUI().setPollInterval(500);
        backgroundExecution.setEnabled(true);

        backgroundExecution.applyToBlockingResponse(Flux.just("Hello"))
                .blockLast();

        Assertions.assertEquals(0, deliveryWarningCount(),
                "Polling delivers the response");
    }

    @Test
    void applyToBlockingResponse_repeatedTurns_warnsOnce() {
        ui.getUI().getPushConfiguration().setPushMode(PushMode.DISABLED);
        backgroundExecution.setEnabled(true);

        backgroundExecution.applyToBlockingResponse(Flux.just("Hello"))
                .blockLast();
        backgroundExecution.applyToBlockingResponse(Flux.just("Hello again"))
                .blockLast();

        Assertions.assertEquals(1, deliveryWarningCount());
    }

    /**
     * A response that records the thread it is subscribed on, standing in for a
     * provider that performs its blocking LLM call at subscription time.
     */
    private record SubscribeThreadProbe(Flux<String> source,
            AtomicReference<Thread> thread) {
    }

    private static SubscribeThreadProbe captureSubscribeThread() {
        var thread = new AtomicReference<Thread>();
        var source = Flux.defer(() -> {
            thread.set(Thread.currentThread());
            return Flux.just("Hello");
        });
        return new SubscribeThreadProbe(source, thread);
    }

    private long deliveryWarningCount() {
        return logger.getLoggingEvents().stream()
                .filter(event -> event.getMessage()
                        .contains("neither automatic push nor polling"))
                .count();
    }
}
