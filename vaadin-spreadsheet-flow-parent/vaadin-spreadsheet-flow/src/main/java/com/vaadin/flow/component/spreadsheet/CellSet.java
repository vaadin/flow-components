/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.poi.ss.util.CellReference;

/**
 * CellSet is a set of cells that also provides utilities regarding the contents
 * of the set.
 * <p>
 * <strong>Internal use only. May be renamed or removed in a future
 * release.</strong>
 */
public class CellSet extends HashSet<CellReference> {

    /**
     * Creates a set with the specified cells
     *
     * @param cells
     *            cells to construct the set with, not {@code null}
     */
    public CellSet(Set<CellReference> cells) {
        super(cells);
    }

    /**
     * Whether the set contains the specified cell. Does not take the sheet
     * names of the cells in set into account if the sheet name of the cell
     * reference is {@code null}.
     *
     * @param object
     *            cell to be checked whether it exists in the set
     * @return {@code true} if set contains the specified cell, {@code false}
     *         otherwise
     */
    @Override
    public boolean contains(Object object) {
        if (isEmpty() || object == null) {
            return false;
        }
        if (object instanceof CellReference cellReference
                && cellReference.getSheetName() == null) {
            CellReference cellWithSheetName = new CellReference(
                    iterator().next().getSheetName(), cellReference.getRow(),
                    cellReference.getCol(), cellReference.isRowAbsolute(),
                    cellReference.isColAbsolute());
            return super.contains(cellWithSheetName);
        }
        return super.contains(object);
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
}
