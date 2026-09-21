/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.client;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @since 23.1
 */
public class CellData implements Serializable {

    public int row;
    public int col;
    @JsonInclude
    public String value;
    public String formulaValue;
    public String originalValue;
    public String cellStyle = "cs0";
    public String textColor;
    public boolean locked = false;
    public boolean needsMeasure;
    /**
     * Width in pixels to give this cell's text so it runs into the empty cells
     * to its right, or {@code 0} when it fits inside its own column.
     * <p>
     * A spreadsheet does not clip text at the column edge; it carries on across
     * empty neighbours and stops at the first one holding a value. The server
     * works out how far because it is the side that knows what the neighbours
     * hold. See {@code CellOverflowCalculator}.
     */
    public int overflowWidth;
    public boolean isPercentage;

    @Override
    public int hashCode() {
        int factor = (row + ((col + 1) / 2));
        return 31 * (col + (factor * factor));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CellData other = (CellData) obj;
        if (col != other.col) {
            return false;
        }
        if (row != other.row) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("r").append(row).append("c")
                .append(col).append(cellStyle).append("tc").append(textColor)
                .append("|").append(value).toString();
    }
}
