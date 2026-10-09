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
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.invocation.Invocation;

import com.vaadin.flow.component.spreadsheet.client.CellData;
import com.vaadin.flow.component.spreadsheet.rpc.SpreadsheetClientRpc;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * A cell whose text runs on into its empty neighbours is sent again when how
 * far it can run changes.
 * <p>
 * The width the text runs to is worked out when the cell is sent. It ends at
 * the first cell to the right that holds a value, and it depends on the widths
 * of the columns it crosses, so a neighbour that gets or loses a value, or a
 * column that changes width, leaves the client with a stale width unless the
 * cell is sent again.
 * <p>
 * The fixture: A2 holds 15 characters at 10 pixels each, 150 pixels, in a
 * 100-pixel column. B and C are 50 pixels wide and D is 200, so the text runs
 * across B and C, to 200 pixels in all.
 */
class SpillRefreshTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private SpreadsheetClientRpc rpc;
    private Spreadsheet spreadsheet;

    @BeforeEach
    void setUp() {
        rpc = mock(SpreadsheetClientRpc.class);
        spreadsheet = new Spreadsheet() {
            @Override
            protected SpreadsheetClientRpc getRpcProxy() {
                return rpc;
            }
        };
        spreadsheet.setColumnWidth(0, 100);
        spreadsheet.setColumnWidth(1, 50);
        spreadsheet.setColumnWidth(2, 50);
        spreadsheet.setColumnWidth(3, 200);
        spreadsheet.createCell(1, 0, "fifteen chars!!");
    }

    /**
     * The client shows the top-left corner of the sheet and has measured a
     * character of each style as 10 pixels wide.
     */
    private void showTheSheet() {
        spreadsheet.onSheetScroll(1, 1, 20, 10);
        HashMap<Integer, Float> ratios = new HashMap<>();
        for (int style = 0; style < 8; style++) {
            ratios.put(style, 10f);
        }
        spreadsheet.getCellValueManager().onCellStyleWidthRatioUpdate(ratios);
    }

    private void edit(String address, String value) {
        spreadsheet.setSelection(address);
        TestHelper.fireClientEvent(spreadsheet, "cellValueEdited",
                "{\"row\": " + row(address) + ", \"col\": " + col(address)
                        + ", \"value\": \"" + value + "\"}");
    }

    private static int row(String address) {
        return Integer.parseInt(address.substring(1));
    }

    private static int col(String address) {
        return address.charAt(0) - 'A' + 1;
    }

    @Test
    void fixture_theTextRunsAcrossBAndC() {
        showTheSheet();

        assertEquals(200, sentCell("A2").overflowWidth);
    }

    @Test
    void typeIntoACellTheTextRunsInto_theTextIsSentAgainCutShortThere() {
        showTheSheet();
        clearInvocations(rpc);

        edit("C2", "x");

        CellData a2 = sentCell("A2");
        assertNotNull(a2, "A2 has to be sent again");
        assertEquals(150, a2.overflowWidth, "It now stops at C2");
    }

    @Test
    void clearACellTheTextStoppedAt_theTextIsSentAgainRunningFurther() {
        spreadsheet.createCell(1, 2, "x");
        showTheSheet();
        assertEquals(150, sentCell("A2").overflowWidth);
        clearInvocations(rpc);

        edit("C2", "");

        CellData a2 = sentCell("A2");
        assertNotNull(a2, "A2 has to be sent again");
        assertEquals(200, a2.overflowWidth, "It now runs across C2");
    }

    @Test
    void deleteACellTheTextStoppedAt_theTextIsSentAgainRunningFurther() {
        spreadsheet.createCell(1, 2, "x");
        showTheSheet();
        clearInvocations(rpc);

        spreadsheet.setSelection("C2");
        TestHelper.fireClientEvent(spreadsheet, "deleteSelectedCells", "{}");

        CellData a2 = sentCell("A2");
        assertNotNull(a2, "A2 has to be sent again");
        assertEquals(200, a2.overflowWidth);
    }

    @Test
    void undoTypingIntoACellTheTextRunsInto_theTextIsSentAgainRunningFurther() {
        // Undo sends the cells it changes only once the client has rendered
        // the sheet; until then the whole sheet is sent anyway.
        ui.add(spreadsheet);
        ui.fakeClientCommunication();
        showTheSheet();
        edit("C2", "x");
        clearInvocations(rpc);

        TestHelper.fireClientEvent(spreadsheet, "onUndo", "{}");

        CellData a2 = sentCell("A2");
        assertNotNull(a2, "A2 has to be sent again");
        assertEquals(200, a2.overflowWidth);
    }

    @Test
    void pasteIntoACellTheTextRunsInto_theTextIsSentAgainCutShortThere() {
        showTheSheet();
        clearInvocations(rpc);

        spreadsheet.setSelection("C2");
        TestHelper.fireClientEvent(spreadsheet, "onPaste", "{\"text\": \"x\"}");

        CellData a2 = sentCell("A2");
        assertNotNull(a2, "A2 has to be sent again");
        assertEquals(150, a2.overflowWidth);
    }

    @Test
    void resizeAColumnTheTextRunsInto_theTextIsSentAgain() {
        showTheSheet();
        clearInvocations(rpc);

        // B to 80 pixels: wide enough for the rest of the text on its own.
        TestHelper.fireClientEvent(spreadsheet, "columnResized",
                "{\"colIndexes\": [2], \"newWidths\": [80]}");

        CellData a2 = sentCell("A2");
        assertNotNull(a2, "A2 has to be sent again");
        assertEquals(180, a2.overflowWidth);
    }

    @Test
    void hideAColumnTheTextRunsInto_theTextIsSentAgain() {
        showTheSheet();
        clearInvocations(rpc);

        spreadsheet.setColumnHidden(1, true);

        CellData a2 = sentCell("A2");
        assertNotNull(a2, "A2 has to be sent again");
        // B adds nothing now, so the text runs on into D.
        assertEquals(350, a2.overflowWidth);
    }

    @Test
    void typeNextToAnotherValue_onlyTheNearestCellToTheLeftIsSentAgain() {
        spreadsheet.createCell(1, 1, "fifteen chars!!");
        showTheSheet();
        clearInvocations(rpc);

        edit("D2", "x");

        assertNotNull(sentCell("B2"),
                "B2's text is the one that runs into C2 and D2");
        assertNull(sentCell("A2"), "A2's text stops at B2 either way");
    }

    @Test
    void typeNextToTextThatFitsItsColumn_thatTextIsNotSentAgain() {
        // 40 pixels in a 100-pixel column: it runs nowhere.
        spreadsheet.createCell(2, 0, "fits");
        showTheSheet();
        clearInvocations(rpc);

        edit("B3", "x");

        assertNotNull(sentCell("B3"));
        assertNull(sentCell("A3"));
    }

    @Test
    void typeNextToTextTheClientDoesNotHave_thatTextIsNotSent() {
        // Row 100 is outside the rows the client has been sent.
        spreadsheet.createCell(99, 0, "fifteen chars!!");
        showTheSheet();
        clearInvocations(rpc);

        edit("C100", "x");

        assertNotNull(sentCell("C100"));
        assertNull(sentCell("A100"),
                "The text is built with its current neighbours when the"
                        + " client first asks for it");
    }

    private CellData sentCell(String address) {
        return sentCells().stream()
                .filter(cd -> cd.col == col(address) && cd.row == row(address))
                .reduce((first, second) -> second).orElse(null);
    }

    /** Every cell sent to the client, through whichever call carried it. */
    private List<CellData> sentCells() {
        List<CellData> all = new ArrayList<>();
        for (Invocation invocation : mockingDetails(rpc).getInvocations()) {
            for (Object argument : invocation.getArguments()) {
                if (argument instanceof List<?> list) {
                    for (Object item : list) {
                        if (item instanceof CellData cellData) {
                            all.add(cellData);
                        }
                    }
                }
            }
        }
        return all;
    }
}
