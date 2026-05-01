/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import com.vaadin.flow.component.menubar.testbench.MenuBarElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-menu-bar/tooltip")
public class MenuBarTooltipIT extends AbstractComponentIT {

    private MenuBarElement menuBar;
    private TestBenchElement menuBarTooltip;

    @Before
    public void init() {
        open();
        menuBar = $(MenuBarElement.class).single();
        menuBarTooltip = menuBar.$("vaadin-tooltip").single();
    }

    @Test
    public void hoverOverRootItems_tooltipDisplayed() {
        var buttons = menuBar.getButtons();

        buttons.get(0).hover();
        Assert.assertEquals("Item 0 / Tooltip", menuBarTooltip.getText());

        buttons.get(1).hover();
        Assert.assertEquals("Item 1 / Tooltip", menuBarTooltip.getText());

        buttons.get(2).hover();
        Assert.assertEquals("Item 2 / Tooltip", menuBarTooltip.getText());
        Assert.assertEquals("top", menuBarTooltip.getDomProperty("_position"));
    }

    @Test
    public void hoverOverSubMenuItems_tooltipDisplayed() {
        var subMenu = menuBar.getButtons().get(0).openSubMenu();
        var subMenuItems = subMenu.getMenuItems();

        subMenuItems.get(0).hover();
        Assert.assertEquals("Item 0-0 / Tooltip", menuBarTooltip.getText());

        subMenuItems.get(1).hover();
        Assert.assertEquals("Item 0-1 / Tooltip", menuBarTooltip.getText());

        subMenuItems.get(2).hover();
        Assert.assertEquals("Item 0-2 / Tooltip", menuBarTooltip.getText());
        Assert.assertEquals("top", menuBarTooltip.getDomProperty("_position"));
    }

    @Test
    public void updateTooltip_updatedTooltipDisplayed() {
        clickElementWithJs("update-tooltips");

        menuBar.getButtons().get(0).hover();
        Assert.assertEquals("Item 0 / Updated Tooltip", menuBarTooltip.getText());

        var subMenu = menuBar.getButtons().get(0).openSubMenu();
        subMenu.getMenuItems().get(0).hover();
        Assert.assertEquals("Item 0-0 / Updated Tooltip", menuBarTooltip.getText());
    }

    @Test
    public void detachAndAttach_hoverOverItems_tooltipDisplayed() {
        detachAndAttach();

        menuBar.getButtons().get(0).hover();
        Assert.assertEquals("Item 0 / Tooltip", menuBarTooltip.getText());

        var subMenu = menuBar.getButtons().get(0).openSubMenu();
        subMenu.getMenuItems().get(0).hover();
        Assert.assertEquals("Item 0-0 / Tooltip", menuBarTooltip.getText());
    }

    private void detachAndAttach() {
        clickElementWithJs("detach");
        clickElementWithJs("attach");
        menuBar = $(MenuBarElement.class).single();
        menuBarTooltip = menuBar.$("vaadin-tooltip").single();
    }
}
