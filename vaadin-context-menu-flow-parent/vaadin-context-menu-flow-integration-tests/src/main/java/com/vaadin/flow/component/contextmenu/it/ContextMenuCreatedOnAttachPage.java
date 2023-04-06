/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.contextmenu.it;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-context-menu/on-attach-listener")
public class ContextMenuCreatedOnAttachPage extends Div {
    public ContextMenuCreatedOnAttachPage() {
        createContextMenuAndTarget("target-open-left-click", true);
        createContextMenuAndTarget("target-open-right-click", false);
    }

    private void createContextMenuAndTarget(String id, boolean openOnClick) {
        final Div target = new Div(new Text(id));
        target.setId(id);
        addAttachListener(ev -> {
            final ContextMenu contextMenu = new ContextMenu();
            contextMenu.addItem("ITEM");
            contextMenu.setTarget(target);
            contextMenu.setOpenOnClick(openOnClick);
            add(contextMenu);
        });
        add(target);
    }
}
