/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.contextmenu.it;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.Route;

/**
 * Test view for automatically attaching ContextMenu if it's not explicitly
 * attached anywhere.
 * <p>
 * Needs to be in its own view to make sure that the frontend dependencies are
 * loaded for the target component even if there are no ContextMenu components
 * attached yet to the page.
 *
 * @author Vaadin Ltd.
 */
@Route("vaadin-context-menu/auto-attached-context-menu")
public class AutoAttachedContextMenuPage extends Div {

    public AutoAttachedContextMenuPage() {
        Label target = new Label(
                "Target for context menu which is automatically added to the UI");
        target.setId("target-for-not-attached-context-menu");
        ContextMenu contextMenu = new ContextMenu(target);
        contextMenu.add(new Label("Auto-attached context menu"));
        contextMenu.setId("not-attached-context-menu");

        add(target);
    }

}
