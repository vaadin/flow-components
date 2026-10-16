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
            // POI's measuring of text can fail where fonts are missing: the
            // column keeps its width.
            LOGGER.warn("Could not autofit column {}", columnIndex + 1, e);
            return;
        }
        int width = withRoomForFilterButton(columnIndex,
                (int) activeSheet.getColumnWidthInPixels(columnIndex));
        autofittedColumnWidths.put(new CellReference(activeSheet.getSheetName(),
                0, columnIndex, true, true), width);
        // Through the component, so the workbook holds the width the client
        // is given, the overlays over the column are sized again, and an
        // override applies.
        spreadsheet.setColumnWidth(columnIndex, width);
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
     * The width, with room for the button of a filter in the column, which is
     * in the header row of its table, so the button does not cover the text.
     */
    private int withRoomForFilterButton(int columnIndex, int width) {
        for (SpreadsheetTable table : spreadsheet.getTablesForActiveSheet()) {
            if (table instanceof SpreadsheetFilterTable
                    && table.getPopupButton(columnIndex) != null) {
                return width + FILTER_BUTTON_PIXEL_WIDTH
                        + FILTER_BUTTON_PIXEL_PADDING;
            }
        }
        return width;
    }
}
