/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.contextmenu.it;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;

@Route("vaadin-context-menu/initial-open-on-click")
public class InitialOpenOnClickPage extends Div {

    public InitialOpenOnClickPage() {
        Paragraph target = new Paragraph(
                "Target for context menu with initial openOnClick");
        target.setId("target");
        add(target);
        ContextMenu contextMenu = new ContextMenu(target);
        contextMenu.addItem("foo");
        contextMenu.setOpenOnClick(true);
    }

}
