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

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.TestPath;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@TestPath("vaadin-grid/" + TreeGridBasicFeaturesPage.VIEW)
public class TreeGridSelectIT extends AbstractTreeGridIT {

    @Before
    public void before() {
        open();

        setupTreeGrid();
    }

    @Test
    public void select_and_deselect_all() {
        findElement(By.id("TreeDataProvider")).click();
        findElementByText("Selection mode - multi").click();

        assertAllRowsDeselected(getTreeGrid());
        clickSelectAll(getTreeGrid());
        assertAllRowsSelected(getTreeGrid());
        getTreeGrid().expandWithClick(1, 1);
        getTreeGrid().expandWithClick(2, 1);
        assertAllRowsSelected(getTreeGrid());
        clickSelectAll(getTreeGrid());
        assertAllRowsDeselected(getTreeGrid());
        clickSelectAll(getTreeGrid());
        getTreeGrid().collapseWithClick(2, 1);
        getTreeGrid().expandWithClick(2, 1);
        assertAllRowsSelected(getTreeGrid());
        getTreeGrid().collapseWithClick(2, 1);
        clickSelectAll(getTreeGrid());
        getTreeGrid().expandWithClick(2, 1);
        assertAllRowsDeselected(getTreeGrid());
    }

    private void assertAllRowsSelected(TreeGridElement grid) {
        for (int i = 0; i < grid.getRowCount(); i++) {
            assertTrue(grid.getRow(i).isSelected());
        }
    }

    private void assertAllRowsDeselected(TreeGridElement grid) {
        for (int i = 0; i < grid.getRowCount(); i++) {
            assertFalse(grid.getRow(i).isSelected());
        }
    }

    private void clickSelectAll(TreeGridElement grid) {
        grid.findElement(By.id("selectAllCheckbox")).click();
    }
}
