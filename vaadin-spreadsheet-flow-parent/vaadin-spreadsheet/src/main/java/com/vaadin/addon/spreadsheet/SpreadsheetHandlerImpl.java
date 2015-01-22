package com.vaadin.addon.spreadsheet;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.Spreadsheet.ProtectedEditEvent;
import com.vaadin.addon.spreadsheet.client.SpreadsheetServerRpc;

/**
 * Implementation of the Spreadsheet Server RPC interface.
 */
@SuppressWarnings("serial")
public class SpreadsheetHandlerImpl implements SpreadsheetServerRpc {

    private Spreadsheet spreadsheet;

    public SpreadsheetHandlerImpl(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    @Override
    public void onSheetScroll(int firstRow, int firstColumn, int lastRow,
            int lastColumn) {
        spreadsheet.onSheetScroll(firstRow, firstColumn, lastRow, lastColumn);
    }

    @Override
    public void cellSelected(int row, int column,
            boolean discardOldRangeSelection) {
        spreadsheet.getCellSelectionManager().onCellSelected(row, column,
                discardOldRangeSelection);
    }

    @Override
    public void sheetAddressChanged(String value) {
        spreadsheet.getCellSelectionManager().onSheetAddressChanged(value);
    }

    @Override
    public void cellRangeSelected(int row1, int col1, int row2, int col2) {
        spreadsheet.getCellSelectionManager().onCellRangeSelected(row1, col1,
                row2, col2);
    }

    /* */
    @Override
    public void cellRangePainted(int selectedCellRow, int selectedCellColumn,
            int row1, int col1, int row2, int col2) {
        spreadsheet.getCellSelectionManager().onCellRangePainted(
                selectedCellRow, selectedCellColumn, row1, col1, row2, col2);
    }

    @Override
    public void cellAddedToSelectionAndSelected(int row, int column) {
        spreadsheet.getCellSelectionManager().onCellAddToSelectionAndSelected(
                row, column);
    }

    @Override
    public void cellsAddedToRangeSelection(int row1, int col1, int row2,
            int col2) {
        spreadsheet.getCellSelectionManager().onCellsAddedToRangeSelection(
                row1, col1, row2, col2);
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
        spreadsheet.getCellSelectionManager().onColumnSelected(firstRowIndex,
                col);
    }

    @Override
    public void columnAddedToSelection(int firstRowIndex, int column) {
        spreadsheet.getCellSelectionManager().onColumnAddedToSelection(firstRowIndex,
                column);
    }

    /* the actual selected cell hasn't changed */
    @Override
    public void selectionIncreasePainted(int r1, int c1, int r2, int c2) {
        spreadsheet.getCellShifter().onSelectionIncreasePainted(r1, c1, r2, c2);
    }

    /* the actual selected cell hasn't changed */
    @Override
    public void selectionDecreasePainted(int r, int c) {
        spreadsheet.getCellShifter().onSelectionDecreasePainted(r, c);
    }

    @Override
    public void cellValueEdited(int row, int col, String value) {
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
    public void linkCellClicked(int row, int column) {
        spreadsheet.onLinkCellClick(row, column);
    }

    @Override
    public void contextMenuOpenOnSelection(int row, int column) {
        spreadsheet.getContextMenuManager().onContextMenuOpenOnSelection(
                row, column);
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
    public void rowsResized(Map<Integer, Float> newRowSizes, int row1,
            int col1, int row2, int col2) {
        spreadsheet.onRowResized(newRowSizes, row1, col1, row2, col2);
    }

    @Override
    public void columnResized(Map<Integer, Integer> newColumnSizes, int row1,
            int col1, int row2, int col2) {
        spreadsheet.onColumnResized(newColumnSizes, row1, col1, row2, col2);
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
        spreadsheet.fireEvent(new ProtectedEditEvent(spreadsheet));
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

                // check for numbers
                Double numVal = checkForNumber(cellContent);
                if (numVal != null) {
                    cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                    cell.setCellValue(numVal);
                } else {
                    cell.setCellValue(cellContent);
                }
                colIndex++;
            }
            rowIndex++;
        }

        // remove last incrementation so that selection goes correctly
        rowIndex--;
        colIndex--;

        // might have impacted formulas outside of selection area, so just
        // reload all values
        spreadsheet.refreshAllCellValues();

        // re-set selection to copied area
        spreadsheet.setSelectionRange(selectedCellReference.getRow(), selectedCellReference.getCol(),
                rowIndex, colIndex);
    }

    protected Double checkForNumber(String cellContent) {

        if (cellContent == null) {
            return null;
        }

        try {

            String trimmedContent = cellContent.replaceAll(" ", "");

            Locale locale = spreadsheet.getLocale();
            if (locale != null) {
                DecimalFormat format = (DecimalFormat) DecimalFormat
                        .getInstance(locale);
                DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();

                char sep = symbols.getDecimalSeparator();
                if (sep != '.') {
                    trimmedContent = trimmedContent.replace(sep + "", ".");
                }

                sep = symbols.getGroupingSeparator();
                if (sep == '.') {
                    trimmedContent = trimmedContent.replace(sep + "\\.", "");
                } else {
                    trimmedContent = trimmedContent.replace(sep + "", "");
                }
            } else {
                // simple check
                trimmedContent = trimmedContent.replace(",", ".");
            }
            Double d = Double.parseDouble(trimmedContent);
            return d;

        } catch (NumberFormatException e) {
            // is OK
        }
        return null;
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
     *            input
     * @return output string parts split at tabs
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

        spreadsheet.refreshAllCellValues();
    }

}
