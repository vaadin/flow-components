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

/**
 * An Excel-style column group (outline) of the active sheet.
 *
 * @param firstColumn
 *            the first column of the group, 0-based
 * @param lastColumn
 *            the last column of the group, 0-based, inclusive
 * @param level
 *            the outline level of the group, 1 being the outermost level
 * @param collapsed
 *            whether the group is collapsed
 */
public record ColumnGroup(int firstColumn, int lastColumn, int level,
        boolean collapsed) implements Serializable {

    /**
     * Returns whether this group contains the given column.
     *
     * @param column
     *            the column index, 0-based
     * @return {@code true} if the column is part of this group
     */
    public boolean contains(int column) {
        return firstColumn <= column && column <= lastColumn;
    }

    boolean sameRange(ColumnGroup other) {
        return firstColumn == other.firstColumn
                && lastColumn == other.lastColumn && level == other.level;
    }
}
