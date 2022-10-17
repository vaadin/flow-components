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
    }

    @Test
    public void expandRow_alreadyExpanded_shouldNotThrow() {
        setActiveSheet(SHEET9);

        expandRow(SHEET9_ROW_GROUP);
    }

    @Test
    public void collapseRow_rowHidden() {
        setActiveSheet(SHEET9);

        collapseRow(SHEET9_ROW_GROUP);
        Assert.assertTrue(spreadsheet.isRowHidden(SHEET9_ROW));
    }

    @Test
    public void collapseRow_expandRow_rowVisible() {
        setActiveSheet(SHEET9);

        collapseRow(SHEET9_ROW_GROUP);
        expandRow(SHEET9_ROW_GROUP);
        Assert.assertFalse(spreadsheet.isRowHidden(SHEET9_ROW));
    }

    @Test
    public void collapseRow_collapseParentRow_expandRow_rowHidden() {
        setActiveSheet(SHEET4);
        // Collapse child row group
        collapseRow(SHEET4_ROW_GROUP);
        Assert.assertTrue(spreadsheet.isRowHidden(SHEET4_ROW));

        // Collapse parent row group
        collapseRow(SHEET4_PARENT_ROW_GROUP);

        // Expand child row group
        expandRow(SHEET4_ROW_GROUP);
        Assert.assertTrue(spreadsheet.isRowHidden(SHEET4_ROW));
    }

    @Test
    public void collapseRow_collapseParentRow_expandParentRow_rowHidden() {
        setActiveSheet(SHEET4);
        // Collapse child row group
        collapseRow(SHEET4_ROW_GROUP);
        Assert.assertTrue(spreadsheet.isRowHidden(SHEET4_ROW));

        // Collapse parent row group
        collapseRow(SHEET4_PARENT_ROW_GROUP);

        // Expand parent row group
        expandRow(SHEET4_PARENT_ROW_GROUP);
        Assert.assertTrue(spreadsheet.isRowHidden(SHEET4_ROW));
    }

    @Test
    public void collapseParentRow_expandParentRow_rowVisible() {
        setActiveSheet(SHEET4);
        // Collapse parent row group
        collapseRow(SHEET4_PARENT_ROW_GROUP);
        Assert.assertTrue(spreadsheet.isRowHidden(SHEET4_ROW));

        // Expand Parent row group
        expandRow(SHEET4_PARENT_ROW_GROUP);
        Assert.assertFalse(spreadsheet.isRowHidden(SHEET4_ROW));
    }

    @Test
    public void inverted_collapseRow_rowHidden() {
        setActiveSheet(SHEET7);

        collapseRow(SHEET7_ROW_GROUP);
        Assert.assertTrue(spreadsheet.isRowHidden(SHEET7_ROW));
    }

    @Test
    public void inverted_collapseRow_expandRow_rowVisible() {
        setActiveSheet(SHEET7);

        collapseRow(SHEET7_ROW_GROUP);
        expandRow(SHEET7_ROW_GROUP);
        Assert.assertFalse(spreadsheet.isRowHidden(SHEET7_ROW));
    }

    @Test
    public void inverted_collapseRow_collapseParentRow_expandRow_rowHidden() {
        setActiveSheet(SHEET7);

        // Collapse child row group
        collapseRow(SHEET7_ROW_GROUP);
        Assert.assertTrue(spreadsheet.isRowHidden(SHEET7_ROW));

        // Collapse parent row group
        collapseRow(SHEET7_PARENT_ROW_GROUP);

        // Expand child row group
        expandRow(SHEET7_ROW_GROUP);
        Assert.assertTrue(spreadsheet.isRowHidden(SHEET7_ROW));
    }

    @Test
    public void inverted_collapseRow_collapseParentRow_expandParentRow_rowHidden() {
        setActiveSheet(SHEET7);

        // Collapse child row group
        collapseRow(SHEET7_ROW_GROUP);
        Assert.assertTrue(spreadsheet.isRowHidden(SHEET7_ROW));

        // Collapse parent row group
        collapseRow(SHEET7_PARENT_ROW_GROUP);

        // Expand parent row group
        expandRow(SHEET7_PARENT_ROW_GROUP);
        Assert.assertTrue(spreadsheet.isRowHidden(SHEET7_ROW));
    }

    @Test
    public void inverted_collapseParentRow_expandParentRow_rowVisible() {
        setActiveSheet(SHEET7);

        // Collapse parent row group
        collapseRow(SHEET7_PARENT_ROW_GROUP);
        Assert.assertTrue(spreadsheet.isRowHidden(SHEET7_ROW));

        // Expand parent row group
        expandRow(SHEET7_PARENT_ROW_GROUP);
        Assert.assertFalse(spreadsheet.isRowHidden(SHEET7_ROW));
    }

    @Test
    public void expandColumn_alreadyExpanded_shouldNotThrow() {
        setActiveSheet(SHEET9);

        expandColumn(SHEET9_COLUMN_GROUP);
    }

    @Test
    public void collapseColumn_columnHidden() {
        setActiveSheet(SHEET9);

        collapseColumn(SHEET9_COLUMN_GROUP);
        Assert.assertTrue(spreadsheet.isColumnHidden(SHEET9_COLUMN));
    }

    @Test
    public void collapseColumn_expandColumn_columnVisible() {
        setActiveSheet(SHEET9);

        collapseColumn(SHEET9_COLUMN_GROUP);
        expandColumn(SHEET9_COLUMN_GROUP);
        Assert.assertFalse(spreadsheet.isColumnHidden(SHEET9_COLUMN));
    }

    @Test
    public void collapseColumn_collapseParentColumn_expandColumn_columnHidden() {
        setActiveSheet(SHEET6);
        // Collapse child column group
        collapseColumn(SHEET6_COLUMN_GROUP);
        Assert.assertTrue(spreadsheet.isColumnHidden(SHEET6_COLUMN));

        // Collapse parent column group
        collapseColumn(SHEET6_COLUMN_PARENT_GROUP);

        // Expand child column group
        expandColumn(SHEET6_COLUMN_GROUP);
        Assert.assertTrue(spreadsheet.isColumnHidden(SHEET6_COLUMN));
    }

    @Test
    public void collapseColumn_collapseParentColumn_expandParentColumn_columnHidden() {
        setActiveSheet(SHEET6);
        // Collapse child column group
        collapseColumn(SHEET6_COLUMN_GROUP);
        Assert.assertTrue(spreadsheet.isColumnHidden(SHEET6_COLUMN));

        // Collapse parent column group
        collapseColumn(SHEET6_COLUMN_PARENT_GROUP);

        // Expand parent column group
        expandColumn(SHEET6_COLUMN_PARENT_GROUP);
        Assert.assertTrue(spreadsheet.isColumnHidden(SHEET6_COLUMN));
    }

    @Ignore("bug in GroupingUtil, collapsing and expanding parent column should not result with collapsed child columns")
    @Test
    public void collapseParentColumn_expandParentColumn_columnVisible() {
        setActiveSheet(SHEET6);
        // Collapse parent column group
        collapseColumn(SHEET6_COLUMN_PARENT_GROUP);
        Assert.assertTrue(spreadsheet.isColumnHidden(SHEET6_COLUMN));

        // Expand Parent column group
        expandColumn(SHEET6_COLUMN_PARENT_GROUP);
        Assert.assertFalse(spreadsheet.isColumnHidden(SHEET6_COLUMN));
    }

    @Test
    public void inverted_collapseColumn_columnHidden() {
        setActiveSheet(SHEET7);

        collapseColumn(SHEET7_COLUMN_GROUP);
        Assert.assertTrue(spreadsheet.isColumnHidden(SHEET7_COLUMN));
    }

    @Test
    public void inverted_collapseColumn_expandColumn_columnVisible() {
        setActiveSheet(SHEET7);

        collapseColumn(SHEET7_COLUMN_GROUP);
        expandColumn(SHEET7_COLUMN_GROUP);
        Assert.assertFalse(spreadsheet.isColumnHidden(SHEET7_COLUMN));
    }

    @Test
    public void inverted_collapseColumn_collapseParentColumn_expandColumn_columnHidden() {
        setActiveSheet(SHEET7);

        // Collapse child column group
        collapseColumn(SHEET7_COLUMN_GROUP);
        Assert.assertTrue(spreadsheet.isColumnHidden(SHEET7_COLUMN));

        // Collapse parent column group
        collapseColumn(SHEET7_PARENT_COLUMN_GROUP);

        // Expand child column group
        expandColumn(SHEET7_COLUMN_GROUP);
        Assert.assertTrue(spreadsheet.isColumnHidden(SHEET7_COLUMN));
    }

    @Test
    public void inverted_collapseColumn_collapseParentColumn_expandParentColumn_columnHidden() {
        setActiveSheet(SHEET7);

        // Collapse child column group
        collapseColumn(SHEET7_COLUMN_GROUP);
        Assert.assertTrue(spreadsheet.isColumnHidden(SHEET7_COLUMN));

        // Collapse parent column group
        collapseColumn(SHEET7_PARENT_COLUMN_GROUP);

        // Expand parent column group
        expandColumn(SHEET7_PARENT_COLUMN_GROUP);
        Assert.assertTrue(spreadsheet.isColumnHidden(SHEET7_COLUMN));
    }

    @Test
    @Ignore("bug in GroupingUtil, collapsing and expanding parent column should not result with collapsed child columns")
    public void inverted_collapseParentColumn_expandParentColumn_columnVisible() {
        setActiveSheet(SHEET7);

        // Collapse parent column group
        collapseColumn(SHEET7_PARENT_COLUMN_GROUP);
        Assert.assertTrue(spreadsheet.isColumnHidden(SHEET7_COLUMN));

        // Expand parent column group
        expandColumn(SHEET7_PARENT_COLUMN_GROUP);
        Assert.assertFalse(spreadsheet.isColumnHidden(SHEET7_COLUMN));
    }

    @Test
    public void clickRowLevelHeader_shouldNotHidePrecedingGroups() {
        setActiveSheet(SHEET6);

        clickRowLevelHeader(4);
        Assert.assertFalse(spreadsheet.isRowHidden(6));
    }

    @Test
    public void clickRowLevelHeader_shouldHideFollowingGroups() {
        setActiveSheet(SHEET6);

        clickRowLevelHeader(4);
        Assert.assertTrue(spreadsheet.isRowHidden(4));
    }

    @Test
    public void clickRowLevelHeader_clickLastLevelHeader_shouldUnhideGroups() {
        setActiveSheet(SHEET6);

        clickRowLevelHeader(4);
        clickRowLevelHeader(8);
        Assert.assertFalse(spreadsheet.isRowHidden(4));
    }

    @Test
    public void clickColumnLevelHeader_shouldNotHidePrecedingGroups() {
        setActiveSheet(SHEET6);

        clickColumnLevelHeader(4);
        Assert.assertFalse(spreadsheet.isColumnHidden(6));
    }

    @Test
    public void clickColumnLevelHeader_shouldHideFollowingGroups() {
        setActiveSheet(SHEET6);

        clickColumnLevelHeader(4);
        Assert.assertTrue(spreadsheet.isColumnHidden(4));
    }

    @Test
    public void clickColumnLevelHeader_clickLastLevelHeader_shouldUnhideGroups() {
        setActiveSheet(SHEET6);

        clickColumnLevelHeader(4);
        clickColumnLevelHeader(8);
        Assert.assertFalse(spreadsheet.isColumnHidden(4));
    }

    private void setActiveSheet(int sheetIndex) {
        spreadsheet.setActiveSheetIndex(sheetIndex);
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "[1, 1, 1, 1]");
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

    private void clickRowLevelHeader(int level) {
        TestHelper.fireClientEvent(spreadsheet, "levelHeaderClicked",
                "[false, " + level + "]");
    }

    private void clickColumnLevelHeader(int level) {
        TestHelper.fireClientEvent(spreadsheet, "levelHeaderClicked",
                "[true, " + level + "]");
    }

    private final int SHEET4 = 3;
    private final int SHEET4_ROW_GROUP = 1;
    private final int SHEET4_ROW = 2;
    private final int SHEET4_PARENT_ROW_GROUP = 0;

    private final int SHEET6 = 5;
    private final int SHEET6_COLUMN_GROUP = 0;
    private final int SHEET6_COLUMN = 1;
    private final int SHEET6_COLUMN_PARENT_GROUP = 2;

    private final int SHEET7 = 6;
    private final int SHEET7_ROW_GROUP = 3;
    private final int SHEET7_ROW = 3;
    private final int SHEET7_PARENT_ROW_GROUP = 2;
    private final int SHEET7_COLUMN_GROUP = 2;
    private final int SHEET7_COLUMN = 3;
    private final int SHEET7_PARENT_COLUMN_GROUP = 1;

    private final int SHEET9 = 8;
    private final int SHEET9_ROW_GROUP = 1;
    private final int SHEET9_ROW = 3;
    private final int SHEET9_COLUMN_GROUP = 1;
    private final int SHEET9_COLUMN = 3;

}
