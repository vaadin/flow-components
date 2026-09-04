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
    private boolean clientUpdateScheduled = false;

    /**
     * Creates a new controller for the given component.
     *
     * @param component
     *            the component to control, not {@code null}
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public DisableOnClickController(C component) {
        this.component = Objects.requireNonNull(component);

        component.addDetachListener((event) -> clientUpdateScheduled = false);

        ComponentUtil.addListener(component, ClickEvent.class,
                (ComponentEventListener) (event -> {
                    if (isDisableOnClick()) {
                        component.setEnabled(false);
                    }
                }));
    }

    /**
     * Sets whether the component should be disabled when clicked.
     * <p>
     * When set to {@code true}, the component will be immediately disabled on
     * the client-side when clicked, preventing further clicks until re-enabled
     * from the server-side.
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
        // If the component is disabled and re-enabled during the same round
        // trip, Flow will not detect any changes and the client side component
        // would not be enabled again. The property is updated before the
        // response so that the effective state at that point is used, for
        // example when a parent is disabled or enabled in the same round trip.
        if (clientUpdateScheduled) {
            return;
        }
        clientUpdateScheduled = true;
        component.getElement().getNode().runWhenAttached(
                ui -> ui.beforeClientResponse(component, context -> {
                    clientUpdateScheduled = false;
                    component.getElement().executeJs("this.disabled = $0",
                            !component.isEnabled());
                }));
    }
}
