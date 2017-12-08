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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderFormatting;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.DifferentialStyleProvider;
import org.apache.poi.ss.usermodel.FontFormatting;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;

/**
 * IncrementalStyleBuilder converts a POI {@link DifferentialStyleProvider} (conditional format rule or table style element) to CSS.
 * note: Create a new instance if the workbook changes, since the colorConverter field relies on the workbook theme
 */
public class IncrementalStyleBuilder implements Serializable {

    /**
     * default
     */
    private static final long serialVersionUID = 1L;

    /**
     * used to generate unique CSS style IDs for the different types of styles needed
     */
    static enum StyleType {
        BOTTOM, RIGHT, TOP, LEFT, HORIZONTAL, // table internal row bottom border
        VERTICAL, // table internal column right border
        BASE,;

        public int offsetFromBase() {
            return BASE.ordinal() - this.ordinal();
        }
    }

    /*
     * Slight hack. This style is used when a CF rule defines 'no border', in
     * which case the border should be empty. However, since we use cell DIV
     * borders for the grid structure, empty borders are in fact grey. So, if
     * one rule says red, and the next says no border, then we need to know what
     * 'no border' means in CSS. Of course, if the default CSS changes, this
     * needs to change too, typically by creating a new instance with the updated value.
     */
    private String defaultBorderStyleCSS;

    private final Spreadsheet ss;

    private final ColorConverter colorConverter;

    private DifferentialStyleProvider styleProvider;
    private int baseCSSIndex;
    private StringBuilder css;

    /**
     * @param ss
     */
    public IncrementalStyleBuilder(Spreadsheet ss, String defaultBorderCSS) {
        this.ss = ss;
        this.defaultBorderStyleCSS = defaultBorderCSS;
        if (ss.getWorkbook() instanceof HSSFWorkbook) {
            colorConverter = new HSSFColorConverter((HSSFWorkbook) ss.getWorkbook());
        } else {
            colorConverter = new XSSFColorConverter((XSSFWorkbook) ss.getWorkbook());
        }
    }

    private ColorConverter getColorConverter() {
        return colorConverter;
    }

    /**
     * @param provider
     * @param baseIndex border indexes are based off this, which should be at least 3 digits different than any other kind of style index
     */
    public void addStyleForRule(DifferentialStyleProvider provider, int baseIndex) {
        this.styleProvider = provider;
        this.baseCSSIndex = baseIndex;
        css = new StringBuilder();

        FontFormatting fontFormatting = provider.getFontFormatting();

        if (fontFormatting != null) {
            String fontColorCSS = getColorConverter().getFontColorCSS(provider);
            if (fontColorCSS != null) {
                css.append("color:" + fontColorCSS);
            }

            // we can't have both underline and line-through in the same
            // DIV element, so use the first one that matches.

            // HSSF might return 255 for 'none'...
            if (fontFormatting.getUnderlineType() != FontFormatting.U_NONE
                    && fontFormatting.getUnderlineType() != 255) {
                css.append("text-decoration: underline;");
            }
            if (fontFormatting.isStruckout()) {
                css.append("text-decoration: line-through;");
            }

            if (fontFormatting.getFontHeight() != -1) {
                // Excel stores height in 1/20th points, convert
                int fontHeight = fontFormatting.getFontHeight() / 20;
                css.append("font-size:" + fontHeight + "pt;");
            }

            // excel has a setting for bold italic, otherwise bold
            // overrides
            // italic and vice versa
            if (fontFormatting.isItalic() && fontFormatting.isBold()) {
                css.append("font-style: italic;");
                css.append("font-weight: bold;");
            } else if (fontFormatting.isItalic()) {
                css.append("font-style: italic;");
                css.append("font-weight: initial;");
            } else if (fontFormatting.isBold()) {
                css.append("font-style: normal;");
                css.append("font-weight: bold;");
            }
        }

        PatternFormatting patternFormatting = provider.getPatternFormatting();
        if (patternFormatting != null) {
            String colorCSS = getColorConverter().getBackgroundColorCSS(provider);

            if (colorCSS != null) {
                css.append("background-color:" + colorCSS);
            }
        }

        addBorderFormatting();

        addCssToComponentState(baseCSSIndex, css);
    }

