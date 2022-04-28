package com.vaadin.addon.spreadsheet.client;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.google.gwt.user.client.ui.Widget;

/**
 * Class that handles finer details of cell selection inside the Spreadsheet.
 *
 * @author Thomas Mattsson / Vaadin Ltd.
 */
public class SelectionHandler {

    private final SheetWidget sheetWidget;
    private final SpreadsheetWidget spreadsheet;

    private int colBeforeMergedCell;
    private int rowBeforeMergedCell;

    public SelectionHandler(SpreadsheetWidget spreadsheet, SheetWidget widget) {
        sheetWidget = widget;
        this.spreadsheet = spreadsheet;
    }

    public void moveSelectionDown(boolean discardSelection) {
        final int leftCol = sheetWidget.getSelectionLeftCol();
        final int rightCol = sheetWidget.getSelectionRightCol();
        final int topRow = sheetWidget.getSelectionTopRow();
        final int bottomRow = sheetWidget.getSelectionBottomRow();
        int col = sheetWidget.getSelectedCellColumn();
        int row = sheetWidget.getSelectedCellRow();
        // if the old selected cell was a merged cell, it changes the actual
        // selected cell
        MergedRegion oldRegion = spreadsheet.getMergedRegion(col, row);
        if (oldRegion != null && colBeforeMergedCell != 0) {
            col = colBeforeMergedCell;
            row = oldRegion.row2;
        }
        row++;

        while (spreadsheet.hiddenRowIndexes != null
                && spreadsheet.hiddenRowIndexes.contains(row)
                && row < spreadsheet.getMaxRows()) {
            row++;
        }

        if (!discardSelection && (leftCol != rightCol || topRow != bottomRow)
                && (oldRegion == null || leftCol != oldRegion.col1
                        || rightCol != oldRegion.col2
                        || topRow != oldRegion.row1
                        || bottomRow != oldRegion.row2)) {
            // move the selected cell inside the selection
            if (row > bottomRow) {
                // move highest and right
                row = topRow;
                // if the row on top is hidden, skip it
                while (spreadsheet.hiddenRowIndexes != null
                        && spreadsheet.hiddenRowIndexes.contains(row)
                        && row < bottomRow) {
                    row++;
                }
                col++;
                // if the column on right is hidden, skip it
                while (spreadsheet.hiddenColumnIndexes != null
                        && spreadsheet.hiddenColumnIndexes.contains(col)
                        && col <= rightCol) {
                    col++;
                }
                if (col > rightCol) {
                    // move to left
                    col = leftCol;
                }
                while (spreadsheet.hiddenColumnIndexes != null
                        && spreadsheet.hiddenColumnIndexes.contains(col)
                        && col <= rightCol) {
                    col++;
                }
            }
            checkNewSelectionInMergedRegion(col, row);
        } else {
            if (row <= spreadsheet.getMaxRows()) {
                // if the new selected cell is a merged cell
                checkSelectionInMergedRegion(col, row);
            }
        }
    }

    public void selectCellRange(String name, int selectedCellColumn,
            int selectedCellRow, int firstColumn, int lastColumn, int firstRow,
            int lastRow, boolean scroll) {
        spreadsheet.updateSelectedCellValues(selectedCellColumn,
                selectedCellRow, name);
        if (!sheetWidget.isCoherentSelection()) {
            sheetWidget.setCoherentSelection(true);
        }
        if (!sheetWidget.isSelectionRangeOutlineVisible()) {
            sheetWidget.setSelectionRangeOutlineVisible(true);
            sheetWidget.clearSelectedCellStyle();
        }
        final int oldSelectedCellCol = sheetWidget.getSelectedCellColumn();
        final int oldSelectedCellRow = sheetWidget.getSelectedCellRow();
        if (oldSelectedCellCol != selectedCellColumn
                || oldSelectedCellRow != selectedCellRow) {
            sheetWidget.setSelectedCell(selectedCellColumn, selectedCellRow);
            newSelectedCellSet();
        }
        sheetWidget.updateSelectionOutline(firstColumn, lastColumn, firstRow,
                lastRow);
        sheetWidget.updateSelectedCellStyles(firstColumn, lastColumn, firstRow,
                lastRow, true);
        if (scroll && !sheetWidget.isAreaCompletelyVisible(firstColumn,
                lastColumn, firstRow, lastRow)) {
            sheetWidget.scrollAreaIntoView(firstColumn, lastColumn, firstRow,
                    lastRow);
        }

        sheetWidget.focusSheet();
    }

