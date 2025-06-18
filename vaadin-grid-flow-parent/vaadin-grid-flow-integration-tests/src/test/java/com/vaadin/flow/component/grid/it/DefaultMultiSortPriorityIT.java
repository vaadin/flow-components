/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/default-multi-sort-priority")
public class DefaultMultiSortPriorityIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void setDefaultMultiSortPriority_addGrid_priorityIsUsed() {
        findElement(By.id("btn-set-add")).click();

        GridElement grid = $(GridElement.class).id("multi-sort-priority-grid");

        // Sort by Name column
        getCellContent(grid.getHeaderCell(0)).click();

        // Sort by Age column
        getCellContent(grid.getHeaderCell(1)).click();

        // "Append" priority
        Assert.assertEquals("Ann", grid.getCell(0, 0).getText());
        Assert.assertEquals("25", grid.getCell(0, 1).getText());

        Assert.assertEquals("Ann", grid.getCell(1, 0).getText());
        Assert.assertEquals("30", grid.getCell(1, 1).getText());

        Assert.assertEquals("Bob", grid.getCell(2, 0).getText());
        Assert.assertEquals("20", grid.getCell(2, 1).getText());
    }

    private WebElement getCellContent(GridTHTDElement cell) {
        return (WebElement) executeScript(
                "return arguments[0].firstElementChild.assignedNodes()[0].firstElementChild;",
                cell);
    }
}
