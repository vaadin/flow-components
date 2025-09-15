/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import org.apache.poi.ss.util.CellReference;

/**
 * CellSet is a set of cells that also provides utilities regarding the contents
 * of the set.
 */
public class CellSet {

    private final Set<CellReference> cells;

    /**
     * Creates a set with the specified cells
     *
     * @param cells
     *            cells to construct the set with, not {@code null}
     */
    public CellSet(Set<CellReference> cells) {
        Objects.requireNonNull(cells, "Cells cannot be null");
        this.cells = cells;
    }

    /**
     * Gets an unmodifiable set of the cells
     *
     * @return an unmodifiable set of the cells
     */
    public Set<CellReference> getCells() {
        return Collections.unmodifiableSet(cells);
    }

    /**
     * Gets the number of the cells
     *
     * @return number of cells
     */
    public int size() {
        return cells.size();
    }

    /**
     * Whether the set contains the specified cell. Does not take the sheet
     * names of the cells in set into account if the sheet name of the cell
     * reference is {@code null}.
     *
     * @param cellReference
     *            cell to be checked whether it exists in the set
     * @return {@code true} if set contains the specified cell, {@code false}
     *         otherwise
     */
    public boolean contains(CellReference cellReference) {
        if (cells.isEmpty()) {
            return false;
        }
        if (cellReference.getSheetName() == null) {
            CellReference cellWithSheetName = new CellReference(
                    cells.iterator().next().getSheetName(),
                    cellReference.getRow(), cellReference.getCol(),
                    cellReference.isRowAbsolute(),
                    cellReference.isColAbsolute());
            return cells.contains(cellWithSheetName);
        }
        return cells.contains(cellReference);
    }

    /**
     * Whether the set contains the specified cell. Does not take the sheet
     * names of the cells in set into account.
     *
     * @param row
     *            row index of the cell, 0-based
     * @param col
     *            col index of the cell, 0-based
     * @return {@code true} if set contains the specified cell, {@code false}
     *         otherwise
     */
    public boolean contains(int row, int col) {
        return contains(new CellReference(row, col));
    }

    /**
     * Whether the set contains the specified cell
     *
     * @param row
     *            row index of the cell, 0-based
     * @param col
     *            col index of the cell, 0-based
     * @param sheetName
     *            sheet name of the cell, not {@code null}
     * @return {@code true} if set contains the specified cell, {@code false}
     *         otherwise
     */
    public boolean contains(int row, int col, String sheetName) {
        Objects.requireNonNull(sheetName, "The sheet name cannot be null");
        return contains(new CellReference(sheetName, row, col, false, false));
    }

    public int getCellCount() {
        return cells.size();
    }
}
