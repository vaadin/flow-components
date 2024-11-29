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

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/treegrid-expand-collapse-recursively")
public class TreeGridExpandCollapseRecursivelyIT extends AbstractComponentIT {

    private static final int LEVEL_SIZE = 2;

    private TreeGridElement treeGrid;
    private WebElement depthSelector;
    private WebElement expandButton;
    private WebElement collapseButton;

    @Before
    public void before() {
        open();
        treeGrid = $(TreeGridElement.class).first();

        depthSelector = findElement(By.tagName("vaadin-radio-group"));

        List<WebElement> buttons = findElements(By.tagName("button"));
        expandButton = buttons.get(0);
        collapseButton = buttons.get(1);
    }

    @Test
    public void expandVariousDepth() {
        selectRecursionDepth(0);
        expandButton.click();
        assertNumberOfExpandedLevels(1);
        assertRowContents("Item-0", "Item-0-0", "Item-0-1");

        selectRecursionDepth(1);
        expandButton.click();
        assertNumberOfExpandedLevels(2);
        assertRowContents("Item-0", "Item-0-0", "Item-0-0-0", "Item-0-0-1");

        selectRecursionDepth(2);
        expandButton.click();
        assertNumberOfExpandedLevels(3);
        assertRowContents("Item-0", "Item-0-0", "Item-0-0-0", "Item-0-0-0-0",
                "Item-0-0-0-1");
    }

    @Test
    public void expandAndCollapseAllItems() {
        selectRecursionDepth(2);

        expandButton.click();
        assertNumberOfExpandedLevels(3);

        collapseButton.click();
        assertNumberOfExpandedLevels(0);
    }

    @Test
    public void partialCollapse() {
        selectRecursionDepth(2);
        expandButton.click();
        assertNumberOfExpandedLevels(3);

        selectRecursionDepth(1);
        collapseButton.click();
        assertNumberOfExpandedLevels(0);

        selectRecursionDepth(0);
        expandButton.click();
        assertNumberOfExpandedLevels(1);

        // Open just one subtree to see if it is still fully expanded
        treeGrid.expandWithClick(2, 0);
        assertRowContents("Item-0", "Item-0-0", "Item-0-1", "Item-0-1-0",
                "Item-0-1-0-0");
    }

    private void selectRecursionDepth(int depth) {
        WebElement radiobutton = depthSelector
                .findElements(By.tagName("vaadin-radio-button")).get(depth);
        executeScript("arguments[0].checked=true", radiobutton);
    }

    private void assertNumberOfExpandedLevels(int expectedNumberOfLevels) {
        // Calculate the total number of rows in a tree with the given number of
        // expanded levels using a geometric-like series: 2 + 6 + 14 + 30 ...
        // when LEVEL_SIZE is 2.
        int expectedRowCount = IntStream.rangeClosed(0, expectedNumberOfLevels)
                .reduce(0, (sum, n) -> sum + (int) Math.pow(LEVEL_SIZE, n + 1));
        Assert.assertEquals(expectedRowCount, treeGrid.getRowCount());
    }

    private void assertRowContents(String... expected) {
        List<String> actual = IntStream.range(0, expected.length)
                .mapToObj(i -> treeGrid.getRow(i).getText()).toList();
        Assert.assertEquals(Arrays.asList(expected), actual);
    }
}
