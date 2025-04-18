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

    private boolean autoAdded;
    private Registration beforeEnterListenerRegistration;

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
                    if (beforeEnterListenerRegistration != null) {
                        beforeEnterListenerRegistration.remove();
                        beforeEnterListenerRegistration = null;
                    }
                });
        if (ui.getSession() != null) {
            // Cancel auto-adding if the current view is navigated away from
            // before the dialog is added to the UI. This can happen if an
            // overlay component is opened in a view constructor, and the
            // view implements a BeforeEnterObserver that forwards to a
            // different view. However, auto-adding should not be canceled if
            // the view that was navigated to opens the overlay component.
            // beforeEnterListener seems to work for this:
            // - The listener is registered when an overlay opens during view
            // construction. At that point the before enter event for entering
            // that view has already been fired on the UI, so it does not
            // cancel auto-adding.
            // - If another before enter event is fired, that means that the
            // view that opened the overlay has forwarded to another view.
            beforeEnterListenerRegistration = ui
                    .addBeforeEnterListener(event -> {
                        addToUiRegistration.remove();
                        beforeEnterListenerRegistration.remove();
                        beforeEnterListenerRegistration = null;
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
