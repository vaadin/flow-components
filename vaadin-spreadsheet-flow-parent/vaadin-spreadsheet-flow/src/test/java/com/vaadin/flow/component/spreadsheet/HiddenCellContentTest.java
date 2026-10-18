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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * A cell whose style hides it, on a protected sheet, keeps what it holds from
 * the client: neither its formula nor its value before formatting is sent, and
 * the client shows only the value the cell displays. Without the protection, or
 * without the hidden style, the formula goes to the client as before.
 */
class HiddenCellContentTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    /**
     * A sheet whose A1 holds 2, B1 the formula A1*21, and C1 the number
     * 0.123456789, which its format shows as 0.12; B1 and C1 wear the given
     * style hidden or not.
     */
    private static XSSFWorkbook workbook(boolean hidden, boolean protect) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Data");
        CellStyle style = workbook.createCellStyle();
        style.setHidden(hidden);
        style.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
        XSSFRow row = sheet.createRow(0);
        row.createCell(0).setCellValue(2);
        row.createCell(1).setCellFormula("A1*21");
        row.getCell(1).setCellStyle(style);
        row.createCell(2).setCellValue(0.123456789);
        row.getCell(2).setCellStyle(style);
        if (protect) {
            sheet.protectSheet("");
        }
        return workbook;
    }

    /** Opens the workbook; returns everything the client was sent. */
    private String sentToTheClient(XSSFWorkbook workbook) {
        spreadsheet = new Spreadsheet(workbook);
        // The values below are formatted as in this locale.
        spreadsheet.setLocale(Locale.US);
        ui.add(spreadsheet);
        List<String> sent = new ArrayList<>(collect());
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": 1, \"firstColumn\": 1, \"lastRow\": 20,"
                        + " \"lastColumn\": 10}");
        sent.addAll(collect());
        spreadsheet.setSelection("B1");
        sent.addAll(collect());
        return String.join("\n", sent);
    }

    /** Ends the request; returns its client calls and the properties set. */
    private List<String> collect() {
        ui.fakeClientCommunication();
        List<String> sent = new ArrayList<>();
        for (PendingJavaScriptInvocation invocation : ui
                .dumpPendingJavaScriptInvocations()) {
            for (Object parameter : invocation.getInvocation()
                    .getParameters()) {
                sent.add(String.valueOf(parameter));
            }
        }
        spreadsheet.getElement().getPropertyNames().forEach(name -> sent.add(
                name + "=" + spreadsheet.getElement().getPropertyRaw(name)));
        return sent;
    }

    @Test
    void theFormulaOfAHiddenCellOfAProtectedSheet_isNotSent() {
        String sent = sentToTheClient(workbook(true, true));

        assertFalse(sent.contains("A1*21"), sent);
        assertTrue(sent.contains("\"value\":\"42.00\""), sent);
    }

    @Test
    void theUnformattedValueOfAHiddenCellOfAProtectedSheet_isNotSent() {
        String sent = sentToTheClient(workbook(true, true));

        assertFalse(sent.contains("0.123456789"), sent);
        assertTrue(sent.contains("\"value\":\"0.12\""), sent);
    }

    @Test
    void theFormulaOfAHiddenCellOfASheetNotProtected_isSent() {
        assertTrue(sentToTheClient(workbook(true, false)).contains("A1*21"));
    }

    @Test
    void theFormulaOfACellNotHidden_isSent() {
        assertTrue(sentToTheClient(workbook(false, true)).contains("A1*21"));
    }

    @Test
    void theFormulaOfAHiddenCellMarkedInvalid_isNotSent() {
        XSSFWorkbook workbook = workbook(true, true);
        XSSFSheet sheet = workbook.getSheet("Data");
        CellStyle hidden = sheet.getRow(0).getCell(1).getCellStyle();
        // As a formula the user typed and POI could not read is kept.
        sheet.getRow(0).createCell(3).setCellValue("=SECRET(A1");
        sheet.getRow(0).getCell(3).setCellStyle(hidden);
        spreadsheet = new Spreadsheet(workbook);
        spreadsheet.markInvalidFormula(4, 1);
        ui.add(spreadsheet);
        collect();
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": 1, \"firstColumn\": 1, \"lastRow\": 20,"
                        + " \"lastColumn\": 10}");

        String sent = String.join("\n", collect());

        assertFalse(sent.contains("SECRET"), sent);
    }
}
