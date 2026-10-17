/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * The rows {@link Spreadsheet#setMaxRows(int)} adds past the ones the sheet was
 * loaded with: the row heights stopped at the loaded rows, and a component on
 * one of the added rows, or deleting them, ran past their end.
 */
class RowsAddedByMaxRowsTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;
    private float defaultHeight;

    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet();
        // A new sheet has the heights of its 200 rows.
        assertEquals(200, spreadsheet.getRowH().length);
        spreadsheet.setMaxRows(1000);
        defaultHeight = spreadsheet.getActiveSheet()
                .getDefaultRowHeightInPoints();
    }

    private void scrollTo(int firstRow, int lastRow) {
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": " + firstRow
                        + ", \"firstColumn\": 1, \"lastRow\": " + lastRow
                        + ", \"lastColumn\": 10}");
    }

    /** A component in A501, 0-based row 500. */
    private void componentInA501() {
        spreadsheet.setSpreadsheetComponentFactory(
                new SpreadsheetComponentFactory() {
                    @Override
                    public Component getCustomComponentForCell(Cell cell,
                            int rowIndex, int columnIndex,
                            Spreadsheet spreadsheet, Sheet sheet) {
                        return rowIndex == 500 && columnIndex == 0 ? new Span()
                                : null;
                    }

                    @Override
                    public Component getCustomEditorForCell(Cell cell,
                            int rowIndex, int columnIndex,
                            Spreadsheet spreadsheet, Sheet sheet) {
                        return null;
                    }

                    @Override
                    public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                            int columnIndex, Spreadsheet spreadsheet,
                            Sheet sheet, Component customEditor) {
                    }
                });
    }

    @Test
    void aComponentOnAnAddedRow_makesTheRowTallEnoughForIt() {
        componentInA501();

        scrollTo(490, 520);

        float[] rowH = spreadsheet.getRowH();
        assertEquals(spreadsheet.getMinimumRowHeightForComponents(), rowH[500]);
        assertEquals(defaultHeight, rowH[499]);
    }

    @Test
    void deletingAddedRows_leavesThemTheDefaultHeight() {
        spreadsheet.setSelection("A1");

        spreadsheet.deleteRows(300, 310);

        float[] rowH = spreadsheet.getRowH();
        assertEquals(defaultHeight, rowH[305]);
        assertEquals(defaultHeight, rowH[310]);
    }

    @Test
    void heightsCovering_takesTheAddedRowsHeightsFromPoi() {
        Sheet sheet = spreadsheet.getActiveSheet();
        sheet.createRow(250).setHeightInPoints(40);
        sheet.createRow(260).setZeroHeight(true);
        float[] loaded = spreadsheet.getRowH().clone();

        float[] rowH = spreadsheet.sizes().heightsCovering(270);

        assertEquals(271, rowH.length);
        assertArrayEquals(loaded, Arrays.copyOf(rowH, loaded.length));
        assertEquals(40f, rowH[250]);
        assertEquals(0f, rowH[260]);
        assertEquals(defaultHeight, rowH[255]);
        assertEquals(defaultHeight, rowH[270]);
    }

    @Test
    void heightsCovering_aLoadedRow_isACopyOfTheHeights() {
        float[] loaded = spreadsheet.getRowH();

        float[] rowH = spreadsheet.sizes().heightsCovering(10);

        assertArrayEquals(loaded, rowH);
        rowH[10] = 99;
        assertEquals(defaultHeight, spreadsheet.getRowH()[10]);
    }
}
