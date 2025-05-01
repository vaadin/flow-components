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
package com.vaadin.flow.component.menubar.tests;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.menubar.testbench.MenuBarButtonElement;
import com.vaadin.flow.component.menubar.testbench.MenuBarElement;
import com.vaadin.flow.component.menubar.testbench.MenuBarItemElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-menu-bar/menu-bar-class-names")
public class MenuBarClassNamesIT extends AbstractComponentIT {

    public static final String OVERLAY_TAG = "vaadin-menu-bar-overlay";

    private MenuBarElement menuBar;

    @Before
    public void init() {
        open();
        menuBar = $(MenuBarElement.class).first();
    }

    @Test
    public void toggleMenuItemClassName_classNameIsToggled() {
        MenuBarButtonElement menuButton1 = menuBar.getButtons().get(0);
        Assert.assertFalse(menuButton1.hasAttribute("class"));
        click("toggle-item1-class-name");
        menuButton1 = menuBar.getButtons().get(0);
        Assert.assertEquals(
                Set.of(MenuBarClassNamesPage.MENU_ITEM_FIRST_CLASS_NAME),
                menuButton1.getClassNames());
        click("toggle-item1-class-name");
        menuButton1 = menuBar.getButtons().get(0);
        Assert.assertEquals(Set.of(), menuButton1.getClassNames());
    }

    @Test
    public void setMenuItemClassName_classNameIsSet() {
        MenuBarButtonElement menuButton1 = menuBar.getButtons().get(0);
        Assert.assertFalse(menuButton1.hasAttribute("class"));
        click("toggle-item1-class-name");
        menuButton1 = menuBar.getButtons().get(0);
        Assert.assertEquals(
                Set.of(MenuBarClassNamesPage.MENU_ITEM_FIRST_CLASS_NAME),
                menuButton1.getClassNames());
        click("set-item1-class-name");
        menuButton1 = menuBar.getButtons().get(0);
        Assert.assertEquals(
                Set.of(MenuBarClassNamesPage.MENU_ITEM_SECOND_CLASS_NAME),
                menuButton1.getClassNames());
    }

    @Test
    public void toggleMenuItemClassNameWithSetClassName_classNameIsToggled() {
        MenuBarButtonElement menuButton1 = menuBar.getButtons().get(0);
        Assert.assertFalse(menuButton1.hasAttribute("class"));
        click("set-unset-item1-class-name");
        menuButton1 = menuBar.getButtons().get(0);
        Assert.assertEquals(
                Set.of(MenuBarClassNamesPage.MENU_ITEM_FIRST_CLASS_NAME),
                menuButton1.getClassNames());
        click("set-unset-item1-class-name");
        menuButton1 = menuBar.getButtons().get(0);
        Assert.assertEquals(Set.of(), menuButton1.getClassNames());
    }

    @Test
    public void toggleMultipleItemClassName_classNamesAreToggled() {
        MenuBarButtonElement menuButton1 = menuBar.getButtons().get(0);
        Assert.assertFalse(menuButton1.hasAttribute("class"));
        click("add-remove-multiple-classes");
        menuButton1 = menuBar.getButtons().get(0);
        Assert.assertEquals(
                Set.of(MenuBarClassNamesPage.MENU_ITEM_FIRST_CLASS_NAME,
                        MenuBarClassNamesPage.MENU_ITEM_SECOND_CLASS_NAME),
                menuButton1.getClassNames());
        click("add-remove-multiple-classes");
        menuButton1 = menuBar.getButtons().get(0);
        Assert.assertEquals(Set.of(), menuButton1.getClassNames());
    }

    @Test
    public void subMenuHasClassName_callRemoveClassName_classNameIsRemoved() {
        verifySubMenuItemClassNames(true,
                MenuBarClassNamesPage.SUB_ITEM_FIRST_CLASS_NAME);

        closeSubMenu();
        click("remove-sub-item-class-name");

        verifySubMenuItemClassNames(false,
                MenuBarClassNamesPage.SUB_ITEM_FIRST_CLASS_NAME);

        closeSubMenu();
    }

