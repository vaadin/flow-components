/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.shared.Registration;

/**
 * An internal controller for automatically adding a component to the UI when
 * it's opened. Not intended to be used publicly.
 *
 * @param <C>
 *            Type of the component that uses this controller.
 */
public class OverlayAutoAddController<C extends Component>
        implements Serializable {
    private final C component;
    private final SerializableSupplier<Boolean> isModalSupplier;

    private boolean skipOnNavigation;
    private boolean autoAdded;
    private Registration beforeLeaveRegistration;

    public OverlayAutoAddController(C component) {
        this(component, () -> false);
    }

    public OverlayAutoAddController(C component,
            SerializableSupplier<Boolean> isModalSupplier) {
        this.component = component;
        this.isModalSupplier = isModalSupplier;

        component.getElement().addPropertyChangeListener("opened", event -> {
            if (isOpened()) {
                handleOpen();
            } else {
                handleClose();
            }
        });
    }

    /**
     * Sets whether to skip auto-adding when the UI navigates to a new view
     * before the component is opened.
     *
     * @param skipOnNavigation
     *            whether to skip auto-adding on navigation
     */
    public void setSkipOnNavigation(boolean skipOnNavigation) {
        this.skipOnNavigation = skipOnNavigation;
    }

    private void handleOpen() {
        UI ui = getUI();
        StateTree.ExecutionRegistration addToUiRegistration = ui
                .beforeClientResponse(ui, context -> {
                    if (isOpened() && !isAttached()) {
                        ui.addToModalComponent(component);
                        ui.setChildComponentModal(component,
                                isModalSupplier.get());
                        autoAdded = true;
                    }
                    if (beforeLeaveRegistration != null) {
                        beforeLeaveRegistration.remove();
                        beforeLeaveRegistration = null;
                    }
                });

        if (skipOnNavigation && ui.getSession() != null) {
            // Cancel auto-adding if the current view is navigated away from
            // before the component is added to the UI.
            beforeLeaveRegistration = ui.addBeforeLeaveListener(event -> {
                addToUiRegistration.remove();
                beforeLeaveRegistration.remove();
                beforeLeaveRegistration = null;
            });
        }
    }

    private void handleClose() {
        if (autoAdded) {
            autoAdded = false;
            component.getElement().removeFromParent();
        }
    }

    private UI getUI() {
        UI ui = UI.getCurrent();
        if (ui == null) {
            throw new IllegalStateException("UI instance is not available. "
                    + "It means that you are calling this method "
                    + "out of a normal workflow where it's always implicitly set. "
                    + "That may happen if you call the method from the custom thread without "
                    + "'UI::access' or from tests without proper initialization.");
        }
        return ui;
    }

    private boolean isOpened() {
        return component.getElement().getProperty("opened", false);
    }

    private boolean isAttached() {
        return component.getElement().getNode().getParent() != null;
    }
}
