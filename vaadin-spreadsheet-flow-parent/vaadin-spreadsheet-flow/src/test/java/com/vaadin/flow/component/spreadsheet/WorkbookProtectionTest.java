/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.tests.MockUIExtension;

/**
 * Whether the client takes the workbook shown as protected: its structure
 * locked, so no sheet can be added, deleted, renamed or moved. It is read from
 * each workbook shown.
 */
class WorkbookProtectionTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private static XSSFWorkbook lockedXlsx() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet();
        workbook.lockStructure();
        return workbook;
    }

    private Spreadsheet show(Workbook workbook) {
        Spreadsheet spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        return spreadsheet;
    }

    private static boolean protectedOnTheClient(Spreadsheet spreadsheet) {
        return spreadsheet.getElement().getProperty("workbookProtected", false);
    }

    @Test
    void anXlsxWorkbookWithItsStructureLocked_isProtected() {
        assertTrue(protectedOnTheClient(show(lockedXlsx())));
    }

    @Test
    void anXlsWorkbookWriteProtected_isProtected() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        workbook.createSheet();
        workbook.writeProtectWorkbook("secret", "author");

        assertTrue(protectedOnTheClient(show(workbook)));
    }

    @Test
    void anXlsxWorkbookNotLocked_afterALockedOne_isNotProtected() {
        Spreadsheet spreadsheet = show(lockedXlsx());

        XSSFWorkbook open = new XSSFWorkbook();
        open.createSheet();
        spreadsheet.setWorkbook(open);

        assertFalse(protectedOnTheClient(spreadsheet));
    }
}
