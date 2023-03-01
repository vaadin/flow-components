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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.contextmenu.testbench.ContextMenuElement;
import com.vaadin.flow.component.contextmenu.testbench.ContextMenuItemElement;
import com.vaadin.flow.component.contextmenu.testbench.ContextMenuOverlayElement;
import com.vaadin.flow.component.menubar.testbench.MenuBarElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-context-menu/sub-menu-helpers-test")
public class SubMenuHelpersIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void menuBarSubMenuTest() {
        MenuBarElement menuBar = $(MenuBarElement.class).first();
        menuBar.getButtons().get(0).click();
        Assert.assertEquals("Bar Item",
                $(NotificationElement.class).last().getText());

        menuBar.getButtons().get(1).click();
        ContextMenuOverlayElement overlay = $(ContextMenuOverlayElement.class)
                .last();
        ContextMenuItemElement menuBarSubItem = overlay.getMenuItems().get(0);
        Assert.assertEquals("Bar Sub Item", menuBarSubItem.getText());
        menuBarSubItem.click();
        Assert.assertEquals("Bar Sub Item",
                $(NotificationElement.class).last().getText());

        menuBar.getButtons().get(1).click();
        overlay = $(ContextMenuOverlayElement.class).last();
        overlay.getMenuItems().get(1).openSubMenu();
        ContextMenuOverlayElement subOverlay = $(
                ContextMenuOverlayElement.class).last();
        subOverlay.getMenuItems().get(0).click();
        Assert.assertEquals("Bar Sub Sub Item",
                $(NotificationElement.class).last().getText());
    }

    @Test
    public void contextMenuSubMenuTest() {
        ButtonElement button = $(ButtonElement.class).first();
        ContextMenuElement.openByRightClick(button);
        ContextMenuOverlayElement overlay = $(ContextMenuOverlayElement.class)
                .first();
        overlay.getMenuItem("Context Item").get().click();
        Assert.assertEquals("Context Item",
                $(NotificationElement.class).last().getText());

        ContextMenuElement.openByRightClick(button);
        overlay = $(ContextMenuOverlayElement.class).first();
        ContextMenuItemElement itemToOpen = overlay
                .getMenuItem("Context Sub Menu").get();
        itemToOpen.openSubMenu();
        $(ContextMenuOverlayElement.class).last().getMenuItems().get(0).click();
        Assert.assertEquals("Context Sub Item",
                $(NotificationElement.class).last().getText());
    }

    @Test
    public void contextMenuOpenCloseTest() {
        ButtonElement button = $(ButtonElement.class).first();
        ContextMenuElement.openByRightClick(button);
        ContextMenuElement contextMenu = $(ContextMenuElement.class).first();
        Assert.assertTrue(contextMenu.isOpen());
        button.click();
        Assert.assertFalse(contextMenu.isOpen());
    }
}
