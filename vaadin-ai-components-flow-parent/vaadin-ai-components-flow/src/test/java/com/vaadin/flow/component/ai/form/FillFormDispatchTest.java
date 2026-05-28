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
package com.vaadin.flow.component.ai.form;

import static com.vaadin.flow.component.ai.form.FormTestSupport.findTool;
import static com.vaadin.flow.component.ai.form.FormTestSupport.idOf;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;
import org.mockito.Mockito;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.form.FormTestFields.TestField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

import tools.jackson.databind.JsonNode;

/**
 * Tests for the dispatch logic in
 * {@code FormAIController.ToolCallbacks.executeFill}. Production has two entry
 * points:
 * <ul>
 * <li><b>Background thread</b> (LLM Reactor scheduler): caller does not hold
 * the session lock — {@code ui.access(...)} queues the command, the queue is
 * purged when the lock cycles, and {@code future.get()} returns.</li>
 * <li><b>UI thread already holding the lock</b> (e.g. a button-click handler
 * invoking the tool directly, or a synchronous LLM provider that runs on the UI
 * thread): {@code ui.access} from the lock-holder queues but cannot purge — the
 * queue is purged only on the ultimate unlock (hold count → 0), which never
 * happens while {@code future.get()} blocks. To avoid the deadlock the
 * controller detects "already on UI thread" via {@code session.hasLock()} and
 * runs the fill inline.</li>
 * </ul>
 *
 * <p>
 * {@code MockUIExtension}'s {@code AlwaysLockedVaadinSession} overrides
 * {@code access(Command)} to execute the command synchronously, which hides
 * both code paths. These tests construct a session that emulates production
 * access semantics (queue + purge-on-ultimate-unlock) so the dispatch fork is
 * observable.
 * </p>
 */
class FillFormDispatchTest {

    private DispatchTestSession session;
    private UI ui;

    @BeforeEach
    @SuppressWarnings("checkstyle:UiSetCurrentCheck")
    void setUp() {
        var service = Mockito.mock(VaadinService.class);
        var config = Mockito.mock(DeploymentConfiguration.class);
        Mockito.when(config.isProductionMode()).thenReturn(false);
        Mockito.when(service.getDeploymentConfiguration()).thenReturn(config);

        session = new DispatchTestSession(service);
        // UIInternals.setSession queries getConfiguration which checks the
        // session lock — must hold it during attach.
        session.lock();
        try {
            ui = new UI();
            ui.getInternals().setSession(session);
        } finally {
            session.unlock();
        }
        UI.setCurrent(ui);
        VaadinSession.setCurrent(session);
    }

