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
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;

/**
 * The cells the client shows, as the public getters tell them: none until the
 * client reports where it is, and none again on a sheet shown anew. A report
 * that shows more cells sends those, and one that shows the same sends nothing.
 * The buffers the client draws around them are 200 pixels until set, and reach
 * the client once set.
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
        // The first response loads the client, which then reports where it
        // is, and is sent the cells it shows.
        ui.fakeClientCommunication();
        ui.dumpPendingJavaScriptInvocations();
    }

    private void clientShows(int firstRow, int firstColumn, int lastRow,
            int lastColumn) {
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": " + firstRow + ", \"firstColumn\": "
                        + firstColumn + ", \"lastRow\": " + lastRow
                        + ", \"lastColumn\": " + lastColumn + "}");
    }

    /**
     * The client asks to load one range, 1-based, and draws a narrower one,
     * which comes along.
     */
    private void clientLoadsAndDraws(int firstRow, int firstColumn, int lastRow,
            int lastColumn, int drawnFirstRow, int drawnFirstColumn,
            int drawnLastRow, int drawnLastColumn) {
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": " + firstRow + ", \"firstColumn\": "
                        + firstColumn + ", \"lastRow\": " + lastRow
                        + ", \"lastColumn\": " + lastColumn
                        + ", \"drawnFirstRow\": " + drawnFirstRow
                        + ", \"drawnFirstColumn\": " + drawnFirstColumn
                        + ", \"drawnLastRow\": " + drawnLastRow
                        + ", \"drawnLastColumn\": " + drawnLastColumn + "}");
    }

    /** First row, first column, last row and last column shown, 1-based. */
    private List<Integer> shown() {
        return List.of(spreadsheet.getFirstRow(), spreadsheet.getFirstColumn(),
                spreadsheet.getLastRow(), spreadsheet.getLastColumn());
    }

    /** Every cell of A1:O40 holds a value, so that it is sent when shown. */
    private void fillA1ToO40() {
        for (int row = 0; row < 40; row++) {
            for (int column = 0; column < 15; column++) {
                spreadsheet.createCell(row, column, row * 100 + column);
            }
        }
    }

    /** The cells sent since the last look, as "B3". */
    private Set<String> cellsSent() {
        ui.fakeClientCommunication();
        Set<String> sent = new TreeSet<>();
        for (PendingJavaScriptInvocation invocation : ui
                .dumpPendingJavaScriptInvocations()) {
            for (Object parameter : invocation.getInvocation()
                    .getParameters()) {
                if (parameter instanceof JsonNode json && json.isArray()
                        && json.path(0).has("col")) {
                    for (JsonNode cell : json) {
                        sent.add(new CellReference(cell.get("row").asInt() - 1,
                                cell.get("col").asInt() - 1).formatAsString());
                    }
                }
            }
        }
        return sent;
    }

    /** The cells of a range, as "B3", 1-based. */
    private static Set<String> cells(int firstRow, int firstColumn, int lastRow,
            int lastColumn) {
        Set<String> cells = new TreeSet<>();
        for (int row = firstRow; row <= lastRow; row++) {
            for (int column = firstColumn; column <= lastColumn; column++) {
                cells.add(new CellReference(row - 1, column - 1)
                        .formatAsString());
            }
        }
        return cells;
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
    void theRangeShown_isTheCellsDrawn_notTheMarginLoadedAhead() {
        // The range drawn: rows 1-32 for 21 rows in view.
        clientLoadsAndDraws(1, 1, 80, 12, 1, 1, 32, 10);

        assertEquals(List.of(1, 1, 32, 10), shown());

        clientLoadsAndDraws(60, 1, 185, 12, 80, 2, 122, 9);

        assertEquals(List.of(80, 2, 122, 9), shown());
    }

    @Test
    void theCellsLoadedAhead_areSentWithThoseDrawn() {
        fillA1ToO40();

        clientLoadsAndDraws(1, 1, 40, 15, 1, 1, 12, 5);

        assertEquals(cells(1, 1, 40, 15), cellsSent());
    }

    @Test
    void aSheetShownAnew_forgetsTheCellsDrawn() {
        clientLoadsAndDraws(1, 1, 80, 12, 1, 1, 32, 10);

        spreadsheet.setActiveSheetIndex(1);

        assertEquals(List.of(-1, -1, -1, -1), shown());
    }

    @Test
    void visibleCellContentsReloaded_areThoseLoaded_notOnlyThoseDrawn() {
        clientLoadsAndDraws(1, 1, 40, 15, 1, 1, 12, 5);
        cellsSent();
        // Made through POI alone, past the cells drawn: never sent yet
        spreadsheet.getActiveSheet().createRow(29).createCell(13)
                .setCellValue("made");

        spreadsheet.reloadVisibleCellContents();

        assertEquals(Set.of("N30"), cellsSent());
    }

    /**
     * A long number in every cell of A1:O40, which a column of the default
     * width shortens once the client has said how wide a digit is: loaded from
     * 1 to 40 and 1 to 15, A1:E12 drawn.
     */
    private void longNumbersShortened() {
        for (int row = 0; row < 40; row++) {
            for (int column = 0; column < 15; column++) {
                spreadsheet.createCell(row, column,
                        row * 100 + column + 0.123456789012);
            }
        }
        clientLoadsAndDraws(1, 1, 40, 15, 1, 1, 12, 5);
        TestHelper.fireClientEvent(spreadsheet, "setCellStyleWidthRatios",
                "{\"styleIndexes\": [0], \"ratios\": [7.0]}");
        cellsSent();
    }

    @Test
    void aColumnResized_sendsItsCellsLoaded_notOnlyThoseDrawn() {
        longNumbersShortened();

        spreadsheet.setColumnWidth(13, 400);

        assertEquals(cells(1, 14, 40, 14), cellsSent());
    }

    @Test
    void newWidthRatios_rebuildTheCellsLoaded_notOnlyThoseDrawn() {
        longNumbersShortened();

        TestHelper.fireClientEvent(spreadsheet, "setCellStyleWidthRatios",
                "{\"styleIndexes\": [0], \"ratios\": [2.5]}");

        assertEquals(cells(1, 1, 40, 15), cellsSent());
    }

    @Test
    void allCellValuesRefreshed_areTheCellsLoaded_notOnlyThoseDrawn() {
        fillA1ToO40();
        clientLoadsAndDraws(1, 1, 40, 15, 1, 1, 12, 5);
        cellsSent();

        spreadsheet.refreshAllCellValues();

        assertEquals(cells(1, 1, 40, 15), cellsSent());
    }

    @Test
    void aWindowGrownDown_sendsTheRowsItShows() {
        fillA1ToO40();
        clientShows(1, 1, 20, 10);
        cellsSent();

        clientShows(1, 1, 30, 10);

        assertEquals(cells(21, 1, 30, 10), cellsSent());
    }

    @Test
    void aWindowGrownRight_sendsTheColumnsItShows() {
        fillA1ToO40();
        clientShows(1, 1, 20, 10);
        cellsSent();

        clientShows(1, 1, 20, 15);

        assertEquals(cells(1, 11, 20, 15), cellsSent());
    }

    @Test
    void aWindowGrownUp_sendsTheRowsItShows() {
        fillA1ToO40();
        clientShows(11, 1, 30, 10);
        cellsSent();

        clientShows(1, 1, 30, 10);

        assertEquals(cells(1, 1, 10, 10), cellsSent());
    }

    @Test
    void aWindowGrownLeft_sendsTheColumnsItShows() {
        fillA1ToO40();
        clientShows(1, 6, 20, 15);
        cellsSent();

        clientShows(1, 1, 20, 15);

        assertEquals(cells(1, 1, 20, 5), cellsSent());
    }

    @Test
    void theSameCellsReportedAgain_sendNothing() {
        fillA1ToO40();
        clientShows(1, 1, 20, 10);
        ui.fakeClientCommunication();
        ui.dumpPendingJavaScriptInvocations();

        clientShows(1, 1, 20, 10);

        ui.fakeClientCommunication();
        assertEquals(List.of(), ui.dumpPendingJavaScriptInvocations());
    }

    @Test
    void aSheetWithoutRowsOrColumns_showsItsFirstCell() {
        spreadsheet.setMaxRows(0);
        spreadsheet.setMaxColumns(0);

        clientShows(1, 1, 20, 10);

        assertEquals(List.of(1, 1, 1, 1), shown());
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
