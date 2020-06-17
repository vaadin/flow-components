package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("header-page")
public class HeaderTextIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
        waitForElementPresent(By.id(HeaderTextPage.SINGLE_HEADER_GRID_ID));
        waitForElementPresent(By.id(HeaderTextPage.MULTI_HEADER_ROWS_GRID_ID));
    }

    @Test
    public void singleRowGrid_changeSimpleHeaderText_shouldBeChanged() {
        GridElement grid = $(GridElement.class)
                .id(HeaderTextPage.SINGLE_HEADER_GRID_ID);
        clickButton(HeaderTextPage.CHANGE_ADDRESS_HEADER_ID);
        Assert.assertEquals(
                "After calling setHeader for a simple column, the header text is not changed to the new value.",
                "Addr.", grid.getHeaderCell(0).getText());
    }

    @Test
    public void singleRowGrid_changeSortableColumnHeaderText_shouldBeChanged() {
        GridElement grid = $(GridElement.class)
                .id(HeaderTextPage.SINGLE_HEADER_GRID_ID);
        clickButton(HeaderTextPage.CHANGE_AGE_HEADER_ID);
        Assert.assertEquals(
                "After calling setHeader for a sortable column, the header text is not changed to the new value.",
                "Birth Year", grid.getHeaderCell(1).getText());
        Assert.assertFalse(
                "After calling setHeader for a sortable column, vaadin-grid-sorter is removed from the column header.",
                grid.getHeaderCellContent(0, 1)
                        .findElements(By.tagName("vaadin-grid-sorter"))
                        .isEmpty());
    }

    @Test
    public void multiHeaderRowsGrid_changeHeaderInJoinedCells_shouldBeChanged() {
        GridElement grid = $(GridElement.class)
                .id(HeaderTextPage.MULTI_HEADER_ROWS_GRID_ID);
        clickButton(HeaderTextPage.CHANGE_HEADER_IN_JOINED_CELLS_ID);
        Assert.assertEquals(
                "After calling setHeader for a joined cell, the header text is not changed to the new value.",
                "New Header", grid.getHeaderCellContent(0, 1).getText());
    }

    @Test
    public void multiHeaderRowsGrid_changeHeaderInMiddle_shouldBeChanged() {
        GridElement grid = $(GridElement.class)
                .id(HeaderTextPage.MULTI_HEADER_ROWS_GRID_ID);
        clickButton(HeaderTextPage.CHANGE_HEADER_IN_MIDDLE_ID);
        Assert.assertEquals(
                "After calling setHeader for a sortable column in a grid with multi header rows, the header text is not changed to the new value.",
                "Afterlife", grid.getHeaderCellContent(1, 1).getText());
        Assert.assertFalse(
                "After calling setHeader for a sortable column, vaadin-grid-sorter is removed from the column header.",
                grid.getHeaderCellContent(1, 1)
                        .findElements(By.tagName("vaadin-grid-sorter"))
                        .isEmpty());
    }

    @Test
    public void multiHeaderRowsGrid_changeHeaderInLastRow_shouldBeChanged() {
        GridElement grid = $(GridElement.class)
                .id(HeaderTextPage.MULTI_HEADER_ROWS_GRID_ID);
        clickButton(HeaderTextPage.CHANGE_HEADER_IN_LAST_ROW_ID);
        Assert.assertEquals(
                "After calling setHeader for a simple column in the third header row of a grid, the header text is not changed to the new value.",
                "Something", grid.getHeaderCellContent(2, 3).getText());
    }

    private void clickButton(String id) {
        findElement(By.id(id)).click();
    }
}
