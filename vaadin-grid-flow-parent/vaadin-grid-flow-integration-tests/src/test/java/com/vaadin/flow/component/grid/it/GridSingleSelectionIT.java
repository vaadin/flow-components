package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-grid/grid-single-selection")
public class GridSingleSelectionIT extends AbstractComponentIT {

    private GridElement grid;
    private TestBenchElement toggleFirstItem;
    private TestBenchElement toggleLastItem;
    private TestBenchElement deselectAll;
    private TestBenchElement selectionLog;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).waitForFirst();
        toggleFirstItem = $(TestBenchElement.class).id("toggle-first-item");
        toggleLastItem = $(TestBenchElement.class).id("toggle-last-item");
        deselectAll = $(TestBenchElement.class).id("deselect-all");
        selectionLog = $(TestBenchElement.class).id("selection-log");
    }

    @Test
    public void selectFirstItem_deselectFirstItem_nothingSelected() {
        toggleFirstItem.click();
        Assert.assertEquals("oldValue=null; newValue=0; fromClient=false",
                selectionLog.getText());
        Assert.assertTrue(grid.getRow(0).isSelected());

        toggleFirstItem.click();
        Assert.assertEquals("oldValue=0; newValue=null; fromClient=false",
                selectionLog.getText());
        Assert.assertFalse(grid.getRow(0).isSelected());
    }

    @Test
    public void selectFirstItemFromClient_deselectFirstItemFromClient_nothingSelected() {
        grid.select(0);
        Assert.assertEquals("oldValue=null; newValue=0; fromClient=true",
                selectionLog.getText());
        Assert.assertTrue(grid.getRow(0).isSelected());

        grid.deselect(0);
        Assert.assertEquals("oldValue=0; newValue=null; fromClient=true",
                selectionLog.getText());
        Assert.assertFalse(grid.getRow(0).isSelected());
    }

    @Test
    public void selectFirstItem_selectSecondItemFromClient_secondItemSelected() {
        toggleFirstItem.click();
        Assert.assertEquals("oldValue=null; newValue=0; fromClient=false",
                selectionLog.getText());
        Assert.assertTrue(grid.getRow(0).isSelected());

        grid.select(1);
        Assert.assertEquals("oldValue=0; newValue=1; fromClient=true",
                selectionLog.getText());
        Assert.assertFalse(grid.getRow(0).isSelected());
        Assert.assertTrue(grid.getRow(1).isSelected());
    }

    @Test
    public void selectSecondItemFromClient_selectFirstItem_firstItemSelected() {
        grid.select(1);
        Assert.assertEquals("oldValue=null; newValue=1; fromClient=true",
                selectionLog.getText());
        Assert.assertTrue(grid.getRow(1).isSelected());

        toggleFirstItem.click();
        Assert.assertEquals("oldValue=1; newValue=0; fromClient=false",
                selectionLog.getText());
        Assert.assertFalse(grid.getRow(1).isSelected());
        Assert.assertTrue(grid.getRow(0).isSelected());
    }

    @Test
    public void selectUncachedItem_itemSelected() {
        toggleLastItem.click();
        Assert.assertEquals("oldValue=null; newValue=499; fromClient=false",
                selectionLog.getText());

        grid.scrollToRow(500);
        Assert.assertTrue(grid.getRow(499).isSelected());
    }

    /**
     * Test that aria-multiselectable=false & the selectable children should
     * have aria-selected=true|false depending on their state
     */
    @Test
    public void ariaSelectionAttributes() {
        TestBenchElement table = grid.$("table").first();
        Assert.assertTrue(table.hasAttribute("aria-multiselectable"));
        Assert.assertFalse(Boolean
                .parseBoolean(table.getAttribute("aria-multiselectable")));

        GridTRElement firstRow = grid.getRow(0);
        firstRow.select();
        Assert.assertTrue(firstRow.hasAttribute("aria-selected"));
        Assert.assertTrue(
                Boolean.parseBoolean(firstRow.getAttribute("aria-selected")));

        GridTRElement secondRow = grid.getRow(1);
        Assert.assertFalse(
                Boolean.parseBoolean(secondRow.getAttribute("aria-selected")));
    }

    // Regression test for: https://github.com/vaadin/flow-components/issues/324
    @Test
    public void deselectAll_clientSideGridHasEmptySelection() {
        toggleFirstItem.click();
        deselectAll.click();

        Assert.assertEquals(0, getNumberOfSelectedItemsClientSide(grid));
    }

    private long getNumberOfSelectedItemsClientSide(GridElement grid) {
        return (Long) executeScript("return arguments[0].selectedItems.length",
                grid);
    }
}
