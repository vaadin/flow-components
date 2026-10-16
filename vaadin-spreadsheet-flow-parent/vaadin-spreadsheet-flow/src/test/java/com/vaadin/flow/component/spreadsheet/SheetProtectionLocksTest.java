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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.tests.MockUIExtension;

/**
 * What the protection of the sheet shown locks on the client: resizing its
 * columns and its rows, as the sheet is loaded, protected, or left.
 */
class SheetProtectionLocksTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    private void show(Workbook workbook) {
        spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        ui.fakeClientCommunication();
    }

    /** Whether resizing columns and rows is locked, as the client has it. */
    private List<Boolean> locks() {
        return List.of(
                spreadsheet.getElement().getProperty("lockFormatColumns",
                        false),
                spreadsheet.getElement().getProperty("lockFormatRows", false));
    }

    private static XSSFWorkbook twoSheets() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet("First");
        workbook.createSheet("Second");
        return workbook;
    }

    @Test
    void aSheetProtectedInTheWorkbook_locksResizing() {
        XSSFWorkbook workbook = twoSheets();
        workbook.getSheet("First").protectSheet("");

        show(workbook);

        assertEquals(List.of(true, true), locks());
    }

    @Test
    void aProtectionThatAllowsResizingColumns_locksOnlyRows() {
        XSSFWorkbook workbook = twoSheets();
        XSSFSheet first = workbook.getSheet("First");
        first.protectSheet("");
        first.getCTWorksheet().getSheetProtection().setFormatColumns(false);

        show(workbook);

        assertEquals(List.of(false, true), locks());
    }

    @Test
    void aProtectionThatAllowsResizingRows_locksOnlyColumns() {
        XSSFWorkbook workbook = twoSheets();
        XSSFSheet first = workbook.getSheet("First");
        first.protectSheet("");
        first.getCTWorksheet().getSheetProtection().setFormatRows(false);

        show(workbook);

        assertEquals(List.of(true, false), locks());
    }

    @Test
    void aProtectionThatDoesNotProtect_locksNothing() {
        XSSFWorkbook workbook = twoSheets();
        // Protection settings kept for a sheet whose protection is off
        workbook.getSheet("First").getCTWorksheet().addNewSheetProtection();

        show(workbook);

        assertEquals(List.of(false, false), locks());
    }

    @Test
    void protectingTheSheetShown_locksResizing() {
        show(twoSheets());

        spreadsheet.setActiveSheetProtected("");

        assertEquals(List.of(true, true), locks());
    }

    @Test
    void takingTheProtectionOff_unlocksResizing() {
        show(twoSheets());
        spreadsheet.setActiveSheetProtected("");

        spreadsheet.setActiveSheetProtected(null);

        assertEquals(List.of(false, false), locks());
    }

    @Test
    void protectingASheetNotShown_locksNothingShown() {
        show(twoSheets());

        spreadsheet.setSheetProtected(1, "");

        assertEquals(List.of(false, false), locks());
    }

    @Test
    void aSheetShownAfterAProtectedOne_isNotLocked() {
        XSSFWorkbook workbook = twoSheets();
        workbook.getSheet("First").protectSheet("");
        show(workbook);

        spreadsheet.setActiveSheetIndex(1);

        assertEquals(List.of(false, false), locks());
    }

    @Test
    void anOldWorkbooksProtectedSheet_locksNoResizing() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        workbook.createSheet("First").protectSheet("");

        show(workbook);

        assertEquals(List.of(false, false), locks());
    }
}
