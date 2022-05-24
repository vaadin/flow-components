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

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridColumnElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.testutil.TestPath;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-grid/treegrid-expand-all")
public class TreeGridExpandAllIT extends AbstractTreeGridIT {

    private static final int ATTEMPTS = 50;

    @Test
    public void addNewItemAfterCollapseAndExpand() throws InterruptedException {
        runAddNewItemAfterCollapseAndExpand();

        int i = 0;
        while (getTreeGrid().getRowCount() != 6) {
            i++;
            if (i > ATTEMPTS) {
                Assert.fail(
                        "The TreeGrid doesn't have required nbumber of rows after adding a grandson.");
            }
            runAddNewItemAfterCollapseAndExpand();
        }
        assertNewChild();
    }

    @Test
    public void recalculateWidthsAfterExpend() throws InterruptedException {
        open();
        TreeGridElement grid = $(TreeGridElement.class).get(1);
        WebElement expandToggleElement = grid.getExpandToggleElement(1, 0);
        int widthBeforeExpend = grid.getCell(1, 0).getSize().getWidth();

        expandToggleElement.click();
        waitUntil(e -> {
            int widthAfterExpend = grid.getCell(1, 0).getSize().getWidth();
            return widthBeforeExpend != widthAfterExpend;
        }, 200);

        grid.collapseWithClick(1);
        waitUntil(e -> {
            int widthAfterCollapse = grid.getCell(1, 0).getSize().getWidth();
            return widthBeforeExpend == widthAfterCollapse;
        }, 200);
    }

    private void runAddNewItemAfterCollapseAndExpand()
            throws InterruptedException {
        open();

        setupTreeGrid();

        waitUntil(driver -> getTreeGrid().getRowCount() == 5);

        $("button").id("collapse").click();
        $("button").id("expand").click();
        waitUntil(driver -> getTreeGrid().getRowCount() == 5);

        findElement(By.id("add-new")).click();

        if (getTreeGrid().getRowCount() != 6) {
            Thread.sleep(500);
        }
    }

    private void assertNewChild() {
        GridTRElement row = getTreeGrid().getRow(5);
        List<GridColumnElement> columns = getTreeGrid().getAllColumns();
        Assert.assertEquals("New son", row.getCell(columns.get(0)).getText());
    }
}
