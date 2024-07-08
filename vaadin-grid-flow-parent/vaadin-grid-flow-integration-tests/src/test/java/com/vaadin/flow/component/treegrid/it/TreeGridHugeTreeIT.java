/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import java.util.List;

import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.data.performance.TreeGridMemory;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/treegrid-huge-tree")
public class TreeGridHugeTreeIT extends AbstractTreeGridIT {

    @Test
    public void toggle_expand_when_row_out_of_cache() {
        open();
        setupTreeGrid();

        TreeGridElement grid = getTreeGrid();

        List<WebElement> buttons = findElements(By.tagName("button"));
        WebElement expandSecondRowButton = buttons.get(0);
        WebElement collapseSecondRowButton = buttons.get(1);

        grid.expandWithClick(2);
        grid.expandWithClick(3);
        grid.scrollToRow(300);

        expandSecondRowButton.click();

        grid.scrollToRow(0);
        assertCellTexts(0, 0, new String[] { "Granddad 0", "Granddad 1",
                "Dad 1/0", "Dad 1/1", "Dad 1/2", "Granddad 2", "Dad 2/0" });

        grid.scrollToRow(300);
        collapseSecondRowButton.click();
        grid.scrollToRow(0);
        assertCellTexts(0, 0, new String[] { "Granddad 0", "Granddad 1",
                "Granddad 2", "Dad 2/0" });

        grid.scrollToRow(300);
        expandSecondRowButton.click();
        collapseSecondRowButton.click();
        grid.scrollToRow(0);
        assertCellTexts(0, 0, new String[] { "Granddad 0", "Granddad 1",
                "Granddad 2", "Dad 2/0" });
    }

    @Test
    public void collapsed_rows_invalidated_correctly() {
        open();
        setupTreeGrid();

        TreeGridElement grid = getTreeGrid();

        grid.expandWithClick(2);
        grid.expandWithClick(3);
        grid.expandWithClick(0);
        grid.collapseWithClick(0);
        grid.expandWithClick(0);
        grid.expandWithClick(1);
        assertCellTexts(0, 0,
                new String[] { "Granddad 0", "Dad 0/0", "Son 0/0/0" });
    }

    @Test
    public void collapsed_subtrees_outside_of_cache_stay_expanded() {
        getDriver().get(getRootURL() + "/vaadin-grid/" + TreeGridMemory.PATH
                + "/items=200&initiallyExpanded");
        setupTreeGrid();

        TreeGridElement grid = getTreeGrid();

        waitUntil(tets -> grid.getNumberOfExpandedRows() == 99);

        // assuming cache size to be visible row count + buffer before/after
        // assuming buffer to match visible row count
        int assumedCachedSize = (grid.getLastVisibleRowIndex()
                - grid.getFirstVisibleRowIndex()) * 3;
        waitUntil(b -> grid.getRowCount() >= assumedCachedSize);
        waitUntil(test -> !grid.isLoadingExpandedRows(), 20);
        String[] cellTexts = new String[assumedCachedSize];
        for (int i = 0; i < assumedCachedSize; i++) {
            cellTexts[i] = grid.getCellWaitForRow(i, 0).getText();
        }
        grid.scrollToRowAndWait(0);

        grid.collapseWithClick(1);
        waitUntil(tets -> grid.getNumberOfExpandedRows() == 98);

        grid.expandWithClick(1);
        waitUntil(tets -> grid.getNumberOfExpandedRows() == 99);
        waitUntil(test -> !grid.isLoadingExpandedRows(), 25);

        assertCellTexts(0, 0, cellTexts);
    }

    @Test
    public void expanded_nodes_populate_after_scroll() {
        open();
        setupTreeGrid();

        TreeGridElement grid = getTreeGrid();

        List<WebElement> buttons = findElements(By.tagName("button"));
        // Init larger data set -button
        buttons.get(2).click();
        // Expand recursively -button
        buttons.get(3).click();

        // Scroll as far as possible
        grid.scrollToRowAndWait(1000000);
        assertExpandedNodesPopulated(grid);

        // Repeat
        grid.scrollToRowAndWait(1000000);
        assertExpandedNodesPopulated(grid);
    }

    private void assertExpandedNodesPopulated(TreeGridElement grid) {
        int firstVisibleIndex = grid.getFirstVisibleRowIndex();
        int lastVisibleIndex = grid.getLastVisibleRowIndex();
        for (int i = lastVisibleIndex; i >= firstVisibleIndex; i--) {
            String cellText = grid.getCell(i, 0).getText();
            if (cellText.contains("Dad")) {
                String sonText = grid.getCell(i + 1, 0).getText();
                Assert.assertThat(sonText + " is not a Son item", sonText,
                        StringContains.containsString("Son"));

            }
        }
    }

}
