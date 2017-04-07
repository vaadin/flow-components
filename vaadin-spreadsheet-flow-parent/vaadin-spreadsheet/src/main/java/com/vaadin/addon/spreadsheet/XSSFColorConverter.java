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
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.BorderFormatting;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.xssf.model.ThemesTable;
import org.apache.poi.xssf.usermodel.XSSFBorderFormatting;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfRule;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
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
                LOGGER.log(Level.FINEST, e.getMessage(), e);
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
        if (color.isIndexed() && ColorConverterUtil
            .hasCustomIndexedColors(workbook)) {
            sb.append(ColorConverterUtil.getIndexedARGB(workbook,color));
            return sb.toString();
        }
        if (color == null || color.isAuto()) {
            sb.append("#000;");
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

    @Override
    public String getBorderColorCSS(BorderSide borderSide, String attr,
            BorderFormatting format) {

        XSSFBorderFormatting casted = (XSSFBorderFormatting) format;

        // getXBorderColor methods are useless with XSSF, so we need to dig
        // deeper.
        CTColor color = getBorderColor(casted, borderSide);

        StringBuilder sb = new StringBuilder();

        sb.append(attr);
        sb.append(":");
        if (color == null || color.getAuto()) {
            sb.append("#000;");
            return sb.toString();
        }

        byte[] argb;
        if (color.isSetTheme()) {
            XSSFColor themeColor = workbook.getTheme().getThemeColor(
                    (int) color.getTheme());
            argb = themeColor.getARGB();
        } else {
            argb = color.getRgb();
        }

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
        } catch (IllegalArgumentException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Incompatible POI implementation; unable to parse border color",
                    e);
        } catch (IllegalAccessException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Incompatible POI implementation; unable to parse border color",
                    e);
        } catch (NoSuchFieldException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Incompatible POI implementation; unable to parse border color",
                    e);
        } catch (SecurityException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Incompatible POI implementation; unable to parse border color",
                    e);
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
                LOGGER.log(Level.FINEST, e.getMessage(), e);
            }
        }
        return false;
    }

    @Override
    public String getBackgroundColorCSS(ConditionalFormattingRule rule) {

        XSSFConditionalFormattingRule r = (XSSFConditionalFormattingRule) rule;
        CTDxf dxf = getXMLColorDataWithReflection(r);

        CTColor bgColor = dxf.getFill().getPatternFill().getBgColor();

        if (bgColor.isSetTheme()) {
            XSSFColor themeColor = workbook.getTheme().getThemeColor(
                    (int) bgColor.getTheme());

            // CF rules have tint in bgColor but not the XSSFColor.
            return styleColor(themeColor, bgColor.getTint());
        } else {
            byte[] rgb = bgColor.getRgb();
            return rgb == null ? null : ColorConverterUtil.toRGBA(rgb);
        }

    }

    @Override
    public String getFontColorCSS(ConditionalFormattingRule rule) {

        XSSFConditionalFormattingRule r = (XSSFConditionalFormattingRule) rule;

        CTDxf dxf = getXMLColorDataWithReflection(r);
        CTFont font = dxf.getFont();

        if (font.getColorList() == null || font.getColorList().isEmpty()) {
            // default color
            return null;
        }

        CTColor ctColor = font.getColorList().get(0);

        if (ctColor.isSetTheme()) {
            XSSFColor themeColor = workbook.getTheme().getThemeColor(
                    (int) ctColor.getTheme());

            return styleColor(themeColor, ctColor.getTint());
        } else {
            byte[] rgb = ctColor.getRgb();
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

    private String styleColor(XSSFColor color) {
        return styleColor(color, color == null ? 0.0 : color.getTint());
    }

    private String styleColor(XSSFColor color, double tint) {
        if (color == null || color.isAuto()) {
            return null;
        }
        //the XSSFColor#getARGB() method returns wrong colors for custom indexed colors
        // to be removed when bug # 60898 is resolved (https://bz.apache.org/bugzilla/show_bug.cgi?id=60898)
        if (color.isIndexed() && ColorConverterUtil
            .hasCustomIndexedColors(workbook)) {
            return ColorConverterUtil.getIndexedARGB(workbook, color);
        }

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
            LOGGER.log(Level.FINE, nfe.getMessage() + " " + nfe.getCause(), nfe);
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

    /**
     * XSSF doesn't have an API to get this value, so brute force it is..
     * 
     * @param rule
     *            The rule that has color data defined
     * @return OpenXML data format that contains the real defined styles
     */
    private CTDxf getXMLColorDataWithReflection(
            XSSFConditionalFormattingRule rule) {
        CTCfRule realRule = null;

        Method declaredMethod = null;
        try {
            declaredMethod = rule.getClass().getDeclaredMethod("getCTCfRule");
            declaredMethod.setAccessible(true);
            realRule = (CTCfRule) declaredMethod.invoke(rule);
        } catch (Exception e) {
            LOGGER.fine(e.getMessage());
        } finally {
            if (declaredMethod != null) {
                declaredMethod.setAccessible(false);
            }
        }

        CTDxf dxf = workbook.getStylesSource().getCTStylesheet().getDxfs()
                .getDxfArray((int) realRule.getDxfId());

        return dxf;
    }

}