    public void selectCell(String name, int col, int row, String value,
            boolean formula, boolean locked, boolean initialSelection) {
        if (spreadsheet.customCellEditorDisplayed) {
            spreadsheet.customCellEditorDisplayed = false;
            sheetWidget.removeCustomCellEditor();
        }
        spreadsheet.cellLocked = locked;
        sheetWidget.setSelectedCell(col, row);
        newSelectedCellSet();

        if (!sheetWidget.isCoherentSelection()) {
            sheetWidget.setCoherentSelection(true);
        }
        if (!sheetWidget.isSelectionRangeOutlineVisible()) {
            sheetWidget.setSelectionRangeOutlineVisible(true);
            sheetWidget.clearSelectedCellStyle();
        }
        sheetWidget.updateSelectionOutline(col, col, row, row);
        sheetWidget.updateSelectedCellStyles(col, col, row, row, true);
        if (formula) {
            spreadsheet.formulaBarWidget.setCellFormulaValue(value);
        } else {
            spreadsheet.formulaBarWidget.setCellPlainValue(value);
        }
        spreadsheet.formulaBarWidget.setFormulaFieldEnabled(!locked);
        if (name != null) {
            spreadsheet.formulaBarWidget.setSelectedCellAddress(name);
        } else {
            spreadsheet.formulaBarWidget.setSelectedCellAddress(
                    spreadsheet.createCellAddress(col, row));
        }

        // scroll the cell into view
        if (!sheetWidget.isSelectedCellCompletelyVisible()) {
            sheetWidget.scrollCellIntoView(col, row);
        }
        if (!initialSelection) {
            sheetWidget.focusSheet();
        }
    }

    public void newSelectedCellSet() {
        if (spreadsheet.customCellEditorDisplayed) {
            spreadsheet.customCellEditorDisplayed = false;
            sheetWidget.removeCustomCellEditor();
        }

        if (!sheetWidget.isSelectedCellCustomized() && !spreadsheet.cellLocked
                && spreadsheet.customEditorFactory != null
                && spreadsheet.customEditorFactory
                        .hasCustomEditor(sheetWidget.getSelectedCellKey())) {
            Widget customEditor = spreadsheet.customEditorFactory
                    .getCustomEditor(sheetWidget.getSelectedCellKey());
            if (customEditor != null) {
                spreadsheet.customCellEditorDisplayed = true;
                spreadsheet.formulaBarWidget.setFormulaFieldEnabled(false);
                sheetWidget.displayCustomCellEditor(customEditor);
            }
        }
    }

    public void moveSelectionUp(boolean discardSelection) {
        final int leftCol = sheetWidget.getSelectionLeftCol();
        final int rightCol = sheetWidget.getSelectionRightCol();
        final int topRow = sheetWidget.getSelectionTopRow();
        final int bottomRow = sheetWidget.getSelectionBottomRow();
        int col = sheetWidget.getSelectedCellColumn();
        int row = sheetWidget.getSelectedCellRow();
        // if the old selected cell was a merged cell, it changes the actual
        // selected cell
        MergedRegion oldRegion = spreadsheet.getMergedRegion(col, row);
        if (oldRegion != null && colBeforeMergedCell != 0) {
            col = colBeforeMergedCell;
            row = oldRegion.row1;
        }
        row--;

        while (spreadsheet.hiddenRowIndexes != null
                && spreadsheet.hiddenRowIndexes.contains(row) && row > 1) {
            row--;
        }
        if (!discardSelection && (leftCol != rightCol || topRow != bottomRow)
                && (oldRegion == null || leftCol != oldRegion.col1
                        || rightCol != oldRegion.col2
                        || topRow != oldRegion.row1
                        || bottomRow != oldRegion.row2)) {
            // move the selected cell inside the selection
            if (row < topRow) {
                // go to bottom and left
                row = bottomRow;
                // if row on bottom is hidden, skip it
                while (spreadsheet.hiddenRowIndexes != null
                        && spreadsheet.hiddenRowIndexes.contains(row)
                        && row > topRow) {
                    row--;
                }
                col--;
                while (spreadsheet.hiddenColumnIndexes != null
                        && spreadsheet.hiddenColumnIndexes.contains(col)
                        && col >= leftCol) {
                    col--;
                }
                if (col < leftCol) {
                    // go to right most
                    col = rightCol;
                }
                while (spreadsheet.hiddenColumnIndexes != null
                        && spreadsheet.hiddenColumnIndexes.contains(col)
                        && col >= leftCol) {
                    col--;
                }
            }
            checkNewSelectionInMergedRegion(col, row);
        } else {
            if (row > 0) {
                checkSelectionInMergedRegion(col, row);
            }
        }
    }

    protected void onCellSelectedWithKeyboard(int column, int row, String value,
            MergedRegion region) {
        spreadsheet.doCommitIfEditing();
        if (!sheetWidget.isCoherentSelection()) {
            sheetWidget.setCoherentSelection(true);
        }
        if (!sheetWidget.isSelectionRangeOutlineVisible()) {
            sheetWidget.setSelectionRangeOutlineVisible(true);
            sheetWidget.clearSelectedCellStyle();
        }
        sheetWidget.setSelectedCell(column, row);
        sheetWidget.updateSelectionOutline(column, column, row, row);
        if (region != null) {
            sheetWidget.updateSelectedCellStyles(column, region.col2, row,
                    region.row2, true);
        } else {
            sheetWidget.updateSelectedCellStyles(column, column, row, row,
                    true);
        }
        newSelectedCellSet();
        spreadsheet.updateSelectedCellValues(column, row);
        spreadsheet.spreadsheetHandler.cellSelected(row, column, true);
        spreadsheet.startDelayedSendingTimer();
    }

