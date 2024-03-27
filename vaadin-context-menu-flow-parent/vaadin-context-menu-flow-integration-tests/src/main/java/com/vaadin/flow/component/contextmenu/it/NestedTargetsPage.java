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
import com.vaadin.flow.router.Route;

/**
 * @author Vaadin Ltd
 */
@Route("vaadin-context-menu/nested-targets")
public class NestedTargetsPage extends Div {

    public NestedTargetsPage() {
        Div messages = new Div();
        messages.setId("messages");

        Div parentTarget = new Div();
        Paragraph notInChildTarget = new Paragraph(
                "Element inside parent target");
        notInChildTarget.setId("not-in-child-target");
        Paragraph childTarget = new Paragraph(
                "Child target inside parent target");
        childTarget.setId("child-target");

        parentTarget.add(notInChildTarget, childTarget);

        ContextMenu menuOnParent = new ContextMenu(parentTarget);
        menuOnParent.addItem("menu on parent target",
                e -> messages.add("parent"));

        ContextMenu menuOnChild = new ContextMenu(childTarget);
        menuOnChild.addItem("menu on child target", e -> messages.add("child"));

        add(parentTarget, messages);
    }
}
