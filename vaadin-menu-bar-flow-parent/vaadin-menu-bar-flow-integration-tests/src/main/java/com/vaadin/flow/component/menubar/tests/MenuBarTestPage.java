/*
 * Copyright 2000-2019 Vaadin Ltd.
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
package com.vaadin.flow.component.menubar.tests;

import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

@Route("menu-bar-test")
@PreserveOnRefresh
public class MenuBarTestPage extends Div {

    public MenuBarTestPage() {
        MenuBar menuBar = new MenuBar();
        add(menuBar);

        Paragraph message = new Paragraph("");
        message.setId("message");
        add(message);

        MenuItem item1 = menuBar.addItem("item 1");

        MenuItem item2 = menuBar.addItem(new Paragraph("item 2"),
                e -> message.setText(message.getText() + "clicked item 2"));

        item1.getSubMenu().addItem("sub item 1",
                e -> message.setText("clicked sub item 1"));
        MenuItem subItem2 = item1.getSubMenu()
                .addItem(new Paragraph("sub item 2"));

        subItem2.getSubMenu().addItem(new Paragraph("sub sub item 1"));
        MenuItem checkable = subItem2.getSubMenu().addItem("checkable");
        checkable.setCheckable(true);
        checkable.addClickListener(
                e -> message.setText(String.valueOf(checkable.isChecked())));

        NativeButton addRootItemButton = new NativeButton("add root item",
                e -> menuBar.addItem("added item"));
        addRootItemButton.setId("add-root-item");

        NativeButton addSubItemButton = new NativeButton("add sub item",
                e -> item2.getSubMenu().addItem("added sub item"));
        addSubItemButton.setId("add-sub-item");

        NativeButton removeItemButton = new NativeButton("remove item 2",
                e -> menuBar.remove(item2));
        removeItemButton.setId("remove-item");

        NativeButton openOnHoverButton = new NativeButton("toggle openOnHover",
                e -> menuBar.setOpenOnHover(!menuBar.isOpenOnHover()));
        openOnHoverButton.setId("toggle-open-on-hover");

        NativeButton setWidthButton = new NativeButton("set width 140px", e -> {
            setWidth("140px");
            menuBar.getElement().callFunction("notifyResize");
        });
        setWidthButton.setId("set-width");

        NativeButton resetWidthButton = new NativeButton("reset width", e -> {
            setWidth("auto");
            menuBar.getElement().callFunction("notifyResize");
        });
        resetWidthButton.setId("reset-width");

        NativeButton disableButton = new NativeButton("toggle disable items",
                e -> menuBar.getItems()
                        .forEach(item -> item.setEnabled(!item.isEnabled())));
        disableButton.setId("toggle-disable");

        NativeButton visibleButton = new NativeButton("toggle visible item 2",
                e -> item2.setVisible(!item2.isVisible()));
        visibleButton.setId("toggle-visible");

        NativeButton checkedButton = new NativeButton("toggle checked",
                e -> checkable.setChecked(!checkable.isChecked()));
        checkedButton.setId("toggle-checked");

        NativeButton toggleAttachedButton = new NativeButton("toggle attached",
                e -> {
                    if (menuBar.getParent().isPresent()) {
                        remove(menuBar);
                    } else {
                        add(menuBar);
                    }
                });
        toggleAttachedButton.setId("toggle-attached");

        add(new Hr(), addRootItemButton, addSubItemButton, removeItemButton,
                openOnHoverButton, setWidthButton, resetWidthButton,
                disableButton, visibleButton, checkedButton,
                toggleAttachedButton);
    }
}
