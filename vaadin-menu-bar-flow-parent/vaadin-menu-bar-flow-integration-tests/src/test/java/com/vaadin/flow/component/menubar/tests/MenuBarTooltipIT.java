/*
 * Copyright 2022 Vaadin Ltd.
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
