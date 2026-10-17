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
 * A number that does not fit its column shows hashes, and one that fits shows
 * its value: the cells of a column are sent again when its width changes, as
 * the client has what a fresh load gives it.
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
}
