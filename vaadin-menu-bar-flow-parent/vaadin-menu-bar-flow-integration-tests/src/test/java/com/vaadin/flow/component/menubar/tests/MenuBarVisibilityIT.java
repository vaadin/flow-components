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

import com.vaadin.flow.component.menubar.testbench.MenuBarButtonElement;
import com.vaadin.flow.component.menubar.testbench.MenuBarElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-menu-bar/menu-bar-visibility")
public class MenuBarVisibilityIT extends AbstractComponentIT {
    private MenuBarElement menuBar;
    private TestBenchElement toggleMenuBarVisibility;
    private TestBenchElement toggleMenuItemVisibility;
    private TestBenchElement toggleOtherItemVisibility;
    private TestBenchElement toggleMenuItemEnabled;

    @Before
    public void init() {
        open();
        menuBar = $(MenuBarElement.class).waitForFirst();
        toggleMenuBarVisibility = $("button").id("toggle-menu-bar-visibility");
        toggleMenuItemVisibility = $("button")
                .id("toggle-menu-item-visibility");
        toggleOtherItemVisibility = $("button")
                .id("toggle-other-item-visibility");
        toggleMenuItemEnabled = $("button").id("toggle-menu-item-enabled");
    }

    @Test
    public void hide_disableMenuItem_show_buttonIsDisabled() {
        // Hide the menu bar.
        toggleMenuBarVisibility.click();
        Assert.assertFalse(menuBar.isDisplayed());

        // Disable the menu item.
        toggleMenuItemEnabled.click();

        // Show the menu bar.
        toggleMenuBarVisibility.click();
        Assert.assertTrue(menuBar.isDisplayed());

        // Check that the menu item is disabled.
        MenuBarButtonElement button = menuBar.getButtons().get(0);
        Assert.assertTrue(button.getPropertyBoolean("disabled"));
    }

    @Test
    public void hideMenuItem_hide_showMenuItem_show_buttonIsVisible() {
        // Hide the menu item.
        toggleMenuItemVisibility.click();
        Assert.assertTrue(menuBar.getButtons().isEmpty());

        // Hide the menu bar.
        toggleMenuBarVisibility.click();
        Assert.assertFalse(menuBar.isDisplayed());

        // Show the menu item.
        toggleMenuItemVisibility.click();

        // Show the menu bar.
        toggleMenuBarVisibility.click();
        Assert.assertTrue(menuBar.isDisplayed());

        // Check that the menu item is visible.
        Assert.assertFalse(menuBar.getButtons().isEmpty());
    }

    @Test
    public void hideMenuItem_showOtherMenuItem_buttonIsVisible() {
        // Hide the menu item.
        toggleMenuItemVisibility.click();
        Assert.assertTrue(menuBar.getButtons().isEmpty());

        // Show other menu item.
        toggleOtherItemVisibility.click();
        Assert.assertFalse(menuBar.getButtons().isEmpty());
    }
}