    public void increaseHorizontalSelection(boolean right) {

        // TODO refactor to smaller pieces

        int topRow = sheetWidget.getSelectionTopRow();
        int leftCol = sheetWidget.getSelectionLeftCol();
        final int oldLeftCol = leftCol;
        int rightCol = sheetWidget.getSelectionRightCol();
        final int oldRightCol = rightCol;
        int bottomRow = sheetWidget.getSelectionBottomRow();
        int selectedCellColumn = sheetWidget.getSelectedCellColumn();
        final int selectedCellRow = sheetWidget.getSelectedCellRow();
        MergedRegion region = spreadsheet.mergedRegionContainer
                .getMergedRegionStartingFrom(selectedCellColumn,
                        selectedCellRow);

        boolean actOnLeftSide = false;

        if (sheetWidget.isCoherentSelection()) {
            // the selection outline is the "correct", even with merged cells,
            // as with a merged cell the selected cell doesn't take the merged
            // edge into account.
            if (region != null && (right && region.col1 != leftCol
                    || !right && region.col2 == rightCol)) {
                selectedCellColumn = region.col2;
            }
            MergedRegion selection = null;
            if (selectedCellColumn == leftCol) {
                if (right && rightCol + 1 <= spreadsheet.getMaxColumns()) {
                    // increase to right
                    rightCol++;
                    while (spreadsheet.hiddenColumnIndexes != null
                            && spreadsheet.hiddenColumnIndexes
                                    .contains(rightCol)
                            && rightCol < spreadsheet.getMaxColumns()) {
                        rightCol++;
                    }
                    selection = MergedRegionUtil.findIncreasingSelection(
                            spreadsheet.mergedRegionContainer, topRow,
                            bottomRow, leftCol, rightCol);
                } else if (!right) {
                    if (rightCol != leftCol) {
                        // Decrease from right
                        rightCol--;
                        while (spreadsheet.hiddenColumnIndexes != null
                                && spreadsheet.hiddenColumnIndexes
                                        .contains(rightCol)
                                && (rightCol) > leftCol) {
                            rightCol--;
                        }
                        selection = findDecreasingSelection(topRow, bottomRow,
                                leftCol, rightCol);
                    } else if (leftCol - 1 > 0) {
                        // Increase to left
                        actOnLeftSide = true;
                        leftCol--;
                        while (spreadsheet.hiddenColumnIndexes != null
                                && spreadsheet.hiddenColumnIndexes
                                        .contains(leftCol)
                                && leftCol > 1) {
                            leftCol--;
                        }
                        selection = MergedRegionUtil.findIncreasingSelection(
                                spreadsheet.mergedRegionContainer, topRow,
                                bottomRow, leftCol, rightCol);
                    }
                }
            } else if (selectedCellColumn == rightCol) {
                if (right) {
                    if (rightCol != leftCol) {
                        // Decrease from left
                        actOnLeftSide = true;
                        leftCol++;
                        while (spreadsheet.hiddenColumnIndexes != null
                                && spreadsheet.hiddenColumnIndexes
                                        .contains(leftCol)
                                && leftCol < rightCol) {
                            leftCol++;
                        }
                        selection = findDecreasingSelection(topRow, bottomRow,
                                leftCol, rightCol);
                    } else if (rightCol + 1 <= spreadsheet.getMaxColumns()) {
                        // increase to right
                        rightCol++;
                        while (spreadsheet.hiddenColumnIndexes != null
                                && spreadsheet.hiddenColumnIndexes
                                        .contains(rightCol)
                                && rightCol < spreadsheet.getMaxColumns()) {
                            rightCol++;
                        }
                        selection = MergedRegionUtil.findIncreasingSelection(
                                spreadsheet.mergedRegionContainer, topRow,
                                bottomRow, leftCol, rightCol);
                    }
                } else if (!right && leftCol - 1 > 0) {
                    // Increase to left
                    actOnLeftSide = true;
                    leftCol--;
                    while (spreadsheet.hiddenColumnIndexes != null
                            && spreadsheet.hiddenColumnIndexes.contains(leftCol)
                            && leftCol > 1) {
                        leftCol--;
                    }
                    selection = MergedRegionUtil.findIncreasingSelection(
                            spreadsheet.mergedRegionContainer, topRow,
                            bottomRow, leftCol, rightCol);
                }
            } else {
                if (right) {
                    // Increase to right
                    if (rightCol + 1 <= spreadsheet.getMaxColumns()) {
                        rightCol++;
                        while (spreadsheet.hiddenColumnIndexes != null
                                && spreadsheet.hiddenColumnIndexes
                                        .contains(rightCol)
                                && rightCol < spreadsheet.getMaxColumns()) {
                            rightCol++;
                        }
                        selection = MergedRegionUtil.findIncreasingSelection(
                                spreadsheet.mergedRegionContainer, topRow,
                                bottomRow, leftCol, rightCol);
                    }
                } else {
                    // Increase to left
                    actOnLeftSide = true;
                    if (leftCol - 1 > 0) {
                        leftCol--;
                        while (spreadsheet.hiddenColumnIndexes != null
                                && spreadsheet.hiddenColumnIndexes
                                        .contains(leftCol)
                                && leftCol > 1) {
                            leftCol--;
                        }
                        selection = MergedRegionUtil.findIncreasingSelection(
                                spreadsheet.mergedRegionContainer, topRow,
                                bottomRow, leftCol, rightCol);
                    }
                }
            }
            if (selection == null) {
                return;
            }
            sheetWidget.updateSelectionOutline(selection.col1, selection.col2,
                    selection.row1, selection.row2);
            sheetWidget.replaceAsSelectedCells(selection.col1, selection.col2,
                    selection.row1, selection.row2);
            sheetWidget.replaceHeadersAsSelected(selection.row1, selection.row2,
                    selection.col1, selection.col2);
            sheetWidget.scrollAreaIntoViewHorizontally(selection.col1,
                    selection.col2, actOnLeftSide);
        } else { // previous selection not coherent
            // discard the old selection and start from previously selected cell
            int row2;
            int col2;
            if (region != null) {
                row2 = region.row2;
                col2 = region.col2;
            } else {
                row2 = selectedCellRow;
                col2 = selectedCellColumn;
            }
            if (right) {
                col2++;
                while (spreadsheet.hiddenColumnIndexes != null
                        && spreadsheet.hiddenColumnIndexes.contains(col2)
                        && col2 < spreadsheet.getMaxColumns()) {
                    col2++;
                }
            } else {
                selectedCellColumn--;
                while (spreadsheet.hiddenColumnIndexes != null
                        && spreadsheet.hiddenColumnIndexes
                                .contains(selectedCellColumn)
                        && selectedCellColumn > 1) {
                    selectedCellColumn--;
                }
            }
            if (selectedCellColumn > 0 && col2 < spreadsheet.getMaxColumns()) {
                MergedRegion selection = MergedRegionUtil
                        .findIncreasingSelection(
                                spreadsheet.mergedRegionContainer,
                                selectedCellRow, row2, selectedCellColumn,
                                col2);
                if (selection != null) {
                    // sheetWidget.clearCellRangeStyles();
                    sheetWidget.setCoherentSelection(true);
                    sheetWidget.setSelectionRangeOutlineVisible(true);
                    sheetWidget.clearSelectedCellStyle();
                    sheetWidget.updateSelectionOutline(selection.col1,
                            selection.col2, selection.row1, selection.row2);
                    sheetWidget.updateSelectedCellStyles(selection.col1,
                            selection.col2, selection.row1, selection.row2,
                            true);
                }
            }
            // scroll area into view
            sheetWidget.scrollSelectionAreaIntoView();
        }

        // update action handler
        if (oldLeftCol != sheetWidget.getSelectionLeftCol()
                || oldRightCol != sheetWidget.getSelectionRightCol()
                || topRow != sheetWidget.getSelectionTopRow()
                || bottomRow != sheetWidget.getSelectionBottomRow()) {
            spreadsheet.spreadsheetHandler.cellRangeSelected(
                    sheetWidget.getSelectionTopRow(),
                    sheetWidget.getSelectionLeftCol(),
                    sheetWidget.getSelectionBottomRow(),
                    sheetWidget.getSelectionRightCol());
            spreadsheet.startDelayedSendingTimer();
        }
    }

