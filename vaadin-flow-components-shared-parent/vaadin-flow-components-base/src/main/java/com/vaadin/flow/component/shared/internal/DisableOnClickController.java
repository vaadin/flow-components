/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
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
 * This controller requires that the component implements both
 * {@link HasEnabled} and {@link ClickNotifier}.
 *
 * @param <C>
 *            Type of the component that uses this controller.
 */
@JsModule("./disableOnClickFunctions.js")
public class DisableOnClickController<C extends Component & HasEnabled & ClickNotifier<C>>
        implements Serializable {

    private final C component;
    private boolean disableOnClick = false;
    private boolean disableOnClickInitialized;

    /**
     * Creates a new controller for the given component.
     *
     * @param component
     *            the component to control, not {@code null}
     */
    public DisableOnClickController(C component) {
        this.component = Objects.requireNonNull(component);
        component.addClickListener(event -> {
            if (isDisableOnClick()) {
                component.setEnabled(false);
            }
        });
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
            component.getElement().setProperty("disableOnClick", "true");
            ensureDisableOnClickInitialized();
        } else {
            component.getElement().removeProperty("disableOnClick");
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
     * updated immediately.
     * <p>
     * This method should be called from the component's
     * {@link HasEnabled#setEnabled} method.
     *
     * @param enabled
     *            value to set
     */
    public void onSetEnabled(boolean enabled) {
        // If the component is then disabled and re-enabled during the same
        // round trip, Flow will not detect any changes and the client side
        // component would not be enabled again.
        component.getElement().executeJs("this.disabled = $0", !enabled);
    }

    /**
     * Initialize client side disabling so disabled if immediate on click even
     * if server-side handling takes some time.
     */
    private void ensureDisableOnClickInitialized() {
        if (disableOnClickInitialized) {
            return;
        }
        disableOnClickInitialized = true;
        if (component.isAttached()) {
            initDisableOnClick();
        }
        component.addAttachListener(event -> initDisableOnClick());
    }

    private void initDisableOnClick() {
        component.getElement().executeJs(
                "window.Vaadin.Flow.disableOnClick.initDisableOnClick(this);");
    }
}
