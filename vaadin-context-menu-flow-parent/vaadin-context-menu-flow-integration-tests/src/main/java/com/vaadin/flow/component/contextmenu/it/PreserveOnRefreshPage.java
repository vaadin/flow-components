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
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

/**
 * @author Vaadin Ltd.
 */
@Route("vaadin-context-menu/preserve-on-refresh")
@PreserveOnRefresh
public class PreserveOnRefreshPage extends Div {

    public PreserveOnRefreshPage() {
        Paragraph target = new Paragraph("Target");
        target.setId("target");
        add(target);

        ContextMenu contextMenu = new ContextMenu(target);
        contextMenu.addItem("foo");
    }

}
