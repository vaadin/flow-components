package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-grid/treegrid-drag-and-drop")
public class TreeGridDragAndDropIT extends AbstractComponentIT {
    TreeGridElement grid;

    @Before
    public void init() {
        open();
        grid = $(TreeGridElement.class).waitForFirst();
    }

    @Test
    public void moveChildBetweenNodes_refreshItemsAndChildren_itemsAndChildrenProperlyUpdated() {
        // Move item 1-1 from item 1 to item 2
        fireDragStart(2);
        fireDrop(3);
        fireDragEnd();

        // Verify new structure
        Assert.assertEquals("root", getCellContent(0).getText());
        Assert.assertEquals("item 1", getCellContent(1).getText());
        Assert.assertEquals("item 2", getCellContent(2).getText());
        Assert.assertEquals("item 2-1", getCellContent(3).getText());
        Assert.assertEquals("item 1-1", getCellContent(4).getText());

        // Move item 2-1 from item 2 to item 1
        fireDragStart(3);
        fireDrop(1);
        fireDragEnd();

        // Verify new structure
        Assert.assertEquals("root", getCellContent(0).getText());
        Assert.assertEquals("item 1", getCellContent(1).getText());
        Assert.assertEquals("item 2-1", getCellContent(2).getText());
        Assert.assertEquals("item 2", getCellContent(3).getText());
        Assert.assertEquals("item 1-1", getCellContent(4).getText());
    }

    private WebElement getCellContent(int rowIndex) {
        return (WebElement) executeScript("return arguments[0]._content",
                grid.getCell(rowIndex, 0));
    }

    private void fireDragStart(int rowIndex) {
        executeScript("fireDragStart(arguments[0])", getCellContent(rowIndex));
    }

    private void fireDragEnd() {
        executeScript("fireDragEnd(arguments[0])", grid);
    }

    private void fireDrop(int rowIndex) {
        executeScript("fireDrop(arguments[0], arguments[1])",
                getCellContent(rowIndex), "on-top");
    }
}
