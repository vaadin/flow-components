package com.vaadin.addon.spreadsheet;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.Spreadsheet.ProtectedCellWriteAttemptedEvent;
import com.vaadin.addon.spreadsheet.client.SpreadsheetServerRpc;

public class SpreadsheetHandlerImpl implements SpreadsheetServerRpc {

    private Spreadsheet spreadsheet;

    public SpreadsheetHandlerImpl(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    @Override
    public void onSheetScroll(int firstRow, int lastRow, int firstColumn,
            int lastColumn) {
        spreadsheet.onSheetScroll(firstRow, lastRow, firstColumn, lastColumn);
    }

    @Override
    public void cellSelected(int column, int row,
            boolean discardOldRangeSelection) {
        spreadsheet.getCellSelectionManager().onCellSelected(column, row,
                discardOldRangeSelection);
    }

    @Override
    public void sheetAddressChanged(String value) {
        spreadsheet.getCellSelectionManager().onSheetAddressChanged(value);
    }

    @Override
    public void cellRangeSelected(int col1, int col2, int row1, int row2) {
        spreadsheet.getCellSelectionManager().onCellRangeSelected(col1, col2,
                row1, row2);
    }

    /* */
    @Override
    public void cellRangePainted(int selectedCellColumn, int selectedCellRow,
            int col1, int col2, int row1, int row2) {
        spreadsheet.getCellSelectionManager().onCellRangePainted(
                selectedCellColumn, selectedCellRow, col1, col2, row1, row2);
    }

    @Override
    public void cellAddedToSelectionAndSelected(int column, int row) {
        spreadsheet.getCellSelectionManager().onCellAddToSelectionAndSelected(
                column, row);
    }

    @Override
    public void cellsAddedToRangeSelection(int col1, int col2, int row1,
            int row2) {
        spreadsheet.getCellSelectionManager().onCellsAddedToRangeSelection(
                col1, col2, row1, row2);
    }

    @Override
    public void rowSelected(int row, int firstColumnIndex) {
        spreadsheet.getCellSelectionManager().onRowSelected(row,
                firstColumnIndex);
    }

    @Override
    public void rowAddedToRangeSelection(int row, int firstColumnIndex) {
        spreadsheet.getCellSelectionManager().onRowAddedToRangeSelection(row,
                firstColumnIndex);
    }

    @Override
    public void columnSelected(int col, int firstRowIndex) {
        spreadsheet.getCellSelectionManager().onColumnSelected(col,
                firstRowIndex);
    }

    @Override
    public void columnAddedToSelection(int column, int firstRowIndex) {
        spreadsheet.getCellSelectionManager().onColumnAddedToSelection(column,
                firstRowIndex);
    }

    /* the actual selected cell hasn't changed */
    @Override
    public void selectionIncreasePainted(int c1, int c2, int r1, int r2) {
        spreadsheet.getCellShifter().onSelectionIncreasePainted(c1, c2, r1, r2);
    }

    /* the actual selected cell hasn't changed */
    @Override
    public void selectionDecreasePainted(int c, int r) {
        spreadsheet.getCellShifter().onSelectionDecreasePainted(c, r);
    }

    @Override
    public void cellValueEdited(int col, int row, String value) {
        spreadsheet.getCellValueManager().onCellValueChange(col, row, value);
    }

    @Override
    public void sheetSelected(int tabIndex, int scrollLeft, int scrollTop) {
        spreadsheet.onSheetSelected(tabIndex, scrollLeft, scrollTop);
    }

    @Override
    public void sheetRenamed(int sheetIndex, String sheetName) {
        spreadsheet.onSheetRename(sheetIndex, sheetName);
    }

    @Override
    public void sheetCreated(int scrollLeft, int scrollTop) {
        spreadsheet.onNewSheetCreated(scrollLeft, scrollTop);
    }

    @Override
    public void deleteSelectedCells() {
        spreadsheet.getCellValueManager().onDeleteSelectedCells();
    }

    @Override
    public void linkCellClicked(int column, int row) {
        spreadsheet.onLinkCellClick(column, row);
    }

    @Override
    public void contextMenuOpenOnSelection(int column, int row) {
        spreadsheet.getContextMenuManager().onContextMenuOpenOnSelection(
                column, row);
    }

    @Override
    public void rowHeaderContextMenuOpen(int rowIndex) {
        spreadsheet.getContextMenuManager()
                .onRowHeaderContextMenuOpen(rowIndex);
    }

    @Override
    public void columnHeaderContextMenuOpen(int columnIndex) {
        spreadsheet.getContextMenuManager().onColumnHeaderContextMenuOpen(
                columnIndex);
    }

    @Override
    public void actionOnCurrentSelection(String actionKey) {
        spreadsheet.getContextMenuManager().onActionOnCurrentSelection(
                actionKey);
    }

    @Override
    public void actionOnRowHeader(String actionKey) {
        spreadsheet.getContextMenuManager().onActionOnRowHeader(actionKey);
    }

    @Override
    public void actionOnColumnHeader(String actionKey) {
        spreadsheet.getContextMenuManager().onActionOnColumnHeader(actionKey);
    }

    @Override
    public void rowsResized(Map<Integer, Float> newRowSizes, int col1,
            int col2, int row1, int row2) {
        spreadsheet.onRowResized(newRowSizes, col1, col2, row1, row2);
    }

    @Override
    public void columnResized(Map<Integer, Integer> newColumnSizes, int col1,
            int col2, int row1, int row2) {
        spreadsheet.onColumnResized(newColumnSizes, col1, col2, row1, row2);
    }

    @Override
    public void onColumnAutofit(int columnIndex) {
        spreadsheet.onColumnAutofit(columnIndex - 1);
    }

    @Override
    public void onUndo() {
        spreadsheet.getSpreadsheetHistoryManager().undo();
    }

    @Override
    public void onRedo() {
        spreadsheet.getSpreadsheetHistoryManager().redo();
    }

    @Override
    public void setCellStyleWidthRatios(
            HashMap<Integer, Float> cellStyleWidthRatioMap) {
        spreadsheet.getCellValueManager().onCellStyleWidthRatioUpdate(
                cellStyleWidthRatioMap);
    }

    @Override
    public void onConnectorInit() {
        spreadsheet.onConnectorInit();
    }

    @Override
    public void protectedCellWriteAttempted() {
        spreadsheet
                .fireEvent(new ProtectedCellWriteAttemptedEvent(spreadsheet));
    }

    @Override
    public void onPaste(String text) {
        Workbook workbook = spreadsheet.getWorkbook();
        Sheet activesheet = workbook.getSheetAt(workbook.getActiveSheetIndex());

        CellReference selectedCellReference = spreadsheet
                .getSelectedCellReference();

        String[] lines;
        if (text.indexOf("\r\n") > -1) {
            lines = text.split("\r\n");
        } else if (text.indexOf("\n") > -1) {
            lines = text.split("\n");
        } else {
            lines = text.split("\r");
        }

        int rowIndex = selectedCellReference.getRow();
        int colIndex = -1;
        for (String line : lines) {

            Row row = activesheet.getRow(rowIndex);
            if (row == null) {
                row = activesheet.createRow(rowIndex);
            }

            colIndex = selectedCellReference.getCol();

            for (String cellContent : splitOnTab(line)) {

                Cell cell = row.getCell(colIndex);
                if (cell == null) {
                    cell = row.createCell(colIndex);
                }
                cell.setCellValue(cellContent);

                colIndex++;
            }
            rowIndex++;
        }

        // remove last incrementation so that selection goes correctly
        rowIndex--;
        colIndex--;

        // might have impacted formulas outside of selection area, so just
        // reload all values
        spreadsheet.updatedAndRecalculateAllCellValues();

        // re-set selection to copied area
        spreadsheet.setSelectionRange(selectedCellReference.getRow(), rowIndex,
                selectedCellReference.getCol(), colIndex);
    }

    /**
     * Splits tab-delimited string into an array of Strings, inserting empty
     * strings between any tab characters and the beginning and end of the line
     * if it would be a tab. Length of array will equal number of tabs + 1.
     * <p>
     * E.g.<br/>
     * "1\t2" - {"1","2"}<br/>
     * "\t\t" - {"","",""}<br/>
     * 
     * @param line
     * @return
     */
    private static String[] splitOnTab(String line) {

        List<String> list = new LinkedList<String>();
        StringTokenizer tokenizer = new StringTokenizer(line, "\t", true);

        // marker for when last token is a tab, meaning we need one
        // additional empty string
        boolean lastCharWasTab = false;

        while (tokenizer.hasMoreTokens()) {

            String content = tokenizer.nextToken();

            if (content.equals("\t")) {
                // empty content; insert empty string here
                content = "";

                if (!tokenizer.hasMoreTokens()) {
                    lastCharWasTab = true;
                }

            } else {
                // normal cell content, value in 'content'

                // skip to next content by skipping tab token
                // (we process 'content\t' on the same loop)
                if (tokenizer.countTokens() > 1) {
                    tokenizer.nextToken();
                } else if (tokenizer.countTokens() == 1) {
                    // if the tab is the last char we need to mark it
                    tokenizer.nextToken();
                    lastCharWasTab = true;
                }
            }

            list.add(content);

            if (lastCharWasTab) {
                list.add("");
            }
        }

        return list.toArray(new String[list.size()]);
    }

    @Override
    public void clearSelectedCellsOnCut() {
        // clear ranges
        List<CellRangeAddress> cellRangeAddresses = spreadsheet
                .getCellSelectionManager().getCellRangeAddresses();
        for (CellRangeAddress a : cellRangeAddresses) {
            for (int row = a.getFirstRow(); row <= a.getLastRow(); row++) {
                for (int col = a.getFirstColumn(); col <= a.getLastColumn(); col++) {
                    Cell cell = spreadsheet.getCell(row, col);
                    if (cell != null) {
                        cell.setCellType(Cell.CELL_TYPE_BLANK);
                        spreadsheet.markCellAsDeleted(cell, true);
                    }
                }
            }
        }

        // clear single cell
        CellReference reference = spreadsheet.getCellSelectionManager()
                .getSelectedCellReference();
        Cell cell = spreadsheet.getCell(reference.getRow(), reference.getCol());
        if (cell != null) {
            cell.setCellType(Cell.CELL_TYPE_BLANK);
            spreadsheet.markCellAsDeleted(cell, true);
        }

        spreadsheet.updatedAndRecalculateAllCellValues();
    }

}
