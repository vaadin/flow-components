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
    private final SerializableConsumer<ExecutionContext> action;
    private Registration attachRegistration;

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
        this(component, new RunnableAction(action));
    }

    /**
     * Creates a new action for the given component that receives the
     * {@link ExecutionContext} of the response it runs before, for example to
     * check whether the component's client side is already initialized. The
     * action is not scheduled until you call {@link #schedule()}.
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
            SerializableConsumer<ExecutionContext> action) {
        this.component = Objects.requireNonNull(component,
                "Component must not be null");
        this.action = Objects.requireNonNull(action, "Action must not be null");
    }

    /**
     * Schedules the action to run before the next client response while the
     * component is attached. If the component is not attached, the action runs
     * before the first response after the component has been attached. Does
     * nothing if the action is already scheduled.
     */
    public void schedule() {
        if (attachRegistration != null) {
            return;
        }
        // The lambdas must only capture this instance and access the action
        // through the field. If they captured the action lambda directly,
        // deserialization could assign an unresolved SerializedLambda to the
        // action field, see
        // https://github.com/vaadin/flow-components/issues/6555
        attachRegistration = component.whenAttached(
                ui -> ui.beforeClientResponse(component, context -> {
                    cancel();
                    action.accept(context);
                }));
    }

    /**
     * Cancels the scheduled action. Does nothing if the action is not
     * scheduled.
     */
    public void cancel() {
        if (attachRegistration == null) {
            return;
        }
        Registration registration = attachRegistration;
        attachRegistration = null;
        registration.remove();
    }

    /**
     * Adapts a runnable to an action that ignores the execution context. A
     * class instead of a lambda, so that the runnable is held in a field and
     * not captured by another lambda, see the note in {@link #schedule()}.
     */
    private static final class RunnableAction
            implements SerializableConsumer<ExecutionContext> {

        private final SerializableRunnable runnable;

        RunnableAction(SerializableRunnable runnable) {
            this.runnable = Objects.requireNonNull(runnable,
                    "Action must not be null");
        }

        @Override
        public void accept(ExecutionContext context) {
            runnable.run();
        }
    }
}