    /**
     * Goes through the given selection and checks that the cells on the edges
     * of the selection are not in "the beginning / middle / end" of a merged
     * cell. Returns the correct decreased selection, after taking the merged
     * cells into account.
     *
     * Parameters 1-based.
     *
     * @param topRow
     * @param bottomRow
     * @param leftColumn
     * @param rightColumn
     * @return merged region
     */
    protected MergedRegion findDecreasingSelection(int topRow, int bottomRow,
            int leftColumn, int rightColumn) {

        if (topRow == bottomRow && leftColumn == rightColumn) {
            MergedRegion mergedRegion = spreadsheet.getMergedRegion(leftColumn,
                    topRow);
            if (mergedRegion == null) {
                mergedRegion = new MergedRegion();
                mergedRegion.col1 = leftColumn;
                mergedRegion.col2 = rightColumn;
                mergedRegion.row1 = topRow;
                mergedRegion.row2 = bottomRow;
            }
            return mergedRegion;
        } else {
            MergedRegion merged = spreadsheet
                    .getMergedRegionStartingFrom(leftColumn, topRow);
            if (merged != null && merged.col2 >= rightColumn
                    && merged.row2 >= bottomRow) {
                return merged;
            }
        }
        int selectedCellColumn = sheetWidget.getSelectedCellColumn();
        int selectedCellRow = sheetWidget.getSelectedCellRow();

        if (selectedCellColumn < leftColumn || selectedCellColumn > rightColumn
                || selectedCellRow < topRow || selectedCellRow > bottomRow) {
            return spreadsheet.getMergedRegion(selectedCellColumn,
                    sheetWidget.getSelectedCellRow());
        }

        boolean trouble = false;
        int i = leftColumn;
        // go through top edge
        while (i <= rightColumn) {
            MergedRegion region = spreadsheet.getMergedRegion(i, topRow);
            if (region != null) {
                i = region.col2 + 1;
                if (topRow > region.row1) {
                    // check if the cell in top row is in middle or end of a
                    // merged cell -> decrease more if it is
                    trouble = true;
                    if (topRow < bottomRow) {
                        if (region.row2 > bottomRow) {
                            topRow = region.row2 + 1;
                        } else {
                            topRow = bottomRow;
                        }
                        i = leftColumn;
                    } else {
                        if (selectedCellColumn < region.col1) {
                            rightColumn = region.col1 - 1;
                        } else if (selectedCellColumn > region.col2) {
                            leftColumn = region.col2 + 1;
                        } else {
                            leftColumn = region.col1;
                            rightColumn = region.col2;
                            break;
                        }
                    }
                }
            } else {
                i++;
            }
        }
        if (topRow > bottomRow) {
            topRow = bottomRow;
        }
        // go through right edge
        i = topRow;
        while (i <= bottomRow) {
            MergedRegion region = spreadsheet.getMergedRegion(rightColumn, i);
            if (region != null) {
                i = region.row2 + 1;
                if (rightColumn < region.col2) {
                    trouble = true;
                    if (rightColumn > leftColumn) {
                        if (region.col1 > leftColumn) {
                            rightColumn = region.col1 - 1;
                        } else {
                            rightColumn = leftColumn;
                        }
                        i = topRow;
                    } else {
                        if (selectedCellRow < region.row1) {
                            bottomRow = region.row1 - 1;
                        } else if (selectedCellRow > region.row2) {
                            topRow = region.row2 + 1;
                        } else { // selected cell row is inside the region
                            topRow = region.row1;
                            bottomRow = region.row2;
                            break;
                        }
                    }
                }
            } else {
                i++;
            }
        }
        if (rightColumn < leftColumn) {
            rightColumn = leftColumn;
        }
        // go through bottom edge
        i = leftColumn;
        while (i <= rightColumn) {
            MergedRegion region = spreadsheet.getMergedRegion(i, bottomRow);
            if (region != null) {
                i = region.col2 + 1;
                if (bottomRow < region.row2) {
                    trouble = true;
                    if (bottomRow > topRow) {
                        if (topRow < region.row1) {
                            bottomRow = region.row1 - 1;
                        } else {
                            bottomRow = topRow;
                        }
                        i = leftColumn;
                    } else {
                        if (selectedCellColumn < region.col1) {
                            rightColumn = region.col1 - 1;
                        } else if (selectedCellColumn > region.col2) {
                            leftColumn = region.col2 + 1;
                        } else {
                            rightColumn = region.col1;
                            leftColumn = region.col2;
                            break;
                        }
                    }
                }
            } else {
                i++;
            }
        }
        if (bottomRow < topRow) {
            bottomRow = topRow;
        }
        // go through left edge
        i = topRow;
        while (i <= bottomRow) {
            MergedRegion region = spreadsheet.getMergedRegion(leftColumn, i);
            if (region != null) {
                i = region.row2 + 1;
                if (leftColumn > region.col1) {
                    trouble = true;
                    if (leftColumn < rightColumn) {
                        if (rightColumn > region.col2) {
                            leftColumn = region.col2 + 1;
                        } else {
                            leftColumn = rightColumn;
                        }
                        i = topRow;
                    } else {
                        if (selectedCellRow < region.row1) {
                            bottomRow = region.row1 - 1;
                        } else if (selectedCellRow > region.row2) {
                            topRow = region.row2 + 1;
                        } else {
                            topRow = region.row1;
                            bottomRow = region.row2;
                            break;
                        }
                    }
                }
            } else {
                i++;
            }
        }
        if (leftColumn > rightColumn) {
            leftColumn = rightColumn;
        }
        if (trouble) {
            return findDecreasingSelection(topRow, bottomRow, leftColumn,
                    rightColumn);
        } else if (topRow == bottomRow && leftColumn == rightColumn) {
            MergedRegion mergedRegion = spreadsheet.getMergedRegion(leftColumn,
                    topRow);
            if (mergedRegion == null) {
                mergedRegion = new MergedRegion();
                mergedRegion.col1 = leftColumn;
                mergedRegion.col2 = rightColumn;
                mergedRegion.row1 = topRow;
                mergedRegion.row2 = bottomRow;
            }
            return mergedRegion;
        } else {
            MergedRegion merged = spreadsheet
                    .getMergedRegionStartingFrom(leftColumn, topRow);
            if (merged != null && merged.col2 >= rightColumn
                    && merged.row2 >= bottomRow) {
                return merged;
            }
        }
        MergedRegion result = new MergedRegion();
        result.col1 = leftColumn;
        result.col2 = rightColumn;
        result.row1 = topRow;
        result.row2 = bottomRow;
        return result;
    }

