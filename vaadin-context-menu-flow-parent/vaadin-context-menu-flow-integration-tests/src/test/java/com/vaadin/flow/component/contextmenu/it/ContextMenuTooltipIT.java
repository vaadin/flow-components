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
import org.junit.Ignore;
import org.junit.Test;

import com.vaadin.flow.component.contextmenu.testbench.ContextMenuElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-context-menu/tooltip")
public class ContextMenuTooltipIT extends AbstractComponentIT {

    private TestBenchElement target;

    @Before
    public void init() {
        open();
        target = $(TestBenchElement.class).id("target");
    }

    @Test
    public void openMenu_hoverOverRootItems_tooltipDisplayed() {
        var contextMenu = ContextMenuElement.openByRightClick(target);

        var items = contextMenu.getMenuItems();

        items.get(0).hover();
        Assert.assertEquals("Item 0 / Tooltip", getTooltipText(contextMenu));

        items.get(1).hover();
        Assert.assertEquals("Item 1 / Tooltip", getTooltipText(contextMenu));

        items.get(2).hover();
        Assert.assertEquals("Item 2 / Tooltip", getTooltipText(contextMenu));
        Assert.assertEquals("top", getTooltipPosition(contextMenu));
    }

    @Test
    public void openMenu_hoverOverSubMenuItems_tooltipDisplayed() {
        var contextMenu = ContextMenuElement.openByRightClick(target);

        var subMenu = contextMenu.getMenuItems().get(0).openSubMenu();
        var subMenuItems = subMenu.getMenuItems();

        subMenuItems.get(0).hover();
        Assert.assertEquals("Item 0-0 / Tooltip", getTooltipText(contextMenu));

        subMenuItems.get(1).hover();
        Assert.assertEquals("Item 0-1 / Tooltip", getTooltipText(contextMenu));

        subMenuItems.get(2).hover();
        Assert.assertEquals("Item 0-2 / Tooltip", getTooltipText(contextMenu));
        Assert.assertEquals("top", getTooltipPosition(contextMenu));
    }

    @Test
    public void updateTooltip_openMenu_hoverOverItems_updatedTooltipDisplayed() {
        clickElementWithJs("update-tooltips");

        var contextMenu = ContextMenuElement.openByRightClick(target);

        contextMenu.getMenuItems().get(0).hover();
        Assert.assertEquals("Item 0 / Updated Tooltip",
                getTooltipText(contextMenu));

        var subMenu = contextMenu.getMenuItems().get(0).openSubMenu();
        subMenu.getMenuItems().get(0).hover();
        Assert.assertEquals("Item 0-0 / Updated Tooltip",
                getTooltipText(contextMenu));
    }

    @Test
    @Ignore("Flaky test, needs investigation")
    public void detachAndAttach_openMenu_hoverOverItems_tooltipDisplayed() {
        detachAndAttach();

        var contextMenu = ContextMenuElement.openByRightClick(target);

        contextMenu.getMenuItems().get(0).hover();
        Assert.assertEquals("Item 0 / Tooltip", getTooltipText(contextMenu));

        var subMenu = contextMenu.getMenuItems().get(0).openSubMenu();
        subMenu.getMenuItems().get(0).hover();
        Assert.assertEquals("Item 0-0 / Tooltip", getTooltipText(contextMenu));
    }

    private void detachAndAttach() {
        clickElementWithJs("detach");
        clickElementWithJs("attach");
        target = $(TestBenchElement.class).id("target");
    }

    private String getTooltipText(ContextMenuElement contextMenu) {
        return contextMenu.$("vaadin-tooltip").single().getText();
    }

    private String getTooltipPosition(ContextMenuElement contextMenu) {
        return contextMenu.$("vaadin-tooltip").single()
                .getDomProperty("_position");
    }
}