    @AfterEach
    @SuppressWarnings("checkstyle:UiSetCurrentCheck")
    void tearDown() {
        UI.setCurrent(null);
        VaadinSession.setCurrent(null);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    void executeFill_fromThreadHoldingSessionLock_doesNotDeadlock() {
        // Regression: if executeFill goes through ui.access on a thread
        // already holding the session lock, the access task queues but
        // never purges (purge runs only on the ultimate unlock, and the
        // future.get() call blocks the lock holder from getting there).
        // Pin that the controller detects the in-lock case and runs the
        // fill inline.
        //
        // The whole test runs on a SEPARATE_THREAD so the lock acquisition
        // and the executeFill call happen on the same thread (which is the
        // production scenario). The @Timeout watchdog interrupts the
        // separate thread if it hangs.
        var field = new TestField();
        var form = new Div(field);

        session.lock();
        try {
            ui.add(form);
            var controller = new FormAIController(form);
            controller.onRequest();

            var args = buildArgs(field, "\"value\"");

            var response = findTool(controller.getTools(), "fill_form")
                    .execute(args);
            Assertions.assertTrue(isSuccess(response),
                    "Expected success, got: " + response);
            Assertions.assertEquals("value", field.getValue(),
                    "Inline dispatch must still apply the value to the field");
        } finally {
            session.unlock();
        }
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    void executeFill_fromUnlockedThread_routesThroughUIAccessAndCompletes() {
        // When the caller does NOT hold the lock and the lock is free,
        // ui.access purges the queue immediately on the ultimate unlock —
        // the access task runs synchronously inside the ui.access call and
        // future.get() returns. This is the typical Reactor-scheduler path
        // when the lock is uncontested at fill time.
        var field = new TestField();
        var form = new Div(field);

        session.lock();
        ui.add(form);
        var controller = new FormAIController(form);
        controller.onRequest();
        session.unlock();

        var args = buildArgs(field, "\"hello\"");

        var response = findTool(controller.getTools(), "fill_form")
                .execute(args);

        Assertions.assertTrue(isSuccess(response),
                "Expected success on uncontested-lock path; got: " + response);
        Assertions.assertEquals("hello", field.getValue());
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    void executeFill_onDetachedForm_returnsErrorString() {
        // The form is never attached to the UI. executeFill throws
        // IllegalStateException inside, which the tool layer wraps as a
        // generic "Error: fill failed." response string.
        var field = new TestField();
        var form = new Div(field); // not attached
        session.lock();
        try {
            var controller = new FormAIController(form);
            controller.onRequest();

            var args = buildArgs(field, "\"x\"");
            var response = findTool(controller.getTools(), "fill_form")
                    .execute(args);

            Assertions.assertTrue(response.contains("Error"),
                    "Detached-form fill must return an error string; got: "
                            + response);
        } finally {
            session.unlock();
        }
    }

    private static boolean isSuccess(String response) {
        try {
            var node = JacksonUtils.getMapper().readTree(response);
            return !node.path("rejected").iterator().hasNext();
        } catch (Exception ex) {
            throw new AssertionError("Response is not valid JSON: " + response,
                    ex);
        }
    }

    private static JsonNode buildArgs(HasValue<?, ?> field, String jsonValue) {
        try {
            var args = JacksonUtils.createObjectNode();
            args.putObject("values").set(idOf(field),
                    JacksonUtils.getMapper().readTree(jsonValue));
            return args;
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Test-only session that emulates the production access-queue + purge
     * semantics. The {@code access(Command)} override:
     * <ul>
     * <li>queues the command on a per-session FIFO,</li>
     * <li>uses {@code tryLock()} to attempt purging — succeeds for a free lock
     * OR for the current thread (re-entrant),</li>
     * <li>purges the queue only if the lock was acquired with hold-count
     * exactly 1 (the same condition as {@code VaadinSession.unlock} uses to
     * detect the ultimate-release moment),</li>
     * <li>otherwise unlocks one level and leaves the queue intact.</li>
     * </ul>
     * This mirrors {@code VaadinService.ensureAccessQueuePurged} +
     * {@code VaadinSession.unlock} closely enough that the dispatch fork in
     * {@code executeFill} is observable.
     */
    private static class DispatchTestSession extends VaadinSession {
        private final ReentrantLock lock = new ReentrantLock();
        private final Queue<Command> pendingQueue = new ConcurrentLinkedQueue<>();

        DispatchTestSession(VaadinService service) {
            super(service);
        }

        @Override
        public Lock getLockInstance() {
            return lock;
        }

        @Override
        public Future<Void> access(Command command) {
            var future = new CompletableFuture<Void>();
            pendingQueue.add(() -> {
                try {
                    command.execute();
                    future.complete(null);
                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                }
            });
            if (lock.tryLock()) {
                try {
                    if (lock.getHoldCount() == 1) {
                        Command pending;
                        while ((pending = pendingQueue.poll()) != null) {
                            try {
                                pending.execute();
                            } catch (Exception ignored) {
                                // suppress — production swallows in the
                                // pending-task runner too.
                            }
                        }
                    }
                } finally {
                    lock.unlock();
                }
            }
            return future;
        }
    }
}
