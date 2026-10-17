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
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * The cells the client shows, as the public getters tell them: none until the
 * client reports where it is, and none again on a sheet shown anew. The buffers
 * the client draws around them are 200 pixels until set, and reach the client
 * once set.
 */
class ViewportTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet();
        spreadsheet.createNewSheet("Second", 50, 20);
        spreadsheet.setActiveSheetIndex(0);
        ui.add(spreadsheet);
    }

    private void clientShows(int firstRow, int firstColumn, int lastRow,
            int lastColumn) {
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": " + firstRow + ", \"firstColumn\": "
                        + firstColumn + ", \"lastRow\": " + lastRow
                        + ", \"lastColumn\": " + lastColumn + "}");
    }

    /** First row, first column, last row and last column shown, 1-based. */
    private List<Integer> shown() {
        return List.of(spreadsheet.getFirstRow(), spreadsheet.getFirstColumn(),
                spreadsheet.getLastRow(), spreadsheet.getLastColumn());
    }

    @Test
    void beforeTheClientReports_noCellIsShown() {
        assertEquals(List.of(-1, -1, -1, -1), shown());
    }

    @Test
    void theCellsTheClientReports_areShown() {
        clientShows(3, 2, 30, 12);

        assertEquals(List.of(3, 2, 30, 12), shown());
    }

    @Test
    void aSheetShownAnew_showsNoCellUntilTheClientReports() {
        clientShows(3, 2, 30, 12);

        spreadsheet.setActiveSheetIndex(1);
        assertEquals(List.of(-1, -1, -1, -1), shown());

        clientShows(1, 1, 20, 10);
        assertEquals(List.of(1, 1, 20, 10), shown());
    }

    @Test
    void theBuffers_areTwoHundredPixels_whichTheClientKnowsUntilSet() {
        assertEquals(200, spreadsheet.getRowBufferSize());
        assertEquals(200, spreadsheet.getColBufferSize());
        assertFalse(spreadsheet.getElement().hasProperty("rowBufferSize"));
        assertFalse(spreadsheet.getElement().hasProperty("columnBufferSize"));
    }

    @Test
    void buffersSet_reachTheClient() {
        spreadsheet.setRowBufferSize(300);
        spreadsheet.setColBufferSize(150);

        assertEquals(300, spreadsheet.getRowBufferSize());
        assertEquals(150, spreadsheet.getColBufferSize());
        assertEquals(300,
                spreadsheet.getElement().getProperty("rowBufferSize", 0));
        assertEquals(150,
                spreadsheet.getElement().getProperty("columnBufferSize", 0));
    }
}
