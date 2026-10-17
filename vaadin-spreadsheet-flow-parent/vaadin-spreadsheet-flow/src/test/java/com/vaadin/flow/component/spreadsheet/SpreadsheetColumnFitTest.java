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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.tests.MockUIExtension;

/**
 * A number that does not fit its column shows hashes, one that fits shows its
 * value, and text runs over the empty columns shown after it: the cells of a
 * column are sent again when its width changes, as the client has what a fresh
 * load gives it.
 */
class SpreadsheetColumnFitTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    /**
     * As the client reports how wide a character of each style is, which the
     * cells need to know whether their number fits.
     */
    private static final Consumer<Spreadsheet> CONFIGURE = spreadsheet -> {
        spreadsheet.setLocale(Locale.US);
        HashMap<Integer, Float> ratios = new HashMap<>();
        ratios.put(0, 7f);
        spreadsheet.getCellValueManager().onCellStyleWidthRatioUpdate(ratios);
    };

    private final FreshLoadHarness harness = new FreshLoadHarness(ui, CONFIGURE,
            new FreshLoadHarness.Viewport(1, 1, 10, 5));

    /** B1 holds a number of six digits. */
    private static XSSFWorkbook workbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        sheet.createRow(0).createCell(1).setCellValue(123456);
        return workbook;
    }

    @Test
    void aNumberInAColumnMadeTooNarrowForIt_showsHashes() {
        harness.show(workbook());

        assertEquals(List.of(), harness
                .check(spreadsheet -> spreadsheet.setColumnWidth(1, 10)));
    }

    @Test
    void aColumnHiddenWhenLoaded_isShownWithItsNumbers() {
        XSSFWorkbook workbook = workbook();
        workbook.getSheetAt(0).setColumnHidden(1, true);
        harness.show(workbook);

        assertEquals(List.of(), harness
                .check(spreadsheet -> spreadsheet.setColumnHidden(1, false)));
    }

    @Test
    void aColumnOfAGroupCollapsedWhenLoaded_isShownWithItsNumbers() {
        XSSFWorkbook workbook = workbook();
        workbook.getSheetAt(0).groupColumn(1, 2);
        workbook.getSheetAt(0).setColumnGroupCollapsed(1, true);
        harness.show(workbook);

        assertEquals(List.of(),
                harness.check(spreadsheet -> harness.clientEvent(
                        "groupingCollapsed",
                        "{\"cols\": true, \"colIndex\": 1, \"collapsed\": false}")));
    }

    @Test
    void textRunningOverAColumnHidden_runsOverTheNextOnes() {
        // B wider than the others, so the text runs over a width of its own
        // with B hidden.
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        sheet.createRow(0).createCell(0)
                .setCellValue("a text long enough to run over some columns");
        sheet.setColumnWidth(1, SheetSizes.columnWidthUnits(sheet, 150));
        harness.show(workbook);

        assertEquals(List.of(), harness
                .check(spreadsheet -> spreadsheet.setColumnHidden(1, true)));
    }
}
