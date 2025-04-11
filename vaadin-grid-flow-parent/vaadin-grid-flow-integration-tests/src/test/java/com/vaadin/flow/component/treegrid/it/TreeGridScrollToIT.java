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

import com.vaadin.flow.component.grid.testbench.GridColumnElement;
import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the TreeGridScrollToPage view.
 */
@TestPath("vaadin-grid/treegrid-scroll-to")
public class TreeGridScrollToIT extends AbstractComponentIT {

    private TreeGridElement grid;

    private TestBenchElement expandAllButton;

    private TestBenchElement scrollToStartButton;

    private TestBenchElement scrollToEndButton;

    private TestBenchElement scrollToIndexInput;

    @Before
    public void init() {
        open();
        waitUntil(e -> $(TreeGridElement.class).exists(), 2);
        grid = $(TreeGridElement.class).first();
        expandAllButton = $("button").id("expand-all");
        scrollToStartButton = $("button").id("scroll-to-start");
        scrollToEndButton = $("button").id("scroll-to-end");
        scrollToIndexInput = $("input").id("scroll-to-index");

    }

    @Test
    public void expandAll_scrollToEnd_correctLastVisibleItem() {
        expandAllButton.click();

        scrollToEndButton.click();

        waitUntil(e -> {
            int lastVisibleRow = grid.getLastVisibleRowIndex();
            return grid.hasRow(lastVisibleRow)
                    && "Son 49/19/19".equals(getCellContent(lastVisibleRow));
        }, 2);
    }

    @Test
    public void expandAll_scrollToEnd_viaElement() {
        expandAllButton.click();

        grid.scrollToEnd();

        waitUntil(e -> {
            int lastVisibleRow = grid.getLastVisibleRowIndex();
            return grid.hasRow(lastVisibleRow)
                    && "Son 49/19/19".equals(getCellContent(lastVisibleRow));
        }, 2);
    }

    @Test
    public void scrollToEnd_correctLastVisibleItem() {
        scrollToEndButton.click();

        Assert.assertEquals("Granddad 49",
                getCellContent(grid.getLastVisibleRowIndex()));
    }

    @Test
    public void scrollToEnd_scrollToStart_correctFirstVisibleItem() {
        scrollToEndButton.click();
        waitUntil(
                e -> "Granddad 49"
                        .equals(getCellContent(grid.getLastVisibleRowIndex())),
                1);

        scrollToStartButton.click();

        Assert.assertEquals("Granddad 0",
                getCellContent(grid.getFirstVisibleRowIndex()));
    }

    @Test
    public void expandAll_scrollToIndex30_correctFirstVisibleItem() {
        expandAllButton.click();

        scrollToIndexInput.sendKeys("30", Keys.TAB);

        waitUntil(e -> {
            int firstVisibleRow = grid.getFirstVisibleRowIndex();
            return grid.hasRow(firstVisibleRow)
                    && "Granddad 30".equals(getCellContent(firstVisibleRow));
        }, 2);
    }

    @Test
    public void scrollToIndex30_correctFirstVisibleItem() {
        scrollToIndexInput.sendKeys("30", Keys.TAB);

        Assert.assertEquals("Granddad 30",
                getCellContent(grid.getFirstVisibleRowIndex()));
    }

    @Test
    public void expandAll_scrollToIndex30_1_correctFirstVisibleItem() {
        expandAllButton.click();

        scrollToIndexInput.sendKeys("30-1", Keys.TAB);

        waitUntil(e -> {
            int firstVisibleRow = grid.getFirstVisibleRowIndex();
            return grid.hasRow(firstVisibleRow)
                    && "Dad 30/1".equals(getCellContent(firstVisibleRow));
        }, 2);
    }

    @Test
    public void scrollToIndex30_1_correctFirstVisibleItem() {
        scrollToIndexInput.sendKeys("30-1", Keys.TAB);

        Assert.assertEquals("Granddad 30",
                getCellContent(grid.getFirstVisibleRowIndex()));
    }

    @Test
    public void expandAll_scrollToIndex30_1_1_correctFirstVisibleItem() {
        expandAllButton.click();

        scrollToIndexInput.sendKeys("30-1-1", Keys.TAB);

        waitUntil(e -> {
            int firstVisibleRow = grid.getFirstVisibleRowIndex();
            return grid.hasRow(firstVisibleRow)
                    && "Son 30/1/1".equals(getCellContent(firstVisibleRow));
        }, 2);
    }

    @Test
    public void expandAll_scrollToIndex10_1_1_correctFirstVisibleItem() {
        expandAllButton.click();

        // Manual test triggers an infinite loop.
        // The indexes to reproduce this loop depends on the grid height.
        scrollToIndexInput.sendKeys("10-1-1", Keys.TAB);

        waitUntil(e -> {
            int firstVisibleRow = grid.getFirstVisibleRowIndex();
            return grid.hasRow(firstVisibleRow)
                    && "Son 10/1/1".equals(getCellContent(firstVisibleRow));
        }, 2);
    }

    private String getCellContent(int rowIndex) {
        GridColumnElement gridColumnElement = grid.getVisibleColumns().get(0);
        return grid.getRow(rowIndex).getCell(gridColumnElement).getText();
    }
}
