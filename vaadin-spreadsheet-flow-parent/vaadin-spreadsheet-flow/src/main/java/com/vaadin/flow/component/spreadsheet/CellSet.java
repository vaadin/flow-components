/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.util.HashSet;
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
     * Whether the set contains the specified cell.
     *
     * @param object
     *            cell to be checked whether it exists in the set
     * @return {@code true} if set contains the specified cell, {@code false}
     *         otherwise
     * @throws IllegalArgumentException
     *             the sheet name for the {@link CellReference} is {@code null}
     */
    @Override
    public boolean contains(Object object) {
        if (object instanceof CellReference cellReference
                && cellReference.getSheetName() == null) {
            throw new IllegalArgumentException(
                    "Sheet name in CellReference cannot be null.");
        }
        return super.contains(object);
    }
}
