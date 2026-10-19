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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * Pop-up buttons in cells: on the sheet while their cell is shown, replaced,
 * removed, opened and closed from the client, and taken off when another sheet
 * or workbook is shown.
 */
class SpreadsheetPopupButtonsTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet();
        spreadsheet.createNewSheet("Second", 10, 10);
        spreadsheet.setActiveSheetIndex(0);
        ui.add(spreadsheet);
        clientShowsRows(1, 20);
        clientCalls();
    }

    /** As the client reports the rows it shows, of columns A-J. */
    private void clientShowsRows(int first, int last) {
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": " + first
                        + ", \"firstColumn\": 1, \"lastRow\": " + last
                        + ", \"lastColumn\": 10}");
    }

    /** The names of the functions called on the client since the last look. */
    private List<String> clientCalls() {
        ui.fakeClientCommunication();
        List<String> calls = new ArrayList<>();
        for (PendingJavaScriptInvocation invocation : ui
                .dumpPendingJavaScriptInvocations()) {
            for (Object parameter : invocation.getInvocation()
                    .getParameters()) {
                if ("addPopupButton".equals(parameter)
                        || "removePopupButton".equals(parameter)) {
                    calls.add((String) parameter);
                }
            }
        }
        return calls;
    }

    private boolean onTheSheet(PopupButton button) {
        return spreadsheet.getElement().equals(button.getElement().getParent());
    }

    private PopupButton button() {
        PopupButton button = new PopupButton();
        button.setContent(new Span("content"));
        return button;
    }

    @Test
    void aButtonOnACellShown_isPutOnTheSheet() {
        PopupButton button = button();

        spreadsheet.setPopup("B2", button);

        assertTrue(onTheSheet(button));
        assertEquals(List.of("addPopupButton"), clientCalls());
    }

    @Test
    void settingAndRemovingAButtonShown_tellsTheClientEach() {
        // Each call draws on the client at the end of the round trip, as
        // every call from the server does: no other mark is sent with it.
        spreadsheet.setPopup("B2", button());
        assertEquals(List.of("addPopupButton"), clientCalls());

        spreadsheet.setPopup("B2", null);
        assertEquals(List.of("removePopupButton"), clientCalls());
    }

    @Test
    void aButtonOnACellNotShown_tellsTheClientNothing() {
        spreadsheet.setPopup("B100", button());

        assertEquals(List.of(), clientCalls());
    }

    @Test
    void aButtonOnACellNotShown_isPutOnTheSheetOnceTheCellIs() {
        PopupButton button = button();
        spreadsheet.setPopup("B100", button);
        assertFalse(onTheSheet(button));

        clientShowsRows(90, 110);

        assertTrue(onTheSheet(button));
    }

    @Test
    void aButton_isOnTheSheetOnlyWhileItsCellIsShown() {
        clientShowsRows(2, 21);
        Map<String, PopupButton> buttons = new LinkedHashMap<>();
        for (String cell : List.of("B1", "B2", "B3", "B21", "B22", "B23", "J5",
                "K5")) {
            buttons.put(cell, button());
            spreadsheet.setPopup(cell, buttons.get(cell));
        }
        assertEquals(List.of("B2", "B3", "B21", "J5"), onTheSheet(buttons));

        clientShowsRows(3, 22);

        assertEquals(List.of("B3", "B21", "B22", "J5"), onTheSheet(buttons));
    }

    private List<String> onTheSheet(Map<String, PopupButton> buttons) {
        return buttons.keySet().stream()
                .filter(cell -> onTheSheet(buttons.get(cell))).toList();
    }

    @Test
    void buttonsOnFrozenRowsAndColumns_stayOnTheSheetAsTheRestScrolls() {
        // Rows 1-2 and columns A-B frozen, and the client scrolled to rows
        // 50-70 of columns E-J.
        spreadsheet.createFreezePane(2, 2);
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": 50, \"firstColumn\": 5, \"lastRow\": 70, \"lastColumn\": 10}");
        Map<String, PopupButton> buttons = new LinkedHashMap<>();
        for (String cell : List.of("A1", "E1", "E2", "C1", "A60", "B60", "A80",
                "E60", "C60", "C3")) {
            buttons.put(cell, button());
            spreadsheet.setPopup(cell, buttons.get(cell));
        }

        assertEquals(List.of("A1", "E1", "E2", "A60", "B60", "E60"),
                onTheSheet(buttons));
    }

    @Test
    void aButtonOfTheSheetShownBefore_isNotPutOnTheSheetShownNow() {
        PopupButton button = button();
        spreadsheet.setPopup("B100", button);
        spreadsheet.setActiveSheetIndex(1);

        clientShowsRows(90, 110);

        assertFalse(onTheSheet(button));
    }

    @Test
    void aButtonScrolledAway_isTakenOffTheSheet() {
        PopupButton button = button();
        spreadsheet.setPopup("B2", button);
        clientCalls();

        clientShowsRows(50, 70);

        assertFalse(onTheSheet(button));
        assertEquals(List.of("removePopupButton"), clientCalls());
    }

    @Test
    void aButtonScrolledAway_isTakenOffOnce() {
        PopupButton button = button();
        spreadsheet.setPopup("B2", button);
        clientShowsRows(50, 70);
        clientCalls();

        clientShowsRows(80, 100);

        assertEquals(List.of(), clientCalls());
    }

    @Test
    void aButtonMovedOffTheSheetElsewhere_isStillTakenOffTheClient() {
        PopupButton button = button();
        spreadsheet.setPopup("B2", button);
        button.getElement().removeFromTree();
        clientCalls();

        clientShowsRows(50, 70);

        assertEquals(List.of("removePopupButton"), clientCalls());
    }

    @Test
    void aButtonNeverShown_isNotTakenOffTheClientWhenRemoved() {
        spreadsheet.setPopup("B100", button());

        spreadsheet.setPopup("B100", null);

        assertEquals(List.of(), clientCalls());
    }

    @Test
    void aButtonTakenOff_isNotTakenOffAgainWhenAnotherSheetIsShown() {
        PopupButton button = button();
        spreadsheet.setPopup("B2", button);
        clientShowsRows(50, 70);
        clientCalls();

        spreadsheet.setActiveSheetIndex(1);

        assertEquals(List.of(), clientCalls());
    }

    @Test
    void aSecondButtonForACell_replacesTheFirst() {
        PopupButton first = button();
        PopupButton second = button();
        spreadsheet.setPopup("B2", first);

        spreadsheet.setPopup("B2", second);

        assertFalse(onTheSheet(first));
        assertTrue(onTheSheet(second));
    }

    @Test
    void noButtonForACell_removesTheOneThere() {
        PopupButton button = button();
        spreadsheet.setPopup("B2", button);

        spreadsheet.setPopup("B2", null);

        assertFalse(onTheSheet(button));
        clientShowsRows(2, 21);
        assertFalse(onTheSheet(button), "and it does not come back");
    }

    @Test
    void theClient_opensAndClosesTheButtonOfACell() {
        PopupButton b2 = button();
        PopupButton c3 = button();
        spreadsheet.setPopup("B2", b2);
        spreadsheet.setPopup("C3", c3);
        List<String> events = new ArrayList<>();
        b2.addPopupOpenListener(e -> events.add("B2 open"));
        b2.addPopupCloseListener(e -> events.add("B2 close"));
        c3.addPopupOpenListener(e -> events.add("C3 open"));

        TestHelper.fireClientEvent(spreadsheet, "popupButtonClick",
                "{\"row\": 2, \"column\": 2}");
        TestHelper.fireClientEvent(spreadsheet, "popupClose",
                "{\"row\": 2, \"column\": 2}");
        TestHelper.fireClientEvent(spreadsheet, "popupButtonClick",
                "{\"row\": 9, \"column\": 9}");

        assertEquals(List.of("B2 open", "B2 close"), events);
    }

    @Test
    void anotherSheetShown_takesTheButtonsOff() {
        PopupButton button = button();
        spreadsheet.setPopup("B2", button);

        spreadsheet.setActiveSheetIndex(1);

        assertFalse(onTheSheet(button));
    }

    @Test
    void anotherWorkbook_forgetsTheButtons() {
        PopupButton button = button();
        spreadsheet.setPopup("B2", button);
        XSSFWorkbook workbook = new XSSFWorkbook();
        // Named as the sheet the button was on.
        workbook.createSheet(spreadsheet.getActiveSheet().getSheetName());

        spreadsheet.setWorkbook(workbook);
        clientShowsRows(1, 20);

        assertFalse(onTheSheet(button));
    }

    @Test
    void aButtonOnARowInsertedAbove_movesDownWithItsRow() {
        PopupButton button = button();
        spreadsheet.setPopup("B3", button);
        spreadsheet.setSelection("A1");
        clientCalls();

        spreadsheet.shiftRows(1, 30, 1, true, true);

        assertEquals(
                new CellReference(spreadsheet.getActiveSheet().getSheetName(),
                        3, 1, true, true),
                button.getCellReference());
        assertTrue(onTheSheet(button));
        // Taken off where it was, and put on where it is now.
        assertEquals(List.of("removePopupButton", "addPopupButton"),
                clientCalls());
        List<String> events = new ArrayList<>();
        button.addPopupOpenListener(e -> events.add("open"));
        TestHelper.fireClientEvent(spreadsheet, "popupButtonClick",
                "{\"row\": 4, \"column\": 2}");
        assertEquals(List.of("open"), events, "opened from its new cell");
    }

    @Test
    void aButtonAboveTheRowsThatMove_staysOnTheSheet() {
        PopupButton button = button();
        spreadsheet.setPopup("B2", button);
        spreadsheet.setSelection("A1");
        clientCalls();

        // Rows 3-31 move down by one.
        spreadsheet.shiftRows(2, 30, 1, true, true);

        assertEquals(
                new CellReference(spreadsheet.getActiveSheet().getSheetName(),
                        1, 1, true, true),
                button.getCellReference());
        assertTrue(onTheSheet(button));
        assertFalse(clientCalls().contains("removePopupButton"));
    }

    /** A button on a cell of the sheet "Second", which is then left. */
    private PopupButton buttonOnTheSecondSheet(String cell) {
        spreadsheet.setActiveSheetIndex(1);
        PopupButton button = button();
        spreadsheet.setPopup(cell, button);
        spreadsheet.setActiveSheetIndex(0);
        clientShowsRows(2, 21);
        spreadsheet.setSelection("A1");
        clientCalls();
        return button;
    }

    @Test
    void aButtonOfAnotherSheet_staysWhereItIsWhenRowsMoveOnTheSheetShown() {
        PopupButton button = buttonOnTheSecondSheet("B3");

        spreadsheet.shiftRows(1, 30, 1, true, true);

        assertEquals(new CellReference("Second", 2, 1, true, true),
                button.getCellReference());
        assertFalse(onTheSheet(button));
        assertEquals(List.of(), clientCalls());
    }

    @Test
    void aButtonOfAnotherSheet_isKeptWhenItsRowIsOverwrittenOnTheSheetShown() {
        PopupButton button = buttonOnTheSecondSheet("B3");

        spreadsheet.shiftRows(3, 30, -1, true, true);
        spreadsheet.setActiveSheetIndex(1);
        clientShowsRows(1, 20);

        assertTrue(onTheSheet(button));
    }

    @Test
    void aButtonOnARowOverwritten_isRemoved() {
        PopupButton button = button();
        spreadsheet.setPopup("B3", button);
        spreadsheet.setSelection("A1");

        clientCalls();

        // Rows 4-31 move up by one: row 3 is overwritten.
        spreadsheet.shiftRows(3, 30, -1, true, true);

        assertFalse(onTheSheet(button));
        assertEquals(List.of("removePopupButton"), clientCalls());
    }

    @Test
    void aButtonRemovedWithItsRow_canBeSetAgain() {
        // As a filter table does with its buttons when it is reloaded.
        PopupButton button = button();
        spreadsheet.setPopup("B3", button);
        spreadsheet.setSelection("A1");
        spreadsheet.shiftRows(3, 30, -1, true, true);
        clientCalls();

        spreadsheet.setPopup("D5", button);

        assertTrue(onTheSheet(button));
        assertEquals(List.of("addPopupButton"), clientCalls());
    }

    @Test
    void aButtonTheClientHas_isNotSentAgainAsItScrolls() {
        spreadsheet.setPopup("B2", button());
        clientCalls();

        clientShowsRows(2, 21);

        assertEquals(List.of(), clientCalls());
    }

    @Test
    void aButtonWhoseStateChanged_isSentAgain() {
        PopupButton button = button();
        spreadsheet.setPopup("B2", button);
        clientCalls();

        button.markActive(true);
        clientShowsRows(2, 21);

        assertEquals(List.of("addPopupButton"), clientCalls());
    }

    @Test
    void aClientStartedAnew_getsTheButtonsAgain() {
        spreadsheet.setPopup("B2", button());
        clientCalls();

        TestHelper.fireClientEvent(spreadsheet, "onConnectorInit", "{}");
        clientShowsRows(1, 20);

        assertEquals(List.of("addPopupButton"), clientCalls());
    }
}