    public void increaseVerticalSelection(boolean down) {

        // TODO refactor to smaller pieces

        int topRow = sheetWidget.getSelectionTopRow();
        int oldTopRow = topRow;
        final int leftCol = sheetWidget.getSelectionLeftCol();
        int bottomRow = sheetWidget.getSelectionBottomRow();
        int oldBottomRow = bottomRow;
        final int rightCol = sheetWidget.getSelectionRightCol();
        int selectedCellRow = sheetWidget.getSelectedCellRow();
        final int selectedCellColumn = sheetWidget.getSelectedCellColumn();
        MergedRegion region = spreadsheet.getMergedRegionStartingFrom(
                selectedCellColumn, selectedCellRow);

        boolean actOnTopEdge = false;

        if (sheetWidget.isCoherentSelection()) {
            if (region != null && (down && region.row1 != topRow
                    || !down && region.row2 == bottomRow)) {
                selectedCellRow = region.row2;
            }
            MergedRegion selection = null;
            if (selectedCellRow == topRow) {
                if (down && bottomRow + 1 <= spreadsheet.getMaxRows()) {
                    // increase selection down
                    bottomRow++;
                    while (spreadsheet.hiddenRowIndexes != null
                            && spreadsheet.hiddenRowIndexes.contains(bottomRow)
                            && bottomRow < spreadsheet.getMaxRows()) {
                        bottomRow++;
                    }
                    selection = MergedRegionUtil.findIncreasingSelection(
                            spreadsheet.mergedRegionContainer, topRow,
                            bottomRow, leftCol, rightCol);
                } else if (!down) {
                    if (topRow != bottomRow) {
                        // Decrease from bottom
                        bottomRow--;
                        while (spreadsheet.hiddenRowIndexes != null
                                && spreadsheet.hiddenRowIndexes
                                        .contains(bottomRow)
                                && bottomRow > topRow) {
                            bottomRow--;
                        }
                        selection = findDecreasingSelection(topRow, bottomRow,
                                leftCol, rightCol);
                    } else if (topRow - 1 > 0) {
                        // Increase up
                        actOnTopEdge = true;
                        topRow--;
                        while (spreadsheet.hiddenRowIndexes != null
                                && spreadsheet.hiddenRowIndexes.contains(topRow)
                                && topRow > 1) {
                            topRow--;
                        }
                        selection = MergedRegionUtil.findIncreasingSelection(
                                spreadsheet.mergedRegionContainer, topRow,
                                bottomRow, leftCol, rightCol);
                    }
                }
            } else if (selectedCellRow == bottomRow) {
                if (down) {
                    if (topRow != bottomRow) {
                        // Decrease from top
                        actOnTopEdge = true;
                        topRow++;
                        while (spreadsheet.hiddenRowIndexes != null
                                && spreadsheet.hiddenRowIndexes.contains(topRow)
                                && topRow < bottomRow) {
                            topRow++;
                        }
                        selection = findDecreasingSelection(topRow, bottomRow,
                                leftCol, rightCol);
                    } else if (bottomRow + 1 <= spreadsheet.getMaxRows()) {
                        // increase selection down
                        bottomRow++;
                        while (spreadsheet.hiddenRowIndexes != null
                                && spreadsheet.hiddenRowIndexes
                                        .contains(bottomRow)
                                && bottomRow < spreadsheet.getMaxRows()) {
                            bottomRow++;
                        }
                        selection = MergedRegionUtil.findIncreasingSelection(
                                spreadsheet.mergedRegionContainer, topRow,
                                bottomRow, leftCol, rightCol);
                    }
                } else if (!down && topRow - 1 > 0) {
                    // Increase up
                    actOnTopEdge = true;
                    topRow--;
                    while (spreadsheet.hiddenRowIndexes != null
                            && spreadsheet.hiddenRowIndexes.contains(topRow)
                            && topRow > 1) {
                        topRow--;
                    }
                    selection = MergedRegionUtil.findIncreasingSelection(
                            spreadsheet.mergedRegionContainer, topRow,
                            bottomRow, leftCol, rightCol);
                }
            } else {
                // Increase the selection on the desired direction
                if (down) {
                    if (bottomRow + 1 <= spreadsheet.getMaxRows()) {
                        bottomRow++;
                        while (spreadsheet.hiddenRowIndexes != null
                                && spreadsheet.hiddenRowIndexes
                                        .contains(bottomRow)
                                && bottomRow < spreadsheet.getMaxRows()) {
                            bottomRow++;
                        }
                        selection = MergedRegionUtil.findIncreasingSelection(
                                spreadsheet.mergedRegionContainer, topRow,
                                bottomRow, leftCol, rightCol);
                    }
                } else {
                    actOnTopEdge = true;
                    if (topRow - 1 > 0) {
                        topRow--;
                        while (spreadsheet.hiddenRowIndexes != null
                                && spreadsheet.hiddenRowIndexes.contains(topRow)
                                && topRow > 1) {
                            topRow--;
                        }
                        selection = MergedRegionUtil.findIncreasingSelection(
                                spreadsheet.mergedRegionContainer, topRow,
                                bottomRow, leftCol, rightCol);
                    }
                }
            }
            if (selection == null) {
                return;
            }
            sheetWidget.updateSelectionOutline(selection.col1, selection.col2,
                    selection.row1, selection.row2);
            sheetWidget.replaceAsSelectedCells(selection.col1, selection.col2,
                    selection.row1, selection.row2);
            sheetWidget.replaceHeadersAsSelected(selection.row1, selection.row2,
                    selection.col1, selection.col2);
            sheetWidget.scrollAreaIntoViewVertically(selection.row1,
                    selection.row2, actOnTopEdge);
        } else {
            // previous selection not coherent
            // discard the old selection and start from previously selected cell
            int row2;
            int col2;
            if (region != null) {
                row2 = region.row2;
                col2 = region.col2;
            } else {
                row2 = selectedCellRow;
                col2 = selectedCellColumn;
            }
            if (down) {
                row2++;
                while (spreadsheet.hiddenRowIndexes != null
                        && spreadsheet.hiddenRowIndexes.contains(row2)
                        && row2 < spreadsheet.getMaxRows()) {
                    row2++;
                }
            } else {
                selectedCellRow--;
                while (spreadsheet.hiddenRowIndexes != null
                        && spreadsheet.hiddenRowIndexes
                                .contains(selectedCellRow)
                        && selectedCellRow > 1) {
                    selectedCellRow--;
                }
            }
            if (selectedCellRow > 0 && row2 <= spreadsheet.getMaxRows()) {
                MergedRegion selection = MergedRegionUtil
                        .findIncreasingSelection(
                                spreadsheet.mergedRegionContainer,
                                selectedCellRow, row2, selectedCellColumn,
                                col2);
                if (selection != null) {
                    sheetWidget.setCoherentSelection(true);
                    sheetWidget.setSelectionRangeOutlineVisible(true);
                    sheetWidget.clearSelectedCellStyle();
                    sheetWidget.updateSelectionOutline(selection.col1,
                            selection.col2, selection.row1, selection.row2);
                    sheetWidget.updateSelectedCellStyles(selection.col1,
                            selection.col2, selection.row1, selection.row2,
                            true);
                }
            }
            // scroll area into view
            sheetWidget.scrollSelectionAreaIntoView();
        }
        // update action handler
        if (leftCol != sheetWidget.getSelectionLeftCol()
                || rightCol != sheetWidget.getSelectionRightCol()
                || oldTopRow != sheetWidget.getSelectionTopRow()
                || oldBottomRow != sheetWidget.getSelectionBottomRow()) {
            spreadsheet.spreadsheetHandler.cellRangeSelected(
                    sheetWidget.getSelectionTopRow(),
                    sheetWidget.getSelectionLeftCol(),
                    sheetWidget.getSelectionBottomRow(),
                    sheetWidget.getSelectionRightCol());
            spreadsheet.startDelayedSendingTimer();
        }
    }

