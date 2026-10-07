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
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.Invocation;

import com.vaadin.flow.component.spreadsheet.client.CellData;
import com.vaadin.flow.component.spreadsheet.rpc.SpreadsheetClientRpc;

/**
 * Cells are rebuilt once the client reports how wide a character is.
 *
 * <p>
 * Whether a value fits its column - and so whether it is shortened, spills into
 * its neighbours or shows hashes - is judged against per-style width ratios
 * only the client can measure, and it can only measure them once the styles
 * have arrived. That is after the first batch of cells went out, so those cells
 * were judged against nothing: a freshly loaded workbook never spilled its
 * text, though a value typed later did.
 */
class WidthRatioRefreshTest {

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
        spreadsheet.createCell(0, 0, "a value far too wide for its column");
        // The client has shown the top-left corner of the sheet.
        spreadsheet.onSheetScroll(1, 1, 20, 10);
        clearInvocations(rpc);
    }

    private static HashMap<Integer, Float> ratios(float perCharacter) {
        HashMap<Integer, Float> map = new HashMap<>();
        for (int style = 0; style < 8; style++) {
            map.put(style, perCharacter);
        }
        return map;
    }

    @Test
    void ratiosArrive_cellsAreSentAgainWithTheirSpill() {
        spreadsheet.getCellValueManager()
                .onCellStyleWidthRatioUpdate(ratios(10f));

        CellData a1 = sentCell(1, 1);
        assertNotNull(a1, "The cell has to be sent again once it can be"
                + " judged against real widths");
        assertTrue(a1.overflowWidth > 0,
                "and now it knows it spills, was " + a1.overflowWidth);
    }

    @Test
    void ratiosArrive_onlyTheCellsInViewAreSentAgain() {
        // On the sheet, but outside the rows and columns the client has
        // shown.
        spreadsheet.createCell(99, 29, "far away");
        assertTrue(
                spreadsheet.getRows() >= 100 && spreadsheet.getColumns() >= 30,
                "The cell has to be on the sheet for this to tell anything");
        clearInvocations(rpc);

        spreadsheet.getCellValueManager()
                .onCellStyleWidthRatioUpdate(ratios(10f));

        assertNotNull(sentCell(1, 1));
        assertNull(sentCell(30, 100),
                "A cell the client has not been sent is built when it is,"
                        + " so it is not sent with the ones in view");
    }

    @Test
    void ratiosArriveBeforeAnyCellIsShown_nothingIsSent() {
        Spreadsheet fresh = new Spreadsheet() {
            @Override
            protected SpreadsheetClientRpc getRpcProxy() {
                return rpc;
            }
        };
        fresh.createCell(0, 0, "a value far too wide for its column");
        clearInvocations(rpc);

        fresh.getCellValueManager().onCellStyleWidthRatioUpdate(ratios(10f));

        assertEquals(List.of(), sentCells(),
                "The client asks for its cells once it has measured its view");
    }

    @Test
    void theSameRatiosAgain_nothingIsRebuilt() {
        spreadsheet.getCellValueManager()
                .onCellStyleWidthRatioUpdate(ratios(10f));
        clearInvocations(rpc);

        spreadsheet.getCellValueManager()
                .onCellStyleWidthRatioUpdate(ratios(10f));

        assertEquals(List.of(), sentCells(),
                "Nothing was judged differently, so nothing is sent");
    }

    @Test
    void differentRatios_cellsAreJudgedAgain() {
        spreadsheet.getCellValueManager()
                .onCellStyleWidthRatioUpdate(ratios(1f));
        CellData narrow = sentCell(1, 1);
        assertNotNull(narrow);
        assertEquals(0, narrow.overflowWidth,
                "At a pixel a character the value fits its column");
        clearInvocations(rpc);

        spreadsheet.getCellValueManager()
                .onCellStyleWidthRatioUpdate(ratios(10f));

        CellData wide = sentCell(1, 1);
        assertNotNull(wide, "Wider characters change the verdict, so the"
                + " cell is sent again");
        assertTrue(wide.overflowWidth > 0);
    }

    private CellData sentCell(int col, int row) {
        return sentCells().stream().filter(cd -> cd.col == col && cd.row == row)
                .findFirst().orElse(null);
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
