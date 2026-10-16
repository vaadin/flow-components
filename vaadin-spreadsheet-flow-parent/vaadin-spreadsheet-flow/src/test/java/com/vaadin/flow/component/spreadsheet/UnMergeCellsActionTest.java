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

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.vaadin.flow.component.spreadsheet.Spreadsheet.SelectionChangeEvent;
import com.vaadin.flow.component.spreadsheet.action.UnMergeCellsAction;
import com.vaadin.flow.component.spreadsheet.client.MergedRegion;

/**
 * The context menu's Unmerge cells: it removes the merged region of the
 * selected cell, from POI and from the client, and leaves the others.
 */
class UnMergeCellsActionTest {

    private static final List<String> REGIONS = List.of("A1:B2", "D4:E5",
            "G1:H1");

    private Spreadsheet spreadsheet;
    private Sheet sheet;

    private void open(List<String> regions) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        sheet = workbook.createSheet();
        for (String region : regions) {
            // Unchecked: POI lets overlapping regions in this way, and some
            // files have them.
            sheet.addMergedRegionUnsafe(CellRangeAddress.valueOf(region));
        }
        spreadsheet = new Spreadsheet(workbook);
    }

    private void unmerge(String selectedRegion) {
        CellRangeAddress region = CellRangeAddress.valueOf(selectedRegion);
        new UnMergeCellsAction().executeActionOnSelection(spreadsheet,
                new SelectionChangeEvent(spreadsheet,
                        new CellReference(region.getFirstRow(),
                                region.getFirstColumn()),
                        List.of(), region, List.of()));
    }

    private List<String> poiRegions() {
        return sheet.getMergedRegions().stream()
                .map(CellRangeAddress::formatAsString).toList();
    }

    private List<String> clientRegions() {
        List<String> regions = new ArrayList<>();
        for (MergedRegion region : spreadsheet.getMergedRegions()) {
            // 1-based
            regions.add(new CellRangeAddress(region.row1 - 1, region.row2 - 1,
                    region.col1 - 1, region.col2 - 1).formatAsString());
        }
        return regions;
    }

    @ParameterizedTest
    @ValueSource(strings = { "A1:B2", "D4:E5", "G1:H1" })
    void unmerging_removesTheSelectedCellsRegionOnly(String selected) {
        open(REGIONS);
        List<String> left = new ArrayList<>(REGIONS);
        left.remove(selected);

        unmerge(selected);

        assertEquals(left, poiRegions());
        assertEquals(left, clientRegions());
    }

    @Test
    void unmerging_removesEveryRegionStartingAtTheCell() {
        open(List.of("A1:B2", "D4:E5", "G1:H1", "D4:F6"));

        unmerge("D4:E5");

        assertEquals(List.of("A1:B2", "G1:H1"), poiRegions());
        assertEquals(List.of("A1:B2", "G1:H1"), clientRegions());
    }
}
