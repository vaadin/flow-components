package com.vaadin.flow.component.menubar.tests;

import com.vaadin.flow.component.menubar.testbench.MenuBarElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-menu-bar/menu-bar-visibility")
public class MenuBarVisibilityIT extends AbstractComponentIT {
    private MenuBarElement menuBar;
    private TestBenchElement toggleMenuBarVisibility;
    private TestBenchElement toggleMenuItemVisibility;
    private TestBenchElement toggleMenuItemEnabled;

    @Before
    public void init() {
        open();
        menuBar = $(MenuBarElement.class).waitForFirst();
        toggleMenuBarVisibility = $("button").id("toggle-menu-bar-visibility");
        toggleMenuItemVisibility = $("button")
                .id("toggle-menu-item-visibility");
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
        TestBenchElement button = menuBar.getButtons().get(0);
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
}
