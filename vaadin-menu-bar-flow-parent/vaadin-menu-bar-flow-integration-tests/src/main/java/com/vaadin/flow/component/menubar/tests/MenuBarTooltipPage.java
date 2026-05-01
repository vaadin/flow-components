/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.shared.Tooltip.TooltipPosition;
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

        // Root items (buttons)
        var item0 = menuBar.addItem("Item 0", "Item 0 / Tooltip");

        var item1 = menuBar.addItem("Item 1", "Item 1 / Tooltip");
        item1.setEnabled(false);

        var item2 = menuBar.addItem("Item 2", "Item 2 / Tooltip");
        item2.setTooltipPosition(TooltipPosition.TOP);

        // Sub menu items
        var item0_0 = item0.getSubMenu().addItem("Item 0-0",
                "Item 0-0 / Tooltip");

        var item0_1 = item0.getSubMenu().addItem("Item 0-1",
                "Item 0-1 / Tooltip");
        item0_1.setEnabled(false);

        var item0_2 = item0.getSubMenu().addItem("Item 0-2",
                "Item 0-2 / Tooltip");
        item0_2.setTooltipPosition(TooltipPosition.TOP);

        var attach = new NativeButton("Attach", event -> add(menuBar));
        attach.setId("attach");
        var detach = new NativeButton("Detach", event -> remove(menuBar));
        detach.setId("detach");

        var updateTooltips = new NativeButton("Update tooltips",
                event -> {
                    item0.setTooltipText("Item 0 / Updated Tooltip");
                    item0_0.setTooltipText("Item 0-0 / Updated Tooltip");
                });
        updateTooltips.setId("update-tooltips");

        add(attach, detach, updateTooltips, menuBar);
    }
}
