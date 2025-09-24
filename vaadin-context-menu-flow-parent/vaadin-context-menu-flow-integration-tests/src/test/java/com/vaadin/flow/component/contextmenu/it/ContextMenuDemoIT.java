/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.contextmenu.testbench.ContextMenuElement;
import com.vaadin.flow.component.contextmenu.testbench.ContextMenuItemElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

/**
 * Integration tests for the {@link ContextMenuView}.
 */
@TestPath("vaadin-context-menu")
public class ContextMenuDemoIT extends AbstractContextMenuIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void openAndCloseBasicContextMenu_contentIsRendered() {
        ContextMenuElement contextMenu = ContextMenuElement
                .openByRightClick($("*").id("basic-context-menu-target"));
        List<ContextMenuItemElement> menuItems = contextMenu.getMenuItems();

        Assert.assertArrayEquals(new String[] { "First menu item",
                "Second menu item", "Disabled menu item" },
                getMenuItemCaptions(menuItems));

        Assert.assertFalse("The last item is supposed to be disabled",
                menuItems.get(2).isEnabled());

        clickBody();
        contextMenu.waitUntilClosed();
    }

    @Test
    public void openAndCloseContextMenuWithComponents_contentIsRendered() {
        ContextMenuElement contextMenu = ContextMenuElement.openByRightClick(
                $("*").id("context-menu-with-components-target"));
        List<ContextMenuItemElement> menuItems = contextMenu.getMenuItems();

        Assert.assertArrayEquals(new String[] { "First menu item", "Checkbox" },
                getMenuItemCaptions(menuItems));
        Assert.assertTrue(getMenuContent(contextMenu).$("hr").exists());
        WebElement span = getMenuContent(contextMenu).$("span").first();
        Assert.assertEquals("This is not a menu item", span.getText());

        WebElement checkbox = menuItems.get(1)
                .findElement(By.tagName("vaadin-checkbox"));
        checkbox.click();
        clickBody();
        contextMenu.waitUntilClosed();
        WebElement message = findElement(
                By.id("context-menu-with-components-message"));
        Assert.assertEquals("Clicked on checkbox with value: true",
                message.getText());
    }

    @Test
    public void hierarchicalContextMenu_openSubMenus() {
        ContextMenuElement contextMenu = ContextMenuElement
                .openByRightClick($("*").id("hierarchical-menu-target"));
        verifyNumberOfMenus(1);

        ContextMenuElement subMenu = contextMenu.getMenuItems().get(1)
                .openSubMenu();
        verifyNumberOfMenus(2);

        ContextMenuElement subSubMenu = subMenu.getMenuItems().get(1)
                .openSubMenu();
        verifyNumberOfMenus(3);

        subSubMenu.getMenuItems().get(0).click();
        contextMenu.waitUntilClosed();

        Assert.assertEquals("Clicked on the third item",
                $("span").id("hierarchical-menu-message").getText());
    }

    @Test
    public void checkableMenuItems() {
        ContextMenuElement contextMenu = ContextMenuElement
                .openByRightClick($("*").id("checkable-menu-items-target"));

        List<ContextMenuItemElement> items = contextMenu.getMenuItems();
        Assert.assertFalse(items.get(0).isChecked());
        Assert.assertTrue(items.get(1).isChecked());

        items.get(1).click();
        contextMenu.waitUntilClosed();

        Assert.assertEquals("Unselected option 2",
                $("span").id("checkable-menu-items-message").getText());

        contextMenu = ContextMenuElement
                .openByRightClick($("*").id("checkable-menu-items-target"));

        items = contextMenu.getMenuItems();
        Assert.assertFalse(items.get(0).isChecked());
        Assert.assertFalse(items.get(1).isChecked());

        items.get(0).click();
        contextMenu.waitUntilClosed();

        Assert.assertEquals("Selected option 1",
                $("span").id("checkable-menu-items-message").getText());

        contextMenu = ContextMenuElement
                .openByRightClick($("*").id("checkable-menu-items-target"));

        items = contextMenu.getMenuItems();
        Assert.assertTrue(items.get(0).isChecked());
        Assert.assertFalse(items.get(1).isChecked());

        items.get(2).click();
        Assert.assertTrue(contextMenu.isOpen());
        Assert.assertFalse(items.get(2).isChecked());

        items.get(2).click();
        Assert.assertTrue(contextMenu.isOpen());
        Assert.assertTrue(items.get(2).isChecked());
    }

    @Test
    public void subMenuHasComponents_componentsAreNotItems() {
        ContextMenuElement contextMenu = ContextMenuElement.openByRightClick(
                $("*").id("context-menu-with-submenu-components-target"));
        ContextMenuElement subMenu = contextMenu.getMenuItems().get(1)
                .openSubMenu();

        TestBenchElement menuContent = getMenuContent(subMenu);
        TestBenchElement menuListBox = menuContent
                .$("vaadin-context-menu-list-box").first();
        List<WebElement> items = menuListBox.findElements(By.xpath("./*"));
        Assert.assertEquals(4, items.size());
        Assert.assertEquals("vaadin-context-menu-item",
                items.get(0).getTagName().toLowerCase(Locale.ENGLISH));
        Assert.assertEquals("hr",
                items.get(1).getTagName().toLowerCase(Locale.ENGLISH));
        Assert.assertEquals("vaadin-context-menu-item",
                items.get(2).getTagName().toLowerCase(Locale.ENGLISH));
        Assert.assertEquals("span",
                items.get(3).getTagName().toLowerCase(Locale.ENGLISH));
    }
}
