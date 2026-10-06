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

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.shared.DisableOnClickMode;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.DownloadEvent;
import com.vaadin.flow.server.streams.DownloadHandler;

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
    private boolean disabledByClick = false;

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
     * @since 25.4
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
     * @since 25.4
     */
    public DisableOnClickMode getDisableOnClickMode() {
        return disableOnClickMode;
    }

    /**
     * Gets whether the component is currently disabled because it was clicked
     * while disable on click was turned on. This is no longer the case once the
     * enabled state is set by application code, or the component is enabled
     * again automatically.
     *
     * @return whether the component was disabled by disable on click and has
     *         not been enabled or disabled explicitly since
     * @since 25.4
     */
    public boolean isDisabledByClick() {
        return disabledByClick;
    }

    /**
     * Wraps the given download handler so that it is also served while the
     * component is {@link #isDisabledByClick() disabled by disable on click},
     * which happens before the browser requests the file. A component disabled
     * explicitly, or inside a disabled parent, still refuses the request unless
     * the handler itself allows it.
     *
     * @param handler
     *            the download handler to wrap, not {@code null}
     * @return a handler that delegates to the given handler
     * @since 25.4
     */
    public DownloadHandler allowDisabledByClick(DownloadHandler handler) {
        Objects.requireNonNull(handler, "handler must not be null");
        return new DownloadHandler() {
            @Override
            public void handleRequest(VaadinRequest request,
                    VaadinResponse response, VaadinSession session,
                    Element owner) throws IOException {
                handler.handleRequest(request, response, session, owner);
            }

            @Override
            public void handleDownloadRequest(DownloadEvent event)
                    throws IOException {
                handler.handleDownloadRequest(event);
            }

            @Override
            public String getUrlPostfix() {
                return handler.getUrlPostfix();
            }

            @Override
            public boolean isAllowInert() {
                return handler.isAllowInert();
            }

            @Override
            public DisabledUpdateMode getDisabledUpdateMode() {
                Element parent = component.getElement().getParent();
                if (disabledByClick && (parent == null || parent.isEnabled())) {
                    return DisabledUpdateMode.ALWAYS;
                }
                return handler.getDisabledUpdateMode();
            }
        };
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
     * 
     * @since 25.2.7
     */
    public void onSetEnabled() {
        if (!updatingEnabled) {
            // The enabled state was set explicitly by application code, so
            // don't override it after the round trip.
            enable.cancel();
            disabledByClick = false;
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
            disabledByClick = !enabled;
        } finally {
            updatingEnabled = false;
        }
    }
}
