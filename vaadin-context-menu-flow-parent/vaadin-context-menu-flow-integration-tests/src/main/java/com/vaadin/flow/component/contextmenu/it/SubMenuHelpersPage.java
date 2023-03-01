
/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;

@Route("vaadin-context-menu/sub-menu-helpers-test")
public class SubMenuHelpersPage extends Div {

    public SubMenuHelpersPage() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem contextMenuItem = contextMenu.addItem("Context Item", e -> {
            Notification.show("Context Item");
        });
        MenuItem contextMenuSub = contextMenu.addItem("Context Sub Menu");
        SubMenu contextBarSubMenu = contextMenuSub.getSubMenu();
        MenuItem contextMenuSubItem = contextBarSubMenu
                .addItem("Context Sub Item", e -> {
                    Notification.show("Context Sub Item");
                });
        MenuItem contextMenuSubSub = contextBarSubMenu
                .addItem("Context Sub Sub Menu");
        SubMenu contextBarSubSubMenu = contextMenuSubSub.getSubMenu();
        MenuItem contextMenuSubSubItem = contextBarSubSubMenu
                .addItem("Context Sub Sub Item", e -> {
                    Notification.show("Context Sub Sub Item");
                });

        Button button = new Button(VaadinIcon.MENU.create());
        contextMenu.setTarget(button);

        add(button);
    }
}
