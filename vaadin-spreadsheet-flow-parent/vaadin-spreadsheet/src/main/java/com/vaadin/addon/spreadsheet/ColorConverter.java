package com.vaadin.addon.spreadsheet;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;

/**
 * Interface for providing different color-related operations on the
 * Spreadsheet.
 * <p>
 * Is an interface because POI implementations differ quite a lot.
 */
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

    /**
     * Create a CSS color string for the background in the given rule.
     * 
     * @param rule
     * @return valid color string with semicolon or <code>null</code> if no
     *         color matches.
     */
    public String getBackgroundColorCSS(ConditionalFormattingRule rule);

    /**
     * Create a CSS color string for the font in the given rule.
     * 
     * @param rule
     * @return valid color string with semicolon or <code>null</code> if no
     *         color matches.
     */
    public String getFontColorCSS(ConditionalFormattingRule rule);
}
