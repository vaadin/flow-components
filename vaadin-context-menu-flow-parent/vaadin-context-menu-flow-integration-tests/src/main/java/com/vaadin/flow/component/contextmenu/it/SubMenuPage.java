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
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;

@Route("vaadin-context-menu/sub-menu-test")
public class SubMenuPage extends Div {

    private Paragraph target;
    private Paragraph message;
    private ContextMenu contextMenu;

    private int counter = 0;

    public SubMenuPage() {
        target = new Paragraph("target");
        target.setId("target");
        message = new Paragraph();
        message.setId("message");
        contextMenu = new ContextMenu(target);

        MenuItem foo = contextMenu.addItem("foo");
        SubMenu subMenu = foo.getSubMenu();

        MenuItem bar = subMenu.addItem("bar", event -> message.setText("bar"));

        NativeButton addItem = new NativeButton("Add item to the sub menu",
                event -> addItem(subMenu));
        addItem.setId("add-item");

        NativeButton addComponent = new NativeButton(
                "Insert a component to the sub-menu", event -> subMenu
                        .addComponentAtIndex(0, new Anchor("foo", "Link")));
        addComponent.setId("add-component");

        NativeButton addCheckableComponent = new NativeButton(
                "Add checkable item to sub-menu", event -> {
                    MenuItem item = subMenu.addItem("checkable",
                            ev -> message.setText("Checkable item is "
                                    + ev.getSource().isChecked()));
                    item.setCheckable(true);
                    item.setChecked(true);
                });
        addCheckableComponent.setId("add-checkable-component");

        NativeButton addSubSubMenu = new NativeButton("Add sub-sub-menu",
                event -> addItem(bar.getSubMenu()));
        addSubSubMenu.setId("add-sub-sub-menu");

        NativeButton removeAllFromSubMenu = new NativeButton(
                "Remove all from sub-menu", e -> subMenu.removeAll());
        removeAllFromSubMenu.setId("remove-all");

        add(target, message, new Div(addItem, addComponent,
                addCheckableComponent, addSubSubMenu, removeAllFromSubMenu));
    }

    private void addItem(SubMenu subMenu) {
        String caption = "" + counter;
        subMenu.addItem(caption, e -> message.setText(caption));
        counter++;
    }

}
