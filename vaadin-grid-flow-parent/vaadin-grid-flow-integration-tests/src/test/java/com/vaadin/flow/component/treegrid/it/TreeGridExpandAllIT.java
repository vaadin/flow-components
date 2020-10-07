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

import com.vaadin.flow.component.grid.testbench.GridColumnElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.List;

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
        int widthBeforeExpend = grid.getCell(1,0).getSize().getWidth();
        expandToggleElement.click();
        grid.getCell(1,0);
        int widthAfterExpend = grid.getCell(1,0).getSize().getWidth();
        Assert.assertNotEquals(widthBeforeExpend, widthAfterExpend);
        grid.collapseWithClick(1);
        int widthAfterCollapse = grid.getCell(1,0).getSize().getWidth();
        Assert.assertEquals(widthBeforeExpend, widthAfterCollapse);
    }

    @Test
    public void loadingIsFalseAfterScrolling() {
        open();
        TreeGridElement grid = $(TreeGridElement.class).get(1);
        findElement(By.id("expand-tree")).click();

        List<Integer> scrollValues = Arrays.asList(44958, 5600, 10600, 20650,
                25650, 30150, 35700, 38700, 33200, 30150);

        // If updating of the scrollTop for table is happening with
        // the round trip, issue doesn't occur.
        executeScript("const grid = arguments[0];" +
                "grid.setAttribute('component-test-scrolling', '');" +
                "const scrollValues = arguments[1];" +
                "let iterator = 0;" +
                "const updateScroll = function() {" +
                "  if (iterator == 9) {" +
                "    iterator = 0;" +
                "  }" +
                "  grid.$.table.scrollTop = scrollValues[iterator];" +
                "  iterator ++;" +
                "};" +
                "const interval = setInterval(updateScroll, 1);" +
                "setTimeout(e => {" +
                "  clearInterval(interval);" +
                "  grid.removeAttribute('component-test-scrolling');" +
                "}, 5000);", grid, scrollValues);

        waitUntil(e -> !grid.hasAttribute("component-test-scrolling") &&
                !grid.getPropertyBoolean("loading"));
    }

    private void runAddNewItemAfterCollapseAndExpand()
            throws InterruptedException {
        open();

        setupTreeGrid();

        waitUntil(driver -> getTreeGrid().getRowCount() == 5);

        findElement(By.id("collapse")).click();
        findElement(By.id("expand")).click();
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
