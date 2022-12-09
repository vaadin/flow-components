package com.vaadin.addon.spreadsheet.client;

/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Widget;

public class Slot extends Widget {
    public Slot(String name) {
        var element = Document.get().createElement("slot");
        element.setAttribute("name", name);
        setElement(element);
    }
}
