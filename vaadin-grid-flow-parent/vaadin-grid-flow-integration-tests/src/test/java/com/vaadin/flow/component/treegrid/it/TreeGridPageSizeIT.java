/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
    @org.junit.Ignore("Unstable test when migrated to mono-repo")
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
