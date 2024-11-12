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
package com.vaadin.flow.component.shared;

import java.io.Serializable;
import java.util.Objects;

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.page.PendingJavaScriptResult;

/**
 * Controller that handles disabling a component when it is clicked.
 * <p>
 * When {@link #setDisableOnClick(boolean)} is enabled, the component will be
 * immediately disabled upon clicking, both on the client-side and server-side,
 * to prevent multiple clicks or submissions while the server processes the
 * event.
 * <p>
 * This controller requires that the component implements both
 * {@link HasEnabled} and {@link ClickNotifier}.
 */
@JsModule("./disableOnClickFunctions.js")
public class DisableOnClickController implements Serializable {

    private final Component component;
    private boolean disableOnClick = false;
    private PendingJavaScriptResult initDisableOnClick;

    /**
     * Creates a new controller for the given component.
     * <p>
     * The component must implement {@link HasEnabled} and
     * {@link ClickNotifier}.
     *
     * @param component
     *            the component to control, not {@code null}
     * @throws IllegalArgumentException
     *             if the component does not implement {@link HasEnabled} or
     *             {@link ClickNotifier}
     */
    public DisableOnClickController(Component component) {
        this.component = Objects.requireNonNull(component);
        if (!(component instanceof HasEnabled)) {
            throw new IllegalArgumentException(
                    "The component has to implement HasEnabled");
        }
        if (!(component instanceof ClickNotifier)) {
            throw new IllegalArgumentException(
                    "The component has to implement ClickNotifier");
        }
        ((ClickNotifier<Component>) component)
                .addClickListener(itemClickEvent -> {
                    if (disableOnClick) {
                        ((HasEnabled) component).setEnabled(false);
                    }
                });
        component.addAttachListener(aAttachEvent -> {
            if (isDisableOnClick()) {
                initDisableOnClick();
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
            component.getElement().setAttribute("disableOnClick", "true");
            initDisableOnClick();
        } else {
            component.getElement().removeAttribute("disableOnClick");
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

    private void initDisableOnClick() {
        if (initDisableOnClick == null) {
            initDisableOnClick = component.getElement().executeJs(
                    "window.Vaadin.Flow.disableOnClick.initDisableOnClick($0)");
            component.getElement().getNode()
                    .runWhenAttached(ui -> ui.beforeClientResponse(component,
                            executionContext -> initDisableOnClick = null));
        }
    }
}
