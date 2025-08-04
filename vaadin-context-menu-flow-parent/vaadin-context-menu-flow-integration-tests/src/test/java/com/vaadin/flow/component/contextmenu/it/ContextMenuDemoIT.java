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
        verifyClosedAndRemoved();

        rightClickOn("basic-context-menu-target");
        verifyOpened();

        Assert.assertArrayEquals(new String[] { "First menu item",
                "Second menu item", "Disabled menu item" },
                getMenuItemCaptions());

        Assert.assertFalse("The last item is supposed to be disabled",
                getMenuItems().get(2).isEnabled());

        $("body").first().click();
        verifyClosedAndRemoved();
    }

    @Test
    public void openAndCloseContextMenuWithComponents_contentIsRendered() {
        verifyClosedAndRemoved();

        rightClickOn("context-menu-with-components-target");
        verifyOpened();

        Assert.assertArrayEquals(new String[] { "First menu item", "Checkbox" },
                getMenuItemCaptions());
        Assert.assertTrue(getMenuContent().$("hr").exists());
        WebElement span = getMenuContent().$("span").first();
        Assert.assertEquals("This is not a menu item", span.getText());

        WebElement checkbox = getMenuItems().get(1)
                .findElement(By.tagName("vaadin-checkbox"));
        checkbox.click();
        $("body").first().click();
        verifyClosedAndRemoved();
        WebElement message = findElement(
                By.id("context-menu-with-components-message"));
        Assert.assertEquals("Clicked on checkbox with value: true",
                message.getText());
    }

    @Test
    public void hierarchicalContextMenu_openSubMenus() {
        verifyClosedAndRemoved();

        rightClickOn("hierarchical-menu-target");
        verifyNumberOfMenus(1);

        openSubMenu(getMenuItems().get(1));
        verifyNumberOfMenus(2);

        List<ContextMenuElement> menus = getAllMenus();
        openSubMenu(getMenuItems(menus.get(1)).get(1));

        verifyNumberOfMenus(3);
        menus = getAllMenus();
        getMenuItems(menus.get(2)).get(0).click();

        Assert.assertEquals("Clicked on the third item",
                $("span").id("hierarchical-menu-message").getText());

        verifyClosedAndRemoved();
    }

    @Test
    public void checkableMenuItems() {
        verifyClosedAndRemoved();

        rightClickOn("checkable-menu-items-target");
        verifyOpened();

        List<ContextMenuItemElement> items = getMenuItems();
        ContextMenuPageIT.assertCheckedInClientSide(items.get(0), false);
        ContextMenuPageIT.assertCheckedInClientSide(items.get(1), true);

        items.get(1).click();

        Assert.assertEquals("Unselected option 2",
                $("span").id("checkable-menu-items-message").getText());
        verifyClosedAndRemoved();

        rightClickOn("checkable-menu-items-target");
        verifyOpened();

        items = getMenuItems();
        ContextMenuPageIT.assertCheckedInClientSide(items.get(0), false);
        ContextMenuPageIT.assertCheckedInClientSide(items.get(1), false);

        items.get(0).click();

        Assert.assertEquals("Selected option 1",
                $("span").id("checkable-menu-items-message").getText());
        verifyClosedAndRemoved();

        rightClickOn("checkable-menu-items-target");
        verifyOpened();

        items = getMenuItems();
        ContextMenuPageIT.assertCheckedInClientSide(items.get(0), true);
        ContextMenuPageIT.assertCheckedInClientSide(items.get(1), false);

        items.get(2).click();
        verifyOpened();
        ContextMenuPageIT.assertCheckedInClientSide(items.get(2), false);

        items.get(2).click();
        verifyOpened();
        ContextMenuPageIT.assertCheckedInClientSide(items.get(2), true);
    }

    @Test
    public void subMenuHasComponents_componentsAreNotItems() {
        verifyClosedAndRemoved();

        rightClickOn("context-menu-with-submenu-components-target");
        verifyOpened();

        openSubMenu(getMenuItems().get(1));
        verifyNumberOfMenus(2);

        ContextMenuElement subMenu = getAllMenus().get(1);
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
