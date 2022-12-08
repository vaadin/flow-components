package com.vaadin.addon.spreadsheet.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

public class FlowComponentRenderer extends Widget {
    public FlowComponentRenderer(String appId, String nodeId) {
        Element element = Document.get()
                .createElement("flow-component-renderer");
        element.setAttribute("appid", appId);
        element.setAttribute("nodeid", nodeId);
        setElement(element);

        setSize("100%", "100%");
    }
}
