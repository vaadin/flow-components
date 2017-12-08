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

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.BorderFormatting;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.DifferentialStyleProvider;
import org.apache.poi.ss.usermodel.FontFormatting;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.xssf.model.ThemesTable;
import org.apache.poi.xssf.usermodel.XSSFBorderFormatting;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;

/**
 * Color converter implementation for the current Excel file type (.xlsx or XSSF
 * in POI terms).
 * 
 * @author Vaadin Ltd.
 * @since 1.0
 */
@SuppressWarnings("serial")
public class XSSFColorConverter implements ColorConverter {

    private static final Logger LOGGER = Logger
            .getLogger(XSSFColorConverter.class.getName());

    private String defaultBackgroundColor;
    private String defaultColor;
    private XSSFWorkbook workbook;

    public XSSFColorConverter(XSSFWorkbook workbook) {
        this.workbook = workbook;
        workbook.getTheme();
    }

    @Override
    public void colorStyles(CellStyle cellStyle, StringBuilder sb) {
        XSSFCellStyle cs = (XSSFCellStyle) cellStyle;
        XSSFColor fillBackgroundXSSFColor = cs.getFillBackgroundXSSFColor();
        XSSFColor fillForegroundXSSFColor = cs.getFillForegroundXSSFColor();
        String backgroundColor = null;
        if (fillForegroundXSSFColor != null
                && !fillForegroundXSSFColor.isAuto()) {
            backgroundColor = styleColor(fillForegroundXSSFColor);
        } else if (fillBackgroundXSSFColor != null
                && !fillBackgroundXSSFColor.isAuto()) {
            backgroundColor = styleColor(fillBackgroundXSSFColor);
        } else {
            // bypass POI API and try to get the fill ourself, because of bug:
            // https://issues.apache.org/bugzilla/show_bug.cgi?id=53262
            try {
                XSSFColor themeColor = getFillColor(cs);
                if (themeColor != null && !themeColor.isAuto()) {
                    backgroundColor = styleColor(themeColor);
                }
            } catch (Exception e) {
                LOGGER.log(Level.FINE, e.getMessage(), e);
            }
        }

        if (backgroundColor != null
                && !backgroundColor.equals(defaultBackgroundColor)) {
            sb.append("background-color:");
            sb.append(backgroundColor);
        }

        XSSFColor xssfColor = cs.getFont().getXSSFColor();
        if (xssfColor != null) {
            String color = styleColor(xssfColor);
            if (color != null && !color.equals(defaultColor)) {
                sb.append("color:");
                sb.append(color);
            }
        }
    }

    @Override
    public String getBorderColorCSS(BorderSide borderSide, String attr,
            CellStyle cellStyle) {

        StringBuilder sb = new StringBuilder();

        XSSFColor color;
        if (cellStyle instanceof XSSFCellStyle
                && !((XSSFCellStyle) cellStyle).getCoreXf().getApplyBorder()) {
            // ApplyBorder is not working for Excel themes, so need to get the
            // color manually
            color = getBorderColor((XSSFCellStyle) cellStyle, borderSide);
        } else {
            color = ((XSSFCellStyle) cellStyle).getBorderColor(borderSide);
        }

        sb.append(attr);
        sb.append(":");

        if (color == null || color.isAuto() || (color.isIndexed() && color.getIndex() == HSSFColorPredefined.AUTOMATIC.getIndex())) {
            sb.append("#000;");
            return sb.toString();
        }

        if (color.isIndexed() && ColorConverterUtil
            .hasCustomIndexedColors(workbook)) {
            sb.append(ColorConverterUtil.getIndexedARGB(workbook,color));
            return sb.toString();
        }

        byte[] argb = color.getARGB();
        if (argb == null) {
            sb.append("#000;");
            return sb.toString();
        }

        final double tint = color.getTint();
        if (tint != 0.0) {
            argb[1] = applyTint(argb[1] & 0xFF, tint);
            argb[2] = applyTint(argb[2] & 0xFF, tint);
            argb[3] = applyTint(argb[3] & 0xFF, tint);
        }

        try {
            String temp = ColorConverterUtil.toRGBA(argb);
            sb.append(temp);
        } catch (NumberFormatException nfe) {
            LOGGER.log(Level.FINE, nfe.getMessage() + " " + nfe.getCause(), nfe);
            sb.append(String
                    .format("#%02x%02x%02x;", argb[1], argb[2], argb[3]));
        }

        return sb.toString();
    }

