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
import org.openqa.selenium.TimeoutException;

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
        assertLastVisibleRowContent("Son 49/19/19");
    }

    @Test
    public void expandAll_scrollToEndFromClient_correctLastVisibleItem() {
        expandAllButton.click();
        grid.scrollToEnd();
        assertLastVisibleRowContent("Son 49/19/19");
    }

    @Test
    public void scrollToEnd_correctLastVisibleItem() {
        scrollToEndButton.click();
        assertLastVisibleRowContent("Granddad 49");
    }

    @Test
    public void scrollToEnd_scrollToStart_correctFirstVisibleItem() {
        scrollToEndButton.click();
        assertLastVisibleRowContent("Granddad 49");

        scrollToStartButton.click();
        assertFirstVisibleRowContent("Granddad 0");
    }

    @Test
    public void expandAll_scrollToIndex30_correctFirstVisibleItem() {
        expandAllButton.click();
        scrollToIndexInput.sendKeys("30", Keys.TAB);
        assertFirstVisibleRowContent("Granddad 30");
    }

    @Test
    public void scrollToIndex30_correctFirstVisibleItem() {
        scrollToIndexInput.sendKeys("30", Keys.TAB);
        assertFirstVisibleRowContent("Granddad 30");
    }

    @Test
    public void expandAll_scrollToIndex30_1_correctFirstVisibleItem() {
        expandAllButton.click();
        scrollToIndexInput.sendKeys("30-1", Keys.TAB);
        assertFirstVisibleRowContent("Dad 30/1");
    }

    @Test
    public void scrollToIndex30_1_correctFirstVisibleItem() {
        scrollToIndexInput.sendKeys("30-1", Keys.TAB);
        assertFirstVisibleRowContent("Granddad 30");
    }

    @Test
    public void expandAll_scrollToIndex30_1_1_correctFirstVisibleItem() {
        expandAllButton.click();
        scrollToIndexInput.sendKeys("30-1-1", Keys.TAB);
        assertFirstVisibleRowContent("Son 30/1/1");
    }

    @Test
    public void expandAll_scrollToIndex10_1_1_correctFirstVisibleItem() {
        expandAllButton.click();

        // Manual test triggers an infinite loop.
        // The indexes to reproduce this loop depends on the grid height.
        scrollToIndexInput.sendKeys("10-1-1", Keys.TAB);
        assertFirstVisibleRowContent("Son 10/1/1");
    }

    private void assertFirstVisibleRowContent(String content) {
        try {
            waitUntil(driver -> {
                int rowIndex = grid.getFirstVisibleRowIndex();
                return grid.hasRow(rowIndex)
                        && grid.getRow(rowIndex).getText().equals(content);
            }, 5);
        } catch (TimeoutException e) {
            Assert.fail("There was no first row with content '%s'".formatted(content));
        }
    }

    private void assertLastVisibleRowContent(String content) {
        try {
            waitUntil(driver -> {
                int rowIndex = grid.getLastVisibleRowIndex();
                return grid.hasRow(rowIndex)
                        && grid.getRow(rowIndex).getText().equals(content);
            }, 5);
        } catch (TimeoutException e) {
            Assert.fail("There was no last row with content '%s'".formatted(content));
        }
    }
}
