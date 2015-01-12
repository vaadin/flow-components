package com.vaadin.addon.spreadsheet;

import org.apache.poi.ss.usermodel.BorderFormatting;
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

    /**
     * Returns CSS border definitions for the given cell style
     * 
     * @param borderSide
     *            Which side the border should go
     * @param attribute
     *            What type of border style we want (solid, dashed..)
     * @param cellStyle
     *            Style for the cell
     */
    String getBorderColorCSS(BorderSide borderSide, String attribute,
            CellStyle cellStyle);

    /**
     * Returns CSS border definitions for the given conditional formatting rule
     * 
     * @param borderSide
     *            Which side the border should go
     * @param attribute
     *            What type of border style we want (solid, dashed..)
     * @param format
     *            the active formatting
     */
    String getBorderColorCSS(BorderSide borderSide, String attr,
            BorderFormatting format);

    void defaultColorStyles(CellStyle cellStyle, StringBuilder sb);

    /**
     * 
     * @param cs
     * @return Whether the given cell style has a defined background color or
     *         not.
     */
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
