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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Copying a selection. The client copies the cells it has, and asks the server
 * for the text of a range it does not have: the selected cells are no longer
 * sent to it in case they are copied, as they were - one column of 100,000 rows
 * sent 10 MB, and selecting everything kept the server busy for seconds.
 */
class ClipboardCopyTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet();
        for (int row = 0; row < 100; row++) {
            spreadsheet.createCell(row, 0, "a" + (row + 1));
            spreadsheet.createCell(row, 1, (double) (row + 1));
        }
        ui.add(spreadsheet);
        // The client shows rows 1-30.
        ui.fakeClientCommunication();
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": 1, \"firstColumn\": 1, \"lastRow\": 30, \"lastColumn\": 10}");
        rowsSent();
    }

    /** The rows of the cells sent to the client since the last look. */
    private Set<Integer> rowsSent() {
        ui.fakeClientCommunication();
        Set<Integer> rows = new HashSet<>();
        JsonMapper mapper = JsonMapper.builder().build();
        for (PendingJavaScriptInvocation invocation : ui
                .dumpPendingJavaScriptInvocations()) {
            for (Object parameter : invocation.getInvocation()
                    .getParameters()) {
                if (parameter instanceof String text && text.startsWith("[{")) {
                    for (JsonNode cell : mapper.readTree(text)) {
                        if (cell.has("row") && cell.has("col")) {
                            rows.add(cell.get("row").asInt());
                        }
                    }
                }
            }
        }
        return rows;
    }

    @Test
    void selectingAColumn_sendsNoneOfItsCellsPastThoseShown() {
        TestHelper.fireClientEvent(spreadsheet, "columnSelected",
                "{\"column\": 1, \"firstRowIndex\": 1}");

        Set<Integer> rows = rowsSent();

        assertTrue(rows.stream().allMatch(row -> row <= 30),
                "rows sent: " + rows);
    }

    @Test
    void theSelectedRange_isCopiedAsItsCellsAreShown() {
        spreadsheet.setSelection("A1:B2");

        assertEquals("a1\t1\na2\t2", spreadsheet.clipboardText(1, 1, 2, 2));
    }

    @Test
    void aSelectedColumn_isCopiedPastTheRowsShown() {
        TestHelper.fireClientEvent(spreadsheet, "columnSelected",
                "{\"column\": 2, \"firstRowIndex\": 1}");

        String[] lines = spreadsheet
                .clipboardText(2, 1, 2, spreadsheet.getRows()).split("\n", -1);

        assertEquals(spreadsheet.getRows(), lines.length);
        assertEquals("100", lines[99]);
        assertEquals("", lines[100]);
    }

    @Test
    void theSelectedCell_isCopied() {
        spreadsheet.setSelection("B3");

        assertEquals("3", spreadsheet.clipboardText(2, 3, 2, 3));
    }

    @Test
    void theMergedRegionOfTheSelectedCell_isCopied() {
        spreadsheet.createCell(0, 3, "m");
        spreadsheet.addMergedRegion("D1:E2");
        spreadsheet.setSelection("D1");

        assertEquals("m\t\n\t", spreadsheet.clipboardText(4, 1, 5, 2));
    }

    @Test
    void aRangeNotSelected_isNotCopied() {
        spreadsheet.setSelection("A1:B2");

        assertNull(spreadsheet.clipboardText(1, 1, 2, 3));
        assertNull(spreadsheet.clipboardText(3, 3, 3, 3));
    }

    @Test
    void aRangeTheWrongWayRound_isNotCopied() {
        spreadsheet.setSelection("A1:B2");

        assertNull(spreadsheet.clipboardText(2, 1, 1, 2));
        assertNull(spreadsheet.clipboardText(1, 2, 2, 1));
    }

    @Test
    void withNoCellSelected_nothingIsCopied() {
        // As while another sheet is being shown.
        spreadsheet.getCellSelectionManager().clear();

        assertNull(spreadsheet.clipboardText(1, 1, 1, 1));
    }

    @Test
    void numbers_areCopiedAsTheirFormatShowsThem() {
        spreadsheet.setLocale(Locale.US);
        Workbook workbook = spreadsheet.getWorkbook();
        CellStyle twoDecimals = workbook.createCellStyle();
        twoDecimals
                .setDataFormat(workbook.createDataFormat().getFormat("0.00"));
        spreadsheet.createCell(0, 2, 1.5).setCellStyle(twoDecimals);
        spreadsheet.setSelection("C1");

        assertEquals("1.50", spreadsheet.clipboardText(3, 1, 3, 1));
    }

    @Test
    void tabsLineBreaksAndQuotes_areQuoted() {
        spreadsheet.createCell(0, 2, "say \"hi\"");
        spreadsheet.setSelection("C1");

        assertEquals("\"say \"\"hi\"\"\"",
                spreadsheet.clipboardText(3, 1, 3, 1));
    }
}