    @Test
    public void subMenuItem_toggleMultipleClassNames_classNamesAreToggled() {
        click("add-second-sub-item-class-name");

        verifySubMenuItemClassNames(true,
                MenuBarClassNamesPage.SUB_ITEM_FIRST_CLASS_NAME,
                MenuBarClassNamesPage.SUB_ITEM_SECOND_CLASS_NAME);

        closeSubMenu();
        click("add-remove-multiple-sub-item-classes");

        verifySubMenuItemClassNames(false,
                MenuBarClassNamesPage.SUB_ITEM_FIRST_CLASS_NAME,
                MenuBarClassNamesPage.SUB_ITEM_SECOND_CLASS_NAME);

        closeSubMenu();
        click("add-remove-multiple-sub-item-classes");

        verifySubMenuItemClassNames(true,
                MenuBarClassNamesPage.SUB_ITEM_FIRST_CLASS_NAME,
                MenuBarClassNamesPage.SUB_ITEM_SECOND_CLASS_NAME);

        closeSubMenu();
    }

    @Test
    public void subMenuItem_toggleSingleClassName_classNameIsToggled() {
        click("toggle-sub-item-class-name");

        verifySubMenuItemClassNames(false,
                MenuBarClassNamesPage.SUB_ITEM_FIRST_CLASS_NAME);

        closeSubMenu();
        click("toggle-sub-item-class-name");

        verifySubMenuItemClassNames(true,
                MenuBarClassNamesPage.SUB_ITEM_FIRST_CLASS_NAME);

        closeSubMenu();
    }

    @Test
    public void subMenuItem_classNamesAreToggleWithSet_classNamesAreToggled() {
        click("set-unset-sub-item-class-name");

        verifySubMenuItemClassNames(false,
                MenuBarClassNamesPage.SUB_ITEM_FIRST_CLASS_NAME);

        closeSubMenu();
        click("set-unset-sub-item-class-name");

        verifySubMenuItemClassNames(true,
                MenuBarClassNamesPage.SUB_ITEM_FIRST_CLASS_NAME);

        closeSubMenu();
    }

    @Test
    public void menuItemWithClassNameInOverflow_changeClassName_classNameIsChanged() {
        click("set-width");
        click("set-item2-class-name");
        waitForResizeObserver();
        menuBar.getOverflowButton().click();
        click("change-item2-class-name");
        menuBar.getOverflowButton().click();
        MenuBarItemElement menuItem = menuBar.getSubMenuItems().get(0);
        Assert.assertEquals(
                Set.of(MenuBarClassNamesPage.MENU_ITEM_SECOND_CLASS_NAME),
                menuItem.getClassNames());
    }

    @Test
    public void menuItemWithClassNameInOverflow_removeClassName_classNameIsRemoved() {
        click("set-width");
        click("set-item2-class-name");
        waitForResizeObserver();
        menuBar.getOverflowButton().click();
        click("remove-item2-class-name");
        menuBar.getOverflowButton().click();
        MenuBarItemElement menuItem = menuBar.getSubMenuItems().get(0);
        Assert.assertEquals(Set.of(), menuItem.getClassNames());
    }

    @Test
    public void menuItemWithClassNameInOverflow_menuItemLeavesOverflow_classNameCanBeChanged() {
        click("set-width");
        click("set-item2-class-name");
        waitForResizeObserver();
        menuBar.getOverflowButton().click();
        click("reset-width");
        waitForResizeObserver();
        click("change-item2-class-name");
        MenuBarButtonElement menuItem = menuBar.getButtons().get(1);
        Assert.assertEquals(
                Set.of(MenuBarClassNamesPage.MENU_ITEM_SECOND_CLASS_NAME),
                menuItem.getClassNames());

        click("remove-item2-class-name");
        menuItem = menuBar.getButtons().get(1);
        Assert.assertEquals(Set.of(), menuItem.getClassNames());
    }

    private void click(String id) {
        findElement(By.id(id)).click();
    }

    public void closeSubMenu() {
        $("body").first().click();
        verifyClosed();
    }

    private void openSubMenu() {
        menuBar.getButtons().get(0).click();
        verifyOpened();
    }

    public void verifyClosed() {
        waitForElementNotPresent(By.tagName(OVERLAY_TAG));
    }

    public void verifyOpened() {
        waitForElementPresent(By.tagName(OVERLAY_TAG));
    }

    private void waitForResizeObserver() {
        getCommandExecutor().getDriver().executeAsyncScript(
                "var callback = arguments[arguments.length - 1];"
                        + "requestAnimationFrame(callback)");
    }

    private void verifySubMenuItemClassNames(boolean containsClassNames,
            String... classNames) {
        openSubMenu();
        MenuBarItemElement subMenuItem = menuBar.getSubMenuItems().get(2);
        var subMenuItemClassNames = subMenuItem.getClassNames();
        for (String className : classNames) {
            if (containsClassNames) {
                Assert.assertTrue(subMenuItemClassNames.contains(className));
            } else {
                Assert.assertFalse(subMenuItemClassNames.contains(className));
            }
        }
    }
}
