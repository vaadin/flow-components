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

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.spreadsheet.client.CellData;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;

/**
 * The cells the client is sent: each once a round trip, as it last was, only
 * when the client does not have it so, and taken off only if the client has it.
 */
class ClientCellMirrorTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    @BeforeEach
    void show() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet();
        spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        calls();
    }

    private static CellData cell(int col, int row, String value) {
        CellData cell = new CellData();
        cell.col = col;
        cell.row = row;
        cell.value = value;
        return cell;
    }

    private void send(CellData... cells) {
        spreadsheet.clientCells.send(new ArrayList<>(List.of(cells)));
    }

    /**
     * The calls of a round trip: the cells sent as "B1=x", taken off as "B1",
     * and the other calls by name.
     */
    private List<String> calls() {
        List<String> calls = new ArrayList<>();
        for (PendingJavaScriptInvocation invocation : ui
                .dumpPendingJavaScriptInvocations()) {
            List<Object> parameters = invocation.getInvocation()
                    .getParameters();
            String function = (String) parameters.get(0);
            if (!"cellsUpdated".equals(function)) {
                calls.add(function);
                continue;
            }
            for (JsonNode cell : TestHelper.sentJson(parameters.get(1))) {
                String name = FreshLoadHarness.name(cell.get("row").asInt(),
                        cell.get("col").asInt());
                calls.add(cell.path("value").isNull() ? name
                        : name + "=" + cell.get("value").asString());
            }
        }
        return calls;
    }

    @Test
    void aCellSentTwice_goesOnce_asItWasLast() {
        send(cell(2, 1, "first"));
        send(cell(2, 1, "last"), cell(3, 1, "other"));

        assertEquals(List.of("B1=last", "C1=other"), calls());
    }

    @Test
    void aCellTheClientHasSo_isNotSentAgain() {
        send(cell(2, 1, "same"));
        calls();

        send(cell(2, 1, "same"));

        assertEquals(List.of(), calls());
    }

    @Test
    void aCellTheClientHasOtherwise_isSent() {
        send(cell(2, 1, "before"));
        calls();

        send(cell(2, 1, "after"));

        assertEquals(List.of("B1=after"), calls());
    }

    @Test
    void aCellTakenOff_goesOnlyIfTheClientHasIt() {
        send(cell(2, 1, "there"));
        calls();

        send(cell(2, 1, null), cell(3, 1, null));

        assertEquals(List.of("B1"), calls());
    }

    @Test
    void aCellTakenOff_andSentAgain_isSent() {
        send(cell(2, 1, "there"));
        calls();
        send(cell(2, 1, null));
        calls();

        send(cell(2, 1, "there"));

        assertEquals(List.of("B1=there"), calls());
    }

    @Test
    void aReload_dropsTheCellsNotYetSent_andTheClientIsSentAllAgain() {
        send(cell(2, 1, "kept"));
        calls();
        send(cell(3, 1, "dropped"));
        spreadsheet.getRpcProxy().refreshCellStyles();

        spreadsheet.reloadActiveSheetData();
        List<String> reloading = calls();
        send(cell(2, 1, "kept"));

        assertEquals(List.of(), reloading);
        assertEquals(List.of("B1=kept"), calls());
    }

    @Test
    void theStyles_areDrawnAgainOnce_afterTheCells() {
        spreadsheet.getRpcProxy().refreshCellStyles();
        send(cell(2, 1, "x"));
        spreadsheet.getRpcProxy().refreshCellStyles();

        assertEquals(List.of("B1=x", "refreshCellStyles"), calls());
    }

    @Test
    void aClientStartedAnew_isSentTheCellsItHadAgain() {
        send(cell(2, 1, "there"));
        calls();

        spreadsheet.onConnectorInit();
        send(cell(2, 1, "there"));

        assertEquals(List.of("B1=there"), calls());
    }
}
