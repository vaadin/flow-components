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
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * A sheet shown again goes back to where it was scrolled, and the selection it
 * was left with leaves it there, wherever the selected cells are. A selection
 * something leads to - a hyperlink, a named range, an undo, the application
 * selecting cells, or an address typed in - is brought into view.
 * <p>
 * GWT brought every selection into view, so every sheet came back showing its
 * selection, not where it was scrolled.
 */
class SelectionWhenASheetIsShownTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    /** A selection as the client is told it: where, and whether to look. */
    private record Sent(String message, int col, int row,
            boolean broughtIntoView) {
    }

    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet();
        spreadsheet.createNewSheet("Second", 100, 20);
        spreadsheet.setActiveSheetIndex(0);
        ui.add(spreadsheet);
        roundTrip();
    }

    /**
     * Ends the request and answers as the client does: it reports where the
     * sheet is, and the server sends the selection in its response. Returns the
     * selections sent.
     */
    private List<Sent> roundTrip() {
        ui.fakeClientCommunication();
        ui.dumpPendingJavaScriptInvocations();
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": 1, \"firstColumn\": 1, \"lastRow\": 30, \"lastColumn\": 10}");
        ui.fakeClientCommunication();
        return selectionsSent();
    }

    private List<Sent> selectionsSent() {
        return ui.dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .map(invocation -> invocation.getParameters().stream()
                        .map(String::valueOf).collect(Collectors.toList()))
                .filter(args -> args.get(0).equals("showSelectedCell")
                        || args.get(0).equals("setSelectedCellAndRange"))
                .map(args -> new Sent(args.get(0),
                        Integer.parseInt(args.get(2)),
                        Integer.parseInt(args.get(3)),
                        args.get(0).equals("showSelectedCell")
                                // showSelectedCell(..., keepView)
                                ? !Boolean.parseBoolean(args.get(8))
                                // setSelectedCellAndRange(..., scroll)
                                : Boolean.parseBoolean(args.get(8))))
                .collect(Collectors.toList());
    }

    /** Leaves the active sheet for another, as a click on its tab does. */
    private void clickTab(int index) {
        spreadsheet.onSheetSelected(index, 0, 400);
    }

    @Test
    void aSheetShownAgain_keepsTheViewForTheCellItWasLeftWith() {
        spreadsheet.setSelection("B5");
        roundTrip();
        clickTab(1);
        roundTrip();

        clickTab(0);

        assertEquals(List.of(new Sent("showSelectedCell", 2, 5, false)),
                roundTrip());
    }

    @Test
    void aSheetShownAgain_keepsTheViewForTheRangeItWasLeftWith() {
        spreadsheet.setSelection("B2:C3");
        roundTrip();
        clickTab(1);
        roundTrip();

        clickTab(0);

        assertEquals(List.of(new Sent("setSelectedCellAndRange", 2, 2, false)),
                roundTrip());
    }

    @Test
    void cellsTheApplicationSelectsWithTheSwitch_areBroughtIntoView() {
        clickTab(1);
        roundTrip();

        spreadsheet.setActiveSheetIndex(0);
        spreadsheet.setSelection("D30");

        assertEquals(List.of(new Sent("showSelectedCell", 4, 30, true)),
                roundTrip());
    }

    @Test
    void aSelectionTheSheetIsShownFor_isBroughtIntoView() {
        // As a hyperlink, a named range or an undo leading to another sheet
        // sets it.
        clickTab(1);
        roundTrip();

        spreadsheet.setActiveSheetIndex(0);
        spreadsheet.initialSheetSelection = "E40";

        assertEquals(List.of(new Sent("showSelectedCell", 5, 40, true)),
                roundTrip());
    }

    @Test
    void aSelectionTheSheetIsShownFor_beforeTheClientAnswered_isBroughtIntoView() {
        // The sheet switch has gone out with the selection it was left
        // with; before the client answers, something leads elsewhere.
        clickTab(1);
        ui.fakeClientCommunication();

        spreadsheet.setActiveSheetIndex(0);
        spreadsheet.initialSheetSelection = "E40";

        assertEquals(List.of(new Sent("showSelectedCell", 5, 40, true)),
                roundTrip());
    }

    @Test
    void anAddressTypedIn_isBroughtIntoView() {
        clickTab(1);
        roundTrip();

        TestHelper.fireClientEvent(spreadsheet, "sheetAddressChanged",
                "{\"value\": \"F50\"}");
        ui.fakeClientCommunication();

        assertEquals(List.of(new Sent("showSelectedCell", 6, 50, true)),
                selectionsSent());
    }
}
