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
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.spreadsheet.client.CellData;
import com.vaadin.flow.component.spreadsheet.rpc.SpreadsheetClientRpc;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;

/**
 * The cells {@link ClientRpcSender} is given, by any of the five methods that
 * carry them: each goes to the client the same way, as cells it does not have.
 */
class ClientRpcSenderTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    @BeforeEach
    void show() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet();
        spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        ui.dumpPendingJavaScriptInvocations();
    }

    static Stream<Arguments> cellMethods() {
        return Stream.of(Arguments.of("updateTopLeftCellValues",
                (BiConsumer<SpreadsheetClientRpc, ArrayList<CellData>>) SpreadsheetClientRpc::updateTopLeftCellValues),
                Arguments.of("updateTopRightCellValues",
                        (BiConsumer<SpreadsheetClientRpc, ArrayList<CellData>>) SpreadsheetClientRpc::updateTopRightCellValues),
                Arguments.of("updateBottomLeftCellValues",
                        (BiConsumer<SpreadsheetClientRpc, ArrayList<CellData>>) SpreadsheetClientRpc::updateBottomLeftCellValues),
                Arguments.of("updateBottomRightCellValues",
                        (BiConsumer<SpreadsheetClientRpc, ArrayList<CellData>>) SpreadsheetClientRpc::updateBottomRightCellValues),
                Arguments.of("cellsUpdated",
                        (BiConsumer<SpreadsheetClientRpc, ArrayList<CellData>>) SpreadsheetClientRpc::cellsUpdated));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cellMethods")
    void theCells_goToTheClientAsCellsItDoesNotHave(String method,
            BiConsumer<SpreadsheetClientRpc, ArrayList<CellData>> send) {
        CellData cell = new CellData();
        cell.col = 2;
        cell.row = 1;
        cell.value = "sent";

        send.accept(spreadsheet.getRpcProxy(), new ArrayList<>(List.of(cell)));

        List<String> sent = new ArrayList<>();
        for (PendingJavaScriptInvocation invocation : ui
                .dumpPendingJavaScriptInvocations()) {
            List<Object> parameters = invocation.getInvocation()
                    .getParameters();
            for (JsonNode each : TestHelper.sentJson(parameters.get(1))) {
                sent.add(parameters.get(0) + " "
                        + FreshLoadHarness.name(each.get("row").asInt(),
                                each.get("col").asInt())
                        + "=" + each.get("value").asString());
            }
        }
        assertEquals(List.of("cellsUpdated B1=sent"), sent);
    }
}
