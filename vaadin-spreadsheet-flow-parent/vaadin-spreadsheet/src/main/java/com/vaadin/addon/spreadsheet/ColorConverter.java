package com.vaadin.addon.spreadsheet;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;

public interface ColorConverter {
    /**
     * Creates the appropriate CSS text and background style for the given cell
     * style.
     * 
     * @param style
     *            The cell style.
     * @param sb
     *            to write the styles
     */
    void colorStyles(CellStyle cellStyle, StringBuilder sb);

    void colorBorder(BorderSide borderSide, String attribute,
            CellStyle cellStyle, StringBuilder sb);

    void defaultColorStyles(CellStyle cellStyle, StringBuilder sb);

    boolean hasBackgroundColor(CellStyle cs);
}
