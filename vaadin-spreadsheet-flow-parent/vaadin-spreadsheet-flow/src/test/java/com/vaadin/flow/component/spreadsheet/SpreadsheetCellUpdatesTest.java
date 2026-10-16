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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * What the client is sent when cells change: the cells changed, the formulas it
 * shows worked out again, the cells whose conditional formatting changes, each
 * once, and their styles after them.
 */
class SpreadsheetCellUpdatesTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final XSSFWorkbook workbook = new XSSFWorkbook();
    private final Sheet sheet = workbook.createSheet();
    private Spreadsheet spreadsheet;

    /** A1 holds 1, and C1 is text. */
    @BeforeEach
    void init() {
        sheet.createRow(0).createCell(0).setCellValue(1);
        sheet.getRow(0).createCell(2).setCellValue("text");
    }

    /** B1 is twice A1. */
    private void formulaInB1() {
        sheet.getRow(0).createCell(1).setCellFormula("A1*2");
    }

    /** Shows the sheet, as the client reports rows 1-20 of columns A-J. */
    private void show() {
        spreadsheet = new Spreadsheet(workbook);
        spreadsheet.setLocale(Locale.US);
        ui.add(spreadsheet);
        showRows(1, 20);
        calls();
    }

    private void showRows(int first, int last) {
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": " + first
                        + ", \"firstColumn\": 1, \"lastRow\": " + last
                        + ", \"lastColumn\": 10}");
    }

    private Cell cell(String name) {
        CellReference reference = new CellReference(name);
        return sheet.getRow(reference.getRow()).getCell(reference.getCol());
    }

    /** The calls that send cells: those changed, and those shown. */
    private static final List<String> CELL_CALLS = List.of("cellsUpdated",
            "updateTopLeftCellValues", "updateTopRightCellValues",
            "updateBottomLeftCellValues", "updateBottomRightCellValues");

    /**
     * The client calls since the last look: the cells sent as "B1=2", or "B1=2
     * cs0 cf0" with their styles, the other calls by name.
     */
    private List<String> calls() {
        return calls(false);
    }

    private List<String> calls(boolean withStyles) {
        ui.fakeClientCommunication();
        List<String> calls = new ArrayList<>();
        JsonMapper mapper = JsonMapper.builder().build();
        for (PendingJavaScriptInvocation invocation : ui
                .dumpPendingJavaScriptInvocations()) {
            List<Object> parameters = invocation.getInvocation()
                    .getParameters();
            if (parameters.contains("refreshCellStyles")) {
                calls.add("refreshCellStyles");
            }
            if (CELL_CALLS.stream().noneMatch(parameters::contains)) {
                continue;
            }
            for (Object parameter : parameters) {
                if (parameter instanceof String text && text.startsWith("[")) {
                    for (JsonNode cell : mapper.readTree(text)) {
                        String sent = new CellReference(
                                cell.get("row").asInt() - 1,
                                cell.get("col").asInt() - 1).formatAsString()
                                + "=" + cell.path("value").asString("");
                        calls.add(
                                withStyles
                                        ? sent + " "
                                                + cell.path("cellStyle")
                                                        .asString()
                                        : sent);
                    }
                }
            }
        }
        return calls;
    }

    @Test
    void aCellChanged_isSentOnce_followedByTheStyles() {
        show();
        cell("C1").setCellValue("new");

        spreadsheet.refreshCells(cell("C1"));

        assertEquals(List.of("C1=new", "refreshCellStyles"), calls());
    }

    @Test
    void aCellAdded_isSentOnce() {
        show();

        spreadsheet.refreshCells(spreadsheet.createCell(0, 4, "added"));

        assertEquals(List.of("E1=added", "refreshCellStyles"), calls());
    }

    @Test
    void nothingChanged_sendsNothing() {
        show();

        spreadsheet.updateMarkedCells();

        assertEquals(List.of(), calls());
    }

    @Test
    void aCellSent_isNotSentAgainWhenTheClientShowsItAgain() {
        show();
        cell("C1").setCellValue("new");
        spreadsheet.refreshCells(cell("C1"));
        calls();

        showRows(50, 70);
        showRows(1, 20);

        assertTrue(calls().stream().noneMatch(sent -> sent.startsWith("C1=")));
    }

    @Test
    void aFormulaShown_isSentAgainWhenWhatItUsesChanges() {
        formulaInB1();
        show();
        cell("A1").setCellValue(5);

        spreadsheet.refreshCells(cell("A1"));

        assertEquals(List.of("A1=5", "B1=10", "refreshCellStyles"), calls());
    }

    @Test
    void aFormulaAdded_isSent_andKeptUpToDateFromThen() {
        show();
        Cell formula = spreadsheet.createFormulaCell(0, 3, "A1*3");
        spreadsheet.refreshCells(formula);
        assertEquals(1, calls().stream().filter("D1=3"::equals).count());

        cell("A1").setCellValue(5);
        spreadsheet.refreshCells(cell("A1"));

        assertTrue(calls().contains("D1=15"));
    }

    @Test
    void aCellFormattedOnAnotherCell_isSentWhenItsFormattingChanges() {
        SheetConditionalFormatting formatting = sheet
                .getSheetConditionalFormatting();
        ConditionalFormattingRule overFive = formatting
                .createConditionalFormattingRule("$A$1>5");
        overFive.createFontFormatting()
                .setFontColorIndex(IndexedColors.RED.getIndex());
        formatting.addConditionalFormatting(
                new CellRangeAddress[] { CellRangeAddress.valueOf("C1") },
                overFive);
        show();
        cell("A1").setCellValue(10);

        spreadsheet.refreshCells(cell("A1"));

        assertTrue(calls(true).stream().anyMatch(
                sent -> sent.startsWith("C1=text ") && sent.contains(" cf")));
    }

    @Test
    void aCellWhoseTextRunsOn_isSentOnce_whenACellItRunsTowardsChangesWithIt() {
        cell("C1").setCellValue("text long enough to run on past its column");
        show();
        // As the client has measured a character of each style.
        HashMap<Integer, Float> widths = new HashMap<>();
        for (int style = 0; style < workbook.getNumCellStyles(); style++) {
            widths.put(style, 10f);
        }
        spreadsheet.getCellValueManager().onCellStyleWidthRatioUpdate(widths);
        calls();
        // C1 runs on into D1 still, up to E1.
        Cell next = sheet.getRow(0).createCell(4);
        next.setCellValue("x");
        cell("C1").setCellValue("still long enough to run on past its column");

        spreadsheet.refreshCells(cell("C1"), next);

        assertEquals(1, calls().stream().filter(sent -> sent.startsWith("C1="))
                .count());
    }
}
