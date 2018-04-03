package com.vaadin.addon.spreadsheet;

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

import org.apache.poi.ss.usermodel.BorderFormatting;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;

/**
 * Interface for providing different color-related operations on the
 * Spreadsheet.
 * <p>
 * This interface has been created because the POI implementations differ quite
 * a lot.
 */
public interface ColorConverter extends Serializable {

    /**
     * Creates the appropriate CSS text and background style for the given cell
     * style.
     * 
     * @param cellStyle
     *            The cell style.
     * @param sb
     *            to write the styles in
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
    String getBorderColorCSS(BorderSide borderSide, String attribute,
            BorderFormatting format);

    /**
     * Writes the default background and foreground colors as CSS styles from
     * the given cell style to the given string buffer.
     * 
     * @param cellStyle
     *            The cell style
     * @param sb
     *            to write the styles in
     */
    void defaultColorStyles(CellStyle cellStyle, StringBuilder sb);

    /**
     * Returns true if the given cell style has a background color.
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
     *            Formatting rule
     * @return valid color string with semicolon or <code>null</code> if no
     *         color matches.
     */
    public String getBackgroundColorCSS(ConditionalFormattingRule rule);

    /**
     * Create a CSS color string for the font in the given rule.
     * 
     * @param rule
     *            Formatting rule
     * @return valid color string with semicolon or <code>null</code> if no
     *         color matches.
     */
    public String getFontColorCSS(ConditionalFormattingRule rule);
}
