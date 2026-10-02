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
import java.math.BigDecimal;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.Cell;

/**
 * Decides whether a formatted cell value fits the cell's column width given the
 * cell's style-specific character-width ratio. Used to drive scientific
 * notation fallback for numeric cells whose formatted value would overflow.
 *
 * <p>
 * Owns the {@code cellStyleIndex → characterWidthRatio} map (sent from the
 * client side as cells are measured). When the map has no entry for a style
 * index, the calculator assumes the value fits — the client will measure on the
 * next sync.
 *
 * <p>
 * The cell-width measurement (which accounts for merged-region spans) lives on
 * {@link CellValueManager#getCellWidth(Cell)} and is passed in as a parameter —
 * the calculator stays free of POI-merged-region knowledge.
 */
final class CellFitCalculator implements Serializable {

    private HashMap<Integer, Float> cellStyleWidthRatioMap;

    CellFitCalculator() {
    }

    /**
     * Replaces the width ratios with the ones the client measured.
     *
     * @return whether they differ from the ones held before, which is when the
     *         cells judged against them need rebuilding
     */
    boolean onWidthRatioUpdate(HashMap<Integer, Float> ratios) {
        // Replace, don't merge — matches the original CellValueManager
        // behavior (the field was reassigned, not putAll'd). Callers expect a
        // full overwrite per ratio-update payload from the client.
        boolean changed = !java.util.Objects.equals(cellStyleWidthRatioMap,
                ratios);
        cellStyleWidthRatioMap = ratios;
        return changed;
    }

    /**
     * {@code true} if no width-ratio entry exists yet (caller must assume fit)
     * or if the value's pixel width is within the cell's column width minus 4px
     * for left/right padding.
     */
    boolean doesValueFit(Cell cell, String value, int cellWidthPixels) {
        if (cellStyleWidthRatioMap == null) {
            return true;
        }
        Float r = cellStyleWidthRatioMap
                .get((int) cell.getCellStyle().getIndex());
        if (r == null) {
            return true;
        }
        BigDecimal ratio = BigDecimal.valueOf(r);
        BigDecimal stringPixels = ratio
                .multiply(BigDecimal.valueOf(value.length()));
        // The -4 here is for 2px cell left/right padding
        BigDecimal columnWidth = BigDecimal.valueOf(cellWidthPixels - 4d);
        return stringPixels.compareTo(columnWidth) <= 0;
    }

    /**
     * Estimated pixel width of {@code value} rendered in {@code cell}'s style,
     * or {@code -1} when the client has not reported a width ratio for that
     * style yet.
     * <p>
     * Same estimate {@link #doesValueFit} judges by, so a value that reports as
     * not fitting cannot then measure as narrow enough to stay put.
     *
     * @param cell
     *            the cell whose style sets the ratio
     * @param value
     *            the text as it will be displayed
     * @return the estimated width in pixels, or {@code -1} when unknown
     */
    int estimateWidth(Cell cell, String value) {
        if (cellStyleWidthRatioMap == null) {
            return -1;
        }
        Float ratio = cellStyleWidthRatioMap
                .get((int) cell.getCellStyle().getIndex());
        if (ratio == null) {
            return -1;
        }
        return (int) Math.ceil(BigDecimal.valueOf(ratio)
                .multiply(BigDecimal.valueOf(value.length())).doubleValue());
    }

    /**
     * Width-ratio for the given style index, or {@code null} if the client
     * hasn't reported one yet. Used by the scientific-notation branch to feed
     * {@link CellValueFormatter#getScientificNotationStringForNumericCell}.
     */
    Float getRatioForStyleIndex(int styleIndex) {
        if (cellStyleWidthRatioMap == null) {
            return null;
        }
        return cellStyleWidthRatioMap.get(styleIndex);
    }
}
