/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.WorkbookUtil;

import com.vaadin.flow.component.spreadsheet.Spreadsheet.CellValueChangeEvent;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.ProtectedEditEvent;
import com.vaadin.flow.component.spreadsheet.command.CellValueCommand;
import com.vaadin.flow.component.spreadsheet.rpc.SpreadsheetServerRpc;

/**
 * Default handler for the messages the client sends to the server. Implements
 * {@link SpreadsheetServerRpc}: each method corresponds to one user action
 * (cell selection, formula edit, context menu, sheet scroll, etc.).
 *
 * <p>
 * The client sends one {@code @DomEvent("spreadsheet-event")} per action,
 * carrying a {@code type} string and a {@code JsonNode} payload, and
 * {@code SpreadsheetEventListener} calls the matching method on this class.
 *
 * <p>
 * Subclass to customize how client events are handled, and pass the subclass
 * instance to {@code Spreadsheet.setSpreadsheetHandler(...)} to replace the
 * default.
 *
 * @since 23.1
 */
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

    /*
     * The client always sends the text fields of its events. One that is
     * missing reaches a handler as null, and the event then does nothing rather
     * than fail the request.
     */

    @Override
    public void sheetAddressChanged(String value) {
        if (value == null) {
            return;
        }
        spreadsheet.getCellSelectionManager().onSheetAddressChanged(value,
                false);
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
        spreadsheet.getCellSelectionManager()
                .onCellAddToSelectionAndSelected(row, column);
    }

    @Override
    public void cellsAddedToRangeSelection(int row1, int col1, int row2,
            int col2) {
        spreadsheet.getCellSelectionManager().onCellsAddedToRangeSelection(row1,
                col1, row2, col2);
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
        spreadsheet.getCellSelectionManager()
                .onColumnAddedToSelection(firstRowIndex, column);
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
        if (value == null) {
            return;
        }
        if (spreadsheet.isCellLocked(new CellAddress(row - 1, col - 1))) {
            protectedCellWriteAttempted();
            return;
        }
        spreadsheet.getCellValueManager().onCellValueChange(col, row, value);
    }

    @Override
    public void sheetSelected(int tabIndex, int scrollLeft, int scrollTop) {
        spreadsheet.onSheetSelected(tabIndex, scrollLeft, scrollTop);
    }

    @Override
    public void sheetRenamed(int sheetIndex, String sheetName) {
        // The client offers no renaming in a protected workbook, and checks
        // a name as POI does; a request that still gets here is left alone.
        if (spreadsheet.isWorkbookStructureLocked()
                || !isNewNameFor(sheetIndex, sheetName)) {
            return;
        }
        spreadsheet.onSheetRename(sheetIndex, sheetName);
    }

    /**
     * Whether POI takes this name for the visible sheet at the given index: a
     * valid name no other sheet has, whatever its case.
     */
    private boolean isNewNameFor(int sheetIndex, String sheetName) {
        if (sheetName == null || sheetIndex < 0
                || sheetIndex >= spreadsheet.getNumberOfVisibleSheets()) {
            return false;
        }
        try {
            WorkbookUtil.validateSheetName(sheetName);
        } catch (IllegalArgumentException e) {
            return false;
        }
        int existing = spreadsheet.getWorkbook().getSheetIndex(sheetName);
        return existing < 0
                || existing == spreadsheet.getVisibleSheetPOIIndex(sheetIndex);
    }

    @Override
    public void sheetCreated(int scrollLeft, int scrollTop) {
        if (spreadsheet.isWorkbookStructureLocked()) {
            return;
        }
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
        spreadsheet.getContextMenuManager().onContextMenuOpenOnSelection(row,
                column);
    }

    @Override
    public void rowHeaderContextMenuOpen(int rowIndex) {
        spreadsheet.getContextMenuManager()
                .onRowHeaderContextMenuOpen(rowIndex);
    }

    @Override
    public void columnHeaderContextMenuOpen(int columnIndex) {
        spreadsheet.getContextMenuManager()
                .onColumnHeaderContextMenuOpen(columnIndex);
    }

    @Override
    public void actionOnCurrentSelection(String actionKey) {
        if (actionKey == null) {
            return;
        }
        spreadsheet.getContextMenuManager()
                .onActionOnCurrentSelection(actionKey);
    }

    @Override
    public void actionOnRowHeader(String actionKey) {
        if (actionKey == null) {
            return;
        }
        spreadsheet.getContextMenuManager().onActionOnRowHeader(actionKey);
    }

    @Override
    public void actionOnColumnHeader(String actionKey) {
        if (actionKey == null) {
            return;
        }
        spreadsheet.getContextMenuManager().onActionOnColumnHeader(actionKey);
    }

    @Override
    public void contextMenuClosed() {
        spreadsheet.getContextMenuManager().onContextMenuClosed();
    }

    @Override
    public void rowsResized(Map<Integer, Float> newRowSizes, int row1, int col1,
            int row2, int col2) {
        if (!spreadsheet.mayResizeFromClient(false)) {
            return;
        }
        spreadsheet.onRowResized(newRowSizes);
    }

    @Override
    public void columnResized(Map<Integer, Integer> newColumnSizes, int row1,
            int col1, int row2, int col2) {
        if (!spreadsheet.mayResizeFromClient(true)) {
            return;
        }
        spreadsheet.onColumnResized(newColumnSizes);
    }

    @Override
    public void onRowAutofit(int rowIndex) {
        if (!spreadsheet.mayResizeFromClient(false)) {
            return;
        }
        spreadsheet.onRowHeaderDoubleClick(rowIndex - 1);
    }

    @Override
    public void onColumnAutofit(int columnIndex) {
        if (!spreadsheet.mayResizeFromClient(true)) {
            return;
        }
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
        spreadsheet.getCellValueManager()
                .onCellStyleWidthRatioUpdate(cellStyleWidthRatioMap);
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
        if (text == null) {
            return;
        }
        Workbook workbook = spreadsheet.getWorkbook();
        Sheet activesheet = workbook.getSheetAt(workbook.getActiveSheetIndex());

        CellReference selectedCellReference = spreadsheet
                .getSelectedCellReference();

        List<List<String>> lines = ClipboardText.parse(text);
        if (lines.isEmpty()) {
            return;
        }

        int pasteHeight = lines.size();
        int pasteWidth = 1;
        for (List<String> line : lines) {
            pasteWidth = Math.max(pasteWidth, line.size());
        }

        int rowIndex = selectedCellReference.getRow();
        int colIndex = selectedCellReference.getCol();

        // Check for protected cells at target
        for (int i = 0; i < pasteHeight; i++) {
            for (int j = 0; j < pasteWidth; j++) {
                CellAddress cellAddress = new CellAddress(rowIndex + i,
                        colIndex + j);
                if (spreadsheet.isCellLocked(cellAddress)) {
                    protectedCellWriteAttempted();
                    return;
                }
            }
        }

        CellValueCommand command = new CellValueCommand(spreadsheet);
        CellRangeAddress affectedRange = new CellRangeAddress(rowIndex,
                rowIndex + pasteHeight - 1, colIndex,
                colIndex + pasteWidth - 1);
        command.captureCellRangeValues(affectedRange);

        for (int i = 0; i < pasteHeight; i++) {
            List<String> tokens = lines.get(i);
            Row row = activesheet.getRow(rowIndex + i);
            if (row == null) {
                row = activesheet.createRow(rowIndex + i);
            }
            for (int j = 0; j < pasteWidth; j++) {
                Cell cell = row.getCell(colIndex + j);
                if (cell == null) {
                    cell = row.createCell(colIndex + j);
                }
                if (j < tokens.size()) {
                    String cellContent = tokens.get(j);
                    Double numVal = SpreadsheetUtil.parseNumber(cell,
                            cellContent, spreadsheet.getLocale());
                    if (numVal != null) {
                        cell.setCellValue(numVal);
                    } else {
                        cell.setCellValue(cellContent);
                    }
                } else {
                    cell.setBlank();
                    spreadsheet.markCellAsDeleted(cell, true);
                }

                spreadsheet.getCellValueManager().markCellForUpdate(cell);
                spreadsheet.getCellValueManager().getFormulaEvaluator()
                        .notifyUpdateCell(cell);
            }
        }

        spreadsheet.getSpreadsheetHistoryManager().addCommand(command);
        spreadsheet.updateMarkedCells();
        // re-set selection to copied area
        spreadsheet.setSelectionRange(rowIndex, colIndex,
                rowIndex + pasteHeight - 1, colIndex + pasteWidth - 1);

        fireCellValueChangeEvent(affectedRange);
    }

    private void fireCellValueChangeEvent(CellRangeAddress region) {
        Set<CellReference> cells = new HashSet<CellReference>();
        for (int x = region.getFirstColumn(); x <= region
                .getLastColumn(); x++) {
            for (int y = region.getFirstRow(); y <= region.getLastRow(); y++) {
                cells.add(new CellReference(
                        spreadsheet.getActiveSheet().getSheetName(), y, x,
                        false, false));
            }
        }
        fireCellValueChangeEvent(cells);
    }

    private void fireCellValueChangeEvent(Set<CellReference> cells) {
        spreadsheet.fireEvent(new CellValueChangeEvent(spreadsheet, cells));
    }

    @Override
    public void clearSelectedCellsOnCut() {
        // clear ranges
        List<Cell> targetCells = new ArrayList<Cell>();
        List<CellRangeAddress> cellRangeAddresses = spreadsheet
                .getCellSelectionManager().getCellRangeAddresses();
        for (CellRangeAddress a : cellRangeAddresses) {
            for (int row = a.getFirstRow(); row <= a.getLastRow(); row++) {
                for (int col = a.getFirstColumn(); col <= a
                        .getLastColumn(); col++) {
                    Cell cell = spreadsheet.getCell(row, col);
                    if (cell != null) {
                        if (spreadsheet.isCellLocked(cell.getAddress())) {
                            protectedCellWriteAttempted();
                            return;
                        }
                        targetCells.add(cell);
                    }
                }
            }
        }

        // clear single cell
        CellReference reference = spreadsheet.getCellSelectionManager()
                .getSelectedCellReference();
        Cell cell = spreadsheet.getCell(reference.getRow(), reference.getCol());
        if (cell != null) {
            if (spreadsheet.isCellLocked(cell.getAddress())) {
                protectedCellWriteAttempted();
                return;
            }
            targetCells.add(cell);
        }
        CellValueCommand command = new CellValueCommand(spreadsheet);
        if (reference != null) {
            command.captureCellValues(reference);
        }
        for (CellRangeAddress range : cellRangeAddresses) {
            command.captureCellRangeValues(range);
        }
        spreadsheet.getSpreadsheetHistoryManager().addCommand(command);

        for (Cell targetCell : targetCells) {
            targetCell.setBlank();
            spreadsheet.markCellAsDeleted(targetCell, true);
        }

        fireCellValueChangeEvent(spreadsheet.getSelectedCellReferences());
        spreadsheet.refreshAllCellValues();
    }

    @Override
    public void updateCellComment(String text, int col, int row) {
        if (text == null) {
            return;
        }
        if (spreadsheet.isCellLocked(new CellAddress(row - 1, col - 1))) {
            protectedCellWriteAttempted();
            return;
        }
        CreationHelper factory = spreadsheet.getWorkbook().getCreationHelper();
        RichTextString str = factory.createRichTextString(text);
        Cell cell = getOrCreateCell(spreadsheet.getActiveSheet(), row - 1,
                col - 1); // poi is 0 based, but spreadsheet is 1 based
        Comment comment = cell.getCellComment();
        if (comment == null) {
            Drawing<?> drawingPatriarch = spreadsheet.getActiveSheet()
                    .createDrawingPatriarch();
            ClientAnchor anchor = factory.createClientAnchor();
            anchor.setCol1(cell.getColumnIndex());
            anchor.setCol2(cell.getColumnIndex());
            anchor.setRow1(cell.getRowIndex());
            anchor.setRow2(cell.getRowIndex());
            comment = drawingPatriarch.createCellComment(anchor);
            cell.setCellComment(comment);
        }
        comment.setString(str);
    }

    public Cell getOrCreateCell(Sheet sheet, int rowIdx, int colIdx) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
            row = sheet.createRow(rowIdx);
        }

        Cell cell = row.getCell(colIdx);
        if (cell == null) {
            cell = row.createCell(colIdx);
        }

        return cell;
    }

    @Override
    public void setGroupingCollapsed(boolean isCols, int colIndex,
            boolean collapsed) {
        spreadsheet.setGroupingCollapsed(isCols, colIndex, collapsed);
    }

    @Override
    public void levelHeaderClicked(boolean isCols, int level) {
        spreadsheet.levelHeaderClicked(isCols, level);
    }

    public void onPopupButtonClick(int row, int column) {
        spreadsheet.onPopupButtonClick(row, column);
    }

    public void onPopupClose(int row, int column) {
        spreadsheet.onPopupClose(row, column);
    }
}
