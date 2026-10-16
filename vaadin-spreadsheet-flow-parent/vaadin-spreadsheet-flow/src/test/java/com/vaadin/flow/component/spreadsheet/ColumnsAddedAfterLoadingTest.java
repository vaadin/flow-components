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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Columns that {@link Spreadsheet#setMaxColumns(int)} adds after the sheet was
 * loaded can be sized, hidden and autofitted like the others. Their widths were
 * written past the end of the widths the sheet was loaded with.
 */
class ColumnsAddedAfterLoadingTest {

    private Spreadsheet spreadsheet;

    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet(20, 5);
        spreadsheet.setMaxColumns(10);
    }

    @Test
    void columnAddedLater_takesTheWidthItIsGiven() {
        spreadsheet.setColumnWidth(8, 120);

        int[] colW = spreadsheet.getColW();
        assertEquals(120, colW[8]);
        // The ones in between have the default width, rather than read as
        // hidden.
        assertEquals(spreadsheet.getDefaultColumnWidth(), colW[6]);
    }

    @Test
    void columnAddedLater_canBeHidden() {
        spreadsheet.setColumnHidden(9, true);

        assertEquals(0, spreadsheet.getColW()[9]);
        assertTrue(spreadsheet.getHiddenColumnIndexes().contains(10));
    }

    @Test
    void columnAddedLater_canBeAutofitted() {
        spreadsheet.createCell(0, 7, "a fairly long piece of text");

        spreadsheet.autofitColumn(7);

        assertTrue(spreadsheet.getColW().length > 7);
        assertTrue(spreadsheet.getColW()[7] > 0);
    }
}
