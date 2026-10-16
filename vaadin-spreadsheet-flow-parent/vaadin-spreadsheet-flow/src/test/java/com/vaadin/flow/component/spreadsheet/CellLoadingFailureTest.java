/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.rpc.SpreadsheetClientRpc;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * A failure while the cells shown are sent is not hidden: the cells would
 * silently stay off the client.
 */
class CellLoadingFailureTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void aFailureSendingTheCellsShown_goesThrough() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet().createRow(0).createCell(0).setCellValue("x");
        SpreadsheetClientRpc rpc = mock(SpreadsheetClientRpc.class);
        NullPointerException thrown = new NullPointerException("broken");
        doThrow(thrown).when(rpc).updateBottomRightCellValues(any());
        Spreadsheet spreadsheet = new Spreadsheet(workbook) {
            @Override
            protected SpreadsheetClientRpc getRpcProxy() {
                return rpc;
            }
        };
        ui.add(spreadsheet);
        ui.fakeClientCommunication();

        assertSame(thrown, assertThrows(NullPointerException.class,
                () -> TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                        "{\"firstRow\": 1, \"firstColumn\": 1, \"lastRow\": 20, "
                                + "\"lastColumn\": 10}")));
    }
}
