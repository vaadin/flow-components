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
package com.vaadin.flow.component.treegrid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/disabled-tree-grid")
public class DisabledTreeGridIT extends AbstractComponentIT {
    private TreeGridElement treeGrid;

    @Before
    public void init() {
        open();
        treeGrid = $(TreeGridElement.class).first();

        // Simulate an unauthorized enabling attempt on the client-side
        treeGrid.getCommandExecutor().executeScript(
                "arguments[0].removeAttribute('disabled')", treeGrid);
    }

    @Test
    public void triggerSetRequestedRange_serverCallAllowed() {
        clickElementWithJs("set-all-rows-visible");
        Assert.assertEquals("Item 99", treeGrid.getCell(99, 0).getText());
    }

    @Test
    public void triggerSetRequestedRangeByIndexPath_serverCallAllowed() {
        clickElementWithJs("scroll-to-end");
        Assert.assertEquals(99, treeGrid.getLastVisibleRowIndex());
    }

    @Test
    public void triggerUpdateExpandedState_serverCallIgnored() {
        treeGrid.getCell(0, 0).focus();
        new Actions(getDriver()).sendKeys(Keys.ARROW_LEFT, Keys.ARROW_RIGHT)
                .perform();
        Assert.assertEquals("Item 1", treeGrid.getCell(1, 0).getText());
    }
}
