package com.vaadin.flow.component.spreadsheet.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

public class GroupingTest {

    private Spreadsheet spreadsheet;

    @Before
    public void init() {
        spreadsheet = TestHelper.createSpreadsheet("Groupingtest.xlsx");
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "[1, 1, 1, 1]");
    }

    @Test
    public void expandRow_alreadyExpanded_shouldNotThrow() {
        expandRow(1);
    }

    @Test
    public void collapseRow_rowHidden() {
        collapseRow(1);
        Assert.assertTrue(spreadsheet.isRowHidden(3));
    }

    @Test
    public void collapseRow_expandRow_rowVisible() {
        collapseRow(1);
        expandRow(1);
        Assert.assertFalse(spreadsheet.isRowHidden(3));
    }

    @Test
    public void collapseRow_collapseParentRow_expandRow_rowHidden() {
        spreadsheet.setActiveSheetIndex(3);
        // Collapse child row
        collapseRow(1);
        Assert.assertTrue(spreadsheet.isRowHidden(2));

        // Collapse parent row
        collapseRow(0);

        // Expand child row
        expandRow(1);
        Assert.assertTrue(spreadsheet.isRowHidden(2));
    }

    @Test
    public void collapseRow_collapseParentRow_expandParentRow_rowHidden() {
        spreadsheet.setActiveSheetIndex(3);
        // Collapse child row
        collapseRow(1);
        Assert.assertTrue(spreadsheet.isRowHidden(2));

        // Collapse parent row
        collapseRow(0);

        // Expand parent row
        expandRow(0);
        Assert.assertTrue(spreadsheet.isRowHidden(2));
    }

    @Test
    public void collapseParentRow_expandParentRow_rowVisible() {
        spreadsheet.setActiveSheetIndex(3);
        // Collapse parent row
        collapseRow(0);
        Assert.assertTrue(spreadsheet.isRowHidden(2));

        // Expand Parent row
        expandRow(0);
        Assert.assertFalse(spreadsheet.isRowHidden(2));
    }

    @Test
    public void inverted_collapseRow_rowHidden() {
        spreadsheet.setActiveSheetIndex(6);
        collapseRow(3);
        Assert.assertTrue(spreadsheet.isRowHidden(3));
    }

    @Test
    public void inverted_collapseRow_expandRow_rowVisible() {
        spreadsheet.setActiveSheetIndex(6);
        collapseRow(3);
        expandRow(3);
        Assert.assertFalse(spreadsheet.isRowHidden(3));
    }

    @Test
    public void inverted_collapseRow_collapseParentRow_expandRow_rowHidden() {
        spreadsheet.setActiveSheetIndex(6);
        // Collapse child row
        collapseRow(3);
        Assert.assertTrue(spreadsheet.isRowHidden(3));

        // Collapse parent row
        collapseRow(2);

        // Expand child row
        expandRow(3);
        Assert.assertTrue(spreadsheet.isRowHidden(3));
    }

    @Test
    public void inverted_collapseRow_collapseParentRow_expandParentRow_rowHidden() {
        spreadsheet.setActiveSheetIndex(6);
        // Collapse child row
        collapseRow(3);
        Assert.assertTrue(spreadsheet.isRowHidden(3));

        // Collapse parent row
        collapseRow(2);

        // Expand parent row
        expandRow(2);
        Assert.assertTrue(spreadsheet.isRowHidden(3));
    }

    @Test
    public void inverted_collapseParentRow_expandParentRow_rowVisible() {
        spreadsheet.setActiveSheetIndex(6);
        // Collapse parent row
        collapseRow(2);
        Assert.assertTrue(spreadsheet.isRowHidden(3));

        // Expand parent row
        expandRow(2);
        Assert.assertFalse(spreadsheet.isRowHidden(3));
    }

    @Test
    public void expandColumn_alreadyExpanded_shouldNotThrow() {
        expandColumn(1);
    }

    @Test
    public void collapseColumn_columnHidden() {
        collapseColumn(1);
        Assert.assertTrue(spreadsheet.isColumnHidden(3));
    }

    @Test
    public void collapseColumn_expandColumn_columnVisible() {
        collapseColumn(1);
        expandColumn(1);
        Assert.assertFalse(spreadsheet.isColumnHidden(3));
    }

    @Test
    public void collapseColumn_collapseParentColumn_expandColumn_columnHidden() {
        spreadsheet.setActiveSheetIndex(5);
        // Collapse child column
        collapseColumn(0);
        Assert.assertTrue(spreadsheet.isColumnHidden(1));

        // Collapse parent column
        collapseColumn(2);

        // Expand child column
        expandColumn(0);
        Assert.assertTrue(spreadsheet.isColumnHidden(1));
    }

    @Test
    public void collapseColumn_collapseParentColumn_expandParentColumn_columnHidden() {
        spreadsheet.setActiveSheetIndex(5);
        // Collapse child column
        collapseColumn(0);
        Assert.assertTrue(spreadsheet.isColumnHidden(1));

        // Collapse parent column
        collapseColumn(2);

        // Expand parent column
        expandColumn(2);
        Assert.assertTrue(spreadsheet.isColumnHidden(1));
    }

    @Ignore("bug in GroupingUtil, collapsing and expanding parent column should not result with collapsed child columns")
    @Test
    public void collapseParentColumn_expandParentColumn_columnVisible() {
        spreadsheet.setActiveSheetIndex(5);
        // Collapse parent column
        collapseColumn(2);
        Assert.assertTrue(spreadsheet.isColumnHidden(1));

        // Expand Parent column
        expandColumn(2);
        Assert.assertFalse(spreadsheet.isColumnHidden(1));
    }

    @Test
    public void inverted_collapseColumn_columnHidden() {
        spreadsheet.setActiveSheetIndex(6);
        collapseColumn(2);
        Assert.assertTrue(spreadsheet.isColumnHidden(3));
    }

    @Test
    public void inverted_collapseColumn_expandColumn_columnVisible() {
        spreadsheet.setActiveSheetIndex(6);
        collapseColumn(2);
        expandColumn(2);
        Assert.assertFalse(spreadsheet.isColumnHidden(3));
    }

    @Test
    public void inverted_collapseColumn_collapseParentColumn_expandColumn_columnHidden() {
        spreadsheet.setActiveSheetIndex(6);
        // Collapse child column
        collapseColumn(2);
        Assert.assertTrue(spreadsheet.isColumnHidden(3));

        // Collapse parent column
        collapseColumn(1);

        // Expand child column
        expandColumn(2);
        Assert.assertTrue(spreadsheet.isColumnHidden(3));
    }

    @Test
    public void inverted_collapseColumn_collapseParentColumn_expandParentColumn_columnHidden() {
        spreadsheet.setActiveSheetIndex(6);
        // Collapse child column
        collapseColumn(2);
        Assert.assertTrue(spreadsheet.isColumnHidden(3));

        // Collapse parent column
        collapseColumn(1);

        // Expand parent column
        expandColumn(1);
        Assert.assertTrue(spreadsheet.isColumnHidden(3));
    }

    @Test
    @Ignore("bug in GroupingUtil, collapsing and expanding parent column should not result with collapsed child columns")
    public void inverted_collapseParentColumn_expandParentColumn_columnVisible() {
        spreadsheet.setActiveSheetIndex(6);
        // Collapse parent column
        collapseColumn(1);
        Assert.assertTrue(spreadsheet.isColumnHidden(3));

        // Expand parent column
        expandColumn(1);
        Assert.assertFalse(spreadsheet.isColumnHidden(3));
    }

    private void expandRow(int row) {
        TestHelper.fireClientEvent(spreadsheet, "groupingCollapsed",
                "[false, " + row + ", false]");
    }

    private void collapseRow(int row) {
        TestHelper.fireClientEvent(spreadsheet, "groupingCollapsed",
                "[false, " + row + ", true]");
    }

    private void expandColumn(int column) {
        TestHelper.fireClientEvent(spreadsheet, "groupingCollapsed",
                "[true, " + column + ", false]");
    }

    private void collapseColumn(int column) {
        TestHelper.fireClientEvent(spreadsheet, "groupingCollapsed",
                "[true, " + column + ", true]");
    }

}
