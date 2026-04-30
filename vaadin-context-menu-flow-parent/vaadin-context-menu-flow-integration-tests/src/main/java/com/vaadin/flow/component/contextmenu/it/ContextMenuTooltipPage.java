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
package com.vaadin.flow.component.contextmenu.it;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.shared.Tooltip.TooltipPosition;
import com.vaadin.flow.component.shared.TooltipConfiguration;
import com.vaadin.flow.router.Route;

@Route("vaadin-context-menu/tooltip")
public class ContextMenuTooltipPage extends Div {

    public ContextMenuTooltipPage() {
        TooltipConfiguration.setDefaultFocusDelay(0);
        TooltipConfiguration.setDefaultHoverDelay(0);
        TooltipConfiguration.setDefaultHideDelay(0);

        var target = new NativeButton("Target");
        target.setId("target");

        var contextMenu = new ContextMenu(target);
        var openItem = contextMenu.addItem("Open");
        contextMenu.setTooltipText(openItem, "Open the selected file");

        var deleteItem = contextMenu.addItem("Delete");
        deleteItem.setEnabled(false);
        contextMenu.setTooltipText(deleteItem, "Not available right now");
        contextMenu.setTooltipPosition(deleteItem, TooltipPosition.START);

        var moreItem = contextMenu.addItem("More");
        var subItem = moreItem.getSubMenu().addItem("Sub item");
        contextMenu.setTooltipText(subItem, "Sub item tooltip");

        var updateTooltipButton = new NativeButton("Update tooltip",
                event -> contextMenu.setTooltipText(openItem,
                        "Updated tooltip"));
        updateTooltipButton.setId("update-tooltip-button");

        add(target, updateTooltipButton);
    }
}
