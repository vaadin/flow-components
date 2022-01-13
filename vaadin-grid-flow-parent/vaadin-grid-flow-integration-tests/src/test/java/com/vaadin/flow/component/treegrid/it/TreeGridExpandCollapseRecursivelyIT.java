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
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/treegrid-expand-collapse-recursively")
public class TreeGridExpandCollapseRecursivelyIT extends AbstractTreeGridIT {

    private static final int itemsPerLevel = 5;
    private static final int rowCount0 = itemsPerLevel;
    private static final int rowCount1 = rowCount0 + rowCount0 * itemsPerLevel;
    private static final int rowCount2 = rowCount1
            + (rowCount1 - rowCount0) * itemsPerLevel;
    private static final int rowCount3 = rowCount2
            + (rowCount2 - rowCount1) * itemsPerLevel;

    private WebElement depthSelector;
    private WebElement expandButton;
    private WebElement collapseButton;

    @Before
    public void before() {
        open();
        setupTreeGrid();

        depthSelector = findElement(By.tagName("vaadin-radio-group"));

        List<WebElement> buttons = findElements(By.tagName("button"));
        expandButton = buttons.get(0);
        collapseButton = buttons.get(1);
    }

    @Test
    public void expandVariousDepth() {
        Assert.assertEquals(rowCount0, getTreeGrid().getRowCount());

        selectDepth(0);
        expandButton.click();
        getTreeGrid().scrollToRow(rowCount1);

        waitUntil(input -> getTreeGrid().getRowCount() == rowCount1, 20);
        Assert.assertEquals(itemsPerLevel,
                getTreeGrid().getNumberOfExpandedRows());

        assertCellTexts(0, 0,
                new String[] { "Item-0", "Item-0-0", "Item-0-1" });

        selectDepth(1);
        expandButton.click();

        waitUntil(input -> getTreeGrid().getNumberOfExpandedRows() == rowCount1,
                20);

        assertCellTexts(0, 0, new String[] { "Item-0", "Item-0-0", "Item-0-0-0",
                "Item-0-0-1" });

        selectDepth(2);
        expandButton.click();

        waitUntil(input -> getTreeGrid().getNumberOfExpandedRows() == rowCount2,
                20);

        assertCellTexts(0, 0, new String[] { "Item-0", "Item-0-0", "Item-0-0-0",
                "Item-0-0-0-0", "Item-0-0-0-1" });

        selectDepth(3);
        expandButton.click();

        waitUntil(input -> getTreeGrid().getNumberOfExpandedRows() == rowCount3,
                20);

        assertCellTexts(0, 0, new String[] { "Item-0", "Item-0-0", "Item-0-0-0",
                "Item-0-0-0-0", "Item-0-0-0-0-0", "Item-0-0-0-0-1" });
    }

    @Test(timeout = 5000)
    public void expandAndCollapseAllItems() {
        Assert.assertEquals(rowCount0, getTreeGrid().getRowCount());

        selectDepth(3);
        expandButton.click();

        waitUntil(
                input -> getTreeGrid().getNumberOfExpandedRows() == rowCount3);

        collapseButton.click();

        waitUntil(input -> getTreeGrid().getNumberOfExpandedRows() == 0);
        Assert.assertEquals(rowCount0, getTreeGrid().getRowCount());
    }

    @Test
    public void partialCollapse() {
        Assert.assertEquals(rowCount0, getTreeGrid().getRowCount());

        selectDepth(3);
        expandButton.click();

        final AtomicInteger expandedRows = new AtomicInteger(rowCount3);
        waitUntil(input -> getTreeGrid()
                .getNumberOfExpandedRows() == expandedRows.get(), 20);

        selectDepth(1);
        collapseButton.click();

        expandedRows.addAndGet(-rowCount1);
        waitUntil(input -> getTreeGrid()
                .getNumberOfExpandedRows() == expandedRows.get(), 20);
        Assert.assertEquals(rowCount0, getTreeGrid().getRowCount());

        selectDepth(0);
        expandButton.click();

        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                return getTreeGrid().getRowCount() == rowCount1;
            }

            @Override
            public String toString() {
                return "Tree Grid has " + getTreeGrid().getRowCount()
                        + " rows intead of exected " + rowCount1;
            }
        }, 20);
        Assert.assertEquals(expandedRows.addAndGet(rowCount0),
                getTreeGrid().getNumberOfExpandedRows());

        // Open just one subtree to see if it is still fully expanded
        getTreeGrid().getExpandToggleElement(2, 0).click();

        expandedRows.addAndGet(1);
        waitUntil(input -> getTreeGrid()
                .getNumberOfExpandedRows() == expandedRows.get(), 20);
        waitUntil(input -> !getTreeGrid().isLoadingExpandedRows(), 20);

        assertCellTexts(0, 0, new String[] { "Item-0", "Item-0-0", "Item-0-1",
                "Item-0-1-0", "Item-0-1-0-0", "Item-0-1-0-0-0" });

    }

    private void selectDepth(int depth) {
        WebElement radiobutton = depthSelector
                .findElements(By.tagName("vaadin-radio-button")).get(depth);
        executeScript("arguments[0].checked=true", radiobutton);
    }

}
