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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.shared.DisableOnClickMode;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.shared.Registration;

/**
 * An internal controller for handling disabling a component when it is clicked.
 * Not intended to be used publicly.
 * <p>
 * When {@link #setDisableOnClick(boolean)} is enabled, the component will be
 * immediately disabled upon clicking, both on the client-side and server-side,
 * to prevent multiple clicks or submissions while the server processes the
 * event.
 * <p>
 * This controller requires that the component implements {@link HasEnabled}.
 *
 * @param <C>
 *            Type of the component that uses this controller.
 * @since 24.6
 */
@JsModule("./disableOnClickFunctions.js")
public class DisableOnClickController<C extends Component & HasEnabled>
        implements Serializable {

    private final C component;
    private boolean disableOnClick = false;
    private DisableOnClickMode disableOnClickMode = DisableOnClickMode.UNTIL_ENABLED;
    private final BeforeClientResponseAction clientUpdate;
    private final BeforeClientResponseAction enable;
    private boolean updatingEnabled = false;

    /**
     * Creates a new controller for the given component.
     *
     * @param component
     *            the component to control, not {@code null}
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public DisableOnClickController(C component) {
        this.component = Objects.requireNonNull(component);
        clientUpdate = new BeforeClientResponseAction(component,
                () -> component.getElement().executeJs("this.disabled = $0",
                        !component.isEnabled()));
        enable = new BeforeClientResponseAction(component,
                () -> setEnabledInternal(true));

        ComponentUtil.addListener(component, ClickEvent.class,
                (ComponentEventListener) (event -> {
                    if (isDisableOnClick()) {
                        // Schedule enabling before disabling so that the
                        // component is enabled again before the client-side
                        // disabled property is updated, which results in a
                        // single update with the final state.
                        if (disableOnClickMode == DisableOnClickMode.UNTIL_RESPONSE) {
                            enable.schedule();
                        }
                        setEnabledInternal(false);
                    }
                }));
    }

    /**
     * Sets whether the component should be disabled when clicked.
     * <p>
     * When set to {@code true}, the component will be immediately disabled on
     * the client-side when clicked, preventing further clicks. How long the
     * component stays disabled depends on the current
     * {@link #getDisableOnClickMode() disable on click mode}.
     *
     * @param disableOnClick
     *            whether the component should be disabled when clicked
     */
    public void setDisableOnClick(boolean disableOnClick) {
        this.disableOnClick = disableOnClick;
        if (disableOnClick) {
            component.getElement().setAttribute("disableonclick", "true");
        } else {
            component.getElement().removeAttribute("disableonclick");
        }
    }

    /**
     * Gets whether the component is set to be disabled when clicked.
     *
     * @return whether the component is set to be disabled on click
     */
    public boolean isDisableOnClick() {
        return disableOnClick;
    }

    /**
     * Enables disabling the component when clicked, using the given mode to
     * determine how long the component stays disabled.
     *
     * @param mode
     *            the disable on click mode, not {@code null}
     * @see #setDisableOnClick(boolean)
     */
    public void setDisableOnClick(DisableOnClickMode mode) {
        this.disableOnClickMode = Objects.requireNonNull(mode,
                "DisableOnClickMode must not be null");
        setDisableOnClick(true);
    }

    /**
     * Gets the mode that determines how long the component stays disabled after
     * it has been disabled on click.
     *
     * @return the disable on click mode, not {@code null}
     */
    public DisableOnClickMode getDisableOnClickMode() {
        return disableOnClickMode;
    }

    /**
     * Forces the client-side component's {@code disabled} property to be
     * updated before the response is sent to the client, so that it matches the
     * component's effective enabled state, including whether any parent is
     * disabled.
     * <p>
     * This method should be called from the component's
     * {@link HasEnabled#setEnabled} method, after the enabled state has been
     * updated.
     */
    public void onSetEnabled() {
        if (!updatingEnabled) {
            // The enabled state was set explicitly by application code, so
            // don't override it after the round trip.
            enable.cancel();
        }
        // If the component is disabled and re-enabled during the same round
        // trip, Flow will not detect any changes and the client side component
        // would not be enabled again. The property is updated before the
        // response so that the effective state at that point is used, for
        // example when a parent is disabled or enabled in the same round trip.
        clientUpdate.schedule();
    }

    private void setEnabledInternal(boolean enabled) {
        updatingEnabled = true;
        try {
            component.setEnabled(enabled);
        } finally {
            updatingEnabled = false;
        }
    }

    /**
     * Runs an action once before the client response, even if the component is
     * detached and attached again in between, possibly to another UI.
     * <p>
     * {@link UI#beforeClientResponse} is bound to the UI the component is
     * attached to when the action is registered, and the action is dropped if
     * the component is not attached to that same UI when the response is sent.
     * This class instead registers the action whenever the component is
     * attached, removes the registration whenever it is detached, and stops
     * doing so once the action has run or has been cancelled.
     */
    private static class BeforeClientResponseAction implements Serializable {

        private final Component component;
        private final SerializableRunnable action;
        private Registration attachRegistration;

        BeforeClientResponseAction(Component component,
                SerializableRunnable action) {
            this.component = component;
            this.action = action;
        }

        /**
         * Schedules the action to run before the next client response while the
         * component is attached. Does nothing if the action is already
         * scheduled.
         */
        void schedule() {
            if (attachRegistration != null) {
                return;
            }
            attachRegistration = component.whenAttached(
                    ui -> ui.beforeClientResponse(component, context -> {
                        cancel();
                        action.run();
                    }));
        }

        /**
         * Cancels the scheduled action. Does nothing if the action is not
         * scheduled.
         */
        void cancel() {
            if (attachRegistration == null) {
                return;
            }
            Registration registration = attachRegistration;
            attachRegistration = null;
            registration.remove();
        }
    }
}
