/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
