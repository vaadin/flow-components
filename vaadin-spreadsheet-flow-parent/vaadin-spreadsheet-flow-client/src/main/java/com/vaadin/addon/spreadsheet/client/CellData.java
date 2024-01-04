/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.addon.spreadsheet.client;

import java.io.Serializable;

import jsinterop.annotations.JsType;

@SuppressWarnings("serial")
@JsType(namespace = "Vaadin.Spreadsheet")
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
