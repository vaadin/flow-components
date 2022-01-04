/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
