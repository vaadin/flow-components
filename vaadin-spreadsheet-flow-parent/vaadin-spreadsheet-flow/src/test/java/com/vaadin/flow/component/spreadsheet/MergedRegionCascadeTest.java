/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.component.spreadsheet.client.MergedRegion;

/**
 * The style and cache updates {@link MergedRegionCascade} makes after a merge
 * or unmerge.
 *
 * <p>
 * Pins the post-merge / post-unmerge side-effect cascades. Both methods walk a
 * (row1..row2+1) x (col1..col2+1) area — one row and one column wider than the
 * merge itself so that the cells just past the bottom-right corner get their
 * border styles re-rendered.
 *
 * <p>
 * Uses a real {@link XSSFWorkbook} for the sheet (the cascade iterates POI's
 * row / cell containers; mocking the iterators would obscure the bounds
 * arithmetic). Mocks the parent {@link Spreadsheet} +
 * {@link SpreadsheetStyleFactory} + {@link CellValueManager} so the outgoing
 * side effects can be counted exactly.
 */
class MergedRegionCascadeTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private SpreadsheetStyleFactory styleFactory;
    @Mock
    private CellValueManager valueManager;

    private XSSFWorkbook workbook;
    private Sheet sheet;
    private MergedRegionCascade cascade;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet();
        when(spreadsheet.getActiveSheet()).thenReturn(sheet);
        when(spreadsheet.getSpreadsheetStyleFactory()).thenReturn(styleFactory);
        when(spreadsheet.getCellValueManager()).thenReturn(valueManager);
        cascade = new MergedRegionCascade(spreadsheet);
    }

    @AfterEach
    void tearDown() throws Exception {
        workbook.close();
    }

    private MergedRegion region(int c1, int r1, int c2, int r2) {
        return new MergedRegion(c1, r1, c2, r2);
    }

    /** Populate a dense (col1..col2+1) x (row1..row2+1) block with values. */
    private void fillBlock(int col1, int row1, int col2, int row2) {
        for (int r = row1; r <= row2 + 1; r++) {
            Row row = sheet.createRow(r - 1);
            for (int c = col1; c <= col2 + 1; c++) {
                row.createCell(c - 1).setCellValue("v");
            }
        }
    }

    // ----- afterMerge -------------------------------------------------------

    @Test
    void afterMerge_2x2region_styleUpdatesAll9CascadeCells() {
        // Pin the wider-by-one cascade area. A 2x2 merge at (1,1)-(2,2)
        // iterates (col 1..3) x (row 1..3) = 9 cells; if all 9 exist, all
        // 9 get cellStyleUpdated.
        fillBlock(1, 1, 2, 2);

        cascade.afterMerge(region(1, 1, 2, 2));

        verify(styleFactory, times(9)).cellStyleUpdated(any(Cell.class),
                eq(false));
    }

    @Test
    void afterMerge_marksHiddenCells_butNotAnchor_andNotOutside() {
        // Pin: markCellForRemove fires for every cell INSIDE the merge that
        // isn't the top-left anchor. For a 2x2 at (1,1)-(2,2):
        // - (1,1) anchor → NOT removed
        // - (1,2), (2,1), (2,2) inside-but-not-anchor → REMOVED
        // - (3,*), (*,3) outside cascade area → NOT removed
        // Total markCellForRemove calls = 3.
        fillBlock(1, 1, 2, 2);

        cascade.afterMerge(region(1, 1, 2, 2));

        verify(valueManager, times(3)).markCellForRemove(any(Cell.class));
    }

    @Test
    void afterMerge_singleCellRegion_marksZeroForRemove() {
        // Edge case pinned: a 1x1 "merge" has only the anchor inside the
        // merge → zero hidden cells → zero markCellForRemove calls. The
        // cascade still walks a 2x2 area (because of the +1) so all
        // existing cells get cellStyleUpdated.
        fillBlock(1, 1, 1, 1);

        cascade.afterMerge(region(1, 1, 1, 1));

        verify(valueManager, never()).markCellForRemove(any(Cell.class));
        verify(styleFactory, times(4)).cellStyleUpdated(any(Cell.class),
                eq(false));
    }

    @Test
    void afterMerge_missingRow_skipsThatRow() {
        // Pin the null-row guard: rows that don't exist on the sheet are
        // skipped silently. Only row 1 exists; the cascade walks rows 1-3
        // but only row 1's existing cells trigger the side effects.
        Row row = sheet.createRow(0); // row 1
        row.createCell(0).setCellValue("v");
        row.createCell(1).setCellValue("v");
        // rows 2 and 3 not created.

        cascade.afterMerge(region(1, 1, 1, 2));

        // Only the two cells in row 1 trigger updates.
        verify(styleFactory, times(2)).cellStyleUpdated(any(Cell.class),
                eq(false));
        // No removal — the only "inside-non-anchor" cell would have been at
        // row 2, which doesn't exist.
        verify(valueManager, never()).markCellForRemove(any(Cell.class));
    }

    @Test
    void afterMerge_missingCellInRow_skipsThatCell() {
        // Pin the null-cell guard within an existing row.
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("v");
        // skip column 2 (POI index 1)
        row.createCell(2).setCellValue("v");

        cascade.afterMerge(region(1, 1, 2, 1));

        // Two existing cells in the cascade area → 2 style updates.
        verify(styleFactory, times(2)).cellStyleUpdated(any(Cell.class),
                eq(false));
    }

    @Test
    void afterMerge_loadCustomBorderStyles_firesOnce() {
        // Pin the terminal call: regardless of merge size or sparse cells,
        // loadCustomBorderStylesToState fires exactly once at the end.
        fillBlock(1, 1, 2, 2);
        cascade.afterMerge(region(1, 1, 2, 2));
        verify(styleFactory, times(1)).loadCustomBorderStylesToState();
    }

    // ----- afterUnmerge -----------------------------------------------------

    @Test
    void afterUnmerge_existingCells_getStyleUpdatedAndMarkedForUpdate() {
        // Pin: every existing cell in the cascade area receives BOTH a
        // style-update AND a markCellForUpdate. The asymmetry vs afterMerge
        // (which only marks the inside-non-anchor cells) is intentional —
        // unmerging makes ALL the previously-hidden cells visible again.
        fillBlock(1, 1, 2, 2);

        cascade.afterUnmerge(region(1, 1, 2, 2));

        verify(styleFactory, times(9)).cellStyleUpdated(any(Cell.class),
                eq(false));
        verify(valueManager, times(9)).markCellForUpdate(any(Cell.class));
    }

    @Test
    void afterUnmerge_missingCell_clearsStyleByCoordinatesInstead() {
        // Pin the dual-mode branch within an existing row: cells that don't
        // exist on the sheet get clearCellStyle(row, col) called with the
        // 0-based coordinates the method takes. The method DOES NOT skip — it
        // explicitly
        // clears the cached style for that coordinate. This matters because
        // the merge may have stored style state for a cell that never had
        // a POI Cell object. (The row-level skip is asserted separately;
        // here both rows in the cascade exist so neither row triggers the
        // continue-the-loop branch.)
        sheet.createRow(0).createCell(0).setCellValue("anchor"); // (c=1,r=1)
        sheet.createRow(1).createCell(0).setCellValue("below"); // (c=1,r=2)

        cascade.afterUnmerge(region(1, 1, 1, 1));

        // 2 cellStyleUpdated for the two existing cells, 2 clearCellStyle
        // for the missing (c=2,r=1) and (c=2,r=2).
        verify(styleFactory, times(2)).cellStyleUpdated(any(Cell.class),
                eq(false));
        verify(styleFactory).clearCellStyle(0, 1);
        verify(styleFactory).clearCellStyle(1, 1);
        verify(valueManager, times(2)).markCellForUpdate(any(Cell.class));
    }

    @Test
    void afterUnmerge_missingRow_skippedEntirely() {
        // Pin: a null row aborts both the cellStyleUpdated AND the
        // clearCellStyle path for that row — the row's cells are NOT
        // synthesized into clearCellStyle calls. Different behavior from
        // the in-row missing-cell branch which DOES synthesize coordinates.
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("anchor");

        cascade.afterUnmerge(region(1, 1, 1, 1));

        // Row 2 is missing → no clearCellStyle for row 2 (otherwise
        // there would be 2 + 3 = 5 total calls instead of 3).
        verify(styleFactory, never()).clearCellStyle(eq(2), anyInt());
    }

    @Test
    void afterUnmerge_loadCustomBorderStyles_firesOnce() {
        fillBlock(1, 1, 2, 2);
        cascade.afterUnmerge(region(1, 1, 2, 2));
        verify(styleFactory, times(1)).loadCustomBorderStylesToState();
    }

    @Test
    void neitherCascade_callsUpdateCustomBorders_perCell() {
        // Pin a subtle invariant: cellStyleUpdated is always called with
        // updateCustomBorders=FALSE in both cascades. The per-cell loop
        // never triggers a per-cell custom-border refresh; the terminal
        // loadCustomBorderStylesToState handles that once for the whole
        // cascade. Avoids quadratic redo.
        fillBlock(1, 1, 1, 1);
        cascade.afterMerge(region(1, 1, 1, 1));
        cascade.afterUnmerge(region(1, 1, 1, 1));

        verify(styleFactory, never()).cellStyleUpdated(any(Cell.class),
                eq(true));
    }
}