    public void moveSelectionRight(boolean discardSelection) {
        final int leftCol = sheetWidget.getSelectionLeftCol();
        final int rightCol = sheetWidget.getSelectionRightCol();
        final int topRow = sheetWidget.getSelectionTopRow();
        final int bottomRow = sheetWidget.getSelectionBottomRow();
        int col = sheetWidget.getSelectedCellColumn();
        int row = sheetWidget.getSelectedCellRow();
        // if the old selected cell was a merged cell, it changes the actual
        // selected cell
        MergedRegion oldRegion = spreadsheet.getMergedRegion(col, row);
        if (oldRegion != null && rowBeforeMergedCell != 0) {
            col = oldRegion.col2;
            row = rowBeforeMergedCell;
        }
        col++;

        while (spreadsheet.hiddenColumnIndexes != null
                && spreadsheet.hiddenColumnIndexes.contains(col)
                && col < spreadsheet.getMaxColumns()) {
            col++;
        }
        if (!discardSelection && (leftCol != rightCol || topRow != bottomRow)
                && (oldRegion == null || leftCol != oldRegion.col1
                        || rightCol != oldRegion.col2
                        || topRow != oldRegion.row1
                        || bottomRow != oldRegion.row2)) {
            // move the selected cell inside the selection
            if (col > rightCol) {
                // move to leftmost and down
                col = leftCol;
                while (spreadsheet.hiddenColumnIndexes != null
                        && spreadsheet.hiddenColumnIndexes
                                .contains(new Integer(col))
                        && col <= rightCol) {
                    col++;
                }
                row++;
                while (spreadsheet.hiddenRowIndexes != null
                        && spreadsheet.hiddenRowIndexes.contains(row)
                        && row <= bottomRow) {
                    row++;
                }
                if (row > bottomRow) {
                    // move to top
                    row = topRow;
                }
                while (spreadsheet.hiddenRowIndexes != null
                        && spreadsheet.hiddenRowIndexes.contains(row)
                        && row <= bottomRow) {
                    row++;
                }
            }
            checkNewSelectionInMergedRegion(col, row);
        } else {
            if (col <= spreadsheet.getMaxColumns()) {
                checkSelectionInMergedRegion(col, row);
            }
        }
    }

