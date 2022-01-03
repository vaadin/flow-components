/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@TestPath("vaadin-grid/treegrid-changing-hierarchy")
public class TreeGridChangingHierarchyIT extends AbstractComponentIT {

    private TreeGridElement grid;
    private WebElement addItemsToABtn;
    private WebElement addItemsToAABtn;
    private WebElement removeAABtn;
    private WebElement removeABtn;
    private WebElement removeChildrenOfABtn;
    private WebElement removeChildrenOfAAABtn;

    @Before
    public void before() {
        open();
        grid = $(TreeGridElement.class).first();

        List<WebElement> buttons = findElements(By.tagName("button"));
        addItemsToABtn = buttons.get(0);
        addItemsToAABtn = buttons.get(1);
        removeAABtn = buttons.get(2);
        removeABtn = buttons.get(4);
        removeChildrenOfABtn = buttons.get(5);
        removeChildrenOfAAABtn = buttons.get(6);
    }

    @After
    public void after() {

        checkLogsForErrors();
    }

    @Test
    public void removing_items_from_hierarchy() {
        addItemsToABtn.click();
        addItemsToAABtn.click();
        grid.expandWithClick(0);
        grid.expandWithClick(1);
        grid.collapseWithClick(0);
        removeAABtn.click();
        // expand "a" after the reset:
        grid.expandWithClick(0);
        // "a/a" should be removed from a's children:
        Assert.assertEquals("a/b", grid.getCell(1, 0).getText());
    }

    @Test
    public void removing_all_children_from_item() {
        addItemsToABtn.click();
        assertTrue(grid.isRowCollapsed(0, 0));
        // drop added children from backing data source
        removeChildrenOfABtn.click();
        // changes are not refreshed, thus the row should still appear as
        // collapsed
        assertTrue(grid.isRowCollapsed(0, 0));
        // when encountering 0 children, will reset
        grid.expandWithClick(0);
        Assert.assertEquals(3, grid.getRowCount());
        assertFalse(grid.hasExpandToggle(0, 0));
        // verify other items still expand/collapse correctly:
        grid.expandWithClick(1);
        Assert.assertEquals("b/a", grid.getCell(2, 0).getText());
        Assert.assertEquals(4, grid.getRowCount());
        grid.collapseWithClick(1);
        Assert.assertEquals("c", grid.getCell(2, 0).getText());
        Assert.assertEquals(3, grid.getRowCount());
    }

    @Test
    public void removal_of_deeply_nested_items() throws InterruptedException {
        int i = 0;
        for (i = 0; i < 50; i++) {
            runRemovalOfDeeplyNestedItems();
            Thread.sleep(500);
            if (!grid.isRowCollapsed(1, 0)) {
                break;
            }
            before();
        }

        waitUntil(driver -> !grid.isRowCollapsed(1, 0));
        grid.collapseWithClick(1);
        grid.expandWithClick(1);
        Assert.assertEquals("a/a/a", grid.getCell(2, 0).getText());
        assertFalse(grid.hasExpandToggle(2, 0));
    }

    @Test
    public void changing_selection_from_selected_removed_item() {
        addItemsToABtn.click();
        grid.expandWithClick(0);
        grid.getCell(1, 0).click();
        removeChildrenOfABtn.click();
        // HierarchyMapper will notice the removal of the children of a, and
        // mark it as collapsed.
        // grid.collapseWithClick(0);
        grid.getCell(1, 0).click();
        assertTrue(grid.getRow(1).isSelected());
    }

    @Test
    public void remove_item_from_root() {
        addItemsToABtn.click();
        removeABtn.click();
        grid.expandWithClick(0);
        Assert.assertEquals("b", grid.getCell(0, 0).getText());
    }

    private void runRemovalOfDeeplyNestedItems() {
        addItemsToABtn.click();
        addItemsToAABtn.click();
        grid.expandWithClick(0);
        grid.expandWithClick(1);
        grid.expandWithClick(2);
        removeChildrenOfAAABtn.click();
    }
}
