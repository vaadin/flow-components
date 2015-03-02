package com.vaadin.addon.spreadsheet.client;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import java.io.Serializable;

@SuppressWarnings("serial")
public class CellData implements Serializable {

    public int row;
    public int col;
    public String value;
    public String formulaValue;
    public String cellStyle = "cs0";
    public boolean rightAlign = false;
    public boolean locked = false;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + col;
        result = prime * result + row;
        return result;
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
