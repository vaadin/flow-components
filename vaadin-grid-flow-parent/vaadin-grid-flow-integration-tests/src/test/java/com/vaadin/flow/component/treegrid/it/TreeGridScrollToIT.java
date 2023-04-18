/*
 * Copyright 2000-2023 Vaadin Ltd.
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
import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests for the TreeGridScrollToPage view.
 */
@TestPath("vaadin-grid/treegrid-scroll-to")
public class TreeGridScrollToIT extends AbstractComponentIT {

    private TreeGridElement grid;

    private TestBenchElement expandAllButton;

    private TestBenchElement scrollToStartButton;

    private TestBenchElement scrollToEndButton;

    private TestBenchElement scrollToIndex30Button;

    private TestBenchElement scrollToIndex30_1Button;

    private TestBenchElement scrollToIndex30_1_1Button;

    @Before
    public void init() {
        open();
        waitUntil(e -> $(TreeGridElement.class).exists(), 500);
        grid = $(TreeGridElement.class).first();
        expandAllButton = $("button").id("expand-all");
        scrollToStartButton = $("button").id("scroll-to-start");
        scrollToEndButton = $("button").id("scroll-to-end");
        scrollToIndex30Button = $("button").id("scroll-to-index-30");
        scrollToIndex30_1Button = $("button").id("scroll-to-index-30-1");
        scrollToIndex30_1_1Button = $("button").id("scroll-to-index-30-1-1");
    }

    @Test
    public void expandAll_scrollToEnd_correctLastVisibleItem()
            throws InterruptedException {
        expandAllButton.click();

        scrollToEndButton.click();
        Thread.sleep(2000);

        Assert.assertEquals("Son 49/2/2",
                getCellContent(grid.getLastVisibleRowIndex()));
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

        scrollToStartButton.click();

        Assert.assertEquals("Granddad 0",
                getCellContent(grid.getFirstVisibleRowIndex()));
    }

    @Test
    public void expandAll_scrollToIndex30_correctFirstVisibleItem()
            throws InterruptedException {
        expandAllButton.click();

        scrollToIndex30Button.click();
        Thread.sleep(2000);

        Assert.assertEquals("Granddad 30",
                getCellContent(grid.getFirstVisibleRowIndex()));
    }

    @Test
    public void scrollToIndex30_correctFirstVisibleItem() {
        scrollToIndex30Button.click();

        Assert.assertEquals("Granddad 30",
                getCellContent(grid.getFirstVisibleRowIndex()));
    }

    @Test
    public void expandAll_scrollToIndex30_1_correctFirstVisibleItem()
            throws InterruptedException {
        expandAllButton.click();

        scrollToIndex30_1Button.click();
        Thread.sleep(2000);

        Assert.assertEquals("Dad 30/1",
                getCellContent(grid.getFirstVisibleRowIndex()));
    }

    @Test
    public void scrollToIndex30_1_correctFirstVisibleItem() {
        scrollToIndex30_1Button.click();

        Assert.assertEquals("Granddad 30",
                getCellContent(grid.getFirstVisibleRowIndex()));
    }

    @Test
    public void expandAll_scrollToIndex30_1_1_correctFirstVisibleItem()
            throws InterruptedException {
        expandAllButton.click();

        scrollToIndex30_1_1Button.click();
        Thread.sleep(2000);

        Assert.assertEquals("Son 30/1/1",
                getCellContent(grid.getFirstVisibleRowIndex()));
    }

    private String getCellContent(int rowIndex) {
        GridColumnElement gridColumnElement = grid.getVisibleColumns().get(0);
        return grid.getRow(rowIndex).getCell(gridColumnElement).getText();
    }
}
