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

import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * {@link SpreadsheetUtil#getFirstVisibleSheetPOIIndex}: the first sheet that is
 * neither hidden nor very hidden. It took a sheet for visible unless it was
 * both, which no sheet is, so it always gave the first sheet.
 */
class FirstVisibleSheetTest {

    private final XSSFWorkbook workbook = new XSSFWorkbook();

    @Test
    void firstVisibleSheet_isTheFirst_whenItIsShown() {
        workbook.createSheet("first");
        workbook.createSheet("second");

        assertEquals(0, SpreadsheetUtil.getFirstVisibleSheetPOIIndex(workbook));
    }

    @Test
    void firstVisibleSheet_skipsHiddenAndVeryHiddenSheets() {
        workbook.createSheet("hidden");
        workbook.createSheet("very hidden");
        workbook.createSheet("shown");
        workbook.setActiveSheet(2);
        workbook.setSheetVisibility(0, SheetVisibility.HIDDEN);
        workbook.setSheetVisibility(1, SheetVisibility.VERY_HIDDEN);

        assertEquals(2, SpreadsheetUtil.getFirstVisibleSheetPOIIndex(workbook));
    }

    @Test
    void firstVisibleSheet_isTheFirst_whenNoneIsShown() {
        workbook.createSheet("hidden");
        workbook.createSheet("very hidden");
        workbook.setSheetVisibility(0, SheetVisibility.HIDDEN);
        workbook.setSheetVisibility(1, SheetVisibility.VERY_HIDDEN);

        assertEquals(0, SpreadsheetUtil.getFirstVisibleSheetPOIIndex(workbook));
    }
}
