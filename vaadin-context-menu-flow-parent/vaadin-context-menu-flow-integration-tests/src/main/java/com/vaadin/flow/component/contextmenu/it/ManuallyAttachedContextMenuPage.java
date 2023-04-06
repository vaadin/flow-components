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
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;

/**
 * @author Vaadin Ltd
 */
@Route("vaadin-context-menu/manually-attached-context-menu")
public class ManuallyAttachedContextMenuPage extends Div {

    public ManuallyAttachedContextMenuPage() {
        Paragraph target = new Paragraph("target");
        target.setId("target");

        ContextMenu contextMenu = new ContextMenu(target);
        MenuItem item = contextMenu.addItem("foo");
        item.setCheckable(true);

        NativeButton toggleChecked = new NativeButton("toggle checked",
                e -> item.setChecked(!item.isChecked()));
        toggleChecked.setId("toggle-checked");

        // Add <vaadin-context-menu> to DOM manually
        add(target, contextMenu, toggleChecked);
    }

}
