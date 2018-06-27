/*
 * Copyright 2000-2018 Vaadin Ltd.
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

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.data.performance.TreeGridMemory;
import com.vaadin.flow.testutil.TestPath;

@TestPath("treegrid-huge-tree")
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
        getDriver().get(getRootURL() + "/" + TreeGridMemory.PATH
                + "/items=200&initiallyExpanded");
        setupTreeGrid();

        TreeGridElement grid = getTreeGrid();

        waitUntil(tets -> grid.getNumberOfExpandedRows() == 99);

        // assuming cache size to be visible row count + buffer before/after
        // assuming buffer to match visible row count
        int assumedCachedSize = (grid.getLastVisibleRowIndex()
                - grid.getFirstVisibleRowIndex()) * 3;
        waitUntil(b -> grid.getRowCount() >= assumedCachedSize);
        waitUntil(test -> !grid.isLoadingExpandedRows());
        String[] cellTexts = new String[assumedCachedSize];
        for (int i = 0; i < assumedCachedSize; i++) {
            cellTexts[i] = grid.getCellWaitForRow(i, 0).getText();
        }
        grid.scrollToRowAndWait(0);

        grid.collapseWithClick(1);
        waitUntil(tets -> grid.getNumberOfExpandedRows() == 98);

        grid.expandWithClick(1);
        waitUntil(tets -> grid.getNumberOfExpandedRows() == 99);
        waitUntil(test -> !grid.isLoadingExpandedRows());

        assertCellTexts(0, 0, cellTexts);
    }

}