    private void addBorderFormatting() {
        BorderFormatting borderFormatting = styleProvider.getBorderFormatting();

        if (borderFormatting == null)
            return;

        BorderStyle borderLeft = borderFormatting.getBorderLeftEnum();
        BorderStyle borderRight = borderFormatting.getBorderRightEnum();
        BorderStyle borderTop = borderFormatting.getBorderTopEnum();
        BorderStyle borderBottom = borderFormatting.getBorderBottomEnum();
        // for range styles, like tables, these are the internal grid lines, bottom and left
        BorderStyle borderHorizontal = borderFormatting.getBorderHorizontalEnum();
        BorderStyle borderVertical = borderFormatting.getBorderVerticalEnum();

        // In Excel, we can set a border to 'none', which overrides previous
        // rules. Default is 'not set', in which case we add no CSS.
        boolean isLeftSet = borderLeft != BorderStyle.NONE;
        boolean isTopSet = borderTop != BorderStyle.NONE;
        boolean isRightSet = borderRight != BorderStyle.NONE;
        boolean isBottomSet = borderBottom != BorderStyle.NONE;
        boolean isHorizontalSet = borderHorizontal != BorderStyle.NONE;
        boolean isVerticalSet = borderVertical != BorderStyle.NONE;

        // bottom/right/horizontal/vertical go on the cell itself but are defined as their own styles for flexibility - for table styles, not all cells get all styles
        if (isRightSet) {
            final StringBuilder sb2 = new StringBuilder("border-right:");
            if (borderRight != BorderStyle.NONE) {
                sb2.append(getBorderStyleCss(borderRight));
                sb2.append(getColorConverter()
                        .getBorderColorCSS(BorderSide.RIGHT, "border-right-color", borderFormatting));

                addCssToComponentState(baseCSSIndex - IncrementalStyleBuilder.StyleType.RIGHT.offsetFromBase(), sb2);
            } else {
                css.append(defaultBorderStyleCSS);
            }
        }
        if (isBottomSet) {
            final StringBuilder sb2 = new StringBuilder("border-bottom:");
            if (borderBottom != BorderStyle.NONE) {
                sb2.append(getBorderStyleCss(borderBottom));
                sb2.append(getColorConverter()
                        .getBorderColorCSS(BorderSide.BOTTOM, "border-bottom-color", borderFormatting));

                addCssToComponentState(baseCSSIndex - IncrementalStyleBuilder.StyleType.BOTTOM.offsetFromBase(), sb2);
            } else {
                css.append(defaultBorderStyleCSS);
            }
        }
        if (isVerticalSet) {
            final StringBuilder sb2 = new StringBuilder("border-right:");
            if (borderVertical != BorderStyle.NONE) {
                sb2.append(getBorderStyleCss(borderVertical));
                sb2.append(getColorConverter()
                        .getBorderColorCSS(BorderSide.RIGHT, "border-right-color", borderFormatting));

                addCssToComponentState(baseCSSIndex - IncrementalStyleBuilder.StyleType.VERTICAL.offsetFromBase(), sb2);
            } else {
                css.append(defaultBorderStyleCSS);
            }
        }
        if (isHorizontalSet) {
            final StringBuilder sb2 = new StringBuilder("border-bottom:");
            if (borderHorizontal != BorderStyle.NONE) {
                sb2.append(getBorderStyleCss(borderHorizontal));
                sb2.append(getColorConverter().getBorderColorCSS("border-bottom-color",
                        XSSFColor.toXSSFColor(borderFormatting.getHorizontalBorderColorColor())));

                addCssToComponentState(baseCSSIndex - IncrementalStyleBuilder.StyleType.HORIZONTAL.offsetFromBase(),
                        sb2);
            } else {
                css.append(defaultBorderStyleCSS);
            }
        }

        // top and left borders might be applied to another cell, so store
        // them with a different index
        if (isTopSet) {
            // bottom border for cell above
            final StringBuilder sb2 = new StringBuilder("border-bottom:");
            if (borderTop != BorderStyle.NONE) {
                sb2.append(getBorderStyleCss(borderTop));
                sb2.append(
                        getColorConverter().getBorderColorCSS(BorderSide.TOP, "border-bottom-color", borderFormatting));

                addCssToComponentState(baseCSSIndex - IncrementalStyleBuilder.StyleType.TOP.offsetFromBase(), sb2);
            } else {
                css.append(defaultBorderStyleCSS);
            }
        }

        if (isLeftSet) {
            // right border for cell to the left
            final StringBuilder sb2 = new StringBuilder("border-right:");
            if (borderLeft != BorderStyle.NONE) {
                sb2.append(getBorderStyleCss(borderLeft));
                sb2.append(
                        getColorConverter().getBorderColorCSS(BorderSide.LEFT, "border-right-color", borderFormatting));

                addCssToComponentState(baseCSSIndex - IncrementalStyleBuilder.StyleType.LEFT.offsetFromBase(), sb2);
            } else {
                css.append(defaultBorderStyleCSS);
            }
        }
    }

    /**
     * @param border
     * @return border style CSS string
     */
    protected String getBorderStyleCss(BorderStyle border) {
        return SpreadsheetStyleFactory.BORDER.get(border).getBorderAttributeValue();
    }

    /**
     * @param index
     * @param cssString - may not be the main CSS, may be a distinct border CSS for a neighboring cell
     */
    protected void addCssToComponentState(int index, StringBuilder cssString) {
        ss.getState().conditionalFormattingStyles.put(new Integer(index), cssString.toString());
    }

}
