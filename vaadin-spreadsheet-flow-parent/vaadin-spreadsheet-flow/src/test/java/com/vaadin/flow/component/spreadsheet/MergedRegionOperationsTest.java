/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.vaadin.flow.dom.Element;

/**
 * Reading POI's merged regions all at once. XSSF finds a region by its index by
 * going through those before it: read one by one, 20,000 regions took 0.7 s to
 * show, and merging cells among them 3.5 s.
 * <p>
 * What merging and unmerging do is in {@link SpreadsheetMergedRegionsTest}.
 */
class MergedRegionOperationsTest {

    private final Sheet sheet = Mockito.spy(new XSSFWorkbook().createSheet());
    private final SheetClientModel model = new SheetClientModel(
            new Element("vaadin-spreadsheet"));
    private MergedRegionOperations operations;

    @BeforeEach
    void init() {
        Spreadsheet spreadsheet = Mockito.mock(Spreadsheet.class,
                Mockito.RETURNS_DEEP_STUBS);
        when(spreadsheet.getActiveSheet()).thenReturn(sheet);
        when(spreadsheet.sheetModel()).thenReturn(model);
        for (String region : List.of("C3:D4", "A1:B2", "F10:F12")) {
            sheet.addMergedRegion(CellRangeAddress.valueOf(region));
        }
        operations = new MergedRegionOperations(spreadsheet);
        operations.load();
        Mockito.clearInvocations(sheet);
    }

    @Test
    void loading_readsPoisRegionsAllAtOnce() {
        operations.load();

        verify(sheet, never()).getMergedRegion(anyInt());
        verify(sheet, times(1)).getMergedRegions();
    }

    @Test
    void reloading_readsPoisRegionsAllAtOnce() {
        operations.reload();

        verify(sheet, never()).getMergedRegion(anyInt());
        verify(sheet, times(1)).getMergedRegions();
    }

    @Test
    void merging_readsPoisRegionsAllAtOnce() {
        operations.addRegion(CellRangeAddress.valueOf("A1:G12"));

        // POI itself reads them all once more, to check the new region.
        verify(sheet, never()).getMergedRegion(anyInt());
    }
}