    public void moveSelectionLeft(boolean discardSelection) {
        final int leftCol = sheetWidget.getSelectionLeftCol();
        final int rightCol = sheetWidget.getSelectionRightCol();
        final int topRow = sheetWidget.getSelectionTopRow();
        final int bottomRow = sheetWidget.getSelectionBottomRow();
        int col = sheetWidget.getSelectedCellColumn();
        int row = sheetWidget.getSelectedCellRow();
        // if the old selected cell was a merged cell, it changes the actual
        // selected cell
        MergedRegion oldRegion = spreadsheet.getMergedRegion(col, row);
        if (oldRegion != null && rowBeforeMergedCell != 0) {
            col = oldRegion.col1;
            row = rowBeforeMergedCell;
        }

        col--;
        while (spreadsheet.hiddenColumnIndexes != null
                && spreadsheet.hiddenColumnIndexes.contains(col) && col > 0) {
            col--;
        }
        if (!discardSelection && (leftCol != rightCol || topRow != bottomRow)
                && (oldRegion == null || leftCol != oldRegion.col1
                        || rightCol != oldRegion.col2
                        || topRow != oldRegion.row1
                        || bottomRow != oldRegion.row2)) {
            // move the selected cell inside the selection
            if (col < leftCol) {
                // move to right most and up
                col = rightCol;
                while (spreadsheet.hiddenColumnIndexes != null
                        && spreadsheet.hiddenColumnIndexes.contains(col)
                        && col >= leftCol) {
                    col--;
                }
                row--;
                while (spreadsheet.hiddenRowIndexes != null
                        && spreadsheet.hiddenRowIndexes.contains(row)
                        && row >= topRow) {
                    row--;
                }
                if (row < topRow) {
                    // go to bottom
                    row = bottomRow;
                }
                while (spreadsheet.hiddenRowIndexes != null
                        && spreadsheet.hiddenRowIndexes.contains(row)
                        && row >= topRow) {
                    row--;
                }
            }
            // if the new selected cell is a merged cell
            checkNewSelectionInMergedRegion(col, row);
        } else {
            if (col > 0) {
                checkSelectionInMergedRegion(col, row);
            }
        }
    }

