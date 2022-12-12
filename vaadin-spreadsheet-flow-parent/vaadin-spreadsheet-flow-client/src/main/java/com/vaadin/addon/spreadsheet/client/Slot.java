package com.vaadin.addon.spreadsheet.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

public class Slot extends Widget {
    public Slot(String name) {
        Element element = Document.get().createElement("slot");
        element.setAttribute("name", name);
        setElement(element);
    }
}