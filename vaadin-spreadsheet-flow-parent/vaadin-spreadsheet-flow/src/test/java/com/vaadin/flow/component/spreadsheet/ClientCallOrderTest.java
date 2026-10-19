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

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.spreadsheet.client.CellData;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;

/**
 * The calls a round trip sends the client, after the properties, which the
 * client takes first: the cells, then the selection, the formula bar, the
 * actions, the comment to write and the pop-up buttons, each kind in the order
 * its calls were made, whatever order the kinds came in. The formula bar reads
 * the selection and the cells, so it is a plain call after them.
 * <p>
 * The keys of the resources are a property, which a spreadsheet attached again
 * is given with the rest.
 */
class ClientCallOrderTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    @BeforeEach
    void show() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet();
        spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": 1, \"firstColumn\": 1, \"lastRow\": 20, \"lastColumn\": 10}");
        invocations();
    }

    private List<PendingJavaScriptInvocation> invocations() {
        return ui.dumpPendingJavaScriptInvocations();
    }

    /** The functions called on the client, by name, in the order sent. */
    private List<String> calls() {
        List<String> calls = new ArrayList<>();
        for (PendingJavaScriptInvocation invocation : invocations()) {
            calls.add(String.valueOf(
                    invocation.getInvocation().getParameters().get(0)));
        }
        return calls;
    }

    private static ArrayList<CellData> cellB1(String value) {
        CellData cell = new CellData();
        cell.col = 2;
        cell.row = 1;
        cell.value = value;
        return new ArrayList<>(List.of(cell));
    }

    @Test
    void theCalls_goOutAfterTheCells_inTheOrderOfTheirKinds() {
        var client = spreadsheet.getRpcProxy();
        client.editCellComment(1, 0);
        client.showActions(new ArrayList<>());
        client.updateFormulaBar(null, 2, 1);
        client.setSelectedCellAndRange("B1", 2, 1, 2, 2, 1, 1, false);
        client.cellsUpdated(cellB1("=1+1"));

        assertEquals(
                List.of("cellsUpdated", "setSelectedCellAndRange",
                        "updateFormulaBar", "showActions", "editCellComment"),
                calls());
    }

    @Test
    void callsOfOneKind_goOutInTheOrderMade() {
        var client = spreadsheet.getRpcProxy();
        client.setSelectedCellAndRange("B1", 2, 1, 2, 2, 1, 1, false);
        client.invalidCellAddress();
        client.setSelectedCellAndRange("C1", 3, 1, 3, 3, 1, 1, false);

        assertEquals(List.of("setSelectedCellAndRange", "invalidCellAddress",
                "setSelectedCellAndRange"), calls());
    }

    @Test
    void aCall_isSentOnce() {
        spreadsheet.getRpcProxy().invalidCellAddress();
        calls();

        assertEquals(List.of(), calls());
    }

    @Test
    void aPopupButtonOpenedAsItIsAdded_isAddedFirst() {
        PopupButton button = new PopupButton();
        button.setContent(new Span("filter"));
        spreadsheet.setPopup("B2", button);
        button.openPopup();

        List<String> popupCalls = calls().stream()
                .filter(call -> call.contains("Popup")).toList();
        assertEquals(List.of("addPopupButton", "onPopupButtonOpen"),
                popupCalls);
    }

    @Test
    void theFormulaBar_isAPlainCall() {
        spreadsheet.getRpcProxy().updateFormulaBar("Total", 2, 1);

        List<PendingJavaScriptInvocation> sent = invocations();

        assertEquals(1, sent.size());
        List<Object> parameters = sent.get(0).getInvocation().getParameters();
        assertEquals(List.of("updateFormulaBar", "Total", 2, 1),
                parameters.subList(0, 4));
        assertFalse(sent.get(0).getInvocation().getExpression()
                .contains("queueMicrotask"));
    }

    private static DownloadHandler image() {
        return event -> {
        };
    }

    private List<String> resourceKeys() {
        List<String> keys = new ArrayList<>();
        Object raw = spreadsheet.getElement().getPropertyRaw("resources");
        if (raw instanceof JsonNode json) {
            json.forEach(key -> keys.add(key.asString()));
        }
        return keys;
    }

    @Test
    void theKeysOfTheResources_areAPropertyOfTheElement() {
        spreadsheet.setResource("chart", image());
        spreadsheet.setResource("logo", image());

        assertEquals(List.of("chart", "logo"),
                resourceKeys().stream().sorted().toList());
        assertEquals(List.of(), calls());
    }

    @Test
    void aResourceTakenOff_isTakenOffTheKeys() {
        spreadsheet.setResource("chart", image());
        spreadsheet.setResource("logo", image());

        spreadsheet.setResource("chart", null);

        assertEquals(List.of("logo"), resourceKeys());
    }

    @Test
    void aSpreadsheetAttachedAgain_isSentNoCallForTheResources() {
        spreadsheet.setResource("chart", image());
        invocations();

        ui.remove(spreadsheet);
        ui.add(spreadsheet);

        assertFalse(invocations().stream().anyMatch(invocation -> invocation
                .getInvocation().getExpression().contains("resources")));
        assertEquals(List.of("chart"), resourceKeys());
    }
}
