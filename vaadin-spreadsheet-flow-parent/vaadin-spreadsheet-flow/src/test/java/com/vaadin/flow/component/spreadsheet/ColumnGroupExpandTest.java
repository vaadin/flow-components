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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Expanding a column group sends the cells of the columns it shows, of the rows
 * on the screen only. The cells of those columns in every row of the sheet were
 * sent too: 360 cells more than needed on a sheet of 200 rows.
 */
class ColumnGroupExpandTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void expandingAColumnGroup_sendsTheCellsItShows_onTheScreenOnly() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        for (int r = 0; r < 200; r++) {
            var row = sheet.createRow(r);
            for (int c = 0; c < 10; c++) {
                row.createCell(c).setCellValue(r * 10 + c);
            }
        }
        // D and E grouped, and collapsed.
        sheet.groupColumn(3, 4);
        sheet.setColumnGroupCollapsed(3, true);
        Spreadsheet spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": 1, \"firstColumn\": 1, \"lastRow\": 20,"
                        + " \"lastColumn\": 10}");
        sent();

        TestHelper.fireClientEvent(spreadsheet, "groupingCollapsed",
                "{\"cols\": true, \"colIndex\": 3, \"collapsed\": false}");

        Set<String> sent = sent();
        assertFalse(spreadsheet.isColumnHidden(3));
        assertTrue(sent.containsAll(Set.of("D1", "E1", "D20", "E20")),
                "the cells the group shows on the screen");
        assertTrue(
                sent.stream().allMatch(
                        cell -> new CellReference(cell).getRow() < 20),
                "no row past the screen: " + sent.size() + " cells sent");
    }

    /** Ends the request; the cells it sends the client, by name. */
    private Set<String> sent() {
        ui.fakeClientCommunication();
        Set<String> cells = new TreeSet<>();
        JsonMapper mapper = JsonMapper.builder().build();
        for (PendingJavaScriptInvocation invocation : ui
                .dumpPendingJavaScriptInvocations()) {
            List<Object> parameters = invocation.getInvocation()
                    .getParameters();
            if (!parameters.contains("cellsUpdated")) {
                continue;
            }
            for (Object parameter : parameters) {
                if (parameter instanceof String text && text.startsWith("[")) {
                    for (JsonNode cell : mapper.readTree(text)) {
                        cells.add(new CellReference(cell.get("row").asInt() - 1,
                                cell.get("col").asInt() - 1).formatAsString());
                    }
                }
            }
        }
        return cells;
    }
}
