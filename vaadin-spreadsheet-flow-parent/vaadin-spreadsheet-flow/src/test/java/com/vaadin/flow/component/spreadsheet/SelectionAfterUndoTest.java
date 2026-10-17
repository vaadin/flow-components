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

import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.internal.UIInternals.JavaScriptInvocation;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * What an undo selects, as the client is told it: the cell typed in, or the
 * range values were deleted from, brought into view. Undone on another sheet,
 * that sheet is shown, and the selection comes once the client has the sheet. A
 * command that selects nothing leaves the selection, and the formula bar is
 * refreshed once, by what the command changes.
 */
class SelectionAfterUndoTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet();
        spreadsheet.createNewSheet("Second", 100, 20);
        spreadsheet.setActiveSheetIndex(0);
        ui.add(spreadsheet);
        respond();
        clientReportsWhereItIs();
    }

    /**
     * Ends the request; returns what its response selects, and the formula bar
     * refreshes it asks for.
     */
    private List<String> respond() {
        ui.fakeClientCommunication();
        return ui.dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .map(SelectionAfterUndoTest::selection).filter(Objects::nonNull)
                .toList();
    }

    /** What a response selects, without the formula bar refreshes. */
    private static List<String> selections(List<String> calls) {
        return calls.stream()
                .filter(call -> !call.startsWith("updateFormulaBar")).toList();
    }

    /** The client reports where it is, as after a reload, and is answered. */
    private List<String> clientReportsWhereItIs() {
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": 1, \"firstColumn\": 1, \"lastRow\": 30, \"lastColumn\": 10}");
        return respond();
    }

    /**
     * A call that selects, as "showSelectedCell B2 into view" or
     * "setSelectedCellAndRange B2 in B2:C3 into view", an address the client is
     * told is no cell, or a formula bar refresh; null for other calls.
     */
    private static String selection(JavaScriptInvocation invocation) {
        List<String> args = invocation.getParameters().stream()
                .map(String::valueOf).toList();
        if (invocation.getExpression().contains("updateFormulaBar")) {
            return "updateFormulaBar " + address(args.get(1), args.get(2));
        }
        return switch (args.get(0)) {
        case "invalidCellAddress" -> "invalidCellAddress";
        // showSelectedCell(name, col, row, value, formula, locked,
        // initialSelection, keepingView)
        case "showSelectedCell" ->
            "showSelectedCell " + address(args.get(2), args.get(3))
                    + (Boolean.parseBoolean(args.get(8)) ? "" : " into view");
        // setSelectedCellAndRange(name, col, row, c1, c2, r1, r2, scroll)
        case "setSelectedCellAndRange" ->
            "setSelectedCellAndRange " + address(args.get(2), args.get(3))
                    + " in " + address(args.get(4), args.get(6)) + ":"
                    + address(args.get(5), args.get(7))
                    + (Boolean.parseBoolean(args.get(8)) ? " into view" : "");
        default -> null;
        };
    }

    /** The client numbers rows and columns from 1. */
    private static String address(String column, String row) {
        return new CellReference(Integer.parseInt(row) - 1,
                Integer.parseInt(column) - 1).formatAsString();
    }

    private void select(int row, int column) {
        TestHelper.fireClientEvent(spreadsheet, "cellSelected",
                "{\"row\": " + row + ", \"column\": " + column
                        + ", \"oldSelectionRangeDiscarded\": true}");
    }

    private void type(int row, int column, String value) {
        select(row, column);
        TestHelper.fireClientEvent(spreadsheet, "cellValueEdited",
                "{\"row\": " + row + ", \"col\": " + column + ", \"value\": \""
                        + value + "\"}");
    }

    /**
     * Selects B2:C3 from B2 and deletes their values, as the Delete key does.
     */
    private void deleteB2ToC3() {
        type(2, 2, "a");
        type(3, 3, "b");
        select(2, 2);
        TestHelper.fireClientEvent(spreadsheet, "cellRangeSelected",
                "{\"row1\": 2, \"col1\": 2, \"row2\": 3, \"col2\": 3}");
        TestHelper.fireClientEvent(spreadsheet, "deleteSelectedCells", "{}");
    }

    private void undo() {
        TestHelper.fireClientEvent(spreadsheet, "onUndo", "{}");
    }

    /** Leaves the first sheet for the second, as a click on its tab does. */
    private void showTheSecondSheet() {
        spreadsheet.onSheetSelected(1, 0, 400);
        respond();
        clientReportsWhereItIs();
    }

    @Test
    void anUndo_selectsTheCellTypedIn() {
        type(2, 2, "x");
        select(4, 4);
        respond();

        undo();

        assertEquals(List.of("showSelectedCell B2 into view"),
                selections(respond()));
    }

    @Test
    void anUndo_selectsTheRangeValuesWereDeletedFrom() {
        deleteB2ToC3();
        select(5, 5);
        respond();

        undo();

        assertEquals(List.of("setSelectedCellAndRange B2 in B2:C3 into view"),
                selections(respond()));
    }

    @Test
    void anUndoOnAnotherSheet_selectsTheCellTypedIn_onceTheClientHasIt() {
        // The sheet is left with D4 selected, not B2.
        type(2, 2, "x");
        select(4, 4);
        respond();
        showTheSecondSheet();

        undo();

        assertEquals(0, spreadsheet.getActiveSheetIndex());
        assertEquals(List.of(), selections(respond()));
        assertEquals(List.of("showSelectedCell B2 into view"),
                selections(clientReportsWhereItIs()));
    }

    @Test
    void anUndoOnAnotherSheet_selectsTheRangeValuesWereDeletedFrom() {
        deleteB2ToC3();
        select(5, 5);
        respond();
        showTheSecondSheet();

        undo();
        respond();

        assertEquals(List.of("setSelectedCellAndRange B2 in B2:C3 into view"),
                selections(clientReportsWhereItIs()));
    }
}
