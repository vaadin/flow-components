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

import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The tooltips of the links in the cells shown: in the frozen corner, the
 * frozen rows and columns and the rest of the sheet, from a link's tooltip, or
 * its address, or where a HYPERLINK formula goes.
 */
class SpreadsheetHyperlinkTooltipsTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Workbook workbook = new XSSFWorkbook();
    private Sheet sheet = workbook.createSheet();
    private Spreadsheet spreadsheet;

    private Cell cell(String name) {
        CellReference reference = new CellReference(name);
        Row row = sheet.getRow(reference.getRow());
        if (row == null) {
            row = sheet.createRow(reference.getRow());
        }
        return row.createCell(reference.getCol());
    }

    private void link(String name, String address, String tooltip) {
        Hyperlink link = workbook.getCreationHelper()
                .createHyperlink(HyperlinkType.URL);
        link.setAddress(address);
        if (tooltip != null) {
            ((XSSFHyperlink) link).setTooltip(tooltip);
        }
        Cell cell = cell(name);
        cell.setCellValue(name);
        cell.setHyperlink(link);
    }

    /**
     * Shows the sheet frozen at two rows and two columns, as the client shows
     * rows 10-20 of columns E-H.
     */
    private void show() {
        sheet.createFreezePane(2, 2);
        spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        showRows(10, 20);
    }

    private void showRows(int first, int last) {
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": " + first
                        + ", \"firstColumn\": 5, \"lastRow\": " + last
                        + ", \"lastColumn\": 8}");
    }

    /** The tooltips the client has, by cell name. */
    private Map<String, String> tooltips() {
        Map<String, String> tooltips = new TreeMap<>();
        JsonNode sent = JsonMapper.builder().build().readTree(
                spreadsheet.getElement().getProperty("hyperlinksTooltips"));
        sent.properties().forEach(tooltip -> tooltips.put(
                new CellReference(
                        SpreadsheetUtil.getRowFromKey(tooltip.getKey()) - 1,
                        SpreadsheetUtil.getColumnIndexFromKey(tooltip.getKey())
                                - 1)
                        .formatAsString(),
                tooltip.getValue().asString()));
        return tooltips;
    }

    @Test
    void theLinksShown_haveTheirTooltips_inEveryPane() {
        link("A1", "https://corner", "Corner");
        link("F1", "https://frozen-rows", null);
        link("A12", "https://frozen-columns", "Frozen columns");
        link("F12", "https://scrolled", null);
        link("C5", "https://not-shown", "Not shown");

        show();

        assertEquals(
                Map.of("A1", "Corner", "A12", "Frozen columns", "F1",
                        "https://frozen-rows", "F12", "https://scrolled"),
                tooltips());
    }

    @Test
    void aLinkOfAnOldWorkbook_showsItsAddress() {
        workbook = new HSSFWorkbook();
        sheet = workbook.createSheet();
        Hyperlink link = workbook.getCreationHelper()
                .createHyperlink(HyperlinkType.URL);
        link.setAddress("https://old");
        cell("F12").setHyperlink(link);

        show();

        assertEquals(Map.of("F12", "https://old"), tooltips());
    }

    @Test
    void aHyperlinkFormula_showsWhereItGoes() {
        cell("F12").setCellFormula("HYPERLINK(\"https://formula\",\"go\")");

        show();

        assertEquals(Map.of("F12", "https://formula"), tooltips());
    }

    @Test
    void aHyperlinkFormula_hasNoTooltip_withoutAClickHandler() {
        cell("F12").setCellFormula("HYPERLINK(\"https://formula\",\"go\")");
        sheet.createFreezePane(2, 2);
        spreadsheet = new Spreadsheet(workbook);
        spreadsheet.setHyperlinkCellClickHandler(null);
        ui.add(spreadsheet);

        showRows(10, 20);

        assertEquals(Map.of(), tooltips());
    }

    @Test
    void theLinksScrolledAway_loseTheirTooltips() {
        link("F12", "https://scrolled", null);
        link("F32", "https://further", null);
        show();

        showRows(30, 40);

        assertEquals(Map.of("F32", "https://further"), tooltips());
    }
}