    /**
     * Same as {@link #checkSelectionInMergedRegion(int, int)}, but discards old
     * selection in favor of the given cell.
     *
     * @param col
     * @param row
     */
    protected void checkNewSelectionInMergedRegion(int col, int row) {
        MergedRegion region = spreadsheet.getMergedRegion(col, row);
        if (region != null) {
            colBeforeMergedCell = col;
            rowBeforeMergedCell = row;
            col = region.col1;
            row = region.row1;
        } else {
            colBeforeMergedCell = 0;
            rowBeforeMergedCell = 0;
        }

        // TODO check if order really matters here, can we call the normal
        // version and just run the extra methods after?
        sheetWidget.swapSelectedCellInsideSelection(col, row);
        sheetWidget.scrollCellIntoView(col, row);
        spreadsheet.updateSelectedCellValues(col, row);
        newSelectedCellSet();
        spreadsheet.spreadsheetHandler.cellSelected(row, col, false);
        spreadsheet.startDelayedSendingTimer();
    }

    /**
     * Same as {@link #checkNewSelectionInMergedRegion(int, int)}, but retains
     * old selection in addition to the new cell.
     */
    protected void checkSelectionInMergedRegion(int col, int row) {
        MergedRegion region = spreadsheet.getMergedRegion(col, row);
        if (region != null) {
            colBeforeMergedCell = col;
            rowBeforeMergedCell = row;
            col = region.col1;
            row = region.row1;
        } else {
            colBeforeMergedCell = 0;
            rowBeforeMergedCell = 0;
        }
        sheetWidget.scrollCellIntoView(col, row);
        onCellSelectedWithKeyboard(col, row, sheetWidget.getCellValue(col, row),
                region);
    }

    public void clearBeforeMergeCells() {
        colBeforeMergedCell = 0;
        rowBeforeMergedCell = 0;
    }

    public void setColBeforeMergedCell(int c) {
        colBeforeMergedCell = c;
    }

    public void setRowBeforeMergedCell(int r) {
        rowBeforeMergedCell = r;
    }
}
