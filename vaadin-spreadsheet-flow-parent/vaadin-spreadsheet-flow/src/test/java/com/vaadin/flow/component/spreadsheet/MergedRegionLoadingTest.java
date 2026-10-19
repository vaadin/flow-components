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

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.vaadin.flow.component.spreadsheet.client.MergedRegion;

/**
 * The merged regions the client is given when a sheet is shown, or when they
 * are read from POI again: those of POI, in POI's order, each with an id of its
 * own.
 */
class MergedRegionLoadingTest {

    private Workbook workbook;
    private Sheet sheet;
    private Spreadsheet spreadsheet;

    private void open(String format, String... regions) {
        workbook = "xls".equals(format) ? new HSSFWorkbook()
                : new XSSFWorkbook();
        sheet = sheetWith(regions);
        spreadsheet = new Spreadsheet(workbook);
    }

    private Sheet sheetWith(String... regions) {
        Sheet added = workbook.createSheet();
        for (String region : regions) {
            added.addMergedRegion(CellRangeAddress.valueOf(region));
        }
        return added;
    }

    /** The regions the client has, written as POI writes them. */
    private List<String> clientRegions() {
        List<String> regions = new ArrayList<>();
        for (MergedRegion region : spreadsheet.sheetModel().mergedRegions
                .get()) {
            // 1-based
            regions.add(new CellRangeAddress(region.row1 - 1, region.row2 - 1,
                    region.col1 - 1, region.col2 - 1).formatAsString());
        }
        return regions;
    }

    private List<Integer> ids() {
        return spreadsheet.sheetModel().mergedRegions.get().stream()
                .map(region -> region.id).toList();
    }

    @ParameterizedTest
    @ValueSource(strings = { "xlsx", "xls" })
    void aSheetShown_givesTheClientItsRegionsInPoisOrder(String format) {
        open(format, "C3:D4", "A1:B2", "F10:F12");

        assertEquals(List.of("C3:D4", "A1:B2", "F10:F12"), clientRegions());
        assertEquals(List.of(0, 1, 2), ids());
    }

    @ParameterizedTest
    @ValueSource(strings = { "xlsx", "xls" })
    void aSheetWithoutRegions_givesTheClientNone(String format) {
        open(format);

        assertNull(spreadsheet.sheetModel().mergedRegions.get());
    }

    @ParameterizedTest
    @ValueSource(strings = { "xlsx", "xls" })
    void anotherSheetShown_givesTheClientItsRegions_numberedAfresh(
            String format) {
        workbook = "xls".equals(format) ? new HSSFWorkbook()
                : new XSSFWorkbook();
        sheetWith("A1:B2");
        sheetWith("D4:E5", "A1:A3");
        spreadsheet = new Spreadsheet(workbook);

        spreadsheet.setActiveSheetIndex(1);

        assertEquals(List.of("D4:E5", "A1:A3"), clientRegions());
        assertEquals(List.of(0, 1), ids());
    }

    @ParameterizedTest
    @ValueSource(strings = { "xlsx", "xls" })
    void reloadAllMergedRegions_followsPoi_numberingTheRegionsAfresh(
            String format) {
        open(format, "C3:D4", "A1:B2", "F10:F12");
        sheet.removeMergedRegion(0);
        sheet.addMergedRegion(CellRangeAddress.valueOf("H1:H2"));

        spreadsheet.reloadAllMergedRegions();

        assertEquals(List.of("A1:B2", "F10:F12", "H1:H2"), clientRegions());
        assertEquals(List.of(0, 1, 2), ids());
    }

    @ParameterizedTest
    @ValueSource(strings = { "xlsx", "xls" })
    void reloadAllMergedRegions_noneLeftInPoi_givesTheClientNone(
            String format) {
        open(format, "A1:B2");
        sheet.removeMergedRegion(0);

        spreadsheet.reloadAllMergedRegions();

        assertNull(spreadsheet.sheetModel().mergedRegions.get());
    }

    @ParameterizedTest
    @ValueSource(strings = { "xlsx", "xls" })
    void updateMergedRegions_followsPoi_numberingOnFromTheLastId(
            String format) {
        // What rows moved or deleted leave in POI.
        open(format, "C3:D4", "A1:B2", "F10:F12");
        sheet.removeMergedRegion(0);
        sheet.addMergedRegion(CellRangeAddress.valueOf("H1:H2"));

        spreadsheet.updateMergedRegions();

        assertEquals(List.of("A1:B2", "F10:F12", "H1:H2"), clientRegions());
        assertEquals(List.of(3, 4, 5), ids());
    }

    @ParameterizedTest
    @ValueSource(strings = { "xlsx", "xls" })
    void updateMergedRegions_noneLeftInPoi_givesTheClientNone(String format) {
        open(format, "A1:B2");
        sheet.removeMergedRegion(0);

        spreadsheet.updateMergedRegions();

        assertNull(spreadsheet.sheetModel().mergedRegions.get());
    }
}