    public String getBorderColorCSS(String attr, Color colorInstance) {
        final XSSFColor color = (XSSFColor) colorInstance;

        StringBuilder sb = new StringBuilder();

        sb.append(attr);
        sb.append(":");

        if (color == null || color.isAuto()) {
            sb.append("#000;");
            return sb.toString();
        }

        byte[] argb = color.getRGB();

        if (argb == null) {
            sb.append("#000;");
            return sb.toString();
        }

        final double tint = color.getTint();
        if (tint != 0.0) {
            argb[1] = applyTint(argb[1] & 0xFF, tint);
            argb[2] = applyTint(argb[2] & 0xFF, tint);
            argb[3] = applyTint(argb[3] & 0xFF, tint);
        }

        try {
            String temp = ColorConverterUtil.toRGBA(argb);
            sb.append(temp);
        } catch (NumberFormatException nfe) {
            LOGGER.log(Level.FINE, nfe.getMessage() + " " + nfe.getCause(), nfe);
            sb.append(String
                    .format("#%02x%02x%02x;", argb[1], argb[2], argb[3]));
        }

        return sb.toString();
    }

    @Override
    public String getBorderColorCSS(BorderSide borderSide, String attr, BorderFormatting format) {

        switch (borderSide) {
            case BOTTOM:
                return getBorderColorCSS(attr, XSSFColor.toXSSFColor(format.getBottomBorderColorColor()));
            case LEFT:
                return getBorderColorCSS(attr, XSSFColor.toXSSFColor(format.getLeftBorderColorColor()));
            case RIGHT:
                return getBorderColorCSS(attr, XSSFColor.toXSSFColor(format.getRightBorderColorColor()));
            case TOP:
                return getBorderColorCSS(attr, XSSFColor.toXSSFColor(format.getTopBorderColorColor()));
            default:
                return ""; // unused, but needed for compilation
        }
    }

    private CTColor getBorderColor(XSSFBorderFormatting casted,
            BorderSide borderSide) {

        // No POI API exists for this, but the data exists in the underlying
        // implementation.

        Field declaredField = null;
        try {
            declaredField = casted.getClass().getDeclaredField("_border");
            declaredField.setAccessible(true);
            CTBorder object = (CTBorder) declaredField.get(casted);
            switch (borderSide) {
            case BOTTOM:
                return object.getBottom().getColor();
            case LEFT:
                return object.getLeft().getColor();
            case RIGHT:
                return object.getRight().getColor();
            case TOP:
                return object.getTop().getColor();

            default:
                break;
            }
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            LOGGER.log(Level.SEVERE, "Incompatible POI implementation; unable to parse border color", e);
        } finally {
            if (declaredField != null) {
                declaredField.setAccessible(false);
            }
        }
        return null;
    }

    @Override
    public void defaultColorStyles(CellStyle cellStyle, StringBuilder sb) {
        XSSFCellStyle cs = (XSSFCellStyle) cellStyle;
        XSSFColor fillForegroundColorColor = cs.getFillForegroundColorColor();
        XSSFColor fillBackgroundColorColor = cs.getFillBackgroundColorColor();
        defaultBackgroundColor = styleColor(fillForegroundColorColor);
        defaultBackgroundColor = styleColor(fillBackgroundColorColor);

        if (defaultBackgroundColor == null) {
            defaultBackgroundColor = "rgba(255,255,255,1.0);";
        }
        sb.append("background-color:");
        sb.append(defaultBackgroundColor);

        XSSFColor xssfColor = cs.getFont().getXSSFColor();
        defaultColor = styleColor(xssfColor);

        if (defaultColor == null) {
            defaultColor = "rgba(0,0,0,1.0);";
        }
        sb.append("color:");
        sb.append(defaultColor);

    }

