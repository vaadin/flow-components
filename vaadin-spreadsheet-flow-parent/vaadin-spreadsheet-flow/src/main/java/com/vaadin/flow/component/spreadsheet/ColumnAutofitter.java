/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.spreadsheet.command.SizeChangeCommand;
import com.vaadin.flow.component.spreadsheet.command.SizeChangeCommand.Type;

/**
 * Runs POI's {@code Sheet.autoSizeColumn} on a column and applies the resulting
 * pixel width. Adds padding for filter-popup-button columns so the autofit
 * doesn't truncate the button itself.
 *
 * <p>
 * Owns the per-Spreadsheet cache of autofitted widths keyed by absolute
 * {@code CellReference} (anchored at the column's row 0). The cache is a
 * {@link WeakHashMap} so cell references held only by the cache don't prevent
 * GC.
 */
final class ColumnAutofitter implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory
            .getLogger(ColumnAutofitter.class);

    /** Pixel width of the filter popup button. */
    private static final int FILTER_BUTTON_PIXEL_WIDTH = 14;
    /**
     * Extra padding (in pixels) to add between the filter popup button and the
     * cell text when autofitting a column.
     */
    private static final int FILTER_BUTTON_PIXEL_PADDING = 2;

    private final Spreadsheet spreadsheet;
    private final Map<CellReference, Integer> autofittedColumnWidths = new WeakHashMap<>();

    ColumnAutofitter(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    void onClientAutofit(int columnIndex) {
        SizeChangeCommand command = new SizeChangeCommand(spreadsheet,
                Type.COLUMN);
        command.captureValues(new Integer[] { columnIndex + 1 });
        // Through the component, so an application that overrides
        // autofitColumn sees the user's double click too.
        spreadsheet.autofitColumn(columnIndex);
        spreadsheet.getSpreadsheetHistoryManager().addCommand(command);
    }

    void autofit(int columnIndex) {
        Sheet activeSheet = spreadsheet.getActiveSheet();
        try {
            activeSheet.autoSizeColumn(columnIndex);
        } catch (NullPointerException e) {
            // NullPointerException is being thrown in POI. Catch to prevent
            // breaking the UI.
            LOGGER.trace(
                    "Poi threw NullPointerException when trying to autofit column",
                    e);
            return;
        }
        int columnPixelWidth = computePixelWidthWithFilterPadding(columnIndex,
                (int) activeSheet.getColumnWidthInPixels(columnIndex));

        int[] colW = ColumnSizeOperations.widthsCovering(spreadsheet,
                columnIndex);
        colW[columnIndex] = columnPixelWidth;
        spreadsheet.setColW(colW);

        spreadsheet.getCellValueManager().clearCacheForColumn(columnIndex + 1);
        spreadsheet.getCellValueManager().loadCellData(
                spreadsheet.getFirstRow(), columnIndex + 1,
                spreadsheet.getLastRow(), columnIndex + 1);
        spreadsheet.getCellValueManager().resendTextRunningInto(columnIndex);

        if (spreadsheet.sheetOverlayManager.hasOverlays()) {
            spreadsheet.sheetOverlayManager.markReloadFromPoi();
            spreadsheet.sheetOverlayManager.loadOrUpdate();
        }
    }

    /**
     * For every column that previously autofitted in the given filter table,
     * re-autofits the column iff its current width still matches the
     * last-recorded autofit width (i.e. the user hasn't manually resized in the
     * meantime).
     */
    void refreshAutofittedColumnsInTable(SpreadsheetFilterTable table) {
        Sheet filteredSheet = table.getSheet();
        org.apache.poi.ss.util.CellRangeAddress fullTableRegion = table
                .getFullTableRegion();
        int firstColumn = fullTableRegion.getFirstColumn();
        int lastColumn = fullTableRegion.getLastColumn();
        for (int i = firstColumn; i <= lastColumn; i++) {
            CellReference cr = new CellReference(filteredSheet.getSheetName(),
                    0, i, true, true);
            if (!autofittedColumnWidths.containsKey(cr)) {
                continue;
            }
            Integer autofittedWidth = autofittedColumnWidths.get(cr);
            int currentWidth = (int) filteredSheet
                    .getColumnWidthInPixels(cr.getCol());
            if (currentWidth == autofittedWidth) {
                spreadsheet.autofitColumn(cr.getCol());
            }
        }
    }

    /**
     * Filter columns reserve extra width so the popup button doesn't truncate
     * when the column autofits to its content.
     */
    private int computePixelWidthWithFilterPadding(int columnIndex,
            int autofitWidth) {
        List<SpreadsheetTable> tablesForActiveSheet = spreadsheet
                .getTablesForActiveSheet();
        CellReference cr = new CellReference(
                spreadsheet.getActiveSheet().getSheetName(), 0, columnIndex,
                true, true);
        autofittedColumnWidths.put(cr, autofitWidth);
        for (SpreadsheetTable st : tablesForActiveSheet) {
            if (!(st instanceof SpreadsheetFilterTable)) {
                continue;
            }
            SpreadsheetFilterTable ft = (SpreadsheetFilterTable) st;
            PopupButton popupButton = ft.getPopupButton(cr);
            if (popupButton != null) {
                return autofitWidth + FILTER_BUTTON_PIXEL_WIDTH
                        + FILTER_BUTTON_PIXEL_PADDING;
            }
        }
        return autofitWidth;
    }
}
