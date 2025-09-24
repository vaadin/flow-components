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
 * @author Vaadin Ltd
 */
@TestPath("vaadin-context-menu/sub-menu-test")
public class SubMenuIT extends AbstractContextMenuIT {

    TestBenchElement target;

    @Before
    public void init() {
        open();
        target = $("*").id("target");
    }

    @Test
    public void addItemToSubMenu_subMenuRendered_clickListenerWorks() {
        ContextMenuElement menu = ContextMenuElement.openByRightClick(target);
        verifyNumberOfMenus(1);

        ContextMenuElement subMenu = menu.getMenuItems().get(0).openSubMenu();
        verifyNumberOfMenus(2);

        ContextMenuItemElement subItem = subMenu.getMenuItems().get(0);
        Assert.assertEquals("bar", subItem.getText());

        subItem.click();
        menu.waitUntilClosed();
        assertMessage("bar");
    }

    @Test
    public void openAndCloseSubMenu_addContent_contentUpdatedAndFunctional() {
        ContextMenuElement menu = ContextMenuElement.openByRightClick(target);
        menu.getMenuItems().get(0).openSubMenu();
        clickBody();
        menu.waitUntilClosed();

        clickElementWithJs("add-item");
        clickElementWithJs("add-item");

        menu = ContextMenuElement.openByRightClick(target);
        ContextMenuElement subMenu = menu.getMenuItems().get(0).openSubMenu();

        List<ContextMenuItemElement> subMenuItems = subMenu.getMenuItems();
        String[] menuItemCaptions = getMenuItemCaptions(subMenuItems);
        Assert.assertArrayEquals(new String[] { "bar", "0", "1" },
                menuItemCaptions);

        subMenuItems.get(1).click();
        menu.waitUntilClosed();
        assertMessage("0");
    }

    @Test
    public void openAndCloseSubMenu_addSubSubMenu_contentUpdatedAndFunctional() {
        ContextMenuElement menu = ContextMenuElement.openByRightClick(target);
        menu.getMenuItems().get(0).openSubMenu();
        clickBody();
        menu.waitUntilClosed();

        clickElementWithJs("add-sub-sub-menu");

        menu = ContextMenuElement.openByRightClick(target);
        ContextMenuElement subMenu = menu.getMenuItems().get(0).openSubMenu();
        ContextMenuElement subSubMenu = subMenu.getMenuItems().get(0)
                .openSubMenu();

        List<ContextMenuItemElement> subSubMenuItems = subSubMenu
                .getMenuItems();
        String[] menuItemCaptions = getMenuItemCaptions(subSubMenuItems);
        Assert.assertArrayEquals(new String[] { "0" }, menuItemCaptions);

        subSubMenuItems.get(0).click();
        menu.waitUntilClosed();
        assertMessage("0");
    }

    @Test
    public void openAndCloseSubMenu_removeAll_noSubMenu_stylesUpdated() {
        ContextMenuElement menu = ContextMenuElement.openByRightClick(target);
        ContextMenuItemElement parent = menu.getMenuItems().get(0);
        assertHasPopup(parent, true);

        parent.hover();
        verifyNumberOfMenus(2);

        clickBody();
        menu.waitUntilClosed();

        clickElementWithJs("remove-all");
        menu = ContextMenuElement.openByRightClick(target);

        parent = menu.getMenuItems().get(0);
        assertHasPopup(parent, false);

        // Should not open a submenu, only the main menu remains
        parent.hover();
        verifyNumberOfMenus(1);
    }

    @Test
    public void componentInsideSubMenu_addComponent_componentIsInSubmenu() {
        findElement(By.id("add-component")).click();
        ContextMenuElement menu = ContextMenuElement.openByRightClick(target);
        ContextMenuElement subMenu = menu.getMenuItems().get(0).openSubMenu();

        WebElement firstItem = getMenuContent(subMenu)
                .$("vaadin-context-menu-list-box").first()
                .findElement(By.xpath("./*"));

        Assert.assertEquals("a",
                firstItem.getTagName().toLowerCase(Locale.ENGLISH));
        Assert.assertEquals("Link", firstItem.getText());
    }

    @Test
    public void checkableItemInsideSubMenu_addCheckableItem_itemIsInSubmenu() {
        findElement(By.id("add-checkable-component")).click();
        ContextMenuElement menu = ContextMenuElement.openByRightClick(target);
        ContextMenuElement subMenu = menu.getMenuItems().get(0).openSubMenu();

        ContextMenuItemElement checkableItem = subMenu.getMenuItem("checkable")
                .orElseThrow();

        // verify checkable item
        Assert.assertTrue(checkableItem.isChecked());

        // uncheck the item
        checkableItem.click();

        menu.waitUntilClosed();

        // We should have a message about selected item:
        assertMessage("Checkable item is false");

        // verify that the item is not checked in UI now
        menu = ContextMenuElement.openByRightClick(target);
        subMenu = menu.getMenuItems().get(0).openSubMenu();

        checkableItem = subMenu.getMenuItem("checkable").orElseThrow();

        Assert.assertFalse(checkableItem.isChecked());
    }

    @Test
    public void clickParentItem_menuNotClosed() {
        ContextMenuElement menu = ContextMenuElement.openByRightClick(target);
        menu.getMenuItems().get(0).click();
        verifyOpened();
    }

    private void assertHasPopup(TestBenchElement item, boolean isParent) {
        boolean hasPopup = Boolean
                .parseBoolean(item.getDomAttribute("aria-haspopup"));
        if (isParent) {
            Assert.assertTrue("Item should have aria-haspopup set to true",
                    hasPopup);
        } else {
            Assert.assertFalse("Item should have aria-haspopup set to false",
                    hasPopup);
        }
    }

    private void assertMessage(String expected) {
        Assert.assertEquals(expected, findElement(By.id("message")).getText());
    }
}
