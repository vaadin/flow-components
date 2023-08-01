/*
 * Copyright 2022 Vaadin Ltd.
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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.shared.TooltipConfiguration;
import com.vaadin.flow.router.Route;

@Route("vaadin-menu-bar/tooltip")
public class MenuBarTooltipPage extends Div {

    public MenuBarTooltipPage() {
        // Reset default delay values from 500 to 0
        TooltipConfiguration.setDefaultFocusDelay(0);
        TooltipConfiguration.setDefaultHoverDelay(0);
        TooltipConfiguration.setDefaultHideDelay(0);

        MenuBar menuBar = new MenuBar();
        // Use each add API with tooltip parameter to add menu items
        var editMenuItem = menuBar.addItem("Edit", "Edit tooltip");
        menuBar.addItem(new Span("Share"), "Share tooltip");
        menuBar.addItem("Move", "Move tooltip", (e) -> {
        });
        menuBar.addItem(new Span("Duplicate"), "Duplicate tooltip");

        // Add a button for toggling the menu-bar attached state.
        var toggleAttachedButton = new NativeButton("Toggle attached",
                event -> {
                    if (menuBar.getParent().isPresent()) {
                        remove(menuBar);
                    } else {
                        add(menuBar);
                    }
                });
        toggleAttachedButton.setId("toggle-attached-button");

        // Add a button for updating an item's tooltip
        var updateItemTooltipButton = new NativeButton("Update item tooltip",
                event -> {
                    menuBar.setTooltipText(editMenuItem,
                            "Updated Edit tooltip");
                });
        updateItemTooltipButton.setId("update-item-tooltip-button");

        add(toggleAttachedButton, updateItemTooltipButton, menuBar);
    }
}
