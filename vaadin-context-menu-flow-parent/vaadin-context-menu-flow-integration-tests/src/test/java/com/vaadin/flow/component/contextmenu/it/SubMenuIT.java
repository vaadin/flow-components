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

    @Before
    public void init() {
        open();
        verifyClosed();
    }

    @Test
    public void addItemToSubMenu_subMenuRendered_clickListenerWorks() {
        rightClickOn("target");
        verifyNumberOfMenus(1);

        openSubMenu(getMenuItems().get(0));
        verifyNumberOfMenus(2);

        List<ContextMenuElement> menus = getAllMenus();
        ContextMenuItemElement subItem = getMenuItems(menus.get(1)).get(0);
        Assert.assertEquals("bar", subItem.getText());

        subItem.click();
        verifyClosed();
        assertMessage("bar");
    }

    @Test
    public void openAndCloseSubMenu_addContent_contentUpdatedAndFunctional() {
        rightClickOn("target");
        openSubMenu(getMenuItems().get(0));
        verifyNumberOfMenus(2);
        clickBody();
        verifyClosed();

        clickElementWithJs("add-item");
        clickElementWithJs("add-item");

        rightClickOn("target");

        openSubMenu(getMenuItems().get(0));
        verifyNumberOfMenus(2);

        List<ContextMenuElement> menus = getAllMenus();
        List<ContextMenuItemElement> subMenuItems = getMenuItems(menus.get(1));
        String[] menuItemCaptions = getMenuItemCaptions(subMenuItems);
        Assert.assertArrayEquals(new String[] { "bar", "0", "1" },
                menuItemCaptions);

        subMenuItems.get(1).click();
        verifyClosed();
        assertMessage("0");
    }

    @Test
    public void openAndCloseSubMenu_addSubSubMenu_contentUpdatedAndFunctional() {
        rightClickOn("target");
        openSubMenu(getMenuItems().get(0));
        clickBody();

        clickElementWithJs("add-sub-sub-menu");

        rightClickOn("target");

        openSubMenu(getMenuItems().get(0));
        verifyNumberOfMenus(2);

        openSubMenu(getMenuItems(getAllMenus().get(1)).get(0));
        verifyNumberOfMenus(3);

        List<ContextMenuElement> menus = getAllMenus();
        List<ContextMenuItemElement> subMenuItems = getMenuItems(menus.get(2));
        String[] menuItemCaptions = getMenuItemCaptions(subMenuItems);
        Assert.assertArrayEquals(new String[] { "0" }, menuItemCaptions);

        subMenuItems.get(0).click();
        verifyClosed();
        assertMessage("0");
    }

    @Test
    public void openAndCloseSubMenu_removeAll_noSubMenu_stylesUpdated() {
        rightClickOn("target");
        ContextMenuItemElement parent = getMenuItems().get(0);
        assertHasPopup(parent, true);

        openSubMenu(parent);
        verifyNumberOfMenus(2);

        clickBody();
        verifyClosed();

        clickElementWithJs("remove-all");
        rightClickOn("target");

        parent = getMenuItems().get(0);
        assertHasPopup(parent, false);

        openSubMenu(parent);
        verifyNumberOfMenus(1);
    }

    @Test
    public void componentInsideSubMenu_addComponent_componentIsInSubmenu() {
        findElement(By.id("add-component")).click();
        rightClickOn("target");

        openSubMenu(getMenuItems().get(0));

        verifyNumberOfMenus(2);

        ContextMenuElement subMenu = getAllMenus().get(1);

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
        rightClickOn("target");

        openSubMenu(getMenuItems().get(0));

        verifyNumberOfMenus(2);

        ContextMenuElement subMenu = getAllMenus().get(1);

        WebElement checkableItem = getMenuContent(subMenu)
                .$("vaadin-context-menu-list-box").first()
                .findElements(By.xpath("./*")).get(1);

        // verify checkable item
        Assert.assertEquals("vaadin-context-menu-item",
                checkableItem.getTagName().toLowerCase(Locale.ENGLISH));
        Assert.assertEquals("checkable", checkableItem.getText());
        Assert.assertEquals("",
                checkableItem.getDomAttribute("menu-item-checked"));

        // uncheck the item
        checkableItem.click();

        verifyClosed();

        // We should have a message about selected item:
        assertMessage("Checkable item is false");

        // verify that the item is not checked in UI now
        rightClickOn("target");

        openSubMenu(getMenuItems().get(0));

        verifyNumberOfMenus(2);

        subMenu = getAllMenus().get(1);

        checkableItem = getMenuContent(subMenu)
                .$("vaadin-context-menu-list-box").first()
                .findElements(By.xpath("./*")).get(1);

        Assert.assertNull(checkableItem.getDomAttribute("menu-item-checked"));
    }

    @Test
    public void clickParentItem_menuNotClosed() {
        rightClickOn("target");
        getMenuItems().get(0).click();
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
