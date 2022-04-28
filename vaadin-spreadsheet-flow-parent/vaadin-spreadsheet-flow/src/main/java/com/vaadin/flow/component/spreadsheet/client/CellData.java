package com.vaadin.flow.component.spreadsheet.client;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import java.io.Serializable;

@SuppressWarnings("serial")
public class CellData implements Serializable {

    public int row;
    public int col;
    public String value;
    public String formulaValue;
    public String originalValue;
    public String cellStyle = "cs0";
    public boolean locked = false;
    public boolean needsMeasure;
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
                .append(col).append(cellStyle).append("|").append(value)
                .toString();
    }
}
