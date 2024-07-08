/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
