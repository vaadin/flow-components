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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/treegrid-page-size")
public class TreeGridPageSizeIT extends AbstractTreeGridIT {

    @Before
    public void before() {
        open();
        setupTreeGrid();
    }

    @Test

    public void treegridWithPageSize10_changeTo80_revertBackTo10() {
        TreeGridElement grid = getTreeGrid();

        waitUntil(test -> grid.getNumberOfExpandedRows() == 12, 1);
        // assert here only minimum required fetches
        assertLogContainsFetch(0, 3, "root");
        assertLogContainsFetch(0, 3, "Granddad 0");
        assertLogContainsFetch(0, 10, "Dad 0/0");
        assertLogContainsFetch(10, 30, "Dad 0/0/");

        assertPageSize(grid, 10);
        assertCellContents(grid);

        assertLogContainsFetch(150, 10, "Dad 0/0");
        assertLogContainsFetch(290, 10, "Dad 0/0");
        assertLogContainsFetch(0, 10, "Dad 0/1");

        clearLog();

        WebElement input = findElement(By.id("size-input"));
        WebElement button = findElement(By.id("size-submit"));

        input.sendKeys("80");
        blur();
        button.click();

        waitUntil(test -> grid.getNumberOfExpandedRows() == 12, 1);
        // assert here only minimum required fetches
        assertLogContainsFetch(0, 3, "root");
        assertLogContainsFetch(0, 3, "Granddad 0");
        assertLogContainsFetch(0, 80, "Dad 0/0");

        assertPageSize(grid, 80);
        assertCellContents(grid);

        assertLogContainsFetch(80, 80, "Dad 0/0");
        assertLogContainsFetch(160, 80, "Dad 0/0");
        assertLogContainsFetch(240, 60, "Dad 0/0");
        assertLogContainsFetch(0, 80, "Dad 0/1");
        clearLog();

        input.clear();
        input.sendKeys("10");
        blur();
        button.click();

        waitUntil(test -> grid.getNumberOfExpandedRows() == 12, 1);
        // assert here only minimum required fetches
        assertLogContainsFetch(0, 3, "root");
        assertLogContainsFetch(0, 3, "Granddad 0");
        assertLogContainsFetch(0, 10, "Dad 0/0");
        assertLogContainsFetch(10, 30, "Dad 0/0/");

        assertPageSize(grid, 10);
        assertCellContents(grid);

        assertLogContainsFetch(150, 10, "Dad 0/0");
        assertLogContainsFetch(290, 10, "Dad 0/0");
        assertLogContainsFetch(0, 10, "Dad 0/1");
    }

    private void assertLogContainsFetch(int offset, int limit, String parent) {
        logContainsText(String.format(
                "Query offset: %d Query limit: %d Query parent: %s", offset,
                limit, parent));
    }

    private void assertCellContents(WebElement grid) {
        assertCellTexts(0, 0, "Granddad 0");
        assertCellTexts(48, 0, "Son 0/0/46");
        assertCellTexts(208, 0, "Son 0/0/206");
        assertCellTexts(301, 0, "Son 0/0/299");
        assertCellTexts(302, 0, "Dad 0/1");
        assertCellTexts(303, 0, "Son 0/1/0");
        assertCellTexts(602, 0, "Son 0/1/299");
        assertCellTexts(603, 0, "Dad 0/2");
        assertCellTexts(604, 0, "Son 0/2/0");
    }

    private void assertPageSize(WebElement grid, int pageSize) {
        Object pageSizeFromGrid = executeScript("return arguments[0].pageSize",
                grid);
        Assert.assertEquals(
                "The pageSize of the webcomponent should be " + pageSize,
                pageSize, Integer.parseInt(String.valueOf(pageSizeFromGrid)));
    }
}
