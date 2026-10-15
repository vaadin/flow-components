/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.contextmenu.it;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;

@Route("vaadin-context-menu/close")
public class ContextMenuClosePage extends Div {

    public ContextMenuClosePage() {
        Paragraph target = new Paragraph("Target for context menu");
        target.setId("context-menu-target");
        add(target);

        ContextMenu contextMenu = new ContextMenu(target);
        contextMenu.addItem("Item 1");
        contextMenu.addItem("Item 2");

        NativeButton closeButton = new NativeButton("Close",
                e -> contextMenu.close());
        closeButton.setId("close-menu");
        contextMenu.add(new Div(closeButton));

        Div message = new Div();
        message.setId("closed-message");
        add(message);

        contextMenu.addOpenedChangeListener(event -> {
            if (!event.isOpened()) {
                message.setText("Closed from client: " + event.isFromClient());
            }
        });
    }
}
