/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.addon.spreadsheet.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

public class Slot extends Widget {

    public Slot(String name, Element assignedElement, Element host) {
        // Create the slot element with the given name
        var slotElement = Document.get().createElement("slot");
        slotElement.setAttribute("name", name);
        setElement(slotElement);

        // Use the given name as the slot attribute of the assigned element
        assignedElement.setAttribute("slot", name);

        // Keep the assigned element in the DOM while the slot is attached
        addAttachHandler(e -> {
            if (e.isAttached()) {
                host.appendChild(assignedElement);
            } else {
                assignedElement.removeFromParent();
            }
        });
    }
}
