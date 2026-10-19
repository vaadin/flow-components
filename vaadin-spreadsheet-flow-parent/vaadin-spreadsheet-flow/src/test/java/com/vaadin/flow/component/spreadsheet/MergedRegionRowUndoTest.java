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
import static org.mockito.ArgumentMatchers.anyInt;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.spreadsheet.client.MergedRegion;
import com.vaadin.flow.component.spreadsheet.command.RowInsertOrDeleteCommand;

/**
 * What happens to merged regions when a row is deleted from the context menu
 * and the delete is undone.
 */
class MergedRegionRowUndoTest {

    private Spreadsheet spreadsheet;
    private Sheet sheet;

    @BeforeEach
    void init() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        sheet = workbook.createSheet();
        for (String region : List.of("A1:B1", "A3:B3", "D3:E3", "A6:C6")) {
            sheet.addMergedRegion(CellRangeAddress.valueOf(region));
        }
        spreadsheet = new Spreadsheet(workbook);
    }

    private List<String> poiRegions() {
        return sheet.getMergedRegions().stream()
                .map(CellRangeAddress::formatAsString).sorted().toList();
    }

    private List<String> clientRegions() {
        List<String> regions = new ArrayList<>();
        for (MergedRegion region : spreadsheet.sheetModel().mergedRegions
                .get()) {
            // 1-based
            regions.add(new CellRangeAddress(region.row1 - 1, region.row2 - 1,
                    region.col1 - 1, region.col2 - 1).formatAsString());
        }
        return regions.stream().sorted().toList();
    }

    @Test
    void rowDeleted_theRegionsOnItAreGone_theOthersMoveUp() {
        new RowInsertOrDeleteCommand(spreadsheet,
                new CellRangeAddress(2, 2, 0, 0)).execute();

        assertEquals(List.of("A1:B1", "A5:C5"), poiRegions());
        assertEquals(List.of("A1:B1", "A5:C5"), clientRegions());
    }

    @Test
    void rowDeletedAndUndone_theRegionsOnItComeBack() {
        RowInsertOrDeleteCommand command = new RowInsertOrDeleteCommand(
                spreadsheet, new CellRangeAddress(2, 2, 0, 0));
        command.execute();

        command.execute();

        List<String> all = List.of("A1:B1", "A3:B3", "A6:C6", "D3:E3");
        assertEquals(all, poiRegions());
        assertEquals(all, clientRegions());
    }

    @Test
    void rowDeleted_poisRegionsAreReadAllAtOnce() {
        // XSSF finds a region by its index by going through those before it.
        Sheet spied = Mockito.spy(new XSSFWorkbook().createSheet());
        for (String region : List.of("A1:B1", "A3:B3", "D3:E3")) {
            spied.addMergedRegion(CellRangeAddress.valueOf(region));
        }
        Mockito.clearInvocations(spied);
        Spreadsheet mocked = Mockito.mock(Spreadsheet.class);
        Mockito.when(mocked.getActiveSheet()).thenReturn(spied);

        new RowInsertOrDeleteCommand(mocked, new CellRangeAddress(2, 2, 0, 0))
                .execute();

        Mockito.verify(spied, Mockito.never()).getMergedRegion(anyInt());
    }
}
