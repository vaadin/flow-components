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

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.framework.Action;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * The context menu opened on the cells: what the action handlers do is theirs,
 * failures included.
 */
class SpreadsheetContextMenuTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    @BeforeEach
    void show() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet();
        spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        ui.fakeClientCommunication();
    }

    @Test
    void whatAnActionHandlerThrows_goesThrough() {
        IllegalStateException thrown = new IllegalStateException("theirs");
        spreadsheet.addActionHandler(new Action.Handler() {
            @Override
            public Action[] getActions(Object target, Object sender) {
                throw thrown;
            }

            @Override
            public void handleAction(Action action, Object sender,
                    Object target) {
            }
        });

        assertSame(thrown,
                assertThrows(IllegalStateException.class,
                        () -> TestHelper.fireClientEvent(spreadsheet,
                                "contextMenuOpenOnSelection",
                                "{\"row\": 2, \"column\": 2}")));
    }
}
