
package com.vaadin.flow.component.grid.contextmenu;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/templated-columns")
public class TemplatedColumnsIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("templated-columns"));
    }

    @Test
    public void cellsAreRendered() {
        WebElement parent = findElement(By.tagName("templated-columns"));
        GridElement grid = $(GridElement.class).context(parent).id("grid");

        Assert.assertEquals(6, grid.getAllColumns().size());

        for (int i = 0; i < 10; i++) {
            assertCellContents(grid, i);
        }
    }

    private void assertCellContents(GridElement grid, int rowIndex) {
        Assert.assertEquals(String.valueOf(rowIndex),
                grid.getCell(rowIndex, 0).getText());
        Assert.assertEquals("Person" + (rowIndex + 1),
                grid.getCell(rowIndex, 1).getText());
        Assert.assertEquals(rowIndex + "son",
                grid.getCell(rowIndex, 2).getText());
        Assert.assertEquals("State " + rowIndex,
                grid.getCell(rowIndex, 3).getText());
        Assert.assertEquals("Country " + rowIndex,
                grid.getCell(rowIndex, 4).getText());
        Assert.assertEquals("Street " + rowIndex,
                grid.getCell(rowIndex, 5).getText());
    }

}
