/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Wires a {@link PopupButton} to the host's DOM and JS bridge:
 * <ul>
 * <li>appends the button's element as a Flow virtual child of the spreadsheet
 * so its rendering can be addressed from the client;</li>
 * <li>invokes the client RPC {@code addPopupButton} / {@code removePopupButton}
 * so the web component knows about the attached button.</li>
 * </ul>
 *
 * <p>
 * Tracks the set of currently attached buttons so they can all be detached on a
 * sheet change (active-sheet swap removes all popups, by design).
 */
final class PopupButtonBinding implements Serializable {

    private final Spreadsheet spreadsheet;
    private final HashSet<PopupButton> attachedButtons = new HashSet<>();

    PopupButtonBinding(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    void attach(PopupButton button) {
        attachedButtons.add(button);
        spreadsheet.getElement().callJsFunction("addPopupButton",
                Serializer.serialize(button.getState()));
        if (!spreadsheet.getElement().equals(button.getElement().getParent())) {
            spreadsheet.getElement().appendVirtualChild(button.getElement());
        }
    }

    void detach(PopupButton button) {
        if (!attachedButtons.remove(button)) {
            // Not on the client, so there is nothing to take off.
            return;
        }
        spreadsheet.getElement().callJsFunction("removePopupButton",
                Serializer.serialize(button.getState()));
        if (spreadsheet.getElement().equals(button.getElement().getParent())) {
            spreadsheet.getElement().removeVirtualChild(button.getElement());
        }
    }

    /**
     * Detaches every button currently attached to the DOM. Used on sheet-change
     * cleanup.
     */
    void detachAll() {
        for (PopupButton button : new ArrayList<>(attachedButtons)) {
            detach(button);
        }
    }
}
