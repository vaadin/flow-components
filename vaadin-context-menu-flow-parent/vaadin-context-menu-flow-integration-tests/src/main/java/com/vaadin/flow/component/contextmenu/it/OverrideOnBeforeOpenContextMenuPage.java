/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.contextmenu.it;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;

import elemental.json.JsonObject;

/**
 * @author Vaadin Ltd
 */
@Route("vaadin-context-menu/override-on-before-open-context-menu")
public class OverrideOnBeforeOpenContextMenuPage extends Div {

    public OverrideOnBeforeOpenContextMenuPage() {
        addContextMenuThatDoesNotOpen();
        addSeparator();
        addContextMenuThatDynamicallyChangesItems();
    }

    private void addContextMenuThatDoesNotOpen() {
        Label target = new Label("Context menu that should not open");
        target.setId("no-open-menu-target");

        ContextMenu contextMenu = new ContextMenu(target) {
            @Override
            protected boolean onBeforeOpenMenu(JsonObject eventDetail) {
                // ensure context menu will not open
                return false;
            }
        };

        contextMenu.addItem("Item 1");
        contextMenu.addItem("Item 2");
        add(target);
    }

    private void addContextMenuThatDynamicallyChangesItems() {
        Label target = new Label("Context menu that changes items dynamically");
        target.setId("dynamic-context-menu-target");

        ContextMenu contextMenu = new ContextMenu(target) {
            @Override
            protected boolean onBeforeOpenMenu(JsonObject eventDetail) {
                removeAll();

                addItem("Dynamic Item");

                return super.onBeforeOpenMenu(eventDetail);
            }
        };

        contextMenu.addItem("Item 1");
        contextMenu.addItem("Item 2");
        add(target);
    }

    private void addSeparator() {
        getElement().appendChild(new Element("hr"));
    }
}
