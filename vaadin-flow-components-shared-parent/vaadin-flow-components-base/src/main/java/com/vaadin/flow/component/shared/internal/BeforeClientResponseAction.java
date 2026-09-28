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
package com.vaadin.flow.component.shared.internal;

import java.io.Serializable;
import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.internal.ExecutionContext;
import com.vaadin.flow.shared.Registration;

/**
 * Runs an action for a component once before the next client response.
 * <p>
 * Use this to defer an update to the end of the round trip, for example when
 * several changes in the same round trip affect the same client-side state and
 * the update should only run once, with the final state. Scheduling the action
 * again before it has run has no effect, and you can cancel a scheduled action.
 * <p>
 * Compared to registering the action directly with
 * {@link UI#beforeClientResponse}, the action also runs if the component is
 * detached and attached again before the response, also to a different UI.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 *
 * @since 25.4
 */
public class BeforeClientResponseAction implements Serializable {

    private final Component component;
    private final SerializableRunnable action;
    private ScheduledRun scheduledRun;

    /**
     * Creates a new action for the given component. The action is not scheduled
     * until you call {@link #schedule()}.
     * <p>
     * If the action is a lambda or a method reference, do not store it in
     * another field and do not capture it in another lambda. Otherwise the
     * component can fail to deserialize.
     *
     * @param component
     *            the component whose client response the action runs before,
     *            not {@code null}
     * @param action
     *            the action to run, not {@code null}
     */
    public BeforeClientResponseAction(Component component,
            SerializableRunnable action) {
        this.component = Objects.requireNonNull(component,
                "Component must not be null");
        this.action = Objects.requireNonNull(action, "Action must not be null");
    }

    /**
     * Schedules the action to run before the next client response while the
     * component is attached. If the component is not attached, the action runs
     * before the first response after the component has been attached. Does
     * nothing if the action is already scheduled.
     * <p>
     * The action only runs while the component is attached.
     */
    public void schedule() {
        if (scheduledRun != null) {
            return;
        }
        scheduledRun = new ScheduledRun();
        scheduledRun.register();
    }

    /**
     * Cancels the scheduled action. Does nothing if the action is not
     * scheduled.
     */
    public void cancel() {
        if (scheduledRun == null) {
            return;
        }
        ScheduledRun run = scheduledRun;
        scheduledRun = null;
        run.cancel();
    }

    /**
     * One scheduling of the action. Flow can still run an entry that it
     * collected before an earlier callback in the same flush cancelled the
     * action or detached the component, so the entry checks both before it runs
     * the action.
     * <p>
     * Always read the action from the {@code action} field when it runs. Do not
     * copy it to a local variable, and do not pass it to a lambda or to another
     * object. Otherwise the component can fail to deserialize, see
     * https://github.com/vaadin/flow-components/issues/6555
     */
    private final class ScheduledRun
            implements SerializableConsumer<ExecutionContext> {

        private Registration attachRegistration;
        private boolean cancelled;

        private void register() {
            attachRegistration = component.whenAttached(
                    ui -> ui.beforeClientResponse(component, this));
        }

        @Override
        public void accept(ExecutionContext context) {
            if (cancelled || !component.isAttached()) {
                // When detached, whenAttached registers this run again on the
                // next attach
                return;
            }
            BeforeClientResponseAction.this.cancel();
            action.run();
        }

        private void cancel() {
            cancelled = true;
            attachRegistration.remove();
        }
    }
}
