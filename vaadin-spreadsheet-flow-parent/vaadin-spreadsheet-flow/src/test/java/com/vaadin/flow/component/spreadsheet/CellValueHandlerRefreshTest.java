/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.vaadin.flow.component.spreadsheet.Spreadsheet.CellValueHandler;
import com.vaadin.flow.component.spreadsheet.client.CellData;
import com.vaadin.flow.component.spreadsheet.rpc.SpreadsheetClientRpc;

/**
 * Covers what the client is told after a {@link CellValueHandler} has taken
 * over an edit.
 *
 * <p>
 * The client puts the text the user typed into the cell as soon as the editor
 * closes. A handler that returns {@code false} is free to store something else,
 * or nothing at all, so that text is never the authoritative value. The server
 * has to send the cell back in every one of those cases, otherwise the sheet
 * keeps showing raw input that matches nothing in the workbook.
 */
class CellValueHandlerRefreshTest {

    private SpreadsheetClientRpc rpc;
    private Spreadsheet spreadsheet;
    private Sheet sheet;

    @BeforeEach
    void setUp() {
        rpc = mock(SpreadsheetClientRpc.class);
        spreadsheet = new Spreadsheet() {
            @Override
            protected SpreadsheetClientRpc getRpcProxy() {
                return rpc;
            }
        };
        sheet = spreadsheet.getActiveSheet();
    }

    @Test
    void handlerRewritesValue_cellIsSentWithTheHandlersValue() {
        spreadsheet.createCell(0, 0, "original");
        spreadsheet.setCellValueHandler(handler((cell, value) -> {
            cell.setCellValue("rewritten by handler");
            return false;
        }));

        edit(1, 1, "typed");

        CellData sent = sentCell(1, 1);
        assertNotNull(sent,
                "A cell a handler rewrote has to be sent to the client");
        assertEquals("rewritten by handler", sent.value);
    }

    @Test
    void handlerIgnoresEdit_cellIsStillSentSoTheTypedTextIsReverted() {
        spreadsheet.createCell(0, 0, "untouched");
        spreadsheet.setCellValueHandler(handler((cell, value) -> false));

        edit(1, 1, "typed");

        CellData sent = sentCell(1, 1);
        assertNotNull(sent,
                "A handler that stores nothing still leaves the typed text on"
                        + " the client, so the cell has to be sent back");
        assertEquals("untouched", sent.value);
    }

    @Test
    void handlerCreatesTheMissingCell_newCellIsSent() {
        spreadsheet.setCellValueHandler(handler((cell, value) -> {
            assertNull(cell, "The cell does not exist yet");
            spreadsheet.createCell(0, 0, "created by handler");
            return false;
        }));

        edit(1, 1, "typed");

        CellData sent = sentCell(1, 1);
        assertNotNull(sent, "A cell the handler created has to be sent");
        assertEquals("created by handler", sent.value);
    }

    @Test
    void handlerLeavesTheCellMissing_removalIsSent() {
        spreadsheet.setCellValueHandler(handler((cell, value) -> false));

        edit(1, 1, "typed");

        CellData sent = sentCell(1, 1);
        assertNotNull(sent,
                "The client is showing the typed text in a cell that does not"
                        + " exist, so a removal has to be sent");
        assertNull(sent.value, "A removal carries no value");
    }

    @Test
    void handlerAsksForDefaultParsing_valueIsParsedAndSentAsBefore() {
        spreadsheet.createCell(0, 0, "original");
        spreadsheet.setCellValueHandler(handler((cell, value) -> true));

        edit(1, 1, "42");

        assertEquals("42", sentCell(1, 1).value);
        assertEquals(42d, sheet.getRow(0).getCell(0).getNumericCellValue());
    }

    @Test
    void noHandler_cellIsSentAsBefore() {
        spreadsheet.createCell(0, 0, "original");

        edit(1, 1, "typed");

        assertEquals("typed", sentCell(1, 1).value);
    }

    @Test
    void handlerTakesOverAnEditOnAnotherCell_onlyTheEditedCellIsSent() {
        spreadsheet.createCell(0, 0, "A1");
        spreadsheet.createCell(0, 1, "B1");
        spreadsheet.setCellValueHandler(handler((cell, value) -> {
            cell.setCellValue("handled");
            return false;
        }));

        edit(2, 1, "typed");

        assertEquals("handled", sentCell(2, 1).value);
        assertNull(sentCell(1, 1),
                "An untouched neighbour must not be re-sent");
    }

    @Test
    void handlerTakesOver_theEditIsStillUndoable() {
        spreadsheet.createCell(0, 0, "original");
        spreadsheet.setCellValueHandler(handler((cell, value) -> {
            cell.setCellValue("handled");
            return false;
        }));

        edit(1, 1, "typed");
        assertTrue(spreadsheet.getSpreadsheetHistoryManager().canUndo());

        spreadsheet.getSpreadsheetHistoryManager().undo();

        assertEquals("original",
                sheet.getRow(0).getCell(0).getStringCellValue());
    }

    @Test
    void handlerTakesOverButNothingChanges_noStyleRefreshWithoutACell() {
        // Nothing exists and nothing is created, so the only thing the client
        // hears about is the removal - it must not be a silent no-op.
        spreadsheet.setCellValueHandler(handler((cell, value) -> false));

        edit(5, 7, "typed");

        verify(rpc, never()).cellsUpdated(new ArrayList<>());
        CellData sent = sentCell(5, 7);
        assertNotNull(sent);
        assertEquals(5, sent.col);
        assertEquals(7, sent.row);
    }

    /**
     * Commits an edit the way the client does: the cell is selected, then its
     * new text arrives.
     *
     * @param col
     *            column index, 1-based
     * @param row
     *            row index, 1-based
     */
    private void edit(int col, int row, String value) {
        // Building the fixture marks cells too. Flushing first keeps the
        // assertions looking only at what this edit sent.
        spreadsheet.updateMarkedCells();
        clearInvocations(rpc);
        spreadsheet.setSelection(SpreadsheetUtil.getColHeader(col) + row);
        spreadsheet.getCellValueManager().onCellValueChange(col, row, value);
    }

    /**
     * The one cell the client was told about at the given 1-based position, or
     * {@code null} when it was never sent.
     */
    private CellData sentCell(int col, int row) {
        return sentCells().stream().filter(cd -> cd.col == col && cd.row == row)
                .findFirst().orElse(null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List<CellData> sentCells() {
        ArgumentCaptor<ArrayList> captor = ArgumentCaptor
                .forClass(ArrayList.class);
        verify(rpc, atLeastOnce()).cellsUpdated(captor.capture());
        List<CellData> all = new ArrayList<>();
        for (Object batch : captor.getAllValues()) {
            for (Object cd : (ArrayList) batch) {
                if (Objects.nonNull(cd)) {
                    all.add((CellData) cd);
                }
            }
        }
        return all;
    }

    private CellValueHandler handler(HandlerBody body) {
        return new CellValueHandler() {
            @Override
            public boolean cellValueUpdated(Cell cell, Sheet sheet,
                    int colIndex, int rowIndex, String newValue,
                    FormulaEvaluator formulaEvaluator, DataFormatter formatter,
                    ConditionalFormattingEvaluator conditionalFormattingEvaluator) {
                return body.handle(cell, newValue);
            }
        };
    }

    @FunctionalInterface
    private interface HandlerBody {
        boolean handle(Cell cell, String newValue);
    }
}
