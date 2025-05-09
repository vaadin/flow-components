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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.menubar.testbench.MenuBarButtonElement;
import com.vaadin.flow.component.menubar.testbench.MenuBarElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-menu-bar/menu-bar-theme")
public class MenuBarThemeIT extends AbstractComponentIT {

    public static final String OVERLAY_TAG = "vaadin-menu-bar-overlay";

    private MenuBarElement menuBar;

    @Before
    public void init() {
        open();
        menuBar = $(MenuBarElement.class).first();
    }

    @Test
    public void toggleMenuBarTheme_themeIsToggled() {
        Assert.assertFalse(menuBar.hasAttribute("theme"));
        click("toggle-theme");
        Assert.assertEquals(menuBar.getDomAttribute("theme"),
                MenuBarThemePage.MENU_BAR_THEME);
        click("toggle-theme");
        Assert.assertFalse(menuBar.hasAttribute("theme"));
    }

    @Test
    public void toggleMenuItemTheme_themeIsToggled() {
        MenuBarButtonElement menuButton1 = menuBar.getButtons().get(0);
        Assert.assertFalse(menuButton1.hasAttribute("theme"));
        click("toggle-item-1-theme");
        menuButton1 = menuBar.getButtons().get(0);
        Assert.assertEquals(menuButton1.getDomAttribute("theme"),
                MenuBarThemePage.MENU_ITEM_THEME);
        click("toggle-item-1-theme");
        menuButton1 = menuBar.getButtons().get(0);
        Assert.assertFalse(menuButton1.hasAttribute("theme"));
    }

    @Test
    public void setMenuItemTheme_toggleVisibility_themeIsPreserved() {
        click("toggle-item-1-theme");
        click("toggle-item-1-visibility");
        click("toggle-item-1-visibility");
        MenuBarButtonElement menuButton1 = menuBar.getButtons().get(0);
        Assert.assertEquals(menuButton1.getDomAttribute("theme"),
                MenuBarThemePage.MENU_ITEM_THEME);
    }

    @Test
    public void setMenuItemTheme_hide_resetTheme_show_themeIsUnset() {
        click("toggle-item-1-theme");
        click("toggle-item-1-visibility");
        click("toggle-item-1-theme");
        click("toggle-item-1-visibility");
        MenuBarButtonElement menuButton1 = menuBar.getButtons().get(0);
        Assert.assertFalse(menuButton1.hasAttribute("theme"));
    }

    @Test
    public void toggleSubMenuItemTheme_themeIsToggled() {
        menuBar.getButtons().get(0).click();
        verifyOpened();
        Assert.assertFalse(
                menuBar.getSubMenuItems().get(1).hasAttribute("theme"));

        click("toggle-sub-theme");
        verifyClosed();

        menuBar.getButtons().get(0).click();
        verifyOpened();
        Assert.assertEquals(
                menuBar.getSubMenuItems().get(1).getDomAttribute("theme"),
                MenuBarThemePage.SUB_ITEM_THEME);

        click("toggle-sub-theme");
        verifyClosed();

        menuBar.getButtons().get(0).click();
        verifyOpened();

        Assert.assertFalse(
                menuBar.getSubMenuItems().get(1).hasAttribute("theme"));
    }

    @Test
    public void toggleMenuBarTheme_toggleMenuItemTheme_themeIsOverridden() {
        click("toggle-theme");
        click("toggle-item-1-theme");

        MenuBarButtonElement menuButton1 = menuBar.getButtons().get(0);
        Assert.assertEquals(MenuBarThemePage.MENU_ITEM_THEME,
                menuButton1.getDomAttribute("theme"));
    }

    private void click(String id) {
        findElement(By.id(id)).click();
    }

    public void verifyClosed() {
        waitForElementNotPresent(By.tagName(OVERLAY_TAG));
    }

    public void verifyOpened() {
        waitForElementPresent(By.tagName(OVERLAY_TAG));
    }
}
