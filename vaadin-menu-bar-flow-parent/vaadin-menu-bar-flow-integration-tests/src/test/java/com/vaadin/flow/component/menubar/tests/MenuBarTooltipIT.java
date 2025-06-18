/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.menubar.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.menubar.testbench.MenuBarElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-menu-bar/tooltip")
public class MenuBarTooltipIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void hoverOverMenuBarButton_showTooltip() {
        var menuBar = $(MenuBarElement.class).first();

        showTooltip(menuBar.getButtons().get(0));
        Assert.assertEquals("Edit tooltip", getActiveTooltipText());

        showTooltip(menuBar.getButtons().get(1));
        Assert.assertEquals("Share tooltip", getActiveTooltipText());

        showTooltip(menuBar.getButtons().get(2));
        Assert.assertEquals("Move tooltip", getActiveTooltipText());

        showTooltip(menuBar.getButtons().get(3));
        Assert.assertEquals("Duplicate tooltip", getActiveTooltipText());
    }

    @Test
    public void toggleAttached_hoverOverTooltipColumnCell_showTooltip() {
        // Remove the menubar
        clickElementWithJs("toggle-attached-button");
        // Add the menubar
        clickElementWithJs("toggle-attached-button");

        var menuBar = $(MenuBarElement.class).first();
        showTooltip(menuBar.getButtons().get(0));
        Assert.assertEquals("Edit tooltip", getActiveTooltipText());
    }

    @Test
    public void updateTooltipText_showUpdatedTooltip() {
        var menuBar = $(MenuBarElement.class).first();

        // Udpate tooltip text
        clickElementWithJs("update-item-tooltip-button");

        showTooltip(menuBar.getButtons().get(0));
        Assert.assertEquals("Updated Edit tooltip", getActiveTooltipText());
    }

    private void showTooltip(TestBenchElement button) {
        executeScript(
                "arguments[0].dispatchEvent(new Event('mouseover', {bubbles:true}))",
                button);
        waitForElementPresent(By.tagName("vaadin-tooltip-overlay"));
    }

    private String getActiveTooltipText() {
        return findElement(By.tagName("vaadin-tooltip-overlay")).getText();
    }
}
