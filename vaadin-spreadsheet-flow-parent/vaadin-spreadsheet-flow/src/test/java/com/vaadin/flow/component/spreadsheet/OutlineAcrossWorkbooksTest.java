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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.dom.Element;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;

/**
 * What the client is left of a sheet's outline when another workbook is shown:
 * nothing. The outline is read only from an XLSX sheet, so an XLS workbook
 * shown after one with groups kept their levels and buttons on screen; main did
 * the same.
 */
class OutlineAcrossWorkbooksTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private static void fill(Sheet sheet) {
        for (int r = 0; r < 6; r++) {
            Row row = sheet.createRow(r);
            for (int c = 0; c < 6; c++) {
                row.createCell(c).setCellValue(r * 10 + c);
            }
        }
    }

    private static Spreadsheet grouped(MockUIExtension ui) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        fill(sheet);
        sheet.groupRow(1, 3);
        sheet.groupColumn(1, 3);
        Spreadsheet spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        return spreadsheet;
    }

    private static boolean isJsonNull(Element element, String property) {
        Object raw = element.getPropertyRaw(property);
        return raw == null || raw instanceof JsonNode json && json.isNull();
    }

    @Test
    void anXlsWorkbook_afterOneWithGroups_leavesNoOutline() {
        Spreadsheet spreadsheet = grouped(ui);
        Element element = spreadsheet.getElement();
        assertEquals(1.0, element.getProperty("colGroupingMax", -1.0),
                "the groups shown first");

        HSSFWorkbook plain = new HSSFWorkbook();
        fill(plain.createSheet());
        spreadsheet.setWorkbook(plain);

        assertEquals(0.0, element.getProperty("colGroupingMax", -1.0));
        assertEquals(0.0, element.getProperty("rowGroupingMax", -1.0));
        assertTrue(isJsonNull(element, "colGroupingData"), "column groups");
        assertTrue(isJsonNull(element, "rowGroupingData"), "row groups");
    }

    @Test
    void anXlsxWorkbook_withoutGroups_leavesNoOutline() {
        Spreadsheet spreadsheet = grouped(ui);
        Element element = spreadsheet.getElement();

        XSSFWorkbook plain = new XSSFWorkbook();
        fill(plain.createSheet());
        spreadsheet.setWorkbook(plain);

        assertEquals(0.0, element.getProperty("colGroupingMax", -1.0));
        assertEquals(0.0, element.getProperty("rowGroupingMax", -1.0));
    }
}
