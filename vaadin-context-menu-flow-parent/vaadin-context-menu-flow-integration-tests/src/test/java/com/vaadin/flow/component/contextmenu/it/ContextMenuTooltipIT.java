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
package com.vaadin.flow.component.contextmenu.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.contextmenu.testbench.ContextMenuElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-context-menu/tooltip")
public class ContextMenuTooltipIT extends AbstractComponentIT {

    private TestBenchElement target;
    private ContextMenuElement contextMenu;
    private TestBenchElement contextMenuTooltip;

    @Before
    public void init() {
        open();
        target = $(TestBenchElement.class).id("target");
        contextMenu = ContextMenuElement.openByRightClick(target);
        contextMenuTooltip = contextMenu.$("vaadin-tooltip").single();
    }

    @Test
    public void hoverOverRootItems_tooltipDisplayed() {
        var items = contextMenu.getMenuItems();

        items.get(0).hover();
        Assert.assertEquals("Item 0 / Tooltip", contextMenuTooltip.getText());

        items.get(1).hover();
        Assert.assertEquals("Item 1 / Tooltip", contextMenuTooltip.getText());

        items.get(2).hover();
        Assert.assertEquals("Item 2 / Tooltip", contextMenuTooltip.getText());
        Assert.assertEquals("top",
                contextMenuTooltip.getDomProperty("_position"));
    }

    @Test
    public void hoverOverSubMenuItems_tooltipDisplayed() {
        var subMenu = contextMenu.getMenuItems().get(0).openSubMenu();
        var subMenuItems = subMenu.getMenuItems();

        subMenuItems.get(0).hover();
        Assert.assertEquals("Item 0-0 / Tooltip", contextMenuTooltip.getText());

        subMenuItems.get(1).hover();
        Assert.assertEquals("Item 0-1 / Tooltip", contextMenuTooltip.getText());

        subMenuItems.get(2).hover();
        Assert.assertEquals("Item 0-2 / Tooltip", contextMenuTooltip.getText());
        Assert.assertEquals("top",
                contextMenuTooltip.getDomProperty("_position"));
    }

    @Test
    public void updateTooltip_updatedTooltipDisplayed() {
        clickElementWithJs("update-tooltips");

        contextMenu.getMenuItems().get(0).hover();
        Assert.assertEquals("Item 0 / Updated Tooltip",
                contextMenuTooltip.getText());

        var subMenu = contextMenu.getMenuItems().get(0).openSubMenu();
        subMenu.getMenuItems().get(0).hover();
        Assert.assertEquals("Item 0-0 / Updated Tooltip",
                contextMenuTooltip.getText());
    }

    @Test
    public void detachAndAttach_hoverOverItems_tooltipDisplayed() {
        detachAndAttach();

        contextMenu.getMenuItems().get(0).hover();
        Assert.assertEquals("Item 0 / Tooltip", contextMenuTooltip.getText());

        var subMenu = contextMenu.getMenuItems().get(0).openSubMenu();
        subMenu.getMenuItems().get(0).hover();
        Assert.assertEquals("Item 0-0 / Tooltip", contextMenuTooltip.getText());
    }

    private void detachAndAttach() {
        clickElementWithJs("detach");
        clickElementWithJs("attach");
        target = $(TestBenchElement.class).id("target");
        contextMenu = ContextMenuElement.openByRightClick(target);
        contextMenuTooltip = contextMenu.$("vaadin-tooltip").single();
    }
}
