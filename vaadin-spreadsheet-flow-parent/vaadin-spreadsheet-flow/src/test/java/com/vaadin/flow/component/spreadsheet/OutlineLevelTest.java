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
import java.util.stream.IntStream;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.tests.MockUIExtension;

/**
 * A level of the row outline clicked: each group of the level is collapsed,
 * wherever it starts, and the client is given the outline as it is then.
 */
class OutlineLevelTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final XSSFWorkbook workbook = new XSSFWorkbook();
    private final XSSFSheet sheet = workbook.createSheet();

    /** Twelve rows of values, but for the rows given, which are not made. */
    private void rowsBut(Integer... missing) {
        for (int r = 0; r < 12; r++) {
            if (!List.of(missing).contains(r)) {
                sheet.createRow(r).createCell(0).setCellValue(r);
            }
        }
    }

    private Spreadsheet show() {
        Spreadsheet spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        return spreadsheet;
    }

    private static List<Integer> hiddenRows(Spreadsheet spreadsheet) {
        return IntStream.range(0, 12).filter(spreadsheet::isRowHidden).boxed()
                .toList();
    }

    @Test
    void aLevelClicked_givesTheClientItsGroupsCollapsed() {
        rowsBut();
        sheet.groupRow(1, 2);
        sheet.groupRow(5, 6);
        Spreadsheet spreadsheet = show();

        spreadsheet.levelHeaderClicked(false, 1);

        assertEquals(List.of(true, true),
                spreadsheet.sheetModel().rowGroupingData.get().stream()
                        .map(group -> group.collapsed).toList());
    }

    @Test
    void groupsOfALevel_withARowMissingBetweenThem_areEachCollapsed() {
        rowsBut(3);
        sheet.groupRow(1, 2);
        sheet.groupRow(4, 5);
        Spreadsheet spreadsheet = show();

        spreadsheet.levelHeaderClicked(false, 1);

        assertEquals(List.of(1, 2, 4, 5), hiddenRows(spreadsheet));
    }

    @Test
    void groupsOfALevel_withRowsMissingBetweenThem_areEachExpanded() {
        // Nothing marks the rows between: the first group is not collapsed.
        rowsBut(3, 4);
        sheet.groupRow(1, 2);
        sheet.groupRow(5, 6);
        sheet.setRowGroupCollapsed(5, true);
        Spreadsheet spreadsheet = show();

        spreadsheet.levelHeaderClicked(false, 2);

        assertEquals(List.of(), hiddenRows(spreadsheet));
    }
}