    @Override
    public boolean hasBackgroundColor(CellStyle cellStyle) {
        XSSFCellStyle cs = (XSSFCellStyle) cellStyle;
        XSSFColor fillBackgroundXSSFColor = cs.getFillBackgroundXSSFColor();
        XSSFColor fillForegroundXSSFColor = cs.getFillForegroundXSSFColor();
        if (fillForegroundXSSFColor != null
                && !fillForegroundXSSFColor.isAuto()) {
            return true;
        } else if (fillBackgroundXSSFColor != null
                && !fillBackgroundXSSFColor.isAuto()) {
            return true;
        } else {
            // bypass POI API and try to get the fill ourself, because of bug:
            // https://issues.apache.org/bugzilla/show_bug.cgi?id=53262
            try {
                XSSFColor themeColor = getFillColor(cs);
                if (themeColor != null && !themeColor.isAuto()) {
                    return true;
                }
            } catch (Exception e) {
                LOGGER.log(Level.FINE, e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * @param styleProvider general interface to support conditional format rules and table styles
     * @return CSS color string, or null if none found
     */
    public String getBackgroundColorCSS(DifferentialStyleProvider styleProvider) {
        PatternFormatting fillFmt = styleProvider.getPatternFormatting();
        if (fillFmt == null) return null;

        XSSFColor color = (XSSFColor) fillFmt.getFillBackgroundColorColor();

        return getColorCSS(color);
    }

    @Override
    public String getBackgroundColorCSS(ConditionalFormattingRule rule) {
        return getBackgroundColorCSS((DifferentialStyleProvider) rule);
    }

    /**
     * @param styleProvider
     * @return CSS or null
     */
    public String getFontColorCSS(DifferentialStyleProvider styleProvider) {

        FontFormatting font = styleProvider.getFontFormatting();

        if (font == null) return null;

        XSSFColor color = (XSSFColor) font.getFontColor();

        return getColorCSS(color);
    }

    @Override
    public String getFontColorCSS(ConditionalFormattingRule rule) {
        return getFontColorCSS((DifferentialStyleProvider) rule);
    }

    /**
     * @param color
     * @return CSS or null
     */
    public String getColorCSS(XSSFColor color) {
        if (color == null || color.getCTColor() == null) return null;

        if (color.isThemed()) {
            XSSFColor themeColor = workbook.getTheme().getThemeColor(color.getTheme());
            // apply tint from the style, it isn't in the theme.
            return styleColor(themeColor, color.getTint());
        } else {
            byte[] rgb = color.getARGB();
            return rgb == null ? null : ColorConverterUtil.toRGBA(rgb);
        }
    }

    private XSSFColor getFillColor(XSSFCellStyle cs) {
        final CTXf _cellXf = cs.getCoreXf();
        int fillIndex = (int) _cellXf.getFillId();
        XSSFCellFill fg = workbook.getStylesSource().getFillAt(fillIndex);

        ThemesTable _theme = workbook.getTheme();
        XSSFColor fillForegroundColor = fg.getFillForegroundColor();
        if (fillForegroundColor != null && _theme != null) {
            _theme.inheritFromThemeAsRequired(fillForegroundColor);
        }
        XSSFColor fillBackgroundColor = fg.getFillBackgroundColor();
        if (fillForegroundColor == null) {
            if (fillBackgroundColor != null && _theme != null) {
                _theme.inheritFromThemeAsRequired(fillBackgroundColor);
            }
            return fillBackgroundColor;
        } else {
            return fillForegroundColor;
        }
    }

    private XSSFColor getBorderColor(XSSFCellStyle cs, BorderSide borderSide) {
        int idx = (int) cs.getCoreXf().getBorderId();
        XSSFCellBorder border = workbook.getStylesSource().getBorderAt(idx);

        return border.getBorderColor(borderSide);
    }

    protected String styleColor(XSSFColor color) {
        return styleColor(color, color == null ? 0.0 : color.getTint());
    }

    protected String styleColor(XSSFColor color, double tint) {
        if (color == null || color.isAuto()) {
            return null;
        }

        // either an explicitly stored color, or a default or custom indexed color
        byte[] argb = color.getARGB();
        if (argb == null) {
            return null;
        }

        if (tint != 0.0) {
            argb[1] = applyTint(argb[1] & 0xFF, tint);
            argb[2] = applyTint(argb[2] & 0xFF, tint);
            argb[3] = applyTint(argb[3] & 0xFF, tint);
        }

        try {
            String temp = ColorConverterUtil.toRGBA(argb);
            return temp;
        } catch (NumberFormatException nfe) {
            LOGGER.log(Level.FINEST, nfe.getMessage() + " " + nfe.getCause(), nfe);
            return String.format("#%02x%02x%02x;", argb[1], argb[2], argb[3]);
        }
    }

    private byte applyTint(int lum, double tint) {
        if (tint > 0) {
            return (byte) (lum * (1.0 - tint) + (255 - 255 * (1.0 - tint)));
        } else if (tint < 0) {
            return (byte) (lum * (1 + tint));
        } else {
            return (byte) lum;
        }
    }

}